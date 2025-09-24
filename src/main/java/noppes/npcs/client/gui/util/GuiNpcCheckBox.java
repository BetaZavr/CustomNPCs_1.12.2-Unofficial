package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;

public class GuiNpcCheckBox extends GuiNpcButton implements IComponentGui {

	protected int offsetX;
	protected int offsetY;
	protected int offsetType;
	protected String fullLabel;
	protected int textColor;
	protected float scale;
	protected List<String> labels;
	protected boolean showShadow;
	protected boolean check;
	protected boolean centered;

	public String trueLabel;
	public String falseLabel;

	public GuiNpcCheckBox(int id, int x, int y, int width, int height, String trueLabelIn, String falseLabelIn) {
		super(id, x, y, width, height, "");
		offsetX = 0;
		offsetY = 0;
		offsetType = -1;
		visible = true;

		if (trueLabelIn == null) { trueLabelIn = ""; }
		trueLabel = trueLabelIn;
		falseLabel = falseLabelIn == null || falseLabelIn.isEmpty() ? trueLabelIn : falseLabelIn;
		check = false;
        scale = 1.0f;
		centered = false;
		labels = new ArrayList<>();
		showShadow = false;
		textColor = CustomNpcs.LableColor.getRGB();
		setText();
	}

	public GuiNpcCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel, boolean select) {
		this(id, x, y, width, height, trueLabel, falseLabel);
		check = select;
		setText();
	}

	public GuiNpcCheckBox(int id, int x, int y, String trueLabel, String falseLabel, boolean select) {
		this(id, x, y, 120, 14, trueLabel, falseLabel);
		check = select;
	}

	public void addLine(String str) {
		labels.add(new TextComponentTranslation(str).getFormattedText());
	}

	private void drawBox(Minecraft mc) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y + ((float) height / 4), 0.0f);
		int colorBlack = 0xFF000000;
		int colorWhite = 0xFFFFFFFF;
		int colorDGray = 0xFF404040;
		int colorGray = 0xFF808080;
		int colorLGray = 0xD4D0C8;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (hovered && enabled) {
			Gui.drawRect(-1, -2, width, height - 2, 0x200000FF);
		}
		int yC = height / 2 - 7;
		drawHorizontalLine(0, 11, -1 + yC, colorGray); // top 1
		drawHorizontalLine(1, 10, yC, colorDGray); // top 2
		drawVerticalLine(0, -1 + yC, 10 + yC, colorGray); // left 1
		drawVerticalLine(1, yC, 10 + yC, colorDGray); // left 2
		drawVerticalLine(11, -2 + yC, 11 + yC, colorWhite); // right 1
		drawVerticalLine(10, yC, 10 + yC, colorLGray); // right 2
		drawHorizontalLine(2, 9, yC, colorLGray); // bottom 1
		drawHorizontalLine(0, 10, 10 + yC, colorWhite); // bottom 2
		Gui.drawRect(2, 1 + yC, 10, 9 + yC, colorWhite); // work
		if (!enabled) {
			Gui.drawRect(-1, -2, width, height - 2, 0x40000000);
			colorBlack = 0xFF606060;
		}
		if (check) {
			drawVerticalLine(3, 2 + yC, 6 + yC, colorBlack); // left 1
			drawVerticalLine(4, 3 + yC, 7 + yC, colorBlack); // left 2
			drawVerticalLine(5, 4 + yC, 8 + yC, colorBlack); // center
			drawVerticalLine(6, 3 + yC, 7 + yC, colorBlack); // right 3
			drawVerticalLine(7, 2 + yC, 6 + yC, colorBlack); // right 2
			drawVerticalLine(8, 1 + yC, 5 + yC, colorBlack); // right 1
		}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		int i = 2 + height / 2;
		int j = i - labels.size() * 10 / 2;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int k = 0; k < labels.size(); ++k) {
			if (centered) {
				mc.fontRenderer.drawString(labels.get(k),
						14 + (float) width / 2 - (float) mc.fontRenderer.getStringWidth(labels.get(k)) / 2,
						j + k * 10 - ((float) height / 2) + yC + 4, textColor, showShadow);
			} else {
				mc.fontRenderer.drawString(labels.get(k), 14, j + k * 10 - (float) (height / 2) + yC + 4, textColor, showShadow);
			}
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { return; }
		hovered = mouseX >= x - 1 && mouseY >= y + 1 && mouseX < x + width && mouseY < y + height + 2;
		drawBox(mc);
	}

	public String getText() { return fullLabel; }

	public boolean isSelected() {
		return check;
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (hovered && enabled && visible) {
			Minecraft.getMinecraft().getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			check = !check;
			setText();
			return true;
		}
		return false;
	}

	public GuiNpcCheckBox setColor(int newTextColor, boolean isShowShadow) {
		textColor = newTextColor;
		showShadow = isShowShadow;
		return this;
	}

	public GuiNpcCheckBox setScale(float newScale) { scale = newScale; return this; }

	public GuiNpcCheckBox setSelected(boolean select) {
		check = select;
		setText();
		return this;
	}

	private void setText() {
		fullLabel = new TextComponentTranslation(check ? trueLabel : falseLabel).getFormattedText();
		labels = new ArrayList<>();
		if (width - 13 < 5) { return; }
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(fullLabel, width - 13)) { addLine(s); }
	}

	public void setText(String trueLabelIn, String falseLabelIn) {
		if (trueLabelIn == null) { trueLabelIn = ""; }
		trueLabel = trueLabelIn;
		falseLabel = falseLabelIn == null || falseLabelIn.isEmpty() ? trueLabelIn : falseLabelIn;
		setText();
	}
	
}
