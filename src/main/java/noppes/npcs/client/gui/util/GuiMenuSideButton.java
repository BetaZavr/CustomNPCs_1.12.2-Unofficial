package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiMenuSideButton extends GuiNpcButton {

	public boolean active;
	public boolean isLeft = true;

	public GuiMenuSideButton(int id, int x, int y, int width, int height, String lable) {
		super(id, x, y, width, height, lable);
		this.active = false;
	}

	public void drawButton(@Nonnull Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.visible) {
			return;
		}
		FontRenderer fontrenderer = minecraft.fontRenderer;
		minecraft.renderEngine.bindTexture(GuiNPCInterface.MENU_SIDE_BUTTON);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int width = this.width + (this.active ? 2 : 0);
		this.hovered = (i >= this.x && j >= this.y && i < this.x + width && j < this.y + this.height);
		int k = this.getHoverState(this.hovered);
		this.drawTexturedModalRect(this.x, this.y, 0, k * 22, width, this.height);
		this.mouseDragged(minecraft, i, j);
		StringBuilder text = new StringBuilder();
		float maxWidth = width * 0.75f;
		if (fontrenderer.getStringWidth(this.displayString) > maxWidth) {
			for (int h = 0; h < this.displayString.length(); ++h) {
				char c = this.displayString.charAt(h);
				if (fontrenderer.getStringWidth(text.toString() + c) > maxWidth) {
					break;
				}
				text.append(c);
			}
			text.append("...");
		} else {
			text = new StringBuilder(this.displayString);
		}
		int l = CustomNpcs.MainColor.getRGB();
		if (this.packedFGColour != 0) {
			l = this.packedFGColour;
		} else if (!this.active) {
			l = CustomNpcs.NotEnableColor.getRGB();
		} else if (this.hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}
		this.drawCenteredString(fontrenderer, text.toString(), this.x + width / 2, this.y + (this.height - 8) / 2, l);
	}

	public int getHoverState(boolean flag) {
		int i = 1;
		if (this.enabled) {

		} else {

		}
		if (this.active) { return 0; }
		if (this.hovered) {
			return Mouse.isButtonDown(0) ? 3 : 2;
		}
		if (isLeft) { i += 4; }
		return i;
	}

	protected void mouseDragged(@Nonnull Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int i, int j) {
		return !this.active && this.visible && this.hovered;
	}

}
