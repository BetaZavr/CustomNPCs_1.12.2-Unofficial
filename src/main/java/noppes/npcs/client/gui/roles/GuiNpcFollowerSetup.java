package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class GuiNpcFollowerSetup
extends GuiContainerNPCInterface2 {
	
	private RoleFollower role;

	public GuiNpcFollowerSetup(EntityNPCInterface npc, ContainerNPCFollowerSetup container) {
		super(npc, container);
		this.ySize = 200;
		this.role = (RoleFollower) npc.advanced.roleInterface;
		this.setBackground("followersetup.png");
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 7) {
			this.role.infiniteDays = ((GuiNpcButtonYesNo) guibutton).getBoolean();
		}
		if (guibutton.id == 8) {
			this.role.disableGui = ((GuiNpcButtonYesNo) guibutton).getBoolean();
		}
		if (guibutton.id == 9) {
			this.role.refuseSoulStone = !((GuiNpcButtonYesNo) guibutton).getBoolean();
		}
		if (guibutton.id == 10) {
			this.role.killed();
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 3; ++i) {
			int x = this.guiLeft + 66;
			int y = this.guiTop + 37;
			y += i * 25;
			GuiNpcTextField tf = new GuiNpcTextField(i, (GuiScreen) this, this.fontRenderer, x, y, 24, 20, "1");
			tf.setNumbersOnly();
			tf.setMinMaxDefault(1, Integer.MAX_VALUE, 1);
			this.addTextField(tf);
		}
		int i = 0;
		for (int day : this.role.rates.values()) {
			this.getTextField(i).setText(day + "");
			++i;
		}
		this.addTextField(new GuiNpcTextField(3, (GuiScreen) this, this.fontRenderer, this.guiLeft + 100,
				this.guiTop + 6, 286, 20, this.role.dialogHire));
		this.addTextField(new GuiNpcTextField(4, (GuiScreen) this, this.fontRenderer, this.guiLeft + 100,
				this.guiTop + 30, 286, 20, this.role.dialogFarewell));
		this.addLabel(new GuiNpcLabel(7, "follower.infiniteDays", this.guiLeft + 235, this.guiTop + 80));
		this.addButton(new GuiNpcButtonYesNo(7, this.guiLeft + 180, this.guiTop + 75, this.role.infiniteDays));
		this.addLabel(new GuiNpcLabel(8, "follower.guiDisabled", this.guiLeft + 235, this.guiTop + 104));
		this.addButton(new GuiNpcButtonYesNo(8, this.guiLeft + 180, this.guiTop + 99, this.role.disableGui));
		this.addLabel(new GuiNpcLabel(9, "follower.allowSoulstone", this.guiLeft + 235, this.guiTop + 128));
		this.addButton(new GuiNpcButtonYesNo(9, this.guiLeft + 180, this.guiTop + 123, !this.role.refuseSoulStone));
		this.addButton(new GuiNpcButton(10, this.guiLeft + 180, this.guiTop + 147, 100, 20, "remote.reset"));
	}

	@Override
	public void save() {
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < this.role.inventory.getSizeInventory(); ++i) {
			ItemStack item = this.role.inventory.getStackInSlot(i);
			if (item != null && !item.isEmpty()) {
				int days = 1;
				if (!this.getTextField(i).isEmpty() && this.getTextField(i).isInteger()) {
					days = this.getTextField(i).getInteger();
				}
				if (days <= 0) {
					days = 1;
				}
				map.put(i, days);
			}
		}
		this.role.rates = map;
		this.role.dialogHire = this.getTextField(3).getText();
		this.role.dialogFarewell = this.getTextField(4).getText();
		Client.sendData(EnumPacketServer.RoleSave, this.role.writeToNBT(new NBTTagCompound()));
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
