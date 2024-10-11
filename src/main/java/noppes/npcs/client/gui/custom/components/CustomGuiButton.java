package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.api.gui.ICustomGuiComponent;
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
		}
		return btn;
	}

	int colour;
	boolean hovered;
	String[] hoverText;
	String label;
	GuiCustom parent;
	ResourceLocation texture;
	public int textureX;
	public int textureY;
	private final int[] offsets;

	public CustomGuiButton(int buttonId, String buttonText, int x, int y, int width, int height,
			CustomGuiButtonWrapper component) {
		super(buttonId, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, buttonText);
		this.colour = 16777215;
		if (component.hasTexture()) {
			this.textureX = component.getTextureX();
			this.textureY = component.getTextureY();
			this.texture = new ResourceLocation(component.getTexture());
		}
		this.label = buttonText;
		this.offsets = new int[] { 0, 0 };
	}

	public int getId() {
		return this.id;
	}

	@Override
	public int[] getPosXY() {
		return new int[] { this.x, this.y };
	}

	protected int hoverState(boolean mouseOver) {
		int i = 0;
		if (mouseOver) {
			i = 1;
		}
		return i;
	}

	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
		if (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height) {
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

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();

		int x = this.offsets[0] == 0 ? this.x : this.offsets[0] - this.x;
		int y = this.offsets[1] == 0 ? this.y : this.offsets[1] - this.y;
		this.hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.translate(x - this.x, y - this.y, this.id);

		FontRenderer fontRenderer = mc.fontRenderer;
		if (this.texture == null) {
			mc.getTextureManager().bindTexture(CustomGuiButton.BUTTON_TEXTURES);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			int i = this.getHoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, 0, 46 + i * 20, this.width / 2, this.height);
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, 46 + i * 20,
					this.width / 2, this.height);
			this.mouseDragged(mc, mouseX, mouseY);
			int j = 14737632;
			if (this.packedFGColour != 0) {
				j = this.packedFGColour;
			} else if (!this.enabled) {
				j = 10526880;
			} else if (this.hovered) {
				j = 16777120;
			}
			GlStateManager.translate(0.0, 0.0, 0.1);
			this.drawCenteredString(fontRenderer, this.displayString, this.x + this.width / 2,
					this.y + (this.height - 8) / 2, j);
        } else {
			mc.getTextureManager().bindTexture(this.texture);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			int i = this.hoverState(this.hovered);
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			this.drawTexturedModalRect(this.x, this.y, this.textureX, this.textureY + i * this.height, this.width,
					this.height);
			this.drawCenteredString(fontRenderer, this.label, this.x + this.width / 2, this.y + (this.height - 8) / 2,
					this.colour);
        }
        if (this.hovered && this.hoverText != null && this.hoverText.length > 0) {
            this.parent.hoverText = this.hoverText;
        }
        GlStateManager.popMatrix();
	}

	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	@Override
	public void setPosXY(int newX, int newY) {
		this.x = newX;
		this.y = newY;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(this.id, this.label, this.x, this.y, this.width,
				this.height, this.texture.toString(), this.textureX, this.textureY);
		component.setHoverText(this.hoverText);
		return component;
	}

}
