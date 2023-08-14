package noppes.npcs.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.HasResult;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.constants.QuestType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.constants.TacticalType;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.client.util.EventData;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MetodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.constants.EnumEventData;
import noppes.npcs.constants.EnumInterfaceData;
import noppes.npcs.constants.EnumScriptType;

public class TempClass {
	
	public static HashMap<Class<?>, String> map = Maps.<Class<?>, String>newHashMap();
	public static HashMap<Class<?>, String> any = Maps.<Class<?>, String>newHashMap();
	public static List<File> files = Lists.<File>newArrayList();
	public static Object temp = null;
	
	@SuppressWarnings("unchecked")
	public static void run(EntityPlayerSP entity, int type) {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() || i>=100) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		List<String> enumText = Lists.newArrayList();
		if (type<=0) {
System.out.println("Collect and save APIs, Events and EnumConstants:");
			File interfaceFile = new File(dir, "interfaces.java");
			File eventFile = new File(dir, "events.java");
			File enumFile = new File(dir, "enums.java");
			//TempClass.map.clear();
			if (TempClass.map.isEmpty()) { TempClass.cheak(new File(dir, "main/java")); }
			String name = "", text = "";
			List<String> interfaceText = Lists.newArrayList(), eventText = Lists.newArrayList();
			try {
				for (Class<?> c : TempClass.map.keySet()) {
					name = c.getSimpleName();
					text = TempClass.map.get(c);
					if (text.charAt(0)=='1' && c.isInterface()) {
						if (name.charAt(0)!='I' || c.getName().indexOf("noppes.npcs.client")!=-1) { continue; }
						String n = null;
						text = text.substring(text.indexOf("interface"));
						for (Class<?> sub : c.getClasses()) {
							if (!sub.isInterface()) { continue; }
							int s = text.indexOf("{", text.indexOf(sub.getSimpleName()))+1;
							int e = text.indexOf("}", s);
							n = text.substring(s, e);
							text = text.substring(0, text.indexOf("public")) + text.substring(e+1);
							interfaceText = TempClass.addInterfase(interfaceText, n, sub.getSimpleName(), sub);
						}
						interfaceText = TempClass.addInterfase(interfaceText, text, name, c);
					} else if (text.charAt(0)=='2') {
						for (Class<?> ev : c.getClasses()) {
							if (text.indexOf(name + "(") !=-1) {
								eventText = TempClass.addEvent(eventText, text, name, c, ev);
							} else {
								System.out.println("error event: "+name+"; text: \""+text+"\"");
							}
						}
					} else if (text.charAt(0)=='3' && c.isEnum()) {
						for (Object e : c.getEnumConstants()) {
							String key = "enum." + c.getSimpleName().toLowerCase().replace("type", "");
							if (c==RoleType.class) { key = "role"; }
							else if (c==MarkType.class) { key = "mark"; }
							else if (c==JobType.class) { key = "job"; }
							else if (c==QuestType.class) { key = "quest"; }
							else if (c==TacticalType.class) { key = "aitactics"; }
							else if (c==AnimationKind.class) { key = "puppet"; }
							else if (c==OptionType.class) { key = "enum.dialog.optiont"; }
							enumText.add(key + "." + ((Enum<?>) e).name().toLowerCase());
						}
					}
				}
			}
			catch (Exception e) {
System.out.println("Class name: \""+name+"\"; text \""+text+"\"; Error: "+e);
				e.printStackTrace();
			}
			finally {
				try {
					BufferedWriter writer = Files.newBufferedWriter(interfaceFile.toPath());
					Collections.sort(interfaceText);
					Collections.sort(eventText);
					String total = "";
					for (String str : interfaceText) {
						if (!total.isEmpty()) { total += ","+((char) 10); }
						total += str;
					}
					writer.write(total);
					writer.close();
System.out.println("save file: "+interfaceFile);
					writer = Files.newBufferedWriter(eventFile.toPath());
					total = "";
					for (String str : eventText) {
						if (!total.isEmpty()) { total += ","+((char) 10); }
						total += str;
					}
					writer.write(total);
					writer.close();
System.out.println("save file: "+eventFile);
					writer = Files.newBufferedWriter(enumFile.toPath());
					total = "";
					for (String str : enumText) {
						if (!total.isEmpty()) { total += ","+((char) 10); }
						total += str;
					}
					writer.write(total);
					writer.close();
System.out.println("save file: "+enumFile);
				}
				catch (Exception e) {  }
			}
		}

