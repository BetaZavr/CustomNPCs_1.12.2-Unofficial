package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityPlayerName;
import noppes.npcs.controllers.data.Availability;

import javax.annotation.Nonnull;

public class SubGuiNpcAvailabilityNames extends SubGuiInterface implements ICustomScrollListener {

	protected final Availability availability;
	protected final Map<String, EnumAvailabilityPlayerName> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityNames(Availability availabilityIn) {
		super(0);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 217;

		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (select.isEmpty()) { return; }
				EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.values()[button.getValue()];
				availability.playerNames.put(select, eapn);
				initGui();
				break;
			}
			case 1: {
				SubGuiEditText subGui = new SubGuiEditText(0, select);
				subGui.hovers[0] = "availability.hover.player.name";
				setSubGui(subGui);
				break;
			}
			case 2: {
				availability.playerNames.remove(select);
				select = "";
				initGui();
				break;
			}
			case 3: {
				save();
				initGui();
				break;
			}
			case 66: onClosed(); break;
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
		data.clear();
		for (String name : availability.playerNames.keySet()) { data.put(name, availability.playerNames.get(name)); }
		if (!select.isEmpty() && !data.containsKey(select)) { select = ""; }
		scroll.setList(new ArrayList<>(data.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) { scroll.setSelected(select); }
		else { scroll.setSelect(-1); }
		addScroll(scroll);
		int p = 0;
		if (!select.isEmpty()) { p = data.get(select).ordinal(); }
		// type
		addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.only", "availability.except" }, p)
				.setHoverText("availability.hover.name." + p));
		// select
		addButton(new GuiNpcButton(1, guiLeft + 58, guiTop + ySize - 46, 170, 20, !select.isEmpty() ? select : "availability.select")
				.setHoverText("availability.hover.player.name"));
		// del
		addButton(new GuiNpcButton(2, guiLeft + 230, guiTop + ySize - 46, 20, 20, "X")
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
		EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.values()[getButton(0).getValue()];
		availability.playerNames.put(select, eapn);
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		SubGuiEditText subGui = new SubGuiEditText(0, select);
		subGui.hovers[0] = "availability.hover.player.name";
		setSubGui(subGui);
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		SubGuiEditText selector = (SubGuiEditText) subgui;
		if (selector.cancelled) { return; }
		EnumAvailabilityPlayerName eapn = EnumAvailabilityPlayerName.Only;
		if (!select.isEmpty()) {
			eapn = data.get(select);
			availability.playerNames.remove(select);
		}
		select = selector.text[0];
		availability.playerNames.put(select, eapn);
		initGui();
	}

}
