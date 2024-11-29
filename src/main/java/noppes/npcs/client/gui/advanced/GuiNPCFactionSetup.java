package noppes.npcs.client.gui.advanced;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcFactionSelect;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCFactionSetup extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener, ISubGuiListener {

	private final HashMap<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scrollFactions;

	public GuiNPCFactionSetup(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
			case 0: {
				this.npc.advanced.attackOtherFactions = (button.getValue() == 1);
				break;
			}
			case 1: {
				this.npc.advanced.defendFaction = (button.getValue() == 1);
				this.initGui();
				break;
			}
			case 2: {
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : this.data.keySet()) {
					int id = this.data.get(name);
					if (this.npc.faction.id == id || this.npc.faction.attackFactions.contains(id)
							|| this.npc.faction.frendFactions.contains(id)
							|| this.npc.advanced.attackFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				this.setSubGui(new SubGuiNpcFactionSelect(0, "faction.friends", this.npc.advanced.frendFactions, corData));
				break;
			}
			case 3: {
				HashMap<String, Integer> corData = new HashMap<>();
				for (String name : this.data.keySet()) {
					int id = this.data.get(name);
					if (this.npc.faction.id == id || this.npc.faction.attackFactions.contains(id)
							|| this.npc.faction.frendFactions.contains(id)
							|| this.npc.advanced.frendFactions.contains(id)) {
						continue;
					}
					corData.put(name, id);
				}
				this.setSubGui(
						new SubGuiNpcFactionSelect(1, "faction.hostiles", this.npc.advanced.attackFactions, corData));
				break;
			}
			case 4: {
				this.setSubGui(new SubGuiNpcFactionOptions(this.npc.advanced.factions));
				break;
			}
			case 5: {
				this.npc.advanced.throughWalls = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
		}
	}

	@Override
	public void close() {
		this.save();
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.addfrends").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.addhostiles").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.replace").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("faction.hover.through.walls").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = 20;
		this.addLabel(new GuiNpcLabel(0, "faction.attackHostile", this.guiLeft + 4, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 124, this.guiTop + y, 60, 20,
				new String[] { "gui.no", "gui.yes" }, (this.npc.advanced.attackOtherFactions ? 1 : 0)));
		y += 22;
		this.addLabel(new GuiNpcLabel(1, "faction.defend", this.guiLeft + 4, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 124, this.guiTop + y, 60, 20,
				new String[] { "gui.no", "gui.yes" }, (this.npc.advanced.defendFaction ? 1 : 0)));
		if (this.npc.advanced.defendFaction) {
			y += 22;
			addButton(new GuiNpcCheckBox(5, guiLeft + 4, guiTop + y, 180, 20, "faction.through.walls", "", npc.advanced.throughWalls));
        }
        y += 32;
        this.addLabel(new GuiNpcLabel(2, "faction.friends", this.guiLeft + 4, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 124, this.guiTop + y, 60, 20, "selectServer.edit"));
		y += 22;
		this.addLabel(new GuiNpcLabel(3, "faction.hostiles", this.guiLeft + 4, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 124, this.guiTop + y, 60, 20, "selectServer.edit"));
		y += 32;
		this.addLabel(new GuiNpcLabel(4, "faction.ondeath", this.guiLeft + 4, this.guiTop + y + 5));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 124, this.guiTop + y, 60, 20, "faction.points"));

		if (this.scrollFactions == null) {
			(this.scrollFactions = new GuiCustomScroll(this, 0)).setSize(180, 200);
		}
		this.scrollFactions.guiLeft = this.guiLeft + 200;
		this.scrollFactions.guiTop = this.guiTop + 4;
		this.addScroll(this.scrollFactions);
		Client.sendData(EnumPacketServer.FactionsGet);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && this.scrollFactions != null) {
			this.scrollFactions.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			Client.sendData(EnumPacketServer.FactionSet, this.data.get(this.scrollFactions.getSelected()));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = this.npc.getFaction().name;
		this.data.clear();
		this.data.putAll(data);
		this.scrollFactions.setList(list);
		if (name != null) {
			this.setSelected(name);
		}
	}

	@Override
	public void setSelected(String selected) {
		this.scrollFactions.setSelected(selected);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNpcFactionSelect)) {
			return;
		}
		SubGuiNpcFactionSelect gui = (SubGuiNpcFactionSelect) subgui;
		if (gui.id == 0) {
			this.npc.advanced.frendFactions.clear();
			for (int id : gui.selectFactions) {
				this.npc.advanced.attackFactions.remove(id);
				this.npc.advanced.frendFactions.add(id);
			}
		} else if (gui.id == 1) {
			this.npc.advanced.attackFactions.clear();
			for (int id : gui.selectFactions) {
				this.npc.advanced.frendFactions.remove(id);
				this.npc.advanced.attackFactions.add(id);
			}
		}
		this.save();
	}

}
