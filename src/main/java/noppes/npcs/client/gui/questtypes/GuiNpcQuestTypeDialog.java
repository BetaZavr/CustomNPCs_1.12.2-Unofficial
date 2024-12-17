package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
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
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.select.GuiDialogSelection;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
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

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeDialog
extends SubGuiInterface
implements GuiSelectionListener, IGuiData, ITextfieldListener {

	private String data = "";
	public GuiScreen parent;
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public GuiNpcQuestTypeDialog(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(npc);
		setBackground("menubg.png");
		title = new TextComponentTranslation("quest.title.dialog").getFormattedText();
		xSize = 256;
		ySize = 96;
		closeOnEsc = true;
		parent = gui;

		task = taskObj;
		IDialog d = DialogController.instance.get(task.getTargetID());
		if (d != null) { data = d.getName(); }
		Client.sendData(EnumPacketServer.QuestDialogGetTitle, task.getTargetID());
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (task == null) { return; }
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 1: {
				setSubGui(new GuiDialogSelection(task.getTargetID(), 0));
				break;
			}
			case 2: {
				task.setTargetID(0);
				initGui();
				break;
			}
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) {
					return;
				}
				task.dimensionID = dataDimIDs.get(button.getValue());
				break;
			}
			case 5: {
				task.setPointOnMiniMap(((GuiNpcCheckBox) guibutton).isSelected());
				break;
			}
			case 10: {
				task.pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
				task.dimensionID = player.world.provider.getDimension();
				initGui();
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
		if (subgui == null) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop + ySize - 1, 0.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(background);
			if (xSize > 256) {
				drawTexturedModalRect(0, 0, 0, 214, 250, 4);
				drawTexturedModalRect(250, 0, 256 - (xSize - 250), 214, xSize - 250, 4);
			} else {
				drawTexturedModalRect(0, 0, 0, 214, xSize, 4);
			}
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
		GuiNpcButton button = new GuiNpcButton(1, x + 24, y, 210, 20, data.isEmpty() ? "dialog.selectoption" : data);
		button.setHoverText("quest.hover.edit.dialog.add");
		addButton(button);
		// del
		button = new GuiNpcButton(2, x, y, 20, 20, "X");
		button.setHoverText("quest.hover.edit.dialog.del");
		addButton(button);
		String tx = "Quest Dialog Setup";
		GuiNpcLabel label = new GuiNpcLabel(lId++, tx, width / 2, y - 10, 0);
		label.center(label.width);
		addLabel(label);

		// X
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 23) + 2));
		GuiNpcTextField textField = new GuiNpcTextField(10, this, fontRenderer, x + 8, y, 40, 14, "" + task.pos.getX());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getX());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 62, y + 2));
		textField = new GuiNpcTextField(11, this, fontRenderer, x + 70, y, 40, 14, "" + task.pos.getY());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 120, y + 2));
		textField = new GuiNpcTextField(12, this, fontRenderer, x + 128, y, 40, 14, "" + task.pos.getZ());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x + 180, y + 2));
		textField = new GuiNpcTextField(14, this, fontRenderer, x + 188, y, 45, 14, "" + task.rangeCompass);
		textField.setMinMaxDefault(0, 64, task.rangeCompass);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText());
		addTextField(textField);
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
		button = new GuiButtonBiDirectional(4, x + 8, y - 1, 60, 16, dimIDs, p);
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText());
		addButton(button);
		// N
		addLabel(new GuiNpcLabel(lId, "N:", x + 74, y + 2));
		textField = new GuiNpcTextField(15, this, fontRenderer, x + 82, y, 151, 14, task.entityName);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// set player pos
		button = new GuiNpcButton(10, x + 174, y += 17, 60, 20, "gui.set");
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText());
		addButton(button);
		// tp
		button = new GuiNpcButton(11, x + 152, y, 20, 20, "TP");
		button.setHoverText("hover.teleport");
		addButton(button);
		// mini map point
		button = new GuiNpcCheckBox(5, x + 42, y, 109, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap());
		button.setHoverText("quest.hover.set.minimap.point");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x, y, 40, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void save() {
		if (task.getTargetID() <= 0) {
			NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
			}
		}
	}

	@Override
	public void selected(int id, String name) {
		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != EnumQuestTask.DIALOG) {
				continue;
			}
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
		switch (textField.getId()) {
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
