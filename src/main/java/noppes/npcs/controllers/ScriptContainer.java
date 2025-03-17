package noppes.npcs.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagByteArray;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.constants.AnimationType;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.constants.ParticleType;
import noppes.npcs.api.constants.PotionEffectType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.constants.SideType;
import noppes.npcs.api.constants.TacticalType;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.handler.IDataObject;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.api.wrapper.DataObject;
import noppes.npcs.api.wrapper.data.StoredData;
import noppes.npcs.api.wrapper.data.TempData;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.api.mixin.nbt.INBTTagLongArrayMixin;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.ScriptEncryption;
import noppes.npcs.util.Util;

public class ScriptContainer {

	public ScriptContainer copyTo(IScriptHandler scriptHandler) {
		ScriptContainer scriptContainer = new ScriptContainer(scriptHandler, isClient);
		scriptContainer.readFromNBT(writeToNBT(new NBTTagCompound()), isClient);
		return scriptContainer;
	}

	public static class Dump implements Function<Object, IDataObject> {

		@Override
		public IDataObject apply(Object o) {
			return new DataObject(o);
		}

	}
	
	public class Log implements Function<Object, Void> {
		@Override
		public Void apply(Object o) {
			String message = o == null ? "null" : o.toString();
			appendConsole(message);
			LogWriter.info(message);
			return null;
		}
	}
	
	public static ScriptContainer Current;
	public static HashMap<String, Object> Data = new HashMap<>();
	private static Method luaCall;
	private static Method luaCoerce;

	private static final Executor executor = Executors.newFixedThreadPool(16);
	public static final ConcurrentSkipListMap<Long, ScriptEngine> contexts = new ConcurrentSkipListMap<>();
	private static final ConcurrentSkipListMap<String, ExecutorService> links = new ConcurrentSkipListMap<>();
	
	static {
		FillMap(AnimationKind.class);
		FillMap(AnimationType.class);
		FillMap(EntityType.class);
		FillMap(GuiComponentType.class);
		FillMap(JobType.class);
		FillMap(MarkType.class);
		FillMap(OptionType.class);
		FillMap(ParticleType.class);
		FillMap(PotionEffectType.class);
		FillMap(RoleType.class);
		FillMap(SideType.class);
		FillMap(TacticalType.class);
		ScriptContainer.Data.put("api", NpcAPI.Instance());
		ScriptContainer.Data.put("API", NpcAPI.Instance());
		ScriptContainer.Data.put("cnpcs", CustomNpcs.instance);
		ScriptContainer.Data.put("PosZero", new BlockPosWrapper(BlockPos.ORIGIN));
	}
	
	private static void FillMap(Class<?> c) {
		if (!c.isEnum()) {
			return;
		}
		ScriptContainer.Data.put(c.getSimpleName(), c.getDeclaringClass() != null ? c.getDeclaringClass() : c);
		for (Object e : c.getEnumConstants()) {
			try {
				Method m = e.getClass().getMethod("get");
				if (m.getReturnType() != int.class) {
					continue;
				}
				ScriptContainer.Data.put(c.getSimpleName() + "_" + ((Enum<?>) e).name(), m.invoke(e));
			} catch (Exception error) { LogWriter.error("Error:", error); }
		}
	}

	private static Object getNBTValue(NBTBase tag) {
		Object value = null;
		switch (tag.getId()) {
			case 1:
				value = ((NBTTagByte) tag).getByte();
				break;
			case 2:
				value = ((NBTTagShort) tag).getShort();
				break;
			case 3:
				value = ((NBTTagInt) tag).getInt();
				break;
			case 4:
				value = ((NBTTagLong) tag).getLong();
				break;
			case 5:
				value = ((NBTTagFloat) tag).getFloat();
				break;
			case 6:
				value = ((NBTTagDouble) tag).getDouble();
				break;
			case 7:
				value = ((NBTTagByteArray) tag).getByteArray();
				break;
			case 8:
				value = ((NBTTagString) tag).getString();
				break;
			case 9: // NBTTagList
				List<Object> list = new ArrayList<>();
				for (NBTBase obj : (NBTTagList) tag) {
					Object v = getNBTValue(obj);
					if (v != null) {
						list.add(v);
					}
				}
				value = list.toArray(new Object[0]);
				break;
			case 10: // NBTTagCompound
				Map<String, Object> comp = new TreeMap<>();
				for (String key : ((NBTTagCompound) tag).getKeySet()) {
					Object v = getNBTValue(((NBTTagCompound) tag).getTag(key));
					if (v != null) {
						comp.put(key, v);
					}
				}
				value = comp;
				break;
			case 11:
				value = ((NBTTagIntArray) tag).getIntArray();
				break;
			case 12:
				value = ((INBTTagLongArrayMixin) tag).npcs$getData();
				break;
		}
		return value;
	}
	
