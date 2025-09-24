package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.SubGuiQuestEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class SubGuiNpcQuestTypeItem extends GuiContainerNPCInterface implements ITextfieldListener {

	protected static final ResourceLocation back = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	protected static final ResourceLocation inv = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");
	protected final Map<Integer, Integer> dataDimIDs = new HashMap<>();
	protected final QuestObjective task;

	public SubGuiNpcQuestTypeItem(EntityNPCInterface npc, ContainerNpcQuestTypeItem container, QuestObjective taskObj) {
		super(npc, container);
		setBackground("menubg.png");
		closeOnEsc = false;
		title = new TextComponentTranslation("quest.title.item").getFormattedText();
		ySize = 192;

		task = taskObj;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0 || task == null) { return; }
		switch (button.getID()) {
			case 0: task.setItemLeave(button.getValue() == 0); break;
			case 1: task.setItemIgnoreDamage(((GuiNpcButtonYesNo) button).getBoolean()); break;
			case 2: task.setItemIgnoreNBT(((GuiNpcButtonYesNo) button).getBoolean()); break;
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
			case 66: {
				task.setItem(inventorySlots.getSlot(0).getStack());
				if (task.getItemStack().isEmpty()) { NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task); }
				else {
					if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof SubGuiQuestEdit) {
						SubGuiQuestEdit subgui = (SubGuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui;
						subgui.setSubGui(null);
						subgui.initGui();
					}
				}
				NoppesUtil.openGUI(player, GuiNPCManageQuest.Instance);
				break;
			}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		drawWorldBackground(0);
		// Back
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(SubGuiNpcQuestTypeItem.back);
		int u = (width - xSize) / 2;
		int v = (height - ySize) / 2;
		drawTexturedModalRect(u, v, 0, 0, xSize, ySize);
		v += ySize;
		drawTexturedModalRect(u, v, 0, 196, xSize, 26);
		// Slot
		mc.getTextureManager().bindTexture(SubGuiNpcQuestTypeItem.inv);
		u = 7 + (width - xSize) / 2;
		v = 91 + (height - ySize) / 2;
		drawTexturedModalRect(u, v, 0, 0, 18, 18);
		// Player Inventory
		u = 7 + (width - xSize) / 2;
		v = 112 + (height - ySize) / 2;
		drawTexturedModalRect(u, v, 0, 0, 162, 76);
		super.drawGuiContainerBackgroundLayer(f, i, j);
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		CustomNPCsScheduler.runTack(this::initGui, 200);
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 6;
		int x1 = guiLeft + xSize - 55;
		int y = guiTop + 3;
		// take items
		addLabel(new GuiNpcLabel(lId++, "quest.takeitems", x, y + 2));
		addButton(new GuiNpcButton(0, x1, y, 50, 14, new String[] { "gui.yes", "gui.no" }, task != null && task.isItemLeave() ? 0 : 1)
				.setHoverText("quest.hover.edit.item.leave"));
		// ignore damage
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreDamage", x, (y += 15) + 2));
		addButton(new GuiNpcButtonYesNo(1, x1, y, 50, 14, task == null || task.isIgnoreDamage())
				.setHoverText("quest.hover.edit.item.ign.dam"));
		// ignore nbt
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreNBT", x, (y += 15) + 2));
		addButton(new GuiNpcButtonYesNo(2, x1, y, 50, 14, task == null || task.isItemIgnoreNBT())
				.setHoverText("quest.hover.edit.item.ign.nbt"));
		// exit
		addButton(new GuiNpcButton(66, x, guiTop + ySize, 40, 20, "gui.back")
				.setHoverText("hover.back"));
		// item amount
		addLabel(new GuiNpcLabel(lId++, "quest.itemamount", x, (y += 15) + 2));
		addTextField(new GuiNpcTextField(0, this, x1 + 1, y + 1, 48, 12, task != null ? task.getMaxProgress() + "" : "0")
				.setMinMaxDefault(0, 576, 1)
				.setHoverText("quest.hover.edit.item.max", "576"));
		// X
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 16) + 2));
		addTextField(new GuiNpcTextField(10, this, x + 8, y, 40, 12, task != null ? "" + task.pos.getX() : "0")
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getX() : 0)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText()));
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 51, y + 2));
		addTextField(new GuiNpcTextField(11, this, x + 58, y, 40, 12, task != null ? "" + task.pos.getY() : "0")
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getY() : 0)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText()));
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 101, y + 2));
		addTextField(new GuiNpcTextField(12, this, x + 109, y, 40, 12, task != null ? "" + task.pos.getZ() : "0")
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getZ() : 0)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText()));
		// tp
		addButton(new GuiNpcButton(11, x + 151, y - 1, 14, 14, "TP")
				.setHoverText("hover.teleport"));
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x, (y += 14) + 2));
		addTextField(new GuiNpcTextField(14, this, x + 8, y, 25, 12, task != null ? "" + task.rangeCompass : "0")
				.setMinMaxDefault(0, 64, task != null ? task.rangeCompass : 0)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText()));
		// N
		addLabel(new GuiNpcLabel(lId++, "N:", x + 36, y + 2));
		addTextField(new GuiNpcTextField(15, this, x + 44, y, 120, 12, task != null ? task.entityName : "")
				.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText()));
		// dim ID
		addLabel(new GuiNpcLabel(lId++, "D:", x + 21, (y += 16) + 2));
		int p = 0, i = 0;
		List<Integer> ids = Arrays.asList(DimensionManager.getStaticDimensionIDs());
		Collections.sort(ids);
		String[] dimIDs = new String[ids.size()];
		for (int id : ids) {
			dimIDs[i] = id + "";
			dataDimIDs.put(i, id);
			if (task != null && id == task.dimensionID) { p = i; }
			i++;
		}
		addButton(new GuiNpcButton(4, x + 28, y - 1, 30, 16, dimIDs, p)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.dim", dimIDs[p]).appendSibling(compass).getFormattedText()));
		// region ID
		addLabel(new GuiNpcLabel(lId, "P:", x + 60, y + 2));
		int id = (task == null ? -1 : task.regionID);
		addTextField(new GuiNpcTextField(9, this, x + 67, y, 32, 14, "" + id)
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, id)
				.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", id).appendSibling(compass).getFormattedText()));
		// set player pos
		addButton(new GuiNpcButton(10, x + 103, y - 1, 60, 16, "gui.set")
				.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText()));
		// mini map point
		addButton(new GuiNpcCheckBox(5, x + 42, guiTop + ySize, 123, 16, "quest.set.minimap.point", null, task != null && task.isSetPointOnMiniMap())
				.setHoverText("quest.hover.set.minimap.point"));
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getID()) {
			case 0: task.setMaxProgress(textField.getInteger()); break;
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
