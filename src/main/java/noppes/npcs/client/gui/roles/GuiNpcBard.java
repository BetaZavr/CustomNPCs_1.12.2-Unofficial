package noppes.npcs.client.gui.roles;

import java.util.Arrays;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;

public class GuiNpcBard extends GuiNPCInterface2 implements ISubGuiListener, ITextfieldListener {

	private final JobBard job;

	public GuiNpcBard(EntityNPCInterface npc) {
		super(npc);
		this.job = (JobBard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			MusicController.Instance.stopSound("", SoundCategory.MUSIC);
			MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
			this.setSubGui(new GuiSoundSelection(this.job.song));
			break;
		}
		case 1: {
			this.job.song = "";
			this.getTextField(1).setText("");
			MusicController.Instance.stopSound("", SoundCategory.MUSIC);
			MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
			break;
		}
		case 2: {
			this.job.isStreamer = button.getValue() == 0;
			this.initGui();
			break;
		}
		case 3: {
			this.job.hasOffRange = button.getValue() == 0;
			this.initGui();
			break;
		}
		case 4: {
			this.job.isRange = button.getValue() == 0;
			this.initGui();
			break;
		}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.select").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.del").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.range." + this.getButton(2).getValue())
					.getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("bard.hover.dist." + this.getButton(3).getValue()).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.type." + this.job.isRange).getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("bard.hover.song").getFormattedText());
		} else if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			if (this.job.isRange) {
				this.setHoverText(new TextComponentTranslation("bard.hover.min").getFormattedText());
			} else {
				this.setHoverText(
						new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>"))
								.appendSibling(new TextComponentTranslation("hover.scale.x")).getFormattedText());
			}
		} else if (this.getTextField(3) != null && this.getTextField(3).enabled && this.getTextField(3).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>"))
							.appendSibling(new TextComponentTranslation("hover.scale.y")).getFormattedText());
		} else if (this.getTextField(4) != null && this.getTextField(4).enabled && this.getTextField(4).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>"))
							.appendSibling(new TextComponentTranslation("hover.scale.z")).getFormattedText());
		} else if (this.getTextField(5) != null && this.getTextField(5).enabled && this.getTextField(5).isMouseOver()) {
			if (this.job.isRange) {
				this.setHoverText(new TextComponentTranslation("bard.hover.max").getFormattedText());
			} else {
				this.setHoverText(
						new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>"))
								.appendSibling(new TextComponentTranslation("hover.scale.x")).getFormattedText());
			}
		} else if (this.getTextField(6) != null && this.getTextField(6).enabled && this.getTextField(6).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>"))
							.appendSibling(new TextComponentTranslation("hover.scale.y")).getFormattedText());
		} else if (this.getTextField(7) != null && this.getTextField(7).enabled && this.getTextField(7).isMouseOver()) {
			this.setHoverText(
					new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>"))
							.appendSibling(new TextComponentTranslation("hover.scale.z")).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = 56, y = 50;
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + x, this.guiTop + y, 200, 20,
				this.job.song));
		this.addButton(new GuiNpcButton(0, this.guiLeft + x + 205, this.guiTop + y, 80, 20, "gui.selectSound"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + x + 289, this.guiTop + y, 20, 20, "X"));
		y += 30;
		this.addButton(new GuiNpcButton(2, this.guiLeft + x - 25, this.guiTop + y, 120, 20,
				new String[] { "bard.jukebox", "bard.background" }, this.job.isStreamer ? 0 : 1));
		this.addButton(new GuiNpcButton(3, this.guiLeft + x + 97, this.guiTop + y, 120, 20,
				new String[] { "bard.hasoff", "bard.hason" }, this.job.hasOffRange ? 0 : 1));
		this.addButton(new GuiNpcButton(4, this.guiLeft + x + 219, this.guiTop + y, 120, 20,
				new String[] { "type.range", "parameter.position" }, this.job.isRange ? 0 : 1));
		y += 30;
		this.addLabel(new GuiNpcLabel(0, "bard.ondistance", this.guiLeft + x, this.guiTop + y + 6));
		for (int i = 0; i < 3; i++) {
			this.addTextField(new GuiNpcTextField(2 + i, this, this.fontRenderer, this.guiLeft + x + 104 + i * 44,
					this.guiTop + y, 40, 20, ""));
			this.getTextField(2 + i).setNumbersOnly();
			this.getTextField(2 + i).setMinMaxDefault(2, 64, 5);
			if (this.job.isRange && i == 0) {
				this.getTextField(2 + i).setText(this.job.range[0] + "");
			} else {
				this.getTextField(2 + i).setText(this.job.minPos[i] + "");
				this.getTextField(2 + i).enabled = !this.job.isRange;
			}
		}
		y += 30;
		this.addLabel(new GuiNpcLabel(1, "bard.offdistance", this.guiLeft + x, this.guiTop + y + 6));
		this.getLabel(1).enabled = this.job.hasOffRange;
		if (this.job.hasOffRange) {
			for (int i = 0; i < 3; i++) {
				this.addTextField(new GuiNpcTextField(5 + i, this, this.fontRenderer, this.guiLeft + x + 104 + i * 44,
						this.guiTop + y, 40, 20, ""));
				this.getTextField(5 + i).setNumbersOnly();
				this.getTextField(5 + i).setMinMaxDefault(2, 256, 64);
				if (this.job.isRange && i == 0) {
					this.getTextField(5 + i).setText(this.job.range[1] + "");
				} else {
					this.getTextField(5 + i).setText(this.job.maxPos[i] + "");
					this.getTextField(5 + i).enabled = !this.job.isRange;
				}
			}
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

	@Override
	public void save() {
		if (this.job.range[0] > this.job.range[1]) {
			this.job.range[1] = this.job.range[0];
		}
		MusicController.Instance.stopSound("", SoundCategory.MUSIC);
		MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
		Client.sendData(EnumPacketServer.JobSave, this.job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) {
			this.job.song = gss.selectedResource.toString();
		}
		this.initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getId()) {
		case 1: {
			this.job.song = textField.getText();
			break;
		}
		case 2: {
			if (this.job.isRange) {
				this.job.range[0] = textField.getInteger();
			} else {
				this.job.minPos[0] = textField.getInteger();
			}
			break;
		}
		case 3: {
			this.job.minPos[1] = textField.getInteger();
			break;
		}
		case 4: {
			this.job.minPos[2] = textField.getInteger();
			break;
		}
		case 5: {
			if (this.job.isRange) {
				this.job.range[1] = textField.getInteger();
			} else {
				this.job.maxPos[0] = textField.getInteger();
			}
			break;
		}
		case 6: {
			this.job.maxPos[1] = textField.getInteger();
			break;
		}
		case 7: {
			this.job.maxPos[2] = textField.getInteger();
			break;
		}
		}
	}
}
