package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiMenuTopButton
extends GuiNpcButton {

	public boolean active = false;
	protected int height = 20;
	public int offsetW = 0;

	public GuiMenuTopButton(int id, GuiButton parent, String label) {
		this(id, parent.x + parent.width, parent.y, label);
	}

	public GuiMenuTopButton(int id, int x, int y, String label) {
		super(id, x, y, label);
		width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString) + 10;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!getVisible()) {
			return;
		}
		int w = offsetW + width;
		hovered = mouseX >= x && mouseY >= y && mouseX < x + w && mouseY < y + height;
		GlStateManager.pushMatrix();
		mc.getTextureManager().bindTexture(GuiNPCInterface.MENU_TOP_BUTTON);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int height = this.height - (active || hovered ? 0 : 2);
		int state = getHoverState(hovered);
		// background
		drawTexturedModalRect(x, y, 0, state * 20, w / 2, height);
		drawTexturedModalRect(x + w / 2, y, 200 - w / 2, state * 20, w / 2, height);
		// mouse
		mouseDragged(mc, mouseX, mouseY);
		// label
		int color = CustomNpcs.MainColor.getRGB();
		if (packedFGColour != 0) {
			color = packedFGColour;
		} else if (!enabled) {
			color = CustomNpcs.NotEnableColor.getRGB();
		} else if (hovered) {
			color = CustomNpcs.HoverColor.getRGB();
		}
		mc.fontRenderer.drawString(displayString, x + 5.0f + offsetW, y + ((float) height - 8.0f) / 2.0f, color, dropShadow);
		GlStateManager.popMatrix();
	}

	public int getHoverState(boolean hovered) {
		if (isSimple) { return super.getHoverState(hovered); }
		if (!enabled) {
			return Mouse.isButtonDown(0) ? 6 : 7;
		}
		if (hovered) {
			if (Mouse.isButtonDown(0)) { return active ? 2 : 5; }
			return active ? 1 : 4;
		}
		return active ? 0 : 3;
	}

	protected void mouseDragged(@Nonnull Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		return !active && visible && hovered;
	}

}
