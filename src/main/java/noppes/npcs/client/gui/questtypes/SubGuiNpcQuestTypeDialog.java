package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.SubGuiQuestEdit;
import noppes.npcs.client.gui.select.SubGuiDialogSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;

import javax.annotation.Nonnull;

public class SubGuiNpcQuestTypeDialog extends SubGuiInterface implements GuiSelectionListener, IGuiData, ITextfieldListener {

	protected final Map<Integer, Integer> dataDimIDs = new HashMap<>();
	protected final QuestObjective task;
	protected String data = "";

	public SubGuiNpcQuestTypeDialog(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(0, npc);
		setBackground("menubg.png");
		closeOnEsc = true;
		title = new TextComponentTranslation("quest.title.dialog").getFormattedText();
		xSize = 256;
		ySize = 96;

		parent = gui;
		task = taskObj;
		IDialog d = DialogController.instance.get(task.getTargetID());
		if (d != null) { data = d.getName(); }
		Client.sendData(EnumPacketServer.QuestDialogGetTitle, task.getTargetID());
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0 || task == null) { return; }
		switch (button.getID()) {
			case 1: setSubGui(new SubGuiDialogSelection(task.getTargetID(), 0)); break;
			case 2: task.setTargetID(0); initGui(); break;
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) { return; }
				task.dimensionID = dataDimIDs.get(button.getValue());
				button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim", "" + task.dimensionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 5: task.setPointOnMiniMap(((GuiNpcCheckBox) button).isSelected()); break;
			case 10: {
				task.pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
				task.dimensionID = player.world.provider.getDimension();
				initGui();
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop + ySize - 1, 0.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(background);
			if (xSize > 256) {
				drawTexturedModalRect(0, 0, 0, 214, 250, 4);
				drawTexturedModalRect(250, 0, 256 - (xSize - 250), 214, xSize - 250, 4);
			}
			else { drawTexturedModalRect(0, 0, 0, 214, xSize, 4); }
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 10;
		int y = guiTop + 15;
		// add
		addButton(new GuiNpcButton(1, x + 24, y, 210, 20, data.isEmpty() ? "dialog.selectoption" : data)
				.setHoverText("quest.hover.edit.dialog.add"));
		// del
		addButton(new GuiNpcButton(2, x, y, 20, 20, "X")
				.setHoverText("quest.hover.edit.dialog.del"));
		String tx = "Quest Dialog Setup";
		GuiNpcLabel label = new GuiNpcLabel(lId++, tx, width / 2, y - 10, 0);
		label.setCenter(label.width);
		addLabel(label);

		// X
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 23) + 2));
		addTextField(new GuiNpcTextField(10, this, x + 8, y, 40, 14, "" + task.pos.getX())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getX())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText()));
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 62, y + 2));
		addTextField(new GuiNpcTextField(11, this, x + 70, y, 40, 14, "" + task.pos.getY())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText()));
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 120, y + 2));
		addTextField(new GuiNpcTextField(12, this, x + 128, y, 40, 14, "" + task.pos.getZ())
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ())
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText()));
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x + 180, y + 2));
		addTextField(new GuiNpcTextField(14, this, x + 188, y, 45, 14, "" + task.rangeCompass)
				.setMinMaxDefault(0, 64, task.rangeCompass)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText()));
		// dim ID
		addLabel(new GuiNpcLabel(lId++, "D:", x, (y += 18) + 2));
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
		addLabel(new GuiNpcLabel(lId, "N:", x + 84, y + 2));
		addTextField(new GuiNpcTextField(15, this, x + 92, y, 141, 14, task.entityName)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText()));
		// set player pos
		addButton(new GuiNpcButton(10, x + 174, y += 17, 60, 20, "gui.set")
				.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText()));
		// tp
		addButton(new GuiNpcButton(11, x + 152, y, 20, 20, "TP")
				.setHoverText("hover.teleport"));
		// mini map point
		addButton(new GuiNpcCheckBox(5, x + 42, y, 109, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap())
				.setHoverText("quest.hover.set.minimap.point"));
		// exit
		addButton(new GuiNpcButton(66, x, y, 40, 20, "gui.back")
				.setHoverText("hover.back"));
	}

	@Override
	public void save() {
		if (task.getTargetID() <= 0) { NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task); }
		else if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof SubGuiQuestEdit) {
			SubGuiQuestEdit subgui = (SubGuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui;
			subgui.setSubGui(null);
			subgui.initGui();
		}
	}

	@Override
	public void selected(int id, String name) {
		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != EnumQuestTask.DIALOG) { continue; }
			if (taskObj.getTargetID() == id) { return; }
		}
		task.setTargetID(id);
		data = name;
		initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		data = "";
		if (compound.hasKey("Title", 8)) { data = compound.getString("Title"); }
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getID()) {
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
