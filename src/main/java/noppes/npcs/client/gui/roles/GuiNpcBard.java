package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;

import javax.annotation.Nonnull;

public class GuiNpcBard extends GuiNPCInterface2 implements ITextfieldListener {

	private final JobBard job;

	public GuiNpcBard(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		job = (JobBard) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				MusicController.Instance.stopSound("", SoundCategory.MUSIC);
				MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
				setSubGui(new SubGuiSoundSelection(job.song));
				break;
			}
			case 1: {
				job.song = "";
				getTextField(1).setText("");
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
		int x = guiLeft + 56;
		int y = guiTop + 50;
		// song
		addTextField(new GuiNpcTextField(1, this, x, y, 200, 20, job.song)
				.setHoverText("bard.hover.song"));
		// select sound
		addButton(new GuiNpcButton(0, x + 205, y, 80, 20, "gui.selectSound")
				.setHoverText("bard.hover.select"));
		// del sound
		addButton(new GuiNpcButton(1, x + 289, y, 20, 20, "X")
				.setHoverText("bard.hover.del"));
		// is streamer
		addButton(new GuiNpcButton(2, x - 25, y += 30, 120, 20, new String[] { "bard.jukebox", "bard.background" }, job.isStreamer ? 0 : 1)
				.setHoverText(new TextComponentTranslation("bard.hover.range." + (job.isStreamer ? 0 : 1)).appendSibling(new TextComponentTranslation("bard.hover.range.2")).getFormattedText()));
		addButton(new GuiNpcButton(3, x + 97, y, 120, 20, new String[] { "bard.hasoff", "bard.hason" }, job.hasOffRange ? 0 : 1)
				.setHoverText("bard.hover.dist." + (job.hasOffRange ? 0 : 1)));
		addButton(new GuiNpcButton(4, x + 219, y, 120, 20, new String[] { "type.range", "parameter.position" }, job.isRange ? 0 : 1)
				.setHoverText("bard.hover.type." + job.isRange));
		addLabel(new GuiNpcLabel(0, "bard.ondistance", x, (y += 30) + 6));
		GuiNpcTextField textField;
		for (int i = 0; i < 3; i++) {
			textField = new GuiNpcTextField(2 + i, this, x + 104 + i * 44, y, 40, 20, "")
					.setMinMaxDefault(2, 64, 5);
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
		addLabel(new GuiNpcLabel(1, "bard.offdistance", x, (y += 30) + 6)
				.setIsEnable(job.hasOffRange));
		if (job.hasOffRange) {
			for (int i = 0; i < 3; i++) {
				textField = new GuiNpcTextField(5 + i, this, x + 104 + i * 44, y, 40, 20, "")
						.setMinMaxDefault(2, 256, 64);
				if (job.isRange && i == 0) {
					textField.setHoverText(new TextComponentTranslation("bard.hover.max").getFormattedText())
							.setText(job.range[1] + "");
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
	public void save() {
		if (job.range[0] > job.range[1]) { job.range[1] = job.range[0]; }
		MusicController.Instance.stopSound("", SoundCategory.MUSIC);
		MusicController.Instance.stopSound("", SoundCategory.AMBIENT);
		Client.sendData(EnumPacketServer.JobSave, job.save(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		SubGuiSoundSelection gss = (SubGuiSoundSelection) subgui;
		if (gss.selectedResource != null) { job.song = gss.selectedResource.toString(); }
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: job.song = textField.getText(); break;
			case 2: {
				if (job.isRange) { job.range[0] = textField.getInteger(); }
				else { job.minPos[0] = textField.getInteger(); }
				break;
			}
			case 3: job.minPos[1] = textField.getInteger(); break;
			case 4: job.minPos[2] = textField.getInteger(); break;
			case 5: {
				if (job.isRange) { job.range[1] = textField.getInteger(); }
				else { job.maxPos[0] = textField.getInteger(); }
				break;
			}
			case 6: job.maxPos[1] = textField.getInteger(); break;
			case 7: job.maxPos[2] = textField.getInteger(); break;
		}
	}

}
