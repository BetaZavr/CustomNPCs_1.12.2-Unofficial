package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.util.ValueUtil;

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
			rect.hoverStack = component.getHoverStack();
		}
		rect.color = component.getColor();
		return rect;
	}

	protected GuiCustom parent;
	protected float scale = 1.0f;
	protected int textureX;
	protected int textureY;
	protected int width;
	protected int height;
	protected int x;
	protected int y;
	protected int color = 0xFFFFFFFF;
	protected ResourceLocation texture;
	protected final int[] offsets = new int[] { 0, 0 };
	protected String[] hoverText;
	protected IItemStack hoverStack;
	public int id;

	public CustomGuiTexturedRect(int id, String texture, int x, int y, int width, int height) {
		this(id, texture, x, y, width, height, 0, 0);
	}

	public CustomGuiTexturedRect(int idIn, String textureIn, int xIn, int yIn, int widthIn, int heightIn, int textureXIn, int textureYIn) {
		id = idIn;
		texture = new ResourceLocation(textureIn);
		x = GuiCustom.guiLeft + xIn;
		y = GuiCustom.guiTop + yIn;
		width = widthIn;
		height = heightIn;
		textureX = textureXIn;
		textureY = textureYIn;
	}

	public int getId() { return id; }

	@Override
	public int[] getPosXY() { return new int[] { x, y }; }

	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch (offsetType) {
			case 1: { // left down
				offsets[0] = 0;
				offsets[1] = (int) windowSize[1];
				break;
			}
			case 2: { // right up
				offsets[0] = (int) windowSize[0];
				offsets[1] = 0;
				break;
			}
			case 3: { // right down
				offsets[0] = (int) windowSize[0];
				offsets[1] = (int) windowSize[1];
				break;
			}
			case 4: { // center
				offsets[0] = (int) (windowSize[0] / 2.0d);
				offsets[1] = (int) (windowSize[1] / 2.0d);
				break;
			}
			default: { // left up
				offsets[0] = 0;
				offsets[1] = 0;
			}
		}
	}

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		int xIn = offsets[0] == 0 ? x : offsets[0] - x - width;
		int yIn = offsets[1] == 0 ? y : offsets[1] - y - height;
		boolean hovered = mouseX >= xIn && mouseY >= yIn && mouseX < xIn + width && mouseY < yIn + height;
		GlStateManager.pushMatrix();
		int pos = Math.min(id, 500);
		GlStateManager.translate(0, 0, pos);
		float a = (float) (color >> 24 & 255) / 255.0f;
		float r = (float) (color >> 16 & 255) / 255.0f;
		float g = (float) (color >> 8 & 255) / 255.0f;
		float b = (float) (color & 255) / 255.0f;
		GlStateManager.color(r, g, b, a);
		GlStateManager.enableBlend();
		mc.getTextureManager().bindTexture(texture);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
		bufferbuilder.pos(xIn, (yIn + height * scale), pos).tex((textureX * 0.00390625f), ((textureY + height) * 0.00390625f)).endVertex();
		bufferbuilder.pos((xIn + width * scale), (yIn + height * scale), pos).tex(((textureX + width) * 0.00390625f), ((textureY + height) * 0.00390625f)).endVertex();
		bufferbuilder.pos((xIn + width * scale), yIn, pos).tex(((textureX + width) * 0.00390625f), (textureY * 0.00390625f)).endVertex();
		bufferbuilder.pos(xIn, yIn, pos).tex((textureX * 0.00390625f), (textureY * 0.00390625f)).endVertex();
		tessellator.draw();
		if (hovered) {
			if (hoverText != null && hoverText.length > 0) { parent.hoverText = hoverText; }
			if (hoverStack != null && !hoverStack.isEmpty()) { parent.hoverStack = hoverStack.getMCItemStack(); }
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setParent(GuiCustom parentIn) { parent = parentIn; }

	@Override
	public void setPosXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public void setScale(float scaleIn) { scale = ValueUtil.correctFloat(scaleIn, 0.0f, 10.0f); }

	@Override
	public ICustomGuiComponent toComponent() {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture.toString(), x, y, width, height, textureX, textureY);
		component.setHoverText(hoverText);
		component.setScale(scale);
		return component;
	}

}
