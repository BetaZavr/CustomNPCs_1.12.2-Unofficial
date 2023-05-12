package noppes.npcs.client.gui.util;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.util.ValueUtil;

public class GuiNpcSlider
extends GuiButton {
	
	public boolean dragging;
	public int id;
	private ISliderListener listener;
	public float sliderValue;

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		if (this.listener != null) {
			this.listener.mouseDragged(this);
		}
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, int width, int height, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		this.width = width;
		this.height = height;
		if (this.listener != null) {
			this.listener.mouseDragged(this);
		}
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, String displayString, float sliderValue) {
		super(id, xPos, yPos, 150, 20, NoppesStringUtils.translate(displayString));
		this.sliderValue = 1.0f;
		this.id = id;
		this.sliderValue = sliderValue;
		if (parent instanceof ISliderListener) {
			this.listener = (ISliderListener) parent;
		}
	}
	
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) { return; }
		mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		int i = this.getHoverState(this.hovered);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
		this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20, this.width / 2, this.height);
		this.mouseDragged(mc, mouseX, mouseY);
		int j = 14737632;
		if (this.packedFGColour != 0) { j = this.packedFGColour; }
		else if (!this.enabled) { j = 10526880; }
		else if (this.hovered) { j = 16777120; }
		this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + (this.height - 8) / 2, j);
	}
	
	public String getDisplayString() { return this.displayString; }

	public int getHoverState(boolean par1) { return 0; }

	public void mouseDragged(Minecraft mc, int par2, int par3) {
		if (!this.visible) {
			return;
		}
		mc.getTextureManager().bindTexture(GuiNpcSlider.BUTTON_TEXTURES);
		if (this.dragging) {
			this.sliderValue = ValueUtil.correctFloat((par2 - (this.x + 4)) / (float) (this.width - 8), 0.0f, 1.0f);
			if (this.listener != null) {
				this.listener.mouseDragged(this);
			}
			if (!Mouse.isButtonDown(0)) {
				this.mouseReleased(0, 0);
			}
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)), this.y, 0, 66, 4, 20);
		this.drawTexturedModalRect(this.x + (int) (this.sliderValue * (this.width - 8)) + 4, this.y, 196, 66, 4, 20);
	}

	public boolean mousePressed(Minecraft par1Minecraft, int par2, int par3) {
		if (this.enabled && this.visible && par2 >= this.x && par3 >= this.y && par2 < this.x + this.width
				&& par3 < this.y + this.height) {
			this.sliderValue = ValueUtil.correctFloat((par2 - (this.x + 4)) / (float) (this.width - 8), 0.0f, 1.0f);
			if (this.listener != null) {
				this.listener.mousePressed(this);
			}
			return this.dragging = true;
		}
		return false;
	}

	public void mouseReleased(int par1, int par2) {
		this.dragging = false;
		if (this.listener != null) {
			this.listener.mouseReleased(this);
		}
	}

	public void setString(String str) {
		try {
			float f = Math.round(Float.parseFloat(str) * 10.0d);
			str = String.valueOf(f / 10.0f).replace(".", ",");
		} catch (Exception e) {
		}
		this.displayString = NoppesStringUtils.translate(str);
	}

}
