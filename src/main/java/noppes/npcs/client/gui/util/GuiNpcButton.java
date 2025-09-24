package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
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
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcButton extends GuiButton implements IComponentGui {

	protected static final double step = 60;

	protected final List<String> hoverText = new ArrayList<>();
	protected ItemStack[] itemStacks = null;
	protected String[] display;
	protected int displayValue = 0;
	protected int ticks = 0;
	protected int wait = 0;
	protected int txrX = 0;
	protected int txrY = 0;
	protected int txrW = 0;
	protected int txrH = 0;
	protected boolean isSimple = false;
	protected int layerColor;
	protected boolean hasDefBack;
	protected boolean hasSound;

	public ResourceLocation texture = null;
	public String label = "";
	public boolean dropShadow;
	public boolean isAnim = false;
	public ItemStack currentStack = ItemStack.EMPTY;
	public int currentStackID = -1;

	public GuiNpcButton(int id, int x, int y, int width, int height, int textureX, int textureY, ResourceLocation textureIn) {
		this(id, x, y, width, height, "");
		display = new String[] { "" };
		texture = textureIn;
		txrX = textureX;
		txrY = textureY;
		isSimple = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, int val, String... displayIn) {
		this(id, x, y, width, height, (displayIn.length == 0) ? "" : displayIn[val % displayIn.length]);
		display = displayIn;
		displayValue = ((displayIn.length == 0) ? 0 : (val % displayIn.length));
	}

	public GuiNpcButton(int idIn, int x, int y, int width, int height, String label) {
		super(idIn, x, y, width, height, label != null ? new TextComponentTranslation(label).getFormattedText() : "");
		id = idIn;
		layerColor = 0;
		dropShadow = true;
		hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String label, boolean enabledIn) {
		this(id, x, y, width, height, label);
		enabled = enabledIn;
	}

	public GuiNpcButton(int id, int x, int y, int width, int height, String[] displayIn, int val) {
		this(id, x, y, width, height, (displayIn.length == 0) ? "" : displayIn[val % displayIn.length]);
		display = displayIn;
		displayValue = ((displayIn.length == 0) ? 0 : (val % displayIn.length));
	}

	public GuiNpcButton(int idIn, int x, int y, String labelIn) {
		super(idIn, x, y, labelIn != null ? new TextComponentTranslation(labelIn).getFormattedText() : "");
		label = labelIn != null ? labelIn : "";
		id = idIn;
		layerColor = 0;
		dropShadow = true;
		hasSound = true;
	}

	public GuiNpcButton(int id, int x, int y, String[] displayIn, int val) {
		this(id, x, y, 200, 20, displayIn, val);
	}

	public static void renderString(@Nonnull FontRenderer font, @Nonnull String message, int left, int top, int right, int bottom, int color, boolean showShadow, boolean centered) {
		//message += " add Test Text manu texts";
		int textWidth = font.getStringWidth(message);
		int height = (top + bottom - 9) / 2 + 1;
		int width = right - left;
		if (textWidth > width) { // moved
			int centerX = textWidth - width;
			double d0 = (double) System.currentTimeMillis() / 1000.0;
			double d1 = Math.max((double) centerX * 0.5, 3.0);
			double d2 = Math.sin(Math.PI / 2.0d * Math.cos(Math.PI * 2.0d * d0 / d1)) / 2.0 + 0.5;
			double d3 = 0.0 + d2 * (centerX - 0.0);
			GlStateManager.pushMatrix();
			GL11.glEnable(GL11.GL_SCISSOR_TEST);
			Minecraft mc = Minecraft.getMinecraft();
			ScaledResolution sw = new ScaledResolution(mc);

			int i = mc.displayHeight;
			double d4 = sw.getScaledWidth() < mc.displayWidth
					? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth())
					: 1;
			double d5 = (double) left * d4;
			double d6 = (double)i - (double) bottom * d4;
			double d7 = (double) (right - left) * d4;
			double d8 = (double) (bottom - top) * d4;

			GL11.glScissor((int)d5, (int)d6, Math.max(0, (int)d7), Math.max(0, (int)d8));
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			font.drawString(message, left - (int) d3, height, color, showShadow);
			GL11.glDisable(GL11.GL_SCISSOR_TEST);
			GlStateManager.popMatrix();
		}
		else {
			if (centered) {
				width = (left + right) / 2;
				font.drawString(message, width - (float) textWidth / 2.0f, height, color, showShadow);
			} else {
				font.drawString(message, left, height, color, showShadow);
			}
		}
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		hovered = (mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height);
		drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
		if (hovered && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) { return false; }

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		int state = getHoverState(hovered);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		GlStateManager.enableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.disableLighting();
		if (layerColor != 0) {
			GlStateManager.color((float) (layerColor >> 16 & 255) / 255.0f,
					(float) (layerColor >> 8 & 255) / 255.0f,
					(float) (layerColor & 255) / 255.0f,
					(float) (layerColor >> 24 & 255) / 255.0f);
		}
		if (texture == null) {
			mc.getTextureManager().bindTexture(GuiNPCInterface.MENU_BUTTON);
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
		else {
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
			}
			else {
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
		if (packedFGColour != 0) { color = packedFGColour; }
		else if (!enabled) { color = CustomNpcs.NotEnableColor.getRGB(); }
		else if (hovered) { color = CustomNpcs.HoverColor.getRGB(); }
		renderString(mc.fontRenderer, getDisplayString(), x + 2, y, x + width - 2, y + height, color, false, true);
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
				mc.getRenderItem().renderItemOverlays(mc.fontRenderer, currentStack, 0, 0);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.popMatrix();
			}
			if (wait == 0) {
				ticks++;
				if (ticks > step * itemStacks.length) { ticks = 0; }
			}
		}
	}

	public int getValue() {
		return displayValue;
	}

	public String[] getVariants() { return display; }

	public String getDisplayString() { return displayString; }

	public int getCurrentStackID() { return currentStackID; }

	public ItemStack getCurrentStack() { return currentStack; }

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (visible && enabled && hovered) {
			if (display != null && display.length != 0) {
				displayValue = (displayValue + 1) % display.length;
				setDisplayText(display[displayValue]);
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) { return false; }

	@Override
	public void playPressSound(@Nonnull SoundHandler soundHandlerIn) {
		if (hasSound) { super.playPressSound(soundHandlerIn); }
	}

	public void resetDisplay(List<String> list) {
		display = list.toArray(new String[0]);
		if (displayValue >= list.size()) { displayValue = list.size() - 1; }
		if (list.isEmpty()) {
			displayValue = 0;
			displayString = "";
		}
		else { setDisplayText(display[displayValue]); }
	}

	public void setDisplay(int value) {
		if (display.length == 0) { return; }
		if (value < 0) { value = 0; }
		if (value >= display.length) { value = display.length; }
		displayValue = value;
		setDisplayText(display[displayValue]);
	}

	public ItemStack[] getStacks() { return itemStacks; }

	public void setCurrentStackPos(int pos) {
		if (itemStacks == null || pos < 0 || pos >= itemStacks.length) { return; }
		currentStackID = pos;
		wait = 160;
		ticks = 0;
	}

	public GuiNpcButton setUV(int u, int v, int width, int height) {
		txrX = u;
		txrY = v;
		txrW = width;
		txrH = height;
		return this;
	}

	public GuiNpcButton simple(boolean bo) { isSimple = bo; return this; }

	public GuiNpcButton setLayerColor(int color) { layerColor = color; return this; }

	public GuiNpcButton setHasSound(boolean bo) { hasSound = bo; return this; }

	public GuiNpcButton setDisplayText(String text) { displayString = new TextComponentTranslation(text).getFormattedText(); return this; }

	public GuiNpcButton setTexture(ResourceLocation location) { texture = location; return this; }

	public GuiNpcButton setHasDefaultBack(boolean bo) { hasDefBack = bo; return this; }

	public GuiNpcButton setIsAnim(boolean bo) {
		isAnim = bo;
		isSimple = !bo;
		return this;
	}

	public GuiNpcButton setTextColor(int color) { packedFGColour = color; return this; }

	public GuiNpcButton setStacks(ItemStack... stacks) {
		if (itemStacks != null && stacks != null) { wait = 160; }
		itemStacks = stacks;
		currentStackID = itemStacks != null ? 0 : -1;
		ticks = 0;
		return this;
	}

	@Override
	public GuiNpcButton setIsEnable(boolean isEnabled) { enabled = isEnabled; return this; }

	@Override
	public GuiNpcButton setIsVisible(boolean isVisible) { visible = isVisible; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		x += addX;
		y += addY;
	}

	@Override
	public void updateCnpcsScreen() { }

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
			return (enabled ? 1 : 4) + (Mouse.isButtonDown(0) ? 1 : 0);
		}
		return enabled ? 0 : 3;
	}

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() {
		return new int[] { x + width / 2, y + height / 2};
	}

	@Override
	public GuiNpcButton setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return this; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
		return this;
	}

	public GuiNpcButton setHoverText(List<String> hovers) {
		hoverText.clear();
		if (hovers == null || hovers.isEmpty()) { return this; }
		hoverText.addAll(hovers);
		return this;
	}

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public boolean isHovered() { return hovered; }

	public GuiNpcButton setWH(int widthIn, int heightIn) {
		width = widthIn;
		height = heightIn;
		return this;
	}

	public GuiNpcButton setXY(int xIn, int yIn) {
		x = xIn;
		y = yIn;
		return this;
	}

	public GuiNpcButton setDropShadow(boolean isDropShadow) {
		dropShadow = isDropShadow;
		return this;
	}

	public int[] getTextureXY() {
		return new int[] { txrX, txrY };
	}

	public void setTextureXY(int x, int y) {
		txrX = x;
		txrY = y;
	}

}
