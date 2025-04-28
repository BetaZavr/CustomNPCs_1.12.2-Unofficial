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

import net.minecraft.nbt.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
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
import noppes.npcs.api.wrapper.data.TempData;
import noppes.npcs.blocks.tiles.TileNpcEntity;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataScript;
import noppes.npcs.reflection.nbt.TagLongArrayReflection;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.ScriptEncryption;
import noppes.npcs.util.Util;

public class ScriptContainer {

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
			} catch (Throwable error) { LogWriter.error("Error:", error); }
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
				value = TagLongArrayReflection.getData((NBTTagLongArray) tag);
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


	public ScriptContainer copyTo(IScriptHandler scriptHandler) {
		ScriptContainer scriptContainer = new ScriptContainer(scriptHandler, isClient);
		scriptContainer.readFromNBT(writeToNBT(new NBTTagCompound()), isClient);
		return scriptContainer;
	}

	public boolean hasHandler() {
		if (handler == null) { return false; }
		if (handler instanceof DataScript) {
			EntityNPCInterface npc = ((DataScript) handler).npc;
			try { return npc != null && npc.world != null && npc.getEntityId() > 0 && npc.equals(npc.world.getEntityByID(npc.getEntityId())); }
			catch (Throwable ignored) {}
			return false;
		}
		if (handler instanceof TileNpcEntity) {
			if (!((TileNpcEntity) handler).hasWorld()) { return false; }
			BlockPos pos = ((TileNpcEntity) handler).getPos();
			return ((TileNpcEntity) handler).getWorld().getTileEntity(pos) instanceof IScriptBlockHandler;
		}
		return true;
	}

	public ITextComponent noticeString() {
		return hasHandler() ? handler.noticeString(null, null) : null;
	}

	public void appendConsole(String message) {
		if (message == null || message.isEmpty()) { return; }
		long time = System.currentTimeMillis();
		if (console.containsKey(time)) { message = console.get(time) + "\n" + message; }
		console.put(time, message);
		while (console.size() > 30) { console.remove(console.firstKey()); }
		ScriptController.Instance.tryAddErrored(this);
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
		if (console.isEmpty()) { ScriptController.Instance.tryRemoveErrored(this); }
		else { ScriptController.Instance.tryAddErrored(this); }
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
		CustomNpcs.debugData.startDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "run");
		run(type, event);
		CustomNpcs.debugData.endDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "run");
	}

	private void run(String type, Object event) {
		if (engine == null) { setEngine(handler.getLanguage()); }
		if (errored && console.isEmpty()) {
			errored = false;
			fullscript = null;
		}
		if (errored || !hasScriptCode() || unknownFunctions.contains(type) || !CustomNpcs.EnableScripting || engine == null) { return; }
		if (ScriptController.Instance.lastLoaded > lastCreated) {
			lastCreated = ScriptController.Instance.lastLoaded;
			fullscript = null;
			hasNoEncryptScriptCode = false;
		}
		synchronized ("lock") {
			ScriptContainer.Current = this;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			try {
				engine.getContext().setWriter(pw);
				engine.getContext().setErrorWriter(pw);
				if (engine.get("dump") == null) { fillEngine(engine); }
				else if (isClient && (engine.get("mc") == null || engine.get("storedData") == null)) { fillEngineClient(engine); }
				if (fullscript == null) { engine.eval(getTotalCode()); }
				if (engine.getFactory().getLanguageName().equals("lua")) {
					Object ob = engine.get(type);
					if (ob != null) {
						if (ScriptContainer.luaCoerce == null) {
							ScriptContainer.luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua").getMethod("coerce", Object.class);
							ScriptContainer.luaCall = ob.getClass().getMethod("call", Class.forName("org.luaj.vm2.LuaValue"));
						}
						ScriptContainer.luaCall.invoke(ob, ScriptContainer.luaCoerce.invoke(null, event));
					}
					else { unknownFunctions.add(type); }
				}
				else { ((Invocable) engine).invokeFunction(type, event); }
			}
			catch (NoSuchMethodException notFunction) { unknownFunctions.add(type); }
			catch (Throwable e) {
				errored = true;
				ITextComponent notice = handler.noticeString(type, event);
				String noticeToLog = Util.instance.deleteColor(notice.getFormattedText());
				pw.write(noticeToLog + "\n");
				e.printStackTrace(pw);
				String errorText = (e.getCause() == null || e.getCause().getLocalizedMessage() == null) ?
						"" + e.getCause() : e.getCause().getLocalizedMessage().replaceAll("" + ((char) 13), "");
				StringBuilder error = new StringBuilder();
				if (errorText.contains("" + (char) 10)) {
					for (int c = 0; c < errorText.length(); c++) {
						error.append(errorText.charAt(c));
						if (errorText.charAt(c) == 10) { error.append((char) 167).append("8"); }
					}
				}
				else { error = new StringBuilder(((char) 167) + "8" + e); }
				ITextComponent errInfo = new TextComponentString("Script " + e.getCause().getClass().getSimpleName() + ": " + error);
				errInfo.getStyle().setColor(TextFormatting.DARK_GRAY);
				NoppesUtilServer.NotifyOPs(notice.appendText("\n").appendSibling(errInfo));
				LogWriter.error(noticeToLog + " ", e);
			}
			finally {
				appendConsole(sw.getBuffer().toString().trim());
				pw.close();
				ScriptContainer.Current = null;
			}
		}
		if (!console.isEmpty()) { ScriptController.Instance.tryAddErrored(this); }

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
		catch (Throwable e) { LogWriter.error(handler.noticeString(async, null) + " script generate async context: ", e); }
	}

	private void runSync(String sync, Object arguments) {
		CustomNPCsScheduler.runTack(() -> {
			try { ((Invocable) engine).invokeFunction(sync, arguments); }
			catch (Throwable e) { LogWriter.error(handler.noticeString(sync, null) + " script sync errored: ", e); }
		});
	}

	public void setEngine(String scriptLanguage) {
		engine = ScriptController.Instance.getEngineByName(scriptLanguage);
		fullscript = null;
		hasNoEncryptScriptCode = false;
		if (engine != null) { fillEngine(engine); }
		errored = engine == null;
	}

	private void fillEngine(ScriptEngine scriptEngine) {
		NBTTagCompound constants = ScriptController.Instance.constants;
		String language = Util.instance.deleteColor(handler.getLanguage()).toLowerCase();
		String side = isClient ? "Client" : "Server";
		boolean needResave= false;
		LogWriter.debug("Fill main classes to data");
		try { scriptEngine.put("Date", scriptEngine.eval("Java.type('" + Date.class.getName() + "')")); } catch (Throwable ignored) { LogWriter.error("Not put key \"Date\" to engine"); }
		try { scriptEngine.put("Calendar", scriptEngine.eval("Java.type('" + Calendar.class.getName() + "')")); } catch (Throwable ignored) { LogWriter.error("Not put key \"Calendar\" to engine"); }
		try { scriptEngine.put("System", scriptEngine.eval("Java.type('" + System.class.getName() + "')")); } catch (Throwable ignored) { LogWriter.error("Not put key \"System\" to engine"); }
		// Custom Functions
		LogWriter.debug("Fill custom functions to data");
		NBTTagList functions = constants.getCompoundTag("Functions").getTagList(language, 8);
		for (int i = 0; i < functions.tagCount(); i++) {
			String body = functions.getStringTagAt(i);
			try {
				if (!body.contains(" ") || !body.contains("(")) { continue; }
				String key = body.substring(body.indexOf(" ") + 1, body.indexOf("("));
				if (key.isEmpty()) { continue; }
				try {
					LogWriter.debug("Put function to data key: " + key + "; value: " + body);
					ScriptContainer.Data.put(key, scriptEngine.eval(body));
				}
				catch (Throwable e) {
					LogWriter.error("Not add function key: \"" + body + "\"; body: \"" + body + "\" to data");
					// save error
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
			catch (Throwable e) { constants.getTagList("Functions", 10).getCompoundTagAt(i).setBoolean("EvalIsError", true); }
		}
		// Custom Constants
		LogWriter.debug("Fill custom constants to data");
		NBTTagCompound cons = constants.getCompoundTag("Constants").getCompoundTag(language);
		for (String key : cons.getKeySet()) {
			Object value = getNBTValue(cons.getTag(key));
			String err = "";
			if (value == null) { err = "NullPointerException"; }
			else {
				try {
					LogWriter.debug("Put constant to data key: " + key + "; value: " + value);
					if (value instanceof String) {
						try { ScriptContainer.Data.put(key, scriptEngine.eval((String) value)); }
						catch (Throwable e) { ScriptContainer.Data.put(key, value); }
					}
					else { ScriptContainer.Data.put(key, value); }
				}
				catch (Throwable t) { err = t.getCause().getClass().getSimpleName(); }
			}
			if (!err.isEmpty()) {
				LogWriter.error("Not add constant key: \"" + key + "\"; value: \"" + value + "\" to data");
				// save error
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
		// Try to put all
		LogWriter.debug("Fill data to engine");
		for (Map.Entry<String, Object> entry : ScriptContainer.Data.entrySet()) {
			try {
				LogWriter.debug("Put to engine key: " + entry.getKey() + "; value: " + entry.getValue());
				scriptEngine.put(entry.getKey(), entry.getValue());
			} catch (Throwable ignored) { LogWriter.error("Not put data key \"" + entry.getKey() + "\" to engine"); }
		}
		if (isClient) { fillEngineClient(scriptEngine); }
		// Main Constants
		LogWriter.debug("Fill mod fields and methods to engine");
		try { scriptEngine.put("cnpcs", CustomNpcs.instance); } catch (Throwable ignored) { LogWriter.error("Not put key \"cnpcs\" to engine"); }
		try { scriptEngine.put("api", NpcAPI.Instance()); } catch (Throwable ignored) { LogWriter.error("Not put key \"api\" to engine"); }
		try { scriptEngine.put("currentThread", Thread.currentThread().getName()); } catch (Throwable ignored) { LogWriter.error("Not put key \"currentThread\" to engine"); }
		try { scriptEngine.put("main", scriptEngine); } catch (Throwable ignored) { LogWriter.error("Not put key \"main\" to engine"); }
		try { scriptEngine.put("currentScriptContainer", this); } catch (Throwable ignored) { LogWriter.error("Not put key \"currentScriptContainer\" to engine"); }
		try { scriptEngine.put("tempData", new TempData()); } catch (Throwable ignored) { LogWriter.error("Not put key \"tempData\" to engine"); }
		// resave constants file
		if (needResave) { Util.instance.saveFile(ScriptController.Instance.constantScriptsFile(), constants.copy()); }
		LogWriter.debug("Done fill engine");
	}

	private void fillEngineClient(ScriptEngine scriptEngine) {
		if (!isClient) { return; }
		// Try to put MC
		try { scriptEngine.put("mc", ClientProxy.mcWrapper); }
		catch (Throwable ignored) { LogWriter.error("Not put key \"mc\" to engine"); }
		try { scriptEngine.put("storedData", ScriptController.Instance.clientScripts.storedData); }
		catch (Throwable ignored) { LogWriter.error("Not put key \"storedData\" to engine"); }
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
