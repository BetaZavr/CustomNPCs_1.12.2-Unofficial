package noppes.npcs.util.temp;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.Event.Result;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IProjectile;
import noppes.npcs.api.event.BlockEvent;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.CustomNPCsEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.ProjectileEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumScriptType;

public class TempDataClass {

	public Class<?> ext, api, real;
	public Map<String, TepmFieldData> fields;
	public Map<String, TempMetodData> metods;
	public Map<Integer, String> enums;
	public String path;
	public boolean isEvent, isEnum;
	
	/** Interfase or Abstract class */
	public TempDataClass(String path, File dir, Class<?> real, Class<?> api, String classCode, String code) {
		this.api = api;
		this.real = real;
		this.path = path;
		this.ext = null;
		this.isEvent = false;
		this.isEnum = false;
		this.fields = Maps.<String, TepmFieldData>newTreeMap();
		this.metods = Maps.<String, TempMetodData>newTreeMap();
		if (classCode == null) { classCode = ""; }
		if (api==NpcAPI.class) { code = "{ " + code.substring(code.indexOf("public abstract ICustomGui")); }
		if (api.isInterface()) {
			code = code.substring(code.indexOf("interface "+api.getSimpleName()) + ("interface "+api.getSimpleName()).length());
		}
		else if (api==NpcAPI.class) {
			while (code.indexOf("public abstract ")!=-1) { code = code.replace("public abstract ", ""); }
		}
		else {
			System.out.println("Error class api: "+api.getSimpleName()+"; real: "+real.getSimpleName());
			return;
		}
		String ex = code.substring(0, code.indexOf("{"));
		while (ex.indexOf(""+((char) 10))!=-1) { ex = ex.replace(""+((char) 10), ""); }
		if (ex.length()>2) {
			if (ex.indexOf("extends")!=-1) {
				int e = ex.indexOf(" ", ex.indexOf("extends")+8);
				if (e==-1) { e = ex.length() - 1; }
				String ext = ex.substring(ex.indexOf("extends")+8, e);
				for (Class<?> a : TempClass.apis.keySet()) {
					if (a.getSimpleName().equals(ext)) {
						this.ext = a;
						break;
					}
				}
			}
		}
		if (code.lastIndexOf(";")!=-1) { code = code.substring(code.indexOf("{")+1, code.lastIndexOf(";")); }
		else { code = ""; }
		List<TempMetodData> list = Lists.<TempMetodData>newArrayList();
		if (code.indexOf(";")!=-1) {
			for (String str : code.split(";")) {
				for (int i = 0; i < str.length(); i++) {
					if (str.charAt(i)==((char) 9) || str.charAt(i)==((char) 10) || str.charAt(i)==((char) 13) || str.charAt(i)==((char) 32)) { continue; }
					str = str.substring(i);
					break;
				}
				if (str.endsWith(")")) {
					list.add(new TempMetodData(str, str.indexOf("@Deprecated")!=-1));
				}
			}
		}
		for (Field f : real.getDeclaredFields()) {
			if (!Modifier.isPublic(f.getModifiers())) { continue; }
			this.fields.put(f.getName(), new TepmFieldData(f));
		}
		for (Method m : real.getDeclaredMethods()) {
			if (!Modifier.isPublic(m.getModifiers())) { continue; }
			TempMetodData td = new TempMetodData(m);
			boolean found = false;
			for (TempMetodData tdm : list) {
				if (tdm.comparison(td)) {
					td.setData(tdm);
					found = true;
					break;
				}
			}
			if (!found && classCode.indexOf(m.getName()+"(")!=-1) {
				String ccD = classCode;
				while (ccD.indexOf(m.getName()+"(")!=-1) {
					String body = ccD.substring(ccD.lastIndexOf(""+((char) 10), ccD.indexOf(m.getName()+"("))+1, ccD.indexOf(")", ccD.indexOf(m.getName()+"("))+1);
					if (body.indexOf("{")==-1 && body.indexOf("}")==-1 && body.indexOf(";")==-1) {
						try {
							boolean isD = false;
							for (Annotation annt : m.getDeclaredAnnotations()) {
								if (annt.annotationType()==Deprecated.class) {
									isD = true;
									break;
								}
							}
							TempMetodData tdm = new TempMetodData(body, isD);
							td.setData(tdm);
						} catch (Exception e) {}
					}
					ccD = ccD.substring(ccD.indexOf(")", ccD.indexOf(m.getName()+"("))+1);
				}
			}
			String key = m.getName()+"(";
			for (Parameter p : m.getParameters()) {
				key += p.getType().getSimpleName()+", ";
			}
			key += ")";
			this.metods.put(key, td);
		}
	}
	
