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
import noppes.npcs.client.gui.select.GuiTextureSelection;
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
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch(button.id) {
			case 3: {
				this.setSubGui(new SubGuiNpcTextArea(this.dialog.text));
				break;
			}
			case 4: {
				this.setSubGui(new SubGuiNpcAvailability(this.dialog.availability));
				break;
			}
			case 5: {
				this.setSubGui(new SubGuiNpcFactionOptions(this.dialog.factionOptions));
				break;
			}
			case 6: {
				this.setSubGui(new SubGuiNpcDialogOptions(this.dialog));
				break;
			}
			case 7: {
				this.setSubGui(new GuiQuestSelection(this.dialog.quest));
				break;
			}
			case 8: {
				this.dialog.quest = -1;
				this.initGui();
				break;
			}
			case 9: {
				this.setSubGui(new GuiSoundSelection(this.getTextField(2).getText()));
				break;
			}
			case 10: {
				this.setSubGui(new SubGuiNpcCommand(this.dialog.command));
				break;
			}
			case 11: {
				this.dialog.hideNPC = (button.getValue() == 1);
				break;
			}
			case 12: {
				this.dialog.showWheel = (button.getValue() == 1);
				break;
			}
			case 13: {
				this.setSubGui(new SubGuiMailmanSendSetup(this.dialog.mail));
				break;
			}
			case 14: {
				this.dialog.mail = new PlayerMail();
				this.initGui();
				break;
			}
			case 15: {
				this.dialog.disableEsc = (button.getValue() == 1);
				break;
			}
			case 16: {
				this.setSubGui(new GuiTextureSelection(null, this.dialog.texture, "png", 3));
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.dialog==null) {
			this.close();
			return;
		}
		this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 4, this.guiTop + 8));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 46, this.guiTop + 3, 220, 20, this.dialog.title));
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
		
		this.addLabel(new GuiNpcLabel(9, "gui.selectSound", this.guiLeft + 4, this.guiTop + 142));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, this.guiLeft + 64, this.guiTop + 138, 262, 18, this.dialog.sound));
		this.addLabel(new GuiNpcLabel(18, "display.texture", this.guiLeft + 4, this.guiTop + 164));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, this.guiLeft + 64, this.guiTop + 160, 262, 18, this.dialog.texture));
		
		this.addButton(new GuiNpcButton(9, this.guiLeft + 330, this.guiTop + 137, 50, 20, "mco.template.button.select"));
		this.addButton(new GuiNpcButton(16, this.guiLeft + 330, this.guiTop + 159, 50, 20, "mco.template.button.select"));
		
		this.addButton(new GuiNpcButton(13, this.guiLeft + 4, this.guiTop + 182, 164, 20, "mailbox.setup"));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 170, this.guiTop + 182, 20, 20, "X"));
		if (!this.dialog.mail.subject.isEmpty()) {
			this.getButton(13).setDisplayText(this.dialog.mail.subject);
		}
		int y = this.guiTop + 4;
		y += 22;
		this.addButton(new GuiNpcButton(10, this.guiLeft + 330, y, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(10, "advMode.command", this.guiLeft + 214, y + 5));
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(11, this.guiLeft + 330, y, this.dialog.hideNPC));
		this.addLabel(new GuiNpcLabel(11, "dialog.hideNPC", this.guiLeft + 214, y + 5));
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(12, this.guiLeft + 330, y, this.dialog.showWheel));
		this.addLabel(new GuiNpcLabel(12, "dialog.showWheel", this.guiLeft + 214, y + 5));
		y += 22;
		this.addButton(new GuiNpcButtonYesNo(15, this.guiLeft + 330, y, this.dialog.disableEsc));
		this.addLabel(new GuiNpcLabel(15, "dialog.disableEsc", this.guiLeft + 214, y + 5));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 362, this.guiTop + 4, 20, 20, "X"));
		y += 23;
		// new
		GuiNpcTextField textField = new GuiNpcTextField(3, this, this.fontRenderer, this.guiLeft + 331, y, 48, 18, ""+this.dialog.delay);
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 1200, this.dialog.delay);
		this.addTextField(textField);
		this.addLabel(new GuiNpcLabel(16, "dialog.cooldown.time", this.guiLeft + 214, y + 5));
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
		if (subgui instanceof GuiTextureSelection) {
			GuiTextureSelection gts = (GuiTextureSelection) subgui;
			if (gts.resource==null) { return; }
			this.dialog.texture = gts.resource.toString();
			System.out.println("CNPCs: "+this.dialog.texture);
			this.initGui();
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
		else if (guiNpcTextField.getId() == 2) {
			this.dialog.sound = guiNpcTextField.getText();
		}
		else if (guiNpcTextField.getId() == 3) {
			this.dialog.delay = guiNpcTextField.getInteger();
		}
	}
	
}
