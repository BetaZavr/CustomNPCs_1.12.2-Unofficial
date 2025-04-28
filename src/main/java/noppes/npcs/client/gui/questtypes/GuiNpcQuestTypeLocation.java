package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeLocation
extends SubGuiInterface
implements ITextfieldListener {

	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public GuiNpcQuestTypeLocation(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(npc);
		setBackground("menubg.png");
		title = new TextComponentTranslation("quest.title.location").getFormattedText();
		xSize = 256;
		ySize = 216;
		closeOnEsc = true;
		parent = gui;

		task = taskObj;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof GuiNpcButton)) {
			super.actionPerformed(guibutton);
			return;
		}
		if (task == null) { return; }
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) { return; }
				task.dimensionID = dataDimIDs.get(button.getValue());
				button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim", "" + task.dimensionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 5: {
				task.setPointOnMiniMap(((GuiNpcCheckBox) guibutton).isSelected());
				break;
			}
			case 10: {
				task.pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
				task.dimensionID = mc.player.world.provider.getDimension();
				initGui();
				break;
			}
			case 11: {
				Client.sendData(EnumPacketServer.TeleportTo, task.dimensionID, task.pos.getX(), task.pos.getY(), task.pos.getZ());
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 6;
		int y = guiTop + 50;
		// target
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("quest.loct.block").getFormattedText(), x, y));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x, y += 12, 244, 18, task.getTargetName());
		textField.setHoverText("quest.hover.edit.kill.name");
		addTextField(textField);
		// X
		addLabel(new GuiNpcLabel(lId++, "quest.task.pos.set", x, y += 24));
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 12) + 2));
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		textField = new GuiNpcTextField(10, this, fontRenderer, x + 8, y, 40, 14, "" + task.pos.getX());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getX());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 63, y + 2));
		textField = new GuiNpcTextField(11, this, fontRenderer, x + 70, y, 40, 14, "" + task.pos.getY());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 127, y + 2));
		textField = new GuiNpcTextField(12, this, fontRenderer, x + 135, y, 40, 14, "" + task.pos.getZ());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x + 191, y + 2));
		textField = new GuiNpcTextField(14, this, fontRenderer, x + 199, y, 45, 14, "" + task.rangeCompass);
		textField.setMinMaxDefault(0, 64, task.rangeCompass);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// dim ID
		addLabel(new GuiNpcLabel(lId++, "D:", x, (y += 20) + 2));
		int p = 0, i = 0;
		List<Integer> ids = Arrays.asList(DimensionManager.getStaticDimensionIDs());
		Collections.sort(ids);
		String[] dimIDs = new String[ids.size()];
		for (int id : ids) {
			dimIDs[i] = id + "";
			dataDimIDs.put(i, id);
			if (id == task.dimensionID) { p = i; }
			i++;
		}
		GuiNpcButton button = new GuiNpcButton(4, x + 8, y - 1, 30, 16, dimIDs, p);
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText());
		addButton(button);
		// region ID
		addLabel(new GuiNpcLabel(lId, "P:", x + 40, y + 2));
		textField = new GuiNpcTextField(9, this, fontRenderer, x + 47, y, 32, 14, "" + task.regionID);
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.regionID);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", task.regionID).appendSibling(compass).getFormattedText());
		addTextField(textField);
		// N
		addLabel(new GuiNpcLabel(lId, "N:", x + 81, y + 2));
		textField = new GuiNpcTextField(15, this, fontRenderer, x + 89, y, 155, 14, task.entityName);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// set player pos
		button = new GuiNpcButton(10, x + 185, y += 19, 60, 20, "gui.set");
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText());
		addButton(button);
		// tp
		button = new GuiNpcButton(11, x + 163, y, 20, 20, "TP");
		button.setHoverText("hover.teleport");
		addButton(button);
		// mini map point
		button = new GuiNpcCheckBox(5, x, y, 160, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap());
		button.setHoverText("quest.hover.set.minimap.point");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize - 25, 98, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void save() {
		task.setTargetName(getTextField(0).getFullText());
		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != EnumQuestTask.LOCATION) {
				continue;
			}
			if (taskObj.getTargetName().equals(task.getTargetName())) {
				getTextField(0).setFullText("");
				task.setTargetName("");
				break;
			}
		}
		if (task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				GuiQuestEdit subgui = (GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui;
				subgui.setSubGui(null);
				subgui.initGui();
			}
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getID()) {
			case 0: task.setTargetName(textField.getFullText()); break;
			case 2: task.setAreaRange(textField.getInteger()); break;
			case 9: {
				if (!BorderController.getInstance().regions.containsKey(textField.getInteger())) {
					textField.setFullText("" + textField.getDefault());
					return;
				}
				task.regionID = textField.getInteger();
				textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", "" + task.regionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 10: task.pos = new BlockPos(textField.getInteger(), task.pos.getY(), task.pos.getZ()); break;
			case 11: task.pos = new BlockPos(task.pos.getX(), textField.getInteger(), task.pos.getZ()); break;
			case 12: task.pos = new BlockPos(task.pos.getX(), task.pos.getY(), textField.getInteger()); break;
			case 14: task.rangeCompass = textField.getInteger(); break;
			case 15: task.entityName = textField.getFullText(); break;
		}
	}

}
