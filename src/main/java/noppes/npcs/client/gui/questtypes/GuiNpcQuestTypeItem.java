package noppes.npcs.client.gui.questtypes;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.CustomNPCsScheduler;

//Changed
public class GuiNpcQuestTypeItem extends GuiContainerNPCInterface implements ITextfieldListener {
	// private Quest quest; // Changed
	private static ResourceLocation back = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static ResourceLocation inv = new ResourceLocation(CustomNpcs.MODID, "textures/gui/baseinventory.png");
	// New
	private final QuestObjective task;

	public GuiNpcQuestTypeItem(EntityNPCInterface npc, ContainerNpcQuestTypeItem container, QuestObjective task) {
		super(npc, container);
		this.title = "";
		this.ySize = 192;
		this.closeOnEsc = false;
		// New
		this.task = task;
		this.npc = npc;
	}

	public void actionPerformed(GuiButton guibutton) {
		if (this.task != null) { // Changed
			if (guibutton.id == 0) {
				this.task.setItemLeave((((GuiNpcButton) guibutton).getValue() == 0));
			}
			if (guibutton.id == 1) {
				this.task.setItemIgnoreDamage(((GuiNpcButtonYesNo) guibutton).getBoolean());
			}
			if (guibutton.id == 2) {
				this.task.setItemIgnoreNBT(((GuiNpcButtonYesNo) guibutton).getBoolean());
			}
		}
		if (guibutton.id == 5) {
			this.task.setItem(this.inventorySlots.getSlot(0).getStack());
			if (this.task.getItemStack().isEmpty()) {
				NoppesUtilServer.getEditingQuest(this.player).questInterface.removeTask(this.task);
			} else {
				if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
					((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).subgui = null;
					((GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui).initGui();
				}
			}
			NoppesUtil.openGUI(this.player, GuiNPCManageQuest.Instance);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		this.drawWorldBackground(0);
		// Back
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(GuiNpcQuestTypeItem.back);
		int u = (this.width - this.xSize) / 2;
		int v = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(u, v, 0, 0, this.xSize, this.ySize);
		v += this.ySize;
		this.drawTexturedModalRect(u, v, 0, 196, this.xSize, 26);
		// New
		// Slot
		this.mc.renderEngine.bindTexture(GuiNpcQuestTypeItem.inv);
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
		if (this.subgui != null) {
			return;
		}
		if (isMouseHover(i, j, this.guiLeft + 90, this.guiTop + 68, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.max", "10000000").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 90, this.guiTop + 2, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.leave").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 90, this.guiTop + 24, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.dam").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 90, this.guiTop + 46, 50, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.nbt").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft, this.guiTop + this.ySize, 98, 20)) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		super.handleMouseClick(slotIn, slotId, mouseButton, type);
		CustomNPCsScheduler.runTack(() -> {
			this.initGui();
		}, 200);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "quest.takeitems", this.guiLeft + 6, this.guiTop + 8));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 90, this.guiTop + 2, 50, 20,
				new String[] { "gui.yes", "gui.no" }, this.task != null && this.task.isItemLeave() ? 0 : 1)); // Changed
		this.addLabel(new GuiNpcLabel(1, "gui.ignoreDamage", this.guiLeft + 6, this.guiTop + 29));
		this.addButton(new GuiNpcButtonYesNo(1, this.guiLeft + 90, this.guiTop + 24, 50, 20,
				this.task != null ? this.task.isIgnoreDamage() : true)); // Changed
		this.addLabel(new GuiNpcLabel(2, "gui.ignoreNBT", this.guiLeft + 6, this.guiTop + 51));
		this.addButton(new GuiNpcButtonYesNo(2, this.guiLeft + 90, this.guiTop + 46, 50, 20,
				this.task != null ? this.task.isItemIgnoreNBT() : true)); // Changed
		this.addButton(new GuiNpcButton(5, this.guiLeft, this.guiTop + this.ySize, 98, 20, "gui.back"));
		// New
		this.addLabel(new GuiNpcLabel(3, "quest.itemamount", this.guiLeft + 6, this.guiTop + 73));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 90, this.guiTop + 68, 50, 20,
				this.task.getMaxProgress() + ""));
		this.getTextField(0).numbersOnly = true;
		this.getTextField(0).setMinMaxDefault(0, 10000000, 1);
	}

	@Override
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		this.task.setMaxProgress(guiNpcTextField.getInteger());
	}

}
