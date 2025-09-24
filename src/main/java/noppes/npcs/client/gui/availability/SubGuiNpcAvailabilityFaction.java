package noppes.npcs.client.gui.availability;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.GuiNPCFactionSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityFactionData;
import noppes.npcs.controllers.data.Faction;

import javax.annotation.Nonnull;

public class SubGuiNpcAvailabilityFaction extends SubGuiInterface implements ICustomScrollListener, GuiSelectionListener {

	protected final Map<String, Integer> dataIDs = new HashMap<>();
	protected final Map<String, AvailabilityFactionData> dataSets = new HashMap<>();
	protected final Availability availability;
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityFaction(Availability availabilityIn) {
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
				int id = dataIDs.get(select);
				AvailabilityFactionData afd = availability.factions.get(id);
				afd.factionAvailable = EnumAvailabilityFactionType.values()[button.getValue()];
				availability.factions.put(id, afd);
				select = "ID:" + id + " - ";
				Faction faction = FactionController.instance.factions.get(id);
				if (faction == null) { select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText()); }
				else {
					String stance = "";
					switch (afd.factionStance) {
						case Friendly: stance = "faction.friendly"; break;
						case Neutral: stance = "faction.neutral"; break;
						case Hostile: stance = "faction.unfriendly"; break;
					}
					select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase()).getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation(stance).getFormattedText() + ((char) 167) + "7)";
				}
				initGui();
				break;
			}
			case 1: {
				GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), select.isEmpty() ? 0 : dataIDs.get(select));
				gui.listener = this;
				NoppesUtil.openGUI(player, gui);
				break;
			}
			case 2: {
				availability.factions.remove(dataIDs.get(select));
				select = "";
				initGui();
				break;
			}
			case 3: {
				save();
				initGui();
				break;
			} // More
			case 4: {
				if (select.isEmpty()) { return; }
				EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[button.getValue()];
				int id = dataIDs.get(select);
				AvailabilityFactionData afd = availability.factions.get(id);
				afd.factionStance = eaf;
				availability.factions.put(id, afd);
				select = "ID:" + id + " - ";
				Faction faction = FactionController.instance.factions.get(id);
				if (faction == null) { select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText()); }
				else {
					String stance = "";
					switch (eaf) {
						case Friendly: stance = "faction.friendly"; break;
						case Neutral: stance = "faction.neutral"; break;
						case Hostile: stance = "faction.unfriendly"; break;
					}
					select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase()).getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation(stance).getFormattedText() + ((char) 167) + "7)";
				}
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
		dataIDs.clear();
		dataSets.clear();
		for (int id : availability.factions.keySet()) {
			String key = "ID:" + id + " - ";
			Faction faction = FactionController.instance.factions.get(id);
			AvailabilityFactionData afd = availability.factions.get(id);
			if (faction == null) { key += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText()); }
			else {
				String stance = "";
				switch (afd.factionStance) {
					case Friendly: stance = "faction.friendly"; break;
					case Neutral: stance = "faction.neutral"; break;
					case Hostile: stance = "faction.unfriendly"; break;
				}
				key += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3"
						+ new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase()).getFormattedText()
						+ ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation(stance).getFormattedText()
						+ ((char) 167) + "7)";
			}
			dataIDs.put(key, id);
			dataSets.put(key, availability.factions.get(id));
		}
		if (!select.isEmpty() && !dataIDs.containsKey(select)) { select = ""; }
		scroll.setList(new ArrayList<>(dataIDs.keySet()));
		scroll.guiLeft = guiLeft + 6;
		scroll.guiTop = guiTop + 14;
		if (!select.isEmpty()) { scroll.setSelected(select); }
		addScroll(scroll);
		// type
		int p = 0, s = 0;
		if (!select.isEmpty()) {
			p = dataSets.get(select).factionAvailable.ordinal();
			s = dataSets.get(select).factionStance.ordinal();
		}
		// type
		addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.always", "availability.is", "availability.isnot" }, p)
				.setHoverText("availability.hover.enum.type"));
		// faction type
		addButton(new GuiNpcButton(4, guiLeft + 58, guiTop + ySize - 46, 50, 20, new String[] { "faction.friendly", "faction.neutral", "faction.unfriendly" }, s)
				.setHoverText("availability.hover.faction.type"));
		// select
		addButton(new GuiNpcButton(1, guiLeft + 110, guiTop + ySize - 46, 178, 20, "availability.select")
				.setHoverText("availability.hover.faction"));
		// del
		addButton(new GuiNpcButton(2, guiLeft + 290, guiTop + ySize - 46, 20, 20, "X")
				.setHoverText("availability.hover.remove"));
		// extra
		addButton(new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more")
				.setIsEnable(!select.isEmpty())
				.setHoverText("availability.hover.more"));
		updateGuiButtons();
	}

	@Override
	public void save() {
		List<Integer> delete = new ArrayList<>();
		for (int id : availability.factions.keySet()) {
			if (availability.factions.get(id).factionAvailable == EnumAvailabilityFactionType.Always) { delete.add(id); }
		}
		for (int id : delete) { availability.factions.remove(id); }
		if (select.isEmpty()) { return; }
		EnumAvailabilityFactionType eaft = EnumAvailabilityFactionType.values()[getButton(0).getValue()];
		EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[getButton(4).getValue()];
		AvailabilityFactionData afd = new AvailabilityFactionData(eaft, eaf);
		int id = dataIDs.get(select);
		if (eaft != EnumAvailabilityFactionType.Always) {
			availability.factions.put(id, afd);
			dataSets.put(select, afd);
		}
		else { availability.factions.remove(id); }
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), dataIDs.get(select));
		gui.listener = this;
		NoppesUtil.openGUI(player, gui);
	}

	@Override
	public void selected(int id, String name) {
		if (id < 0) { return; }
		if (!select.isEmpty()) { availability.factions.remove(dataIDs.get(select)); }
		Faction faction = FactionController.instance.factions.get(id);
		AvailabilityFactionData afd = new AvailabilityFactionData(EnumAvailabilityFactionType.Is, EnumAvailabilityFaction.Friendly);
		select = "ID:" + id + " - ";
		if (faction == null) { select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText()); }
		else { select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation("availability.is").getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation("faction.friendly").getFormattedText() + ((char) 167) + "7)"; }
		availability.factions.put(id, afd);
		initGui();
		updateGuiButtons();
	}

	private void updateGuiButtons() {
		getButton(1).setDisplayText("availability.selectquest");
		int p = 0, s = 0;
		Faction faction = null;
		if (!select.isEmpty()) {
			faction = FactionController.instance.factions.get(dataIDs.get(select));
			p = dataSets.get(select).factionAvailable.ordinal();
			s = dataSets.get(select).factionStance.ordinal();
		}
		getButton(0).setDisplay(p);
		getButton(0).setIsEnable(!select.isEmpty());
		getButton(4).setDisplay(s);
		getButton(4).setIsEnable(!select.isEmpty());
		getButton(1).setIsEnable(p != 0 || select.isEmpty());
		getButton(1).setDisplayText(faction == null ? "availability.select" : faction.getName());
		getButton(2).setIsEnable(p != 0);
	}

}
