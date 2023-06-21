package noppes.npcs.client.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.constants.EnumEventData;

public class EventData {
	
	public Class<?> event, extend;
	public List<MetodData> metods;
	public String comment, func;
	
	public EventData(Class<?> event, Class<?> extend, String comment, String func, MetodData ... mds) {
		this.comment = comment;
		this.func = func;
		this.metods = Lists.<MetodData>newArrayList();
		for (MetodData md : mds) { this.metods.add(md); }
		this.extend = extend;
	}
	
	public List<MetodData> getAllMetods(List<MetodData> parent) {
		for (MetodData md : this.metods) { parent.add(md); }
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
	
	
}
