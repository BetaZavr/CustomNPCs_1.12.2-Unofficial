package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFarmer;

public class GuiJobFarmer extends GuiNPCInterface2 {

	private JobFarmer job;

	public GuiJobFarmer(EntityNPCInterface npc) {
		super(npc);
		this.job = (JobFarmer) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.job.chestMode = button.getValue();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "farmer.itempicked", this.guiLeft + 10, this.guiTop + 20));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 100, this.guiTop + 15, 160, 20,
				new String[] { "farmer.donothing", "farmer.chest", "farmer.drop" }, this.job.chestMode));
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
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}
}
