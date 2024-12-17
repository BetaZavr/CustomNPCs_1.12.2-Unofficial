package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiMenuSideButton
extends GuiNpcButton {

	private boolean left = false;
	public boolean active = false;
	public int data;
	public int offsetText = 0;

	/**
	 * @param id button
	 * @param x - if left, then right position of the button. And if right, then left position of the button
	 * @param y - top pos
	 * @param label - title (button name)
	 */
	public GuiMenuSideButton(int id, int x, int y, String label) {
		super(id, x, y, label);
		width = Minecraft.getMinecraft().fontRenderer.getStringWidth(displayString) + 10;
		height = 20;
		setIsLeft(true);
	}

	public void setIsLeft(boolean isLeft) {
		if (left != isLeft) {
			if (isLeft) { x -= width; } else { x += width; }
		}
		left = isLeft;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		GlStateManager.pushMatrix();
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		mc.getTextureManager().bindTexture(GuiNPCInterface.MENU_SIDE_BUTTON);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		hovered = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
		int state = getHoverState(hovered);
		int h0 = height / 2, h1 = height - h0;
		// background
		if (left) {
			drawTexturedModalRect(x, y, 0, state * 21, width, h0);
			drawTexturedModalRect(x, y + h0, 0, (21 - h1) + state * 21, width, h1);
			if (active || hovered) {
				drawTexturedModalRect(x + width, y, 197, state * 21, 3, h0);
				drawTexturedModalRect(x + width, y + h1, 197, (21 - h1) + state * 21, 3, h1);
			}
		} else {
			for (int i = 0, j = width; i < width & j >= 0; i++, j--) {
				drawTexturedModalRect(x + j - 1, y, i, state * 21, 1, h0);
				drawTexturedModalRect(x + j - 1, y + h0, i, (21 - h1) + state * 21, 1, h1);
			}
			if (active || hovered) {
				drawTexturedModalRect(x - 3, y, 200, state * 21, 3, h0);
				drawTexturedModalRect(x - 3, y + h0, 200, (21 - h1) + state * 21, 3, h1);
			}
		}
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
		mc.fontRenderer.drawString(displayString, x + 5.0f, y + offsetText + ((float) height - 8.0f) / 2.0f, color, dropShadow);
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

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		return !active && visible && hovered;
	}

}