		if (type==-1 || type==1) {
System.out.println("Sorting and save Langs:");
			TreeMap<String, String> map = Maps.newTreeMap();
			for (EnumInterfaceData enumID : EnumInterfaceData.values()) {
				map.put(enumID.it.comment, "");
				for (MetodData md : enumID.it.metods) {
					map.put(md.comment, "");
					for (ParameterData pd : md.parameters) {
						map.put(pd.comment, "");
					}
				}
			}
			for (EnumEventData enumED : EnumEventData.values()) {
				map.put(enumED.ed.comment, "");
				for (MetodData md : enumED.ed.metods) {
					map.put(md.comment, "");
				}
			}
			for (String str : enumText) {
				map.put(str, "");
			}
			boolean start = false;
			for (String lang : new String[] { "en_us", "ru_ru", "en_us" }) {
				File file = new File(dir, "main/resources/assets/customnpcs/lang/" + lang + ".lang");
				try {
					BufferedReader reader = Files.newBufferedReader(file.toPath());
					String line;
					while((line = reader.readLine()) != null) { 
						if (line.indexOf("=")>0) {
							map.put(line.substring(0, line.indexOf("=")), line.indexOf("=")+1<=line.length() ? line.substring(line.indexOf("=")+1) : "");
						}
					}
					reader.close();
				}
				catch (Exception e) { e.printStackTrace(); }
				if (!start) {
					start = true;
					continue;
				}
				File save = new File(dir, lang + ".lang");
				try {
					BufferedWriter writer = Files.newBufferedWriter(save.toPath());
					String total = "", pre = "";
					for (String key : map.keySet()) {
						String p = key;
						if (key.indexOf(".")!=-1) { p = key.substring(0, key.indexOf(".")); }
						if (pre.isEmpty() || !pre.equals(p)) {
							if (!pre.isEmpty()) { total += ((char) 10); }
							pre = p;
						}
						total += key + "=" + map.get(key) + ((char) 10);
					}
					writer.write(total);
					writer.close();
System.out.println("save file: "+save);
				}
				catch (Exception e) { e.printStackTrace(); }
			}
		}
		
