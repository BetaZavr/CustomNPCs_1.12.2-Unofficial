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
import java.util.ArrayList;
import java.util.List;

public class GuiNpcButton
extends GuiButton
implements IComponentGui, IGuiNpcButton {

	private static final double step = 60;

	protected String[] display;
	private int displayValue = 0;
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
	public boolean isSimple = false;
	public boolean isAnim = false;
	private ItemStack[] itemStacks = null;
	public ItemStack currentStack = ItemStack.EMPTY;
	public int currentStackID = -1;
	private int ticks = 0;
	private int wait = 0;
	private final List<String> hoverText = new ArrayList<>();

	public GuiNpcButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation texture) {
		this(id, x, y, width, height, "");
		display = new String[] { "" };
		this.texture = texture;
		txrX = textureX;
		txrY = textureY;
		isSimple = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, int val, String... display) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, new TextComponentTranslation(label).getFormattedText());
		this.id = id;
		layerColor = 0;
		dropShadow = true;
		hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String label, boolean enabled) {
		this(id, x, y, width, height, label);
		this.enabled = enabled;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String[] display, int val) {
		this(id, x, y, width, height, (display.length == 0) ? "" : display[val % display.length]);
		this.display = display;
		displayValue = ((display.length == 0) ? 0 : (val % display.length));
	}

	public GuiNpcButton(int id, int x, int y, String label) {
		super(id, x, y, new TextComponentTranslation(label).getFormattedText());
		this.label = label;
		this.id = id;
		layerColor = 0;
		dropShadow = true;
		hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, String[] display, int val) {
		this(id, x, y, display[val]);
		this.display = display;
		displayValue = val;
	}

	public GuiNpcButton simple(boolean bo) {
		isSimple = bo;
		return this;
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
		if (hovered && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		hovered = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
		int state = getHoverState(hovered);
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		if (texture == null) {
			mc.getTextureManager().bindTexture(GuiNPCInterface.MENU_BUTTON);
			if (layerColor != 0) {
				GlStateManager.color((float) (layerColor >> 16 & 255) / 255.0f, (float) (layerColor >> 8 & 255) / 255.0f, (float) (layerColor & 255) / 255.0f, (float) (layerColor >> 24 & 255) / 255.0f);
			}
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
			GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

			drawTexturedModalRect(x, y, 0, state * 20, width / 2, Math.min(height, 20));
			drawTexturedModalRect(x + width / 2, y, 200 - width / 2, state * 20, width / 2, Math.min(height, 20));

			if (height < 20 && height >= 6) {
				drawTexturedModalRect(x, y + height - 3, 0, 17 + state * 20, width / 2, 3);
				drawTexturedModalRect(x + width / 2, y + height - 3, 200 - width / 2, 17 + state * 20, width / 2, 3);
			}
			if (height > 20) {
				int h = height - 20;
				int j = 0;
				while (h > 0) {
					drawTexturedModalRect(x, y + 17 + j * 15, 0, state * 20 + 2, width / 2, Math.min(h, 15));
					drawTexturedModalRect(x + width / 2, y + 17 + j * 15, 200 - width / 2, state * 20 + 2, width / 2, Math.min(h, 15));
					h -= 15;
					j++;
				}
				drawTexturedModalRect(x, y + height - 3, 0, state * 20 + 17, width / 2, 3);
				drawTexturedModalRect(x + width / 2, y + height - 3, 200 - width / 2, state * 20 + 17, width / 2, 3);
			}
			GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
			mouseDragged(mc, mouseX, mouseY);
		}
		if (texture != null) {
			if (hasDefBack) {
				drawGradientRect(x - 1, y - 1, x + width + 1, y + height + 1, 0xFF202020, 0xFF202020);
				drawGradientRect(x, y, x + width, y + height, 0xFFA0A0A0, 0xFFA0A0A0);
			}
			if (isSimple) {
				GlStateManager.pushMatrix();
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

				GlStateManager.translate(x, y, 0.0f);
				mc.getTextureManager().bindTexture(texture);

				drawTexturedModalRect(0, 0, txrX, txrY + state * height, width, height);
				GlStateManager.popMatrix();
			} else {
				boolean isPrefabricated = txrW == 0;
				int tw = isPrefabricated ? 200 : txrW;
				int th = txrH == 0 ? 20 : txrH;
				float scaleH = height / (float) th;
				float scaleW = isPrefabricated ? scaleH : width / (float) tw;
				GlStateManager.pushMatrix();
				GlStateManager.scale(scaleW, scaleH, 1.0f);
				GlStateManager.translate(x / scaleW, y / scaleH, 0.0f);
				mc.getTextureManager().bindTexture(texture);
				GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
				GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);

				if (layerColor != 0) {
					GlStateManager.color((float) (layerColor >> 16 & 255) / 255.0f, (float) (layerColor >> 8 & 255) / 255.0f, (float) (layerColor & 255) / 255.0f, (float) (layerColor >> 24 & 255) / 255.0f);
				}
				if (isPrefabricated) {
					tw = (int) (((float) width / 2.0f) / scaleH);
					drawTexturedModalRect(0, 0, txrX, txrY + state * th, tw, th);
					drawTexturedModalRect(tw, 0, txrX + 200 - tw, txrY + state * th, tw, th);
				} else {
					drawTexturedModalRect(0, 0, txrX, txrY + state * th, tw, th);
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
				drawString(mc.fontRenderer, "" + currentStack.getCount(), 16 - mc.fontRenderer.getStringWidth("" + currentStack.getCount()), 9, 0xFFFFFFFF);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}
			if (wait == 0) {
				ticks++;
				if (ticks > step * itemStacks.length) { ticks = 0; }
			}
		}
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public void customKeyTyped(char c, int id) { }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mousePressed(Minecraft.getMinecraft(), mouseX, mouseY); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { mouseReleased(mouseX, mouseY); }

	@Override
	public boolean isVisible() { return visible; }

	@Override
	public int getValue() {
		return displayValue;
	}

	@Override
	public String[] getVariants() { return display; }

	@Override
	public int[] getTextureXY() { return new int[] { txrX, txrY }; }

	@Override
	public void setTextureXY(int x, int y) {
		txrX = x;
		txrY = y;
	}

	@Override
	public int[] getTextureUV() { return new int[] { txrW, txrH }; }

	@Override
	public void setTextureUV(int u, int v) {
		txrW = u;
		txrH = v;
	}

	@Override
	public void setLayerColor(int color) { layerColor = color; }

	@Override
	public String getDisplayString() { return displayString; }

	@Override
	public void setActive(boolean bo) { }

	@Override
	public void setHasSound(boolean bo) { hasSound = bo; }

	@Override
	public boolean hasSound() { return hasSound; }

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getCurrentStackID() { return currentStackID; }

	@Override
	public ItemStack getCurrentStack() { return currentStack; }

	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		boolean bo = super.mousePressed(mc, mouseX, mouseY);
		if (bo && display != null && display.length != 0) {
			displayValue = (displayValue + 1) % display.length;
			setDisplayText(display[displayValue]);
		}
		return bo;
	}

	public void playPressSound(@Nonnull SoundHandler soundHandlerIn) {
		if (hasSound) {
			super.playPressSound(soundHandlerIn);
		}
	}

	@Override
	public void resetDisplay(List<String> list) {
		display = list.toArray(new String[0]);
		if (displayValue >= list.size()) { displayValue = list.size() - 1; }
		if (list.isEmpty()) {
			displayValue = 0;
			displayString = "";
		}
		else { setDisplayText(display[displayValue]); }
	}

	@Override
	public void setDisplay(int value) {
		if (display.length == 0) { return; }
		if (value < 0) { value = 0; }
		if (value >= display.length) { value = display.length; }
		displayValue = value;
		setDisplayText(display[displayValue]);
	}

	@Override
	public void setDisplayText(String text) {
		displayString = new TextComponentTranslation(text).getFormattedText();
	}

	@Override
	public void setTexture(ResourceLocation location) { texture = location; }

	@Override
	public void setEnabled(boolean bo) {
		enabled = bo;
	}

	@Override
	public void setHasDefaultBack(boolean bo) { hasDefBack = bo; }

	@Override
	public void setIsAnim(boolean bo) { isAnim = bo; }

	@Override
	public void setTextColor(int color) { packedFGColour = color; }

	@Override
	public void setVisible(boolean bo) { visible = bo; }

	@Override
	public boolean isEnabled() { return enabled; }

	@Override
	protected int getHoverState(boolean hovered) {
		if (isAnim) {
			if (!enabled) { return 1; }
			return hovered ? Mouse.isButtonDown(0) ? 3 : 2 : 0;
		}
		if (isSimple) {
			int i = 0;
			if (!enabled) { i = 2; }
			else if (hovered) { i = Mouse.isButtonDown(0) ? 2 : 1; }
			return i;
		}
		if (hovered) {
			return enabled ? 1 : 4;
		}
		return enabled ? 0 : 3;
	}

	@Override
	public void setStacks(ItemStack... stacks) {
		if (itemStacks != null && stacks != null) { wait = 160; }
		itemStacks = stacks;
		currentStackID = itemStacks != null ? 0 : -1;
		ticks = 0;
	}

	@Override
	public ItemStack[] getStacks() { return itemStacks; }

	@Override
	public void setCurrentStackPos(int pos) {
		if (itemStacks == null || pos < 0 || pos >= itemStacks.length) { return; }
		currentStackID = pos;
		wait = 160;
		ticks = 0;
	}

	@Override
	public int getId() { return id; }

	@Override
	public int[] getCenter() {
		return new int[] { x + width / 2, y + height / 2};
	}

	@Override
	public void setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
	}

	@Override
	public int getLeft() { return x; }

	@Override
	public int getTop() { return y; }

	@Override
	public void setLeft(int left) { x = left; }

	@Override
	public void setTop(int top) { y = top; }

}
