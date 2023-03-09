package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;

public class MetodData {

	public Class<?> returnType;
	public String name;
	public List<ParameterData> parameters;
	public String comment;

	public MetodData(Class<?> ret, String name, String comment, ParameterData ... parameters) {
		this.returnType = ret;
		this.name = name;
		this.comment = comment;
		this.parameters = Lists.<ParameterData>newArrayList();
		for (ParameterData pd : parameters) { this.parameters.add(pd); }
	}
	
	public String getText() {
		char chr = Character.toChars(0x00A7)[0];
		String text = "";
		if (!this.parameters.isEmpty()) {
			for (ParameterData pd : this.parameters) {
				if (text.isEmpty()) { text = "("+pd.typename; }
				else { text += ", "+pd.typename; }
			}
			text += ")";
		} else { text = "()"; }
		String ret = "c" + this.returnType.getSimpleName();
		if (this.returnType.isInterface()) { ret = "9" + this.returnType.getSimpleName(); }
		else if (this.returnType == boolean.class || this.returnType == byte.class || this.returnType == short.class || this.returnType == int.class || this.returnType == float.class || this.returnType == double.class || this.returnType == long.class || this.returnType == String.class) { ret = "e" + this.returnType.getSimpleName(); }
		else if (this.returnType == Void.class) { ret = "8void"; }
		return chr + ret +chr+"f " + this.name + text + chr + "f;";
	}
	
	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		comment.add(((char) 167)+"bInterfase: "+((char) 167)+"f"+this.name+((char) 167)+"b:");
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) {
				comment.add(t);
			}
		}
		else { comment.add(tr); }
		if (!this.parameters.isEmpty()) {
			for (ParameterData pd : this.parameters) {
				comment.addAll(pd.getComment());
			}
		}
		return comment;
	}

}
