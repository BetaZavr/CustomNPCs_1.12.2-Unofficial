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

public class GuiNpcCheckBox
extends GuiNpcButton
implements IComponentGui {

	public int id;
	int offsetX;
	int offsetY;
	int offsetType;

	String fullLabel;
	int textColor;
	float scale;
	List<String> labels;
	boolean showShadow;
	boolean check;
	boolean centered;

	public String trueLabel;
	public String falseLabel;

	public GuiNpcCheckBox(int id, int x, int y, int width, int height, String trueLabel, String falseLabel) {
		super(id, x, y, width, height, "");
		offsetX = 0;
		offsetY = 0;
		offsetType = -1;
		visible = true;

		if (trueLabel == null) { trueLabel = ""; }
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel == null || falseLabel.isEmpty() ? trueLabel : falseLabel;
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
	}

	public GuiNpcCheckBox(int id, int x, int y, String trueLabel, String falseLabel, boolean select) {
		this(id, x, y, 120, 14, trueLabel, falseLabel);
		check = select;
	}

	public void addLine(String str) {
		this.labels.add(new TextComponentTranslation(str).getFormattedText());
	}

	private void drawBox(Minecraft mc) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.x, this.y + ((float) this.height / 4), (float) this.id);
		int colorBlack = 0xFF000000;
		int colorWhite = 0xFFFFFFFF;
		int colorDGray = 0xFF404040;
		int colorGray = 0xFF808080;
		int colorLGray = 0xD4D0C8;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (this.hovered && this.enabled) {
			Gui.drawRect(-1, -2, this.width, this.height - 2, 0x200000FF);
		}
		int yC = this.height / 2 - 7;
		this.drawHorizontalLine(0, 11, -1 + yC, colorGray); // top 1
		this.drawHorizontalLine(1, 10, yC, colorDGray); // top 2
		this.drawVerticalLine(0, -1 + yC, 10 + yC, colorGray); // left 1
		this.drawVerticalLine(1, yC, 10 + yC, colorDGray); // left 2
		this.drawVerticalLine(11, -2 + yC, 11 + yC, colorWhite); // right 1
		this.drawVerticalLine(10, yC, 10 + yC, colorLGray); // right 2
		this.drawHorizontalLine(2, 9, yC, colorLGray); // bottom 1
		this.drawHorizontalLine(0, 10, 10 + yC, colorWhite); // bottom 2
		Gui.drawRect(2, 1 + yC, 10, 9 + yC, colorWhite); // work
		if (!this.enabled) {
			Gui.drawRect(-1, -2, this.width, this.height - 2, 0x40000000);
			colorBlack = 0xFF606060;
		}
		if (this.check) {
			this.drawVerticalLine(3, 2 + yC, 6 + yC, colorBlack); // left 1
			this.drawVerticalLine(4, 3 + yC, 7 + yC, colorBlack); // left 2
			this.drawVerticalLine(5, 4 + yC, 8 + yC, colorBlack); // center
			this.drawVerticalLine(6, 3 + yC, 7 + yC, colorBlack); // right 3
			this.drawVerticalLine(7, 2 + yC, 6 + yC, colorBlack); // right 2
			this.drawVerticalLine(8, 1 + yC, 5 + yC, colorBlack); // right 1
		}
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		int i = 2 + this.height / 2;
		int j = i - this.labels.size() * 10 / 2;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		for (int k = 0; k < this.labels.size(); ++k) {
			if (this.centered) {
				mc.fontRenderer.drawString(this.labels.get(k),
						14 + (float) this.width / 2 - (float) mc.fontRenderer.getStringWidth(this.labels.get(k)) / 2,
						j + k * 10 - ((float) this.height / 2) + yC + 4, this.textColor, this.showShadow);
			} else {
				mc.fontRenderer.drawString(this.labels.get(k), 14, j + k * 10 - (float) (this.height / 2) + yC + 4, this.textColor, this.showShadow);
			}
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		this.hovered = mouseX >= this.x - 1 && mouseY >= this.y + 1 && mouseX < this.x + this.width
				&& mouseY < this.y + this.height + 2;
		this.drawBox(mc);
	}

	public String getText() {
		return this.fullLabel;
	}

	public boolean isSelected() {
		return this.check;
	}

	@Override
	public boolean mousePressed(@Nonnull Minecraft mc, int mouseX, int mouseY) {
		if (hovered && enabled && visible) {
			mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			check = !check;
			setText();
			return true;
		}
		return false;
	}

	public void setColor(int textColor, boolean showShadow) {
		this.textColor = textColor;
		this.showShadow = showShadow;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setSelected(boolean select) {
		check = select;
		setText();
	}

	private void setText() {
		fullLabel = new TextComponentTranslation(check ? trueLabel : falseLabel).getFormattedText();
		labels = new ArrayList<>();
		if (width - 13 < 5) {
			return;
		}
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(fullLabel, width - 13)) {
			addLine(s);
		}
	}

	public void setText(String trueLabel, String falseLabel) {
		if (trueLabel == null) { trueLabel = ""; }
		this.trueLabel = trueLabel;
		this.falseLabel = falseLabel == null || falseLabel.isEmpty() ? trueLabel : falseLabel;
		setText();
	}

	@Override
	public int[] getCenter() {
		return new int[] { this.x + this.width / 2, this.y + this.height / 2};
	}
	
}