	public static void reloadConstants() {
		ScriptContainer.Data.remove("dump");
	}
	
	public TreeMap<Long, String> console = new TreeMap<>();
	public ScriptEngine engine = null;
	public boolean isClient;
	public boolean errored = false;
	private String fullscript = null;
	private boolean hasNoEncryptScriptCode = false;
	private final IScriptHandler handler;
	public long lastCreated = 0L;
	public String script = "";
	public List<String> scripts = new ArrayList<>();
	private HashSet<String> unknownFunctions = new HashSet<>();

	public ScriptContainer(IScriptHandler handlerIn, boolean isClientIn) {
		handler = handlerIn;
		isClient = isClientIn;
	}

	public void appendConsole(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		long time = System.currentTimeMillis();
		if (console.containsKey(time)) {
			message = console.get(time) + "\n" + message;
		}
		console.put(time, message);
		while (console.size() > 250) {
			console.remove(console.firstKey());
		}
	}

	public void clear() {
		script = "";
		fullscript = null;
		scripts.clear();
	}

	private String getTotalCode() {
		if (fullscript == null) {
			hasNoEncryptScriptCode = script != null && !script.isEmpty();
			fullscript = script;
			if (!fullscript.isEmpty()) {
				fullscript += "\n";
			}
			ScriptController sData = ScriptController.Instance;
			Map<String, String> map = isClient ? sData.clients : sData.scripts;

			StringBuilder sbCode = new StringBuilder();
			for (String loc : scripts) {
				String code;
				if (!isClient && sData.encrypts.containsKey(loc)) {
					code = ScriptEncryption.decryptScriptFromFile(sData.encrypts.get(loc));
				}
				else {
					code =  map.get(loc);
					if (code != null) { hasNoEncryptScriptCode = true; }
				}
				if (code != null && !code.isEmpty()) {
					sbCode.append(code).append("\n");
				}
			}
			fullscript += sbCode.toString();
			if (map.containsKey("all.js")) { fullscript = map.get("all.js") + "\n" + fullscript; }
			unknownFunctions = new HashSet<>();
		}
		return fullscript;
	}

	public boolean hasNoEncryptScriptCode() {
		boolean sr = !(script == null || script.isEmpty());
		if (sr) {
			String tempScript = script.replace(" ", "").replace("" + ((char) 9), "").replace("" + ((char) 10), "");
			sr = !tempScript.isEmpty();
		}
		return sr || hasNoEncryptScriptCode;
	}

	public boolean hasScriptCode() {
		return !getTotalCode().isEmpty();
	}

	public boolean isInit() {
		return fullscript != null;
	}

	public boolean isValid() {
		return isInit() && !errored;
	}

