package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public class GuiMenuTopButton
extends GuiNpcButton {
	
	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menutopbutton.png");
	public boolean active;
	protected int height;
	public boolean hover;
	public IButtonListener listener;
	public boolean rotated;

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
		this.rotated = false;
		this.active = false;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString) + 12;
		this.height = 20;
	}

	public void drawButton(Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.getVisible()) {
			return;
		}
        this.hovered = i >= this.x && j >= this.y && i < this.x + this.width && j < this.y + this.height;
		GlStateManager.pushMatrix();
		minecraft.renderEngine.bindTexture(GuiMenuTopButton.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int height = this.height - (this.active ? 0 : 2);
		this.hover = (i >= this.x && j >= this.y && i < this.x + this.getWidth() && j < this.y + height);
		int k = this.getHoverState(this.hover);
		this.drawTexturedModalRect(this.x, this.y, 0, k * 20, this.getWidth() / 2, height);
		this.drawTexturedModalRect(this.x + this.getWidth() / 2, this.y, 200 - this.getWidth() / 2, k * 20,
				this.getWidth() / 2, height);
		this.mouseDragged(minecraft, i, j);
		FontRenderer fontrenderer = minecraft.fontRenderer;
		if (this.rotated) {
			GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
		}
		if (this.active) {
			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.getWidth() / 2,
					this.y + (height - 8) / 2, 16777120);
		} else if (this.hover) {
			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.getWidth() / 2,
					this.y + (height - 8) / 2, 16777120);
		} else {
			this.drawCenteredString(fontrenderer, this.displayString, this.x + this.getWidth() / 2,
					this.y + (height - 8) / 2, 14737632);
		}
		GlStateManager.popMatrix();
	}

	public int getHoverState(boolean flag) {
		byte byte0 = 1;
		if (this.active) {
			byte0 = 0;
		} else if (flag) {
			byte0 = 2;
		}
		return byte0;
	}

	protected void mouseDragged(Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		boolean bo = !this.active && this.getVisible() && this.hover;
		if (bo && this.listener != null) {
			this.listener.actionPerformed(this);
			return false;
		}
		return bo;
	}

	public void mouseReleased(int i, int j) {
	}

}
