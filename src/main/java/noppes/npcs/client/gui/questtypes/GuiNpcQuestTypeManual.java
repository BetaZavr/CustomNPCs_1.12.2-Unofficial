package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
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
import noppes.npcs.controllers.BorderController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeManual
extends SubGuiInterface
implements ITextfieldListener {

	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public GuiNpcQuestTypeManual(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(npc);
		setBackground("menubg.png");
		title = new TextComponentTranslation("quest.title.manual").getFormattedText();
		xSize = 214;
		ySize = 217;
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
		switch (guibutton.id) {
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
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x, y += 12, 180, 20, task.getTargetName());
		textField.setHoverText("quest.hover.edit.kill.name");
		addTextField(textField);
		// max progress
		textField = new GuiNpcTextField(1, this, fontRenderer, x + 182, y, 24, 20, task.getMaxProgress() + "");
		textField.setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		textField.setHoverText("quest.hover.edit.kill.value", "" + Integer.MAX_VALUE);
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
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 52, y + 2));
		textField = new GuiNpcTextField(11, this, fontRenderer, x + 60, y, 40, 14, "" + task.pos.getY());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 104, y + 2));
		textField = new GuiNpcTextField(12, this, fontRenderer, x + 112, y, 40, 14, "" + task.pos.getZ());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x + 156, y + 2));
		textField = new GuiNpcTextField(14, this, fontRenderer, x + 164, y, 41, 14, "" + task.rangeCompass);
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
		GuiNpcButton button = new GuiNpcButton(4, x + 8, y - 1, 60, 16, dimIDs, p);
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText());
		addButton(button);
		// region ID
		addLabel(new GuiNpcLabel(lId, "P:", x + 40, y + 2));
		textField = new GuiNpcTextField(9, this, fontRenderer, x + 47, y, 32, 14, "" + task.regionID);
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.regionID);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", task.regionID).appendSibling(compass).getFormattedText());
		addTextField(textField);
		// N
		addLabel(new GuiNpcLabel(lId, "N:", x + 71, y + 2));
		textField = new GuiNpcTextField(15, this, fontRenderer, x + 79, y, 126, 14, task.entityName);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// set player pos
		button = new GuiNpcButton(10, x + 146, y += 19, 30, 20, "gui.set");
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText());
		addButton(button);
		// tp
		button = new GuiNpcButton(11, x + 124, y, 20, 20, "TP");
		button.setHoverText("hover.teleport");
		addButton(button);
		// mini map point
		button = new GuiNpcCheckBox(5, x, y, 121, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap());
		button.setHoverText("quest.hover.set.minimap.point");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize - 25, 98, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void save() {
		task.setTargetName(getTextField(0).getText());
		task.setMaxProgress(getTextField(1).getInteger());

		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != EnumQuestTask.MANUAL) {
				continue;
			}
			if (taskObj.getTargetName().equals(task.getTargetName())) {
				getTextField(0).setText("");
				task.setTargetName("");
				task.setMaxProgress(1);
				break;
			}
		}

		if (task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
			}
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getId()) {
			case 0: {
				task.setTargetName(textField.getText());
				break;
			}
			case 1: {
				task.setMaxProgress(textField.getInteger());
				break;
			}
			case 9: {
				if (!BorderController.getInstance().regions.containsKey(textField.getInteger())) {
					textField.setText("" + textField.def);
					return;
				}
				task.regionID = textField.getInteger();
				textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", "" + task.regionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 10: {
				task.pos = new BlockPos(textField.getInteger(), task.pos.getY(), task.pos.getZ());
				break;
			}
			case 11: {
				task.pos = new BlockPos(task.pos.getX(), textField.getInteger(), task.pos.getZ());
				break;
			}
			case 12: {
				task.pos = new BlockPos(task.pos.getX(), task.pos.getY(), textField.getInteger());
				break;
			}
			case 14: {
				task.rangeCompass = textField.getInteger();
				break;
			}
			case 15: {
				task.entityName = textField.getText();
				break;
			}
		}
	}

}
