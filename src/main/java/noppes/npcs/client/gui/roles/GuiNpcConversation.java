package noppes.npcs.client.gui.roles;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcAvailability;
import noppes.npcs.client.gui.select.GuiQuestSelection;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobConversation;

public class GuiNpcConversation
extends GuiNPCInterface2
implements ITextfieldListener, GuiSelectionListener {

	private final JobConversation job;
	private int slot = -1;

	public GuiNpcConversation(EntityNPCInterface npc) {
		super(npc);
		job = (JobConversation) npc.advanced.jobInterface;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id >= 0 && button.id < 14) {
			slot = button.id;
			JobConversation.ConversationLine line = job.getLine(slot);
			setSubGui(new SubGuiNpcConversationLine(line.getText(), line.getSound()));
		}
		if (button.id == 51) {
			setSubGui(new GuiQuestSelection(job.quest));
		}
		if (button.id == 52) {
			job.quest = -1;
			job.questTitle = "";
			initGui();
		}
		if (button.id == 53) {
			setSubGui(new SubGuiNpcAvailability(job.availability, this));
		}
		if (button.id == 55) {
			job.mode = button.getValue();
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
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
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() >= 0 && textfield.getId() < 14) {
			JobConversation.ConversationLine line = job.getLine(textfield.getId());
			line.npc = textfield.getText();
		}
		if (textfield.getId() >= 14 && textfield.getId() < 28) {
			JobConversation.ConversationLine line = job.getLine(textfield.getId() - 14);
			line.delay = textfield.getInteger();
		}
		if (textfield.getId() == 50) {
			job.generalDelay = textfield.getInteger();
		}
		if (textfield.getId() == 54) {
			job.range = textfield.getInteger();
		}
	}

}
