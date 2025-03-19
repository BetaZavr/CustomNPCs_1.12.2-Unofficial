package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;

public class GuiNpcItemGiver
extends GuiContainerNPCInterface2 {

	public static GuiScreen parent;
	private final JobItemGiver role;

	public GuiNpcItemGiver(EntityNPCInterface npc, ContainerNpcItemGiver container) {
		super(npc, container);
		ySize = 200;
		setBackground("npcitemgiver.png");

		role = (JobItemGiver) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: role.givingMethod = button.getValue(); break;
			case 1: {
				role.cooldownType = button.getValue();
				getTextField(0).setEnabled(role.isOnTimer());
				getLabel(0).setEnabled(role.isOnTimer());
				break;
			}
			case 4: setSubGui(new SubGuiNpcAvailability(role.availability, parent)); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiNpcButton(0, guiLeft + 6, guiTop + 6, 140, 20, new String[] { "role.give.rnd", "role.give.all", "role.give.not.owned", "role.give.doesnt.own", "role.give.chained" }, role.givingMethod));
		addButton(new GuiNpcButton(1, guiLeft + 6, guiTop + 29, 140, 20, new String[] { "role.cooldown.timer", "role.cooldown.one", "quest.rldaily" }, role.cooldownType));

		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, guiLeft + 55, guiTop + 54, 90, 20, role.cooldown + "");
		textField.setMinMaxDefault(0, Integer.MAX_VALUE, role.cooldown);
		textField.enabled = role.isOnTimer();
		addTextField(textField);

		GuiNpcLabel label = new GuiNpcLabel(0, "spawner.cooldown", guiLeft + 6, guiTop + 59);
		label.enabled = role.isOnTimer();
		addLabel(label);

		addLabel(new GuiNpcLabel(1, "role.items.give", guiLeft + 46, guiTop + 79));
		int i = 0;
		for (String line : role.lines) {
			addTextField(new GuiNpcTextField(i + 1, this, fontRenderer, guiLeft + 150, guiTop + 6 + i * 24, 236, 20, line));
			++i;
		}
		while (i < 3) {
			addTextField(new GuiNpcTextField(i + 1, this, fontRenderer, guiLeft + 150, guiTop + 6 + i * 24, 236, 20, ""));
			++i;
		}
		addLabel(new GuiNpcLabel(4, "availability.options", guiLeft + 180, guiTop + 101));
		addButton(new GuiNpcButton(4, guiLeft + 280, guiTop + 96, 50, 20, "selectServer.edit"));
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
		List<String> lines = new ArrayList<>();
		for (int i = 1; i < 4; ++i) {
			IGuiNpcTextField tf = getTextField(i);
			if (!tf.isEmpty()) {
				lines.add(tf.getText());
			}
		}
		role.lines = lines;
		int cc = 10;
		if (!getTextField(0).isEmpty() && getTextField(0).isInteger()) {
			cc = getTextField(0).getInteger();
		}
		role.cooldown = cc;
		Client.sendData(EnumPacketServer.JobSave, role.writeToNBT(new NBTTagCompound()));
	}
}
