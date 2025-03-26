package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobConversation;

public class GuiNpcConversation
extends GuiNPCInterface2
implements ITextfieldListener, GuiSelectionListener, ISubGuiListener {

	private final JobConversation job;
	private int slot = -1;

	public GuiNpcConversation(EntityNPCInterface npc) {
		super(npc);
		job = (JobConversation) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() >= 0 && button.getID() < 14) {
			slot = button.getID();
			JobConversation.ConversationLine line = job.getLine(slot);
			setSubGui(new SubGuiNpcConversationLine(line.getText(), line.getSound()));
			return;
		}
		switch (button.getID()) {
			case 51: setSubGui(new GuiQuestSelection(job.quest)); break;
			case 52: {
				job.quest = -1;
				job.questTitle = "";
				initGui();
				break;
			}
			case 53: setSubGui(new SubGuiNpcAvailability(job.availability, this)); break;
			case 55: job.mode = button.getValue(); break;
		}
	}

	@Override
	public void subGuiClosed(ISubGuiInterface gui) {
		if (gui instanceof SubGuiNpcConversationLine) {
			SubGuiNpcConversationLine sub = (SubGuiNpcConversationLine) gui;
			JobConversation.ConversationLine line = job.getLine(slot);
			line.setText(sub.line);
			line.setSound(sub.sound);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addLabel(new GuiNpcLabel(40, "gui.name", guiLeft + 40, guiTop + 4));
		addLabel(new GuiNpcLabel(41, "gui.name", guiLeft + 240, guiTop + 4));
		addLabel(new GuiNpcLabel(42, "conversation.delay", guiLeft + 164, guiTop + 4));
		addLabel(new GuiNpcLabel(43, "conversation.delay", guiLeft + 364, guiTop + 4));
		GuiNpcTextField textField;
		for (int i = 0; i < 14; ++i) {
			JobConversation.ConversationLine line = job.getLine(i);
			int offset = (i >= 7) ? 200 : 0;
			addLabel(new GuiNpcLabel(i, "" + (i + 1), guiLeft + 5 + offset - ((i > 8) ? 6 : 0), guiTop + 18 + i % 7 * 22));
			addTextField(new GuiNpcTextField(i, this, fontRenderer, guiLeft + 13 + offset, guiTop + 13 + i % 7 * 22, 100, 20, line.npc));
			addButton(new GuiNpcButton(i, guiLeft + 115 + offset, guiTop + 13 + i % 7 * 22, 46, 20, "conversation.line"));
			if (i > 0) {
				textField = new GuiNpcTextField(i + 14, this, fontRenderer, guiLeft + 164 + offset, guiTop + 13 + i % 7 * 22, 30, 20, line.delay + "");
				textField.setMinMaxDefault(5, 1000, 40);
				addTextField(textField);
			}
		}
		addLabel(new GuiNpcLabel(50, "conversation.delay", guiLeft + 202, guiTop + 175));
		textField = new GuiNpcTextField(50, this, fontRenderer, guiLeft + 260, guiTop + 170, 40, 20, job.generalDelay + "");
		textField.setMinMaxDefault(10, 1000000, 12000);
		addTextField(textField);

		addLabel(new GuiNpcLabel(54, "gui.range", guiLeft + 202, guiTop + 196));
		textField = new GuiNpcTextField(54, this, fontRenderer, guiLeft + 260, guiTop + 191, 40, 20, job.range + "");
		textField.setMinMaxDefault(4, 60, 20);
		addTextField(textField);

		addLabel(new GuiNpcLabel(51, "quest.quest", guiLeft + 13, guiTop + 175));
		String title = job.questTitle;
		if (title.isEmpty()) { title = "gui.select"; }
		addButton(new GuiNpcButton(51, guiLeft + 70, guiTop + 170, 100, 20, title));
		addButton(new GuiNpcButton(52, guiLeft + 171, guiTop + 170, 20, 20, "X"));
		addLabel(new GuiNpcLabel(53, "availability.name", guiLeft + 13, guiTop + 196));
		addButton(new GuiNpcButton(53, guiLeft + 110, guiTop + 191, 60, 20, "selectServer.edit"));
		addButton(new GuiNpcButton(55, guiLeft + 310, guiTop + 181, 96, 20, new String[] { "gui.always", "gui.playernearby" }, job.mode));
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
		Client.sendData(EnumPacketServer.JobSave, job.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void selected(int ob, String name) {
		job.quest = ob;
		job.questTitle = name;
		initGui();
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getID() >= 0 && textfield.getID() < 14) {
			JobConversation.ConversationLine line = job.getLine(textfield.getID());
			line.npc = textfield.getFullText();
		}
		if (textfield.getID() >= 14 && textfield.getID() < 28) {
			JobConversation.ConversationLine line = job.getLine(textfield.getID() - 14);
			line.delay = textfield.getInteger();
		}
		if (textfield.getID() == 50) { job.generalDelay = textfield.getInteger(); }
		if (textfield.getID() == 54) { job.range = textfield.getInteger(); }
	}

}
