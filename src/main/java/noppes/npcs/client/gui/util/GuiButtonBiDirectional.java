package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;

public class GuiButtonBiDirectional extends GuiNpcButton {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/arrowbuttons.png");
	public boolean checkWidth, showShadow;
	private final int color;

	public GuiButtonBiDirectional(int id, int x, int y, int width, int height, String[] arr, int current) {
		super(id, x, y, width, height, arr, current);
		this.checkWidth = true;
		this.color = CustomNpcs.MainColor.getRGB();
		this.showShadow = true;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
				&& mouseY < this.y + this.height;
		boolean hoverL = mouseX >= this.x && mouseX < this.x + this.width / 2 && mouseY >= this.y
				&& mouseY < this.y + this.height;
		boolean hoverR = !hoverL && this.hovered;
		if (this.layerColor != 0) {
			Gui.drawRect(this.x + 7, this.y + 1, this.x + this.width - 7, this.y + this.height - 1, this.layerColor);
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(this.x, this.y, 0.0f);
		float s = 1.0f;
		if (height != 20) {
			s = (float) height / 20.0f;
			GlStateManager.scale(s, s, 1.0f);
		}
		if (!this.enabled) {
			this.drawGradientRect(11, 0, (int) ((float) this.width / s - 11.0f),
					(int) (((float) this.height - 0.5f) / s), 0xFF808080, 0xFF808080);
		} else if (this.hovered) {
			this.drawGradientRect(11, 0, (int) ((float) this.width / s - 11.0f),
					(int) (((float) this.height - 0.5f) / s), 0xFF7E88BF, 0xFF7E88BF);
		}
		this.drawHorizontalLine(11, (int) ((float) this.width / s - 11.0f), 0, 0xFF000000);
		this.drawHorizontalLine(11, (int) ((float) this.width / s - 11.0f), (int) (((float) this.height - 0.5f) / s),
				0xFF000000);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiButtonBiDirectional.resource);
		this.drawTexturedModalRect(0, 0, 0, hoverL ? 40 : 20, 11, 20);
		this.drawTexturedModalRect((int) ((float) this.width / s - 11.0f), 0, 11, hoverR ? 40 : 20, 11, 20);
		GlStateManager.popMatrix();

		int l = this.color;
		if (this.packedFGColour != 0) {
			l = this.packedFGColour;
		} else if (!this.enabled) {
			l = CustomNpcs.NotEnableColor.getRGB();
		} else if (this.hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}

		String text = "";
		float maxWidth = (this.width - 36);
		if (this.checkWidth && mc.fontRenderer.getStringWidth(this.displayString) > maxWidth) {
			for (int h = 0; h < this.displayString.length(); ++h) {
				char c = this.displayString.charAt(h);
				text += c;
				if (mc.fontRenderer.getStringWidth(text) > maxWidth) {
					break;
				}
			}
			text += "...";
		} else {
			text = this.displayString;
		}
		if (this.hovered) {
			text = ((char) 167) + "n" + text;
		}

		if (this.showShadow) {
			this.drawCenteredString(mc.fontRenderer, text, this.x + this.width / 2, this.y + (this.height - 10) / 2, l);
		} else {
			mc.fontRenderer.drawString(text, this.x + (float) (this.width - mc.fontRenderer.getStringWidth(text)) / 2,
					this.y + (float) (this.height - 10) / 2, l, false);
		}

	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		int value = this.getValue();
		boolean bo = super.mousePressed(minecraft, mouseX, mouseY);
		if (bo && this.display != null && this.display.length != 0) {
			boolean hoverL = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + 14
					&& mouseY < this.y + this.height;
			boolean hoverR = !hoverL && mouseX >= this.x + 14 && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height;
			if (hoverR) {
				value = (value + 1) % this.display.length;
			}
			if (hoverL) {
				if (value <= 0) {
					value = this.display.length;
				}
				--value;
			}
			this.setDisplay(value);
		}
		return bo;
	}

}
