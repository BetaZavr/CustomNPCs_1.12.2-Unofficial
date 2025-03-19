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
import noppes.npcs.client.gui.util.IGuiNpcButton;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNpcQuestRewardItem;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;

import javax.annotation.Nonnull;

public class GuiNpcQuestRewardItem
extends GuiContainerNPCInterface {

	private final ResourceLocation resource = getResource("extrasmallbg.png");
	private final ResourceLocation slots = getResource("baseinventory.png");
	private final Quest quest;
	private ItemStack reward = ItemStack.EMPTY;

	public GuiNpcQuestRewardItem(ContainerNpcQuestRewardItem container, int questId) {
		super(null, container);
		xSize = 176;
		ySize = 71;

		quest = (Quest) QuestController.instance.get(questId);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) { close(); }
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		int u = (width - xSize) / 2;
		int v = (height - ySize) / 2;
		drawTexturedModalRect(u, v, 0, 0, xSize, ySize);

		super.drawGuiContainerBackgroundLayer(f, i, j);

		v += 19;
		int size = inventorySlots.inventoryItemStacks.size();
		u += 7 + (9 * 9) - size * 9;
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(slots);
		drawTexturedModalRect(u, v, 0, 0, size * 18, 18);
	}

	@Override
	protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
        reward = slotIn.getStack();
		close();
	}

	@Override
	public void initGui() {
		super.initGui();
		String text = new TextComponentTranslation("quest.choose.reward").getFormattedText();
		addLabel(new GuiNpcLabel(0, text, guiLeft + (xSize - mc.fontRenderer.getStringWidth(text)) / 2, guiTop + 4));
		addButton(new GuiNpcButton(0, guiLeft + (xSize - 110) / 2, guiTop + ySize - 26, 110, 20, "quest.no.thanks"));
	}

	@Override
	public void save() {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletionReward, quest.id, reward.writeToNBT(new NBTTagCompound()));
	}

}
