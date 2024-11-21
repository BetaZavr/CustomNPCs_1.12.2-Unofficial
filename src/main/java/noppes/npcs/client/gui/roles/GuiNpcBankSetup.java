package noppes.npcs.client.gui.roles;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleBank;

public class GuiNpcBankSetup extends GuiNPCInterface2 implements IScrollData, ICustomScrollListener {

	private final HashMap<String, Integer> data = new HashMap<>();
	private final RoleBank role;
	private GuiCustomScroll scroll;

	public GuiNpcBankSetup(EntityNPCInterface npc) {
		super(npc);
		this.role = (RoleBank) npc.advanced.roleInterface;
	}

    @Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.scroll.setSize(200, 152);
		this.scroll.guiLeft = this.guiLeft + 85;
		this.scroll.guiTop = this.guiTop + 20;
		this.addScroll(this.scroll);
	}

	@Override
	public void initPacket() {
		Client.sendData(EnumPacketServer.BanksGet);
	}

	@Override
	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
		if (i == 1) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && this.scroll != null) {
			this.scroll.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.RoleSave, this.role.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			this.role.bankId = this.data.get(this.scroll.getSelected());
			this.save();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		this.close();
		CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = null;
		Bank bank = this.role.getBank();
		if (bank != null) {
			name = bank.name;
		}
		this.data.clear();
		this.data.putAll(data);
		this.scroll.setList(list);
		if (name != null) {
			this.setSelected(name);
		}
	}

	@Override
	public void setSelected(String selected) {
		this.scroll.setSelected(selected);
	}
}
