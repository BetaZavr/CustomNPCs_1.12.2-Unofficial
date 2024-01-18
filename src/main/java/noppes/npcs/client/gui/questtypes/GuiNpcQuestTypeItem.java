package noppes.npcs.client.gui.questtypes;

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
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcButtonYesNo;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerNpcQuestTypeItem;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.CustomNPCsScheduler;

//Changed
public class GuiNpcQuestTypeItem
extends GuiContainerNPCInterface
implements ITextfieldListener {
	
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

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "quest.takeitems", this.guiLeft + 6, this.guiTop + 7));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 90, this.guiTop + 4, 50, 15, new String[] { "gui.yes", "gui.no" }, this.task != null && this.task.isItemLeave() ? 0 : 1)); // Changed
		this.addLabel(new GuiNpcLabel(1, "gui.ignoreDamage", this.guiLeft + 6, this.guiTop + 24));
		this.addButton(new GuiNpcButtonYesNo(1, this.guiLeft + 90, this.guiTop + 21, 50, 15, this.task != null ? this.task.isIgnoreDamage() : true)); // Changed
		this.addLabel(new GuiNpcLabel(2, "gui.ignoreNBT", this.guiLeft + 6, this.guiTop + 43));
		this.addButton(new GuiNpcButtonYesNo(2, this.guiLeft + 90, this.guiTop + 40, 50, 15, this.task != null ? this.task.isItemIgnoreNBT() : true)); // Changed
		this.addButton(new GuiNpcButton(5, this.guiLeft+7, this.guiTop + this.ySize, 60, 20, "gui.back"));
		// New
		this.addLabel(new GuiNpcLabel(3, "quest.itemamount", this.guiLeft + 6, this.guiTop + 62));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 91, this.guiTop + 59, 48, 15, this.task.getMaxProgress() + ""));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, 576, 1);
		
		this.addLabel(new GuiNpcLabel(11, "X:", this.guiLeft + 4, this.guiTop + 78));
		this.addTextField(new GuiNpcTextField(10, this, this.fontRenderer, this.guiLeft + 12, this.guiTop + 77, 40, 13, ""+this.task.pos.getX()));
		this.getTextField(10).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(12, "Y:", this.guiLeft + 54, this.guiTop + 78));
		this.addTextField(new GuiNpcTextField(11, this, this.fontRenderer, this.guiLeft + 62, this.guiTop + 77, 40, 13, ""+this.task.pos.getY()));
		this.getTextField(11).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(13, "Z:", this.guiLeft + 104, this.guiTop + 78));
		this.addTextField(new GuiNpcTextField(12, this, this.fontRenderer, this.guiLeft + 112, this.guiTop + 77, 40, 13, ""+this.task.pos.getZ()));
		this.getTextField(12).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(14, "D:", this.guiLeft + 63, this.guiTop + 96));
		this.addTextField(new GuiNpcTextField(13, this, this.fontRenderer, this.guiLeft + 71, this.guiTop + 95, 25, 13, ""+this.task.dimensionID));
		this.getTextField(13).setNumbersOnly();
		this.addLabel(new GuiNpcLabel(15, "R:", this.guiLeft + 28, this.guiTop + 96));
		this.addTextField(new GuiNpcTextField(14, this, this.fontRenderer, this.guiLeft + 36, this.guiTop + 95, 25, 13, ""+this.task.rangeCompass));
		this.getTextField(14).setNumbersOnly();
		this.getTextField(14).setMinMaxDefault(0, 64, this.task.rangeCompass);
		this.addButton(new GuiNpcButton(10, this.guiLeft+109, this.guiTop + this.ySize, 60, 20, "gui.set"));
		this.addButton(new GuiNpcButton(11, this.guiLeft+87, this.guiTop + this.ySize, 20, 20, "TP"));
		this.addLabel(new GuiNpcLabel(16, "N:", this.guiLeft + 98, this.guiTop + 96));
		this.addTextField(new GuiNpcTextField(15, this, this.fontRenderer, this.guiLeft + 106, this.guiTop + 95, 62, 13, this.task.entityName));
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 0: {
				if (this.task == null) { return; }
				this.task.setItemLeave(button.getValue() == 0);
				break;
			}
			case 1: {
				if (this.task == null) { return; }
				this.task.setItemIgnoreDamage(((GuiNpcButtonYesNo) button).getBoolean());
				break;
			}
			case 2: {
				if (this.task == null) { return; }
				this.task.setItemIgnoreNBT(((GuiNpcButtonYesNo) button).getBoolean());
				break;
			}
			case 5: {
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
				break;
			}
			case 10: {
				if (this.task == null) { return; }
				this.task.pos = this.mc.player.getPosition();
				this.task.dimensionID = this.mc.player.world.provider.getDimension();
				this.initGui();
				break;
			}
			case 11: {
				if (this.task == null) { return; }
				Client.sendData(EnumPacketServer.TeleportTo, new Object[] { this.task.dimensionID, this.task.pos.getX(), this.task.pos.getY(), this.task.pos.getZ() });
				break;
			}
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
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(10)!=null && this.getTextField(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(11)!=null && this.getTextField(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(12)!=null && this.getTextField(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(13)!=null && this.getTextField(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(14)!=null && this.getTextField(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getTextField(15)!=null && this.getTextField(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport").getFormattedText());
		}
		else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.max", ""+this.getTextField(0).max).getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.leave").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.dam").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.item.ign.nbt").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
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
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.task == null) { return; }
		switch(textField.getId()) {
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
			case 13: {
				int dim = textField.getInteger();
				if (!DimensionManager.isDimensionRegistered(dim)) {
					textField.setText(""+this.task.dimensionID);
					return;
				}
				this.task.dimensionID = textField.getInteger();
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
