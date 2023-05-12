package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleDialog;

public class GuiRoleDialog
extends GuiNPCInterface2
implements ISubGuiListener {
	
	private RoleDialog role;
	private int slot;

	public GuiRoleDialog(EntityNPCInterface npc) {
		super(npc);
		this.slot = 0;
		this.role = (RoleDialog) npc.advanced.roleInterface;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id <= 6) {
			this.save();
			this.slot = guibutton.id;
			String text = this.role.dialog;
			if (this.slot >= 1) {
				text = this.role.optionsTexts.get(this.slot);
			}
			if (text == null) {
				text = "";
			}
			this.setSubGui(new SubGuiNpcTextArea(text));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "dialog.starttext", this.guiLeft + 4, this.guiTop + 10));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 60, this.guiTop + 5, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(100, "dialog.options", this.guiLeft + 4, this.guiTop + 34));
		for (int i = 1; i <= 6; ++i) {
			int y = this.guiTop + 24 + i * 23;
			this.addLabel(new GuiNpcLabel(i, i + ":", this.guiLeft + 4, y + 5));
			String text = this.role.options.get(i);
			if (text == null) {
				text = "";
			}
			this.addTextField(new GuiNpcTextField(i, this, this.guiLeft + 16, y, 280, 20, text));
			this.addButton(new GuiNpcButton(i, this.guiLeft + 310, y, 50, 20, "selectServer.edit"));
		}
	}

	@Override
	public void save() {
		HashMap<Integer, String> map = new HashMap<Integer, String>();
		for (int i = 1; i <= 6; ++i) {
			String text = this.getTextField(i).getText();
			if (!text.isEmpty()) {
				map.put(i, text);
			}
		}
		this.role.options = map;
		Client.sendData(EnumPacketServer.RoleSave, this.role.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea text = (SubGuiNpcTextArea) subgui;
			if (this.slot == 0) {
				this.role.dialog = text.text;
			} else if (text.text.isEmpty()) {
				this.role.optionsTexts.remove(this.slot);
			} else {
				this.role.optionsTexts.put(this.slot, text.text);
			}
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}
}
