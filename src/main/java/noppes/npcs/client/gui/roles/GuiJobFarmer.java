package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobFarmer;

import javax.annotation.Nonnull;

public class GuiJobFarmer extends GuiNPCInterface2 {

	protected final JobFarmer job;

	public GuiJobFarmer(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		job = (JobFarmer) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && button.getID() == 0) { job.chestMode = button.getValue(); }
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(0, "farmer.itempicked", guiLeft + 10, guiTop + 20));
		addButton(new GuiNpcButton(0, guiLeft + 100, guiTop + 15, 160, 20, new String[] { "farmer.donothing", "farmer.chest", "farmer.drop" }, job.chestMode));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.JobSave, job.save(new NBTTagCompound())); }

}
