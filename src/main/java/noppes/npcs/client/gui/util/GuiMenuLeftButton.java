package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;

public class GuiMenuLeftButton extends GuiNpcButton {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menuleftbutton.png");
	public boolean active;
	public int width, height, offsetYtext, data;
	public IButtonListener listener;
	public boolean rotated;

	public GuiMenuLeftButton(int i, int j, int k, String s) {
		super(i, j, k, new TextComponentTranslation(s).getFormattedText());
		this.rotated = false;
		this.active = false;
		this.width = Minecraft.getMinecraft().fontRenderer.getStringWidth(this.displayString) + 12;
		this.height = 20;
		this.offsetYtext = 0;
		this.x -= this.width;
	}

	public void drawButton(@Nonnull Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.getVisible()) {
			return;
		}
		GlStateManager.pushMatrix();
		minecraft.renderEngine.bindTexture(GuiMenuLeftButton.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.hovered = (i >= this.x && j >= this.y && i < this.x + this.width && j < this.y + this.height);
		int k = this.getHoverState(this.hovered);
		if (this.height == 20) {
			this.drawTexturedModalRect(this.x, this.y, 0, k * 20, this.width,
					20 + (!this.active && this.hovered ? 1 : 0));
			if (this.active) {
				this.drawTexturedModalRect(this.x + this.getWidth(), this.y + 1, 197, 1 + k * 20, 2, 18);
				this.drawTexturedModalRect(this.x + this.getWidth() + 2, this.y + 1, 199, 1 + k * 20, 1, 17);
			}
		} else {
			this.drawTexturedModalRect(this.x, this.y, 0, k * 20, this.width,
					this.height + (!this.active && this.hovered ? 1 : 0) - 4);
			this.drawTexturedModalRect(this.x, this.y + this.height - 4, 0, (k + 1) * 20 - 4, this.width,
					4 + (!this.active && this.hovered ? 1 : 0));
		}
		this.mouseDragged(minecraft, i, j);
		FontRenderer fontrenderer = minecraft.fontRenderer;
		if (this.rotated) {
			GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
		}
		int l = CustomNpcs.MainColor.getRGB();
		if (this.packedFGColour != 0) {
			l = this.packedFGColour;
		} else if (this.hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}

		this.drawCenteredString(fontrenderer, this.displayString, this.x + this.width / 2,
				this.y + this.offsetYtext + (this.height - 8) / 2, l);
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

	protected void mouseDragged(@Nonnull Minecraft minecraft, int i, int j) {
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int i, int j) {
		boolean bo = !this.active && this.getVisible() && this.hovered;
		if (bo && this.listener != null) {
			this.listener.actionPerformed(this);
			return false;
		}
		return bo;
	}

}
