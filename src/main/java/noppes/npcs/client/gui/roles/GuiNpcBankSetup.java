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

public class GuiNpcBankSetup extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected final RoleBank role;
	protected GuiCustomScroll scroll;

	public GuiNpcBankSetup(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

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
	public void initPacket() { Client.sendData(EnumPacketServer.BanksGet); }

	@Override
	public void save() { Client.sendData(EnumPacketServer.RoleSave, role.save(new NBTTagCompound())); }

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			role.bankId = data.get(scroll.getSelected());
			save();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		onClosed();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		String name = null;
		Bank bank = role.getBank();
		if (bank != null) { name = bank.name; }
		data.clear();
		data.putAll(dataMap);
		scroll.setList(dataList);
		if (name != null) { setSelected(name); }
	}

	@Override
	public void setSelected(String selected) { scroll.setSelected(selected); }

}
