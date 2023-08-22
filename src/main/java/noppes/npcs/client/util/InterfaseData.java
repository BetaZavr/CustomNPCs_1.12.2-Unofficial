package noppes.npcs.client.util;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.constants.EnumInterfaceData;

public class InterfaseData {

	public Class<?> interfaseClass, extendClass;
	public Class<?>[] wraperClass;
	public List<MethodData> metods;
	public String comment;
	
	public InterfaseData(Class<?> interF, Class<?> extend, Class<?>[] wraper, String comment, MethodData ... mds) {
		this.interfaseClass = interF;
		this.comment = comment;
		this.metods = Lists.<MethodData>newArrayList();
		for (MethodData md : mds) {
			md.ifc = interF.getSimpleName();
			this.metods.add(md);
		}
		this.extendClass = extend;
		this.wraperClass = wraper;
	}
	
	public InterfaseData() {
		this.interfaseClass = null;
		this.extendClass = null;
		this.wraperClass = null;
		this.comment = "";
		this.metods = Lists.<MethodData>newArrayList();
	}

	public List<MethodData> getAllMetods(List<MethodData> parent) {
		for (MethodData md : this.metods) { parent.add(md); }
		if (this.extendClass!=null) {
			InterfaseData it = EnumInterfaceData.get(this.extendClass.getSimpleName());
			if (it!=null) { it.getAllMetods(parent); }
		}
		return parent;
	}

	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		} else { comment.add(tr); }
		if (this.wraperClass!=null) {
			String text = "";
			for (Class<?> c : this.wraperClass) {
				if (!text.isEmpty()) { text += ", "; }
				text += this.wraperClass.length>1 ? c.getSimpleName() : c.getName();
			}
			comment.add(new TextComponentTranslation("interfase.wraper", (this.wraperClass.length>1 ? "[" + text + "]" : text)).getFormattedText()); }
		return comment;
	}

	public MethodData getMethodData(Method m) {
		for (MethodData md : this.metods) {
			if (md.name.equals(m.getName()) && md.parameters.length==m.getParameterCount()) {
				Parameter[] ps = m.getParameters();
				for (int i = 0; i < md.parameters.length; i++) {
					if (!md.parameters[i].clazz.equalsIgnoreCase(ps[i].getType().getSimpleName())) {
						return null;
					}
				}
				return md;
			}
		}
		return null;
	}

	public String getEnumCode() {
		String ent = ""+((char) 10);
		String tab = "	";
		String name = (this.interfaseClass==null ? "NULL" : this.interfaseClass.getSimpleName());

		String wrapers = "";
		if (this.wraperClass!=null) {
			wrapers = "";
			for (Class<?> c : this.wraperClass) {
				if (!wrapers.isEmpty()) { wrapers += ", "; }
				wrapers += c.getSimpleName()+".class";
			}
			wrapers = "new Class<?>[] { " + wrapers + " }";
		} else { wrapers = "null"; }
		String metods = "";
		for (MethodData md : this.metods) {
			if (!metods.isEmpty()) { metods += ","+ent; }
			metods += md.getEnumCode();
		}
		String code = tab+name+"(new InterfaseData("+name+".class, " + ent +
				tab+tab+tab+(this.extendClass==null ? "null" : this.extendClass.getSimpleName()+".class")+"," + ent +
				tab+tab+tab+wrapers+"," + ent +
				tab+tab+tab+"\"interfase."+name.toLowerCase()+"\"" + 
				(metods.isEmpty() ? "" : ", "+ ent + metods) + ent +
				tab + tab + ")" + ent +
				tab+")";
		return code;
	}
	
	public String toString() {
		String wrapers = "";
		if (this.wraperClass!=null) {
			for (Class<?> c : this.wraperClass) {
				if (!wrapers.isEmpty()) { wrapers += ", "; }
				wrapers += c.getSimpleName();
			}
		}
		return "InterfaseData \""+(this.interfaseClass==null ? "NULL" : this.interfaseClass.getSimpleName())+"\": { " +
				(this.extendClass==null ? "" : "extends: "+this.extendClass.getSimpleName()+", ")+
				(wrapers.isEmpty() ? "" : "wraper Classes: ["+wrapers+"]"+", ")+
				"metods size: "+this.metods.size()+" }";
	}
}
