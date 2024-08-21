package noppes.npcs.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLongArray;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.wrapper.WorldWrapper;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.ClientScriptData;
import noppes.npcs.controllers.data.EncryptData;
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.NpcScriptData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.PotionScriptData;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.TempFile;

public class ScriptController {

	public static boolean HasStart = false;
	public static ScriptController Instance;

	public static boolean hasGraalLib() {
		Class<?> graal = null;
		try {
			graal = Class.forName("com.oracle.truffle.js.scriptengine.GraalJSScriptEngine");
			return true;
		}
		catch (Exception e) { LogWriter.info("GraalJS is missing: "+graal); }
		return false;
	}

    public boolean isLoad = false;
	public boolean shouldSave = false;
	public long lastLoaded = 0L;
	public long lastPlayerUpdate = 0L;
	public NBTTagCompound compound = new NBTTagCompound();
	public NBTTagCompound constants = new NBTTagCompound();
	public File dir, clientDir;
	
	public final Map<String, ScriptEngineFactory> factories = Maps.newTreeMap();
	public final Map<String, String> languages = Maps.newTreeMap();
	public final Map<String, Long> sizes = Maps.newTreeMap();
	public final Map<String, Long> clientSizes = Maps.newTreeMap();
	public final Map<String, String> scripts = Maps.newTreeMap();
	public final Map<String, String> clients = Maps.newTreeMap();

	public EncryptData encryptData;
	
	public ForgeScriptData forgeScripts = new ForgeScriptData();
	public ClientScriptData clientScripts = new ClientScriptData();
	public PlayerScriptData playerScripts = new PlayerScriptData(null);
	public PotionScriptData potionScripts = new PotionScriptData();
	public NpcScriptData npcsScripts = new NpcScriptData();

	public ScriptController() {
		ScriptController.Instance = this;
		if (!CustomNpcs.NashorArguments.isEmpty()) {
			System.setProperty("nashorn.args", CustomNpcs.NashorArguments);
		}
        ScriptEngineManager manager = new ScriptEngineManager();
        try {
			if (manager.getEngineByName("ecmascript") == null) {
				LogWriter.debug("Try create Nashorn Script Engine");
				Launch.classLoader.addClassLoaderExclusion("jdk.nashorn.");
				Launch.classLoader.addClassLoaderExclusion("jdk.internal.dynalink");
				Class<?> c = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
				ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
				factory.getScriptEngine();
				try {
					Class<?> require = Class.forName("com.coveo.nashorn_modules.Require");
					Method methodEnable = require.getMethod("enable");
					methodEnable.invoke(factory, CustomNpcs.getWorldSaveDirectory("scripts/common_js"));
				}
				catch (Exception e) { LogWriter.info("Kotlin Require is missed:"); }
				manager.registerEngineName("ecmascript", factory);
				manager.registerEngineExtension("js", factory);
				manager.registerEngineMimeType("application/ecmascript", factory);
				this.languages.put(AdditionalMethods.instance.deleteColor(factory.getLanguageName()), ".js");
				this.factories.put(factory.getLanguageName().toLowerCase(), factory);
			}
		} catch (Exception t) { LogWriter.info("Kotlin JS is missed:"); }
		try {
			Class<?> c = Class.forName("org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory");
			ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
			factory.getScriptEngine();
			manager.registerEngineName("kotlin", factory);
			manager.registerEngineExtension("ktl", factory);
			manager.registerEngineMimeType("application/kotlin", factory);
			this.languages.put(AdditionalMethods.instance.deleteColor(factory.getLanguageName()), ".ktl");
			this.factories.put(factory.getLanguageName().toLowerCase(), factory);
		}
		catch (Exception e) { LogWriter.info("Kotlin JS is missed:"); }
		LogWriter.info("Script Engines Available:");
		for (ScriptEngineFactory fac : manager.getEngineFactories()) {
			try {
				LogWriter.debug("Found script Library: \"" + fac.getLanguageName() + "\"; type: \"" + fac.getClass().getSimpleName() + "\"");
				if (fac.getExtensions().isEmpty()) {
					LogWriter.debug("Library: \"" + fac.getLanguageName() + "\"; type: \"" + fac.getClass().getSimpleName() + "\" Extensions isEmpty ");
					continue;
				}
				if (!(fac.getScriptEngine() instanceof Invocable) && !fac.getLanguageName().equals("lua")) {
					LogWriter.debug("Library: \"" + fac.getLanguageName() + "\"; type: \"" + fac.getClass().getSimpleName() + "\" Engine is not Invocable or not Lua");
					continue;
				}
				String ext = "." + fac.getExtensions().get(0).toLowerCase();
				LogWriter.info("Added script Library: \"" + fac.getLanguageName() + "\"; type: \"" + fac.getClass().getSimpleName() + "\"; files index: \"" + ext + "\"");
				this.languages.put(AdditionalMethods.instance.deleteColor(fac.getLanguageName()), ext);
				this.factories.put(fac.getLanguageName().toLowerCase(), fac);
			} catch (Throwable t3) {
				LogWriter.error("Error Added Script Library: \"" + fac.getLanguageName() + "\": " + t3);
			}
		}
	}

