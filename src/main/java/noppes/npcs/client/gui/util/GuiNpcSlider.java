package noppes.npcs.client.gui.util;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcSlider extends GuiButton implements IComponentGui {

	protected final ISliderListener listener;
	public boolean dragging;
	public float sliderValue;

	// New from Unofficial (BetaZavr)
	public boolean isVertical;
	protected boolean showShadow = true;
	protected final List<String> hoverText = new ArrayList<>();

	public GuiNpcSlider(ISliderListener parent, int id, int xPos, int yPos, float sliderValue) {
		this(parent, id, xPos, yPos, 150, 20, "", sliderValue);
	}

	public GuiNpcSlider(ISliderListener parent, int id, int xPos, int yPos, int widthIn, int heightIn, float sliderValue) {
		this(parent, id, xPos, yPos, widthIn, heightIn, "", sliderValue);
	}

	public GuiNpcSlider(ISliderListener parent, int idIn, int xPos, int yPos, int widthIn, int heightIn, String displayString, float sliderValueIn) {
		super(idIn, xPos, yPos, widthIn, heightIn, NoppesStringUtils.translate(displayString));
        id = idIn;
		isVertical = height > width;
		sliderValue = sliderValueIn;
		listener = parent;
		visible = true;
		listener.mouseDragged(this);
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		drawButton(Minecraft.getMinecraft(), mouseX, mouseY, partialTicks);
		if (hovered && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
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
				listener.mouseDragged(this);
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
		int color = CustomNpcs.MainColor.getRGB();
		if (packedFGColour != 0) { color = packedFGColour; }
		else if (!enabled) { color = CustomNpcs.NotEnableColor.getRGB(); }
		else if (hovered) { color = CustomNpcs.HoverColor.getRGB(); }
		float xPos = (float) x + ((float) width - (float) ClientProxy.Font.height(displayString))/ 2.0f;
		float yPos = (float) y + 2.0f + ((float) height - (float) ClientProxy.Font.height(displayString)) / 2.0f;
		mc.fontRenderer.drawString(displayString, xPos, yPos, color, showShadow);
	}

	@Override
	public int getHoverState(boolean par1) { return 0; }

	@Override
	public void mouseDragged(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		if (!visible) { return; }
		mc.getTextureManager().bindTexture(GuiNpcSlider.BUTTON_TEXTURES);
		int w = width, h = height;
		if (isVertical) {
			w = height;
			h = width;
		}
		if (dragging) {
			if (isVertical) { sliderValue = ValueUtil.correctFloat((mouseY - (y + 4)) / (float) (w - 8), 0.0f, 1.0f); }
			else { sliderValue = ValueUtil.correctFloat((mouseX - (x + 4)) / (float) (w - 8), 0.0f, 1.0f); }
			listener.mouseDragged(this);
			if (!Mouse.isButtonDown(0)) { mouseReleased(0, 0); }
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
		}
		else { GlStateManager.translate(xPos, yPos, 0.0f); }
		drawTexturedModalRect(0, 0, 0, 66, 4, h);
		drawTexturedModalRect(4, 0, 196, 66, 4, h);
		if (h < 20) { drawHorizontalLine(1, 6, h - 1, 0xFF000000); }
		GlStateManager.popMatrix();
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (enabled && visible && hovered) {
			sliderValue = ValueUtil.correctFloat((mouseX - (x + 4)) / (float) (width - 8), 0.0f, 1.0f);
			listener.mousePressed(this);
			return dragging = true;
		}
		return false;
	}

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int mouseButton) {
		return hovered;
	}

	public void mouseReleased(int par1, int par2) {
		dragging = false;
		listener.mouseReleased(this);
	}

	public void setString(String str) {
		try {
			float f = Math.round(Double.parseDouble(str.replace(",", ".")) * 10.0d) / 10.0f;
			str = String.valueOf(f).replace(".", ",");
		}
		catch (Exception ignored) { }
		displayString = NoppesStringUtils.translate(str);
	}

	public void setSliderValue(float value) { sliderValue = ValueUtil.correctFloat(value, 0.0f, 1.0f); }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) { return false; }

	public GuiNpcSlider setHoverText(Object... components) {
		hoverText.clear();
		if (components == null) { return this; }
		noppes.npcs.util.Util.instance.putHovers(hoverText, components);
		return this;
	}

	@Override
	public GuiNpcSlider setIsVisible(boolean bo) { visible = bo; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		x += addX;
		y += addY;
	}

	@Override
	public void updateCnpcsScreen() { }

	@Override
	public GuiNpcSlider setIsEnable(boolean bo) { enabled = bo; return this; }

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public boolean isHovered() { return hovered; }

}
