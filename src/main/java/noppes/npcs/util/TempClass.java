package noppes.npcs.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PackageReceived;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.CustomPotionEvent;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.IsReadyEvent;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.util.EventData;
import noppes.npcs.client.util.InterfaseData;
import noppes.npcs.client.util.MethodData;
import noppes.npcs.client.util.ParameterData;
import noppes.npcs.constants.EnumEventData;
import noppes.npcs.constants.EnumInterfaceData;

public class TempClass {

	public static Map<Class<?>, String> apis = Maps.<Class<?>, String>newHashMap();
	public static Map<Class<?>, String> evs = Maps.<Class<?>, String>newHashMap();
	public static Map<String, String> imps = Maps.<String, String>newHashMap();
	public static Map<Class<?>, List<Class<?>>> fulls = Maps.<Class<?>, List<Class<?>>>newHashMap();
	public static List<File> javaFiles = Lists.<File>newArrayList();
	
	public static int ii, ie, iv;

	public static void deobfucation() {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() && i<=10) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		if (!dir.exists()) { return; }
		final File dirSave = new File(dir, "deobfucation");
		if (!dirSave.exists()) { dirSave.mkdirs(); return; }
System.out.println("Start deobfucation in work place: \""+dirSave.getAbsolutePath()+"\"");
		if (TempClass.javaFiles.isEmpty()) {
			TempClass.collectFiles(dirSave, ".java");
		}
		TreeMap<String, String> map = AdditionalMethods.instance.obfuscations;
		TempClass.iv = 1;
		long g = 0;
		for (File f : TempClass.javaFiles) {
			String fileText = null;
			try { fileText = Files.toString(f, Charset.forName("UTF-8")); } catch (IOException e) { }
			if (fileText==null) { continue; }
			for (String key : map.keySet()) {
				while(fileText.indexOf(key)!=-1) {
					fileText = fileText.replace(key, map.get(key));
					g++;
				}
			}
			if (fileText.indexOf("package ")!=-1) { fileText = fileText.substring(fileText.indexOf("package ")); }
			try {
				Files.write(fileText.getBytes(), f);
System.out.println("Save Deobfucation["+TempClass.iv+"/"+TempClass.javaFiles.size()+"]: "+f.getAbsolutePath());
				TempClass.iv++;
			} catch (Exception e) { }
		}
System.out.println("Total deobfucation Files: "+TempClass.iv+"; total correct keys: "+g);
	}

	public static void createAPIs(boolean acceptAllVariablesAndMethods) {
		File dir = new File(CustomNpcs.Dir, "src");
		int i = 0;
		while (!dir.exists() && i<=10) {
			dir = dir.getParentFile().getParentFile();
			dir = new File(dir, "src");
			i++;
		}
		if (!dir.exists()) { return; }
System.out.println("Start created "+(acceptAllVariablesAndMethods ? "full " : "")+"APIs TypeScript in work place: \""+dir.getAbsolutePath()+"\"");
//TempClass.apis.clear();
		if (TempClass.apis.isEmpty()) {
			TempClass.imps.clear();
			TempClass.evs.clear();
			TempClass.fulls.clear();
			TempClass.collectInterfases(dir, acceptAllVariablesAndMethods);
			if (acceptAllVariablesAndMethods && !TempClass.fulls.isEmpty()) {
				Map<Class<?>, List<Class<?>>> newFulls = Maps.<Class<?>, List<Class<?>>>newHashMap();
				for (Class<?> z : TempClass.fulls.keySet()) {
					Class<?> cv;
					if (z.getInterfaces().length==1) { cv = z.getInterfaces()[0]; }
					else { cv = z.getSuperclass(); }
					if (cv==null) { continue; }
					if (TempClass.apis.containsKey(cv)) {
						if (!newFulls.containsKey(cv)) { newFulls.put(cv, Lists.<Class<?>>newArrayList()); }
						newFulls.get(cv).add(z);
					}
				}
				TempClass.fulls = newFulls;
			}
		}
		if (TempClass.apis.isEmpty()) { return; }
		final File dirSave = new File(dir, "api");
		if (!dirSave.exists()) { dirSave.mkdirs(); }
		String tab = "    ";
		String ent = ""+((char) 10);

		List<String> baseLangKeys = Lists.<String>newArrayList();
		Map<String, String> enumInterfaceData = Maps.<String, String>newTreeMap();
		
		Map<String, Object> consts = Maps.<String, Object>newTreeMap();
		TempClass.ii = 1;
		for (Class<?> c : TempClass.apis.keySet()) {
			if (c.isEnum()) {
				try {
					Method m = c.getDeclaredMethod("get");
					if (!m.isAccessible()) { m.setAccessible(true); }
					for (Object en : c.getEnumConstants()) {
						consts.put(c.getSimpleName()+"_"+en.toString(), m.invoke(en));
					}
				} catch (Exception e) { }
System.out.println("Found Constant Class["+TempClass.ii+"/"+TempClass.apis.size()+"]: "+c.getSimpleName());
				TempClass.ii++;
				continue;
			}
			
			InterfaseData ind = new InterfaseData();
			InterfaseData baseInd = EnumInterfaceData.get(c.getSimpleName());
			String cName = c.getSimpleName();
			if (acceptAllVariablesAndMethods) {
				ind.interfaseClass = c;
				ind.comment = "interfase." + cName.toLowerCase();
				if (baseInd!=null) { ind.comment = baseInd.comment; }
			}
			List<String> imps = Lists.newArrayList();
			File apiFile;
			if (!TempClass.imps.get(c.getSimpleName()).isEmpty()) {
				apiFile = new File(dirSave, TempClass.imps.get(c.getSimpleName())+".ts");
			} else {
				apiFile = new File(dirSave, c.getSimpleName()+".ts");
			}
			if (c.getInterfaces().length==1 && TempClass.imps.containsKey(c.getInterfaces()[0].getSimpleName())) {
				if (acceptAllVariablesAndMethods) { ind.extendClass = c.getInterfaces()[0]; }
				String subName = c.getInterfaces()[0].getSimpleName();
				if (!subName.equals(cName) && TempClass.imps.containsKey(subName) && !imps.contains(subName)) { imps.add(subName); }
			} else if (acceptAllVariablesAndMethods && baseInd!=null) { ind.extendClass = baseInd.extendClass; }
			Map<String, String> fs = Maps.<String, String>newTreeMap();
			Map<String, String> ms = Maps.<String, String>newTreeMap();
			List<Method> metods = Lists.newArrayList(c.getDeclaredMethods());
			String pName = "";
			if (acceptAllVariablesAndMethods) {
				if (TempClass.fulls.containsKey(c)) {
					ind.wraperClass = TempClass.fulls.get(c).toArray(new Class<?>[TempClass.fulls.get(c).size()]);
					for (Class<?> z : TempClass.fulls.get(c)) {
						if (!pName.isEmpty()) { pName += ", "; }
						pName += z.getSimpleName();
						for (Field fl : z.getFields()) {
							if (Modifier.isStatic(fl.getModifiers()) || fs.containsKey(fl.getName())) { continue; }
							fs.put(fl.getName(), tab+fl.getName()+": "+TempClass.getTitle(fl.getType())+"; // "+z.getSimpleName());
							if (TempClass.imps.containsKey(fl.getType().getSimpleName())) {
								String subName = fl.getType().getSimpleName();
								if (!subName.equals(cName) && TempClass.imps.containsKey(subName) && !imps.contains(subName)) { imps.add(subName); }
							}
						}
						for (Method m : z.getMethods()) {
							if (!m.isAccessible() || metods.contains(m)) { continue; }
							metods.add(m);
						}
					}
				} else if (baseInd != null) { ind.wraperClass = baseInd.wraperClass; }
			}
			for (Method m : metods) {
				String mName = m.getName();
				if (Modifier.isStatic(m.getModifiers()) || mName.equals("equals") || mName.equals("toString") || mName.equals("wait") || mName.equals("notify") || mName.equals("notifyAll") || mName.equals("hashCode")) { continue; }
				String type = TempClass.getTitle(m.getReturnType());
				String added = "";
				String key = mName+"(";
				String body = "";
				MethodData md = new MethodData();
				MethodData baseMd = null;
				if (baseInd != null) { baseMd = baseInd.getMethodData(m); }
				if (acceptAllVariablesAndMethods) {
					md.name = mName;
					md.returnType = m.getReturnType();
					if (md.returnType!=null) {
						md.returnTypeName = "c" + md.returnType.getSimpleName();
						if (md.returnType.isInterface()) { md.returnTypeName = "9" + md.returnType.getSimpleName(); }
						else if (md.returnType == boolean.class || md.returnType == byte.class || md.returnType == short.class || md.returnType == int.class || md.returnType == float.class || md.returnType == double.class || md.returnType == long.class || md.returnType == String.class) { md.returnTypeName = "e" + md.returnType.getSimpleName(); }
						else if (md.returnType == Void.class) { md.returnTypeName = "8void"; }
					}
					md.comment = "method."+cName.toLowerCase()+"."+mName.toLowerCase();
					if (baseMd != null) {
						md.comment = baseMd.comment;
						md.isDeprecated = baseMd.isDeprecated;
						md.isVarielble = baseMd.isVarielble;
					}
					md.parameters = new ParameterData [m.getParameterCount()];
				}
				if (m.getParameterCount()>0) {
					String fileText = ""+TempClass.apis.get(c);
					String[] names = null;
					names = TempClass.getNames(fileText, mName, m);
					Parameter[] ps = m.getParameters();
					for (int h = 0; h < ps.length; h++) {
						if (!body.isEmpty()) { body += ", "; }
						String name = ps[h].getName();
						if (names!=null && h<names.length) { name = names[h];}
						if (TempClass.imps.containsKey(ps[h].getType().getSimpleName())) {
							String subName = ps[h].getType().getSimpleName();
							if (!subName.equals(cName) && TempClass.imps.containsKey(subName) && !imps.contains(subName)) { imps.add(subName); }
						}
						body += name+": "+TempClass.getTitle(ps[h].getType());
						if (acceptAllVariablesAndMethods) {
							ParameterData pd = new ParameterData(ps[h].getType(), name, "parameter."+cName.toLowerCase()+"."+name.toLowerCase());
							if (baseMd != null) {
								ParameterData basePd = baseMd.getParameterData(h, ps[h]);
								if (basePd != null) { pd.comment = basePd.comment; }
							}
							md.parameters[h] = pd;
						}
					}
				}
				if (!type.equals(cName) && TempClass.imps.containsKey(type) && !imps.contains(type)) { imps.add(type); }
				added = tab+mName+"("+body+"): "+type;
				if ((mName.indexOf("get")==0 && !mName.equals("get")) || (mName.indexOf("is")==0 && !mName.equals("is"))) {
					int p = 3;
					if (mName.indexOf("is")==0) { p = 2; }
					added += ";"+ent+tab+"get "+(""+mName.charAt(p)).toLowerCase()+(mName.substring(p+1))+"("+body+"): "+type;
				}
				else if (mName.indexOf("set")==0 && !mName.equals("set")) {
					added += ";"+ent+tab+"set "+(""+mName.charAt(3)).toLowerCase()+(mName.substring(4))+"("+body+"): "+type;
				}
				added += ";";
				if (acceptAllVariablesAndMethods) {
					if (!pName.isEmpty()) {
						boolean isM = true;
						for (Method mh : c.getDeclaredMethods()) {
							if (m.equals(mh)) {
								isM = false;
								break;
							}
						}
						if (isM) { added += " // "+pName;}
					}
					ind.metods.add(md);
				}
				key += "("+body+"): "+type;
				ms.put(key, added);
			}
			
			// API
			String text = "";
			Collections.sort(imps);
			for (String imp : imps) {
				String imprt;
				if (TempClass.imps.get(imp).isEmpty()) { imprt = "import { "+imp+" } from \"./"+imp+"\";"; }
				else { imprt = "import { "+imp+" } from \"./"+TempClass.imps.get(imp)+"\";"; }
				text += imprt+ent;
			}
			text += "export interface "+c.getSimpleName();
			if (c.getInterfaces().length==1 && TempClass.imps.containsKey(c.getInterfaces()[0].getSimpleName())) {
				text += " extends "+c.getInterfaces()[0].getSimpleName();
			}
			text += " {"+ent;
			if (!fs.isEmpty()) {
				for (String value : fs.values()) {
					text += value+ent;
				}
				text += ent;
			}
			for (String value : ms.values()) {
				text += value+ent+ent;
			}
			text = text.substring(0, text.length()-1)+"}";
			try {
				if (!apiFile.getParentFile().exists()) { apiFile.getParentFile().mkdirs(); }
				if (!apiFile.exists()) { apiFile.createNewFile(); }
				Files.write(text.getBytes(), apiFile);
System.out.println("Save api["+TempClass.ii+"/"+TempClass.apis.size()+"/"+EnumInterfaceData.values().length+"]: "+apiFile.getAbsolutePath());
				TempClass.ii++;
			} catch (Exception e) { }
			if (acceptAllVariablesAndMethods && ind!=null) {
				enumInterfaceData.put(cName, ind.getEnumCode());
				if (!baseLangKeys.contains(ind.comment)) { baseLangKeys.add(ind.comment); }
				for (MethodData md : ind.metods) {
					if (!baseLangKeys.contains(md.comment)) { baseLangKeys.add(md.comment); }
					for (ParameterData pd : md.parameters) {
						if (!baseLangKeys.contains(pd.comment)) { baseLangKeys.add(pd.comment); }
					}
				}
			}
		}
		
		//Interfase Enums
		File enumInterfaseFile = new File(dir, "EnumInterfaceData.java");
		try {
			if (!enumInterfaseFile.exists()) { enumInterfaseFile.createNewFile(); }
			String text = "";
			for (String code : enumInterfaceData.values()) {
				if (!text.isEmpty()) { text += ","+ent; }
				text += code;
			}
			Files.write(text.getBytes(), enumInterfaseFile);
System.out.println("Save EnumInterfaceData: "+enumInterfaseFile.getAbsolutePath());
		} catch (Exception e) { }
		
		// Constants:
		consts.put("api", "NpcAPI.Instance()");
		consts.put("Java", "Java");
		String text = "", pre = "";
		for (String key : consts.keySet()) {
			if (key.indexOf("_")!=-1) {
				String clazz = key.substring(0, key.indexOf("_"));
				if (pre.isEmpty() || !pre.equals(clazz)) {
					pre = clazz;
					text += "/**@type{ "+pre+" }*/"+ent;
				}
			}
			else { text += "/**@type{ "+key+" }*/"+ent; }
			text += "let "+key+" = "+consts.get(key)+";"+ent;
		}
		File constsFile = new File(dirSave, "constants.js");
		try {
			if (!constsFile.exists()) { constsFile.createNewFile(); }
			Files.write(text.getBytes(), constsFile);
System.out.println("Save constants: "+constsFile.getAbsolutePath());
		} catch (Exception e) { }
		
		
		// Events
		Map<String, String> enumEventData = Maps.<String, String>newTreeMap();
		final File dTemp = new File(dirSave, "event");
		if (!dTemp.exists()) { dTemp.mkdirs(); }
		
		TempClass.ie = 1;
		for (Class<?> c : TempClass.evs.keySet()) {
			String cName = c.getSimpleName();
			List<String> imps = Lists.newArrayList();
			String subName = c.getSuperclass().getSimpleName();
			if (!subName.equals(cName) && !imps.contains(subName)) {
				if (TempClass.evs.containsKey(c.getSuperclass()) || TempClass.imps.containsKey(subName)) { imps.add(subName); }
			}
			String addEvents = "";
			for (Class<?> e : c.getDeclaredClasses()) {
				String eName = e.getSimpleName();
				EventData eed = new EventData();
				eed.extend = c;
				EventData baseEed = EnumEventData.get(cName, eName);
				if (acceptAllVariablesAndMethods) {
					eed.event = e;
					eed.comment = "event." + eName.toLowerCase();
					if (baseEed!=null) {
						eed.comment = baseEed.comment;
						eed.func = baseEed.func;
					}
				}
				addEvents += "export interface "+eName+" extends "+cName;
				String func = EnumEventData.get(e);
				if (!func.isEmpty()) { func = " // "+func; }
				if (e.getDeclaredFields().length>0) {
					addEvents += " {"+func;
					boolean added = false;
					for (Field fl : e.getDeclaredFields()) {
						if (fl.getName().equals("LISTENER_LIST")) { continue; }
						added = true;
						addEvents += ent+tab+fl.getName()+": "+TempClass.getTitle(fl.getType())+";";
						if (TempClass.imps.containsKey(fl.getType().getSimpleName())) {
							String sName = fl.getType().getSimpleName();
							if (!subName.equals(cName) && TempClass.imps.containsKey(sName) && !imps.contains(sName)) { imps.add(sName); }
						}
					}
					if (added) { addEvents += ent; }
					addEvents += "}";
				} else { addEvents += " {}"+func; }
				if (acceptAllVariablesAndMethods || e.getFields().length>0) {
					int g = 0;
					List<MethodData> lmd = Lists.<MethodData>newArrayList();
					for (Field fl : (e==ForgeEvent.InitEvent.class ? e.getDeclaredFields() : e.getFields())) {
						if (fl.getName().equals("LISTENER_LIST")) { continue; }
						MethodData md = new MethodData(fl.getType(), fl.getName(), "parameter."+eName.toLowerCase()+"."+fl.getName().toLowerCase());
						if (baseEed!=null) {
							MethodData baseMd = baseEed.getMetods(g, fl);
							if (baseMd!=null) {
								md.comment = baseMd.comment;
								md.name = baseMd.name;
							} else {
								System.out.println(cName+"."+eName+"; ["+g+"] "+fl.getName()+" - not found in class: "+baseEed.event.getName());
							}
						}
						lmd.add(md);
						g++;
					}
					eed.metods = lmd.toArray(new MethodData[lmd.size()]);
				}
				TempClass.ie++;
				addEvents += ent;
				if (acceptAllVariablesAndMethods && e!=ForgeEvent.InitEvent.class && e!=Event.class && e!=NpcEvent.class && e!=CustomPotionEvent.class) {
					String code = eed.getEnumCode();
					if (!eed.func.equals("unknownFunction")) {
						String key = cName+"."+eName;
						if (eName.equals("PlayerSound")) {
							key = "PlayerSoundPlayed";
							code = code.replace("PlayerPlayerSound(", "PlayerSoundPlay(");
						}
						enumEventData.put(key, code);
						if (cName.equals("PlayerEvent") && eName.equals("KeyPressedEvent")) {
							enumEventData.put("PlayerKeyDown", code.replace("PlayerKeyPressed", "PlayerKeyDown").replace("event.player.key.up", "event.player.key.down").replace(".KEY_UP", ".KEY_DOWN"));
							enumEventData.put("PlayerMousePressed", code.replace("PlayerKeyPressed", "PlayerMousePressed").replace("event.player.key.up", "event.player.mouse.up").replace(".KEY_UP", ".MOUSE_UP"));
							enumEventData.put("PlayerMouseDown", code.replace("PlayerKeyPressed", "PlayerMouseDown").replace("event.player.key.up", "event.player.mouse.down").replace(".KEY_UP", ".MOUSE_DOWN"));
							TempClass.ie += 3;
						}
						if (cName.equals("PlayerEvent") && eName.equals("PlayerSound")) {
							enumEventData.put("PlayerSoundStoped", code.replace("PlayerSoundPlay(", "PlayerSoundStop(").replace("event.player.sound.play", "event.player.sound.stop").replace(".SOUND_PLAY", ".SOUND_STOP"));
							TempClass.ie ++;
						}
						if (!baseLangKeys.contains(eed.comment)) { baseLangKeys.add(eed.comment); }
						for (MethodData md : eed.metods) {
							if (!baseLangKeys.contains(md.comment)) { baseLangKeys.add(md.comment); }
						}
					}
				}
			}
			if (acceptAllVariablesAndMethods && (c==ForgeEvent.class || c==PackageReceived.class || c==PerformEffect.class || c==IsReadyEvent.class || c==EndEffect.class || c==AffectEntity.class)) {
				EventData eed = new EventData();
				eed.event = c;
				eed.extend = null;
				eed.comment = "event.forge";
				eed.func = "any_forge_event";
				EventData baseEed = EnumEventData.get(null, cName);
				if (baseEed!=null) {
					eed.comment = baseEed.comment;
					eed.func = baseEed.func;
				}
				int g = 0;
				List<MethodData> lmd = Lists.<MethodData>newArrayList();
				for (Field fl : c.getFields()) {
					if (fl.getName().equals("LISTENER_LIST")) { continue; }
					MethodData md = new MethodData(fl.getType(), fl.getName(), "parameter."+cName.toLowerCase()+"."+fl.getName().toLowerCase());
					if (baseEed!=null) {
						MethodData baseMd = baseEed.getMetods(g, fl);
						if (baseMd!=null) {
							md.comment = baseMd.comment;
							md.name = baseMd.name;
						} else {
							System.out.println(cName+"; ["+g+"] "+fl.getName()+" - not found in class: "+baseEed.event.getName());
						}
					}
					lmd.add(md);
					g++;
				}
				eed.metods = lmd.toArray(new MethodData[lmd.size()]);
				enumEventData.put(cName, eed.getEnumCode());
				if (!baseLangKeys.contains(eed.comment)) { baseLangKeys.add(eed.comment); }
				for (MethodData md : eed.metods) {
					if (!baseLangKeys.contains(md.comment)) { baseLangKeys.add(md.comment); }
				}
			}
			if (c.getDeclaredFields().length>0) {
				for (Field fl : c.getDeclaredFields()) {
					if (fl.getName().equals("LISTENER_LIST")) { continue; }
					if (TempClass.imps.containsKey(fl.getType().getSimpleName())) {
						String sName = fl.getType().getSimpleName();
						if (!subName.equals(cName) && TempClass.imps.containsKey(sName) && !imps.contains(sName)) { imps.add(sName); }
					}
				}
			}
			text = "";
			Collections.sort(imps);
			for (String imp : imps) {
				String imprt = "";
				for (Class<?> ev : TempClass.evs.keySet()) {
					if (ev.getSimpleName().equals(imp)) {
						imprt = "import { " + imp + " } from \"./" + imp + "\";";
						break;
					}
				}
				if (imprt.isEmpty()) {
					if (TempClass.imps.get(imp).isEmpty()) { imprt = "import { "+imp+" } from \"../"+imp+"\";"; }
					else { imprt = "import { "+imp+" } from \"../"+TempClass.imps.get(imp)+"\";"; }
				}
				text += imprt+ent;
			}
			
			text += "export interface "+cName+" extends "+subName;
			if (c.getDeclaredFields().length>0) {
				text += " {"+ent;
				for (Field fl : c.getDeclaredFields()) {
					if (fl.getName().equals("LISTENER_LIST")) { continue; }
					text += tab+fl.getName()+": "+TempClass.getTitle(fl.getType())+";"+ent;
				}
				text += "}";
			}
			else { text += " {}"; }
			if (!addEvents.isEmpty()) { text += ent+addEvents; }
			File eventFile = new File(dTemp, cName+".ts");
			try {
				if (!eventFile.exists()) { eventFile.createNewFile(); }
				Files.write(text.getBytes(), eventFile);
System.out.println("Save event["+TempClass.ie+"/"+TempClass.evs.size()+"/"+EnumEventData.values().length+"]: "+eventFile.getAbsolutePath());
			} catch (Exception e) { }
		}
		
		//Interfase Enums
		File enumEventFile = new File(dir, "EnumEventData.java");
		try {
			if (!enumEventFile.exists()) { enumEventFile.createNewFile(); }
			text = "";
			for (String code : enumEventData.values()) {
				if (!text.isEmpty()) { text += ","+ent; }
				text += code;
			}
			Files.write(text.getBytes(), enumEventFile);
System.out.println("Save EnumEventData: "+enumEventFile.getAbsolutePath());
		} catch (Exception e) { }
		
		baseLangKeys.remove("Integer");
		Map<String, String> baseLangs = Maps.<String, String>newTreeMap();
		for (String str : baseLangKeys) { baseLangs.put(str, ""); }
		
		boolean start = false;
		for (String lang : new String[] { "en_us", "ru_ru", "en_us" }) {
			File file = new File(dir, "main/resources/assets/customnpcs/lang/" + lang + ".lang");
			try {
				BufferedReader reader = java.nio.file.Files.newBufferedReader(file.toPath());
				String line;
				while((line = reader.readLine()) != null) { 
					if (line.indexOf("=")>0) {
						baseLangs.put(line.substring(0, line.indexOf("=")), line.indexOf("=")+1<=line.length() ? line.substring(line.indexOf("=")+1) : "");
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
				text = "";
				pre = "";
				for (String key : baseLangs.keySet()) {
					String p = key;
					if (key.indexOf(".")!=-1) { p = key.substring(0, key.indexOf(".")); }
					if (pre.isEmpty() || !pre.equals(p)) {
						if (!pre.isEmpty()) { text += ((char) 10); }
						pre = p;
					}
					text += key + "=" + baseLangs.get(key) + ((char) 10);
				}
				Files.write(text.getBytes(), save);
System.out.println("Save Land data: "+save);
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
	private static String[] getNames(String fileText, String mName, Method m) {
		String[] names = null;
		if (fileText.indexOf(mName)>0) {
			int i = 0;
			while (names==null && i<10) {
				i++;
				String b = fileText.substring(fileText.indexOf("(", fileText.indexOf(mName))+1);
				b = b.substring(0, b.indexOf(")"));
				while (b!=null && b.indexOf("<")!=-1) {
					b = b.substring(0, b.indexOf("<")) + b.substring(b.indexOf(">")+1);
				}
				if (b==null) { return names; }
				if (b.split(",").length == m.getParameterCount()) {
					names = b.split(",");
					for (int h = 0; h < names.length; h++) {
						names[h] = names[h].substring(names[h].lastIndexOf(" ")+1);
					}
					break;
				}
				fileText = fileText.substring(fileText.indexOf("(", fileText.indexOf(mName))+1);
			}
		}
		return names;
	}

	private static String getTitle(Class<?> clazz) {
		String type = clazz.getSimpleName(), sfx = "";
		if (clazz.isArray()) {
			type = type.replace("[]", "");
			sfx = "[]";
		}
		if (type.equalsIgnoreCase("void")) { type = "void"; }
		else if (type.equalsIgnoreCase("boolean")) { type = "boolean"; }
		else if (type.equalsIgnoreCase("char") || type.equalsIgnoreCase("string")) { type = "string"; }
		else if (type.equalsIgnoreCase("byte") ||
				type.equalsIgnoreCase("short") ||
				type.equalsIgnoreCase("int") ||
				type.equalsIgnoreCase("integer") ||
				type.equalsIgnoreCase("long") ||
				type.equalsIgnoreCase("float") ||
				type.equalsIgnoreCase("double")) {
			type = "number";
		}
		else if (!TempClass.apis.containsKey(clazz)) { type = "any"; }
		return type + sfx;
	}
	
	private static void collectFiles(File dir, String sfx) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.collectFiles(f, sfx); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(sfx)) { TempClass.javaFiles.add(f); }
		}
	}
	
	public static void collectInterfases(File dir, boolean acceptAllVariablesAndMethods) {
		for (File f : dir.listFiles()) {
			if (f.getName().equals("assets")) { continue; }
			if (f.isDirectory()) { TempClass.collectInterfases(f, acceptAllVariablesAndMethods); }
			if (f.isFile() && f.getName().toLowerCase().endsWith(".java")) {
				try { 
					String classPath = f.getAbsolutePath();
					classPath = classPath.substring(classPath.indexOf("java")+5, classPath.length()-5);
					while(classPath.indexOf("/")!=-1) { classPath = classPath.replace("/", "."); }
					while(classPath.indexOf("\\")!=-1) { classPath = classPath.replace("\\", "."); }
					Class<?> c = null;
					try { c = Class.forName(classPath); } catch (ClassNotFoundException e) { continue; }
					if (c.getName().indexOf(".client")!=-1) { continue; }
					if (acceptAllVariablesAndMethods && !c.isInterface() && (c.getInterfaces().length==1 || c.getName().indexOf("wrapper")!=-1)) {
						TempClass.fulls.put(c, null);
					}
					if (c.getSuperclass()==CustomNPCsEvent.class || c.getSuperclass()==Event.class || c.getSuperclass()==NpcEvent.class || c.getSuperclass()==CustomPotionEvent.class) {
						try { TempClass.evs.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						continue;
					}
					if (c.isEnum() && c.getName().startsWith("noppes.npcs.api.constants")) {
						try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { }
						continue;
					}
					if ((c.isInterface() &&
							c.getSimpleName().startsWith("I") &&
							c.getSimpleName().toLowerCase().indexOf("listener")==-1 &&
							c!=IDataHolder.class) || c==NpcAPI.class) {
						if (c.getName().startsWith("noppes.npcs.api.")) {
							try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { continue; }
							String inf = c.getName().replace("noppes.npcs.api.", "");
							if (inf.equals(c.getSimpleName())) { inf = ""; }
							else { while(inf.indexOf(".")!=-1) { inf = inf.replace(".", "/"); } }
							TempClass.imps.put(c.getSimpleName(), inf);
						} else if (acceptAllVariablesAndMethods) {
							String inf = c.getName().replace("noppes.npcs.", "");
							if (inf.equals(c.getSimpleName()) || inf.indexOf("util")==0 || inf.indexOf("config")==0) { continue; }
							try { TempClass.apis.put(c, Files.toString(f, Charset.forName("UTF-8"))); } catch (IOException e) { continue; }
							if (inf.equals(c.getSimpleName())) { inf = ""; }
							else { while(inf.indexOf(".")!=-1) { inf = inf.replace(".", "/"); } }
							TempClass.imps.put(c.getSimpleName(), inf);
						}
					}
				}
				catch (Exception e) { System.out.println("Error File: \""+f.getName()+"\" - "+e); }
			}
		}
	}
	
}
