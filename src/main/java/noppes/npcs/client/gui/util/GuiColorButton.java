package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;

import javax.annotation.Nonnull;

public class GuiColorButton extends GuiNpcButton {

	public int color;

	public GuiColorButton(int id, int x, int y, int color) {
		super(id, x, y, 50, 20, "");
		this.color = color;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) { return; }
		drawRect(this.x, this.y, this.x + 50, this.y + this.height, 0xFF808080);
		drawRect(this.x + 1, this.y + 1, this.x + 49, this.y + this.height - 1, 0xFF000000 + this.color);
	}

}
