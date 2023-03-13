package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;

public class MetodData {

	public String returnTypeName;
	public String name;
	public List<ParameterData> parameters;
	public String comment;

	public MetodData(Class<?> ret, String name, String comment, ParameterData ... parameters) {
		this.returnTypeName = "c" + ret.getSimpleName();
		if (ret.isInterface()) { this.returnTypeName = "9" + ret.getSimpleName(); }
		else if (ret == boolean.class || ret == byte.class || ret == short.class || ret == int.class || ret == float.class || ret == double.class || ret == long.class || ret == String.class) { this.returnTypeName = "e" + ret.getSimpleName(); }
		else if (ret == Void.class) { this.returnTypeName = "8void"; }
		if (ret.isArray()) { this.returnTypeName += "[]";}
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
		return chr + this.returnTypeName +chr+"f " + this.name + text + chr + "f;";
	}
	
	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		comment.add(((char) 167)+"bMetod: "+((char) 167)+"f"+this.name+((char) 167)+"b:");
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
