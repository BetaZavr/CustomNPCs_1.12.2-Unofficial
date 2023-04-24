package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.GuiSelectionListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeDialog
extends SubGuiInterface
implements GuiSelectionListener, IGuiData, ITextfieldListener {
	
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
		this.ySize = 96;
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
		switch(button.id) {
			case 0: {
				if (this.task.getTargetID() <= 0) {
					NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
				} else {
					if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
						((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
						((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
					}
				}
				this.close();
				break;
			}
			case 1: {
				this.setSubGui(new GuiDialogSelection(this.task.getTargetID()));
				break;
			}
			case 2: {
				this.task.setTargetID(0);
				this.initGui();
				break;
			}
			case 10: {
				if (this.task == null) { return; }
				this.task.pos = this.mc.player.getPosition();
				this.task.dimensionID = this.mc.player.world.provider.getDimension();
				this.initGui();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.subgui == null && this.mc.renderEngine != null) {
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
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		// New
		if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posx").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posy").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(12)!=null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.posz").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(13)!=null && this.getTextField(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.range").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(14)!=null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("parameter.dimensionId").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.add").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.del").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").getFormattedText());
		} 
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(0, this.guiLeft + 10, this.guiTop + 72, 98, 20, "gui.back"));
		// New
		String title = "dialog.selectoption";
		if (!this.data.isEmpty()) { title = this.data; }
		this.addButton(new GuiNpcButton(1, this.guiLeft + 34, this.guiTop + 15, 210, 20, title));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 10, this.guiTop + 15, 20, 20, "X"));
		String tx = "Quest Dialog Setup";
		this.addLabel(new GuiNpcLabel(0, tx, (this.width - this.mc.fontRenderer.getStringWidth(tx)) / 2, this.guiTop + 4, 0));
		
		this.addLabel(new GuiNpcLabel(10, "quest.task.pos.set", this.guiLeft + 6, this.guiTop + 37));
		this.addLabel(new GuiNpcLabel(11, "X:", this.guiLeft + 40, this.guiTop + 46));
		this.addTextField(new GuiNpcTextField(10, this, this.fontRenderer, this.guiLeft + 30, this.guiTop + 56, 25, 13, ""+this.task.pos.getX()));
		this.getTextField(10).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(12, "Y:", this.guiLeft + 68, this.guiTop + 46));
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 58, this.guiTop + 56, 25, 13, ""+this.task.pos.getY()));
		this.getTextField(11).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(13, "Z:", this.guiLeft + 96, this.guiTop + 46));
		this.addTextField(new GuiNpcTextField(12, this, this.fontRenderer, this.guiLeft + 86, this.guiTop + 56, 25, 13, ""+this.task.pos.getZ()));
		this.getTextField(12).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(14, "DimID:", this.guiLeft + 116, this.guiTop + 46));
		this.addTextField(new GuiNpcTextField(13, this, this.fontRenderer, this.guiLeft + 114, this.guiTop + 56, 25, 13, ""+this.task.dimensionID));
		this.getTextField(13).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(15, "Range:", this.guiLeft + 145, this.guiTop + 46));
		this.addTextField(new GuiNpcTextField(14, this, this.fontRenderer, this.guiLeft + 142, this.guiTop + 56, 25, 13, ""+this.task.rangeCompass));
		this.getTextField(14).numbersOnly = true;
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);
		this.addButton(new GuiNpcButton(10, this.guiLeft+186, this.guiTop + 72, 60, 20, "gui.set"));
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

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.task == null) { return; }
		switch(textField.getId()) {
			case 10: {
				int y = this.task.pos.getY();
				int z = this.task.pos.getZ();
				this.task.pos = new BlockPos(textField.getInteger(), y, z);
				break;
			}
			case 11: {
				int x = this.task.pos.getX();
				int z = this.task.pos.getZ();
				this.task.pos = new BlockPos(x, textField.getInteger(), z);
				break;
			}
			case 12: {
				int x = this.task.pos.getX();
				int y = this.task.pos.getY();
				this.task.pos = new BlockPos(x, y, textField.getInteger());
				break;
			}
			case 13: {
				int dim = textField.getInteger();
				if (!DimensionManager.isDimensionRegistered(dim)) {
					textField.setText(""+this.task.dimensionID);
					return;
				}
				this.task.dimensionID = textField.getInteger();
				break;
			}
			case 14: {
				this.task.rangeCompass = textField.getInteger();
				break;
			}
		}
	}
}
