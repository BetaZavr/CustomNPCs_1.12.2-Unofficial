package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;
import java.awt.*;

public class GuiButtonBiDirectional extends GuiNpcButton {

	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/arrowbuttons.png");
	public boolean checkWidth = true;
	public boolean showShadow = true;

	protected boolean hoverL;
	protected boolean hoverR;
	protected final int color;

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
		if (!enabled) {
			c = new Color(0xFF303030).getRGB();
		} else if (hovered) {
			c = new Color(0xFF48528C).getRGB();
		} else {
			c = new Color(0xFF707070).getRGB();
		}
		drawGradientRect(11, 0, (int) ((float) width / s - 11.0f), (int) (((float) height - 0.5f) / s), c, c);

		c = new Color(0xFF000000).getRGB();
		drawHorizontalLine(11, (int) ((float) width / s - 11.0f), 0, c);
		drawHorizontalLine(11, (int) ((float) width / s - 11.0f), (int) (((float) height - 0.5f) / s), c);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiButtonBiDirectional.resource);
		int state = !enabled ? 20 : hoverL ? 40 : 0;
		drawTexturedModalRect(0, 0, 0, state, 11, 20);
		state = !enabled ? 20 : hoverR ? 40 : 0;
		drawTexturedModalRect((int) ((float) width / s - 11.0f), 0, 11, state, 11, 20);
		GlStateManager.popMatrix();

		String text = "";
		float maxWidth = (width - 36);
		if (checkWidth && mc.fontRenderer.getStringWidth(displayString) > maxWidth) {
			for (int h = 0; h < displayString.length(); ++h) {
				text += displayString.charAt(h);
				if (mc.fontRenderer.getStringWidth(text) > maxWidth) { break; }
			}
			text += "...";
		}
		else { text = displayString; }
		if (hovered && enabled) { text = TextFormatting.UNDERLINE + text; }

		c = color;
		if (packedFGColour != 0) { c = packedFGColour; }
		else if (!enabled) { c = CustomNpcs.NotEnableColor.getRGB(); }
		else if (hovered) { c = CustomNpcs.HoverColor.getRGB(); }

		renderString(mc.fontRenderer, text, x + 11, y, x + width - 11, y + height, c, showShadow, true);

	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		int value = getValue();
		boolean bo = false;
		if (visible && enabled && display != null && display.length != 0) {
			if (hoverR) {
				value = (value + 1) % display.length;
				bo = true;
			}
			if (hoverL) {
				if (value <= 0) { value = display.length; }
				--value;
				bo = true;
			}
			setDisplay(value);
		}
		return bo || super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

	public GuiButtonBiDirectional setCheckWidth(boolean checkWidthIn) {
		checkWidth = checkWidthIn;
		return this;
	}

}
