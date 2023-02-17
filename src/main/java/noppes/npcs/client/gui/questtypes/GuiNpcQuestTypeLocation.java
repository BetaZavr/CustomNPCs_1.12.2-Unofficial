package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeLocation extends SubGuiInterface implements ITextfieldListener {
	public GuiScreen parent;
	// private Quest quest; // Changed
	// New
	private QuestObjective task;

	public GuiNpcQuestTypeLocation(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		// this.quest = NoppesUtilServer.getEditingQuest((EntityPlayer)this.player); //
		// Changed
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Location Setup";
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
		// New
		this.task = task;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 0) {
			this.close();
		}
	}

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null) {
			return;
		}
		if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 70, 180, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 150, this.guiTop + 190, 98, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.loct.block").getFormattedText(),
				this.guiLeft + 6, this.guiTop + 50));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70, 180, 20,
				this.task.getTargetName())); // Changed
		this.addButton(new GuiNpcButton(0, this.guiLeft + 150, this.guiTop + 190, 98, 20, "gui.back"));
	}

	@Override
	public void save() {
		this.task.setTargetName(this.getTextField(0).getText());
		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != EnumQuestTask.LOCATION) {
				continue;
			}
			if (task.getTargetName().equals(this.task.getTargetName())) {
				this.getTextField(0).setText("");
				this.task.setTargetName("");
				break;
			}
		}
		if (this.task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
				((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
			}
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		this.task.setTargetName(textfield.getText()); // Changed
	}

}
