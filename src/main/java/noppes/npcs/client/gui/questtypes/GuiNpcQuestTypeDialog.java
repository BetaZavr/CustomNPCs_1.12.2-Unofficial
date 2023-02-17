package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeDialog extends SubGuiInterface implements GuiSelectionListener, IGuiData {
	// private Quest quest; // Changed
	private String data = ""; // Changed
	public GuiScreen parent;
	// private int selectedSlot; Changed
	// New
	private QuestObjective task;

	public GuiNpcQuestTypeDialog(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		// this.quest = NoppesUtilServer.getEditingQuest(this.player); // Changed
		this.npc = npc;
		this.parent = parent;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 70;
		this.closeOnEsc = false;
		this.task = task; // New
		IDialog d = DialogController.instance.get(task.getTargetID());
		if (d != null) {
			this.data = d.getName();
		}
		Client.sendData(EnumPacketServer.QuestDialogGetTitle, this.task.getTargetID());

	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 0) {
			if (this.task.getTargetID() <= 0) {
				NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
			} else {
				if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
					((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
					((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
				}
			}
			this.close();
		}
		if (button.id == 1) {
			this.setSubGui(new GuiDialogSelection(this.task.getTargetID()));
		}
		if (button.id == 2) {
			this.task.setTargetID(-1);
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null) {
			return;
		}
		if (this.mc.renderEngine != null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop + this.ySize - 1, 0.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.mc.renderEngine.bindTexture(this.background);
			if (this.xSize > 256) {
				this.drawTexturedModalRect(0, 0, 0, 214, 250, 4);
				this.drawTexturedModalRect(250, 0, 256 - (this.xSize - 250), 214, this.xSize - 250, 4);
			} else {
				this.drawTexturedModalRect(0, 0, 0, 214, this.xSize, 4);
			}
			GlStateManager.popMatrix();
		}
		this.drawCenteredString(this.fontRenderer, "Quest Dialog Setup", this.width / 2, this.ySize + 18, 0xFFFFFFFF);
		// New
		if (isMouseHover(mouseX, mouseY, this.guiLeft + 34, this.guiTop + 15, 210, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.add").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 10, this.guiTop + 15, 20, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.del").getFormattedText());
		} else if (isMouseHover(mouseX, mouseY, this.guiLeft + 10, this.guiTop + 37, 98, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 10, this.guiTop + 37, 98, 20, "gui.back"));
		// New
		String title = "dialog.selectoption";
		if (!this.data.isEmpty()) {
			title = this.data;
		}
		this.addButton(new GuiNpcButton(1, this.guiLeft + 34, this.guiTop + 15, 210, 20, title));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 10, this.guiTop + 15, 20, 20, "X"));
	}

	@Override
	public void selected(int id, String name) {
		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != EnumQuestTask.DIALOG) {
				continue;
			}
			if (task.getTargetID() == id) {
				return;
			}
		}
		this.task.setTargetID(id);
		this.data = name;
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) { // Changed
		this.data = "";
		if (compound.hasKey("Title", 8)) {
			this.data = compound.getString("Title");
		}
		this.initGui();
	}

}
