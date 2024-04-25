package noppes.npcs.client.gui.questtypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

public class GuiNpcQuestTypeLocation extends SubGuiInterface implements ITextfieldListener {

	public GuiScreen parent;
	private QuestObjective task;
	private Map<Integer, Integer> dataDimIDs = Maps.<Integer, Integer>newHashMap();

	public GuiNpcQuestTypeLocation(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Location Setup";
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
		this.closeOnEsc = true;
		this.task = task;
	}

	@Override
	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 10: {
			if (this.task == null) {
				return;
			}
			this.task.pos = new BlockPos(Math.floor(this.mc.player.posX), Math.floor(this.mc.player.posY),
					Math.floor(this.mc.player.posZ));
			this.task.dimensionID = this.mc.player.world.provider.getDimension();
			this.initGui();
			break;
		}
		case 4: {
			if (!dataDimIDs.containsKey(button.getValue())) {
				return;
			}
			this.task.dimensionID = dataDimIDs.get(button.getValue());
			break;
		}
		case 5: {
			this.task.setPointOnMiniMap(((GuiNpcCheckBox) guibutton).isSelected());
			break;
		}
		case 11: {
			if (this.task == null) {
				return;
			}
			Client.sendData(EnumPacketServer.TeleportTo, new Object[] { this.task.dimensionID, this.task.pos.getX(),
					this.task.pos.getY(), this.task.pos.getZ() });
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(10) != null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(11) != null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(12) != null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(13) != null && this.getTextField(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.dim")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(14) != null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.range")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(15) != null && this.getTextField(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.entity")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport").getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 6;
		int y = this.guiTop + 50;
		this.addLabel(new GuiNpcLabel(0, new TextComponentTranslation("quest.loct.block").getFormattedText(), x, y));
		this.addTextField(
				new GuiNpcTextField(0, this, this.fontRenderer, x, y += 12, 244, 18, this.task.getTargetName()));

		this.addLabel(new GuiNpcLabel(10, "quest.task.pos.set", x, y += 24));
		this.addLabel(new GuiNpcLabel(11, "X:", x, (y += 12) + 2));
		this.addTextField(
				new GuiNpcTextField(10, this, this.fontRenderer, x + 8, y, 40, 14, "" + this.task.pos.getX()));
		this.getTextField(10).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(12, "Y:", x + 63, y + 2));
		this.addTextField(
				new GuiNpcTextField(11, this, this.fontRenderer, x + 70, y, 40, 14, "" + this.task.pos.getY()));
		this.getTextField(11).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(13, "Z:", x + 127, y + 2));
		this.addTextField(
				new GuiNpcTextField(12, this, this.fontRenderer, x + 135, y, 40, 14, "" + this.task.pos.getZ()));
		this.getTextField(12).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(15, "R:", x + 191, y + 2));
		this.addTextField(
				new GuiNpcTextField(14, this, this.fontRenderer, x + 199, y, 45, 14, "" + this.task.rangeCompass));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);

		this.addLabel(new GuiNpcLabel(14, "D:", x, (y += 20) + 2));
		int p = 0, i = 0;
		List<Integer> ids = Lists.newArrayList(DimensionManager.getStaticDimensionIDs());
		Collections.sort(ids);
		String[] dimIDs = new String[ids.size()];
		for (int id : ids) {
			dimIDs[i] = id + "";
			dataDimIDs.put(i, id);
			if (id == this.task.dimensionID) {
				p = i;
			}
			i++;
		}
		this.addButton(new GuiButtonBiDirectional(4, x + 8, y - 1, 60, 16, dimIDs, p));
		this.addLabel(new GuiNpcLabel(15, "N:", x + 71, y + 2));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, x + 79, y, 165, 14, this.task.entityName));

		this.addButton(new GuiNpcButton(10, x + 185, y += 19, 60, 20, "gui.set"));
		this.addButton(new GuiNpcButton(11, x + 163, y, 20, 20, "TP"));
		this.addButton(
				new GuiNpcCheckBox(5, x, y, 160, 16, "quest.set.minimap.point", this.task.isSetPointOnMiniMap()));

		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize - 25, 98, 20, "gui.back"));
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
		if (this.task == null) {
			return;
		}
		switch (textField.getId()) {
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
		case 14: {
			this.task.rangeCompass = textField.getInteger();
			break;
		}
		case 15: {
			this.task.entityName = textField.getText();
			break;
		}
		}
	}

}
