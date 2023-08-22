package noppes.npcs.client.util;

import java.lang.reflect.Parameter;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.util.AdditionalMethods;

public class MethodData {

	String ifc = "";
	public String returnTypeName;
	public String name;
	public ParameterData[] parameters;
	public String comment;
	public boolean isDeprecated;
	public Class<?> returnType;
	public boolean isVarielble;

	public MethodData(Class<?> ret, String name, String comment, ParameterData ... parameters) {
		this.returnType = ret;
		this.returnTypeName = "8null";
		if (ret!=null) {
			this.returnTypeName = "c" + ret.getSimpleName();
			if (ret.isInterface()) { this.returnTypeName = "9" + ret.getSimpleName(); }
			else if (ret == boolean.class || ret == byte.class || ret == short.class || ret == int.class || ret == float.class || ret == double.class || ret == long.class || ret == String.class) { this.returnTypeName = "e" + ret.getSimpleName(); }
			else if (ret == Void.class) { this.returnTypeName = "8void"; }
		}
		this.name = name;
		this.comment = comment;
		this.parameters = new ParameterData[parameters.length];
		int i = 0;
		for (ParameterData pd : parameters) { this.parameters[i] = pd; i++; }
		this.isDeprecated = false;
		this.isVarielble = false;
	}
	
	public MethodData() {
		this.returnType = Void.class;
		this.returnTypeName = "8null";
		this.name = "";
		this.comment = "";
		this.parameters = new ParameterData [0];
		this.isDeprecated = false;
		this.isVarielble = false;
	}

	public String getText() {
		char chr = Character.toChars(0x00A7)[0];
		String text = "";
		if (this.parameters.length!=0) {
			for (ParameterData pd : this.parameters) {
				if (text.isEmpty()) { text = "("+pd.typename; }
				else { text += ", "+pd.typename; }
			}
			text += ")";
		} else { text = "()"; }
		String total = chr + this.returnTypeName +chr+"f " + this.name + text + chr + "f;";
		if (this.isDeprecated) {
			total = chr + "8" + AdditionalMethods.instance.deleteColor(total);
		}
		return total;
	}
	
	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		ITextComponent intf = new TextComponentTranslation("gui.interfase", ": ");
		ITextComponent md = new TextComponentTranslation(this.isVarielble ? "gui.variable" : "gui.method", ": ");
		intf.getStyle().setColor(TextFormatting.AQUA);
		md.getStyle().setColor(TextFormatting.AQUA);
		
		if (!this.ifc.isEmpty()) { comment.add(intf.getFormattedText() + ((char) 167) + "f" + this.ifc + ((char) 167) + "b;"); }
		comment.add(md.getFormattedText() + ((char) 167) + "f" + this.name + ((char) 167) + "b;");
		if (this.isDeprecated) { comment.add(new TextComponentTranslation("metod.deprecated").getFormattedText()); }
		
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		}
		else { comment.add(tr); }
		if (this.parameters.length!=0) {
			for (ParameterData pd : this.parameters) { comment.addAll(pd.getComment()); }
		}
		return comment;
	}
	
	public MethodData setDeprecated() {
		this.isDeprecated = true;
		return this;
	}

	public ParameterData getParameterData(int h, Parameter parameter) {
		if (h < this.parameters.length) {
			ParameterData pd = this.parameters[h];
			if (pd!=null && pd.clazz.equals(parameter.getType().getSimpleName())) {
				return pd;
			}
		}
		return null;
	}

	public String getEnumCode() {
		String ent = ""+((char) 10);
		String tab = "	";
		String parameters = "";
		for (ParameterData pd : this.parameters) {
			if (!parameters.isEmpty()) { parameters += "," + ent; }
			parameters += pd.getEnumCode();
		}
		
		String code = tab + tab + tab + "new MethodData("+(this.returnTypeName.substring(1))+".class, " +
				"\""+this.name+"\", \""+this.comment+"\"" + 
				(parameters.isEmpty() ? ")" : "," + ent + parameters + ent + tab + tab + tab + ")");
							
		return code;
	}
}