		if (type==2) {
			dir = new File(CustomNpcs.Dir, "deobfuscations");
System.out.println("Directory: \""+dir.getName()+"\"; exists: "+dir.exists()+"; isDir: "+dir.isDirectory());
			if (TempClass.temp==null || TempClass.files.size()==0) {
				TempClass.temp = Maps.<String, String>newTreeMap();
				for (int s = 0; s<=8; s++) {
					File obf = new File(CustomNpcs.Dir, "obfuscation_"+s+".json");
System.out.println("Load "+(s+1)+"/9 - \""+obf.getAbsolutePath().substring(obf.getAbsolutePath().indexOf("customnpcs"))+"\"");
					try {
						NBTTagCompound nbt = NBTJsonUtil.LoadFile(obf);
						for (String key : nbt.getKeySet()) {
							((Map<String, String>) TempClass.temp).put(key, nbt.getString(key));
						}
					}
					catch (Exception e) {
						e.printStackTrace();
						return;
					}
				}
System.out.println("Load all java files:");
				TempClass.deobfuscation(dir);
			}
System.out.println("Total keys: "+((Map<String, String>) TempClass.temp).size());
System.out.println("Total files: "+TempClass.files.size());
			List<File> errors = Lists.<File>newArrayList();
			int p = 0, g = 0;
			for (File f : TempClass.files) {
				try {
					boolean start = true;
					BufferedReader reader = Files.newBufferedReader(f.toPath());
					String line, text = "";
					while((line = reader.readLine()) != null) {
						if (start && line.indexOf("package ")==0) { start = false; }
						if (start) { continue; }
						text += line + ((char) 10);
						if (line.indexOf("could not be decompiled")!=-1) { errors.add(f); }
					}
					reader.close();
					int r = 0;
					for (String key : ((Map<String, String>) TempClass.temp).keySet()) {
						while(text.indexOf(key)!=-1) {
							text = text.replace(key, ((Map<String, String>) TempClass.temp).get(key));
							r++;
						}
					}
					p += r;
					BufferedWriter writer = Files.newBufferedWriter(f.toPath());
					writer.write(text);
					writer.close();
System.out.println((g+1)+"/" + TempClass.files.size() + " - " +f.getAbsolutePath().substring(f.getAbsolutePath().indexOf("customnpcs"))+"; text: "+text.length()+" ; replaces: "+r);
					g++;
				}
				catch (Exception e) {  }
			}
System.out.println("Total replaces: "+p);
			if (!errors.isEmpty()) {
System.out.println("Found errors in:");
				for (File f : errors) {
					System.out.println(f.getAbsolutePath().substring(f.getAbsolutePath().indexOf("customnpcs")));
				}
			}
		}
		if (type==3) {
			System.out.println("Ctart APIs: ");
			
		}
	}

	private static void deobfuscation(File dir) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.deobfuscation(f); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(".java")) { TempClass.files.add(f); }
		}
	}

	public static List<String> addEvent(List<String> eventText, String text, String name, Class<?> c, Class<?> ev) {
		if (ev == HasResult.class || ev == Result.class || c == ForgeEvent.class) { return eventText; }
		String g1 = ev.getSimpleName().toLowerCase().replace("event", "");
		String g0 = name.toLowerCase().replace("event", "");
		String n = g0 + "." + g1;
		String fnc;
		switch(g1) {
			case "break": fnc = EnumScriptType.BROKEN.function; break;
			case "clicked": fnc = EnumScriptType.CLICKED.function; break;
			case "collided": fnc = EnumScriptType.COLLIDE.function; break;
			case "doortoggle": fnc = EnumScriptType.DOOR_TOGGLE.function; break;
			case "entityfallenupon": fnc = EnumScriptType.FALLEN_UPON.function; break;
			case "harvested": fnc = EnumScriptType.HARVESTED.function; break;
			case "init": fnc = EnumScriptType.INIT.function; break;
			case "interact": fnc = EnumScriptType.INTERACT.function; break;
			case "neighborchanged": fnc = EnumScriptType.NEIGHBOR_CHANGED.function; break;
			case "rainfill": fnc = EnumScriptType.RAIN_FILLED.function; break;
			case "redstone": fnc = EnumScriptType.REDSTONE.function; break;
			case "timer": fnc = EnumScriptType.TIMER.function; break;
			case "update":
				switch(g0) {
					case "projectile": fnc = EnumScriptType.PROJECTILE_TICK.function; break;
					default: fnc = EnumScriptType.TICK.function; break;
				}
				break;
			case "exploded": fnc = EnumScriptType.EXPLODED.function; break;
			case "close":
				switch(g0) {
					case "customcontainer": fnc = EnumScriptType.CUSTOM_CHEST_CLOSED.function; break;
					case "customgui": fnc = EnumScriptType.CUSTOM_GUI_CLOSED.function; break;
					case "dialog": fnc = EnumScriptType.DIALOG_CLOSE.function; break;
					default: fnc = ""; break;
				}
				break;
			case "slotclicked": fnc = EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function; break;
			case "slotclick": fnc = EnumScriptType.CUSTOM_GUI_SLOT_CLICKED.function; break;
			case "button": fnc = EnumScriptType.CUSTOM_GUI_BUTTON.function; break;
			case "scroll": fnc = EnumScriptType.CUSTOM_GUI_SCROLL.function; break;
			case "slot": fnc = EnumScriptType.CUSTOM_GUI_SLOT.function; break;
			case "attack": fnc = EnumScriptType.ATTACK.function; break;
			case "pickup": fnc = EnumScriptType.PICKUP.function; break;
			case "pickedup": fnc = EnumScriptType.PICKEDUP.function; break;
			case "spawn": fnc = EnumScriptType.SPAWN.function; break;
			case "tossed": fnc = EnumScriptType.TOSSED.function; break;
			case "collide": fnc = EnumScriptType.COLLIDE.function; break;
			case "damaged": fnc = EnumScriptType.DAMAGED.function; break;
			case "died": fnc = EnumScriptType.DIED.function; break;
			case "killedentity": fnc = EnumScriptType.KILL.function; break;
			case "meleeattack": fnc = EnumScriptType.ATTACK_MELEE.function; break;
			case "rangedlaunched": fnc = EnumScriptType.RANGED_LAUNCHED.function; break;
			case "target": fnc = EnumScriptType.TARGET.function; break;
			case "targetlost": fnc = EnumScriptType.TARGET_LOST.function; break;
			case "chat": fnc = EnumScriptType.CHAT.function; break;
			case "containerclosed": fnc = EnumScriptType.CONTAINER_CLOSED.function; break;
			case "containeropen": fnc = EnumScriptType.CONTAINER_OPEN.function; break;
			case "damagedentity": fnc = EnumScriptType.DAMAGED_ENTITY.function; break;
			case "factionupdate": fnc = EnumScriptType.FACTION_UPDATE.function; break;
			case "itemcrafted": fnc = EnumScriptType.ITEM_CRAFTED.function; break;
			case "itemfished": fnc = EnumScriptType.ITEM_FISHED.function; break;
			case "keypressed":
				fnc = EnumScriptType.KEY_DOWN.function + ";"
						+ EnumScriptType.KEY_UP.function + ";"
						+ EnumScriptType.MOUSE_DOWN.function + ";"
						+ EnumScriptType.MOUSE_UP.function;
				break;
			case "levelup": fnc = EnumScriptType.LEVEL_UP.function; break;
			case "login": fnc = EnumScriptType.LOGIN.function; break;
			case "logout": fnc = EnumScriptType.LOGOUT.function; break;
			case "place": fnc = EnumScriptType.PLASED.function; break;
			case "playersound":
				fnc = EnumScriptType.SOUND_PLAY.function + ";"
						+ EnumScriptType.SOUND_STOP.function;
				break;
			case "toss": fnc = EnumScriptType.TOSS.function; break;
			case "impact": fnc = EnumScriptType.PROJECTILE_IMPACT.function; break;
			case "questcanceled": fnc = EnumScriptType.QUEST_CANCELED.function; break;
			case "questcompleted": fnc = EnumScriptType.QUEST_COMPLETED.function; break;
			case "queststart": fnc = EnumScriptType.QUEST_START.function; break;
			case "questturnedin": fnc = EnumScriptType.QUEST_TURNIN.function; break;
			case "bankunlocked": fnc = EnumScriptType.ROLE.function; break;
			case "bankupgraded": fnc = EnumScriptType.ROLE.function; break;
			case "followerfinished": fnc = EnumScriptType.ROLE.function; break;
			case "followerhire": fnc = EnumScriptType.ROLE.function; break;
			case "mailman": fnc = EnumScriptType.ROLE.function; break;
			case "tradefailed": fnc = EnumScriptType.ROLE.function; break;
			case "trader": fnc = EnumScriptType.ROLE.function; break;
			case "transporterunlocked": fnc = EnumScriptType.ROLE.function; break;
			case "transporteruse": fnc = EnumScriptType.ROLE.function; break;
			case "scriptcommand": fnc = EnumScriptType.SCRIPT_COMMAND.function; break;
			case "scripttrigger": fnc = EnumScriptType.SCRIPT_TRIGGER.function; break;
			case "open": fnc = EnumScriptType.DIALOG.function; break;
			case "option": fnc = EnumScriptType.DIALOG_OPTION.function; break;
			default: fnc = ""; break;
		}
		if (fnc.isEmpty()) { return eventText; }
		for (String fn : fnc.split(";")) {
			if (fn.equals("role")) { fn = ev.getSimpleName().replace("Event", ""); }
			String func = ("" + fn.charAt(0)).toUpperCase() + fn.substring(1);
			String eventinfo = name.replace("Event", "").replace("event", "") + func + 
					"(new EventData(" + name + "." + ev.getSimpleName() + ".class, " + ((char) 10) +
					((char) 9) + ((char) 9) + name +  ".class," + ((char) 10) +
					((char) 9) + ((char) 9) + "\"event."+ n + "\"," + ((char) 10) +
					((char) 9) + ((char) 9) + "\""+ fn + "\"," + ((char) 10) +
					((char) 9) + ((char) 9);
			Class<?> sc = ev;
			boolean start = false;
			List<String> list = Lists.newArrayList();
			while(sc.getSuperclass()!=null) {
				for (Field f : sc.getFields()) {
					if (list.contains(f.getName())) { continue; }
					list.add(f.getName());
					if (start) { eventinfo += "," + ((char) 10) + ((char) 9) + ((char) 9); }
					start = true;
					String key = TempClass.getParametrName(f.getType().getSimpleName());
					if (key.indexOf(f.getType().getSimpleName().toLowerCase())==0) { key = "event."+g0+"."+f.getType().getSimpleName().toLowerCase(); }
					EventData ed = EnumEventData.get(name);
					if (ed!=null) {
						for (MetodData md : ed.metods) {
							if (!md.name.equals(key)) { continue; }
							key  = md.comment;
							break;
						}
					}
					eventinfo += "new MetodData("+f.getType().getSimpleName()+".class, \""+f.getName()+"\", \""+key+"\")";
				}
				
				sc = sc.getSuperclass();
				if (sc==CustomNPCsEvent.class) { break; }
			}
			eventinfo += ((char) 10) + ((char) 9) + ")" + ((char) 10) + ")";
			eventText.add(eventinfo);
		}
		return eventText;
	}

	public static List<String> addInterfase(List<String> list, String text, String name, Class<?> c) {
		String classes = "";
		for (Class<?> ca : TempClass.any.keySet()) {
			String a = TempClass.any.get(ca);
			if (a.indexOf("implements")!=-1) {
				String impls = a.substring(a.indexOf("implements")+10, a.indexOf("{", a.indexOf("implements")));
				while(impls.indexOf(" ")!=-1) { impls = impls.replace(" ", ""); }
				while(impls.indexOf(""+((char) 10))!=-1) { impls = impls.replace(""+((char) 10), ""); }
				for (String imp : impls.split(",")) {
					if (imp.equals(name)) {
						if (!classes.isEmpty()) { classes += ", "; }
						classes += ca.getSimpleName() + ".class";
						break;
					}
				}
			}
		}
		String extend = "null";
		int x = text.indexOf("{")!=-1 ? text.lastIndexOf("extends", text.indexOf("{")) : text.lastIndexOf("extends");
		if (x!=-1 && x < text.indexOf("{") && x > text.indexOf(name)) {
			extend = text.substring(x+8, text.indexOf("{")-1) + ".class";
		}
		if (extend.indexOf("<")!=-1) { extend = extend.substring(0, extend.indexOf("<")) + ".class"; }
		String interfaceText = name+"(new InterfaseData("+name+".class, " + ((char) 10) + 
				((char) 9) + ((char) 9) + extend +  "," + ((char) 10) + 
				((char) 9) + ((char) 9) + (classes.isEmpty() ? "null," : "new Class<?>[] { "+classes+ " },") + ((char) 10) + 
				((char) 9) + ((char) 9) + "\"interfase."+name.toLowerCase() + "\"";
		while (text.indexOf(""+((char) 9))!=-1) { text = text.replace(""+((char) 9), ""); }
		while (text.indexOf(""+((char) 10)+((char) 10))!=-1) { text = text.replace(""+((char) 10)+((char) 10), ""); }
		if (text.indexOf("{")!=-1) {
			int e = text.lastIndexOf("}");
			if (text.lastIndexOf(";")!=-1) { e = text.lastIndexOf(";"); }
			if (e!=-1 && e>=text.indexOf("{")+1) {
				text = text.substring(text.indexOf("{")+1, e);
			}
			else { text = ""; }
		}
		if (!text.isEmpty() && !text.equals(""+((char) 10))) {
			for (String m : text.split(";")) {
				if (m.replace(""+((char) 10), "").isEmpty()) { continue; }
				boolean isDeprecated = false;
				interfaceText += "," + ((char) 10) + ((char) 9) + ((char) 9);
				String clz = m.substring(0, m.indexOf(" "));
				String key = m.substring(m.indexOf(" ")+1);
				if (key.indexOf("//")!=-1) { key = key.substring(0, key.indexOf("//")); }
				String prs = key.substring(key.indexOf("(")+1, key.indexOf(")"));
				key = key.substring(0, key.indexOf("("));
				while (clz.indexOf(" ")!=-1) { clz = clz.replace(" ", ""); }
				while (clz.indexOf(""+((char) 10))!=-1) { clz = clz.replace(""+((char) 10), ""); }
				if (clz.indexOf("<")!=-1) { clz = clz.substring(0, clz.indexOf("<")); }
				while (key.indexOf(" ")!=-1) { key = key.replace(" ", ""); }
				while (key.indexOf(""+((char) 10))!=-1) { key = key.replace(""+((char) 10), ""); }
				String trKey = "method."+name.toLowerCase()+"."+key.toLowerCase();
				InterfaseData intD = EnumInterfaceData.get(name);
				if (intD!=null) {
					for (MetodData md : intD.metods) {
						if (!md.name.equals(key)) { continue; }
						trKey  = md.comment;
						break;
					}
				}
				if (clz.indexOf("@Deprecated")!=-1) {
					isDeprecated = true;
					clz = clz.replace("@Deprecated", "");
				}
				interfaceText += "new MetodData("+clz+".class, \""+key+"\", \"" + trKey + "\"";
				if (!prs.isEmpty()) {
					List<String> cP = Lists.newArrayList(), kP = Lists.newArrayList();
					while(!prs.isEmpty()) {
						int s = prs.indexOf(" ");
						if (s==-1) { s = prs.length(); }
						int e = prs.indexOf(">");
						String clzP;
						if (e>-1 && e < s) {
							s = e;
							clzP = prs.substring(0, prs.indexOf(">")+1);
						} else {
							clzP = prs.substring(0, prs.indexOf(" "));
						}
						int f = prs.indexOf(" ", s) + 1;
						String keyP = prs.substring(f, prs.indexOf(", ", f)==-1 ? prs.length() : prs.indexOf(", ", f));
						while (clzP.indexOf(" ")!=-1) { clzP = clzP.replace(" ", ""); }
						if (clzP.indexOf("<")!=-1) { clzP = clzP.substring(0, clzP.indexOf("<")); }
						while (keyP.indexOf(" ")!=-1) { keyP = keyP.replace(" ", ""); }
						cP.add(clzP);
						kP.add(keyP);
						if (prs.indexOf(", ", f)==-1) { prs = ""; break; }
						else { prs = prs.substring(prs.indexOf(", ", f)+2); }
					}
					MetodData mdD = null;
					if (intD!=null) {
						for (MetodData md : intD.metods) {
							if (!md.name.equals(key) || md.parameters.size()!=cP.size()) { continue; }
							boolean equals = true;
							for (int i = 0; i < md.parameters.size(); i++) {
								if (!md.parameters.get(i).clazz.equals(cP.get(i))) { equals = false; break; }
							}
							if (!equals) { continue; }
							mdD = md;
							break;
						}
					}
					for (int i = 0; i < cP.size(); i++) {
						trKey = TempClass.getParametrName(cP.get(i));
						if (trKey.indexOf(cP.get(i).toLowerCase())==0) { trKey = "parameter."+name.toLowerCase()+"."+kP.get(i).toLowerCase(); }
						if (mdD!=null && i < mdD.parameters.size()) { trKey = mdD.parameters.get(i).comment; }
						interfaceText += "," + ((char) 10) + ((char) 9) + ((char) 9) + ((char) 9) + 
								"new ParameterData(" + cP.get(i) + ".class, \"" + kP.get(i) + "\", \"" + trKey + "\")";
					}
					interfaceText += "" + ((char) 10) + ((char) 9) + ((char) 9);
				}
				interfaceText += ")" + (isDeprecated ? ".setDeprecated()" : "");
			}
		}
		interfaceText += "" + ((char) 10) + ((char) 9) + ")" + ((char) 10) + ")";
		list.add(interfaceText);
		return list;
	}

	public static void cheak(File dir) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.cheak(f); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(".java")) {
				try {
					String classPath = f.getAbsolutePath();
					classPath = classPath.substring(classPath.indexOf("java")+5, classPath.length()-5);
					while(classPath.indexOf("/")!=-1) { classPath = classPath.replace("/", "."); }
					while(classPath.indexOf("\\")!=-1) { classPath = classPath.replace("\\", "."); }
					Class<?> c = null;
					try { c = Class.forName(classPath); }
					catch (ClassNotFoundException e) { System.out.println("error: "+e); continue; }
					if (c == null) { continue; }
					BufferedReader reader = Files.newBufferedReader(f.toPath());
					String line, text = "";
					while((line = reader.readLine()) != null) { text += line + ((char) 10); }
					reader.close();
					boolean isSys = false;
					if (c.isInterface()) { text = "1" + text; isSys = true; }
					else if (c.isEnum() && c.getName().indexOf("noppes.npcs.api.constants")==0) { text = "3" + text; isSys = true; }
					boolean isEvent = false;
					if (c.getSuperclass()!=null) {
						Class<?> sp = c;
						while (sp.getSuperclass()!=null) {
							sp = sp.getSuperclass();
							if (sp == Event.class) {
								isEvent = true;
								break;
							}
						}
					}
					if (isEvent) { text = "2" + text; isSys = true; }
					if (isSys) { TempClass.map.put(c, text); }
					else { TempClass.any.put(c, text); }
				}
				catch (Exception e) {  }
			}
		}
	}

	public static String getParametrName(String name) {
		String key = name.toLowerCase();
		if (name.indexOf("[]")!=-1) { key = name.replace("[]", ""); }
		switch(key) {
			case "npcapi": key = "event.npcapi"; break;
			case "iblock": key = "parameter.block"; break;
			case "block": key = "parameter.block"; break;
			case "ipos": key = "parameter.pos"; break;
			case "blockpos": key = "parameter.pos"; break;
			case "itransportlocation": key = "parameter.transport.location"; break;
			case "iitemstack": key = "parameter.stack"; break;
			case "iitemscripted": key = "parameter.stack"; break;
			case "itemstack": key = "parameter.stack"; break;
			case "icontainer": key = "parameter.container"; break;
			case "container": key = "parameter.container"; break;
			case "iplayer": key = "parameter.player"; break;
			case "entityplayer": key = "parameter.player"; break;
			case "entityplayersp": key = "parameter.player"; break;
			case "entityplayermp": key = "parameter.player"; break;
			case "ientity": key = "parameter.entity"; break;
			case "ientityliving": key = "parameter.entity"; break;
			case "ientitylivingbase": key = "parameter.entity"; break;
			case "entity": key = "parameter.entity"; break;
			case "entityliving": key = "parameter.entity"; break;
			case "entitylivingbase": key = "parameter.entity"; break;
			case "icustomgui": key = "parameter.customgui"; break;
			case "idamagesource": key = "parameter.damagesource"; break;
			case "icustomnpc": key = "parameter.npc"; break;
			case "idialog": key = "parameter.dialog"; break;
			case "idialogoption": key = "parameter.dialog.option"; break;
			case "ientityitem": key = "parameter.entity.item"; break;
			case "entityitem": key = "parameter.entity.item"; break;
			case "ifaction": key = "parameter.faction"; break;
			case "faction": key = "parameter.faction"; break;
			case "iinventory": key = "parameter.inventory"; break;
			case "inventory": key = "parameter.inventory"; break;
			case "boolean": key = "parameter.boolean"; break;
			case "iprojectile": key = "parameter.projectile"; break;
			case "iquest": key = "parameter.quest"; break;
			case "factionoptions": key = "parameter.faction.options"; break;
			case "iplayermail": key = "parameter.mail"; break;
			case "playermail": key = "parameter.mail"; break;
			default: break;
		}
		
		return key;
	}
	
}
