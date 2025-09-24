package noppes.npcs.client.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.player.GuiMailmanWrite;
import noppes.npcs.client.gui.select.SubGuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.data.PlayerMail;

import javax.annotation.Nonnull;

public class SubGuiMailmanSendSetup extends SubGuiInterface
		implements ITextfieldListener, GuiSelectionListener {

	protected final PlayerMail mail;

	public SubGuiMailmanSendSetup(PlayerMail mailIn) {
		super(0);
		setBackground("menubg.png");
		xSize = 256;

		mail = mailIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				onClosed();
				break;
			}
			case 1: {
				mail.questId = -1;
				mail.message = new NBTTagCompound();
				onClosed();
				break;
			}
			case 2: {
				GuiMailmanWrite.parent = getParent();
				GuiMailmanWrite.mail = mail;
				Client.sendData(EnumPacketServer.MailOpenSetup, mail.writeNBT());
				break;
			}
			case 3: setSubGui(new SubGuiQuestSelection(mail.questId)); break;
			case 4: {
				mail.questId = -1;
				initGui();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// title
		addLabel(new GuiNpcLabel(1, "mailbox.subject", guiLeft + 4, guiTop + 19));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 60, guiTop + 14, 180, 20, mail.title));
		// sender
		addLabel(new GuiNpcLabel(0, "mailbox.sender", guiLeft + 4, guiTop + 41));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 60, guiTop + 36, 180, 20, mail.sender));
		// write
		addButton(new GuiNpcButton(2, guiLeft + 29, guiTop + 100, "mailbox.write"));
		// quest
		addLabel(new GuiNpcLabel(3, "quest.quest", guiLeft + 13, guiTop + 135));
		IQuest quest = mail.getQuest();
		String title = "gui.select";
		if (quest != null) { title = quest.getName(); }
		addButton(new GuiNpcButton(3, guiLeft + 70, guiTop + 130, 100, 20, title));
		// del
		addButton(new GuiNpcButton(4, guiLeft + 171, guiTop + 130, 20, 20, "X"));
		// exit
		addButton(new GuiNpcButton(0, guiLeft + 26, guiTop + 190, 100, 20, "gui.done"));
		// cancel
		addButton(new GuiNpcButton(1, guiLeft + 130, guiTop + 190, 100, 20, "gui.cancel"));
		if (player.openContainer instanceof ContainerMail) {
			ContainerMail container = (ContainerMail) player.openContainer;
			mail.items = container.mail.items;
		}
	}

    @Override
	public void selected(int ob, String name) {
		mail.questId = ob;
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getID() == 0) { mail.sender = textField.getText(); }
		else if (textField.getID() == 1) { mail.title = textField.getText(); }
	}

}
