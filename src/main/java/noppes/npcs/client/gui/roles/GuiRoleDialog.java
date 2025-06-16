package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleDialog;

public class GuiRoleDialog
extends GuiNPCInterface2
implements ISubGuiListener {

	private final RoleDialog role;
	private int slot;

	public GuiRoleDialog(EntityNPCInterface npc) {
		super(npc);
		slot = 0;
		role = (RoleDialog) npc.advanced.roleInterface;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() <= 6) {
			save();
			slot = button.getID();
			String text = role.dialog;
			if (slot >= 1) { text = role.optionsTexts.get(slot); }
			if (text == null) { text = ""; }
			setSubGui(new SubGuiNpcTextArea(text));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(0, "dialog.starttext", guiLeft + 4, guiTop + 10));
		addButton(new GuiNpcButton(0, guiLeft + 60, guiTop + 5, 50, 20, "selectServer.edit"));
		addLabel(new GuiNpcLabel(100, "dialog.options", guiLeft + 4, guiTop + 34));
		for (int i = 1; i <= 6; ++i) {
			int y = guiTop + 24 + i * 23;
			addLabel(new GuiNpcLabel(i, i + ":", guiLeft + 4, y + 5));
			String text = role.options.get(i);
			if (text == null) { text = ""; }
			addTextField(new GuiNpcTextField(i, this, guiLeft + 16, y, 280, 20, text));
			addButton(new GuiNpcButton(i, guiLeft + 310, y, 50, 20, "selectServer.edit"));
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
	}

	@Override
	public void save() {
		HashMap<Integer, String> map = new HashMap<>();
		for (int i = 1; i <= 6; ++i) {
			String text = getTextField(i).getFullText();
			if (!text.isEmpty()) {
				map.put(i, text);
			}
		}
		role.options = map;
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea text = (SubGuiNpcTextArea) subgui;
			if (slot == 0) { role.dialog = text.text; }
			else if (text.text.isEmpty()) { role.optionsTexts.remove(slot); }
			else { role.optionsTexts.put(slot, text.text); }
		}
	}
}
