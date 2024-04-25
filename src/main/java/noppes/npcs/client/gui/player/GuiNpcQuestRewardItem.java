package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNpcQuestRewardItem;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;

public class GuiNpcQuestRewardItem extends GuiContainerNPCInterface {

	private ResourceLocation resource = this.getResource("extrasmallbg.png");
	private ResourceLocation slots = this.getResource("baseinventory.png");
	private Quest quest;
	private ItemStack reward = ItemStack.EMPTY;

	public GuiNpcQuestRewardItem(ContainerNpcQuestRewardItem container, int questId) {
		super(null, container);
		this.quest = (Quest) QuestController.instance.get(questId);
		this.xSize = 176;
		this.ySize = 71;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			this.close();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		int u = (this.width - this.xSize) / 2;
		int v = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(u, v, 0, 0, this.xSize, this.ySize);

		super.drawGuiContainerBackgroundLayer(f, i, j);

		v += 19;
		int size = this.inventorySlots.inventoryItemStacks.size();
		u += 7 + (9 * 9) - size * 9;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.slots);
		this.drawTexturedModalRect(u, v, 0, 0, size * 18, 18);
	}

	@Override
	protected void handleMouseClick(Slot slotIn, int slotId, int mouseButton, ClickType type) {
		if (slotIn == null) {
			return;
		}
		this.reward = slotIn.getStack();
		this.close();
	}

	@Override
	public void initGui() {
		super.initGui();
		String text = new TextComponentTranslation("quest.choose.reward").getFormattedText();
		this.addLabel(new GuiNpcLabel(0, text,
				this.guiLeft + (this.xSize - this.mc.fontRenderer.getStringWidth(text)) / 2, this.guiTop + 4));
		this.addButton(new GuiNpcButton(0, this.guiLeft + (this.xSize - 110) / 2, this.guiTop + this.ySize - 26, 110,
				20, "quest.no.thanks"));
	}

	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletionReward, this.quest.id,
				this.reward.writeToNBT(new NBTTagCompound()));
	}

}
