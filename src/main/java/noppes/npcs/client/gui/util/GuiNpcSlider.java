package noppes.npcs.client.gui.util;

import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcSlider
extends GuiButton
implements IComponentGui, IGuiNpcSlider {

	public boolean dragging;
	public boolean isVertical;
	public int id;
	private ISliderListener listener;
	public float sliderValue;
	private final List<String> hoverText = new ArrayList<>();

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		if (listener != null) {
			listener.mouseDragged(this);
		}
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, int width, int height, float sliderValue) {
		this(parent, id, xPos, yPos, "", sliderValue);
		this.width = width;
		this.height = height;
		isVertical = height > width;
		if (listener != null) {
			listener.mouseDragged(this);
		}
	}

	public GuiNpcSlider(GuiScreen parent, int id, int xPos, int yPos, String displayString, float sliderValue) {
		super(id, xPos, yPos, 150, 20, NoppesStringUtils.translate(displayString));
        this.id = id;
		this.sliderValue = sliderValue;
		if (parent instanceof ISliderListener) {
			listener = (ISliderListener) parent;
		}
		visible = true;
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
		if (hovered && !gui.hasSubGui() && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) {
			return;
		}
		mc.getTextureManager().bindTexture(BUTTON_TEXTURES);
		GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, 0.0f);
		// place:
		int w = width, h = height;
		if (isVertical) {
			w = height;
			h = width;
		}
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered) {
			int dWheel = Mouse.getDWheel();
			if (dWheel != 0) {
				float f = (dWheel < 0 ? -1.0f : 1.0f) / width;
				float t = sliderValue + f;
				if (t < 0) { t += 1.0f; }
				else if (t > 1.0f) { t -= 1.0f; }
				sliderValue = ValueUtil.correctFloat(t, 0.0f, 1.0f);
				if (listener != null) {
					listener.mouseDragged(this);
				}
			}
		}
		if (isVertical) {
			GlStateManager.translate(h, 0.0f, 0.0f);
			GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
		}
		drawTexturedModalRect(0, 0, 0, 46, w / 2, h);
		drawTexturedModalRect(w / 2, 0, 200 - w / 2, 46, w / 2, h);
		if (h < 20) { drawHorizontalLine(0, w - 2, h - 1, 0xFF000000); }
		GlStateManager.popMatrix();
		// button:
		mouseDragged(mc, mouseX, mouseY);
		// text:
		if (displayString.isEmpty()) { return; }

		int l = CustomNpcs.MainColor.getRGB();
		if (packedFGColour != 0) {
			l = packedFGColour;
		} else if (!enabled) {
			l = CustomNpcs.NotEnableColor.getRGB();
		} else if (hovered) {
			l = CustomNpcs.HoverColor.getRGB();
		}
		drawCenteredString(mc.fontRenderer, displayString, x + width / 2, y + 2 + (height - ClientProxy.Font.height(displayString)) / 2, l);
	}

	@Override
	public String getDisplayString() {
		return displayString;
	}

	public int getHoverState(boolean par1) {
		return 0;
	}

	public void mouseDragged(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		if (!visible) {
			return;
		}
		mc.getTextureManager().bindTexture(GuiNpcSlider.BUTTON_TEXTURES);
		int w = width, h = height;
		if (isVertical) {
			w = height;
			h = width;
		}
		if (dragging) {
			if (isVertical) {
				sliderValue = ValueUtil.correctFloat((mouseY - (y + 4)) / (float) (w - 8), 0.0f, 1.0f);
			} else {
				sliderValue = ValueUtil.correctFloat((mouseX - (x + 4)) / (float) (w - 8), 0.0f, 1.0f);
			}
			if (listener != null) {
				listener.mouseDragged(this);
			}
			if (!Mouse.isButtonDown(0)) {
				mouseReleased(0, 0);
			}
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int xPos = x + (int) (sliderValue * (w - 8)), yPos = y;
		GlStateManager.pushMatrix();
		if (isVertical) {
			xPos = x;
			yPos = y + (int) (sliderValue * (w - 8));
			GlStateManager.translate(xPos, yPos, 0.0f);
			GlStateManager.translate(h, 0.0f, 0.0f);
			GlStateManager.rotate(90.0f, 0.0f, 0.0f, 1.0f);
		} else {
			GlStateManager.translate(xPos, yPos, 0.0f);
		}
		drawTexturedModalRect(0, 0, 0, 66, 4, h);
		drawTexturedModalRect(4, 0, 196, 66, 4, h);
		if (h < 20) {
			drawHorizontalLine(1, 6, h - 1, 0xFF000000);
		}
		GlStateManager.popMatrix();
	}

	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		if (enabled && visible && hovered) {
			sliderValue = ValueUtil.correctFloat((mouseX - (x + 4)) / (float) (width - 8), 0.0f, 1.0f);
			if (listener != null) {
				listener.mousePressed(this);
			}
			return dragging = true;
		}
		return false;
	}

	public void mouseReleased(int par1, int par2) {
		dragging = false;
		if (listener != null) {
			listener.mouseReleased(this);
		}
	}

	@Override
	public void setString(String str) {
		try {
			float f = Math.round(Double.parseDouble(str.replace(",", ".")) * 10.0d) / 10.0f;
			str = String.valueOf(f).replace(".", ",");
		}
		catch (Exception ignored) { }
		displayString = NoppesStringUtils.translate(str);
	}

	@Override
	public void setDisplayString(String newDisplayString) { displayString = newDisplayString; }

	@Override
	public float getSliderValue() { return sliderValue; }

	@Override
	public void setSliderValue(float value) { sliderValue = ValueUtil.correctFloat(value, 0.0f, 1.0f); }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

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

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mousePressed(Minecraft.getMinecraft(), mouseX, mouseY); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { mouseReleased(mouseX, mouseY); }

	@Override
	public boolean isVisible() { return visible; }

	@Override
	public void setIsVisible(boolean bo) { visible = bo; }

	@Override
	public boolean isEnabled() { return enabled; }

	@Override
	public void setEnabled(boolean bo) { enabled = bo; }

	@Override
	public boolean isHovered() { return isMouseOver(); }

}
