package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;

public class GuiNpcBard
extends GuiNPCInterface2
implements ISubGuiListener, ITextfieldListener {

	private final JobBard job;

	public GuiNpcBard(EntityNPCInterface npc) {
		super(npc);
		job = (JobBard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: {
				MusicController.Instance.stopSound("", SoundCategory.MUSIC);
				MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
				setSubGui(new GuiSoundSelection(job.song));
				break;
			}
			case 1: {
				job.song = "";
				getTextField(1).setFullText("");
				MusicController.Instance.stopSound("", SoundCategory.MUSIC);
				MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
				break;
			}
			case 2: {
				job.isStreamer = button.getValue() == 0;
				initGui();
				break;
			}
			case 3: {
				job.hasOffRange = button.getValue() == 0;
				initGui();
				break;
			}
			case 4: {
				job.isRange = button.getValue() == 0;
				initGui();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = 56, y = 50;
		// song
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, guiLeft + x, guiTop + y, 200, 20, job.song);
		textField.setHoverText("bard.hover.song");
		addTextField(textField);
		// select sound
		GuiNpcButton button = new GuiNpcButton(0, guiLeft + x + 205, guiTop + y, 80, 20, "gui.selectSound");
		button.setHoverText("bard.hover.select");
		addButton(button);
		// del sound
		button = new GuiNpcButton(1, guiLeft + x + 289, guiTop + y, 20, 20, "X");
		button.setHoverText("bard.hover.del");
		addButton(button);

		y += 30;
		// is streamer
		button = new GuiNpcButton(2, guiLeft + x - 25, guiTop + y, 120, 20, new String[] { "bard.jukebox", "bard.background" }, job.isStreamer ? 0 : 1);
		button.setHoverText(new TextComponentTranslation("bard.hover.range." + (job.isStreamer ? 0 : 1)).appendSibling(new TextComponentTranslation("bard.hover.range.2")).getFormattedText());
		addButton(button);
		button = new GuiNpcButton(3, guiLeft + x + 97, guiTop + y, 120, 20, new String[] { "bard.hasoff", "bard.hason" }, job.hasOffRange ? 0 : 1);
		button.setHoverText("bard.hover.dist." + (job.hasOffRange ? 0 : 1));
		addButton(button);
		button = new GuiNpcButton(4, guiLeft + x + 219, guiTop + y, 120, 20, new String[] { "type.range", "parameter.position" }, job.isRange ? 0 : 1);
		button.setHoverText("bard.hover.type." + job.isRange);
		addButton(button);

		y += 30;
		addLabel(new GuiNpcLabel(0, "bard.ondistance", guiLeft + x, guiTop + y + 6));
		for (int i = 0; i < 3; i++) {
			textField = new GuiNpcTextField(2 + i, this, fontRenderer, guiLeft + x + 104 + i * 44, guiTop + y, 40, 20, "");
			textField.setMinMaxDefault(2, 64, 5);
			if (job.isRange && i == 0) {
				textField.setText(job.range[0] + "");
				textField.setHoverText("bard.hover.min");
			}
			else {
				textField.setText(job.minPos[i] + "");
				textField.enabled = !job.isRange;
				if (i == 0) { textField.setHoverText(new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.x")).getFormattedText()); }
				else if (i == 1) { textField.setHoverText(new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.y")).getFormattedText()); }
				else { textField.setHoverText(new TextComponentTranslation("bard.hover.min").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.z")).getFormattedText()); }
			}
			addTextField(textField);
		}

		y += 30;
		addLabel(new GuiNpcLabel(1, "bard.offdistance", guiLeft + x, guiTop + y + 6));
		getLabel(1).setEnabled(job.hasOffRange);
		if (job.hasOffRange) {
			for (int i = 0; i < 3; i++) {
				textField = new GuiNpcTextField(5 + i, this, fontRenderer, guiLeft + x + 104 + i * 44, guiTop + y, 40, 20, "");
				textField.setMinMaxDefault(2, 256, 64);
				if (job.isRange && i == 0) {
					textField.setText(job.range[1] + "");
					textField.setHoverText(new TextComponentTranslation("bard.hover.max").getFormattedText());
				} else {
					textField.setText(job.maxPos[i] + "");
					textField.enabled = !job.isRange;
					if (i == 0) { textField.setHoverText(new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.x")).getFormattedText()); }
					else if (i == 1) { textField.setHoverText(new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.y")).getFormattedText()); }
					else { textField.setHoverText(new TextComponentTranslation("bard.hover.max").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("hover.scale.z")).getFormattedText()); }
				}
				addTextField(textField);
			}
		}
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
		if (job.range[0] > job.range[1]) { job.range[1] = job.range[0]; }
		MusicController.Instance.stopSound("", SoundCategory.MUSIC);
		MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		GuiSoundSelection gss = (GuiSoundSelection) subgui;
		if (gss.selectedResource != null) { job.song = gss.selectedResource.toString(); }
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: {
				job.song = textField.getFullText();
				break;
			}
			case 2: {
				if (job.isRange) {
					job.range[0] = textField.getInteger();
				} else {
					job.minPos[0] = textField.getInteger();
				}
				break;
			}
			case 3: {
				job.minPos[1] = textField.getInteger();
				break;
			}
			case 4: {
				job.minPos[2] = textField.getInteger();
				break;
			}
			case 5: {
				if (job.isRange) {
					job.range[1] = textField.getInteger();
				} else {
					job.maxPos[0] = textField.getInteger();
				}
				break;
			}
			case 6: {
				job.maxPos[1] = textField.getInteger();
				break;
			}
			case 7: {
				job.maxPos[2] = textField.getInteger();
				break;
			}
		}
	}

}
