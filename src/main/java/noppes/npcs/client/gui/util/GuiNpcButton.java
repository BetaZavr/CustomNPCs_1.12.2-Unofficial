package noppes.npcs.client.gui.util;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;

public class GuiNpcButton
extends GuiButton
implements IComponentGui {

	@Override
	public int[] getCenter() {
		return new int[] { this.x + this.width / 2, this.y + this.height / 2};
	}
	
	protected String[] display;
	private int displayValue;
	public int id;
	public int layerColor, txrX = 0, txrY = 0, txrW = 0, txrH = 0;
	public ResourceLocation texture = null;
	public String lable = "";
	public boolean dropShadow, hasDefBack, hasSound, isPressed;
	public int textColor = CustomNpcs.MainColor.getRGB();

	public GuiNpcButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture) {
		this(id, x, y, width, height, "");
		this.display = new String[] { "" };
		this.displayValue = 0;
		this.texture = texture;
		this.txrX = textureX;
		this.txrY = textureY;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, int val, String... display) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String lable) {
		super(id, x, y, width, height, new TextComponentTranslation(lable).getFormattedText());
		this.displayValue = 0;
		this.id = id;
		this.layerColor = 0;
		this.dropShadow = true;
		this.hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String lable, boolean enabled) {
		this(id, x, y, width, height, lable);
		this.enabled = enabled;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String[] display, int val) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, String lable) {
		super(id, x, y, new TextComponentTranslation(lable).getFormattedText());
		this.lable = lable;
		this.displayValue = 0;
		this.layerColor = 0;
		this.id = id;
		this.dropShadow = true;
		this.hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, String[] display, int val) {
		this(id, x, y, display[val]);
		this.display = display;
		this.displayValue = val;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		if (this.texture == null) {
			mc.renderEngine.bindTexture(GuiNPCInterface.MENU_BUTTON);
			if (this.layerColor != 0) {
				GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f, (float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f, (float) (this.layerColor >> 24 & 255) / 255.0f);
			} else {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
			int i = this.getHoverState(this.hovered);

			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(this.x, this.y, 0, i * 20, this.width / 2, Math.min(this.height, 20));
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, i * 20, this.width / 2, Math.min(this.height, 20));

			if (this.height < 20 && this.height >= 6) {
				this.drawTexturedModalRect(this.x, this.y + this.height - 3, 0, 17 + i * 20, this.width / 2, 3);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - 3, 200 - this.width / 2, 17 + i * 20, this.width / 2, 3);
			}
			if (this.height > 20) {
				int h = this.height - 20;
				int j = 0;
				while (h > 0) {
					this.drawTexturedModalRect(this.x, this.y + 17 + j * 15, 0, i * 20 + 2, this.width / 2, Math.min(h, 15));
					this.drawTexturedModalRect(this.x + this.width / 2, this.y + 17 + j * 15, 200 - this.width / 2, i * 20 + 2, this.width / 2, Math.min(h, 15));
					h -= 15;
					j++;
				}
				this.drawTexturedModalRect(this.x, this.y + this.height - 3, 0, i * 20 + 17, this.width / 2, 3);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - 3, 200 - this.width / 2, i * 20 + 17, this.width / 2, 3);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(mc, mouseX, mouseY);
		}
		if (this.texture != null) {
			if (this.hasDefBack) {
				this.drawGradientRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFF202020, 0xFF202020);
				this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFFA0A0A0, 0xFFA0A0A0);
			}
			int i = !this.enabled ? 1 : this.hovered ? 2 : 0;
			this.hovered = (mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height);
			if (this.isPressed) {
				this.isPressed = Mouse.isButtonDown(0) && this.enabled && this.hovered;
			} else if (Mouse.getEventButtonState() && Mouse.isButtonDown(0) && this.enabled && this.hovered) {
				this.isPressed = true;
			}
			if (this.isPressed) {
				i = 3;
			}
			boolean isPrefabricated = txrW == 0;
			int tw = isPrefabricated ? 200 : txrW;
			int th = txrH == 0 ? 20 : txrH;
			float scaleH = this.height / (float) th;
			float scaleW = isPrefabricated ? scaleH : this.width / (float) tw;
			GlStateManager.pushMatrix();
			GlStateManager.scale(scaleW, scaleH, 1.0f);
			GlStateManager.translate(this.x / scaleW, this.y / scaleH, 0.0f);
			mc.renderEngine.bindTexture(this.texture);
			if (this.layerColor != 0) {
				GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f,
						(float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f,
						(float) (this.layerColor >> 24 & 255) / 255.0f);
			} else {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
			if (isPrefabricated) {
				tw = (int) (((float) this.width / 2.0f) / scaleH);
				this.drawTexturedModalRect(0, 0, txrX, txrY + i * th, tw, th);
				this.drawTexturedModalRect(tw, 0, txrX + 200 - tw, txrY + i * th, tw, th);
			} else {
				this.drawTexturedModalRect(0, 0, txrX, txrY + i * th, tw, th);
			}

			GlStateManager.popMatrix();

		}
		int l = textColor;
		if (this.packedFGColour != 0) {
			l = this.packedFGColour;
		} else if (!this.enabled) {
			l = CustomNpcs.NotEnableColor.getRGB();
		} else if (this.hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}
		mc.fontRenderer.drawString(this.displayString,
				this.x + (float) (this.width - mc.fontRenderer.getStringWidth(this.displayString)) / 2,
				this.y + (float) (this.height - 8) / 2, l, this.dropShadow);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public int getHeight() {
		return this.height;
	}

	public int getValue() {
		return this.displayValue;
	}

	public String[] getVariants() {
		return this.display;
	}

	public boolean getVisible() {
		return this.visible;
	}

	public int getWidth() {
		return this.width;
	}

	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		boolean bo = super.mousePressed(mc, mouseX, mouseY);
		if (bo && this.display != null && this.display.length != 0) {
			this.displayValue = (this.displayValue + 1) % this.display.length;
			this.setDisplayText(this.display[this.displayValue]);
		}
		return bo;
	}

	public void playPressSound(@Nonnull SoundHandler soundHandlerIn) {
		if (this.hasSound) {
			super.playPressSound(soundHandlerIn);
		}
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

	@Override
	protected int getHoverState(boolean hovered) {
		if (hovered) {
			if (Mouse.isButtonDown(0)) { return this.enabled ? 2 : 5; }
			return this.enabled ? 1 : 4;
		}
		return this.enabled ? 0 : 3;
	}

}
