package noppes.npcs.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
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
import net.minecraft.nbt.NBTTagLongArray;
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
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.util.ObfuscationHelper;

public class ScriptContainer {

	public static ScriptContainer Current;
	public static HashMap<String, Object> Data = Maps.<String, Object>newHashMap();
	private static Method luaCall;
	private static Method luaCoerce;
	public TreeMap<Long, String> console;
	public ScriptEngine engine;
	public boolean errored;
	public String fullscript;
	private IScriptHandler handler;
	private boolean init;
	public long lastCreated;
	public String script;
	public List<String> scripts;
	private HashSet<String> unknownFunctions;
	
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
	}

	private static void FillMap(Class<?> c) {
		if (!c.isEnum()) { return; }
		ScriptContainer.Data.put(c.getSimpleName(), c.getDeclaringClass() !=null ? c.getDeclaringClass() : c);
		for (Object e : c.getEnumConstants()) {
			try {
				Method m = e.getClass().getMethod("get");
				if (m==null || m.getReturnType()!=int.class) { continue; }
				ScriptContainer.Data.put(c.getSimpleName() + "_" + ((Enum<?>) e).name(), (int) m.invoke(e));
				LogWriter.debug("Add Base Script Constant: \"" + c.getSimpleName() + "_" + ((Enum<?>) e).name() + "\" == " + ((int) m.invoke(e)));
			}
			catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException error) {
				error.printStackTrace();
			}
		}
	}

	private static void FillMap(NBTTagCompound c) { // New
		if (!c.hasKey("Constants", 10)) { return; }
		for (String key : c.getCompoundTag("Constants").getKeySet()) {
			NBTBase tag = c.getCompoundTag("Constants").getTag(key);
			Object value = getNBTValue(tag);
			if (value != null) {
				ScriptContainer.Data.put(key, value);
				LogWriter.debug("Add Custom Script Constant: " + key + " == " + value);
			}
		}
		NpcAPI api = NpcAPI.Instance();
		ScriptContainer.Data.put("API", api); // New
		LogWriter.debug("Add Base Script Constant: \"API\" == " + api);
		BlockPosWrapper bpw = new BlockPosWrapper(BlockPos.ORIGIN);
		ScriptContainer.Data.put("PosZero", bpw);
		LogWriter.debug("Add Base Script Constant: \"PosZero\" == " + bpw);
	}

	private static Object getNBTValue(NBTBase tag) {
		Object value = null;
		switch (tag.getId()) {
			case 1: value = ((NBTTagByte) tag).getByte(); break;
			case 2: value = ((NBTTagShort) tag).getShort(); break;
			case 3: value = ((NBTTagInt) tag).getInt(); break;
			case 4: value = ((NBTTagLong) tag).getLong(); break;
			case 5: value = ((NBTTagFloat) tag).getFloat(); break;
			case 6: value = ((NBTTagDouble) tag).getDouble(); break;
			case 7: value = ((NBTTagByteArray) tag).getByteArray(); break;
			case 8: value = ((NBTTagString) tag).getString(); break;
			case 9: // NBTTagList
				List<Object> list = Lists.<Object>newArrayList();
				for (NBTBase obj : (NBTTagList) tag) {
					Object v = getNBTValue(obj);
					if (v != null) { list.add(v); }
				}
				value = list.toArray(new Object[list.size()]);
				break;
			case 10: // NBTTagCompound
				Map<String, Object> comp = Maps.<String, Object>newTreeMap();
				for (String key : ((NBTTagCompound) tag).getKeySet()) {
					Object v = getNBTValue(((NBTTagCompound) tag).getTag(key));
					if (v != null) { comp.put(key, v); }
				}
				value = comp;
				break;
			case 11: value = ((NBTTagIntArray) tag).getIntArray(); break;
			case 12: value = ObfuscationHelper.getValue(NBTTagLongArray.class, (NBTTagLongArray) tag, long[].class); break;
		}
		return value;
	}

	public ScriptContainer(IScriptHandler handler) {
		this.fullscript = "";
		this.script = "";
		this.console = new TreeMap<Long, String>();
		this.errored = false;
		this.scripts = new ArrayList<String>();
		this.unknownFunctions = new HashSet<String>();
		this.lastCreated = 0L;
		this.engine = null;
		this.handler = null;
		this.init = false;
		this.handler = handler;
	}

	public void appandConsole(String message) {
		if (message == null || message.isEmpty()) {
			return;
		}
		long time = System.currentTimeMillis();
		if (this.console.containsKey(time)) {
			message = this.console.get(time) + "\n" + message;
		}
		this.console.put(time, message);
		while (this.console.size() > 40) {
			this.console.remove(this.console.firstKey());
		}
	}

	public String getFullCode() {
		if (!this.init) {
			this.fullscript = this.script;
			if (!this.fullscript.isEmpty()) {
				this.fullscript += "\n";
			}
			for (String loc : this.scripts) {
				String code = ScriptController.Instance.scripts.get(loc);
				if (code != null && !code.isEmpty()) {
					this.fullscript = this.fullscript + code + "\n";
				}
			}
			this.unknownFunctions = new HashSet<String>();
		}
		return this.fullscript;
	}

	public boolean hasCode() {
		return !this.getFullCode().isEmpty();
	}

	public boolean isValid() {
		return this.init && !this.errored;
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.script = compound.getString("Script");
		this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
		this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
		this.lastCreated = 0L;
	}

	public void run(EnumScriptType type, Event event) {
		Object key = event instanceof BlockEvent ? "Block"
				: event instanceof PlayerEvent ? "Player"
						: event instanceof ItemEvent ? "Item"
								: event instanceof NpcEvent ? "Npc" : null;
		CustomNpcs.debugData.startDebug("Server", "In "+key, "ScriptContainer_run_" + type.function);
		this.run(type.function, event);
		CustomNpcs.debugData.endDebug("Server", key, "ScriptContainer_run_" + type.function);
	}

	public void run(String type, Object event) {
		if (this.engine==null) { this.setEngine(this.handler.getLanguage()); }
		if (this.errored || !this.hasCode() || this.unknownFunctions.contains(type) || !CustomNpcs.EnableScripting) {
			return;
		}
		if (this.engine == null) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastCreated) {
			this.lastCreated = ScriptController.Instance.lastLoaded;
			this.init = false;
		}
		synchronized ("lock") {
			ScriptContainer.Current = this;
			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);
			this.engine.getContext().setWriter(pw);
			this.engine.getContext().setErrorWriter(pw);
			try {
				if (!this.init) {
					this.engine.eval(this.getFullCode());
					this.init = true;
				}
				if (this.engine.getFactory().getLanguageName().equals("lua")) {
					Object ob = this.engine.get(type);
					if (ob != null) {
						if (ScriptContainer.luaCoerce == null) {
							ScriptContainer.luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua").getMethod("coerce", Object.class);
							ScriptContainer.luaCall = ob.getClass().getMethod("call", Class.forName("org.luaj.vm2.LuaValue"));
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
				NoppesUtilServer.NotifyOPs(this.handler.noticeString() + " script errored", new Object[0]);
			} finally {
				this.appandConsole(sw.getBuffer().toString().trim());
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
		// New
		if (!ScriptContainer.Data.containsKey("dump")) {
			for (int i=0; i<ScriptController.Instance.constants.getTagList("Functions", 8).tagCount(); i++) {
				String body = ScriptController.Instance.constants.getTagList("Functions", 8).getStringTagAt(i);
				if (body.toLowerCase().indexOf("function ")!=0) { continue; }
				try {
					String key = body.substring(body.indexOf(" ")+1, body.indexOf("("));
					ScriptContainer.Data.put(key, this.engine.eval(body));
					ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).removeTag("EvalIsError");
				}
				catch (Exception e) {
					ScriptController.Instance.constants.getTagList("Functions", 10).getCompoundTagAt(i).setBoolean("EvalIsError", true);
				}
			}
			for (String key : ScriptController.Instance.constants.getCompoundTag("Constants").getKeySet()) {
				NBTBase tag = ScriptController.Instance.constants.getCompoundTag("Constants").getTag(key);
				if (tag.getId()==8) {
					try {
						ScriptContainer.Data.put(key, this.engine.eval(((NBTTagString) tag).getString()));
					}
					catch (Exception e) { }
				}
			}
			ScriptContainer.Data.put("dump", new Dump());
			ScriptContainer.Data.put("log", new Log());
		}
		for (Map.Entry<String, Object> entry : ScriptContainer.Data.entrySet()) {
			this.engine.put(entry.getKey(), entry.getValue());
		}
		this.init = false;
	}

	public boolean varIsConstant(String name) {
		return ScriptContainer.Data.containsKey(name);
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setString("Script", this.script);
		compound.setTag("Console", NBTTags.NBTLongStringMap(this.console));
		compound.setTag("ScriptList", NBTTags.nbtStringList(this.scripts));
		return compound;
	}
	
	// New
	public class Dump implements Function<Object, IDataObject> {

		@Override
		public IDataObject apply(Object o) {
			return new DataObject(o);
		}

	}

	public class Log implements Function<Object, Void> {
		@Override
		public Void apply(Object o) {
			ScriptContainer.this.appandConsole(o + "");
			LogWriter.info(o + "");
			return null;
		}
	}

	public static void reloadConstants() {
		ScriptContainer.Data.remove("dump");
	}

}
