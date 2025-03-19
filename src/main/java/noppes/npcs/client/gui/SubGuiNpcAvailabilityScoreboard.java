package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityScoreboard;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityScoreboardData;

public class SubGuiNpcAvailabilityScoreboard
extends SubGuiInterface
implements ICustomScrollListener, ITextfieldListener {

	private final Availability availability;
	private final String chr = "" + ((char) 167);
	private final Map<String, String> dataNames = new HashMap<>();
	private final Map<String, AvailabilityScoreboardData> dataSets = new HashMap<>();
	private GuiCustomScroll scroll;
	private String select = "";

	public SubGuiNpcAvailabilityScoreboard(Availability availability) {
		setBackground("menubg.png");
		xSize = 316;
		ySize = 217;
		closeOnEsc = true;

		this.availability = availability;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0 : {
				if (select.isEmpty()) { return; }
				String obj = dataNames.get(select);
				AvailabilityScoreboardData asd = availability.scoreboards.get(obj);
				asd.scoreboardType = EnumAvailabilityScoreboard.values()[button.getValue()];
				availability.scoreboards.put(obj, asd);
				select = obj + " - " + chr + "7 (" + chr + "3" + new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase()).getFormattedText() + chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
				initGui();
				break;
			}
			case 2 : {
				availability.scoreboards.remove(dataNames.get(select));
				select = "";
				initGui();
				break;
			}
			case 3 : { // More
				save();
				initGui();
				break;
			}
			case 66 : close(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		GuiNpcLabel label = new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4);
		label.setCenter(xSize);
		addLabel(label);
		// exit
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 6, guiTop + 192, 70, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
		// data
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 6)).setSize(xSize - 12, ySize - 66);
		}
		dataNames.clear();
		dataSets.clear();
		for (String obj : availability.scoreboards.keySet()) {
			AvailabilityScoreboardData asd = availability.scoreboards.get(obj);
			String key = obj + " - " + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase())
							.getFormattedText()
					+ chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
			dataNames.put(key, obj);
			dataSets.put(key, asd);
		}
		if (!select.isEmpty() && !dataNames.containsKey(select)) {
			select = "";
		}
		scroll.setList(new ArrayList<>(dataNames.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) {
			scroll.setSelected(select);
		}
		addScroll(scroll);
		// type
		int p = 0;
		if (!select.isEmpty()) {
			p = dataSets.get(select).scoreboardType.ordinal();
		}
		button = new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.smaller", "availability.equals", "availability.bigger" }, p);
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.enum.type");
		addButton(button);
		// name
		GuiNpcTextField textField = new GuiNpcTextField(0, this, guiLeft + 59, guiTop + ySize - 46, 189, 20, !select.isEmpty() ? dataNames.get(select) : "");
		textField.setHoverText("availability.hover.scoreboard.name");
		addTextField(textField);
		// value
		textField = new GuiNpcTextField(1, this, guiLeft + 252, guiTop + ySize - 46, 36, 20, !select.isEmpty() ? "" + dataSets.get(select).scoreboardValue : "");
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
		textField.setHoverText("availability.hover.scoreboard.value");
		addTextField(textField);
		button = new GuiNpcButton(2, guiLeft + 290, guiTop + ySize - 46, 20, 20, "X");
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.remove");
		addButton(button);
		// extra
		button = new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more");
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.more");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int key) {
		if (key == 28 && getTextField(0).isFocused()) { // Enter
			getTextField(0).unFocused();
		}
		super.keyTyped(c, key);
	}

	@Override
	public void save() {
		if (select.isEmpty()) {
			return;
		}
		EnumAvailabilityScoreboard eas = EnumAvailabilityScoreboard.values()[getButton(0).getValue()];
		int value = NoppesStringUtils.parseInt(getTextField(1).getText(), 0);
		String obj = dataNames.get(select);
		availability.scoreboards.put(obj, new AvailabilityScoreboardData(eas, value));
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.isEmpty()) {
			return;
		}
		String obj = "";
		AvailabilityScoreboardData asd = null;
		int value = NoppesStringUtils.parseInt(getTextField(1).getText(), 0);
		if (!select.isEmpty()) {
			obj = dataNames.get(select);
			asd = availability.scoreboards.get(obj);
		}
		if (textfield.getID() == 0) {
			if (obj == null || obj.isEmpty() || asd == null) {
				obj = textfield.getText();
				asd = new AvailabilityScoreboardData(EnumAvailabilityScoreboard.SMALLER, value);
			} else {
				if (obj.equals(textfield.getText())) {
					return;
				}
				obj = textfield.getText();
				availability.scoreboards.remove(dataNames.get(select));
			}
		}
		else if (textfield.getID() == 1) {
			if (asd == null || asd.scoreboardValue == value) {
				return;
			}
			asd.scoreboardValue = value;
		}
		if (asd != null) {
			availability.scoreboards.put(obj, asd);
			select = obj + " - " + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation(("availability." + asd.scoreboardType).toLowerCase()).getFormattedText()
					+ chr + "7: " + chr + "9" + asd.scoreboardValue + chr + "7)";
		}
		initGui();
	}

}
