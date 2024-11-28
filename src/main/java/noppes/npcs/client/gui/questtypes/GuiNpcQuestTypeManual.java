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

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeManual extends SubGuiInterface implements ITextfieldListener {

	public GuiScreen parent;
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = Maps.newHashMap();

	public GuiNpcQuestTypeManual(EntityNPCInterface npc, QuestObjective task, GuiScreen parent) {
		this.npc = npc;
		this.parent = parent;
		this.title = "Quest Manual Setup";
		this.setBackground("menubg.png");
		this.xSize = 214;
		this.ySize = 217;
		this.closeOnEsc = true;
		this.task = task;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (guibutton.id) {
		case 4: {
			if (!dataDimIDs.containsKey(button.getValue())) {
				return;
			}
			this.task.dimensionID = dataDimIDs.get(button.getValue());
			break;
		}
		case 5: {
			task.setPointOnMiniMap(((GuiNpcCheckBox) guibutton).isSelected());
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
		case 11: {
			if (this.task == null) {
				return;
			}
			Client.sendData(EnumPacketServer.TeleportTo, this.task.dimensionID, this.task.pos.getX(), this.task.pos.getY(), this.task.pos.getZ());
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
		// Back
		if (this.subgui == null) {
			int u = this.guiLeft + this.xSize - 1;
			int v = this.guiTop;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.mc.getTextureManager().bindTexture(this.background);
			this.drawTexturedModalRect(u, v, 252, 0, 4, this.ySize);
		}
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
		} else if (this.getTextField(1) != null && this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.kill.value", "" + this.getTextField(1).max)
					.getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.dim")
					.appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.set.minimap.point").getFormattedText());
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
		int lId = 0;
		int x = this.guiLeft + 6;
		int y = this.guiTop + 50;
		this.addLabel(
				new GuiNpcLabel(lId++, new TextComponentTranslation("quest.manual.names").getFormattedText(), x, y));
		// New
		this.addTextField(
				new GuiNpcTextField(0, this, this.fontRenderer, x, y += 12, 180, 20, this.task.getTargetName()));
		this.addTextField(
				new GuiNpcTextField(1, this, this.fontRenderer, x + 182, y, 24, 20, this.task.getMaxProgress() + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(1, Integer.MAX_VALUE, 1);

		this.addLabel(new GuiNpcLabel(lId++, "quest.task.pos.set", x, y += 24));
		this.addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 12) + 2));
		this.addTextField(
				new GuiNpcTextField(10, this, this.fontRenderer, x + 8, y, 40, 14, "" + this.task.pos.getX()));
		this.getTextField(10).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(lId++, "Y:", x + 52, y + 2));
		this.addTextField(
				new GuiNpcTextField(11, this, this.fontRenderer, x + 60, y, 40, 14, "" + this.task.pos.getY()));
		this.getTextField(11).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(lId++, "Z:", x + 104, y + 2));
		this.addTextField(
				new GuiNpcTextField(12, this, this.fontRenderer, x + 112, y, 40, 14, "" + this.task.pos.getZ()));
		this.getTextField(12).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(lId++, "R:", x + 156, y + 2));
		this.addTextField(
				new GuiNpcTextField(14, this, this.fontRenderer, x + 164, y, 41, 14, "" + this.task.rangeCompass));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);

		this.addLabel(new GuiNpcLabel(lId++, "D:", x, (y += 20) + 2));
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
		this.addLabel(new GuiNpcLabel(lId, "N:", x + 71, y + 2));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, x + 79, y, 126, 14, this.task.entityName));

		this.addButton(new GuiNpcButton(10, x + 146, y += 19, 60, 20, "gui.set"));
		this.addButton(new GuiNpcButton(11, x + 124, y, 20, 20, "TP"));
		addButton(new GuiNpcCheckBox(5, x, y, 121, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap()));

		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize - 25, 98, 20, "gui.back"));
	}

	@Override
	public void save() {
		this.task.setTargetName(this.getTextField(0).getText());
		this.task.setMaxProgress(this.getTextField(1).getInteger());

		for (QuestObjective task : NoppesUtilServer.getEditingQuest(this.player).questInterface.tasks) {
			if (task == this.task || task.getEnumType() != EnumQuestTask.MANUAL) {
				continue;
			}
			if (task.getTargetName().equals(this.task.getTargetName())) {
				this.getTextField(0).setText("");
				this.task.setTargetName("");
				this.task.setMaxProgress(1);
				break;
			}
		}

		if (this.task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
				((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
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
			this.task.setTargetName(textField.getText());
			break;
		}
		case 1: {
			this.task.setMaxProgress(textField.getInteger());
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
