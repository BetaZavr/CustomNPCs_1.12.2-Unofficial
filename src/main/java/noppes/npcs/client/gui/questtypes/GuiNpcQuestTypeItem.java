package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
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
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeItem extends GuiContainerNPCInterface implements ITextfieldListener {

	private static final ResourceLocation back = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation inv = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public GuiNpcQuestTypeItem(EntityNPCInterface npc, ContainerNpcQuestTypeItem container, QuestObjective taskObj) {
		super(npc, container);
		setBackground("menubg.png");
		title = new TextComponentTranslation("quest.title.item").getFormattedText();
		ySize = 192;
		closeOnEsc = false;

		task = taskObj;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (task == null) { return; }
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 0: {
				task.setItemLeave(button.getValue() == 0);
				break;
			}
			case 1: {
				task.setItemIgnoreDamage(((GuiNpcButtonYesNo) button).getBoolean());
				break;
			}
			case 2: {
				task.setItemIgnoreNBT(((GuiNpcButtonYesNo) button).getBoolean());
				break;
			}
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) { return; }
				task.dimensionID = dataDimIDs.get(button.getValue());
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
				task.setItem(inventorySlots.getSlot(0).getStack());
				if (task.getItemStack().isEmpty()) {
					NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task);
				} else {
					if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
						((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
						((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
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
		mc.getTextureManager().bindTexture(GuiNpcQuestTypeItem.back);
		int u = (width - xSize) / 2;
		int v = (height - ySize) / 2;
		drawTexturedModalRect(u, v, 0, 0, xSize, ySize);
		v += ySize;
		drawTexturedModalRect(u, v, 0, 196, xSize, 26);
		// Slot
		mc.getTextureManager().bindTexture(GuiNpcQuestTypeItem.inv);
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
		GuiNpcButton button = new GuiNpcButton(0, x1, y, 50, 14, new String[] { "gui.yes", "gui.no" }, task != null && task.isItemLeave() ? 0 : 1);
		button.setHoverText("quest.hover.edit.item.leave");
		addButton(button);
		// ignore damage
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreDamage", x, (y += 15) + 2));
		button = new GuiNpcButtonYesNo(1, x1, y, 50, 14, task == null || task.isIgnoreDamage());
		button.setHoverText("quest.hover.edit.item.ign.dam");
		addButton(button);
		// ignore nbt
		addLabel(new GuiNpcLabel(lId++, "gui.ignoreNBT", x, (y += 15) + 2));
		button = new GuiNpcButtonYesNo(2, x1, y, 50, 14, task == null || task.isItemIgnoreNBT());
		button.setHoverText("quest.hover.edit.item.ign.nbt");
		addButton(button);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize, 40, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
		// item amount
		addLabel(new GuiNpcLabel(lId++, "quest.itemamount", x, (y += 15) + 2));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x1 + 1, y + 1, 48, 12, task != null ? task.getMaxProgress() + "" : "0");
		textField.setMinMaxDefault(0, 576, 1);
		textField.setHoverText("quest.hover.edit.item.max", "576");
		addTextField(textField);
		// X
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 16) + 2));
		textField = new GuiNpcTextField(10, this, fontRenderer, x + 8, y, 40, 12, task != null ? "" + task.pos.getX() : "0");
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getX() : 0);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 51, y + 2));
		textField = new GuiNpcTextField(11, this, fontRenderer, x + 58, y, 40, 12, task != null ? "" + task.pos.getY() : "0");
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getY() : 0);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 101, y + 2));
		textField = new GuiNpcTextField(12, this, fontRenderer, x + 109, y, 40, 12, task != null ? "" + task.pos.getZ() : "0");
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task != null ? task.pos.getZ() : 0);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// tp
		button = new GuiNpcButton(11, x + 151, y - 1, 14, 14, "TP");
		button.setHoverText("hover.teleport");
		addButton(button);
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", x, (y += 14) + 2));
		textField = new GuiNpcTextField(14, this, fontRenderer, x + 8, y, 25, 12, task != null ? "" + task.rangeCompass : "0");
		textField.setMinMaxDefault(0, 64, task != null ? task.rangeCompass : 0);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// N
		addLabel(new GuiNpcLabel(lId++, "N:", x + 36, y + 2));
		textField = new GuiNpcTextField(15, this, fontRenderer, x + 44, y, 120, 12, task != null ? task.entityName : "");
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// dim ID
		addLabel(new GuiNpcLabel(lId, "D:", x + 21, (y += 16) + 2));
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
		button = new GuiButtonBiDirectional(4, x + 28, y - 1, 60, 16, dimIDs, p);
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText());
		addButton(button);
		button = new GuiNpcButton(10, x + 103, y - 1, 60, 16, "gui.set");
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText());
		addButton(button);
		button = new GuiNpcCheckBox(5, x + 42, guiTop + ySize, 123, 16, "quest.set.minimap.point", null, task != null && task.isSetPointOnMiniMap());
		button.setHoverText("quest.hover.set.minimap.point");
		addButton(button);
	}

	@Override
	public void save() { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (task == null) {
			return;
		}
		switch (textField.getId()) {
			case 0: {
				task.setMaxProgress(textField.getInteger());
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
