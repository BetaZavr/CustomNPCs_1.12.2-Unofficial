package noppes.npcs.client;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.entity.data.TextBlock;
import noppes.npcs.util.Util;

public class TextBlockClient extends TextBlock {

	public int color;
	private String name;
    private ICommandSender sender;
	private final Style style;
	public Entity entity;
	public String text;

	public TextBlockClient(ICommandSender senderIn, String text, int lineWidth, int colorIn, Entity entity, Object... obs) {
		this(text, lineWidth, false, entity, obs);
		color = colorIn;
		sender = senderIn;
	}

	public TextBlockClient(String totalText, int lineWidth, boolean mcFont, Entity entityIn, Object... obs) {
		color = new Color(0xE0E0E0).getRGB();
		style = new Style();
		entity = entityIn;
		text = NoppesStringUtils.formatText(totalText, obs);
		text = text.replace("\n", " \n ");
		text = text.replace("\r", " \r ");
		resetWidth(lineWidth, mcFont);
	}

	public TextBlockClient(String nameIn, String text, int lineWidth, int colorIn, Entity entity, Object... obs) {
		this(text, lineWidth, false, entity, obs);
		color = colorIn;
		name = nameIn;
	}

	private void addLine(String text) { // Change
		TextComponentString line = new TextComponentString(text);
		line.setStyle(style);
		lines.add(line);
	}

	public String getName() {
		if (sender != null) { return sender.getName(); }
		return name;
	}

	public void resetWidth(int lineWidth, boolean mcFont) {
		String line = "";
		final List<String> tempList = getStrings();
		String[] words = tempList.toArray(new String[0]);
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;

		String language = Minecraft.getMinecraft().getLanguageManager().getCurrentLanguage().getLanguageCode();
		if (language.startsWith("zh_") || language.startsWith("ja_")) {
			for(int i = 0; i < text.length(); ++i) {
				line = line + text.charAt(i);
				if ((mcFont ? font.getStringWidth(line) : ClientProxy.Font.width(line)) > lineWidth) {
					addLine(line);
					line = "";
				}
			}
		}
		else {
			String color = ((char) 167) + "r";
			for (String word : words) {
				if (word.isEmpty()) { continue; }
				if (word.length() == 1) {
					char c = word.charAt(0);
					if (c == '\r' || c == '\n') {
						addLine(color + line);
						color = Util.instance.getLastColor(color, line);
						line = "";
						continue;
					}
				}
				String newLine = line + word;
				int widthLine = (mcFont ? font.getStringWidth(newLine) : ClientProxy.Font.width(newLine));
				if (widthLine > lineWidth && !line.isEmpty()) {
					addLine(color + line);
					color = Util.instance.getLastColor(color, line);
					line = word;
				}
				else { line = newLine; }
			}
			if (!line.isEmpty()) { addLine(color + line); }
		}
	}

	private List<String> getStrings() {
		String tempText = text;
		List<String> tempList = new ArrayList<>();
		int fm;
		while (true) {
			fm = -1;
            String corr = "" + ((char) 9) + ((char) 10) + " ()[]{}.,<>:;+-*\\/\"";
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
		return tempList;
	}

}
