package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityScoreboard;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityScoreboardData;

import javax.annotation.Nonnull;

public class SubGuiNpcAvailabilityScoreboard extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

	protected final Availability availability;
	protected final Map<String, String> dataNames = new HashMap<>();
	protected final Map<String, AvailabilityScoreboardData> dataSets = new HashMap<>();
	protected final String chr = "" + ((char) 167);
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityScoreboard(Availability availabilityIn) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 316;
		ySize = 217;

		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
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
			case 66 : onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		addLabel(new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4)
				.setCenter(xSize));
		// exit
		addButton(new GuiNpcButton(66, guiLeft + 6, guiTop + 192, 70, 20, "gui.done")
				.setHoverText("hover.back"));
		// data
		if (scroll == null) { scroll = new GuiCustomScroll(this, 6).setSize(xSize - 12, ySize - 66); }
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
		if (!select.isEmpty() && !dataNames.containsKey(select)) { select = ""; }
		scroll.setList(new ArrayList<>(dataNames.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) { scroll.setSelected(select); }
		addScroll(scroll);
		// type
		int p = 0;
		if (!select.isEmpty()) { p = dataSets.get(select).scoreboardType.ordinal(); }
		addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.smaller", "availability.equals", "availability.bigger" }, p)
				.setIsEnable(!select.isEmpty())
				.setHoverText("availability.hover.enum.type"));
		// name
		addTextField(new GuiNpcTextField(0, this, guiLeft + 59, guiTop + ySize - 46, 189, 20, !select.isEmpty() ? dataNames.get(select) : "")
				.setHoverText("availability.hover.scoreboard.name"));
		// value
		addTextField(new GuiNpcTextField(1, this, guiLeft + 252, guiTop + ySize - 46, 36, 20, !select.isEmpty() ? "" + dataSets.get(select).scoreboardValue : "")
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, 0)
				.setHoverText("availability.hover.scoreboard.value"));
		addButton(new GuiNpcButton(2, guiLeft + 290, guiTop + ySize - 46, 20, 20, "X")
				.setIsEnable(!select.isEmpty())
				.setHoverText("availability.hover.remove"));
		// extra
		addButton(new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more")
				.setIsEnable(!select.isEmpty())
				.setHoverText("availability.hover.more"));
	}

	@Override
	public void save() {
		if (select.isEmpty()) { return; }
		EnumAvailabilityScoreboard eas = EnumAvailabilityScoreboard.values()[getButton(0).getValue()];
		int value = NoppesStringUtils.parseInt(getTextField(1).getText(), 0);
		String obj = dataNames.get(select);
		availability.scoreboards.put(obj, new AvailabilityScoreboardData(eas, value));
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.isEmpty()) { return; }
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
				if (obj.equals(textfield.getText())) { return; }
				obj = textfield.getText();
				availability.scoreboards.remove(dataNames.get(select));
			}
		}
		else if (textfield.getID() == 1) {
			if (asd == null || asd.scoreboardValue == value) { return; }
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
