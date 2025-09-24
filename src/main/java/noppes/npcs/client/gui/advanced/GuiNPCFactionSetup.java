package noppes.npcs.client.gui.advanced;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcFactionSelect;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCFactionSetup extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scrollFactions;

	public GuiNPCFactionSetup(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: npc.advanced.attackOtherFactions = (button.getValue() == 1); break;
			case 1: npc.advanced.defendFaction = (button.getValue() == 1); initGui(); break;
			case 2: {
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : data.keySet()) {
					int id = data.get(name);
					if (npc.faction.id == id || npc.faction.attackFactions.contains(id)
							|| npc.faction.frendFactions.contains(id)
							|| npc.advanced.attackFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				setSubGui(new SubGuiNpcFactionSelect(0, "faction.friends", npc.advanced.friendFactions, corData));
				break;
			}
			case 3: {
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : data.keySet()) {
					int id = data.get(name);
					if (npc.faction.id == id || npc.faction.attackFactions.contains(id)
							|| npc.faction.frendFactions.contains(id)
							|| npc.advanced.friendFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				setSubGui(new SubGuiNpcFactionSelect(1, "faction.hostiles", npc.advanced.attackFactions, corData));
				break;
			}
			case 4: setSubGui(new SubGuiNpcFactionOptions(npc.advanced.factions)); break;
			case 5: npc.advanced.throughWalls = ((GuiNpcCheckBox) button).isSelected(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = 20;
		addLabel(new GuiNpcLabel(0, "faction.attackHostile", guiLeft + 4, guiTop + y + 5));
		addButton(new GuiNpcButton(0, guiLeft + 124, guiTop + y, 60, 20, new String[] { "gui.no", "gui.yes" }, (npc.advanced.attackOtherFactions ? 1 : 0)));
		y += 22;
		addLabel(new GuiNpcLabel(1, "faction.defend", guiLeft + 4, guiTop + y + 5));
		addButton(new GuiNpcButton(1, guiLeft + 124, guiTop + y, 60, 20, new String[] { "gui.no", "gui.yes" }, (npc.advanced.defendFaction ? 1 : 0)));
		if (npc.advanced.defendFaction) {
			y += 22;
			addButton(new GuiNpcCheckBox(5, guiLeft + 4, guiTop + y, 180, 20, "faction.through.walls", "", npc.advanced.throughWalls)
					.setHoverText("faction.hover.through.walls"));
        }
        y += 32;
        addLabel(new GuiNpcLabel(2, "faction.friends", guiLeft + 4, guiTop + y + 5));
		addButton(new GuiNpcButton(2, guiLeft + 124, guiTop + y, 60, 20, "selectServer.edit")
				.setHoverText("faction.hover.addfrends"));
		y += 22;
		addLabel(new GuiNpcLabel(3, "faction.hostiles", guiLeft + 4, guiTop + y + 5));
		addButton(new GuiNpcButton(3, guiLeft + 124, guiTop + y, 60, 20, "selectServer.edit")
				.setHoverText("faction.hover.addhostiles"));
		y += 32;
		addLabel(new GuiNpcLabel(4, "faction.ondeath", guiLeft + 4, guiTop + y + 5));
		addButton(new GuiNpcButton(4, guiLeft + 124, guiTop + y, 60, 20, "faction.points")
				.setHoverText("faction.hover.replace"));
		if (scrollFactions == null) { scrollFactions = new GuiCustomScroll(this, 0).setSize(180, 200); }
		scrollFactions.guiLeft = guiLeft + 200;
		scrollFactions.guiTop = guiTop + 4;
		addScroll(scrollFactions);
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.save(new NBTTagCompound())); }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) { Client.sendData(EnumPacketServer.FactionSet, data.get(scrollFactions.getSelected())); }
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		String name = npc.getFaction().name;
		data.clear();
		data.putAll(dataMap);
		scrollFactions.setList(dataList);
		if (name != null) { setSelected(name); }
	}

	@Override
	public void setSelected(String selected) { scrollFactions.setSelected(selected); }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNpcFactionSelect)) { return; }
		SubGuiNpcFactionSelect gui = (SubGuiNpcFactionSelect) subgui;
		if (gui.id == 0) {
			npc.advanced.friendFactions.clear();
			for (int id : gui.selectFactions) {
				npc.advanced.attackFactions.remove(id);
				npc.advanced.friendFactions.add(id);
			}
		} else if (gui.id == 1) {
			npc.advanced.attackFactions.clear();
			for (int id : gui.selectFactions) {
				npc.advanced.friendFactions.remove(id);
				npc.advanced.attackFactions.add(id);
			}
		}
		save();
	}

}