	public void readFromNBT(NBTTagCompound compound, boolean isClientIn) {
		if (compound.hasKey("Script", 9)) {
			NBTTagList list = compound.getTagList("Script", 8);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.tagCount(); i++) {
				sb.append(list.getStringTagAt(i));
			}
			script = sb.toString();
		} else {
			script = compound.getString("Script");
		}
		console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
		scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
		hasNoEncryptScriptCode = compound.getBoolean("HasNoEncryptScriptCode");
		isClient = isClientIn;
		if (isClient) { errored = false; }
		lastCreated = 0L;
		fullscript = null;
		unknownFunctions.clear();
	}

	public void run(String type, Event event, boolean side) {
		Object key = event instanceof BlockEvent ? "Block"
				: event instanceof PlayerEvent ? "Player"
						: event instanceof ItemEvent ? "Item" : event instanceof NpcEvent ? "Npc" : null;
		CustomNpcs.debugData.startDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "ScriptContainer_run");
		run(type, event);
		CustomNpcs.debugData.endDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "ScriptContainer_run");
	}

	private void run(String type, Object event) {
		if (engine == null) { setEngine(handler.getLanguage()); }
		if (errored || !hasScriptCode() || unknownFunctions.contains(type) || !CustomNpcs.EnableScripting || engine == null) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > lastCreated) {
			lastCreated = ScriptController.Instance.lastLoaded;
			fullscript = null;
			hasNoEncryptScriptCode = false;
		}
		synchronized ("lock") {
			ScriptContainer.Current = this;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			ScriptEngine action = engine;
			action.getContext().setWriter(pw);
			action.getContext().setErrorWriter(pw);
			if (action.get("dump") == null) { fillEngine(action); }
			else if (isClient && (action.get("mc") == null || action.get("storedData") == null)) { fillEngineClient(action); }
			try {
				if (fullscript == null) { action.eval(getTotalCode()); }
				if (action.getFactory().getLanguageName().equals("lua")) {
					Object ob = action.get(type);
					if (ob != null) {
						if (ScriptContainer.luaCoerce == null) {
							ScriptContainer.luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua").getMethod("coerce", Object.class);
							ScriptContainer.luaCall = ob.getClass().getMethod("call", Class.forName("org.luaj.vm2.LuaValue"));
						}
						ScriptContainer.luaCall.invoke(ob, ScriptContainer.luaCoerce.invoke(null, event));
					}
					else { unknownFunctions.add(type); }
				}
				else { ((Invocable) action).invokeFunction(type, event); }
			} catch (NoSuchMethodException err0) { unknownFunctions.add(type); }
			catch (Exception err1) {
				errored = true;
				err1.printStackTrace(pw);
				String e = err1.getCause().getLocalizedMessage().replaceAll("" + ((char) 13), "");
				StringBuilder error = new StringBuilder();
				if (e.contains("" + (char) 10)) {
					for (int c = 0; c < e.length(); c++) {
						error.append(e.charAt(c));
						if (e.charAt(c) == 10) { error.append((char) 167).append("8"); }
					}
				}
				else { error = new StringBuilder(((char) 167) + "8" + e); }
				NoppesUtilServer.NotifyOPs(handler.noticeString() + " - script errored:\n" + ((char) 167) + "8" + err1.getCause().getClass().getSimpleName() + ": " + error);
				LogWriter.error(handler.noticeString() + " script errored: ", err1);
			}
			finally {
				appendConsole(sw.getBuffer().toString().trim());
				pw.close();
				ScriptContainer.Current = null;
			}
		}
	}

	public void runAsync(String link, String async, String sync, Object arguments) {
		if (!async.isEmpty()) {
			if (!link.isEmpty()) {
				if (!links.containsKey(link)) {
					links.put(link, Executors.newSingleThreadExecutor());
				}
				links.get(link).execute(() -> generateAsyncContext(async, sync, arguments));
			}
			else { executor.execute(() -> generateAsyncContext(async, sync, arguments)); }
		}
		else { runSync(sync, arguments); }
	}

	private void generateAsyncContext(String async, String sync, Object arguments) {
		try {
			ScriptEngine engine;
			if (!contexts.containsKey(Thread.currentThread().getId())) {
				engine = ScriptController.Instance.getEngineByName(handler.getLanguage());
				fillEngine(engine);
				Map<String, String> map = isClient ? ScriptController.Instance.clients : ScriptController.Instance.scripts;
				engine.eval(map.get("all.js") + "\n");
				contexts.put(Thread.currentThread().getId(), engine);
			}
			engine = contexts.get(Thread.currentThread().getId());
			engine.eval("var asyncFunction = (" + async + ");");
			Object result = ((Invocable) engine).invokeFunction("asyncFunction", arguments);
			if (!sync.isEmpty()) { runSync(sync, result); }
		}
		catch (Exception e) { LogWriter.error(handler.noticeString() + " script generate async context: ", e); }
	}

	private void runSync(String sync, Object arguments) {
		CustomNPCsScheduler.runTack(() -> {
			try { ((Invocable) engine).invokeFunction(sync, arguments); }
			catch (Exception e) { LogWriter.error(handler.noticeString() + " script sync errored: ", e); }
		});
	}

	public void setEngine(String scriptLanguage) {
		engine = ScriptController.Instance.getEngineByName(scriptLanguage);
		fullscript = null;
		hasNoEncryptScriptCode = false;
		if (engine != null && engine.get("dump") == null) { fillEngine(engine); }
		errored = engine == null;
	}

	private void fillEngine(ScriptEngine scriptEngine) {
		NBTTagCompound constants = ScriptController.Instance.constants;
		String language = Util.instance.deleteColor(handler.getLanguage()).toLowerCase();
		String side = isClient ? "Client" : "Server";
		boolean needResave= false;

		// Custom Functions
		NBTTagList functions = constants.getCompoundTag("Functions").getTagList(language, 8);
		for (int i = 0; i < functions.tagCount(); i++) {
			String body = functions.getStringTagAt(i);
			try {
				if (!body.contains(" ") || !body.contains("(")) { continue; }
				String key = body.substring(body.indexOf(" ") + 1, body.indexOf("("));
				if (key.isEmpty()) { continue; }
				try { ScriptContainer.Data.put(key, scriptEngine.eval(body)); }
				catch (Exception e) {
					LogWriter.error("Key: " + key + "; Value: " + body + " put error:", e);
					if (!constants.getCompoundTag("Functions").hasKey("EvalIsError", 10)) {
						constants.getCompoundTag("Functions").setTag("EvalIsError", new NBTTagCompound());
					}
					if (!constants.getCompoundTag("Functions").getCompoundTag("EvalIsError").hasKey(side, 10)) {
						constants.getCompoundTag("Functions").getCompoundTag("EvalIsError").setTag(side, new NBTTagCompound());
					}
					NBTTagList errors = constants.getCompoundTag("Functions").getCompoundTag("EvalIsError").getCompoundTag(side).getTagList(language, 8);
					boolean has = false;
					for (int j = 0; j < errors.tagCount(); j++) {
						if (errors.getStringTagAt(i).equals(body)) { has = true; break; }
					}
					if (!has) {
						errors.appendTag(new NBTTagString(e.getCause().getClass().getSimpleName() + ": " + body));
						constants.getCompoundTag("Functions").getCompoundTag("EvalIsError").getCompoundTag(side).setTag(language, errors);
						needResave = true;
					}
				}
			}
			catch (Exception e) { constants.getTagList("Functions", 10).getCompoundTagAt(i).setBoolean("EvalIsError", true); }
		}
		// Custom Constants
		NBTTagCompound cons = constants.getCompoundTag("Constants").getCompoundTag(language);
		for (String key : cons.getKeySet()) {
			Object value = getNBTValue(cons.getTag(key));
			String err = "";
			if (value == null) { err = "NullPointerException"; }
			else {
				if (value instanceof String) {
					try { ScriptContainer.Data.put(key, scriptEngine.eval((String) value)); }
					catch (Throwable e) { ScriptContainer.Data.put(key, value); }
				}
				else { ScriptContainer.Data.put(key, value); }
			}
			if (!err.isEmpty()) {
				if (!constants.getCompoundTag("Constants").hasKey("EvalIsError", 10)) {
					constants.getCompoundTag("Constants").setTag("EvalIsError", new NBTTagCompound());
				}
				if (!constants.getCompoundTag("Constants").getCompoundTag("EvalIsError").hasKey(side, 10)) {
					constants.getCompoundTag("Constants").getCompoundTag("EvalIsError").setTag(side, new NBTTagCompound());
				}
				NBTTagCompound errors = constants.getCompoundTag("Constants").getCompoundTag("EvalIsError").getCompoundTag(side).getCompoundTag(language);
				errors.setString(key, err);
				constants.getCompoundTag("Constants").getCompoundTag("EvalIsError").getCompoundTag(side).setTag(language, errors);
				needResave = true;
			}
		}
		// Base Functions
		ScriptContainer.Data.put("dump", new Dump());
		ScriptContainer.Data.put("log", new Log());
		// Base Constants
		try { ScriptContainer.Data.put("date", scriptEngine.eval("Java.type('" + Date.class.getName() + "')")); } catch (Exception ignored) { }
		try { ScriptContainer.Data.put("calendar", scriptEngine.eval("Java.type('" + Calendar.class.getName() + "')")); } catch (Exception ignored) { }
		// Try to put all
		for (Map.Entry<String, Object> entry : ScriptContainer.Data.entrySet()) {
			try { scriptEngine.put(entry.getKey(), entry.getValue()); } catch (Exception ignored) { }
		}
		if (isClient) { fillEngineClient(scriptEngine); }
		scriptEngine.put("api", NpcAPI.Instance());
		scriptEngine.put("currentThread", Thread.currentThread().getName());
		scriptEngine.put("main", scriptEngine);
		scriptEngine.put("currentScriptContainer", this);
		scriptEngine.put("tempData", new TempData());

		if (needResave) {
			try { Util.instance.saveFile(ScriptController.Instance.constantScriptsFile(), constants.copy()); }
			catch (Exception e) { LogWriter.except(e); }
		}
	}

	private void fillEngineClient(ScriptEngine scriptEngine) {
		if (!isClient) { return; }
		// Try to put MC
		try { scriptEngine.put("mc", ClientProxy.mcWrapper); } catch (Exception ignored) {  }
		try { scriptEngine.put("storedData", ScriptController.Instance.clientScripts.storedData); } catch (Exception ignored) {  }
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("Script", script);
		compound.setTag("Console", NBTTags.NBTLongStringMap(console));
		compound.setTag("ScriptList", NBTTags.nbtStringList(scripts));
		compound.setBoolean("isClient", isClient);
		compound.setBoolean("HasNoEncryptScriptCode", hasNoEncryptScriptCode);
		return compound;
	}

}
