package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;

public class ParameterData {
	
	public String typename, name;
	public String comment;
	public String clazz;
	
	public ParameterData(Class<?> clazz, String name, String comment) {
		char chr = Character.toChars(0x00A7)[0];
		this.name = name;
		this.clazz = clazz.getSimpleName();
		String type = "c"+ this.clazz;
		if (clazz.isInterface()) { type = "9"+ this.clazz; }
		else if (clazz == boolean.class || clazz == byte.class || clazz == short.class || clazz == int.class || clazz == float.class || clazz == double.class || clazz == long.class || clazz == String.class) { type = "e"+ this.clazz; }
		this.typename = chr + type  + chr + "f " + name;
		this.comment = comment;
	}

	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		String tr = this.typename + ((char) 167)+"7 - "+new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		} else { comment.add(tr); }
		return comment;
	}

	public String getEnumCode() {
		String tab = "	";
		return tab + tab + tab + tab + "new ParameterData("+this.clazz+".class, "+ "\""+this.name+"\", \""+this.comment+"\")";
	}
	
}
