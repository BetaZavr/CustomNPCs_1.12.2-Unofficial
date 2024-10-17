package noppes.npcs.client.gui.util;

import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
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
	private static final double step = 60;

	protected String[] display;
	private int displayValue;
	public int id;
	public int layerColor;
	public int txrX = 0;
	public int txrY = 0;
	public int txrW = 0;
	public int txrH = 0;
	public ResourceLocation texture = null;
	public String label = "";
	public boolean dropShadow;
	public boolean hasDefBack;
	public boolean hasSound;
	public boolean isPressed;
	public int textColor = CustomNpcs.MainColor.getRGB();
	public boolean isSimple = false;
	private ItemStack[] itemStacks = null;
	public ItemStack currentStack = ItemStack.EMPTY;
	public int currentStackID = -1;
	private int ticks = 0;
	private int wait = 0;

	public GuiNpcButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture) {
		this(id, x, y, width, height, "");
		display = new String[] { "" };
		displayValue = 0;
		this.texture = texture;
		txrX = textureX;
		txrY = textureY;
		isSimple = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, int val, String... display) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, new TextComponentTranslation(label).getFormattedText());
		this.displayValue = 0;
		this.id = id;
		this.layerColor = 0;
		this.dropShadow = true;
		this.hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String label, boolean enabled) {
		this(id, x, y, width, height, label);
		this.enabled = enabled;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String[] display, int val) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		this.displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, String label) {
		super(id, x, y, new TextComponentTranslation(label).getFormattedText());
		this.label = label;
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

	public GuiNpcButton simple(boolean bo) {
		this.isSimple = bo;
		return this;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		hovered = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
		int state = this.getHoverState(this.hovered);
		if (this.texture == null) {
			mc.getTextureManager().bindTexture(GuiNPCInterface.MENU_BUTTON);
			if (this.layerColor != 0) {
				GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f, (float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f, (float) (this.layerColor >> 24 & 255) / 255.0f);
			} else {
				GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			}
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			this.drawTexturedModalRect(this.x, this.y, 0, state * 20, this.width / 2, Math.min(this.height, 20));
			this.drawTexturedModalRect(this.x + this.width / 2, this.y, 200 - this.width / 2, state * 20, this.width / 2, Math.min(this.height, 20));

			if (this.height < 20 && this.height >= 6) {
				this.drawTexturedModalRect(this.x, this.y + this.height - 3, 0, 17 + state * 20, this.width / 2, 3);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - 3, 200 - this.width / 2, 17 + state * 20, this.width / 2, 3);
			}
			if (this.height > 20) {
				int h = this.height - 20;
				int j = 0;
				while (h > 0) {
					this.drawTexturedModalRect(this.x, this.y + 17 + j * 15, 0, state * 20 + 2, this.width / 2, Math.min(h, 15));
					this.drawTexturedModalRect(this.x + this.width / 2, this.y + 17 + j * 15, 200 - this.width / 2, state * 20 + 2, this.width / 2, Math.min(h, 15));
					h -= 15;
					j++;
				}
				this.drawTexturedModalRect(this.x, this.y + this.height - 3, 0, state * 20 + 17, this.width / 2, 3);
				this.drawTexturedModalRect(this.x + this.width / 2, this.y + this.height - 3, 200 - this.width / 2, state * 20 + 17, this.width / 2, 3);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			this.mouseDragged(mc, mouseX, mouseY);
		}
		if (this.texture != null) {
			if (this.hasDefBack) {
				this.drawGradientRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFF202020, 0xFF202020);
				this.drawGradientRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFFA0A0A0, 0xFFA0A0A0);
			}
			if (isSimple) {
				GlStateManager.pushMatrix();
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

				GlStateManager.translate(this.x, this.y, 0.0f);
				mc.getTextureManager().bindTexture(this.texture);

				this.drawTexturedModalRect(0, 0, txrX, txrY + state * this.height, this.width, this.height);
				GlStateManager.popMatrix();

			} else {
				boolean isPrefabricated = txrW == 0;
				int tw = isPrefabricated ? 200 : txrW;
				int th = txrH == 0 ? 20 : txrH;
				float scaleH = this.height / (float) th;
				float scaleW = isPrefabricated ? scaleH : this.width / (float) tw;
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				GlStateManager.translate(this.x / scaleW, this.y / scaleH, 0.0f);
				mc.getTextureManager().bindTexture(this.texture);
				if (this.layerColor != 0) {
					GlStateManager.color((float) (this.layerColor >> 16 & 255) / 255.0f,
							(float) (this.layerColor >> 8 & 255) / 255.0f, (float) (this.layerColor & 255) / 255.0f,
							(float) (this.layerColor >> 24 & 255) / 255.0f);
				} else {
					GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
				}
				GlStateManager.enableBlend();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

				if (isPrefabricated) {
					tw = (int) (((float) this.width / 2.0f) / scaleH);
					this.drawTexturedModalRect(0, 0, txrX, txrY + state * th, tw, th);
					this.drawTexturedModalRect(tw, 0, txrX + 200 - tw, txrY + state * th, tw, th);
				} else {
					this.drawTexturedModalRect(0, 0, txrX, txrY + state * th, tw, th);
				}
				GlStateManager.popMatrix();
			}
		}
		int color = CustomNpcs.MainColor.getRGB();
		if (packedFGColour != 0) {
			color = packedFGColour;
		} else if (!enabled) {
			color = CustomNpcs.NotEnableColor.getRGB();
		} else if (hovered) {
			color = CustomNpcs.HoverColor.getRGB();
		}
		mc.fontRenderer.drawString(displayString, x + (float) (width - mc.fontRenderer.getStringWidth(displayString)) / 2, y + (float) (height - 8) / 2, color, dropShadow);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		if (itemStacks != null && itemStacks.length != 0) {
			currentStack = itemStacks[0];
			currentStackID = 0;
			if (itemStacks.length > 1) {
				if (wait > 0) { wait --; }
				else {
					currentStackID = (int) Math.floor(((double) ticks % (step * (double) itemStacks.length - 1.0d)) / step);
					currentStack = itemStacks[currentStackID];
				}
			}
			if (currentStack != null && !currentStack.isEmpty()) {
				GlStateManager.pushMatrix();
				RenderHelper.enableGUIStandardItemLighting();
				GlStateManager.translate((float) x + (float) width / 2.0f - 8.0f, (float) y + (float) height / 2.0f - 8.0f, 0.0f);
				mc.getRenderItem().renderItemAndEffectIntoGUI(currentStack, 0, 0);
				this.drawString(mc.fontRenderer, "" + currentStack.getCount(), 16 - mc.fontRenderer.getStringWidth("" + currentStack.getCount()), 9, 0xFFFFFFFF);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}
			if (wait == 0) {
				ticks++;
				if (ticks > step * itemStacks.length) { ticks = 0; }
			}
		}
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
		if (this.isSimple) {
			int i = 0;
			if (!this.enabled) { i = 2; }
			else if (hovered) { i = Mouse.isButtonDown(0) ? 2 : 1; }
			return i;
		}
		if (hovered) {
			if (Mouse.isButtonDown(0)) { return enabled ? 2 : 5; }
			return enabled ? 1 : 4;
		}
		return enabled ? 0 : 3;
	}

	public void setStacks(ItemStack ... stacks) {
		if (itemStacks != null && stacks != null) { wait = 160; }
		itemStacks = stacks;
		currentStackID = itemStacks != null ? 0 : -1;
		ticks = 0;
	}

	public ItemStack[] getStacks() { return itemStacks; }

	public void setCurrentStackPos(int pos) {
		if (itemStacks == null || pos < 0 || pos >= itemStacks.length) { return; }
		currentStackID = pos;
		wait = 160;
		ticks = 0;
	}

}
