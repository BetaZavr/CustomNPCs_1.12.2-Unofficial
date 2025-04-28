package noppes.npcs.client.gui;

import java.util.Calendar;
import java.util.GregorianCalendar;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;

public class SubGuiDataSend
extends SubGuiInterface
implements ITextfieldListener {

	public boolean cancelled = true;
	public int day = -1;
	public int month = -1;
	public int year = -1;
	public long time;

    public SubGuiDataSend(int id) {
		setBackground("smallbg.png");
		closeOnEsc = true;
		xSize = 176;
		ySize = 71;

		this.id = id;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) { cancelled = false; }
		GuiNpcTextField.unfocus();
		close();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		GlStateManager.scale(bgScale, bgScale, bgScale);
		mc.getTextureManager().bindTexture(background);
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		if (xSize > 256) {
			drawTexturedModalRect(0, ySize - 1, 0, 218, 250, ySize);
			drawTexturedModalRect(250, ySize - 1, 256 - (xSize - 250), 218, xSize - 250, ySize);
		} else {
			drawTexturedModalRect(0, ySize - 1, 0, 218, xSize, 4);
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void initGui() {
		super.initGui();
		Calendar cal = Calendar.getInstance();
		if (year == -1) { year = cal.get(Calendar.YEAR); }
		if (month == -1) { month = cal.get(Calendar.MONTH); }
		if (day == -1) {
			day = cal.get(Calendar.DAY_OF_MONTH);
			if (day < 5) {
				month--;
				if (month < 0) {
					month = 11;
					year--;
				}
			}
			day = 1;
		}
		if (year < 2011) { year = 2011; }
		if (year == 2011 && month < 10) { month = 10; }
		if (year == 2011 && month == 11 && day < 18) { day = 18; }
		GregorianCalendar setCal = new GregorianCalendar(year, month, day);
		time = setCal.getTimeInMillis();

		addLabel(new GuiNpcLabel(0, "gui.setdata", guiLeft + 7, guiTop + 4));

		GuiNpcTextField textField = new GuiNpcTextField(0, parent, guiLeft + 4, guiTop + 16, 54, 20, "" + day);
		textField.setMinMaxDefault(year == 2011 && month == 11 ? 18 : 1, setCal.getActualMaximum(Calendar.DAY_OF_MONTH), day);
		textField.setHoverText("hover.data.day", "" + textField.getMax());
		addTextField(textField);

		textField = new GuiNpcTextField(1, parent, guiLeft + 61, guiTop + 16, 54, 20, "" + (month + 1));
		textField.setMinMaxDefault(year == 2011 ? 11 : 1, (year == cal.get(Calendar.YEAR) ? cal.get(Calendar.MONTH) + 1 : 12), (month + 1));
		textField.setHoverText("hover.data.month", "" + textField.getMax(), new TextComponentTranslation("month." + month).getFormattedText());
		addTextField(textField);

		textField = new GuiNpcTextField(2, parent, guiLeft + 118, guiTop + 16, 54, 20, "" + year);
		textField.setMinMaxDefault(2011, cal.get(Calendar.YEAR), year);
		textField.setHoverText("hover.data.year", "" + textField.getMin(), "" + textField.getMax());
		addTextField(textField);

		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 44, 80, 20, "gui.done"));
		addButton(new GuiNpcButton(1, guiLeft + 90, guiTop + 44, 80, 20, "gui.cancel"));
	}

    @Override
	public void unFocused(IGuiNpcTextField textField) {
		switch (textField.getID()) {
			case 0: {
				day = textField.getInteger();
				break;
			}
			case 1: {
				month = textField.getInteger() - 1;
				break;
			}
			case 2: {
				year = textField.getInteger();
				break;
			}
		}
		initGui();
	}

}
