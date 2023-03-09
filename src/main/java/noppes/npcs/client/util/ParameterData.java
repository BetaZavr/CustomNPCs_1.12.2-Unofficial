package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;

public class ParameterData {
	
	public String typename;
	private String comment;
	
	public ParameterData(Class<?> type, String name, String comment) {
		char chr = Character.toChars(0x00A7)[0];
		String color = "c";
		if (type.isInterface()) { color = "9"; }
		else if (type == boolean.class || type == byte.class || type == short.class || type == int.class || type == float.class || type == double.class || type == long.class || type == String.class) { color = "e"; }
		this.typename = chr + color + type.getSimpleName() + chr + "f " + name;
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
