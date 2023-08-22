package noppes.npcs.client.util;

import java.lang.reflect.Field;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.constants.EnumEventData;
import noppes.npcs.constants.EnumScriptType;

public class EventData {
	
	public Class<?> event, extend;
	public MethodData[] metods;
	public String comment, func;
	
	public EventData(Class<?> event, Class<?> extend, String comment, String func, MethodData ... mds) {
		this.event = event;
		this.extend = extend;
		this.comment = comment;
		this.func = func;
		this.metods = new MethodData[mds.length];
		for (int i =0; i < mds.length; i++) { this.metods[i] = mds[i]; }
	}
	
	public EventData() {
		this.event = null;
		this.extend = null;
		this.comment = "";
		this.func = "";
		this.metods = new MethodData[0];
	}

	public List<MethodData> getAllMetods(List<MethodData> parent) {
		for (MethodData md : this.metods) { parent.add(md); }
		if (this.extend!=null) {
			EventData ed = EnumEventData.get(this.extend.getSimpleName());
			if (ed!=null) { ed.getAllMetods(parent); }
		}
		return parent;
	}

	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		} else { comment.add(tr); }
		return comment;
	}

	public MethodData getMetods(int pos, Field f) {
		if (pos >= 0 && pos < this.metods.length) {
			if (this.metods[pos].returnType==f.getType()) { return this.metods[pos]; }
		}
		for (MethodData md : this.metods) {
			if (md.name.equals(f.getName())) { return md; }
		}
		for (MethodData md : this.metods) {
			if (md.returnType==f.getType()) { return md; }
		}
		return null;
	}

	public String getEnumCode() {
		String ent = ""+((char) 10);
		String tab = "	";
		String n = (this.event==null ? "null" : this.event.getSimpleName()), name = n;
		String claz = "null";
		String info = ""+name.toLowerCase();
		name = name.replace("Event", "");
		if (name.equals("Break")) { name = "Broken"; }
		if (name.indexOf("Entity")==0) { name = name.replace("Entity", ""); }
		if (this.extend!=null) {
			String s = this.extend.getSimpleName().replace("Event", "");
			claz = this.extend.getSimpleName()+"."+this.event.getSimpleName()+".class";
			info = s.toLowerCase()+"."+info;
			name = s + name;
		} else { claz = n+".class"; }
		String metods = "";
		for (MethodData md : this.metods) {
			if (!metods.isEmpty()) { metods += ","+ent; }
			metods += md.getEnumCode();
		}
		String function = "";
		if (this.extend == RoleEvent.class) {
			function = "EnumScriptType.ROLE.function";
		} else {
			for (EnumScriptType est : EnumScriptType.values()) {
				if (est.function.equalsIgnoreCase(this.func)) { function = "EnumScriptType."+est.name()+".function";}
			}
		}
		if (function.isEmpty()) {
			for (EnumScriptType est : EnumScriptType.values()) {
				if (est.name().equalsIgnoreCase(n)) { function = "EnumScriptType."+est.name()+".function";}
			}
		}
		if (function.isEmpty()) {
			for (EnumScriptType est : EnumScriptType.values()) {
				if (est.name().equalsIgnoreCase(name) || est.function.equalsIgnoreCase(name)) { function = "EnumScriptType."+est.name()+".function";}
			}
		}
		if (function.isEmpty()) {
			if (this.func.isEmpty()) { this.func = "unknownFunction"; }
			function = "\""+this.func+"\"";
		}
		
		String code = tab+name+"(new EventData("+claz+", " + ent +
				tab+tab+tab+(this.extend==null ? "null" : this.extend.getSimpleName()+".class")+"," + ent +
				tab+tab+tab+"\""+this.comment+"\"," + ent + 
				tab+tab+tab+function + 
				(metods.isEmpty() ? "" : ", "+ ent + metods) + ent +
				tab + tab + ")" + ent +
				tab+")";
		return code;
	}
	
	
}
