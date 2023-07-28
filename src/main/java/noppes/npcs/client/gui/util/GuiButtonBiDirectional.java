package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public class GuiButtonBiDirectional
extends GuiNpcButton {
	
	public static ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/arrowbuttons.png");
	public boolean cheakWidth;
	private int color;

	public GuiButtonBiDirectional(int id, int x, int y, int width, int height, String[] arr, int current) {
		super(id, x, y, width, height, arr, current);
		this.cheakWidth = true;
		this.color = CustomNpcs.mainColor;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) { return; }
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		boolean hoverL = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + 14 && mouseY < this.y + this.height;
		boolean hoverR = !hoverL && mouseX >= this.x + this.width - 14 && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		if (this.layerColor != 0) {
			Gui.drawRect(this.x + 7, this.y + 1, this.x + this.width - 7, this.y + this.height - 1, this.layerColor);
		}
		this.drawHorizontalLine(this.x + 7, this.x + this.width - 12, this.y, 0xFF000000);
		this.drawHorizontalLine(this.x + 7, this.x + this.width - 12, this.y + this.height - 1, 0xFF000000);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiButtonBiDirectional.resource);
		this.drawTexturedModalRect(this.x, this.y, 0, hoverL ? 40 : 20, 11, 20);
		this.drawTexturedModalRect(this.x + this.width - 11, this.y, 11, ((this.hovered && !hoverL) || hoverR) ? 40 : 20, 11, 20);
		int l = this.color;
		if (this.packedFGColour != 0) { l = this.packedFGColour; }
		else if (!this.enabled) { l = CustomNpcs.notEnableColor; }
		else if (this.hovered) { l = CustomNpcs.hoverColor; }
		
		String text = "";
		float maxWidth = (this.width - 36);
		if (this.cheakWidth && mc.fontRenderer.getStringWidth(this.displayString) > maxWidth) {
			for (int h = 0; h < this.displayString.length(); ++h) {
				char c = this.displayString.charAt(h);
				text += c;
				if (mc.fontRenderer.getStringWidth(text) > maxWidth) {
					break;
				}
			}
			text += "...";
		}
		else { text = this.displayString; }
		if (this.hovered) { text = new String(Character.toChars(0x00A7)) + "n" + text; }
		
		this.drawCenteredString(mc.fontRenderer, text, this.x + this.width / 2, this.y + (this.height - 8) / 2, l);
	}

	@Override
	public boolean mousePressed(Minecraft minecraft, int mouseX, int mouseY) {
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
