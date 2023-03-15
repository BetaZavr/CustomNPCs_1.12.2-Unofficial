package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.constants.EnumInterfaceData;

public class InterfaseData {

	public Class<?> interF, extend;
	public Class<?>[] wraper;
	private List<MetodData> metods;
	public String comment;
	
	public InterfaseData(Class<?> interF, Class<?> extend, Class<?>[] wraper, String comment, MetodData ... mds) {
		this.interF = interF;
		this.comment = comment;
		this.metods = Lists.<MetodData>newArrayList();
		for (MetodData md : mds) { this.metods.add(md); }
		this.extend = extend;
		this.wraper = wraper;
	}
	
	public List<MetodData> getAllMetods(List<MetodData> parent) {
		if (this.extend!=null) {
			for (EnumInterfaceData enumIT : EnumInterfaceData.values()) {
				if (this.extend.getSimpleName().equals(enumIT.name())) {
					return enumIT.it.getAllMetods(parent);
				}
			}
		}
		for (MetodData md : this.metods) { parent.add(md); }
		return parent;
	}

	public List<String> getComment() {
		List<String> comment = Lists.<String>newArrayList();
		String tr = new TextComponentTranslation(this.comment).getFormattedText();
		if (tr.indexOf("<br>")!=-1) {
			for (String t : tr.split("<br>")) { comment.add(t); }
		} else { comment.add(tr); }
		if (this.wraper!=null) {
			String text = "";
			for (Class<?> c : this.wraper) {
				if (!text.isEmpty()) { text += ", "; }
				text += this.wraper.length>1 ? c.getSimpleName() : c.getName();
			}
			comment.add(new TextComponentTranslation("interfase.wraper", (this.wraper.length>1 ? "[" + text + "]" : text)).getFormattedText()); }
		return comment;
	}
	
}