	/** Event Class */
	public TempDataClass(String path, File dir, Class<?> base, Class<?> real, String code) {
		this.api = real;
		this.real = real;
		this.path = path;
		this.ext = base;
		this.isEvent = true;
		this.isEnum = false;
		this.fields = Maps.<String, TepmFieldData>newTreeMap();
		this.metods = Maps.<String, TempMetodData>newTreeMap();
		if (real==base && base.getSuperclass()!=null && base.getSuperclass()!=Object.class) {
			this.ext = base.getSuperclass();
		}
//System.out.println("Event Base: "+base.getSimpleName()+"; real: "+real.getSimpleName());
		int s = code.indexOf("{", code.indexOf(real.getSimpleName()+" extends " + base.getSimpleName()));
		int e = code.length() - 1, sum = 1;
		for (int i = s + 1; i < code.length(); i++) {
			if (code.charAt(i) == '{') { sum ++; }
			if (code.charAt(i) == '}') { sum --; }
			if (sum == 0) {
				e = i + 1;
				break;
			}
		}
		code = code.substring(s, e);
		for (Field f : real.getDeclaredFields()) {
			if (!Modifier.isPublic(f.getModifiers())) { continue; }
			this.fields.put(f.getName(), new TepmFieldData(f));
		}
		for (Method m : real.getDeclaredMethods()) {
			if (!Modifier.isPublic(m.getModifiers()) || m.getName().equals("getListenerList") || m.getName().equals("isCancelable")) { continue; }
			TempMetodData td = new TempMetodData(m);
			
			String ccD = code;
			while (ccD.indexOf(m.getName()+"(")!=-1) {
				String body = ccD.substring(ccD.lastIndexOf(""+((char) 10), ccD.indexOf(m.getName()+"("))+1, ccD.indexOf(")", ccD.indexOf(m.getName()+"("))+1);
				if (body.indexOf("{")==-1 && body.indexOf("}")==-1 && body.indexOf(";")==-1) {
					try {
						boolean isD = false;
						for (Annotation annt : m.getDeclaredAnnotations()) {
							if (annt.annotationType()==Deprecated.class) {
								isD = true;
								break;
							}
						}
						TempMetodData tdm = new TempMetodData(body, isD);
						td.setData(tdm);
					} catch (Exception err) {}
				}
				ccD = ccD.substring(ccD.indexOf(")", ccD.indexOf(m.getName()+"("))+1);
			}
			
			String key = m.getName()+"(";
			for (Parameter p : m.getParameters()) { key += p.getType().getSimpleName()+", "; }
			key += ")";
			this.metods.put(key, td);
		}
	}

