package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiButtonBiDirectional
extends GuiNpcButton {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/arrowbuttons.png");
	public boolean checkWidth = true;
	public boolean showShadow = true;
	private boolean hoverL;
	private boolean hoverR;
	private final int color;

	public GuiButtonBiDirectional(int id, int x, int y, int width, int height, String[] arr, int current) {
		super(id, x, y, width, height, arr, current);
		color = CustomNpcs.MainColor.getRGB();
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		hoverL = hovered && mouseX < x + width / 2 && mouseY < y + height;
		hoverR = !hoverL && hovered;
		if (layerColor != 0) {
			Gui.drawRect(x + 7, y + 1, x + width - 7, y + height - 1, layerColor);
		}

		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.translate(x, y, 0.0f);
		float s = 1.0f;
		if (height != 20) {
			s = (float) height / 20.0f;
			GlStateManager.scale(s, s, 1.0f);
		}
		int c;
		int l = color;
		if (!enabled) {
			c = new Color(0xFF808080).getRGB();
			l = CustomNpcs.NotEnableColor.getRGB();
		} else if (hovered) {
			c = new Color(0xFF48528C).getRGB();
			l = CustomNpcs.HoverColor.getRGB();
		} else {
			c = new Color(0xFF707070).getRGB();
			if (packedFGColour != 0) { l = packedFGColour; }
		}
		RenderHelper.enableGUIStandardItemLighting();
		drawGradientRect(11, 0, (int) ((float) width / s - 11.0f), (int) (((float) height - 0.5f) / s), c, c);

		c = new Color(0xFF000000).getRGB();
		drawHorizontalLine(11, (int) ((float) width / s - 11.0f), 0, c);
		drawHorizontalLine(11, (int) ((float) width / s - 11.0f), (int) (((float) height - 0.5f) / s), c);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiButtonBiDirectional.resource);
		drawTexturedModalRect(0, 0, 0, hoverL ? 40 : 20, 11, 20);
		drawTexturedModalRect((int) ((float) width / s - 11.0f), 0, 11, hoverR ? 40 : 20, 11, 20);
		GlStateManager.popMatrix();

		String text = "";
		float maxWidth = (width - 36);
		if (checkWidth && mc.fontRenderer.getStringWidth(displayString) > maxWidth) {
			for (int h = 0; h < displayString.length(); ++h) {
				text += displayString.charAt(h);
				if (mc.fontRenderer.getStringWidth(text) > maxWidth) {
					break;
				}
			}
			text += "...";
		} else {
			text = displayString;
		}
		if (hovered) {
			text = ((char) 167) + "n" + text;
		}

		if (showShadow) {
			drawCenteredString(mc.fontRenderer, text, x + width / 2, y + (height - 10) / 2, l);
		} else {
			mc.fontRenderer.drawString(text, x + (float) (width - mc.fontRenderer.getStringWidth(text)) / 2, y + (float) (height - 10) / 2, l, false);
		}
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft minecraft, int mouseX, int mouseY) {
		int value = getValue();
		boolean bo = super.mousePressed(minecraft, mouseX, mouseY);
		if (bo && display != null && display.length != 0) {
			if (hoverR) {
				value = (value + 1) % display.length;
			}
			if (hoverL) {
				if (value <= 0) {
					value = display.length;
				}
				--value;
			}
			setDisplay(value);
		}
		return bo;
	}

}
