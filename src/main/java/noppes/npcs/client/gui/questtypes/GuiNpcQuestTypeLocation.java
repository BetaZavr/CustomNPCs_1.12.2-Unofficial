package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
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

public class GuiNpcQuestTypeLocation
extends SubGuiInterface
implements ITextfieldListener {
	
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
		switch(guibutton.id) {
			case 0: {
				this.close();
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

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
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
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").getFormattedText());
		} 
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.loct.block").getFormattedText(), this.guiLeft + 6, this.guiTop + 50));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 70, 180, 20, this.task.getTargetName())); // Changed
		this.addButton(new GuiNpcButton(0, this.guiLeft + 150, this.guiTop + 190, 98, 20, "gui.back"));
		
		this.addLabel(new GuiNpcLabel(10, "quest.task.pos.set", this.guiLeft + 6, this.guiTop + 98));
		this.addLabel(new GuiNpcLabel(11, "X:", this.guiLeft + 40, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(10, this, this.fontRenderer, this.guiLeft + 30, this.guiTop + 117, 25, 13, ""+this.task.pos.getX()));
		this.getTextField(10).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(12, "Y:", this.guiLeft + 68, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 58, this.guiTop + 117, 25, 13, ""+this.task.pos.getY()));
		this.getTextField(11).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(13, "Z:", this.guiLeft + 96, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(12, this, this.fontRenderer, this.guiLeft + 86, this.guiTop + 117, 25, 13, ""+this.task.pos.getZ()));
		this.getTextField(12).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(14, "DimID:", this.guiLeft + 116, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(13, this, this.fontRenderer, this.guiLeft + 114, this.guiTop + 117, 25, 13, ""+this.task.dimensionID));
		this.getTextField(13).numbersOnly = true;
		this.addLabel(new GuiNpcLabel(15, "Range:", this.guiLeft + 145, this.guiTop + 107));
		this.addTextField(new GuiNpcTextField(14, this, this.fontRenderer, this.guiLeft + 142, this.guiTop + 117, 25, 13, ""+this.task.rangeCompass));
		this.getTextField(14).numbersOnly = true;
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);
		this.addButton(new GuiNpcButton(10, this.guiLeft+172, this.guiTop + 114, 60, 20, "gui.set"));
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
	public void unFocused(GuiNpcTextField textField) {
		if (this.task == null) { return; }
		switch(textField.getId()) {
			case 0: {
				this.task.setTargetName(textField.getText()); // Changed
				break;
			}
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
