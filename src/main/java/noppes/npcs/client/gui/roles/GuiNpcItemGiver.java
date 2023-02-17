package noppes.npcs.client.gui.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcItemGiver;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobItemGiver;

public class GuiNpcItemGiver extends GuiContainerNPCInterface2 {
	private JobItemGiver role;

	public GuiNpcItemGiver(EntityNPCInterface npc, ContainerNpcItemGiver container) {
		super(npc, container);
		this.ySize = 200;
		this.role = (JobItemGiver) npc.jobInterface;
		this.setBackground("npcitemgiver.png");
	}

	public void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			this.role.givingMethod = button.getValue();
		}
		if (button.id == 1) {
			this.role.cooldownType = button.getValue();
			this.getTextField(0).enabled = this.role.isOnTimer();
			this.getLabel(0).enabled = this.role.isOnTimer();
		}
		if (button.id == 4) {
			this.setSubGui(new SubGuiNpcAvailability(this.role.availability));
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(
				new GuiNpcButton(
						0, this.guiLeft + 6, this.guiTop + 6, 140, 20, new String[] { "Random Item", "All Items",
								"Give Not Owned Items", "Give When Doesnt Own Any", "Chained" },
						this.role.givingMethod));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 6, this.guiTop + 29, 140, 20,
				new String[] { "Timer", "Give Only Once", "Daily" }, this.role.cooldownType));
		this.addTextField(new GuiNpcTextField(0, (GuiScreen) this, this.fontRenderer, this.guiLeft + 55,
				this.guiTop + 54, 90, 20, this.role.cooldown + ""));
		this.getTextField(0).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(0, "Cooldown:", this.guiLeft + 6, this.guiTop + 59));
		this.addLabel(new GuiNpcLabel(1, "Items to give", this.guiLeft + 46, this.guiTop + 79));
		this.getTextField(0).numbersOnly = true;
		int i = 0;
		for (String line : this.role.lines) {
			this.addTextField(new GuiNpcTextField(i + 1, (GuiScreen) this, this.fontRenderer, this.guiLeft + 150,
					this.guiTop + 6 + i * 24, 236, 20, line));
			++i;
		}
		while (i < 3) {
			this.addTextField(new GuiNpcTextField(i + 1, (GuiScreen) this, this.fontRenderer, this.guiLeft + 150,
					this.guiTop + 6 + i * 24, 236, 20, ""));
			++i;
		}
		this.getTextField(0).enabled = this.role.isOnTimer();
		this.getLabel(0).enabled = this.role.isOnTimer();
		this.addLabel(new GuiNpcLabel(4, "availability.options", this.guiLeft + 180, this.guiTop + 101));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 280, this.guiTop + 96, 50, 20, "selectServer.edit"));
	}

	@Override
	public void save() {
		List<String> lines = new ArrayList<String>();
		for (int i = 1; i < 4; ++i) {
			GuiNpcTextField tf = this.getTextField(i);
			if (!tf.isEmpty()) {
				lines.add(tf.getText());
			}
		}
		this.role.lines = lines;
		int cc = 10;
		if (!this.getTextField(0).isEmpty() && this.getTextField(0).isInteger()) {
			cc = this.getTextField(0).getInteger();
		}
		this.role.cooldown = cc;
		Client.sendData(EnumPacketServer.JobSave, this.role.writeToNBT(new NBTTagCompound()));
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