	public TempDataClass(String path, File dir, Class<?> api, String code) {
		this.api = api;
		this.real = api;
		this.path = path;
		this.ext = null;
		this.isEvent = false;
		this.isEnum = true;
		this.enums = Maps.<Integer, String>newTreeMap();
		this.fields = Maps.<String, TepmFieldData>newTreeMap();
		this.metods = Maps.<String, TempMetodData>newTreeMap();
		for (Object c : api.getEnumConstants()) {
			try {
				Field f = c.getClass().getDeclaredField("type");
				f.setAccessible(true);
				this.enums.put(f.getInt(c), api.getSimpleName()+"_"+c);
			}
			catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				System.out.println(api.getSimpleName()+"; enum error: \""+c+"\"" + e);
			}
		}
	}

	public String getPageCode() {
		if (this.isEvent) { return this.getEventPageCode(); }
		if (this.isEnum) { return this.getEnumPageCode(); }
		char ent = ((char) 10);

		Map<String, TempDataClass> extds = Maps.<String, TempDataClass>newTreeMap();
		Map<String, String> getts = Maps.<String, String>newTreeMap();
		for (Class<?> cl : TempClass.tempMap.keySet()) {
			if (cl==this.api) { continue; }
			if (TempClass.tempMap.get(cl).ext==this.api) {
				extds.put(cl.getSimpleName(), TempClass.tempMap.get(cl));
			}
			for (TempMetodData tmd : TempClass.tempMap.get(cl).metods.values()) {
				if (getts.containsKey(cl.getSimpleName())) { continue; }
				if (tmd.type==this.api) {
					TempDataClass tdc = TempClass.tempMap.get(cl);
					String path = tdc.path;
					if (tdc.isEvent && tdc.ext!=CustomNPCsEvent.class) { path += "/" + tdc.ext.getSimpleName(); }
					getts.put(cl.getSimpleName(), "[[Custom NPCs/Unoficial_API_1.12.2/"+path+"/"+tdc.api.getSimpleName()+"#"+tmd.name+"|"+tdc.api.getSimpleName()+"]]");
				}
			}
			for (TepmFieldData tfd : TempClass.tempMap.get(cl).fields.values()) {
				if (getts.containsKey(cl.getSimpleName())) { continue; }
				if (tfd.type==this.api) {
					TempDataClass tdc = TempClass.tempMap.get(cl);
					String path = tdc.path;
					if (tdc.isEvent && tdc.ext!=CustomNPCsEvent.class) { path += "/" + tdc.ext.getSimpleName(); }
					getts.put(cl.getSimpleName(), "[[Custom NPCs/Unoficial_API_1.12.2/"+path+"/"+tdc.api.getSimpleName()+"#"+tfd.name+"|"+tdc.api.getSimpleName()+"]]");
				}
			}
		}
		Map<String, Class<?>> clss = Maps.<String, Class<?>>newTreeMap();
		if (TempClass.fulls.containsKey(this.api)) {
			for (Class<?> itfs : TempClass.fulls.get(this.api)) {
				if (clss.containsKey(itfs.getSimpleName())) { continue; }
				clss.put(itfs.getSimpleName(), itfs);
			}
		}
		Map<String, Class<?>> evnts = Maps.<String, Class<?>>newTreeMap();
		for (Class<?> ev : TempClass.evs.keySet()) {
			if (ev.getDeclaredClasses().length>0) {
				for (Class<?> sc : ev.getDeclaredClasses()) {
					for (Field f : sc.getDeclaredFields()) {
						if (f.getType()==this.api) {
							evnts.put(sc.getSimpleName(), sc);
							break;
						}
					}
				}
			} else {
				
			}
		}
		
		String subIntrfs = "", getters = "", classes = "", events = "", extend = "";
		for (String clName : extds.keySet()) {
			TempDataClass td = extds.get(clName);
			if (!subIntrfs.isEmpty()) { subIntrfs += ", "; }
			else { subIntrfs = TempClass.getLoc("all.sub.i") + ent; }
			subIntrfs += "[[Custom NPCs/Unoficial_API_1.12.2/"+td.path+"/"+clName+"|"+clName+"]]";
		}
		for (String clName : getts.keySet()) {
			if (!getters.isEmpty()) { getters += ", "; }
			else { getters = TempClass.getLoc("all.sub.g") + ent; }
			getters += getts.get(clName);
		}
		for (String clName : clss.keySet()) {
			if (!classes.isEmpty()) { classes += ", "; }
			else { classes = TempClass.getLoc("all.sub.c") + ent; }
			if (clss.get(clName).getName().startsWith("noppes.npcs")) {
				String path = clss.get(clName).getName();
				while (path.indexOf(".")!=-1) { path = path.replace(".", "/"); }
				classes += "[https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/"+path+".java "+clss.get(clName).getSimpleName()+"]";
			}
			else { classes += clss.get(clName).getName(); }
		}
		for (String clName : evnts.keySet()) {
			if (!events.isEmpty()) { events += ", "; }
			else { events = TempClass.getLoc("all.sub.e") + "<br>" + ent; }
			if (evnts.get(clName).getName().startsWith("noppes.npcs")) {
				String path = evnts.get(clName).getName().replace("$", "/").replace("noppes.npcs.", "");
				while (path.indexOf(".")!=-1) { path = path.replace(".", "/"); }
				events += "[[Custom NPCs/Unoficial_API_1.12.2/"+path+"|"+evnts.get(clName).getSuperclass().getSimpleName()+"."+clName+"]]";
			}
			else { events += clss.get(clName).getName(); }
		}
		if (getters.isEmpty() && !events.isEmpty()) {
			getters = TempClass.getLoc("all.sub.g") + ent;
		}
		
		if (this.ext!=null) {
			extend = this.getExtendMetods(extend);
		}
		String clazz = "[https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/noppes/npcs/"+this.path+"/"+this.api.getSimpleName()+".java "+this.api.getSimpleName()+"]";
		String path = "noppes.npcs", t = "";
		String[] paths = this.path.split("/");
		for (int i = 0; i < paths.length; i++) {
			if (i==0) {
				path += ".[[Custom NPCs/Unoficial_API_1.12.2/api|api]]";
			} else {
				if (!t.isEmpty()) { t += "/"; }
				t += paths[i];
				path += ".[[Custom NPCs/Unoficial_API_1.12.2/"+t+"|"+paths[i]+"]]";
			}
		}
		String text = TempClass.getLoc("r")+" "+path+"<br>" + ent +
				"'''"+TempClass.getLoc("i")+"''' "+clazz+"<br><br>" + ent + 
				(getters.isEmpty() ? "" : getters + "<br>" + ent) +
				(events.isEmpty() ? "" : events + "<br>" + ent) +
				(subIntrfs.isEmpty() ? "" : subIntrfs + "<br>" + ent) +
				(classes.isEmpty() ? "" : classes + "<br>" + ent) +
				(extend.isEmpty() ? "" : extend + "<br>" + ent);
		if (!this.fields.isEmpty()) {
			text += "=== "+TempClass.getLoc("mf")+" ===" + ent + 
					"{| class=\"wikitable\" style=\"text-align:left; width:100%\"" + ent + 
					TempClass.getLoc("pt") + ent;
			for (TepmFieldData tfd : this.fields.values()) { text += this.getFieldCode(tfd, null); }
			text += "|}" + ent;
		}
		
		text += "=== "+TempClass.getLoc("mi")+" ===" + ent + 
				"{| class=\"wikitable\" style=\"text-align:left\"" + ent + 
				TempClass.getLoc("mt") + ent;
		String data = "";
		for (TempMetodData tmd : this.metods.values()) {
			text += this.getMethodCode(tmd, null);
			if (data.isEmpty()) { data = "=== "+TempClass.getLoc("ms")+" ===" + ent; }
			data += ent + "==== "+tmd.name+" ====" + ent +
					"{| class=\"wikitable\" style=\"text-align:left; width:100%\";" + ent +
					"|-" + ent +
					"| ''" + TempMetodData.getCode(tmd.type) + " " + tmd.name + "(";
			String mKey = tmd.getMetodKey(this);
			if (!tmd.parameters.isEmpty()) {
				String prms = "", pInfo = "";
				for (String p : tmd.parameters.keySet()) {
					if (!prms.isEmpty()) {
						prms += ", ";
						pInfo += "<br>";
					} else {
						pInfo = "<br>" + TempClass.getLoc("parametrs") + "<br>";
					}
					prms += p;
					pInfo += TempMetodData.getCode(tmd.parameters.get(p)) + " '''" + p + "''' = " + TempClass.getLoc(tmd.getParametrKey(this, p));
				}
				data += prms + ")''<br>" + TempClass.getLoc("return") + "<br>" + TempClass.getLoc(mKey) + (tmd.isDeprecated ? TempClass.getLoc("field.isDeprecated"): "") + "<br>" + pInfo + ent + "|}";
			}
			else { data += ")''<br>"+TempClass.getLoc("return") + "<br>" + TempClass.getLoc(mKey) + (tmd.isDeprecated ? TempClass.getLoc("field.isDeprecated"): "") + ent + "|}"; }
		}
		text += "|}";
		if (!data.isEmpty()) { text += ent + data; }
		return text;
	}

	private String getEnumPageCode() {
		char ent = ((char) 10);
		String text = TempClass.getLoc("r") + " noppes.npcs.[[Custom NPCs/Unoficial_API_1.12.2/api|api]].[[Custom NPCs/Unoficial_API_1.12.2/constants|constants]]<br>" + ent +
				"'''"+TempClass.getLoc("cc")+"''' [https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/noppes/npcs/api/constants/"+this.real.getSimpleName()+".java "+this.real.getSimpleName()+"]<br><br>" + ent +
				TempClass.getLoc("all.sub.f") + ent +
				"{| class=\"wikitable\" style=\"text-align:left; width:100%\"" + ent + 
				TempClass.getLoc("pf") + ent;
		for (int id : this.enums.keySet()) {
			text += "|-" + ent +
					"| " + id + ent +
					"| '''" + this.enums.get(id) + "'''" + ent +
					"| ''" + TempClass.getLoc("enum."+this.real.getSimpleName()+"." + this.enums.get(id)) + "''" + ent;
		}
		return text + "|}";
	}

	private String getExtendMetods(String extend) {
		char ent = ((char) 10);
		Class<?> e = this.ext;
		if (e==null) { return extend; }
		if (!TempClass.tempMap.containsKey(e)) {
			for (Class<?> intf : TempClass.fulls.keySet()) {
				if (TempClass.fulls.get(intf).contains(e)) {
					e = intf;
				}
			}
		}
		if (TempClass.tempMap.containsKey(e)) {
			TempDataClass tcd = TempClass.tempMap.get(e);
			String loc = TempMetodData.getCode(e), key = "|"+e.getSimpleName()+"]";
			String exStr = "";
			for (String m : tcd.metods.keySet()) {
				if (!exStr.isEmpty()) { exStr += ", "; }
				exStr += loc.replace(key, "#"+tcd.metods.get(m).name+"|"+tcd.metods.get(m).name+"]");
			}
			if (extend.isEmpty()) {
				extend = TempClass.getLoc("all.sub.y") + ent + TempClass.getLoc("in") + loc + ":<br>" + ent + exStr;
			} else {
				extend += "<br>" + ent + TempClass.getLoc("in") + loc + ":<br>" + ent + exStr;
			}
		}
		else {
			System.out.println("unknow extend class: "+e.getName());
		}
		return extend;
	}

	private String getEventPageCode() {
		char ent = ((char) 10);
		String clName = this.ext.getName().replace(this.ext.getSimpleName(), "").toLowerCase();
		while (clName.indexOf(".")!=-1) { clName = clName.replace(".", "/"); }
		clName = this.ext.getName().replace(this.ext.getSimpleName(), "")+"[[Custom_NPCs/Unoficial_API_1.12.2/api/event/"+this.ext.getSimpleName()+"|"+this.ext.getSimpleName()+"]].[https://github.com/BetaZavr/CustomNPCs_1.12.2-Unofficial/tree/master/src/main/java/"+clName+this.ext.getSimpleName()+".java "+this.real.getSimpleName()+"]";
		String text = "<big>'''"+TempClass.getLoc("c")+clName+"'''</big><br>" + ent;
		EnumScriptType lang = TempClass.scType.get(this.real);
		if (lang!=null) {
			text += TempClass.getLoc("scUsed")+"'''{{"+TempClass.getLoc("color")+"|DarkBlue|function}} " + "{{"+TempClass.getLoc("color")+"|DarkGreen|" + lang.function + "}}" + "{{"+TempClass.getLoc("color")+"|DarkBlue|(}}event" + "{{"+TempClass.getLoc("color")+"|DarkBlue|)}} {}" + ent;
		}
		return text += this.getEventCode();
	}

	public String getEventCode() {
		String text = "";
		char ent = ((char) 10);
		String k = this.path;
		while(k.indexOf("/")!=-1) { k = k.replace("/", "."); }
		if (!this.fields.isEmpty()) {
			for (TepmFieldData tfd : this.fields.values()) {
				text += this.getFieldCode(tfd, null);
			}
		}
		if (this.ext == ForgeEvent.class) {
			for (Field f : ForgeEvent.class.getDeclaredFields()) {
				if (f.getType()==NpcAPI.class) { text += this.getFieldCode(f, "API", ForgeEvent.class); }
				else if (f.getType()==Event.class) { text += this.getFieldCode(f, "event", ForgeEvent.class); }
				else if (f.getType()==IEntity.class) { text += this.getFieldCode(f, "entity", ForgeEvent.class); }
				else if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", ForgeEvent.class); }
				else if (f.getType()==ICustomNpc.class) { text += this.getFieldCode(f, "npc", ForgeEvent.class); }
				else if (f.getType()==IWorld.class) { text += this.getFieldCode(f, "world", ForgeEvent.class); }
				else if (f.getType()==IPos.class) { text += this.getFieldCode(f, "pos", ForgeEvent.class); }
				else if (f.getType()==IBlock.class) { text += this.getFieldCode(f, "block", ForgeEvent.class); }
				else if (f.getType()==IItemStack.class) { text += this.getFieldCode(f, "stack", ForgeEvent.class); }
			}
		}
		if (this.ext == BlockEvent.class) {
			for (Field f : BlockEvent.class.getDeclaredFields()) {
				if (f.getType()==IBlock.class) { text += this.getFieldCode(f, "block", BlockEvent.class); }
			}
		}
		if (this.ext == CustomGuiEvent.class) {
			for (Field f : CustomGuiEvent.class.getDeclaredFields()) {
				if (f.getType()==ICustomGui.class) { text += this.getFieldCode(f, "gui", CustomGuiEvent.class); }
				else if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", CustomGuiEvent.class); }
			}
		}
		if (this.ext == DialogEvent.class) {
			for (Field f : DialogEvent.class.getDeclaredFields()) {
				if (f.getType()==IDialog.class) { text += this.getFieldCode(f, "dialog", DialogEvent.class); }
				else if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", DialogEvent.class); }
			}
			
		}
		if (this.ext == ItemEvent.class) {
			for (Field f : ItemEvent.class.getDeclaredFields()) {
				if (f.getType()==IItemScripted.class) { text += this.getFieldCode(f, "item", ItemEvent.class); }
			}
		}
		if (this.ext == NpcEvent.class || this.ext == DialogEvent.class) {
			for (Field f : NpcEvent.class.getDeclaredFields()) {
				if (f.getType()==ICustomNpc.class) { text += this.getFieldCode(f, "npc", NpcEvent.class); }
			}
		}
		if (this.ext == PlayerEvent.class) {
			for (Field f : PlayerEvent.class.getDeclaredFields()) {
				if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", PlayerEvent.class); }
			}
		}
		if (this.ext == ProjectileEvent.class) {
			for (Field f : ProjectileEvent.class.getDeclaredFields()) {
				if (f.getType()==IProjectile.class) { text += this.getFieldCode(f, "projectile", ProjectileEvent.class); }
			}
		}
		if (this.ext == QuestEvent.class) {
			for (Field f : QuestEvent.class.getDeclaredFields()) {
				if (f.getType()==IQuest.class) { text += this.getFieldCode(f, "quest", QuestEvent.class); }
				if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", QuestEvent.class); }
			}
		}
		if (this.ext == RoleEvent.class) {
			for (Field f : RoleEvent.class.getDeclaredFields()) {
				if (f.getType()==ICustomNpc.class) { text += this.getFieldCode(f, "npc", RoleEvent.class); }
				if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", RoleEvent.class); }
			}
		}
		if (this.ext == WorldEvent.class) {
			for (Field f : WorldEvent.class.getDeclaredFields()) {
				if (f.getType()==IWorld.class) { text += this.getFieldCode(f, "world", WorldEvent.class); }
			}
		}
		if (this.ext == CustomContainerEvent.class) {
			for (Field f : CustomContainerEvent.class.getDeclaredFields()) {
				if (f.getType()==IContainer.class) { text += this.getFieldCode(f, "container", CustomContainerEvent.class); }
				if (f.getType()==IPlayer.class) { text += this.getFieldCode(f, "player", CustomContainerEvent.class); }
			}
		}
		if (this.real!=CustomNPCsEvent.class && this.ext != ForgeEvent.class) {
			for (Field f : CustomNPCsEvent.class.getDeclaredFields()) {
				if (f.getType()==NpcAPI.class) { text += this.getFieldCode(f, "API", CustomNPCsEvent.class); }
			}
		}
		if (!text.isEmpty()) {
			text = "=== "+TempClass.getLoc("mf")+" ===" + ent + 
					"{| class=\"wikitable\" style=\"text-align:left; width:100%\"" + ent + 
					TempClass.getLoc("pte") + ent + text + "|}" + ent;
		}
		text += "=== "+TempClass.getLoc("mi")+" ===" + ent + 
				"{| class=\"wikitable\" style=\"text-align:left\"" + ent + 
				TempClass.getLoc("mie") + ent;
		if (!this.metods.isEmpty()) {
			for (TempMetodData tmd : this.metods.values()) { text += this.getMethodCode(tmd, null); }
		}
		for (Method m : Event.class.getDeclaredMethods()) { text += this.getEventMethodCode(m); }
		text += "|}";
		return text;
	}

	private String getFieldCode(TepmFieldData tfd, Class<?> parent) {
		String mKey = tfd.getFieldKey(this);
		char ent = ((char) 10);
		return	"|-" + ent + 
				"| "+TempMetodData.getCode(tfd.type) + ent + 
				"| "+tfd.name + ent + 
				"| ''" + (tfd.isStatic ? TempClass.getLoc("field.isStatic"): "") + (tfd.isDeprecated ? TempClass.getLoc("field.isDeprecated"): "") + TempClass.getLoc(mKey) + "''" + ent +  
				(parent == null ? "" : "| " + TempMetodData.getCode(parent) + ent);
	}
	
	private String getFieldCode(Field f, String name, Class<?> parent) {
		TepmFieldData tfd = new TepmFieldData(f);
		if (name!=null && !name.isEmpty()) { tfd.name = name; }
		return this.getFieldCode(tfd, parent);
	}
	
	private String getMethodCode(TempMetodData tmd, Class<?> parent) {
		String mKey = tmd.getMetodKey(this);
		char ent = ((char) 10);
		return	"|-" + ent + 
				"| " + TempMetodData.getCode(tmd.type) + ent + 
				"| " + tmd.getMetodBody(this) + ent + 
				"| ''" + (tmd.isStatic ? TempClass.getLoc("field.isStatic"): "") + (tmd.isDeprecated ? TempClass.getLoc("field.isDeprecated"): "") + TempClass.getLoc(mKey) + "''" + ent +
				(parent == null ? "" : "| " + TempMetodData.getCode(parent) + ent);
	}
	
	private String getEventMethodCode(Method m) {
		String parms = "";
		for (Parameter p : m.getParameters()) {
			if (!parms.isEmpty()) { parms += ", "; }
			parms += TempMetodData.getCode(p.getType()) + " ";
			if (p.getType() == Result.class) { parms += "result"; }
			else if (p.getType() == EventPriority.class) { parms += "priority"; }
			else if (m.getName().equals("setCanceled")) { parms += "cancel"; }
			else { parms += p.getName(); }
		}
		boolean isD = false;
		for (Annotation annt : m.getDeclaredAnnotations()) {
			if (annt.annotationType()==Deprecated.class) {
				isD = true;
				break;
			}
		}
		char ent = ((char) 10);
		return "|-" + ent + 
				"| " + TempMetodData.getCode(m.getReturnType()) + ent + 
				"| [https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.12.2/net/minecraftforge/fml/common/eventhandler/Event.html#" + m.getName() + " " + m.getName() + "](" + parms + ")" + ent + 
				"| ''" + (Modifier.isStatic(m.getModifiers()) ? TempClass.getLoc("field.isStatic"): "") + (isD ? TempClass.getLoc("field.isDeprecated"): "") + TempClass.getLoc("parametr.event."+m.getName()) + "''" + ent +
				"| [https://nekoyue.github.io/ForgeJavaDocs-NG/javadoc/1.12.2/net/minecraftforge/fml/common/eventhandler/Event.html Event]" + ent;
	}

}
