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
import java.util.function.Function;

import javax.script.Invocable;
import javax.script.ScriptEngine;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import noppes.npcs.client.ClientProxy;
import noppes.npcs.api.mixin.nbt.INBTTagLongArrayMixin;
import noppes.npcs.util.ScriptEncryption;

public class ScriptContainer {

	public ScriptContainer copyTo(IScriptHandler scriptHandler) {
		ScriptContainer scriptContainer = new ScriptContainer(scriptHandler, isClient);
		scriptContainer.readFromNBT(this.writeToNBT(new NBTTagCompound()), isClient);
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
			ScriptContainer.this.appendConsole(o + "");
			LogWriter.info(o + "");
			return null;
		}
	}
	
	public static ScriptContainer Current;
	public static HashMap<String, Object> Data = Maps.newHashMap();
	private static Method luaCall;
	private static Method luaCoerce;
	
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
		FillMap(ScriptController.Instance.constants);
		ScriptContainer.Data.put("api", NpcAPI.Instance());
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
	
	private static void FillMap(NBTTagCompound c) {
		if (!c.hasKey("Constants", 10)) {
			return;
		}
		for (String key : c.getCompoundTag("Constants").getKeySet()) {
			NBTBase tag = c.getCompoundTag("Constants").getTag(key);
			Object value = getNBTValue(tag);
			if (value != null) {
				ScriptContainer.Data.put(key, value);
			}
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
			List<Object> list = Lists.newArrayList();
			for (NBTBase obj : (NBTTagList) tag) {
				Object v = getNBTValue(obj);
				if (v != null) {
					list.add(v);
				}
			}
			value = list.toArray(new Object[0]);
			break;
		case 10: // NBTTagCompound
			Map<String, Object> comp = Maps.newTreeMap();
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
	
	public TreeMap<Long, String> console;
	public ScriptEngine engine;
	public boolean errored;
	private String fullscript;

	private boolean hasNoEncryptScriptCode = false;

	private final IScriptHandler handler;

	public long lastCreated;

	public String script;

	public List<String> scripts;

	private HashSet<String> unknownFunctions;

	public boolean isClient;

	public ScriptContainer(IScriptHandler handler, boolean isClient) {
		this.fullscript = null;
		this.script = "";
		this.console = new TreeMap<>();
		this.errored = false;
		this.scripts = new ArrayList<>();
		this.unknownFunctions = new HashSet<>();
		this.lastCreated = 0L;
		this.engine = null;
		this.handler = handler;
		this.isClient = isClient;
	}

	public void appendConsole(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		long time = System.currentTimeMillis();
		if (this.console.containsKey(time)) {
			message = this.console.get(time) + "\n" + message;
		}
		this.console.put(time, message);
		while (this.console.size() > 250) {
			this.console.remove(this.console.firstKey());
		}
	}

	public void clear() {
		this.script = "";
		this.fullscript = null;
		this.scripts.clear();
	}

	private String getTotalCode() {
		if (this.fullscript == null) {
			this.hasNoEncryptScriptCode = this.script != null && !this.script.isEmpty();
			this.fullscript = this.script;
			if (!this.fullscript.isEmpty()) {
				this.fullscript += "\n";
			}
			ScriptController sData = ScriptController.Instance;
			Map<String, String> map = this.isClient ? sData.clients : sData.scripts;

			StringBuilder sbCode = new StringBuilder();
			for (String loc : this.scripts) {
				String code;
				if (!this.isClient && sData.encrypts.containsKey(loc)) {
					code = ScriptEncryption.decryptScriptFromFile(sData.encrypts.get(loc));
				}
				else {
					code =  map.get(loc);
					if (code != null) { this.hasNoEncryptScriptCode = true; }
				}
				if (code != null && !code.isEmpty()) {
					sbCode.append(code).append("\n");
				}
			}
			this.fullscript += sbCode.toString();
			if (map.containsKey("all.js")) {
				this.fullscript = map.get("all.js") + "\n" + this.fullscript;
			}
			this.unknownFunctions = new HashSet<>();
		}
		return this.fullscript;
	}

	public boolean hasNoEncryptScriptCode() {
		boolean sr = !(this.script == null || this.script.isEmpty());
		if (sr) {
			String tempScript = this.script.replace(" ", "").replace("" + ((char) 9), "").replace("" + ((char) 10), "");
			sr = !tempScript.isEmpty();
		}
		return sr || this.hasNoEncryptScriptCode;
	}

	public boolean hasScriptCode() {
		return !this.getTotalCode().isEmpty();
	}

	public boolean isInit() {
		return this.fullscript != null;
	}

	public boolean isValid() {
		return this.isInit() && !this.errored;
	}

	public void readFromNBT(NBTTagCompound compound, boolean isClient) {
		if (compound.hasKey("Script", 9)) {
			NBTTagList list = compound.getTagList("Script", 8);
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < list.tagCount(); i++) {
				sb.append(list.getStringTagAt(i));
			}
			this.script = sb.toString();
		} else {
			this.script = compound.getString("Script");
		}
		this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
		this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
		this.hasNoEncryptScriptCode = compound.getBoolean("HasNoEncryptScriptCode");
		this.isClient = isClient;
		if (this.isClient) {
			this.errored = false;
		}
		this.lastCreated = 0L;
		this.fullscript = null;
		this.unknownFunctions.clear();
	}

	public void run(String type, Event event, boolean side) {
		Object key = event instanceof BlockEvent ? "Block"
				: event instanceof PlayerEvent ? "Player"
						: event instanceof ItemEvent ? "Item" : event instanceof NpcEvent ? "Npc" : null;
		CustomNpcs.debugData.startDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "ScriptContainer_run");
		this.run(type, event);
		CustomNpcs.debugData.endDebug(side ? "Server" : "Client", "Run" + key + "Script_" + type, "ScriptContainer_run");
	}

	private void run(String type, Object event) {
		if (this.engine == null) {
			this.setEngine(this.handler.getLanguage());
		}
		if (this.errored || !this.hasScriptCode() || this.unknownFunctions.contains(type) || !CustomNpcs.EnableScripting || this.engine == null) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastCreated) {
			this.lastCreated = ScriptController.Instance.lastLoaded;
			this.fullscript = null;
			this.hasNoEncryptScriptCode = false;
		}
		synchronized ("lock") {
			ScriptContainer.Current = this;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			this.engine.getContext().setWriter(pw);
			this.engine.getContext().setErrorWriter(pw);
			try {
				if (this.engine.get("dump") == null) {
					this.fillEngine();
				}
				else if (this.isClient && (this.engine.get("mc") == null || this.engine.get("storedData") == null)) {
					this.fillEngineClient();
				}
				if (this.fullscript == null) {
					this.engine.eval(this.getTotalCode());
				}
				if (this.engine.getFactory().getLanguageName().equals("lua")) {
					Object ob = this.engine.get(type);
					if (ob != null) {
						if (ScriptContainer.luaCoerce == null) {
							ScriptContainer.luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua")
									.getMethod("coerce", Object.class);
							ScriptContainer.luaCall = ob.getClass().getMethod("call",
									Class.forName("org.luaj.vm2.LuaValue"));
						}
						ScriptContainer.luaCall.invoke(ob, ScriptContainer.luaCoerce.invoke(null, event));
					} else {
						this.unknownFunctions.add(type);
					}
				} else {
					((Invocable) this.engine).invokeFunction(type, event);
				}
			} catch (NoSuchMethodException e2) {
				this.unknownFunctions.add(type);
			} catch (Throwable e) {
				this.errored = true;
				e.printStackTrace(pw);
				if (!this.isClient) { NoppesUtilServer.NotifyOPs(this.handler.noticeString() + " script errored"); }
				LogWriter.error(this.handler.noticeString() + " script errored: " + e);
			} finally {
				this.appendConsole(sw.getBuffer().toString().trim());
				pw.close();
				ScriptContainer.Current = null;
			}
		}
	}

	public void setEngine(String scriptLanguage) {
		this.engine = ScriptController.Instance.getEngineByName(scriptLanguage);
		if (this.engine == null) {
			this.errored = true;
			return;
		}
		if (this.engine.get("dump") == null) { this.fillEngine(); }
		this.fullscript = null;
		this.hasNoEncryptScriptCode = false;
	}

	private void fillEngine() {
		// Custom Functions
		for (int i = 0; i < ScriptController.Instance.constants.getTagList("Functions", 8).tagCount(); i++) {
			String body = ScriptController.Instance.constants.getTagList("Functions", 8).getStringTagAt(i);
			if (body.toLowerCase().indexOf("function ") != 0) {
				continue;
			}
			try {
				String key = body.substring(body.indexOf(" ") + 1, body.indexOf("("));
				if (!this.isClient || (!key.equals("getField") && !key.equals("setField") && !key.equals("invoke"))) {
					ScriptContainer.Data.put(key, this.engine.eval(body));
					ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).removeTag("EvalIsError");
				}
			} catch (Exception e) {
				ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).setBoolean("EvalIsError", true);
			}
		}
		// Custom Constants
		for (String key : ScriptController.Instance.constants.getCompoundTag("Constants").getKeySet()) {
			NBTBase tag = ScriptController.Instance.constants.getCompoundTag("Constants").getTag(key);
			if (tag.getId() == 8) {
				try {
					ScriptContainer.Data.put(key, this.engine.eval(((NBTTagString) tag).getString()));
				}
				catch (Exception e) { LogWriter.error("Error:", e); }
			}
		}
		// Base Functions
		ScriptContainer.Data.put("dump", new Dump());
		ScriptContainer.Data.put("log", new Log());
		// Base Constants
		try {
			ScriptContainer.Data.put("date", this.engine.eval("Java.type('" + Date.class.getName() + "')"));
			ScriptContainer.Data.put("calendar", this.engine.eval("Java.type('" + Calendar.class.getName() + "')"));
		}
		catch (Exception e) { LogWriter.error("Error:", e); }
		// Try to put all
		for (Map.Entry<String, Object> entry : ScriptContainer.Data.entrySet()) {
			try { this.engine.put(entry.getKey(), entry.getValue()); }
			catch (Exception e) { LogWriter.error("Error:", e); }
		}
		if (this.isClient) { this.fillEngineClient(); }
		this.engine.put("currentThread", Thread.currentThread().getName());
	}

	private void fillEngineClient() {
		if (!this.isClient) { return; }
		// Try to put MC
		try { this.engine.put("mc", ClientProxy.mcWrapper); }
		catch (Exception e) { LogWriter.error("Error:", e); }
		try { this.engine.put("storedData", ScriptController.Instance.clientScripts.storedData); }
		catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("Script", this.script);
		compound.setTag("Console", NBTTags.NBTLongStringMap(this.console));
		compound.setTag("ScriptList", NBTTags.nbtStringList(this.scripts));
		compound.setBoolean("isClient", this.isClient);
		compound.setBoolean("HasNoEncryptScriptCode", this.hasNoEncryptScriptCode);
		return compound;
	}

}
