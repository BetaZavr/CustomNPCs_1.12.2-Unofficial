package noppes.npcs.client.gui.roles;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleBank;

public class GuiNpcBankSetup
extends GuiNPCInterface2
implements IScrollData, ICustomScrollListener {

	private final HashMap<String, Integer> data = new HashMap<>();
	private final RoleBank role;
	private GuiCustomScroll scroll;

	public GuiNpcBankSetup(EntityNPCInterface npc) {
		super(npc);
		role = (RoleBank) npc.advanced.roleInterface;
	}

    @Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0); }
		scroll.setSize(200, 152);
		scroll.guiLeft = guiLeft + 85;
		scroll.guiTop = guiTop + 20;
		addScroll(scroll);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.BanksGet);
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
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && scroll != null) {
			scroll.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.RoleSave, role.save(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			role.bankId = data.get(scroll.getSelected());
			save();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
		close();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
		String name = null;
		Bank bank = role.getBank();
		if (bank != null) { name = bank.name; }
		data.clear();
		data.putAll(dataMap);
		scroll.setList(list);
		if (name != null) { setSelected(name); }
	}

	@Override
	public void setSelected(String selected) { scroll.setSelected(selected); }

}