	public synchronized void encrypt() {
		/* the code is hidden because it uses a CustomNpcs.ScriptPassword */
	}
	
	private File clientScriptsFile() {
		return new File(this.dir, "client_scripts.json");
	}

	private File constantScriptsFile() {
		return new File(this.dir, "constant_scripts.json");
	}

	private File npcsScriptsFile() {
		return new File(this.dir, "npc_scripts.json");
	}
	
	private File forgeScriptsFile() {
		return new File(this.dir, "forge_scripts.json");
	}

	private File playerScriptsFile() {
		return new File(this.dir, "player_scripts.json");
	}

	private File potionScriptsFile() {
		return new File(this.dir, "potion_scripts.json");
	}

	private File worldDataFile() {
		return new File(this.dir, "world_data.json");
	}
	
	public ScriptEngine getEngineByName(String language) {
		if (language.equalsIgnoreCase("ECMAScript") && ScriptController.hasGraalLib()) {
			return this.getNewGraalEngine();
		}
		ScriptEngineFactory fac = this.factories.get(AdditionalMethods.instance.deleteColor(language).toLowerCase());
		if (fac == null) {
			return null;
		}
		return fac.getScriptEngine();
	}

	private ScriptEngine getNewGraalEngine() {
		try {
			Class<?> graal = Class.forName("com.oracle.truffle.js.scriptengine.GraalJSScriptEngine");
			Method create = null;
			for (Method m : graal.getMethods()) {
				if (m.getName().equals("create") && m.getParameterCount() == 2) {
					create = m;
					break;
				}
			}
			Class<?> cnt = Class.forName("org.graalvm.polyglot.Context");
			Class<?> hostA = Class.forName("org.graalvm.polyglot.HostAccess");
			Object contextBuilder; // org.graalvm.polyglot.Context.Builder
			contextBuilder = cnt.getDeclaredMethod("newBuilder", String[].class).invoke(cnt,
					(Object) new String[] { "js" });
			if (contextBuilder != null) {
				for (Method m : contextBuilder.getClass().getDeclaredMethods()) {
					switch (m.getName()) {
					case "allowExperimentalOptions":
                        case "allowHostClassLoading":
                        case "allowNativeAccess":
                        case "allowIO":
                        case "allowCreateProcess":
                            contextBuilder = m.invoke(contextBuilder, true);
						break;
					case "allowHostClassLookup":
						contextBuilder = m.invoke(contextBuilder, (Predicate<String>) (s -> true));
						break;
                        case "allowHostAccess": {
						if (m.getParameters()[0].getType() == Boolean.class
								|| m.getParameters()[0].getType() == boolean.class) {
							continue;
						}
						Field f = hostA.getDeclaredField("ALL");
						Method nb = hostA.getMethod("newBuilder", f.getType());
						Object hostAccessBuilder = nb.invoke(hostA, f.get(hostA)); // org.graalvm.polyglot.HostAccess
						Method ttm = null, b = null;
						for (Method d : hostAccessBuilder.getClass().getMethods()) {
							if (d.getName().equals("targetTypeMapping") && d.getParameterCount() == 4) {
								ttm = d;
							}
							if (d.getName().equals("build") && d.getParameterCount() == 0) {
								b = d;
							}
						}
						// Double to
                        if (ttm != null) {
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Byte.class, null, (Function<Double, Byte>) (Double::byteValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Float.class, null, (Function<Double, Float>) (Double::floatValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Integer.class, null, (Function<Double, Integer>) (Double::intValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Long.class, null, (Function<Double, Long>) (Double::longValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, String.class, null, (Function<Double, String>) (Object::toString));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Boolean.class, null, (Function<Double, Boolean>) (n -> n != 0.0d));
							// Float to
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, Byte.class, null, (Function<Float, Byte>) (Float::byteValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, Double.class, null, (Function<Float, Double>) (Float::doubleValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, Integer.class, null, (Function<Float, Integer>) (Float::intValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, Long.class, null, (Function<Float, Long>) (Float::longValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, String.class, null, (Function<Float, String>) (Object::toString));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Float.class, Boolean.class, null, (Function<Float, Boolean>) (n -> n != 0.0f));
							// Integer to
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, Byte.class, null, (Function<Integer, Byte>) (Integer::byteValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, Double.class, null, (Function<Integer, Double>) (Integer::doubleValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, Float.class, null, (Function<Integer, Float>) (Integer::floatValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, Long.class, null, (Function<Integer, Long>) (Integer::longValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, String.class, null, (Function<Integer, String>) (Object::toString));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Integer.class, Boolean.class, null, (Function<Integer, Boolean>) (n -> n != 0));
							// Long to
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Double.class, Byte.class, null, (Function<Long, Byte>) (Long::byteValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Long.class, Double.class, null, (Function<Long, Double>) (Long::doubleValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Long.class, Float.class, null, (Function<Long, Float>) (Long::floatValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Long.class, Integer.class, null, (Function<Long, Integer>) (Long::intValue));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Long.class, String.class, null, (Function<Long, String>) (Object::toString));
							hostAccessBuilder = ttm.invoke(hostAccessBuilder, Long.class, Boolean.class, null, (Function<Long, Boolean>) (n -> n != 0L));
							if (b != null) { hostAccessBuilder = b.invoke(hostAccessBuilder); }
							// invoke to main
							contextBuilder = m.invoke(contextBuilder, hostAccessBuilder);
						}
						break;
					}
                        case "allowAllAccess": {
						contextBuilder = m.invoke(contextBuilder, true);
						break;
					}
                        case "option": {
						contextBuilder = m.invoke(contextBuilder, "js.ecmascript-version", "2022");
						contextBuilder = m.invoke(contextBuilder, "js.nashorn-compat", "true");
						break;
					}
					default:
                    }
				}
                if (create == null) { return null; }
                return (ScriptEngine) create.invoke(graal, null, contextBuilder);
			}
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
		return null;
	}

	private List<String> getScripts(String language, boolean isClient) {
		List<String> list = new ArrayList<>();
		String ext = this.languages.get(AdditionalMethods.instance.deleteColor(language));
		if (ext == null) { return list; }
		for (String script : (isClient ? this.clients : this.scripts).keySet()) {
			if (script.endsWith(ext) || script.endsWith(ext.replace(".", ".p"))) {
				list.add(script);
			}
		}
		return list;
	}

	public void load() {
		ScriptController sData = ScriptController.Instance;
		sData.loadCategories();
		sData.loadStoredData();
		sData.loadPlayerScripts();
		sData.loadForgeScripts();
		sData.loadNPCsScripts();
		sData.loadClientScripts();
		sData.loadPotionScripts();
		sData.loadConstantData();
		ScriptController.HasStart = true;
	}

	public void loadCategories() {
		this.dir = new File(CustomNpcs.getWorldSaveDirectory(), "scripts");
		if (!this.dir.exists()) {
			this.dir.mkdirs();
		}
		this.clientDir = new File(this.dir, "client");
		if (!this.clientDir.exists()) {
			this.clientDir.mkdirs();
		}

		if (!this.worldDataFile().exists()) {
			this.shouldSave = true;
		}
		WorldWrapper.tempData.clear();
		this.scripts.clear();
		this.sizes.clear();
		for (String key : this.clients.keySet()) {
			CommonProxy.loadFiles.remove(key);
		}
		this.clients.clear();
		this.clientSizes.clear();
		for (String language : this.languages.keySet()) {
			String ext = this.languages.get(AdditionalMethods.instance.deleteColor(language));
			File scriptDir = new File(this.dir, language.toLowerCase());
			if (!scriptDir.exists()) {
				scriptDir.mkdir();
			} else {
				this.loadDir(scriptDir, "", ext, false);
				this.loadDir(scriptDir, "", ext.replace(".", ".p"), false);
			}

			scriptDir = new File(this.clientDir, language.toLowerCase());
			if (!scriptDir.exists()) {
				scriptDir.mkdir();
			} else {
				this.loadDir(scriptDir, "", ext, true);
				this.loadDir(scriptDir, "", ext.replace(".", ".p"), true);
			}
		}
		this.lastLoaded = System.currentTimeMillis();
		this.isLoad = true;
	}

	public boolean loadClientScripts() {
		this.clientScripts.clear();
		File file = this.clientScriptsFile();
		try {
			if (!file.exists()) { return false; }
			this.clientScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	public boolean loadConstantData() {
		this.constants = new NBTTagCompound();
		File file = this.constantScriptsFile();
		boolean isLoad = true;
		try {
			if (file.exists()) {
				this.constants = NBTJsonUtil.LoadFile(file);
			} else {
				NBTTagCompound nbtC = new NBTTagCompound();
				nbtC.setInteger("value", 0);
				nbtC.setString("sData", "Java.type(\"noppes.npcs.api.NpcAPI\").Instance().getIWorld(0).getStoreddata()");
				nbtC.setString("tData", "Java.type(\"noppes.npcs.api.NpcAPI\").Instance().getIWorld(0).getTempdata()");
				nbtC.setString("System", "Java.type(\"java.lang.System\")");
				nbtC.setString("JLists", "Java.type(\"com.google.common.collect.Lists\")");
				nbtC.setString("JList", "Java.type(\"java.util.ArrayList\")");
				nbtC.setString("JClt", "Java.type(\"java.util.Collections\")");
				nbtC.setString("Juuid", "Java.type(\"java.util.UUID\")");
				nbtC.setString("JHMap", "Java.type(\"java.util.HashMap\")");
				nbtC.setString("JHSet", "Java.type(\"java.util.HashSet\")");
				nbtC.setString("JFiles", "Java.type(\"java.nio.file.Files\")");
				nbtC.setString("JioF", "Java.type(\"java.io.File\")");
				nbtC.setString("JioFOS", "Java.type(\"java.io.FileOutputStream\")");
				nbtC.setString("JioFIS", "Java.type(\"java.io.FileInputStream\")");
				nbtC.setString("JStr", "Java.type(\"java.lang.String\")");
				nbtC.setString("JSArr", "Java.type(\"java.lang.String[]\")");
				nbtC.setString("JSToNbt", "Java.type(\"net.minecraft.nbt.JsonToNBT\")");
				nbtC.setString("JnbtB", "Java.type(\"net.minecraft.nbt.NBTTagByte\")");
				nbtC.setString("JnbtI", "Java.type(\"net.minecraft.nbt.NBTTagInt\")");
				nbtC.setString("JnbtF", "Java.type(\"net.minecraft.nbt.NBTTagFloat\")");
				nbtC.setString("JnbtD", "Java.type(\"net.minecraft.nbt.NBTTagDouble\")");
				nbtC.setString("JnbtBA", "Java.type(\"net.minecraft.nbt.NBTTagByteArray\")");
				nbtC.setString("JnbtS", "Java.type(\"net.minecraft.nbt.NBTTagString\")");
				nbtC.setString("JnbtL", "Java.type(\"net.minecraft.nbt.NBTTagList\")");
				nbtC.setString("Jnbt", "Java.type(\"net.minecraft.nbt.NBTTagCompound\")");
				nbtC.setString("JnbtIA", "Java.type(\"net.minecraft.nbt.NBTTagIntArray\")");
				nbtC.setString("JCST", "Java.type(\"net.minecraft.nbt.CompressedStreamTools\")");
				nbtC.setString("JEnPart", "Java.type(\"net.minecraft.util.EnumParticleTypes\")");
				nbtC.setString("JComSr", "Java.type(\"net.minecraft.util.text.ITextComponent.Serializer\")");
				nbtC.setString("JTCs", "Java.type(\"net.minecraft.util.text.TextComponentTranslation\")");
				nbtC.setString("JTC", "Java.type(\"net.minecraft.util.text.TextComponentString\")");
				nbtC.setString("JItB", "Java.type(\"net.minecraft.util.text.TextComponentSelector\")");
				nbtC.setString("JStl", "Java.type(\"net.minecraft.util.text.Style\")");
				nbtC.setString("JECe", "Java.type(\"net.minecraft.util.text.event.ClickEvent\")");
				nbtC.setString("JEHe", "Java.type(\"net.minecraft.util.text.event.HoverEvent\")");
				nbtC.setString("JCa", "Java.type(\"net.minecraft.util.text.event.ClickEvent.Action\")");
				nbtC.setString("JHa", "Java.type(\"net.minecraft.util.text.event.HoverEvent.Action\")");
				nbtC.setString("JBPos", "Java.type(\"net.minecraft.util.math.BlockPos\")");
				nbtC.setString("JItem", "Java.type(\"net.minecraft.item.Item\")");
				nbtC.setString("JItemS", "Java.type(\"net.minecraft.item.ItemStack\")");
				nbtC.setString("JResloc", "Java.type(\"net.minecraft.util.ResourceLocation\")");
				nbtC.setString("JAmod", "Java.type(\"net.minecraft.entity.ai.attributes.AttributeModifier\")");
				nbtC.setString("JRAtr", "Java.type(\"net.minecraft.entity.ai.attributes.RangedAttribute\")");
				nbtC.setString("JSAtr", "Java.type(\"net.minecraft.entity.SharedMonsterAttributes\")");
				nbtC.setString("JIvnB", "Java.type(\"net.minecraft.inventory.InventoryBasic\")");
				nbtC.setString("JPEff", "Java.type(\"net.minecraft.potion.PotionEffect\")");
				nbtC.setString("Jreg", "Java.type(\"net.minecraftforge.fml.common.registry.ForgeRegistries\")");
				nbtC.setString("Jobf", "Java.type(\"net.minecraftforge.fml.common.ObfuscationReflectionHelper\")");
				nbtC.setString("Nsrts", "Java.type(\"noppes.npcs.controllers.ScriptController\").Instance");
				nbtC.setString("NSc", "Java.type(\"noppes.npcs.controllers.ScriptContainer\")");
				nbtC.setString("NTrP", "Java.type(\"noppes.npcs.controllers.TransportController\")");
				NBTTagList list = new NBTTagList();
				list.appendTag(new NBTTagString("function getField(key,object) { try { var f = dump(object).getField(key); if (f) { return f.getValue(); } } catch (error) { log('Error: \"'+key+'\" is not a Field or not found in \"'+object.getClass().getName()+'\"');} return null; }"));
				list.appendTag(new NBTTagString("function setField(value,object,key) { try { var f = dump(object).getField(key); if (f) { return f.setValue(value); } } catch (error) { log('Error: \"'+key+'\" is not a Field or not found, or not type mismatch in \"'+object.getClass().getName()+'\". Error: ' + error); } return false; }"));
				list.appendTag(new NBTTagString("function invoke(value,object,key) { try { var m = dump(object).getMethod(key); if (m) { var jo = Java.type('java.lang.Object[]'); if (value!=jo) { try { if (value.length>=0) { var v = new jo(value.length); for (var i=0; i<value.length; i++) { v[i] = value[i]; } return m.invoke(v); } } catch (err) { } var v = new jo(1); v[0] = value; return m.invoke(v); } else { return m.invoke(value); } } } catch (error) { log('Error: \"'+key+'\" is not a Method or not found, or not type mismatch in \"'+object.getClass().getName()+'\"'); } return null; }"));
				list.appendTag(new NBTTagString("function getCustomFunction(name, ev) {var fhm;try {var actor=\"Any\";if (ev) {if (ev.player) { actor = \"Player\"; }else if (ev.npc) { actor = \"NPC\"; }else if (ev.block) { actor = \"Block\"; }};fhm = api.getIWorld(0).getTempdata().get(\"functions\");if (fhm instanceof JHMap && fhm.containsKey(name)) {return fhm.get(name)};if (name!=\"loadFile\") {var dir = existsDir(api.getWorldDir().toPath().resolve(\"data\").resolve(\"functions\"));gFunc(\"loadFile\",ev)(dir.resolve(name+\".json\"), \"fhm\");if (fhm instanceof JHMap && fhm.containsKey(name)) {return fhm.get(name)}}} catch (error) {if (fhm && fhm instanceof JHMap) {gFunc(\"errorMes\",ev)(actor, error, \"Name: §f\"+name, ev);}};return eval(\"function fnull(a,b,c,d,e,f,g,h,i,k,l,m,n,o,p,r,s,t,q,v) {return;}\");}"));
				this.constants.setTag("Constants", nbtC);
				this.constants.setTag("Functions", list);
				try {
					NBTJsonUtil.SaveFile(this.constantScriptsFile(), this.constants.copy());
					isLoad = false;
				} catch (Exception e) {
					LogWriter.except(e);
				}
			}
			ScriptContainer.reloadConstants();
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return isLoad;
	}

	public boolean loadNPCsScripts() {
		this.npcsScripts.clear();
		File file = this.npcsScriptsFile();
		try {
			if (!file.exists()) { return false; }
			this.npcsScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}
	
	public boolean loadForgeScripts() {
		this.forgeScripts.clear();
		File file = this.forgeScriptsFile();
		try {
			if (!file.exists()) {
				return false;
			}
			this.forgeScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	public void loadItemTextures() {
		ItemScripted.Resources.clear();
		File file = new File(this.dir, "item_models.dat");
		if (!file.exists()) {
			return;
		}
		try {
			NBTTagCompound compound = CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
			for (NBTBase nbt : compound.getTagList("Models", 10)) {
				ItemScripted.Resources.put(((NBTTagCompound) nbt).getInteger("meta"),
						((NBTTagCompound) nbt).getString("model"));
			}
			CustomNpcs.proxy.reloadItemTextures();
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public boolean loadPlayerScripts() {
		this.playerScripts.clear();
		File file = this.playerScriptsFile();
		try {
			if (!file.exists()) {
				return false;
			}
			this.playerScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	public boolean loadPotionScripts() {
		this.potionScripts.clear();
		File file = this.potionScriptsFile();
		try {
			if (!file.exists()) {
				return false;
			}
			this.potionScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	public boolean loadStoredData() {
		this.compound = new NBTTagCompound();
		File file = this.worldDataFile();
		boolean isLoad = true;
		try {
			if (file.exists()) {
				this.compound = NBTJsonUtil.LoadFile(file);
				this.shouldSave = false;
			} else {
				isLoad = false;
			}
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return isLoad;
	}

	public void loadDir(File dir, String name, String ext, boolean isClient) {
		for (File file : Objects.requireNonNull(dir.listFiles())) {
			String filename = name + file.getName().toLowerCase();
			if (file.isDirectory()) {
				this.loadDir(file, filename + "/", ext, isClient);
			} else if (filename.endsWith(ext)) {
				try {
					String code = this.readFile(file);
					if (isClient) {
						this.clients.put(filename, code);
						this.clientSizes.put(filename, file.length());
					} else {
						this.scripts.put(filename, code);
						this.sizes.put(filename, file.length());
					}
				} catch (IOException e) { LogWriter.error("Error:", e); }
			}
		}
	}
	
	public NBTTagList nbtLanguages(boolean isClient) {
		NBTTagList list = new NBTTagList();
		for (String language : this.languages.keySet()) {
			String ext = this.languages.get(AdditionalMethods.instance.deleteColor(language));
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList scripts = new NBTTagList();
			for (String script : this.getScripts(language, isClient)) {
				scripts.appendTag(new NBTTagString(script));
			}
			compound.setTag("Scripts", scripts);
			compound.setString("Language", language);
			compound.setString("FileSfx", ext);
			long[] sizes = new long[scripts.tagCount()];
			int i = 0;
			for (Long l : (isClient ? this.clientSizes : this.sizes).values()) {
				sizes[i] = l;
				if (!scripts.getStringTagAt(i).endsWith(ext)) {
					sizes[i] *= -1;
				}
				i++;
			}
			compound.setTag("sizes", new NBTTagLongArray(sizes));
			list.appendTag(compound);
		}
		return list;
	}

	private String readFile(File file) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(Files.newInputStream(file.toPath()), StandardCharsets.UTF_8))) {
            StringBuilder sb = new StringBuilder();
            for (String line = br.readLine(); line != null; line = br.readLine()) {
                sb.append(line);
                sb.append("\n");
            }
            return sb.toString();
        }
	}

	public void saveItemTextures() {
		File file = new File(this.dir, "item_models.dat");
		if (!file.exists()) {
			try {
				file.createNewFile();
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (!file.exists()) {
			return;
		}
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (int meta : ItemScripted.Resources.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger("meta", meta);
			nbt.setString("model", ItemScripted.Resources.get(meta));
			list.appendTag(nbt);
		}
		compound.setTag("Models", list);
		try {
			CompressedStreamTools.writeCompressed(compound, Files.newOutputStream(file.toPath()));
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	@SubscribeEvent
	public void saveWorld(WorldEvent.Save event) {
		if (!this.shouldSave || event.getWorld().isRemote || event.getWorld() != Objects.requireNonNull(event.getWorld().getMinecraftServer()).worlds[0]) {
			return;
		}
		try {
			NBTJsonUtil.SaveFile(this.worldDataFile(), this.compound.copy());
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
			NBTJsonUtil.SaveFile(this.constantScriptsFile(), this.constants.copy());
		} catch (Exception e) {
			LogWriter.except(e);
		}
		this.shouldSave = false;
	}

	public void sendClientTo(EntityPlayerMP player) {
		NBTTagCompound compound = new NBTTagCompound();
		this.clientScripts.writeToNBT(compound);
		Server.sendData(player, EnumPacketClient.SCRIPT_CLIENT, compound);
		NBTTagList list = new NBTTagList();
		for (String key : this.clients.keySet()) {
			if (!CommonProxy.loadFiles.containsKey(key)) {
				CommonProxy.loadFiles.put(key, new TempFile(key, 0, 1, this.clientSizes.get(key)));
			}
			TempFile file = CommonProxy.loadFiles.get(key);
			if (!file.isLoad()) {
				file.size = -1;
				file.saveType = 1;
				file.reset(this.clients.get(key));
			}
			list.appendTag(file.getTitle());
		}
		compound = new NBTTagCompound();
		compound.setTag("FileList", list);
		Server.sendData(player, EnumPacketClient.SEND_FILE_LIST, compound);
	}

	public void setClientScripts(NBTTagCompound compound) {
		this.clientScripts.readFromNBT(compound);
		File file = this.clientScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.clientScripts.lastInited = -1L;
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void setNPCsScripts(NBTTagCompound compound) {
		this.npcsScripts.readFromNBT(compound);
		File file = this.npcsScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.npcsScripts.lastInited = -1L;
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}
	
	public void setForgeScripts(NBTTagCompound compound) {
		this.forgeScripts.readFromNBT(compound);
		File file = this.forgeScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.forgeScripts.lastInited = -1L;
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void setPlayerScripts(NBTTagCompound compound) {
		this.playerScripts.readFromNBT(compound);
		if (CustomNpcs.Server != null) {
			for (EntityPlayerMP player : CustomNpcs.Server.getPlayerList().getPlayers()) {
				PlayerData data = PlayerData.get(player);
				if (data != null) {
					data.scriptData.readFromNBT(compound);
				}
			}
		}
		try {
			NBTJsonUtil.SaveFile(this.playerScriptsFile(), compound);
			this.lastPlayerUpdate = System.currentTimeMillis();
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void setPotionScripts(NBTTagCompound compound) {
		this.potionScripts.readFromNBT(compound);
		File file = this.potionScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.potionScripts.lastInited = -1L;
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

}
