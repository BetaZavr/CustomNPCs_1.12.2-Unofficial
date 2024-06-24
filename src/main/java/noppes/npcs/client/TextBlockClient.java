package noppes.npcs.client;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.entity.data.TextBlock;
import noppes.npcs.util.AdditionalMethods;

public class TextBlockClient extends TextBlock {

	public int color;
	private String name;
	private String corr = "" + ((char) 9) + ((char) 10) + " ()[]{}.,<>:;+-*\\/\"";
	private ICommandSender sender;
	private Style style;
	public Entity entity;
	public String text;

	public TextBlockClient(ICommandSender sender, String text, int lineWidth, int color, Entity entity, Object... obs) {
		this(text, lineWidth, false, entity, obs);
		this.color = color;
		this.sender = sender;
	}

	public TextBlockClient(String totalText, int lineWidth, boolean mcFont, Entity entity, Object... obs) {
		this.color = 0xE0E0E0;
		this.style = new Style();
		this.entity = entity;
		this.text = NoppesStringUtils.formatText(totalText, obs);
		this.text = this.text.replace("\n", " \n ");
		this.text = this.text.replace("\r", " \r ");
		this.resetWidth(lineWidth, mcFont);
	}

	public TextBlockClient(String name, String text, int lineWidth, int color, Entity entity, Object... obs) {
		this(text, lineWidth, false, entity, obs);
		this.color = color;
		this.name = name;
	}

	private void addLine(String text) { // Change
		TextComponentString line = new TextComponentString(text);
		line.setStyle(this.style);
		this.lines.add(line);
	}

	public String getName() {
		if (this.sender != null) {
			return this.sender.getName();
		}
		return this.name;
	}

	public void resetWidth(int lineWidth, boolean mcFont) {
		String line = "";
		String tempText = new String(this.text);
		List<String> tempList = Lists.newArrayList();
		int fm;
		while (true) {
			fm = -1;
			for (int i = 0; i < corr.length(); i++) {
				int found = tempText.indexOf("" + corr.charAt(i));
				if (found != -1 && (found < fm || fm == -1)) { fm = found; }
			}
			if (fm >= 0) {
				String subWorld = tempText.substring(0, fm + 1);
				tempList.add(subWorld);
				tempText = tempText.substring(fm + 1);
			}
			else { break; }
		}
		tempList.add(tempText);
		String[] words = tempList.toArray(new String[tempList.size()]);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		String color = ((char) 167) + "r";
		for (String word : words) {
			if (word.isEmpty()) { continue; }
			if (word.length() == 1) {
				char c = word.charAt(0);
				if (c == '\r' || c == '\n') {
					this.addLine(color + line);
					color = AdditionalMethods.getLastColor(color, line);
					line = "";
					continue;
				}
			}
			String newLine = line + word;
			int widthLine = (mcFont ? font.getStringWidth(newLine) : ClientProxy.Font.width(newLine));
			if (widthLine > lineWidth && !line.isEmpty()) {
				this.addLine(color + line);
				color = AdditionalMethods.getLastColor(color, line);
				line = word;
			}
			else { line = newLine; }
		}
		if (!line.isEmpty()) {
			this.addLine(color + line);
		}
	}

}
