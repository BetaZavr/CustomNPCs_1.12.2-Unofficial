package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.select.GuiSoundSelection;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;

public class GuiDialogEdit
extends SubGuiInterface
implements ISubGuiListener, ITextfieldListener, IGuiData, GuiYesNoCallback {

	private final Dialog dialog;
	public final GuiScreen parent;

	public GuiDialogEdit(Dialog d, GuiScreen gui) {
		setBackground("menubg.png");
		xSize = 386;
		ySize = 226;
		closeOnEsc = true;

		parent = gui;
		dialog = d;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 3: {
				setSubGui(new SubGuiNpcTextArea(dialog.text));
				break;
			}
			case 4: {
				setSubGui(new SubGuiNpcAvailability(dialog.availability, parent));
				break;
			}
			case 5: {
				setSubGui(new SubGuiNpcFactionOptions(dialog.factionOptions));
				break;
			}
			case 6: {
				setSubGui(new SubGuiNpcDialogOptions(dialog, this));
				break;
			}
			case 7: {
				setSubGui(new GuiQuestSelection(dialog.quest));
				break;
			}
			case 8: {
				dialog.quest = -1;
				initGui();
				break;
			}
			case 9: {
				setSubGui(new GuiSoundSelection(getTextField(2).getText()));
				break;
			}
			case 10: {
				setSubGui(new SubGuiNpcCommand(dialog.command));
				break;
			}
			case 11: {
				dialog.hideNPC = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 12: {
				dialog.showWheel = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 13: {
				setSubGui(new SubGuiMailmanSendSetup(dialog.mail));
				break;
			}
			case 14: {
				dialog.mail = new PlayerMail();
				initGui();
				break;
			}
			case 15: {
				dialog.disableEsc = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 16: {
				setSubGui(new GuiTextureSelection(null, dialog.texture, "png", 3));
				break;
			}
			case 17: {
				dialog.stopSound = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 18: {
				dialog.showFits = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 24: { // reset ID
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("message.change.id", "" + dialog.id).getFormattedText(),
						new TextComponentTranslation("message.change").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface) parent).setSubGui(this);
			NoppesUtil.openGUI(player, parent);
		} else {
			NoppesUtil.openGUI(player, this);
		}
		if (!result) {
			return;
		}
		if (id == 0) {
			Client.sendData(EnumPacketServer.DialogMinID, dialog.id);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (getButton(17) != null) {
			getButton(17).setEnabled(getTextField(2) != null && !getTextField(2).getText().isEmpty());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui == null) {
			drawVerticalLine(guiLeft + 196, guiTop + 24, guiTop + 159, 0xFF808080);
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 5, guiTop + 159, 0xFF808080);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (dialog == null) {
			close();
			return;
		}
		int lID = 0;
		int y = guiTop + 4;
		int x = guiLeft + 120;
		int xl = guiLeft + 4;
		// name
		addLabel(new GuiNpcLabel(lID, "gui.title", xl, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, x - 74, y + 1, 220, 18, dialog.title);
		textField.setHoverText("dialog.hover.name");
		addTextField(textField);
		// reset id
		addLabel(new GuiNpcLabel(++lID, "ID: " + dialog.id, x + 150, y + 5));
		GuiNpcButton button = new GuiNpcButton(24, x + 188, y, 50, 20, "gui.reset");
		button.setHoverText("hover.reset.id");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x + 240, y, 20, 20, "X");
		button.setHoverText("hover.back");
		addButton(button);
		// text
		addLabel(new GuiNpcLabel(++lID, "dialog.dialogues", xl, (y += 22) + 5));
		button = new GuiNpcButton(3, x, y, 50, 20, "selectServer.edit");
		button.setHoverText("dialog.hover.text");
		addButton(button);
		// availability
		addLabel(new GuiNpcLabel(++lID, "availability.options", xl, (y += 22) + 5));
		button = new GuiNpcButton(4, x, y, 50, 20, "selectServer.edit");
		button.setHoverText("availability.hover");
		addButton(button);
		// faction
		addLabel(new GuiNpcLabel(++lID, "faction.options", xl, (y += 22) + 5));
		button = new GuiNpcButton(5, x, y, 50, 20, "selectServer.edit");
		button.setHoverText("dialog.hover.faction");
		addButton(button);
		// options
		addLabel(new GuiNpcLabel(++lID, "dialog.options", xl, (y += 22) + 5));
		button = new GuiNpcButton(6, x, y, 50, 20, "selectServer.edit");
		button.setHoverText("dialog.hover.options");
		addButton(button);
		// quest
		button = new GuiNpcButton(7, xl, y += 22, 166, 20, "availability.selectquest");
		if (dialog.hasQuest()) { button.setDisplayText(dialog.getQuest().getTitle()); }
		button.setHoverText("dialog.hover.quest");
		addButton(button);
		button = new GuiNpcButton(8, xl + 168, y, 20, 20, "X");
		button.setHoverText("dialog.hover.quest.del");
		addButton(button);
		// mail
		button = new GuiNpcButton(13, xl, y += 22, 166, 20, "mailbox.setup");
		if (!dialog.mail.title.isEmpty()) { button.setDisplayText(dialog.mail.title); }
		button.setHoverText("dialog.hover.mail");
		addButton(button);
		button = new GuiNpcButton(14, xl + 168, y, 20, 20, "X");
		button.setHoverText("dialog.hover.mail.del");
		addButton(button);

		// sound
		addLabel(new GuiNpcLabel(++lID, "gui.selectSound", xl, (y += 28) + 5));
		textField = new GuiNpcTextField(2, this, fontRenderer, xl + 70, y, 252, 18, dialog.sound);
		textField.setHoverText("dialog.hover.sound");
		addTextField(textField);
		// sound select
		button = new GuiNpcButton(9, xl + 326, y - 1, 50, 20, "mco.template.button.select");
		button.setHoverText("dialog.hover.sound.del");
		addButton(button);

		// texture
		addLabel(new GuiNpcLabel(++lID, "display.texture", xl, (y += 22) + 5));
		textField = new GuiNpcTextField(4, this, fontRenderer, xl + 70, y, 252, 18, dialog.texture);
		textField.setHoverText("dialog.hover.texture");
		addTextField(textField);
		button = new GuiNpcButton(16, xl + 326, y - 1, 50, 20, "mco.template.button.select");
		button.setHoverText("dialog.hover.texture.del");
		addButton(button);

		y = guiTop + 26;
		xl = guiLeft + 200;
		x = guiLeft + 330;
		button = new GuiNpcCheckBox(11, xl, y, 180, 14, "dialog.hideNPC", null, dialog.hideNPC);
		button.setHoverText("dialog.hover.hidenpc");
		addButton(button);
		button = new GuiNpcCheckBox(12, xl, y += 16, 180, 14, "dialog.showWheel", null, dialog.showWheel);
		button.setHoverText("dialog.hover.wheel");
		addButton(button);
		button = new GuiNpcCheckBox(15, xl, y += 16, 180, 14, "dialog.disableEsc", null, dialog.disableEsc);
		button.setHoverText("dialog.hover.esc");
		addButton(button);
		button = new GuiNpcCheckBox(17, xl, y += 16, 180, 14, "dialog.sound.stop", null, dialog.stopSound);
		button.setHoverText("dialog.hover.sound.stop");
		addButton(button);
		button = new GuiNpcCheckBox(18, xl, y + 16, 180, 14, "dialog.showFits", null, dialog.showFits);
		button.setHoverText("dialog.hover.show.fits");
		addButton(button);

		// delay
		y = guiTop + 137;
		textField = new GuiNpcTextField(3, this, fontRenderer, x + 1, y, 48, 18, "" + dialog.delay);
		textField.setMinMaxDefault(0, 1200, dialog.delay);
		textField.setHoverText("dialog.hover.delay");
		addTextField(textField);
		addLabel(new GuiNpcLabel(++lID, "dialog.cooldown.time", xl, y + 4));

		// command
		addLabel(new GuiNpcLabel(++lID, "advMode.command", xl, (y -= 22) + 5));
		button = new GuiNpcButton(10, x, y, 50, 20, "selectServer.edit");
		button.setHoverText("dialog.hover.command");
		addButton(button);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		Client.sendData(EnumPacketServer.DialogSave, dialog.category.id, dialog.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("MinimumID", 3) && dialog.id != compound.getInteger("MinimumID")) {
			Client.sendData(EnumPacketServer.DialogRemove, dialog.id);
			dialog.id = compound.getInteger("MinimumID");
			Client.sendData(EnumPacketServer.DialogSave, dialog.category.id, dialog.writeToNBT(new NBTTagCompound()));
			initGui();
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcTextArea) {
			SubGuiNpcTextArea gui = (SubGuiNpcTextArea) subgui;
			dialog.text = gui.text;
		}
		if (subgui instanceof SubGuiNpcDialogOption) {
			setSubGui(new SubGuiNpcDialogOptions(dialog, this));
		}
		if (subgui instanceof SubGuiNpcCommand) {
			dialog.command = ((SubGuiNpcCommand) subgui).command;
		}
		if (subgui instanceof GuiQuestSelection) {
			GuiQuestSelection gqs = (GuiQuestSelection) subgui;
			if (gqs.selectedQuest != null) {
				dialog.quest = gqs.selectedQuest.id;
				initGui();
			}
		}
		if (subgui instanceof GuiSoundSelection) {
			GuiSoundSelection gss = (GuiSoundSelection) subgui;
			if (gss.selectedResource != null) {
				getTextField(2).setText(gss.selectedResource.toString());
				unFocused(getTextField(2));
			}
		}
		if (subgui instanceof GuiTextureSelection) {
			GuiTextureSelection gts = (GuiTextureSelection) subgui;
			if (gts.resource == null) {
				return;
			}
			dialog.texture = gts.resource.toString();
			initGui();
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField guiNpcTextField) {
		if (guiNpcTextField.getId() == 1) {
			StringBuilder t = new StringBuilder(guiNpcTextField.getText());
			boolean has = true;
			while (has) {
				has = false;
				for (Dialog dia : dialog.category.dialogs.values()) {
					if (dia.id != dialog.id && dia.title.equalsIgnoreCase(dialog.title)) {
						has = true;
						break;
					}
				}
				if (has) { t.append("_"); }
			}
			dialog.title = t.toString();
		} else if (guiNpcTextField.getId() == 2) {
			dialog.sound = guiNpcTextField.getText();
		} else if (guiNpcTextField.getId() == 3) {
			dialog.delay = guiNpcTextField.getInteger();
		}
	}

}
