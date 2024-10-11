package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import org.lwjgl.input.Mouse;

import javax.annotation.Nonnull;

public class GuiMenuTopButton extends GuiNpcButton {

	public boolean hover;
	public boolean active;
	protected int height;
	public IButtonListener listener;

	public GuiMenuTopButton(int i, GuiButton parent, String s) {
		this(i, parent.x + parent.width, parent.y, s);
	}

	public GuiMenuTopButton(int i, GuiButton parent, String s, IButtonListener listener) {
		this(i, parent, s);
		this.listener = listener;
	}

	public GuiMenuTopButton(int i, int j, int k, String s) {
		super(i, j, k, s);
		this.hover = false;
		this.active = false;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString) + 12;
		this.height = 20;
	}

	public void drawButton(@Nonnull Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.getVisible()) {
			return;
		}
		this.hovered = i >= this.x && j >= this.y && i < this.x + this.width && j < this.y + this.height;
		GlStateManager.pushMatrix();
		minecraft.getTextureManager().bindTexture(GuiNPCInterface.MENU_TOP_BUTTON);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int height = this.height - (this.active ? 0 : 2);
		this.hover = (i >= this.x && j >= this.y && i < this.x + this.getWidth() && j < this.y + height);
		int k = this.getHoverState(this.hover);
		this.drawTexturedModalRect(this.x, this.y, 0, k * 20, this.getWidth() / 2, height);
		this.drawTexturedModalRect(this.x + this.getWidth() / 2, this.y, 200 - this.getWidth() / 2, k * 20, this.getWidth() / 2, height);
		this.mouseDragged(minecraft, i, j);
		FontRenderer fontrenderer = minecraft.fontRenderer;
		int l = CustomNpcs.MainColor.getRGB();
		if (this.packedFGColour != 0) {
			l = this.packedFGColour;
		} else if (this.hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}
		this.drawCenteredString(fontrenderer, this.displayString, this.x + this.getWidth() / 2,
				this.y + (height - 8) / 2, l);
		GlStateManager.popMatrix();
	}

	public int getHoverState(boolean hovered) {
		if (this.active) {
			if (hovered) {
				if (Mouse.isButtonDown(0)) { return 2; }
				return 1;
			}
			return 0;
		} else {
			if (hovered) {
				if (Mouse.isButtonDown(0)) { return this.enabled ? 5 : 7; }
				return this.enabled ? 4 : 6;
			}
			return 3;
		}
	}

	protected void mouseDragged(@Nonnull Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int i, int j) {
		boolean bo = !this.active && this.getVisible() && this.hover;
		if (bo && this.listener != null) {
			this.listener.actionPerformed(this);
			return false;
		}
		return bo;
	}

}
