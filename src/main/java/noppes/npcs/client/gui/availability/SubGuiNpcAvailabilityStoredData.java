package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityStoredData;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityStoredData;

import javax.annotation.Nonnull;

public class SubGuiNpcAvailabilityStoredData extends SubGuiInterface implements ICustomScrollListener, ITextfieldListener {

	protected final Availability availability;
	protected final Map<String, AvailabilityStoredData> data = new TreeMap<>();
	protected AvailabilityStoredData select = null;
	protected GuiCustomScroll scroll;
	protected int keyError;

	public SubGuiNpcAvailabilityStoredData(Availability availabilityIn) {
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
			case 0: {
				if (select == null) { return; }
				select.type = EnumAvailabilityStoredData.values()[button.getValue()];
				initGui();
				break;
			}
			case 2: {
				if (select == null) { return; }
				availability.storeddata.remove(select);
				select = null;
				initGui();
				break;
			} // remove
			case 3: {
				if (getTextField(0) == null || getTextField(1) == null || getButton(0) == null) { return; }
				String key = getTextField(0).getText();
				int i = 0;
				if (select != null) {
					while (i < availability.storeddata.size()) {
						AvailabilityStoredData asd = availability.storeddata.get(i);
						i++;
						if (asd == select) { continue; }
						if (asd.key.equals(key)) {
							key += "_";
							i = 0;
						}
					}
					select.key = key;
					select.value = getTextField(1).getText();
					select.type = EnumAvailabilityStoredData.values()[getButton(0).getValue()];
					select = null;
				} else {
					while (i < availability.storeddata.size()) {
						AvailabilityStoredData asd = availability.storeddata.get(i);
						i++;
						if (asd.key.equals(key)) {
							key += "_";
							i = 0;
						}
					}
					availability.storeddata.add(new AvailabilityStoredData(key, getTextField(1).getText(), EnumAvailabilityStoredData.values()[getButton(0).getValue()]));
				}
				initGui();
				break;
			} // more
			case 66: onClosed(); break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (keyError > 0) {
			keyError--;
			if (getTextField(0) != null) {
				GuiNpcTextField textField = getTextField(0);
				if (keyError != 0) {
					textField.setTextColor(0xFFFF0000);
					textField.setDisabledTextColour(0xFFFF0000);
				} else {
					textField.setTextColor(0xFFFFFFFF);
					textField.setDisabledTextColour(0xFFFFFFFF);
				}
			}
		}
		if (getButton(3) != null && getTextField(0) != null) { getButton(3).setIsEnable(!getTextField(0).getText().isEmpty()); }
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		addLabel(new GuiNpcLabel(1, "availability.available", guiLeft, guiTop + 4)
				.setCenter(xSize));
		// exit
		int y = guiTop + ySize - 46;
		addButton(new GuiNpcButton(66, guiLeft + 6, y + 22, 70, 20, "gui.done")
				.setHoverText("hover.back"));
		// data list
		data.clear();
		String selKey = "";
		for (AvailabilityStoredData sd : availability.storeddata) {
			String type = "" + ((char) 167);
			switch(sd.type) {
				case ONLY: type += "a+"; break;
				case EXCEPT: type += "c-"; break;
				case SMALLER: type += "e<"; break;
				case EQUAL: type += "d="; break;
				case BIGGER: type += "b>"; break;
			}
			String key = type + ((char) 167) + "6\"" + ((char) 167) + "r" + sd.key + ((char) 167) + "6\"";
			if (!sd.value.isEmpty()) { key += ((char) 167) + "r - " + ((char) 167) + "b\"" + ((char) 167) + "r" + sd.value + ((char) 167) + "b\""; }
			data.put(key, sd);
			if (select != null && select.key.equals(sd.key)) {
				select = sd;
				selKey = key;
			}
		}
		if (select != null && selKey.isEmpty()) { select = null; }
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(xSize - 12, ySize - 64); }
		scroll.setList(new ArrayList<>(data.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!selKey.isEmpty()) { scroll.setSelected(selKey); }
		else { scroll.setSelect(-1); }
		addScroll(scroll);
		// type
		String[] enumNames = new String[EnumAvailabilityStoredData.values().length];
		int i = 0;
		for (EnumAvailabilityStoredData easd : EnumAvailabilityStoredData.values()) {
			enumNames[i] = "availability." + easd.name().toLowerCase();
			i++;
		}
		addButton(new GuiNpcButton(0, guiLeft + 6, y, 50, 20, enumNames, select == null || select.type == null ? 0 : select.type.ordinal())
				.setHoverText("availability.hover.sdtype." + (select == null || select.type == null  ? 0 : select.type.ordinal())));
		addButton(new GuiNpcButton(2, guiLeft + 290, y, 20, 20, "X")
				.setIsEnable(select != null)
				.setHoverText("availability.hover.remove"));
		// key
		int x = guiLeft + 58;
		addTextField(new GuiNpcTextField(0, this, x, y + 1, 112, 18, select != null ? select.key : "")
				.setHoverText("availability.hover.sd.key"));
		getTextField(0).setMaxStringLength(120);
		// value
		addTextField(new GuiNpcTextField(1, this, x + 116, y + 1, 112, 18, select != null ? select.value : "")
				.setHoverText("availability.hover.sd.value"));
		getTextField(1).setMaxStringLength(120);
		// extra
		addButton(new GuiNpcButton(3, guiLeft + xSize - 76, y + 22, 70, 20, "availability.more")
				.setHoverText("availability.hover.more"));
	}

    @Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!data.containsKey(scroll.getSelected())) { return; }
		select = data.get(scroll.getSelected());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getID() == 0) {
			if (textfield.isEmpty()) { return; }
			String key = textfield.getText();
			int i = 0;
            while (i < availability.storeddata.size()) {
                AvailabilityStoredData asd = availability.storeddata.get(i);
                i++;
                if (asd == select) {
                    continue;
                }
                if (asd.key.equals(key)) {
                    key += "_";
                    i = 0;
                }
            }
			if (!textfield.getText().equals(key)) {
				textfield.setText(key);
				keyError = 60;
			}
			if (select != null) {
				select.key = key;
				initGui();
			}
		} else if (select != null) {
			select.value = textfield.getText();
			initGui();
		}
	}

}
