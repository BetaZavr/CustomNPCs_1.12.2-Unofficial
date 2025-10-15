package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiMailmanSendSetup;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.SubGuiNpcCommand;
import noppes.npcs.client.gui.SubGuiNpcFactionOptions;
import noppes.npcs.client.gui.SubGuiNpcTextArea;
import noppes.npcs.client.gui.select.SubGuiQuestSelection;
import noppes.npcs.client.gui.select.SubGuiSoundSelection;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class SubGuiDialogEdit extends SubGuiInterface
		implements ITextfieldListener, IGuiData, GuiYesNoCallback {

	protected final Dialog dialog;

	// New from Unofficial (BetaZavr)
	public final GuiScreen parent;

	public SubGuiDialogEdit(EntityNPCInterface npcIn, Dialog dialogIn, GuiScreen gui) {
		super(0, npcIn);
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 386;
		ySize = 226;

		dialog = dialogIn;
		parent = gui;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 3: setSubGui(new SubGuiNpcTextArea(0, dialog.text)); break;
			case 4: setSubGui(new SubGuiNpcAvailability(dialog.availability, parent)); break;
			case 5: setSubGui(new SubGuiNpcFactionOptions(dialog.factionOptions)); break;
			case 6: setSubGui(new SubGuiNpcDialogOptions(npc, dialog, this)); break;
			case 7: setSubGui(new SubGuiQuestSelection(dialog.quest)); break;
			case 8: dialog.quest = -1; initGui(); break;
			case 9: setSubGui(new SubGuiSoundSelection(getTextField(2).getText())); break;
			case 10: setSubGui(new SubGuiNpcCommand(dialog.command)); break;
			case 11: dialog.hideNPC = ((GuiNpcCheckBox) button).isSelected(); break;
			case 12: dialog.showWheel = ((GuiNpcCheckBox) button).isSelected(); break;
			case 13: setSubGui(new SubGuiMailmanSendSetup(dialog.mail)); break;
			case 14: dialog.mail = new PlayerMail(); initGui(); break;
			case 15: dialog.disableEsc = ((GuiNpcCheckBox) button).isSelected(); break;
			case 16: setSubGui(new SubGuiTextureSelection(0, null, dialog.texture, "png", 3)); break;
			case 17: dialog.stopSound = ((GuiNpcCheckBox) button).isSelected(); break;
			case 18: dialog.showFits = ((GuiNpcCheckBox) button).isSelected(); break;
			case 24: {
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("message.change.id", "" + dialog.id).getFormattedText(),
						new TextComponentTranslation("message.change").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
				break;
			} // reset ID
			case 66: onClosed(); break;
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface) parent).setSubGui(this);
			NoppesUtil.openGUI(player, parent);
		}
		else { NoppesUtil.openGUI(player, this); }
		if (!result) { return; }
		if (id == 0) { Client.sendData(EnumPacketServer.DialogMinID, dialog.id); }
	}

	@Override
	public void initGui() {
		super.initGui();
		if (dialog == null) { onClosed(); return; }
		int lID = 0;
		int y = guiTop + 4;
		int x = guiLeft + 120;
		int xl = guiLeft + 4;
		// name
		addLabel(new GuiNpcLabel(lID, "gui.title", xl, y + 5));
		addTextField(new GuiNpcTextField(1, this, x - 74, y + 1, 220, 18, dialog.title)
				.setHoverText("dialog.hover.name"));
		// reset id
		addLabel(new GuiNpcLabel(++lID, "ID: " + dialog.id, x + 150, y + 5));
		addButton(new GuiNpcButton(24, x + 188, y, 50, 20, "gui.reset")
				.setHoverText("hover.reset.id"));
		// exit
		addButton(new GuiNpcButton(66, x + 240, y, 20, 20, "X")
				.setHoverText("hover.back"));
		// text
		addLabel(new GuiNpcLabel(++lID, "dialog.dialogtext", xl, (y += 22) + 5));
		addButton(new GuiNpcButton(3, x, y, 50, 20, "selectServer.edit")
				.setHoverText("dialog.hover.text"));
		// availability
		addLabel(new GuiNpcLabel(++lID, "availability.options", xl, (y += 22) + 5));
		addButton(new GuiNpcButton(4, x, y, 50, 20, "selectServer.edit")
				.setHoverText("availability.hover"));
		// faction
		addLabel(new GuiNpcLabel(++lID, "faction.options", xl, (y += 22) + 5));
		addButton(new GuiNpcButton(5, x, y, 50, 20, "selectServer.edit")
				.setHoverText("dialog.hover.faction"));
		// options
		addLabel(new GuiNpcLabel(++lID, "dialog.options", xl, (y += 22) + 5));
		addButton(new GuiNpcButton(6, x, y, 50, 20, "selectServer.edit")
				.setHoverText("dialog.hover.options"));
		// quest
		addButton(new GuiNpcButton(7, xl, y += 22, 166, 20, "availability.selectquest")
				.setHoverText("dialog.hover.quests"));
		if (dialog.hasQuest()) { getButton(7).setDisplayText(dialog.getQuest().getTitle()); }
		addButton(new GuiNpcButton(8, xl + 168, y, 20, 20, "X")
				.setHoverText("dialog.hover.quests.del"));
		// mail
		addButton(new GuiNpcButton(13, xl, y += 22, 166, 20, "mailbox.setup")
				.setHoverText("dialog.hover.mail"));
		if (!dialog.mail.title.isEmpty()) { getButton(13).setDisplayText(dialog.mail.title); }
		addButton(new GuiNpcButton(14, xl + 168, y, 20, 20, "X")
				.setHoverText("dialog.hover.mail.del"));
		// sound
		addLabel(new GuiNpcLabel(++lID, "gui.selectSound", xl, (y += 28) + 5));
		addTextField(new GuiNpcTextField(2, this, xl + 70, y, 252, 18, dialog.sound)
				.setHoverText("dialog.hover.sound"));
		// sound select
		addButton(new GuiNpcButton(9, xl + 326, y - 1, 50, 20, "mco.template.button.select")
				.setHoverText("dialog.hover.sound.del"));
		// texture
		addLabel(new GuiNpcLabel(++lID, "display.texture", xl, (y += 22) + 5));
		addTextField(new GuiNpcTextField(4, this, xl + 70, y, 252, 18, dialog.texture)
				.setHoverText("dialog.hover.texture"));
		addButton(new GuiNpcButton(16, xl + 326, y - 1, 50, 20, "mco.template.button.select")
				.setHoverText("dialog.hover.texture.del"));
		y = guiTop + 26;
		xl = guiLeft + 200;
		x = guiLeft + 330;
		addButton(new GuiNpcCheckBox(11, xl, y, 180, 14, "dialog.hideNPC", null, dialog.hideNPC)
				.setHoverText("dialog.hover.hidenpc"));
		addButton(new GuiNpcCheckBox(12, xl, y += 16, 180, 14, "dialog.showWheel", null, dialog.showWheel)
				.setHoverText("dialog.hover.wheel"));
		addButton(new GuiNpcCheckBox(15, xl, y += 16, 180, 14, "dialog.disableEsc", null, dialog.disableEsc)
				.setHoverText("dialog.hover.esc"));
		addButton(new GuiNpcCheckBox(17, xl, y += 16, 180, 14, "dialog.sound.stop", null, dialog.stopSound)
				.setHoverText("dialog.hover.sound.stop"));
		addButton(new GuiNpcCheckBox(18, xl, y + 16, 180, 14, "dialog.showFits", null, dialog.showFits)
				.setHoverText("dialog.hover.show.fits"));
		// delay
		y = guiTop + 137;
		addTextField(new GuiNpcTextField(3, this, x + 1, y, 48, 18, "" + dialog.delay)
				.setMinMaxDefault(0, 1200, dialog.delay)
				.setHoverText("dialog.hover.delay"));
		addLabel(new GuiNpcLabel(++lID, "dialog.cooldown.time", xl, y + 4));
		// command
		addLabel(new GuiNpcLabel(++lID, "advMode.command", xl, (y -= 22) + 5));
		addButton(new GuiNpcButton(10, x, y, 50, 20, "selectServer.edit")
				.setHoverText("dialog.hover.command"));
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		Client.sendData(EnumPacketServer.DialogSave, dialog.category.id, dialog.save(new NBTTagCompound()));
	}

	@Override
	public void subGuiClosed(GuiScreen subgui) {
		if (subgui instanceof SubGuiNpcTextArea) { dialog.text = ((SubGuiNpcTextArea) subgui).text; }
		else if (subgui instanceof SubGuiNpcDialogOption) { setSubGui(new SubGuiNpcDialogOptions(npc, dialog, this)); }
		else if (subgui instanceof SubGuiNpcCommand) { dialog.command = ((SubGuiNpcCommand) subgui).command; }
		else if (subgui instanceof SubGuiQuestSelection) {
			SubGuiQuestSelection gui = (SubGuiQuestSelection) subgui;
			if (gui.selectedQuest != null) {
				dialog.quest = gui.selectedQuest.id;
				initGui();
			}
		}
		else if (subgui instanceof SubGuiSoundSelection) {
			SubGuiSoundSelection gss = (SubGuiSoundSelection) subgui;
			if (gss.selectedResource != null) {
				getTextField(2).setText(gss.selectedResource.toString());
				unFocused(getTextField(2));
			}
		}
		else if (subgui instanceof SubGuiTextureSelection) {
			SubGuiTextureSelection gts = (SubGuiTextureSelection) subgui;
			if (gts.resource == null) { return; }
			dialog.texture = gts.resource.toString();
			initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: {
				StringBuilder t = new StringBuilder(textField.getText());
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
				break;
			}
			case 2: dialog.sound = textField.getText(); break;
			case 3: dialog.delay = textField.getInteger(); break;
			case 4: dialog.texture = textField.getText(); break;
		}
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (getButton(17) != null) {
			getButton(17).setIsEnable(getTextField(2) != null && !getTextField(2).getText().isEmpty());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui == null) {
			drawVerticalLine(guiLeft + 196, guiTop + 24, guiTop + 159, 0xFF808080);
			drawHorizontalLine(guiLeft + 4, guiLeft + xSize - 5, guiTop + 159, 0xFF808080);
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("MinimumID", 3) && dialog.id != compound.getInteger("MinimumID")) {
			Client.sendData(EnumPacketServer.DialogRemove, dialog.id);
			dialog.id = compound.getInteger("MinimumID");
			Client.sendData(EnumPacketServer.DialogSave, dialog.category.id, dialog.save(new NBTTagCompound()));
			initGui();
		}
	}

}
