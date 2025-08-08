package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiColorButton extends GuiNpcButton {

	public int color;

	public GuiColorButton(int id, int x, int y, int width, int height, int colorIn) {
		super(id, x, y, width, height, "");
		color = 0xFF000000 | colorIn & 0x00FFFFFF;
	}

	public GuiColorButton(int id, int x, int y, int colorIn) {
		this(id, x, y, 50, 20, colorIn);
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		drawRect(x, y, x + width, y + height, new Color(0x80, 0x80, 0x80, 0xFF).getRGB());
		drawRect(x + 1, y + 1, x + width - 1, y + height - 1, color);
	}

}
