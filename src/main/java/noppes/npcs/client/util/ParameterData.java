package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;

public class ParameterData {
	
	public String typename;
	private String comment;
	
	public ParameterData(Class<?> clazz, String name, String comment) {
		char chr = Character.toChars(0x00A7)[0];
		String type = "c"+ clazz.getSimpleName();
		if (clazz.isInterface()) { type = "9"+ clazz.getSimpleName(); }
		else if (clazz == boolean.class || clazz == byte.class || clazz == short.class || clazz == int.class || clazz == float.class || clazz == double.class || clazz == long.class || clazz == String.class) { type = "e"+ clazz.getSimpleName(); }
		if (clazz.isArray()) { type += "[]";}
		this.typename = chr + type  + chr + "f " + name;
		this.comment = comment;
	}
	
	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		} else { comment.add(tr); }
		return comment;
	}
	
}
