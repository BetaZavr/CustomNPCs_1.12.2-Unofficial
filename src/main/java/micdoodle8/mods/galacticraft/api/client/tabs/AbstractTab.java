package micdoodle8.mods.galacticraft.api.client.tabs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.client.FMLClientHandler;

public abstract class AbstractTab extends GuiButton {
	protected RenderItem itemRender;
	public int potionOffsetLast;
	ItemStack renderStack;
	protected ResourceLocation texture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

	public AbstractTab(int id, int posX, int posY, ItemStack renderStack) {
		super(id, posX, posY, 28, 32, "");
		this.renderStack = renderStack;
		this.itemRender = FMLClientHandler.instance().getClient().getRenderItem();
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		int newPotionOffset = TabRegistry.getPotionOffsetNEI();
		if (mc.currentScreen instanceof GuiInventory) { // New
			ScaledResolution scaleW = new ScaledResolution(mc);
			int left = (scaleW.getScaledWidth() - ((GuiContainer) mc.currentScreen).getXSize()) / 2;
			newPotionOffset = ((GuiContainer) mc.currentScreen).getGuiLeft() - left;
		}
		if (newPotionOffset != this.potionOffsetLast) {
			this.x += newPotionOffset - this.potionOffsetLast;
			this.potionOffsetLast = newPotionOffset;
		}
		if (this.visible) { // Changed
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			int yTexPos = this.enabled ? 3 : 32;
			int ySize = this.enabled ? 25 : 32;
			int xOffset = ((this.id != 2) ? 1 : 0) * 28;
			int yPos = this.y + (this.enabled ? 3 : 0);
			mc.renderEngine.bindTexture(this.texture);
			this.drawTexturedModalRect(this.x, yPos, xOffset, yTexPos, 28, ySize);

			RenderHelper.enableGUIStandardItemLighting();
			this.zLevel = 100.0f;
			this.itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			this.itemRender.renderItemAndEffectIntoGUI(this.renderStack, this.x + 6, this.y + 8);
			this.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, this.renderStack, this.x + 6, this.y + 8,
					(String) null);
			GlStateManager.disableLighting();
			this.itemRender.zLevel = 0.0f;
			this.zLevel = 0.0f;
			RenderHelper.disableStandardItemLighting();
		}
	}

	public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
		boolean inWindow = this.enabled && this.visible && mouseX >= this.x && mouseY >= this.y
				&& mouseX < this.x + this.width && mouseY < this.y + this.height;
		if (inWindow) {
			this.onTabClicked();
		}
		return inWindow;
	}

	public abstract void onTabClicked();

	public abstract boolean shouldAddToList();
}
