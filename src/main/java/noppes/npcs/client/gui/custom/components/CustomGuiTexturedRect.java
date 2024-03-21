package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;

public class CustomGuiTexturedRect extends Gui implements IGuiComponent {
	
	public static CustomGuiTexturedRect fromComponent(CustomGuiTexturedRectWrapper component) {
		CustomGuiTexturedRect rect;
		if (component.getTextureX() >= 0 && component.getTextureY() >= 0) {
			rect = new CustomGuiTexturedRect(component.getId(), component.getTexture(), component.getPosX(),
					component.getPosY(), component.getWidth(), component.getHeight(), component.getTextureX(),
					component.getTextureY());
		} else {
			rect = new CustomGuiTexturedRect(component.getId(), component.getTexture(), component.getPosX(),
					component.getPosY(), component.getWidth(), component.getHeight());
		}
		rect.scale = component.getScale();
		if (component.hasHoverText()) {
			rect.hoverText = component.getHoverText();
		}
		rect.color = component.getColor();
		return rect;
	}

	GuiCustom parent;
	float scale;
	public int id, textureX, textureY, width, height, x, y, color;
	ResourceLocation texture;
    private final int[] offsets;
	String[] hoverText;

	public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
		this(id, texture, x, y, width, height, 0, 0);
	}

	public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
		this.scale = 1.0f;
		this.id = id;
		this.texture = new ResourceLocation(texture);
		this.x = GuiCustom.guiLeft + x;
		this.y = GuiCustom.guiTop + y;
		this.width = width;
		this.height = height;
		this.textureX = textureX;
		this.textureY = textureY;
        this.offsets = new int [] { 0, 0 };
        this.color = 0xFFFFFFFF;
	}

	public int getId() { return this.id; }

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		int x = this.offsets[0] == 0 ? this.x : this.offsets[0] - this.x - this.width;
		int y = this.offsets[1] == 0 ? this.y : this.offsets[1] - this.y - this.height;
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.pushMatrix();
		int pos = this.id < 500 ? this.id : 500;
		GlStateManager.translate(0, 0, pos);
		float a = (float) (this.color >> 24 & 255) / 255.0f;
		float r = (float) (this.color >> 16 & 255) / 255.0f;
		float g = (float) (this.color >> 8 & 255) / 255.0f;
		float b = (float) (this.color & 255) / 255.0f;		
		GlStateManager.color(r, g, b, a);
		GlStateManager.enableBlend();
		mc.renderEngine.bindTexture(this.texture);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(x, (y + this.height * this.scale), pos).tex(((this.textureX + 0) * 0.00390625f), ((this.textureY + this.height) * 0.00390625f)).endVertex();
		bufferbuilder.pos((x + this.width * this.scale), (y + this.height * this.scale), pos).tex(((this.textureX + this.width) * 0.00390625f), ((this.textureY + this.height) * 0.00390625f)).endVertex();
		bufferbuilder.pos((x + this.width * this.scale), y, pos).tex(((this.textureX + this.width) * 0.00390625f), ((this.textureY + 0) * 0.00390625f)).endVertex();
		bufferbuilder.pos(x, y, pos).tex(((this.textureX + 0) * 0.00390625f), ((this.textureY + 0) * 0.00390625f)).endVertex();
		tessellator.draw();
		if (hovered && this.hoverText != null && this.hoverText.length > 0) {
			this.parent.hoverText = this.hoverText;
		}
		GlStateManager.popMatrix();
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(this.id, this.texture.toString(), this.x, this.y, this.width, this.height, this.textureX, this.textureY);
		component.setHoverText(this.hoverText);
		component.setScale(this.scale);
		return component;
	}
	
	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch(offsetType) {
			case 1: { // left down
				this.offsets[0] = 0;
				this.offsets[1] = (int) windowSize[1];
				break;
			}
			case 2: { // right up
				this.offsets[0] = (int) windowSize[0];
				this.offsets[1] = 0;
				break;
			}
			case 3: { // right down
				this.offsets[0] = (int) windowSize[0];
				this.offsets[1] = (int) windowSize[1];
				break;
			}
			default: { // left up
				this.offsets[0] = 0;
				this.offsets[1] = 0;
			}
		}
	}
	
	@Override
	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	@Override
	public int[] getPosXY() { return new int[] { this.x, this.y }; }

	@Override
	public void setPosXY(int newX, int newY) { 
		this.x = newX;
		this.y = newY;
	}
	
}
