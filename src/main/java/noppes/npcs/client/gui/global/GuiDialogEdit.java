package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;

public class GuiDialogEdit extends SubGuiInterface implements ISubGuiListener, ITextfieldListener {
	private Dialog dialog;

	public GuiDialogEdit(Dialog dialog) {
		this.dialog = dialog;
		this.setBackground("menubg.png");
		this.xSize = 386;
		this.ySize = 226;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		int id = guibutton.id;
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (id == 3) {
			this.setSubGui(new SubGuiNpcTextArea(this.dialog.text));
		}
		if (id == 4) {
			this.setSubGui(new SubGuiNpcAvailability(this.dialog.availability));
		}
		if (id == 5) {
			this.setSubGui(new SubGuiNpcFactionOptions(this.dialog.factionOptions));
		}
		if (id == 6) {
			this.setSubGui(new SubGuiNpcDialogOptions(this.dialog));
		}
		if (id == 7) {
			this.setSubGui(new GuiQuestSelection(this.dialog.quest));
		}
		if (id == 8) {
			this.dialog.quest = -1;
			this.initGui();
		}
		if (id == 9) {
			this.setSubGui(new GuiSoundSelection(this.getTextField(2).getText()));
		}
		if (id == 10) {
			this.setSubGui(new SubGuiNpcCommand(this.dialog.command));
		}
		if (id == 11) {
			this.dialog.hideNPC = (button.getValue() == 1);
		}
		if (id == 12) {
			this.dialog.showWheel = (button.getValue() == 1);
		}
		if (id == 15) {
			this.dialog.disableEsc = (button.getValue() == 1);
		}
		if (id == 13) {
			this.setSubGui(new SubGuiMailmanSendSetup(this.dialog.mail));
		}
		if (id == 14) {
			this.dialog.mail = new PlayerMail();
			this.initGui();
		}
		if (id == 66) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 4, this.guiTop + 8));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 46, this.guiTop + 3, 220, 20,
				this.dialog.title));
		this.addLabel(new GuiNpcLabel(0, "ID", this.guiLeft + 268, this.guiTop + 4));
		this.addLabel(new GuiNpcLabel(2, this.dialog.id + "", this.guiLeft + 268, this.guiTop + 14));
		this.addLabel(new GuiNpcLabel(3, "dialog.dialogtext", this.guiLeft + 4, this.guiTop + 30));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 120, this.guiTop + 25, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(4, "availability.options", this.guiLeft + 4, this.guiTop + 51));
		this.addButton(new GuiNpcButton(4, this.guiLeft + 120, this.guiTop + 46, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(5, "faction.options", this.guiLeft + 4, this.guiTop + 72));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 120, this.guiTop + 67, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(6, "dialog.options", this.guiLeft + 4, this.guiTop + 93));
		this.addButton(new GuiNpcButton(6, this.guiLeft + 120, this.guiTop + 89, 50, 20, "selectServer.edit"));
		this.addButton(new GuiNpcButton(7, this.guiLeft + 4, this.guiTop + 114, 144, 20, "availability.selectquest"));
		this.addButton(new GuiNpcButton(8, this.guiLeft + 150, this.guiTop + 114, 20, 20, "X"));
		if (this.dialog.hasQuest()) {
			this.getButton(7).setDisplayText(this.dialog.getQuest().getTitle()); // Changed
		}
		this.addLabel(new GuiNpcLabel(9, "gui.selectSound", this.guiLeft + 4, this.guiTop + 138));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 148, 264, 20,
				this.dialog.sound));
		this.addButton(
				new GuiNpcButton(9, this.guiLeft + 270, this.guiTop + 148, 60, 20, "mco.template.button.select"));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 4, this.guiTop + 172, 164, 20, "mailbox.setup"));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 170, this.guiTop + 172, 20, 20, "X"));
		if (!this.dialog.mail.subject.isEmpty()) {
			this.getButton(13).setDisplayText(this.dialog.mail.subject);
		}
		int y = this.guiTop + 4;
		int i = 10;
		int j = this.guiLeft + 330;
		y += 22;
		this.addButton(new GuiNpcButton(i, j, y, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(10, "advMode.command", this.guiLeft + 214, y + 5));
		int id = 11;
		int x = this.guiLeft + 330;
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(id, x, y, this.dialog.hideNPC));
		this.addLabel(new GuiNpcLabel(11, "dialog.hideNPC", this.guiLeft + 214, y + 5));
		int id2 = 12;
		int x2 = this.guiLeft + 330;
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(id2, x2, y, this.dialog.showWheel));
		this.addLabel(new GuiNpcLabel(12, "dialog.showWheel", this.guiLeft + 214, y + 5));
		int id3 = 15;
		int x3 = this.guiLeft + 330;
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(id3, x3, y, this.dialog.disableEsc));
		this.addLabel(new GuiNpcLabel(15, "dialog.disableEsc", this.guiLeft + 214, y + 5));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 362, this.guiTop + 4, 20, 20, "X"));
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		Client.sendData(EnumPacketServer.DialogSave, this.dialog.category.id,
				this.dialog.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			this.dialog.text = gui.text;
		}
		if (subgui instanceof SubGuiNpcDialogOption) {
			this.setSubGui(new SubGuiNpcDialogOptions(this.dialog));
		}
		if (subgui instanceof SubGuiNpcCommand) {
			this.dialog.command = ((SubGuiNpcCommand) subgui).command;
		}
		if (subgui instanceof GuiQuestSelection) {
			GuiQuestSelection gqs = (GuiQuestSelection) subgui;
			if (gqs.selectedQuest != null) {
				this.dialog.quest = gqs.selectedQuest.id;
				this.initGui();
			}
		}
		if (subgui instanceof GuiSoundSelection) {
			GuiSoundSelection gss = (GuiSoundSelection) subgui;
			if (gss.selectedResource != null) {
				this.getTextField(2).setText(gss.selectedResource.toString());
				this.unFocused(this.getTextField(2));
			}
		}
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		if (guiNpcTextField.getId() == 1) {
			this.dialog.title = guiNpcTextField.getText();
			while (DialogController.instance.containsDialogName(this.dialog.category, this.dialog)) {
				StringBuilder sb = new StringBuilder();
				Dialog dialog = this.dialog;
				dialog.title = sb.append(dialog.title).append("_").toString();
			}
		}
		if (guiNpcTextField.getId() == 2) {
			this.dialog.sound = guiNpcTextField.getText();
		}
	}
}
