package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.SubGuiQuestEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class SubGuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener {

	protected final QuestObjective task;
	protected final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public SubGuiNpcQuestTypeManual(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(0, npc);
		setBackground("menubg.png");
		closeOnEsc = true;
		title = new TextComponentTranslation("quest.title.manual").getFormattedText();
		xSize = 214;
		ySize = 217;

		parent = gui;
		task = taskObj;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0 || task == null) { return; }
		switch (button.getID()) {
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) { return; }
				task.dimensionID = dataDimIDs.get(button.getValue());
				button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim", "" + task.dimensionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 5: task.setPointOnMiniMap(((GuiNpcCheckBox) button).isSelected()); break;
			case 10: {
				task.pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
				task.dimensionID = mc.player.world.provider.getDimension();
				initGui();
				break;
			}
			case 11: Client.sendData(EnumPacketServer.TeleportTo, task.dimensionID, task.pos.getX(), task.pos.getY(), task.pos.getZ()); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		// Back
		if (subgui == null) {
			int u = guiLeft + xSize - 1;
			int v = guiTop;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(background);
			drawTexturedModalRect(u, v, 252, 0, 4, ySize);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 6;
		int y = guiTop + 50;
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("quest.manual.names").getFormattedText(), x, y));
		// target
		addTextField(new GuiNpcTextField(0, this, x, y += 12, 180, 20, task.getTargetName())
				.setHoverText("quest.hover.edit.kill.name"));
		// max progress
		addTextField(new GuiNpcTextField(1, this, x + 182, y, 24, 20, task.getMaxProgress() + "")
				.setMinMaxDefault(1, Integer.MAX_VALUE, 1)
				.setHoverText("quest.hover.edit.kill.value", "" + Integer.MAX_VALUE));
		// X
		addLabel(new GuiNpcLabel(lId++, "quest.task.pos.set", x, y += 24));
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 12) + 2));
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		addTextField(new GuiNpcTextField(10, this, x + 8, y, 40, 14, "" + task.pos.getX())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getX())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText()));
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 52, y + 2));
		addTextField(new GuiNpcTextField(11, this, x + 60, y, 40, 14, "" + task.pos.getY())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText()));
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 104, y + 2));
		addTextField(new GuiNpcTextField(12, this, x + 112, y, 40, 14, "" + task.pos.getZ())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText()));
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x + 156, y + 2));
		addTextField(new GuiNpcTextField(14, this, x + 164, y, 41, 14, "" + task.rangeCompass)
				.setMinMaxDefault(0, 64, task.rangeCompass)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText()));
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
		addButton(new GuiNpcButton(4, x + 8, y - 1, 30, 16, dimIDs, p)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText()));
		// region ID
		addLabel(new GuiNpcLabel(lId, "P:", x + 40, y + 2));
		addTextField(new GuiNpcTextField(9, this, x + 47, y, 32, 14, "" + task.regionID)
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.regionID)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", task.regionID).appendSibling(compass).getFormattedText()));
		// N
		addLabel(new GuiNpcLabel(lId, "N:", x + 81, y + 2));
		addTextField(new GuiNpcTextField(15, this, x + 89, y, 116, 14, task.entityName)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText()));
		// set player pos
		addButton(new GuiNpcButton(10, x + 146, y += 19, 30, 20, "gui.set")
				.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText()));
		// tp
		addButton(new GuiNpcButton(11, x + 124, y, 20, 20, "TP")
				.setHoverText("hover.teleport"));
		// mini map point
		addButton(new GuiNpcCheckBox(5, x, y, 121, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap())
				.setHoverText("quest.hover.set.minimap.point"));
		// exit
		addButton(new GuiNpcButton(66, x, guiTop + ySize - 25, 98, 20, "gui.back")
				.setHoverText("hover.back"));
	}

	@Override
	public void save() {
		task.setTargetName(getTextField(0).getText());
		task.setMaxProgress(getTextField(1).getInteger());
		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != EnumQuestTask.MANUAL) { continue; }
			if (taskObj.getTargetName().equals(task.getTargetName())) {
				getTextField(0).setText("");
				task.setTargetName("");
				task.setMaxProgress(1);
				break;
			}
		}
		if (task.getTargetName().isEmpty()) { NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task); }
		else if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof SubGuiQuestEdit) {
			SubGuiQuestEdit subgui = (SubGuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui;
			subgui.setSubGui(null);
			subgui.initGui();
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getID()) {
			case 0: task.setTargetName(textField.getText()); break;
			case 1: task.setMaxProgress(textField.getInteger()); break;
			case 2: task.setAreaRange(textField.getInteger()); break;
			case 9: {
				if (!BorderController.getInstance().regions.containsKey(textField.getInteger())) {
					textField.setText("" + textField.def);
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
			case 15: task.entityName = textField.getText(); break;
		}
	}

}
