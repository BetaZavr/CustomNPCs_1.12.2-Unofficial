package noppes.npcs.client.gui.questtypes;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

public class GuiNpcQuestTypeDialog extends SubGuiInterface
		implements GuiSelectionListener, IGuiData, ITextfieldListener {

	private String data = "";
	public GuiScreen parent;
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = Maps.newHashMap();

	public GuiNpcQuestTypeDialog(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		this.npc = npc;
		this.parent = parent;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 96;
		this.closeOnEsc = true;
		this.task = task; // New
		IDialog d = DialogController.instance.get(task.getTargetID());
		if (d != null) {
			this.data = d.getName();
		}
		Client.sendData(EnumPacketServer.QuestDialogGetTitle, this.task.getTargetID());
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 1: {
			this.setSubGui(new GuiDialogSelection(this.task.getTargetID(), 0));
			break;
		}
		case 2: {
			this.task.setTargetID(0);
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
		case 66: {
			this.close();
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
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(2) != null && this.getTextField(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.area.range").getFormattedText());
		} else if (this.getTextField(10) != null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(11) != null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(12) != null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(14) != null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.range")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(15) != null && this.getTextField(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.entity")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.dim")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.set.minimap.point").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport").getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.name").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.value", "" + this.getTextField(1).max)
					.getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.add").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.dialog.del").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 10;
		int y = this.guiTop + 15;
		String title = "dialog.selectoption";
		if (!this.data.isEmpty()) {
			title = this.data;
		}
		this.addButton(new GuiNpcButton(1, x + 24, y, 210, 20, title));
		this.addButton(new GuiNpcButton(2, x, y, 20, 20, "X"));
		String tx = "Quest Dialog Setup";
		this.addLabel(new GuiNpcLabel(0, tx, (this.width - this.mc.fontRenderer.getStringWidth(tx)) / 2, y - 10, 0));

		this.addLabel(new GuiNpcLabel(11, "X:", x, (y += 23) + 2));
		this.addTextField(
				new GuiNpcTextField(10, this, this.fontRenderer, x + 8, y, 40, 14, "" + this.task.pos.getX()));
		this.getTextField(10).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(12, "Y:", x + 62, y + 2));
		this.addTextField(
				new GuiNpcTextField(11, this, this.fontRenderer, x + 70, y, 40, 14, "" + this.task.pos.getY()));
		this.getTextField(11).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(13, "Z:", x + 120, y + 2));
		this.addTextField(
				new GuiNpcTextField(12, this, this.fontRenderer, x + 128, y, 40, 14, "" + this.task.pos.getZ()));
		this.getTextField(12).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(15, "R:", x + 180, y + 2));
		this.addTextField(
				new GuiNpcTextField(14, this, this.fontRenderer, x + 188, y, 45, 14, "" + this.task.rangeCompass));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);

		this.addLabel(new GuiNpcLabel(14, "D:", x, (y += 18) + 2));
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
		this.addLabel(new GuiNpcLabel(16, "N:", x + 74, y + 2));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, x + 82, y, 151, 14, this.task.entityName));

		this.addButton(new GuiNpcButton(10, x + 174, y += 17, 60, 20, "gui.set"));
		this.addButton(new GuiNpcButton(11, x + 152, y, 20, 20, "TP"));

		this.addButton(
				new GuiNpcCheckBox(5, x + 42, y, 109, 16, "quest.set.minimap.point", this.task.isSetPointOnMiniMap()));

		this.addButton(new GuiNpcButton(66, x, y, 40, 20, "gui.back"));
	}

	@Override
	public void save() {
		if (this.task.getTargetID() <= 0) {
			NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
			}
		}
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
		if (this.task == null) {
			return;
		}
		switch (textField.getId()) {
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
