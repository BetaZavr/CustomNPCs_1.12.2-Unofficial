package noppes.npcs.client.gui.advanced;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNPCLinesEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCLinesMenu
extends GuiNPCInterface2
implements IGuiData, ISubGuiListener {

	public GuiNPCLinesMenu(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				setSubGui(new SubGuiNPCLinesEdit(0, npc, npc.advanced.worldLines, "lines.world"));
				break;
			}
			case 1: {
				setSubGui(new SubGuiNPCLinesEdit(1, npc, npc.advanced.attackLines, "lines.attack"));
				break;
			}
			case 2: {
				setSubGui(new SubGuiNPCLinesEdit(2, npc, npc.advanced.interactLines, "lines.interact"));
				break;
			}
			case 3: {
				setSubGui(new SubGuiNPCLinesEdit(3, npc, npc.advanced.killedLines, "lines.killed"));
				break;
			}
			case 4: {
				setSubGui(new SubGuiNPCLinesEdit(4, npc, npc.advanced.killLines, "lines.kill"));
				break;
			}
			case 5: {
				setSubGui(new SubGuiNPCLinesEdit(5, npc, npc.advanced.npcInteractLines, "lines.npcinteract"));
				break;
			}
			case 6: {
				npc.advanced.orderedLines = !((GuiNpcButtonYesNo) button).getBoolean();
				break;
			}
		}
	}

	@Override
	public void close() {
		save();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 85;
		int y = guiTop + 20;
		addButton(new GuiNpcButton(0, x, y, "lines.world"));
		addButton(new GuiNpcButton(1, x, y += 23, "lines.attack"));
		addButton(new GuiNpcButton(2, x, y += 23, "lines.interact"));
		addButton(new GuiNpcButton(3, x, y += 23, "lines.killed"));
		addButton(new GuiNpcButton(4, x, y += 23, "lines.kill"));
		addButton(new GuiNpcButton(5, x, y += 23, "lines.npcinteract"));
		addLabel(new GuiNpcLabel(16, "lines.random", x, (y += 23) + 5));
		addButton(new GuiNpcButtonYesNo(6, x + 95, y, !this.npc.advanced.orderedLines));
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
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.npc.advanced.readToNBT(compound);
		this.initGui();
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNPCLinesEdit)) {
			return;
		}
		SubGuiNPCLinesEdit sub = (SubGuiNPCLinesEdit) subgui;
		sub.lines.correctLines();
		switch (sub.id) {
			case 0: {
				this.npc.advanced.worldLines = sub.lines;
				break;
			}
			case 1: {
				this.npc.advanced.attackLines = sub.lines;
				break;
			}
			case 2: {
				this.npc.advanced.interactLines = sub.lines;
				break;
			}
			case 3: {
				this.npc.advanced.killedLines = sub.lines;
				break;
			}
			case 4: {
				this.npc.advanced.killLines = sub.lines;
				break;
			}
			case 5: {
				this.npc.advanced.npcInteractLines = sub.lines;
				break;
			}
		}
		this.save();
	}

}
