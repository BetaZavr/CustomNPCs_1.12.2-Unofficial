package noppes.npcs.client.gui.roles;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;

public class GuiNpcBard extends GuiNPCInterface2 implements ISubGuiListener {
	private JobBard job;

	public GuiNpcBard(EntityNPCInterface npc) {
		super(npc);
		this.job = (JobBard) npc.jobInterface;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			this.setSubGui(new GuiSoundSelection(this.job.song));
			MusicController.Instance.stopMusic();
		}
		if (button.id == 1) {
			this.job.song = "";
			this.getLabel(0).setLabel(null);
			MusicController.Instance.stopMusic();
		}
		if (button.id == 3) {
			this.job.isStreamer = (button.getValue() == 0);
			this.initGui();
		}
		if (button.id == 4) {
			this.job.hasOffRange = (button.getValue() == 1);
			this.initGui();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(1, this.guiLeft + 55, this.guiTop + 15, 20, 20, "X"));
		this.addLabel(new GuiNpcLabel(0, this.job.song, this.guiLeft + 80, this.guiTop + 20));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 75, this.guiTop + 50, "gui.selectSound"));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 75, this.guiTop + 92,
				new String[] { "bard.jukebox", "bard.background" }, (this.job.isStreamer ? 0 : 1)));
		this.addLabel(new GuiNpcLabel(2, "bard.ondistance", this.guiLeft + 60, this.guiTop + 143));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 160, this.guiTop + 138, 40, 20,
				this.job.minRange + ""));
		this.getTextField(2).numbersOnly = true;
		this.getTextField(2).setMinMaxDefault(2, 64, 5);
		this.addLabel(new GuiNpcLabel(4, "bard.hasoff", this.guiLeft + 60, this.guiTop + 166));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 160, this.guiTop + 161, 60, 20,
				new String[] { "gui.no", "gui.yes" }, (this.job.hasOffRange ? 1 : 0)));
		this.addLabel(new GuiNpcLabel(3, "bard.offdistance", this.guiLeft + 60, this.guiTop + 189));
		this.addTextField(new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 160, this.guiTop + 184, 40, 20,
				this.job.maxRange + ""));
		this.getTextField(3).numbersOnly = true;
		this.getTextField(3).setMinMaxDefault(2, 64, 10);
		this.getLabel(3).enabled = this.job.hasOffRange;
		this.getTextField(3).enabled = this.job.hasOffRange;
	}

	@Override
	public void save() {
		this.job.minRange = this.getTextField(2).getInteger();
		this.job.maxRange = this.getTextField(3).getInteger();
		if (this.job.minRange > this.job.maxRange) {
			this.job.maxRange = this.job.minRange;
		}
		MusicController.Instance.stopMusic();
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			this.job.song = gss.selectedResource.toString();
			this.getLabel(0).setLabel(this.job.song);
		}
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
