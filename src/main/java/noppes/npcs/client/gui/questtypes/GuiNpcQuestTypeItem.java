package noppes.npcs.client.gui.questtypes;

import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
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

//Changed
public class GuiNpcQuestTypeItem extends GuiContainerNPCInterface implements ITextfieldListener {

	// private Quest quest; // Changed
	private static final ResourceLocation back = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation inv = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");
	// New
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();

	public GuiNpcQuestTypeItem(EntityNPCInterface npc, ContainerNpcQuestTypeItem container, QuestObjective task) {
		super(npc, container);
		this.title = "";
		this.ySize = 192;
		this.closeOnEsc = false;
		this.task = task;
		this.npc = npc;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 0: {
			if (this.task == null) {
				return;
			}
			this.task.setItemLeave(button.getValue() == 0);
			break;
		}
		case 1: {
			if (this.task == null) {
				return;
			}
			this.task.setItemIgnoreDamage(((GuiNpcButtonYesNo) button).getBoolean());
			break;
		}
		case 2: {
			if (this.task == null) {
				return;
			}
			this.task.setItemIgnoreNBT(((GuiNpcButtonYesNo) button).getBoolean());
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
			this.task.setItem(this.inventorySlots.getSlot(0).getStack());
			if (this.task.getItemStack().isEmpty()) {
				NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
			} else {
				if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
					((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.subgui = null;
					((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui.initGui();
				}
			}
			NoppesUtil.openGUI(this.player, GuiNPCManageQuest.Instance);
			break;
		}
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.drawWorldBackground(0);
		// Back
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiNpcQuestTypeItem.back);
		int u = (this.width - this.xSize) / 2;
		int v = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(u, v, 0, 0, this.xSize, this.ySize);
		v += this.ySize;
		this.drawTexturedModalRect(u, v, 0, 196, this.xSize, 26);
		// New
		// Slot
		this.mc.getTextureManager().bindTexture(GuiNpcQuestTypeItem.inv);
		u = 7 + (this.width - this.xSize) / 2;
		v = 91 + (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(u, v, 0, 0, 18, 18);
		// Player Inventory
		u = 7 + (this.width - this.xSize) / 2;
		v = 112 + (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(u, v, 0, 0, 162, 76);
		super.drawGuiContainerBackgroundLayer(f, i, j);
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
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.max", "" + this.getTextField(0).max)
					.getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.leave").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.dam").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.nbt").getFormattedText());
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
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		CustomNPCsScheduler.runTack(this::initGui, 200);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 6;
		int x1 = this.guiLeft + this.xSize - 55;
		int y = this.guiTop + 3;
		this.addLabel(new GuiNpcLabel(0, "quest.takeitems", x, y + 2));
		this.addButton(new GuiNpcButton(0, x1, y, 50, 14, new String[] { "gui.yes", "gui.no" },
				this.task != null && this.task.isItemLeave() ? 0 : 1)); // Changed

		this.addLabel(new GuiNpcLabel(1, "gui.ignoreDamage", x, (y += 15) + 2));
		this.addButton(new GuiNpcButtonYesNo(1, x1, y, 50, 14, this.task == null || this.task.isIgnoreDamage()));

		this.addLabel(new GuiNpcLabel(2, "gui.ignoreNBT", x, (y += 15) + 2));
		this.addButton(new GuiNpcButtonYesNo(2, x1, y, 50, 14, this.task == null || this.task.isItemIgnoreNBT()));

		this.addButton(new GuiNpcButton(66, x, this.guiTop + this.ySize, 40, 20, "gui.back"));
		// New
		this.addLabel(new GuiNpcLabel(3, "quest.itemamount", x, (y += 15) + 2));
        this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, x1 + 1, y + 1, 48, 12, this.task != null ? this.task.getMaxProgress() + "" : "0"));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, 576, 1);

		this.addLabel(new GuiNpcLabel(11, "X:", x, (y += 16) + 2));
		this.addTextField(
				new GuiNpcTextField(10, this, this.fontRenderer, x + 8, y, 40, 12, this.task != null ? "" + this.task.pos.getX() : "0"));
		this.getTextField(10).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(12, "Y:", x + 51, y + 2));
		this.addTextField(
				new GuiNpcTextField(11, this, this.fontRenderer, x + 58, y, 40, 12, this.task != null ? "" + this.task.pos.getY() : "0"));
		this.getTextField(11).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(13, "Z:", x + 101, y + 2));
		this.addTextField(
				new GuiNpcTextField(12, this, this.fontRenderer, x + 109, y, 40, 12, this.task != null ? "" + this.task.pos.getZ() : "0"));
		this.getTextField(12).setNumbersOnly();
		this.addButton(new GuiNpcButton(11, x + 151, y - 1, 14, 14, "TP"));

		this.addLabel(new GuiNpcLabel(15, "R:", x, (y += 14) + 2));
		this.addTextField(
				new GuiNpcTextField(14, this, this.fontRenderer, x + 8, y, 25, 12, this.task != null ? "" + this.task.rangeCompass : "0"));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 64, this.task != null ? this.task.rangeCompass : 0);
		this.addLabel(new GuiNpcLabel(16, "N:", x + 36, y + 2));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, x + 44, y, 120, 12, this.task != null ? this.task.entityName : ""));

		this.addLabel(new GuiNpcLabel(14, "D:", x + 21, (y += 16) + 2));
		int p = 0, i = 0;
		List<Integer> ids = Arrays.asList(DimensionManager.getStaticDimensionIDs());
		Collections.sort(ids);
		String[] dimIDs = new String[ids.size()];
		for (int id : ids) {
			dimIDs[i] = id + "";
			dataDimIDs.put(i, id);
			if (this.task != null && id == this.task.dimensionID) {
				p = i;
			}
			i++;
		}
		this.addButton(new GuiButtonBiDirectional(4, x + 28, y - 1, 60, 16, dimIDs, p));
		this.addButton(new GuiNpcButton(10, x + 103, y - 1, 60, 16, "gui.set"));

		addButton(new GuiNpcCheckBox(5, x + 42, guiTop + ySize, 123, 16, "quest.set.minimap.point", null, task != null && task.isSetPointOnMiniMap()));
	}

	@Override
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.task == null) {
			return;
		}
		switch (textField.getId()) {
		case 0: {
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
