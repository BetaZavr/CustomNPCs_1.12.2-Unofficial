package noppes.npcs.client.gui;

import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiDataSend
extends SubGuiInterface
implements ITextfieldListener {
	
	public boolean cancelled;
	public int day, month, year;
	public long time;
	private GregorianCalendar setCal;
	
	public SubGuiDataSend(int id) {
		this.id = id;
		this.cancelled = true;
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.xSize = 176;
		this.ySize = 71;
		this.day = -1;
		this.month = -1;
		this.year = -1;
	}

	@Override
	public void initGui() {
		super.initGui();
		Calendar cal = Calendar.getInstance();
		if (this.year == -1) { this.year = cal.get(Calendar.YEAR); }
		if (this.month == -1) { this.month = cal.get(Calendar.MONTH); }
		if (this.day == -1) {
			this.day = cal.get(Calendar.DAY_OF_MONTH);
			if (this.day < 5) {
				this.month --;
				if (this.month < 0) {
					this.month = 11;
					this.year --;
				}
			}
			this.day = 1;
		}
		if (this.year < 2011) { this.year = 2011; }
		if (this.year == 2011 && this.month < 10) { this.month = 10; }
		if (this.year == 2011 && this.month == 11 && this.day < 18) { this.day = 18; }
		
		this.setCal = new GregorianCalendar(this.year, this.month, this.day);
		this.time = this.setCal.getTimeInMillis();
		
		this.addLabel(new GuiNpcLabel(0, "gui.setdata", this.guiLeft + 7, this.guiTop + 4));
		
		this.addTextField(new GuiNpcTextField(0, this.parent, this.guiLeft + 4, this.guiTop + 16, 54, 20, "" + this.day));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(this.year == 2011 && this.month == 11 ? 18 : 1, this.setCal.getActualMaximum(Calendar.DAY_OF_MONTH), this.day);
		
		this.addTextField(new GuiNpcTextField(1, this.parent, this.guiLeft + 61, this.guiTop + 16, 54, 20, "" + (this.month + 1)));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(this.year == 2011 ? 11 : 1, (this.year == cal.get(Calendar.YEAR) ? cal.get(Calendar.MONTH) + 1 : 12), (this.month + 1));
		
		this.addTextField(new GuiNpcTextField(2, this.parent, this.guiLeft + 118, this.guiTop + 16, 54, 20, "" + this.year));
		this.getTextField(2).setNumbersOnly();
		this.getTextField(2).setMinMaxDefault(2011, cal.get(Calendar.YEAR), this.year);
		
		
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 44, 80, 20, "gui.done"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 90, this.guiTop + 44, 80, 20, "gui.cancel"));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) { this.cancelled = false; }
		GuiNpcTextField.unfocus();
		this.close();
	}
	
	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (!textField.isInteger()) { return; }
		switch(textField.getId()) {
			case 0: {
				this.day = textField.getInteger();
				break;
			}
			case 1: {
				this.month = textField.getInteger() - 1;
				break;
			}
			case 2: {
				this.year = textField.getInteger();
				break;
			}
		}
		this.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.mc.renderEngine != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 0.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			this.mc.renderEngine.bindTexture(this.background);
			GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
			if (this.xSize > 256) {
				this.drawTexturedModalRect(0, this.ySize - 1, 0, 218, 250, this.ySize);
				this.drawTexturedModalRect(250, this.ySize - 1, 256 - (this.xSize - 250), 218, this.xSize - 250, this.ySize);
			} else {
				this.drawTexturedModalRect(0, this.ySize - 1, 0, 218, this.xSize, 4);
			}
			GlStateManager.popMatrix();
		}
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.data.day", "" + this.getTextField(0).max).getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.data.month", "" + this.getTextField(1).max, new TextComponentTranslation("month."+this.month).getFormattedText()).getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.data.year", "" + this.getTextField(2).min, "" + this.getTextField(2).max).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void save() { }

	
}
