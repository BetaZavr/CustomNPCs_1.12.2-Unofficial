package noppes.npcs.client.gui.global;

import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.PlayerMail;

public class GuiDialogEdit
extends SubGuiInterface
implements ISubGuiListener, ITextfieldListener, IGuiData, GuiYesNoCallback {
	
	private Dialog dialog;

	public GuiDialogEdit(Dialog dialog) {
		this.dialog = dialog;
		this.setBackground("menubg.png");
		this.xSize = 386;
		this.ySize = 226;
		this.closeOnEsc = true;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.dialog==null) {
			this.close();
			return;
		}
		int lID = 0;
		int y = this.guiTop + 4;
		int x = this.guiLeft + 120;
		int xl = this.guiLeft + 4;
		this.addLabel(new GuiNpcLabel(lID, "gui.title", xl, y + 5));
		
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, x - 74, y + 1, 220, 18, this.dialog.title));
		this.addLabel(new GuiNpcLabel(++lID, "ID: "+this.dialog.id, x + 150, y + 5));
		
		this.addButton(new GuiNpcButton(24, x + 188, y, 50, 20, "gui.reset"));
		
		this.addButton(new GuiNpcButton(66, x + 240, y, 20, 20, "X"));
		
		this.addLabel(new GuiNpcLabel(++lID, "dialog.dialogtext", xl, (y += 22) + 5));
		this.addButton(new GuiNpcButton(3, x, y, 50, 20, "selectServer.edit"));
		
		this.addLabel(new GuiNpcLabel(++lID, "availability.options", xl, (y += 22) + 5));
		this.addButton(new GuiNpcButton(4, x, y, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(++lID, "faction.options", xl, (y += 22) + 5));
		this.addButton(new GuiNpcButton(5, x, y, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(++lID, "dialog.options", xl, (y += 22) + 5));
		this.addButton(new GuiNpcButton(6, x, y, 50, 20, "selectServer.edit"));
		this.addButton(new GuiNpcButton(7, xl, y += 22, 166, 20, "availability.selectquest"));
		if (this.dialog.hasQuest()) { this.getButton(7).setDisplayText(this.dialog.getQuest().getTitle()); }
		this.addButton(new GuiNpcButton(8, xl + 168, y, 20, 20, "X"));
		this.addButton(new GuiNpcButton(13, xl, y += 22, 166, 20, "mailbox.setup"));
		if (!this.dialog.mail.title.isEmpty()) { this.getButton(13).setDisplayText(this.dialog.mail.title); }
		this.addButton(new GuiNpcButton(14, xl + 168, y, 20, 20, "X"));
		
		this.addLabel(new GuiNpcLabel(++lID, "gui.selectSound", xl, (y += 28) + 5));
		this.addTextField(new GuiNpcTextField(2, this, this.fontRenderer, xl + 70, y, 252, 18, this.dialog.sound));
		this.addButton(new GuiNpcButton(9, xl + 326, y - 1, 50, 20, "mco.template.button.select"));
		this.addLabel(new GuiNpcLabel(++lID, "display.texture", xl, (y += 22) + 5));
		this.addTextField(new GuiNpcTextField(4, this, this.fontRenderer, xl + 70, y, 252, 18, this.dialog.texture));
		this.addButton(new GuiNpcButton(16, xl + 326, y - 1, 50, 20, "mco.template.button.select"));
		
		y = this.guiTop + 26;
		xl = this.guiLeft + 200;
		x = this.guiLeft + 330;
		this.addButton(new GuiNpcCheckBox(11, xl, y, 180, 14, "dialog.hideNPC", this.dialog.hideNPC));
		this.addButton(new GuiNpcCheckBox(12, xl, y += 16, 180, 14, "dialog.showWheel", this.dialog.showWheel));
		this.addButton(new GuiNpcCheckBox(15, xl, y += 16, 180, 14, "dialog.disableEsc", this.dialog.disableEsc));
		this.addButton(new GuiNpcCheckBox(17, xl, y += 16, 180, 14, "dialog.sound.stop", this.dialog.stopSound));
		this.addButton(new GuiNpcCheckBox(18, xl, y += 16, 180, 14, "dialog.showFits", this.dialog.showFits));
		
		y = this.guiTop + 137;
		GuiNpcTextField textField = new GuiNpcTextField(3, this, this.fontRenderer, x + 1, y, 48, 18, ""+this.dialog.delay);
		textField.setNumbersOnly();
		textField.setMinMaxDefault(0, 1200, this.dialog.delay);
		this.addTextField(textField);
		this.addLabel(new GuiNpcLabel(++lID, "dialog.cooldown.time", xl, y + 4));

		this.addButton(new GuiNpcButton(10, x, y -= 22, 50, 20, "selectServer.edit"));
		this.addLabel(new GuiNpcLabel(++lID, "advMode.command", xl, y + 5));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.getButton(17) != null) { this.getButton(17).setEnabled(this.getTextField(2) != null && !this.getTextField(2).getText().isEmpty()); }
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui ==null) {
			this.drawVerticalLine(this.guiLeft + 196, this.guiTop + 24, this.guiTop + 159, 0xFF808080);
			this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + this.xSize - 5, this.guiTop + 159, 0xFF808080);
		}
		if (this.subgui !=null || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.name").getFormattedText());
		} else if (this.getTextField(2)!=null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.sound").getFormattedText());
		} else if (this.getTextField(3)!=null && this.getTextField(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.delay").getFormattedText());
		} else if (this.getTextField(4)!=null && this.getTextField(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.texture").getFormattedText());
		}
		else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.text").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availabitily.hover").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.faction").getFormattedText());
		} else if (this.getButton(6)!=null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.options").getFormattedText());
		} else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.quets").getFormattedText());
		} else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.quets.del").getFormattedText());
		} else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.sound.del").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.command").getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.hidenpc").getFormattedText());
		} else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.wheel").getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.mail").getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.mail.del").getFormattedText());
		} else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.esc").getFormattedText());
		} else if (this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.texture.del").getFormattedText());
		} else if (this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.sound.stop").getFormattedText());
		} else if (this.getButton(18)!=null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("dialog.hover.show.fits").getFormattedText());
		} else if (this.getButton(24)!=null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.reset.id").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
	@Override
	public void buttonEvent(GuiNpcButton button) {
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
				this.dialog.hideNPC = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 12: {
				this.dialog.showWheel = ((GuiNpcCheckBox) button).isSelected();
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
				this.dialog.disableEsc = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 16: {
				this.setSubGui(new GuiTextureSelection(null, this.dialog.texture, "png", 3));
				break;
			}
			case 17: {
				this.dialog.stopSound = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 18: {
				this.dialog.showFits = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 24: { // reset ID
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("message.change.id", ""+this.dialog.id).getFormattedText(), new TextComponentTranslation("message.change").getFormattedText(), 0);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 66: {
				this.close();
				break;
			}
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		if (this.parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface) this.parent).setSubGui(this);
			NoppesUtil.openGUI((EntityPlayer) this.player, this.parent);
		}
		else { NoppesUtil.openGUI((EntityPlayer) this.player, this); }
		if (!result) { return; }
		if (id == 0) { Client.sendData(EnumPacketServer.DialogMinID, this.dialog.id); }
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		Client.sendData(EnumPacketServer.DialogSave, this.dialog.category.id, this.dialog.writeToNBT(new NBTTagCompound()));
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

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("MinimumID", 3) && this.dialog.id != compound.getInteger("MinimumID")) {
			Client.sendData(EnumPacketServer.DialogRemove, this.dialog.id);
			this.dialog.id = compound.getInteger("MinimumID");
			Client.sendData(EnumPacketServer.DialogSave, this.dialog.category.id, this.dialog.writeToNBT(new NBTTagCompound()));
			this.initGui();
		}
	}
	
}
