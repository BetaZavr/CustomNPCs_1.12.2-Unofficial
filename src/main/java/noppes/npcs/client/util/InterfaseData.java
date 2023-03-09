package noppes.npcs.client.util;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.text.TextComponentTranslation;

public class InterfaseData {

	public Class<?> interF;
	public Map<Integer, MetodData> metods;
	public String comment;
	
	public InterfaseData(Class<?> interF, String comment, MetodData ... mds) {
		this.interF = interF;
		this.comment = comment;
		this.metods = Maps.<Integer, MetodData>newTreeMap();
		int i = 0;
		for (MetodData md : mds) { this.metods.put(i, md); i++; }
	}
	
	public String getText() {
		String text = "";
		for (int pos : this.metods.keySet()) {
			if (text.isEmpty()) { text = this.metods.get(pos).getText(); }
			else { text += "<br>"+this.metods.get(pos).getText(); }
		}
		while (text.indexOf("<br>")!=-1) { text = text.replace("<br>", ""+((char) 10)); }
		while (text.indexOf(((char) 167)+"r")!=-1) { text = text.replace(((char) 167)+"r", ""); }
		return text;
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
