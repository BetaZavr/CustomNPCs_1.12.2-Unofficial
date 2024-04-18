package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumAvailabilityFaction;
import noppes.npcs.constants.EnumAvailabilityFactionType;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityFactionData;
import noppes.npcs.controllers.data.Faction;

public class SubGuiNpcAvailabilityFaction extends SubGuiInterface
		implements ICustomScrollListener, GuiSelectionListener {
	private Availability availabitily;
	private String chr = "" + ((char) 167);
	private Map<String, Integer> dataIDs;
	private Map<String, AvailabilityFactionData> dataSets;
	private GuiCustomScroll scroll;
	private String select;

	public SubGuiNpcAvailabilityFaction(Availability availabitily) {
		this.availabitily = availabitily;
		this.setBackground("menubg.png");
		this.xSize = 316;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.dataIDs = new HashMap<String, Integer>();
		this.dataSets = new HashMap<String, AvailabilityFactionData>();
		this.select = "";
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			if (this.select.isEmpty()) {
				return;
			}
			int id = this.dataIDs.get(this.select);
			AvailabilityFactionData afd = this.availabitily.factions.get(id);
			afd.factionAvailable = EnumAvailabilityFactionType.values()[button.getValue()];
			this.availabitily.factions.put(id, afd);
			this.select = "ID:" + id + " - ";
			Faction faction = FactionController.instance.factions.get(id);
			if (faction == null) {
				this.select += chr + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
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
				this.select += faction.getName() + chr + "7 (" + chr + "3"
						+ new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase())
								.getFormattedText()
						+ chr + "7)" + chr + "7 (" + chr + "9" + new TextComponentTranslation(stance).getFormattedText()
						+ chr + "7)";
			}

			this.initGui();
		}
		if (button.id == 1) {
			GuiNPCFactionSelection gui = new GuiNPCFactionSelection(this.npc, this.getParent(),
					this.select.isEmpty() ? 0 : this.dataIDs.get(this.select));
			gui.listener = this;
			NoppesUtil.openGUI((EntityPlayer) this.player, gui);
		}
		if (button.id == 2) {
			this.availabitily.factions.remove(this.dataIDs.get(this.select));
			this.select = "";
			this.initGui();
		}
		if (button.id == 3) { // More
			this.save();
			this.initGui();
		}
		if (button.id == 4) {
			if (this.select.isEmpty()) {
				return;
			}
			EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[button.getValue()];
			int id = this.dataIDs.get(this.select);
			AvailabilityFactionData afd = this.availabitily.factions.get(id);
			afd.factionStance = eaf;
			this.availabitily.factions.put(id, afd);
			this.select = "ID:" + id + " - ";
			Faction faction = FactionController.instance.factions.get(id);
			if (faction == null) {
				this.select += chr + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
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
				this.select += faction.getName() + chr + "7 (" + chr + "3"
						+ new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase())
								.getFormattedText()
						+ chr + "7)" + chr + "7 (" + chr + "9" + new TextComponentTranslation(stance).getFormattedText()
						+ chr + "7)";
			}
			this.initGui();
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void close() {
		super.close();
		List<Integer> delete = new ArrayList<Integer>();
		for (int id : this.availabitily.factions.keySet()) {
			if (this.availabitily.factions.get(id).factionAvailable == EnumAvailabilityFactionType.Always) {
				delete.add(id);
			}
		}
		for (int id : delete) {
			this.availabitily.factions.remove(id);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + this.ySize - 46, 102, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.enum.type").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 110, this.guiTop + this.ySize - 46, 178, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.faction").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.remove").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover.more").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + 192, 70, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "availability.available", this.guiLeft, this.guiTop + 4));
		this.getLabel(1).center(this.xSize);
		this.addButton(new GuiNpcButton(66, this.guiLeft + 6, this.guiTop + 192, 70, 20, "gui.done"));
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 6)).setSize(this.xSize - 12, this.ySize - 66);
		}
		this.dataIDs = new HashMap<String, Integer>();
		this.dataSets = new HashMap<String, AvailabilityFactionData>();
		for (int id : this.availabitily.factions.keySet()) {
			String key = "ID:" + id + " - ";
			Faction faction = FactionController.instance.factions.get(id);
			AvailabilityFactionData afd = this.availabitily.factions.get(id);
			if (faction == null) {
				key += chr + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
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
				key += faction.getName() + chr + "7 (" + chr + "3"
						+ new TextComponentTranslation(("availability." + afd.factionAvailable).toLowerCase())
								.getFormattedText()
						+ chr + "7)" + chr + "7 (" + chr + "9" + new TextComponentTranslation(stance).getFormattedText()
						+ chr + "7)";
			}
			this.dataIDs.put(key, id);
			this.dataSets.put(key, this.availabitily.factions.get(id));
		}
		if (!this.select.isEmpty() && !this.dataIDs.containsKey(this.select)) {
			this.select = "";
		}
		this.scroll.setList(Lists.newArrayList(this.dataIDs.keySet()));
		this.scroll.guiLeft = this.guiLeft + 6;
		this.scroll.guiTop = this.guiTop + 14;
		if (!this.select.isEmpty()) {
			this.scroll.setSelected(this.select);
		}
		this.addScroll(this.scroll);
		int p = 0, s = 0;
		if (!this.select.isEmpty()) {
			p = this.dataSets.get(this.select).factionAvailable.ordinal();
			s = this.dataSets.get(this.select).factionStance.ordinal();
		}
		this.addButton(new GuiNpcButton(0, this.guiLeft + 6, this.guiTop + this.ySize - 46, 50, 20,
				new String[] { "availability.always", "availability.is", "availability.isnot" }, p));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 58, this.guiTop + this.ySize - 46, 50, 20,
				new String[] { "faction.friendly", "faction.neutral", "faction.unfriendly" }, s));
		this.addButton(
				new GuiNpcButton(1, this.guiLeft + 110, this.guiTop + this.ySize - 46, 178, 20, "availability.select"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 290, this.guiTop + this.ySize - 46, 20, 20, "X"));

		this.addButton(
				new GuiNpcButton(3, this.guiLeft + this.xSize - 76, this.guiTop + 192, 70, 20, "availability.more"));
		this.getButton(3).setEnabled(!this.select.isEmpty());
		this.updateGuiButtons();
	}

	@Override
	public void save() {
		if (this.select.isEmpty()) {
			return;
		}
		EnumAvailabilityFactionType eaft = EnumAvailabilityFactionType.values()[this.getButton(0).getValue()];
		EnumAvailabilityFaction eaf = EnumAvailabilityFaction.values()[this.getButton(4).getValue()];
		AvailabilityFactionData afd = new AvailabilityFactionData(eaft, eaf);
		int id = this.dataIDs.get(this.select);
		if (eaft != EnumAvailabilityFactionType.Always) {
			this.availabitily.factions.put(id, afd);
			this.dataSets.put(this.select, afd);
		} else {
			this.availabitily.factions.remove(id);
		}
		this.select = "";
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		this.select = scroll.getSelected();
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		GuiNPCFactionSelection gui = new GuiNPCFactionSelection(this.npc, this.getParent(),
				this.dataIDs.get(this.select));
		gui.listener = this;
		NoppesUtil.openGUI((EntityPlayer) this.player, gui);
	}

	@Override
	public void selected(int id, String name) {
		if (id < 0) {
			return;
		}
		if (!this.select.isEmpty()) {
			this.availabitily.factions.remove(this.dataIDs.get(this.select));
		}
		Faction faction = FactionController.instance.factions.get(id);
		AvailabilityFactionData afd = new AvailabilityFactionData(EnumAvailabilityFactionType.Is,
				EnumAvailabilityFaction.Friendly);
		this.select = "ID:" + id + " - ";
		if (faction == null) {
			this.select += chr + "4" + (new TextComponentTranslation("faction.notfound").getFormattedText());
		} else {
			this.select += faction.getName() + chr + "7 (" + chr + "3"
					+ new TextComponentTranslation("availability.is").getFormattedText() + chr + "7)" + chr + "7 ("
					+ chr + "9" + new TextComponentTranslation("faction.friendly").getFormattedText() + chr + "7)";
		}
		this.availabitily.factions.put(id, afd);
		this.initGui();
		this.updateGuiButtons();
	}

	private void updateGuiButtons() {
		this.getButton(1).setDisplayText("availability.selectquest");
		int p = 0, s = 0;
		Faction faction = null;
		if (!this.select.isEmpty()) {
			faction = FactionController.instance.factions.get(this.dataIDs.get(this.select));
			p = this.dataSets.get(this.select).factionAvailable.ordinal();
			s = this.dataSets.get(this.select).factionStance.ordinal();
		}
		this.getButton(0).setDisplay(p);
		this.getButton(0).setEnabled(!this.select.isEmpty());
		this.getButton(4).setDisplay(s);
		this.getButton(4).setEnabled(!this.select.isEmpty());
		this.getButton(1).setEnabled(p != 0 || this.select.isEmpty());
		this.getButton(1).setDisplayText(faction == null ? "availability.select" : faction.getName());
		this.getButton(2).setEnabled(p != 0);
	}

}
