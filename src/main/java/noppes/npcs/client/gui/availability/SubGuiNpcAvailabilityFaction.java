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

public class SubGuiNpcAvailabilityFaction
extends SubGuiInterface
implements ICustomScrollListener, GuiSelectionListener {

	protected final Availability availability;
	protected final Map<String, Integer> dataIDs = new HashMap<>();
	protected final Map<String, AvailabilityFactionData> dataSets = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected String select = "";

	public SubGuiNpcAvailabilityFaction(Availability availabilityIn) {
		setBackground("menubg.png");
		xSize = 316;
		ySize = 217;
		closeOnEsc = true;

		availability = availabilityIn;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0 : {
				if (select.isEmpty()) { return; }
				int id = dataIDs.get(select);
				AvailabilityFactionData afd = availability.factions.get(id);
				afd.factionAvailable = EnumAvailabilityFactionType.values()[button.getValue()];
				availability.factions.put(id, afd);
				select = "ID:" + id + " - ";
				Faction faction = FactionController.instance.factions.get(id);
				if (faction == null) {
					select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
				} else {
					String stance = "";
					switch (afd.factionStance) {
						case Friendly: {
							stance = "faction.friendly";
							break;
						}
						case Neutral: {
							stance = "faction.neutral";
							break;
						}
						case Hostile: {
							stance = "faction.unfriendly";
							break;
						}
					}
					select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase()).getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation(stance).getFormattedText() + ((char) 167) + "7)";
				}
				initGui();
				break;
			}
			case  1: {
				GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), select.isEmpty() ? 0 : dataIDs.get(select));
				gui.listener = this;
				NoppesUtil.openGUI(player, gui);
				break;
			}
			case  2: {
				availability.factions.remove(dataIDs.get(select));
				select = "";
				initGui();
				break;
			}
			case  3: { // More
				save();
				initGui();
				break;
			}
			case  4: {
				if (select.isEmpty()) { return; }
				EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[button.getValue()];
				int id = dataIDs.get(select);
				AvailabilityFactionData afd = availability.factions.get(id);
				afd.factionStance = eaf;
				availability.factions.put(id, afd);
				select = "ID:" + id + " - ";
				Faction faction = FactionController.instance.factions.get(id);
				if (faction == null) {
					select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
				} else {
					String stance = "";
					switch (eaf) {
						case Friendly: {
							stance = "faction.friendly";
							break;
						}
						case Neutral: {
							stance = "faction.neutral";
							break;
						}
						case Hostile: {
							stance = "faction.unfriendly";
							break;
						}
					}
					select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase()).getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation(stance).getFormattedText() + ((char) 167) + "7)";
				}
				initGui();
				break;
			}
			case 66 : close(); break;
		}
	}

	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<>();
		for (int id : availability.factions.keySet()) {
			if (availability.factions.get(id).factionAvailable == EnumAvailabilityFactionType.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			availability.factions.remove(id);
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
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 6)).setSize(xSize - 12, ySize - 66); }
		dataIDs.clear();
		dataSets.clear();
		for (int id : availability.factions.keySet()) {
			String key = "ID:" + id + " - ";
			Faction faction = FactionController.instance.factions.get(id);
			AvailabilityFactionData afd = availability.factions.get(id);
			if (faction == null) {
				key += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
			} else {
				String stance = "";
				switch (afd.factionStance) {
				case Friendly: {
					stance = "faction.friendly";
					break;
				}
				case Neutral: {
					stance = "faction.neutral";
					break;
				}
				case Hostile: {
					stance = "faction.unfriendly";
					break;
				}
				}
				key += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3"
						+ new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase())
								.getFormattedText()
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
		button = new GuiNpcButton(0, guiLeft + 6, guiTop + ySize - 46, 50, 20, new String[] { "availability.always", "availability.is", "availability.isnot" }, p);
		button.setHoverText("availability.hover.enum.type");
		addButton(button);
		// faction type
		button = new GuiNpcButton(4, guiLeft + 58, guiTop + ySize - 46, 50, 20, new String[] { "faction.friendly", "faction.neutral", "faction.unfriendly" }, s);
		button.setHoverText("availability.hover.faction.type");
		addButton(button);
		// select
		button = new GuiNpcButton(1, guiLeft + 110, guiTop + ySize - 46, 178, 20, "availability.select");
		button.setHoverText("availability.hover.faction");
		addButton(button);
		// del
		button = new GuiNpcButton(2, guiLeft + 290, guiTop + ySize - 46, 20, 20, "X");
		button.setHoverText("availability.hover.remove");
		addButton(button);
		// extra
		button = new GuiNpcButton(3, guiLeft + xSize - 76, guiTop + 192, 70, 20, "availability.more");
		button.setEnabled(!select.isEmpty());
		button.setHoverText("availability.hover.more");
		addButton(button);
		updateGuiButtons();
	}

	@Override
	public void save() {
		if (select.isEmpty()) { return; }
		EnumAvailabilityFactionType eaft = EnumAvailabilityFactionType.values()[getButton(0).getValue()];
		EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[getButton(4).getValue()];
		AvailabilityFactionData afd = new AvailabilityFactionData(eaft, eaf);
		int id = dataIDs.get(select);
		if (eaft != EnumAvailabilityFactionType.Always) {
			availability.factions.put(id, afd);
			dataSets.put(select, afd);
		} else {
			availability.factions.remove(id);
		}
		select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		select = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		GuiNPCFactionSelection gui = new GuiNPCFactionSelection(npc, getParent(), dataIDs.get(select));
		gui.listener = this;
		NoppesUtil.openGUI(player, gui);
	}

	@Override
	public void selected(int id, String name) {
		if (id < 0) {
			return;
		}
		if (!select.isEmpty()) {
			availability.factions.remove(dataIDs.get(select));
		}
		Faction faction = FactionController.instance.factions.get(id);
		AvailabilityFactionData afd = new AvailabilityFactionData(EnumAvailabilityFactionType.Is,
				EnumAvailabilityFaction.Friendly);
		select = "ID:" + id + " - ";
		if (faction == null) {
			select += ((char) 167) + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
		} else {
			select += faction.getName() + ((char) 167) + "7 (" + ((char) 167) + "3" + new TextComponentTranslation("availability.is").getFormattedText() + ((char) 167) + "7) (" + ((char) 167) + "9" + new TextComponentTranslation("faction.friendly").getFormattedText() + ((char) 167) + "7)";
		}
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
		getButton(0).setEnabled(!select.isEmpty());
		getButton(4).setDisplay(s);
		getButton(4).setEnabled(!select.isEmpty());
		getButton(1).setEnabled(p != 0 || select.isEmpty());
		getButton(1).setDisplayText(faction == null ? "availability.select" : faction.getName());
		getButton(2).setEnabled(p != 0);
	}

}
