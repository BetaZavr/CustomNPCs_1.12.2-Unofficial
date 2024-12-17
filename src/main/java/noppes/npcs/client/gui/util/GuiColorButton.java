package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiColorButton extends GuiNpcButton {

	public int color;

	public GuiColorButton(int id, int x, int y, int color) {
		super(id, x, y, 50, 20, "");
		this.color = color;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		drawRect(x, y, x + 50, y + height, new Color(0x80, 0x80, 0x80, 0xFF).getRGB());
		drawRect(x + 1, y + 1, x + 49, y + height - 1, 0xFF000000 + color);
	}

}
