package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class GuiNpcButton
extends GuiButton {
	
	protected String[] display;
	private int displayValue;
	public int id;
	// New
	public int layerColor = 0, textureX = 0, textureY = 0;
	private ResourceLocation texture = null;
	public String lable = "";
	public boolean dropShadow;

	public GuiNpcButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture) {
		this(id, x, y, width, height, "");
		this.display = new String[] { "" };
		this.displayValue = 0;
		this.texture = texture;
		this.textureX = textureX;
		this.textureY = textureY;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, int val, String... display) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String string) {
		super(id, x, y, width, height, new TextComponentTranslation(string).getFormattedText());
		this.displayValue = 0;
		this.id = id;
		this.layerColor = 0;
		this.dropShadow = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String string, boolean enabled) {
		this(id, x, y, width, height, string);
		this.enabled = enabled;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String[] display, int val) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, String s) {
		super(id, x, y, new TextComponentTranslation(s).getFormattedText());
		this.lable = s;
		this.displayValue = 0;
		this.layerColor = 0;
		this.id = id;
		this.dropShadow = true;
	}

	public GuiNpcButton(int id, int x, int y, String[] display, int val) {
		this(id, x, y, display[val]);
		this.display = display;
		this.displayValue = val;
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) { return; }
		if (this.texture == null) {
			mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
			if (this.layerColor != 0) { GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f, (float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f, (float) (this.layerColor >> 24 & 255) / 255.0f); }
			else { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
			if (this.height<20 && this.height>=6) {
				this.drawTexturedModalRect(this.x, this.y+this.height-3, 0, 63 + i * 20, this.width / 2, 3);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y+this.height-3, 200 - this.width / 2, 63 + i * 20, this.width / 2, 3);
			}
			if (this.height>20 && this.height<=40) {
				int h = this.height-17;
				this.drawTexturedModalRect(this.x, this.y+17, 0, 66 - h + i * 20, this.width / 2, h);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y+17, 200 - this.width / 2, 66 - h + i * 20, this.width / 2, h);
			}

			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);

			this.mouseDragged(mc, mouseX, mouseY);
			
			int color = 0xFFE0E0E0;
			if (this.packedFGColour != 0) { color = this.packedFGColour; }
			else if (!this.enabled) { color = 0xFFA0A0A0; }
			else if (this.hovered) { color = 0xFFFFFFA0; }

			mc.fontRenderer.drawString(this.displayString, this.x + (this.width - mc.fontRenderer.getStringWidth(this.displayString)) / 2, this.y + (this.height - 8) / 2, color, this.dropShadow);
			//this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
		} else {
			GlStateManager.translate(0.0f, 0.0f, this.id);
			mc.getTextureManager().bindTexture(this.texture);
			if (this.layerColor != 0) { GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f, (float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f, (float) (this.layerColor >> 24 & 255) / 255.0f); }
			else { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
			this.hovered = (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height);
			int i = this.hovered || !this.enabled ? 1 : 0;
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, this.textureX, this.textureY + i * this.height, this.width, this.height);
			int color = 0xFFE0E0E0;
			if (this.packedFGColour != 0) { color = this.packedFGColour; }
			else if (!this.enabled) { color = 0xFFA0A0A0; }
			else if (this.hovered) { color = 0xFFFFFFA0; }
			
			mc.fontRenderer.drawString(this.displayString, this.x + (this.width - mc.fontRenderer.getStringWidth(this.displayString)) / 2, this.y + (this.height - 8) / 2, color, this.dropShadow);
			//this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		}

	}

	public int getValue() {
		return this.displayValue;
	}

	// New
	public String[] getVariants() {
		return this.display;
	}

	public boolean getVisible() {
		return this.visible;
	}

	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}

	public boolean mousePressed(Minecraft minecraft, int i, int j) {
		boolean bo = super.mousePressed(minecraft, i, j);
		if (bo && this.display != null && this.display.length != 0) {
			this.displayValue = (this.displayValue + 1) % this.display.length;
			this.setDisplayText(this.display[this.displayValue]);
		}
		return bo;
	}

	public void setDisplay(int value) {
		this.displayValue = value;
		this.setDisplayText(this.display[value]);
	}

	public void setDisplayText(String text) {
		this.displayString = new TextComponentTranslation(text).getFormattedText();
	}

	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	public void setTextColor(int color) {
		this.packedFGColour = color;
	}

	public void setVisible(boolean b) {
		this.visible = b;
	}
}
