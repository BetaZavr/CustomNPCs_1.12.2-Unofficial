package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

public class GuiMenuLeftButton
extends GuiNpcButton {
	
	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menuleftbutton.png");
	public boolean active;
	protected int height;
	public boolean hover;
	public IButtonListener listener;
	public boolean rotated;

	public GuiMenuLeftButton(int i, GuiButton parent, String s) {
		this(i, parent.x + parent.width, parent.y, s);
	}

	public GuiMenuLeftButton(int i, GuiButton parent, String s, IButtonListener listener) {
		this(i, parent, s);
		this.listener = listener;
	}

	public GuiMenuLeftButton(int i, int j, int k, String s) {
		super(i, j, k, new TextComponentTranslation(s).getFormattedText());
		this.hover = false;
		this.rotated = false;
		this.active = false;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString) + 12;
		this.height = 20;
		this.x -= this.width;
	}

	public void drawButton(Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.getVisible()) { return; }
		GlStateManager.pushMatrix();
		minecraft.renderEngine.bindTexture(GuiMenuLeftButton.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.hover = (i >= this.x && j >= this.y && i < this.x + this.getWidth() && j < this.y + 20);
		int k = this.getHoverState(this.hover);
		this.drawTexturedModalRect(this.x, this.y, 0, k * 20, this.getWidth(), 20);
		if (this.active) {
			this.drawTexturedModalRect(this.x+this.getWidth(), this.y+1, 197, 1+k * 20, 2, 18);
			this.drawTexturedModalRect(this.x+this.getWidth()+2, this.y+1, 199, 1+k * 20, 1, 17);
		}
		this.mouseDragged(minecraft, i, j);
		FontRenderer fontrenderer = minecraft.fontRenderer;
		if (this.rotated) { GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f); }
		int color = 14737632;
		if (this.active) { color = 16777120; }
		else if (this.hover) { color = 16777120; }
		this.drawCenteredString(fontrenderer, this.displayString, this.x + this.getWidth() / 2, this.y + (20 - 8) / 2, color);
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
