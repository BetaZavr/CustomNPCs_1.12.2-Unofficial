package noppes.npcs.client.gui.roles;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNPCFollowerSetup;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class GuiNpcFollowerSetup
extends GuiContainerNPCInterface2 {

	private final RoleFollower role;

	public GuiNpcFollowerSetup(EntityNPCInterface npc, ContainerNPCFollowerSetup container) {
		super(npc, container);
		ySize = 200;
		role = (RoleFollower) npc.advanced.roleInterface;
		setBackground("followersetup.png");
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 7: {
				role.infiniteDays = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 8: {
				role.disableGui = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 9: {
				role.refuseSoulStone = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 10: {
				role.killed();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 66;
		int y = guiTop + 39;
		int lId = 0;
		// days
		addLabel(new GuiNpcLabel(lId++, "follower.hire", x, y - 12));
		int days;
		GuiNpcTextField textField;
		for (int i = 0; i < 3; ++i) {
			days = role.rates.getOrDefault(i, 1);
			addLabel(new GuiNpcLabel(lId++, "#" + (i + 1), x - 34, y + i * 25 + 4));
			textField = new GuiNpcTextField(i, this, fontRenderer, x, y + i * 25, 24, 16, "" + days);
			textField.setMinMaxDefault(1, Integer.MAX_VALUE, days);
			setHoverText(new TextComponentTranslation("follower.hover.days", "" + (i + 1)).getFormattedText());
			addTextField(textField);
		}

		x += 34;
		y -= 33;
		textField = new GuiNpcTextField(3, this, fontRenderer, x, y, 286, 16, role.dialogHire);
		textField.setHoverText("follower.hover.mes.hire");
		addTextField(textField);

		textField = new GuiNpcTextField(4, this, fontRenderer, x, y += 19, 286, 16, role.dialogFarewell);
		textField.setHoverText("follower.hover.mes.let.go");
		addTextField(textField);

		textField = new GuiNpcTextField(5, this, fontRenderer, x, y += 19, 286, 16, role.dialogFired);
		textField.setHoverText("follower.hover.mes.fired");
		addTextField(textField);

		x += 73;
		GuiNpcButton button = new GuiNpcCheckBox(7, x, y += 19, 120, 14, "follower.infiniteDays", null, role.infiniteDays);
		button.setHoverText("follower.hover.infinite");
		addButton(button);
		button = new GuiNpcCheckBox(8, x, y += 16, 120, 14, "follower.guiDisabled", null, role.disableGui);
		button.setHoverText("follower.hover.disable.gui");
		addButton(button);
		button = new GuiNpcCheckBox(9, x, y += 16, 120, 14, "follower.allowSoulstone", null, role.refuseSoulStone);
		button.setHoverText("follower.hover.soulstone", new TextComponentTranslation("item.npcsoulstoneempty.name").getFormattedText());
		addButton(button);

		textField = new GuiNpcTextField(6, this, fontRenderer, x + 45, y += 18, 60, 16, "" + role.rentalMoney);
		textField.setMinMaxDefault(0L, 9999999999L, role.rentalMoney);
		textField.setHoverText("follower.hover.money");
		addTextField(textField);
		addLabel(new GuiNpcLabel(lId++, "gui.money", x, y + 4));
		addLabel(new GuiNpcLabel(lId++, "#4", x + 33, y + 4));
		addLabel(new GuiNpcLabel(lId++, CustomNpcs.displayCurrencies, x + 107, y + 4));

		days = role.rates.getOrDefault(3, 1);
		textField = new GuiNpcTextField(7, this, fontRenderer, x + 120, y, 24, 16, "" + days);
		textField.setMinMaxDefault(1, Integer.MAX_VALUE, days);
		textField.setHoverText("follower.hover.days", "4");
		addTextField(textField);

		textField = new GuiNpcTextField(8, this, fontRenderer, x + 45, y += 19, 24, 16, "" + role.inventory.getSizeInventory());
		textField.setMinMaxDefault(0L, 9L, role.inventory.getSizeInventory());
		textField.setHoverText("follower.hover.inventory");
		addTextField(textField);
		addLabel(new GuiNpcLabel(lId, "gui.things", x, y + 4));

		button = new GuiNpcButton(10, x, y + 19, 100, 20, "remote.reset");
		button.setHoverText("follower.hover.reset");
		addButton(button);
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
		// days
		HashMap<Integer, Integer> map = new HashMap<>();
		for (int i = 0; i < 3; ++i) {
			int days = 1;
			if (!getTextField(i).isEmpty() && getTextField(i).isInteger()) { days = getTextField(i).getInteger(); }
			if (days <= 0) { days = 1; }
			map.put(i, days);
		}
		role.rentalMoney = getTextField(6).getInteger();
		if (role.rentalMoney > 0) {
			int days = 1;
			if (!getTextField(7).isEmpty() && getTextField(7).isInteger()) { days = getTextField(7).getInteger(); }
			if (days <= 0) { days = 1; }
			map.put(3, days);
		}
		role.rates = map;
		role.dialogHire = getTextField(3).getText();
		role.dialogFarewell = getTextField(4).getText();
		role.dialogFired = getTextField(5).getText();
		int size = role.disableGui ? 0 : getTextField(8).getInteger();
		if (role.inventory.getSizeInventory() != size) { role.inventory = new NpcMiscInventory(size); }
		Client.sendData(EnumPacketServer.RoleSave, role.writeToNBT(new NBTTagCompound()));
	}

}
