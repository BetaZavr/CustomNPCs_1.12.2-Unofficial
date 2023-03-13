package noppes.npcs.client.util;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.constants.EnumInterfaceData;

public class InterfaseData {

	public Class<?> interF;
	public Class<?> extend;
	private Map<String, MetodData> metods;
	public String comment;
	
	public InterfaseData(Class<?> interF, String comment, MetodData ... mds) {
		this.interF = interF;
		this.comment = comment;
		this.metods = Maps.<String, MetodData>newTreeMap();
		for (MetodData md : mds) { this.metods.put(md.name, md); }
	}
	
	public InterfaseData(Class<?> interF, Class<?> clazz, String comment, MetodData ... mds) {
		this(interF, comment, mds);
		this.extend = clazz;
	}
	
	public TreeMap<String, MetodData> getAllMetods(TreeMap<String, MetodData> parent) {
		if (this.extend!=null) {
			for (EnumInterfaceData enumIT : EnumInterfaceData.values()) {
				if (this.extend.getSimpleName().equals(enumIT.name())) {
					return enumIT.it.getAllMetods(parent);
				}
			}
		}
		for (MetodData md : this.metods.values()) {
			String name = md.name;
			while (parent.containsKey(name)) { name += "_"; }
			parent.put(name, md);
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
	
}
