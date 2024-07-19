package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class GuiNpcFollowerSetup extends GuiContainerNPCInterface2 {

	private final RoleFollower role;

	public GuiNpcFollowerSetup(EntityNPCInterface npc, ContainerNPCFollowerSetup container) {
		super(npc, container);
		this.ySize = 200;
		this.role = (RoleFollower) npc.advanced.roleInterface;
		this.setBackground("followersetup.png");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 7: {
			this.role.infiniteDays = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 8: {
			this.role.disableGui = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 9: {
			this.role.refuseSoulStone = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 10: {
			this.role.killed();
			break;
		}
		}
	}

    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(3) != null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.mes.hire").getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.mes.let.go").getFormattedText());
		} else if (this.getTextField(5) != null && this.getTextField(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.mes.fired").getFormattedText());
		} else if (this.getTextField(6) != null && this.getTextField(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.money").getFormattedText());
		} else if (this.getTextField(7) != null && this.getTextField(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.days", "4").getFormattedText());
		} else if (this.getTextField(8) != null && this.getTextField(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.inventory").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.infinite").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.disable.gui").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.soulstone",
					new TextComponentTranslation("item.npcsoulstoneempty.name").getFormattedText()).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.reset").getFormattedText());
		} else {
			for (int i = 0; i < 3; i++) {
				if (this.getTextField(i) != null && this.getTextField(i).isMouseOver()) {
					this.setHoverText(
							new TextComponentTranslation("follower.hover.days", "" + (i + 1)).getFormattedText());
				}
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 66;
		int y = this.guiTop + 39;
		int lId = 0;
		this.addLabel(new GuiNpcLabel(lId++, "follower.hire", x, y - 12));
		int days;
		GuiNpcTextField tf;
		for (int i = 0; i < 3; ++i) {
			days = this.role.rates.getOrDefault(i, 1);
			this.addLabel(new GuiNpcLabel(lId++, "#" + (i + 1), x - 34, y + i * 25 + 4));
			tf = new GuiNpcTextField(i, this, this.fontRenderer, x, y + i * 25, 24, 16, "" + days);
			tf.setNumbersOnly();
			tf.setMinMaxDefault(1, Integer.MAX_VALUE, days);
			this.addTextField(tf);
		}
		days = this.role.rates.getOrDefault(3, 1);
		tf = new GuiNpcTextField(3, this, this.fontRenderer, x, y + 100, 24, 16, "" + days);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(1, Integer.MAX_VALUE, days);
		this.addTextField(tf);

		x += 34;
		y -= 33;
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, x, y, 286, 16, this.role.dialogHire));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, x, y += 19, 286, 16, this.role.dialogFarewell));
		this.addTextField(new GuiNpcTextField(5, this, this.fontRenderer, x, y += 19, 286, 16, this.role.dialogFired));

		x += 73;
		this.addButton(new GuiNpcCheckBox(7, x, y += 19, 120, 14, "follower.infiniteDays", this.role.infiniteDays));
		this.addButton(new GuiNpcCheckBox(8, x, y += 16, 120, 14, "follower.guiDisabled", this.role.disableGui));
		this.addButton(new GuiNpcCheckBox(9, x, y += 16, 120, 14, "follower.allowSoulstone", this.role.refuseSoulStone));

		tf = new GuiNpcTextField(6, this, this.fontRenderer, x + 45, y += 18, 60, 16, "" + this.role.rentalMoney);
		this.addLabel(new GuiNpcLabel(lId++, "gui.money", x, y + 4));
		this.addLabel(new GuiNpcLabel(lId++, "#4", x + 33, y + 4));
		this.addLabel(new GuiNpcLabel(lId++, CustomNpcs.displayCurrencies, x + 107, y + 4));
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0L, 9999999999L, this.role.rentalMoney);
		this.addTextField(tf);
		days = this.role.rates.getOrDefault(3, 1);
		tf = new GuiNpcTextField(7, this, this.fontRenderer, x + 120, y, 24, 16, "" + days);
		tf.setNumbersOnly();
		tf.setMinMaxDefault(1, Integer.MAX_VALUE, days);
		this.addTextField(tf);

		tf = new GuiNpcTextField(8, this, this.fontRenderer, x + 45, y += 19, 24, 16, "" + this.role.inventory.getSizeInventory());
		this.addLabel(new GuiNpcLabel(lId, "gui.things", x, y + 4));
		tf.setNumbersOnly();
		tf.setMinMaxDefault(0L, 9L, this.role.inventory.getSizeInventory());
		this.addTextField(tf);

		this.addButton(new GuiNpcButton(10, x, y + 19, 100, 20, "remote.reset"));
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
	public void save() {
		HashMap<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < 3; ++i) {
			int days = 1;
			if (!this.getTextField(i).isEmpty() && this.getTextField(i).isInteger()) {
				days = this.getTextField(i).getInteger();
			}
			if (days <= 0) {
				days = 1;
			}
			map.put(i, days);
		}
		this.role.rentalMoney = this.getTextField(6).getInteger();
		if (this.role.rentalMoney > 0) {
			int days = 1;
			if (!this.getTextField(7).isEmpty() && this.getTextField(7).isInteger()) {
				days = this.getTextField(7).getInteger();
			}
			if (days <= 0) {
				days = 1;
			}
			map.put(3, days);
		}
		this.role.rates = map;
		this.role.dialogHire = this.getTextField(3).getText();
		this.role.dialogFarewell = this.getTextField(4).getText();
		this.role.dialogFired = this.getTextField(5).getText();
		int size = this.role.disableGui ? 0 : this.getTextField(8).getInteger();
		if (this.role.inventory.getSizeInventory() != size) {
			this.role.inventory = new NpcMiscInventory(size);
		}
		Client.sendData(EnumPacketServer.RoleSave, this.role.writeToNBT(new NBTTagCompound()));
	}

}
