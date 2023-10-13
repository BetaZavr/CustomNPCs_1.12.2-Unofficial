package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.TextBlock;
import noppes.npcs.util.AdditionalMethods;

public class TextBlockClient extends TextBlock {
	public int color;
	private String name;
	private ICommandSender sender;
	private Style style;

	public TextBlockClient(ICommandSender sender, String text, int lineWidth, int color, Object... obs) {
		this(text, lineWidth, false, obs);
		this.color = color;
		this.sender = sender;
	}

	public TextBlockClient(String text, int lineWidth, boolean mcFont, Object... obs) {
		this.color = 0xE0E0E0;
		this.style = new Style();
		text = NoppesStringUtils.formatText(text, obs);
		String line = "";
		text = text.replace("\n", " \n ");
		text = text.replace("\r", " \r ");
		String[] words = text.split(" ");
		FontRenderer font = Minecraft.getMinecraft().fontRenderer;
		String color = ((char) 167) + "r";
		for (String word : words) {
			Label_0235: {
				if (!word.isEmpty()) {
					if (word.length() == 1) {
						char c = word.charAt(0);
						if (c == '\r' || c == '\n') {
							this.addLine(color + line);
							color = AdditionalMethods.getLastColor(color, line);
							line = "";
							break Label_0235;
						}
					}
					String newLine;
					if (line.isEmpty()) { newLine = word; } else { newLine = line + " " + word; }
					if ((mcFont ? font.getStringWidth(newLine) : ClientProxy.Font.width(newLine)) > lineWidth) {
						this.addLine(color + line);
						color = AdditionalMethods.getLastColor(color, line);
						line = word.trim();
					} else {
						line = newLine;
					}
				}
			}
		}
		if (!line.isEmpty()) {
			this.addLine(color + line);
		}
	}

	public TextBlockClient(String name, String text, int lineWidth, int color, Object... obs) {
		this(text, lineWidth, false, obs);
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

}
