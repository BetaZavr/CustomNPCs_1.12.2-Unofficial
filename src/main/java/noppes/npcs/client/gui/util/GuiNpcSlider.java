package noppes.npcs.client.gui.util;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.util.ValueUtil;

public class GuiNpcSlider
extends GuiButton {
	
	public boolean dragging, isVertical;
	public int id;
	private ISliderListener listener;
	public float sliderValue;

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		if (this.listener != null) { this.listener.mouseDragged(this); }
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, int width, int height, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		this.width = width;
		this.height = height;
		this.isVertical = height > width;
		if (this.listener != null) { this.listener.mouseDragged(this); }
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, String displayString, float sliderValue) {
		super(id, xPos, yPos, 150, 20, NoppesStringUtils.translate(displayString));
		this.sliderValue = 1.0f;
		this.id = id;
		this.sliderValue = sliderValue;
		if (parent instanceof ISliderListener) { this.listener = (ISliderListener) parent; }
		this.visible = true;
	}
	
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) { return; }
		mc.renderEngine.bindTexture(BUTTON_TEXTURES);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.x, this.y, 0.0f);
		// place:
		int w = this.width, h = this.height;
		if (this.isVertical) {
			w = this.height;
			h = this.width;
		}
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		if (this.isVertical) {
			GlStateManager.translate(h, 0.0f, 0.0f);
			GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
		}
		this.drawTexturedModalRect(0, 0, 0, 46 , w / 2, h);
		this.drawTexturedModalRect(w / 2, 0, 200 - w / 2, 46 , w / 2, h);
		if (h<20) {
			this.drawHorizontalLine(0, w-2, h-1, 0xFF000000);
		}
		GlStateManager.popMatrix();
		// button:
		this.mouseDragged(mc, mouseX, mouseY);
		// text:
		if (this.displayString.isEmpty()) { return; }
		
		int l = CustomNpcs.MainColor.getRGB();
		if (this.packedFGColour != 0) { l = this.packedFGColour; }
		else if (!this.enabled) { l = CustomNpcs.NotEnableColor.getRGB(); }
		else if (this.hovered) { l = CustomNpcs.HoverColor.getRGB(); }
		this.drawCenteredString(mc.fontRenderer, this.displayString, this.x + this.width / 2, this.y + 2 + (this.height - ClientProxy.Font.height(this.displayString)) / 2, l);
	}
	
	public String getDisplayString() { return this.displayString; }

	public int getHoverState(boolean par1) { return 0; }

	public void mouseDragged(Minecraft mc, int mouseX, int mouseY) {
		if (!this.visible) { return; }
		mc.renderEngine.bindTexture(GuiNpcSlider.BUTTON_TEXTURES);
		int w = this.width, h = this.height;
		if (this.isVertical) {
			w = this.height;
			h = this.width;
		}
		if (this.dragging) {
			if (this.isVertical) { this.sliderValue = ValueUtil.correctFloat((mouseY - (this.y + 4)) / (float) (w - 8), 0.0f, 1.0f); }
			else { this.sliderValue = ValueUtil.correctFloat((mouseX - (this.x + 4)) / (float) (w - 8), 0.0f, 1.0f); }
			if (this.listener != null) { this.listener.mouseDragged(this); }
			if (!Mouse.isButtonDown(0)) { this.mouseReleased(0, 0); }
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int x = this.x + (int) (this.sliderValue * (w - 8)), y = this.y;
		GlStateManager.pushMatrix();
		if (this.isVertical) {
			x = this.x;
			y = this.y + (int) (this.sliderValue * (w - 8));
			GlStateManager.translate(x, y, 0.0f);
			GlStateManager.translate(h, 0.0f, 0.0f);
			GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
		} else { GlStateManager.translate(x, y, 0.0f); }
		this.drawTexturedModalRect(0, 0, 0, 66, 4, h);
		this.drawTexturedModalRect(4, 0, 196, 66, 4, h);
		if (h<20) {
			this.drawHorizontalLine(1, 6, h-1, 0xFF000000);
		}
		GlStateManager.popMatrix();
	}

	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) {
			this.sliderValue = ValueUtil.correctFloat((mouseX - (this.x + 4)) / (float) (this.width - 8), 0.0f, 1.0f);
			if (this.listener != null) {
				this.listener.mousePressed(this);
			}
			return this.dragging = true;
		}
		return false;
	}

	public void mouseReleased(int par1, int par2) {
		this.dragging = false;
		if (this.listener != null) { this.listener.mouseReleased(this); }
	}

	public void setString(String str) {
		try {
			float f = Math.round(Float.parseFloat(str) * 10.0d) / 10.0f;
			str = String.valueOf(f).replace(".", ",");
		} catch (Exception e) { }
		this.displayString = NoppesStringUtils.translate(str);
	}

}
