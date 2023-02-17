package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
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

public class GuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener {
	public GuiScreen parent;
	// private Quest quest; Changed
	// public GuiNpcTextField lastSelected; Changed
	// New
	private QuestObjective task;

	public GuiNpcQuestTypeManual(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		// this.quest = NoppesUtilServer.getEditingQuest((EntityPlayer)this.player); //
		// Changed
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Manual Setup";
		this.setBackground("menubg.png");
		this.xSize = 214;
		this.ySize = 217;
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
		// Back
		int u = this.guiLeft + this.xSize - 1;
		int v = this.guiTop;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.background);
		this.drawTexturedModalRect(u, v, 252, 0, 4, this.ySize);
		super.drawScreen(i, j, f);

		if (this.subgui != null) {
			return;
		}
		if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 70, 180, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 186, this.guiTop + 70, 24, 20)) {
			this.setHoverText(
					new TextComponentTranslation("quest.hover.edit.kill.value", "10000000").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 140, 98, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		// int i = 0;
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.manual.names").getFormattedText(),
				this.guiLeft + 6, this.guiTop + 50));
		/*
		 * Changed for (String name : this.quest.manuals.keySet()) {
		 * this.addTextField(new GuiNpcTextField(i, this, this.fontRenderer,
		 * this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20, name));
		 * this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer,
		 * this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20,
		 * this.quest.manuals.get(name) + "")); this.getTextField(i + 3).numbersOnly =
		 * true; this.getTextField(i + 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		 * ++i; } while (i < 3) { this.addTextField(new GuiNpcTextField(i, this,
		 * this.fontRenderer, this.guiLeft + 4, this.guiTop + 70 + i * 22, 180, 20,
		 * "")); this.addTextField(new GuiNpcTextField(i + 3, this, this.fontRenderer,
		 * this.guiLeft + 186, this.guiTop + 70 + i * 22, 24, 20, "1"));
		 * this.getTextField(i + 3).numbersOnly = true; this.getTextField(i +
		 * 3).setMinMaxDefault(1, Integer.MAX_VALUE, 1); ++i; }
		 */
		// New
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70, 180, 20,
				this.task.getTargetName()));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 186, this.guiTop + 70, 24, 20,
				this.task.getMaxProgress() + ""));
		this.getTextField(1).numbersOnly = true;
		this.getTextField(1).setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 140, 98, 20, "gui.back"));
	}

	@Override
	public void save() {
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());

		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != EnumQuestTask.MANUAL) {
				continue;
			}
			if (task.getTargetName().equals(this.task.getTargetName())) {
				this.getTextField(0).setText("");
				this.task.setTargetName("");
				this.task.setMaxProgress(1);
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
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		// Changed
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());
	}

}
