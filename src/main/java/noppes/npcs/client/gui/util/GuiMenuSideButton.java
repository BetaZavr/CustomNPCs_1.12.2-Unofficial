package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public class GuiMenuSideButton
extends GuiNpcButton {
	
	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menusidebutton.png");
	public boolean active;

	public GuiMenuSideButton(int i, int j, int k, int l, int i1, String s) {
		super(i, j, k, l, i1, s);
		this.active = false;
	}

	public GuiMenuSideButton(int i, int j, int k, String s) {
		this(i, j, k, 200, 20, s);
	}

	public void drawButton(Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.visible) { return; }
		FontRenderer fontrenderer = minecraft.fontRenderer;
		minecraft.renderEngine.bindTexture(GuiMenuSideButton.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int width = this.width + (this.active ? 2 : 0);
		this.hovered = (i >= this.x && j >= this.y && i < this.x + width && j < this.y + this.height);
		int k = this.getHoverState(this.hovered);
		this.drawTexturedModalRect(this.x, this.y, 0, k * 22, width, this.height);
		this.mouseDragged(minecraft, i, j);
		String text = "";
		float maxWidth = width * 0.75f;
		if (fontrenderer.getStringWidth(this.displayString) > maxWidth) {
			for (int h = 0; h < this.displayString.length(); ++h) {
				char c = this.displayString.charAt(h);
				if (fontrenderer.getStringWidth(text + c) > maxWidth) {
					break;
				}
				text += c;
			}
			text += "...";
		} else {
			text = this.displayString;
		}
		int l = CustomNpcs.mainColor;
		if (this.packedFGColour != 0) { l = this.packedFGColour; }
		else if (!this.active) { l = CustomNpcs.notEnableColor; }
		else if (this.hovered) { l = CustomNpcs.hoverColor; }
		this.drawCenteredString(fontrenderer, text, this.x + width / 2, this.y + (this.height - 8) / 2, l);
	}

	public int getHoverState(boolean flag) {
		if (this.active) {
			return 0;
		}
		return 1;
	}

	protected void mouseDragged(Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		return !this.active && this.visible && this.hovered;
	}

	public void mouseReleased(int i, int j) {
	}

}
