package noppes.npcs.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.launchwrapper.Launch;
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
import noppes.npcs.controllers.data.ForgeScriptData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.PotionScriptData;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NBTJsonUtil;
import noppes.npcs.util.TempFile;

public class ScriptController {
	
	public static boolean HasStart = false;
	public static ScriptController Instance;

	private ScriptEngineManager manager;
	
	public boolean shouldSave;
	public long lastLoaded, lastPlayerUpdate;
	public NBTTagCompound compound;
	public NBTTagCompound constants;
	public File dir, clientDir;
	public ForgeScriptData forgeScripts;
	public ClientScriptData clientScripts;
	public final Map<String, ScriptEngineFactory> factories;
	public final Map<String, String> languages;
	public final Map<String, Long> sizes, clientSizes;
	public final Map<String, String> scripts, clients;
	public PlayerScriptData playerScripts;
	public PotionScriptData potionScripts;
	
	private ScriptEngine graalEngine;
	
	public ScriptController() {
		this.languages = Maps.<String, String>newTreeMap();
		this.factories = Maps.<String, ScriptEngineFactory>newTreeMap();
		this.scripts = Maps.<String, String>newTreeMap();
		this.clients = Maps.<String, String>newTreeMap();
		this.sizes = Maps.<String, Long>newTreeMap();
		this.clientSizes = Maps.<String, Long>newTreeMap();
		this.playerScripts = new PlayerScriptData(null);
		this.forgeScripts = new ForgeScriptData();
		this.clientScripts = new ClientScriptData();
		this.potionScripts = new PotionScriptData();
		this.lastLoaded = 0L;
		this.lastPlayerUpdate = 0L;
		this.compound = new NBTTagCompound();
		this.constants = new NBTTagCompound();
		this.shouldSave = false;
		ScriptController.Instance = this;
		if (!CustomNpcs.NashorArguments.isEmpty()) {
			System.setProperty("nashorn.args", CustomNpcs.NashorArguments);
		}
		this.graalEngine = null;
		try {
			Class<?> graal = Class.forName("com.oracle.truffle.js.scriptengine.GraalJSScriptEngine");
			Class<?> cnt = Class.forName("org.graalvm.polyglot.Context");
			Object cntInstance = null;
			for (Method m : cnt.getDeclaredMethods()) {
				if (m.getName().equals("newBuilder") && m.isAccessible()) {
					try { cntInstance = m.invoke(cnt, "js"); }
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { e.printStackTrace(); }
					break;
				}
			}
			if (cntInstance!=null) {
				for (Method m : cnt.getDeclaredMethods()) {
					if (!m.isAccessible()) { continue; }
					try {
						switch(m.getName()) {
							case "allowExperimentalOptions": cntInstance = m.invoke(cntInstance, true); break;
							case "allowHostClassLookup": cntInstance = m.invoke(cntInstance, true); break;
							case "allowCreateProcess": cntInstance = m.invoke(cntInstance, true); break;
							case "allowHostClassLoading": cntInstance = m.invoke(cntInstance, true); break;
							case "allowNativeAccess": cntInstance = m.invoke(cntInstance, true); break;
							case "allowAllAccess": cntInstance = m.invoke(cntInstance, true); break;
							case "allowIO": cntInstance = m.invoke(cntInstance, true); break;
							case "option":
								cntInstance = m.invoke(cntInstance, "js.ecmascript-version", "2022");
								cntInstance = m.invoke(cntInstance, "js.nashorn-compat", "true");
							break;
							default: continue;
						}
					}
					catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { }
				}
				for (Method m : graal.getDeclaredMethods()) {
					if (m.getName().equals("create")) {
						try {
							this.graalEngine = (ScriptEngine) m.invoke(graal, null, cntInstance);
						}
						catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) { e.printStackTrace(); }
					}
				}
			}
		}
		catch (ClassNotFoundException e) { }
		this.manager = new ScriptEngineManager();
		try {
			if (this.manager.getEngineByName("ecmascript") == null) {
				Launch.classLoader.addClassLoaderExclusion("jdk.nashorn.");
				Launch.classLoader.addClassLoaderExclusion("jdk.internal.dynalink");
				Class<?> c = Class.forName("jdk.nashorn.api.scripting.NashornScriptEngineFactory");
				ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
				factory.getScriptEngine();
				try {
					Class<?> require = Class.forName("com.coveo.nashorn_modules.Require");
					Method metodEnable = require.getMethod("enable");
					metodEnable.invoke(factory, CustomNpcs.getWorldSaveDirectory("scripts/common_js"));
				} catch (Exception e) {
				}
				this.manager.registerEngineName("ecmascript", factory);
				this.manager.registerEngineExtension("js", factory);
				this.manager.registerEngineMimeType("application/ecmascript", factory);
				this.languages.put(factory.getLanguageName(), ".js");
				this.factories.put(factory.getLanguageName().toLowerCase(), factory);
			}
		} catch (Throwable t) {
		}
		try {
			Class<?> c = Class.forName("org.jetbrains.kotlin.script.jsr223.KotlinJsr223JvmLocalScriptEngineFactory");
			ScriptEngineFactory factory = (ScriptEngineFactory) c.newInstance();
			factory.getScriptEngine();
			this.manager.registerEngineName("kotlin", factory);
			this.manager.registerEngineExtension("ktl", factory);
			this.manager.registerEngineMimeType("application/kotlin", factory);
			this.languages.put(factory.getLanguageName(), ".ktl");
			this.factories.put(factory.getLanguageName().toLowerCase(), factory);
		} catch (Throwable t2) {
		}
		LogWriter.info("Script Engines Available:");
		for (ScriptEngineFactory fac : this.manager.getEngineFactories()) {
			try {
				LogWriter.debug("Found script Library: \""+fac.getLanguageName() + "\"");
				if (fac.getExtensions().isEmpty()) {
					continue;
				}
				if (!(fac.getScriptEngine() instanceof Invocable) && !fac.getLanguageName().equals("lua")) {
					continue;
				}
				String ext = "." + fac.getExtensions().get(0).toLowerCase();
				LogWriter.info("Added script Library: \""+fac.getLanguageName() + "\"; files index: \"" + ext + "\"");
				this.languages.put(fac.getLanguageName(), ext);
				this.factories.put(fac.getLanguageName().toLowerCase(), fac);
			} catch (Throwable t3) {
				LogWriter.error("Error Added Script Library: \""+fac.getLanguageName() + "\": "+t3);
			}
		}
	}
	
	public boolean hasGraalLib() { return this.graalEngine!=null; }

	private File constantScriptsFile() {
		return new File(this.dir, "constant_scripts.json");
	}

	private File forgeScriptsFile() {
		return new File(this.dir, "forge_scripts.json");
	}

	private File clientScriptsFile() {
		return new File(this.dir, "client_scripts.json");
	}
	
	public ScriptEngine getEngineByName(String language) {
		if (language.equals("ECMAScript") && this.hasGraalLib()) { return this.graalEngine; }
		ScriptEngineFactory fac = this.factories.get(AdditionalMethods.instance.deleteColor(language).toLowerCase());
		if (fac == null) { return null; }
		return fac.getScriptEngine();
	}

	private List<String> getScripts(String language, boolean isClient) {
		List<String> list = new ArrayList<String>();
		String ext = this.languages.get(language);
		if (ext == null) { return list; }
		for (String script : (isClient ? this.clients : this.scripts).keySet()) {
			if (script.endsWith(ext)) { list.add(script); }
		}
		return list;
	}

	public void loadCategories() {
		this.dir = new File(CustomNpcs.getWorldSaveDirectory(), "scripts");
		if (!this.dir.exists()) { this.dir.mkdirs(); }
		this.clientDir = new File(this.dir, "client");
		if (!this.clientDir.exists()) { this.clientDir.mkdirs(); }
		
		if (!this.worldDataFile().exists()) { this.shouldSave = true; }
		WorldWrapper.tempData.clear();
		this.scripts.clear();
		this.sizes.clear();
		for (String key : this.clients.keySet()) { CommonProxy.loadFiles.remove(key); }
		this.clients.clear();
		this.clientSizes.clear();
		for (String language : this.languages.keySet()) {
			String ext = this.languages.get(language);
			File scriptDir = new File(this.dir, language.toLowerCase());
			if (!scriptDir.exists()) { scriptDir.mkdir(); }
			else { this.loadDir(scriptDir, "", ext, false); }

			scriptDir = new File(this.clientDir, language.toLowerCase());
			if (!scriptDir.exists()) { scriptDir.mkdir(); }
			else { this.loadDir(scriptDir, "", ext, true); }
		}
		this.lastLoaded = System.currentTimeMillis();
	}

	private void loadDir(File dir, String name, String ext, boolean isClient) {
		for (File file : dir.listFiles()) {
			String filename = name + file.getName().toLowerCase();
			if (file.isDirectory()) {
				this.loadDir(file, filename + "/", ext, isClient);
			} else if (filename.endsWith(ext)) {
				try {
					String code = this.readFile(file);
					if (isClient) {
						this.clients.put(filename, code);
						this.clientSizes.put(filename, file.length());
					}
					else {
						this.scripts.put(filename, code);
						this.sizes.put(filename, file.length());
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
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

	public boolean loadClientScripts() {
		this.clientScripts.clear();
		File file = this.clientScriptsFile();
		try {
			if (!file.exists()) {
				return false;
			}
			this.clientScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
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

	public boolean loadStoredData() {
		this.compound = new NBTTagCompound();
		File file = this.worldDataFile();
		boolean isLoad = true;
		try {
			if (file.exists()) {
				this.compound = NBTJsonUtil.LoadFile(file);
				this.shouldSave = false;
			}
			else { isLoad = false; }
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return isLoad;
	}
	
	public boolean loadConstantData() {
		this.constants = new NBTTagCompound();
		File file = this.constantScriptsFile();
		boolean isLoad = true;
		try {
			if (file.exists()) {
				this.constants = NBTJsonUtil.LoadFile(file);
			}
			else {
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
				list.appendTag(new NBTTagString("function setField(value,object,key) { try { var f = dump(object).getField(key); if (f) { return f.setValue(object,value); } } catch (error) { log('Error: \"'+key+'\" is not a Field or not found, or not type mismatch in \"'+object.getClass().getName()+'\"'); } return false; }"));
				list.appendTag(new NBTTagString("function invoke(value,object,key) { try { var m = dump(object).getMethod(key); if (m) { var jo = Java.type('java.lang.Object[]'); if (value!=jo) { try { if (value.length>=0) { var v = new jo(value.length); for (var i=0; i<value.length; i++) { v[i] = value[i]; } return m.invoke(v); } } catch (err) { } var v = new jo(1); v[0] = value; return m.invoke(v); } else { return m.invoke(value); } } } catch (error) { log('Error: \"'+key+'\" is not a Method or not found, or not type mismatch in \"'+object.getClass().getName()+'\"'); } return null; }"));
				list.appendTag(new NBTTagString("function getCustomFunction(name, ev) {var fhm;try {var actor=\"Any\";if (ev) {if (ev.player) { actor = \"Player\"; }else if (ev.npc) { actor = \"NPC\"; }else if (ev.block) { actor = \"Block\"; }};fhm = api.getIWorld(0).getTempdata().get(\"functions\");if (fhm instanceof JHMap && fhm.containsKey(name)) {return fhm.get(name)};if (name!=\"loadFile\") {var dir = existsDir(api.getWorldDir().toPath().resolve(\"data\").resolve(\"functions\"));gFunc(\"loadFile\",ev)(dir.resolve(name+\".json\"), \"fhm\");if (fhm instanceof JHMap && fhm.containsKey(name)) {return fhm.get(name)}}} catch (error) {if (fhm && fhm instanceof JHMap) {gFunc(\"errorMes\",ev)(actor, error, \"Name: \u00A7f\"+name, ev);}};return eval(\"function fnull(a,b,c,d,e,f,g,h,i,k,l,m,n,o,p,r,s,t,q,v) {return;}\");}"));
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

	public NBTTagList nbtLanguages(boolean isClient) {
		NBTTagList list = new NBTTagList();
		for (String language : this.languages.keySet()) {
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList scripts = new NBTTagList();
			for (String script : this.getScripts(language, isClient)) {
				scripts.appendTag(new NBTTagString(script));
			}
			compound.setTag("Scripts", scripts);
			compound.setString("Language", language);
			long[] sizes = new long[scripts.tagCount()];
			int i = 0;
			for (Long l : (isClient ? this.clientSizes : this.sizes).values()) { sizes[i] = l; i++; }
			compound.setTag("sizes", new NBTTagLongArray(sizes));
			list.appendTag(compound);
		}
		return list;
	}

	private File playerScriptsFile() {
		return new File(this.dir, "player_scripts.json");
	}

	private String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF8"));
		try {
			StringBuilder sb = new StringBuilder();
			for (String line = br.readLine(); line != null; line = br.readLine()) {
				sb.append(line);
				sb.append("\n");
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	@SubscribeEvent
	public void saveWorld(WorldEvent.Save event) {
		if (!this.shouldSave || event.getWorld().isRemote
				|| event.getWorld() != event.getWorld().getMinecraftServer().worlds[0]) {
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

	public void setForgeScripts(NBTTagCompound compound) {
		this.forgeScripts.readFromNBT(compound);
		File file = this.forgeScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.forgeScripts.lastInited = -1L;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NBTJsonUtil.JsonException e2) {
			e2.printStackTrace();
		}
	}
	
	public void setClientScripts(NBTTagCompound compound) {
		this.clientScripts.readFromNBT(compound);
		File file = this.clientScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.clientScripts.lastInited = -1L;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NBTJsonUtil.JsonException e2) {
			e2.printStackTrace();
		}
	}

	public void setPlayerScripts(NBTTagCompound compound) {
		this.playerScripts.readFromNBT(compound);
		File file = this.playerScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.lastPlayerUpdate = System.currentTimeMillis();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NBTJsonUtil.JsonException e2) {
			e2.printStackTrace();
		}
	}

	private File worldDataFile() {
		return new File(this.dir, "world_data.json");
	}

	public boolean loadPotionScripts() {
		this.potionScripts.clear();
		File file = this.potionScriptsFile();
		try {
			if (!file.exists()) { return false; }
			this.potionScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	public void setPotionScripts(NBTTagCompound compound) {
		this.potionScripts.readFromNBT(compound);
		File file = this.potionScriptsFile();
		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.potionScripts.lastInited = -1L;
		} catch (IOException e) {
			e.printStackTrace();
		} catch (NBTJsonUtil.JsonException e2) {
			e2.printStackTrace();
		}
	}

	private File potionScriptsFile() { return new File(this.dir, "potion_scripts.json"); }
	
	public void load() {
		ScriptController sData = ScriptController.Instance;
		sData.loadCategories();
		sData.loadStoredData();
		sData.loadPlayerScripts();
		sData.loadForgeScripts();
		sData.loadClientScripts();
		sData.loadPotionScripts();
		sData.loadConstantData();
		ScriptController.HasStart = false;
	}

	public void sendClientTo(EntityPlayerMP player) {
		NBTTagCompound compound = new NBTTagCompound();
		this.clientScripts.writeToNBT(compound);
		Server.sendData(player, EnumPacketClient.SCRIPT_CLIENT, compound);
		NBTTagList list = new NBTTagList();
		for (String key : this.clients.keySet()) {
			if (!CommonProxy.loadFiles.containsKey(key)) { CommonProxy.loadFiles.put(key, new TempFile(key, 0, 1, this.clientSizes.get(key))); }
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
	
}
