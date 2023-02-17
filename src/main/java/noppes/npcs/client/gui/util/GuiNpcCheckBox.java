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

public class GuiNpcCheckBox
extends GuiNpcButton
{
	
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
        this.textColor = 0xFF404040;
        this.setText(label);
    }
    
    public void setScale(float scale) { this.scale = scale; }
	
    public void addLine(String str) { this.labels.add(new TextComponentTranslation(str).getFormattedText()); }

    public boolean isSelected() { return this.check; }

    public void setSelected(boolean select) { this.check = select; }
    
    public String getText() { return this.fullLabel; }
    
    public void setText(String label) {
        this.fullLabel = new TextComponentTranslation(label, new Object[] {}).getFormattedText();
        this.labels = Lists.<String>newArrayList();
        if (this.width-13 < 5) { return; }
        for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(this.fullLabel, this.width-13)) {
            this.addLine(s);
        }
    }

	public void setColor(int textColor, boolean showShadow) {
		this.textColor = textColor;
		this.showShadow = showShadow;
	}
	
    public void setCentered() {
        this.centered = true;
    }
	
	private void drawBox(Minecraft mc, int mouseX, int mouseY) {
        GlStateManager.pushMatrix();
        GlStateManager.translate(this.x, this.y+(this.height/4), (float)this.id);
		int colorBlack = 0xFF000000;
		int colorWhite = 0xFFFFFFFF;
		int colorDGray = 0xFF404040;
		int colorGray = 0xFF808080;
		int colorLGray = 0xD4D0C8;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawHorizontalLine(0, 11, 0, colorGray); // top 1
		this.drawHorizontalLine(1, 10, 1, colorDGray); // top 2
		this.drawVerticalLine(0, 0, 11, colorGray); // left 1
		this.drawVerticalLine(1, 1, 11, colorDGray); // left 2
		this.drawVerticalLine(11, -1, 12, colorWhite); // right 1
		this.drawVerticalLine(10, 1, 11, colorLGray); // right 2
		this.drawHorizontalLine(2, 9, 10, colorLGray); // bottop 1
		this.drawHorizontalLine(0, 10, 11, colorWhite); // bottop 2
		Gui.drawRect(2, 2, 10, 10, colorWhite); // work
		if (this.check) {
			this.drawVerticalLine(3, 3, 7, colorBlack); // left 1
			this.drawVerticalLine(4, 4, 8, colorBlack); // left 2
			this.drawVerticalLine(5, 5, 9, colorBlack); // center
			this.drawVerticalLine(6, 4, 8, colorBlack); // right 3
			this.drawVerticalLine(7, 3, 7, colorBlack); // right 2
			this.drawVerticalLine(8, 2, 6, colorBlack); // right 1
		}
		GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        int i = 2 + this.height / 2;
        int j = i - this.labels.size() * 10 / 2;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        for (int k = 0; k < this.labels.size(); ++k) {
            if (this.centered) { mc.fontRenderer.drawString(this.labels.get(k), 14 + this.width / 2 - mc.fontRenderer.getStringWidth(this.labels.get(k)) / 2, j + k * 10 - (this.height/4), this.textColor, this.showShadow); }
            else { mc.fontRenderer.drawString(this.labels.get(k), 14, j + k * 10 - (this.height/4), this.textColor, this.showShadow); }
        }
        GlStateManager.popMatrix();
	}
    
    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        if (this.hovered && this.enabled && this.visible) {
        	mc.getSoundHandler().playSound((ISound)PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
        	this.check = !this.check;
            return true;
        }
        return false;
    }

    @Override
	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
    	if (!this.visible) { return; }
    	this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        this.drawBox(mc, mouseX, mouseY);
	}
    
}
