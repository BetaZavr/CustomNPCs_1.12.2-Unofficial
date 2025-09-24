package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiButtonWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;

public class CustomGuiButton extends GuiButton implements IClickListener {

	public static CustomGuiButton fromComponent(CustomGuiButtonWrapper component) {
		CustomGuiButton btn;
		if (component.getWidth() >= 0 && component.getHeight() >= 0) {
			btn = new CustomGuiButton(component.getId(), component.getLabel(), component.getPosX(), component.getPosY(),
					component.getWidth(), component.getHeight(), component);
		} else {
			btn = new CustomGuiButton(component.getId(), component.getLabel(), component.getPosX(), component.getPosY(),
					200, 20, component);
		}
		if (component.hasHoverText()) {
			btn.hoverText = component.getHoverText();
			btn.hoverStack = component.getHoverStack();
		}
		return btn;
	}

	protected boolean hovered;
	protected String[] hoverText;
	protected IItemStack hoverStack;
	protected String label;
	protected GuiCustom parent;
	protected ResourceLocation texture;
	protected int colour = 0xFFFFFF;
	protected int textureX;
	protected int textureY;
	protected final int[] offsets = new int[] { 0, 0 };

	public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height, CustomGuiButtonWrapper component) {
		super(buttonId, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, buttonText);
		if (component.hasTexture()) {
			textureX = component.getTextureX();
			textureY = component.getTextureY();
			texture = new ResourceLocation(component.getTexture());
		}
		label = buttonText;
	}

	public int getId() { return id; }

	@Override
	public int[] getPosXY() { return new int[] { x, y }; }

	protected int hoverState(boolean mouseOver) {
		int i = 0;
		if (mouseOver) { i = 1; }
		return i;
	}

	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
		if (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			gui.buttonClick(this);
			return true;
		}
		return false;
	}

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
		GlStateManager.pushMatrix();
		int xIn = offsets[0] == 0 ? x : offsets[0] - x;
		int yIn = offsets[1] == 0 ? y : offsets[1] - y;
		hovered = mouseX >= xIn && mouseY >= yIn && mouseX < xIn + width && mouseY < yIn + height;
		GlStateManager.translate(xIn - x, yIn - y, id);

		FontRenderer fontRenderer = mc.fontRenderer;
		if (texture == null) {
			mc.getTextureManager().bindTexture(CustomGuiButton.BUTTON_TEXTURES);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			int i = getHoverState(hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(x, y, 0, 46 + i * 20, width / 2, height);
			drawTexturedModalRect(x + width / 2, y, 200 - width / 2, 46 + i * 20, width / 2, height);
			mouseDragged(mc, mouseX, mouseY);
			int j = 0xE0E0E0;
			if (packedFGColour != 0) { j = packedFGColour; }
			else if (!enabled) { j = 0xA0A0A0; }
			else if (hovered) { j = 0xFFFFA0; }
			GlStateManager.translate(0.0, 0.0, 0.1);
			drawCenteredString(fontRenderer, displayString, x + width / 2, y + (height - 8) / 2, j);
        } else {
			mc.getTextureManager().bindTexture(texture);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			int i = hoverState(hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			drawTexturedModalRect(x, y, textureX, textureY + i * height, width, height);
			drawCenteredString(fontRenderer, label, x + width / 2,  y + (height - 8) / 2, colour);
        }
		if (hovered) {
			if (hoverText != null && hoverText.length > 0) { parent.hoverText = hoverText; }
			if (hoverStack != null && !hoverStack.isEmpty()) { parent.hoverStack = hoverStack.getMCItemStack(); }
		}
        GlStateManager.popMatrix();
	}

	public void setParent(GuiCustom parentIn) { parent = parentIn; }

	@Override
	public void setPosXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture.toString(), textureX, textureY);
		component.setHoverText(hoverText);
		return component;
	}

}
