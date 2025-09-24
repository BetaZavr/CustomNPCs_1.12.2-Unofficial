package noppes.npcs.client.gui.advanced;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNPCLinesEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCLinesMenu extends GuiNPCInterface2 implements IGuiData {

	public GuiNPCLinesMenu(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: setSubGui(new SubGuiNPCLinesEdit(0, npc, npc.advanced.worldLines, "lines.world")); break;
			case 1: setSubGui(new SubGuiNPCLinesEdit(1, npc, npc.advanced.attackLines, "lines.attack")); break;
			case 2: setSubGui(new SubGuiNPCLinesEdit(2, npc, npc.advanced.interactLines, "lines.interact")); break;
			case 3: setSubGui(new SubGuiNPCLinesEdit(3, npc, npc.advanced.killedLines, "lines.killed")); break;
			case 4: setSubGui(new SubGuiNPCLinesEdit(4, npc, npc.advanced.killLines, "lines.kill")); break;
			case 5: setSubGui(new SubGuiNPCLinesEdit(5, npc, npc.advanced.npcInteractLines, "lines.npcinteract")); break;
			case 6: npc.advanced.orderedLines = !((GuiNpcButtonYesNo) button).getBoolean(); break;
		}
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
		addButton(new GuiNpcButtonYesNo(6, x + 95, y, !npc.advanced.orderedLines));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.save(new NBTTagCompound())); }

	@Override
	public void setGuiData(NBTTagCompound compound) {
		npc.advanced.load(compound);
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (!(subgui instanceof SubGuiNPCLinesEdit)) {
			return;
		}
		SubGuiNPCLinesEdit sub = (SubGuiNPCLinesEdit) subgui;
		sub.lines.correctLines();
		switch (sub.id) {
			case 0: npc.advanced.worldLines = sub.lines; break;
			case 1: npc.advanced.attackLines = sub.lines; break;
			case 2: npc.advanced.interactLines = sub.lines; break;
			case 3: npc.advanced.killedLines = sub.lines; break;
			case 4: npc.advanced.killLines = sub.lines; break;
			case 5: npc.advanced.npcInteractLines = sub.lines; break;
		}
		save();
	}

}
