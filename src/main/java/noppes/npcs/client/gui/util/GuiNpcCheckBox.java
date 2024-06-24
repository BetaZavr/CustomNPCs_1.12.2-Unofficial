package noppes.npcs.client.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

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
	boolean labelBgEnabled;
	List<String> labels;
	boolean showShadow;
	boolean check;
	boolean centered;

	public GuiNpcCheckBox(int id, int x, int y, int width, int height, String label) {
		super(id, x, y, width, height, new TextComponentTranslation(label).getFormattedText());
		this.offsetX = 0;
		this.offsetY = 0;
		this.offsetType = -1;
		this.visible = true;

		this.check = false;
		this.showShadow = false;
		this.scale = 1.0f;
		this.centered = false;
		this.labels = Lists.<String>newArrayList();
		this.showShadow = false;
		this.textColor = CustomNpcs.LableColor.getRGB();
		this.setText(label);
	}

	public GuiNpcCheckBox(int id, int x, int y, int width, int height, String label, boolean select) {
		this(id, x, y, width, height, label);
		this.check = select;
	}

	public GuiNpcCheckBox(int id, int x, int y, String label, boolean select) {
		this(id, x, y, 120, 14, label);
		this.check = select;
	}

	public void addLine(String str) {
		this.labels.add(new TextComponentTranslation(str).getFormattedText());
	}

	private void drawBox(Minecraft mc, int mouseX, int mouseY) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.x, this.y + (this.height / 4), (float) this.id);
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
		this.drawHorizontalLine(1, 10, 0 + yC, colorDGray); // top 2
		this.drawVerticalLine(0, -1 + yC, 10 + yC, colorGray); // left 1
		this.drawVerticalLine(1, 0 + yC, 10 + yC, colorDGray); // left 2
		this.drawVerticalLine(11, -2 + yC, 11 + yC, colorWhite); // right 1
		this.drawVerticalLine(10, yC, 10 + yC, colorLGray); // right 2
		this.drawHorizontalLine(2, 9, yC, colorLGray); // bottop 1
		this.drawHorizontalLine(0, 10, 10 + yC, colorWhite); // bottop 2
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
						14 + this.width / 2 - mc.fontRenderer.getStringWidth(this.labels.get(k)) / 2,
						j + k * 10 - (this.height / 2) + yC + 4, this.textColor, this.showShadow);
			} else {
				mc.fontRenderer.drawString(this.labels.get(k), 14, j + k * 10 - (this.height / 2) + yC + 4,
						this.textColor, this.showShadow);
			}
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		this.hovered = mouseX >= this.x - 1 && mouseY >= this.y + 1 && mouseX < this.x + this.width
				&& mouseY < this.y + this.height + 2;
		this.drawBox(mc, mouseX, mouseY);
	}

	public String getText() {
		return this.fullLabel;
	}

	public boolean isSelected() {
		return this.check;
	}

	@Override
	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		if (this.hovered && this.enabled && this.visible) {
			mc.getSoundHandler()
					.playSound((ISound) PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
			this.check = !this.check;
			return true;
		}
		return false;
	}

	public void setCentered() {
		this.centered = true;
	}

	public void setColor(int textColor, boolean showShadow) {
		this.textColor = textColor;
		this.showShadow = showShadow;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setSelected(boolean select) {
		this.check = select;
	}

	public void setText(String label) {
		this.fullLabel = new TextComponentTranslation(label, new Object[] {}).getFormattedText();
		this.labels = Lists.<String>newArrayList();
		if (this.width - 13 < 5) {
			return;
		}
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(this.fullLabel,
				this.width - 13)) {
			this.addLine(s);
		}
	}

	@Override
	public int[] getCenter() {
		return new int[] { this.x + this.width / 2, this.y + this.height / 2};
	}
	
}
