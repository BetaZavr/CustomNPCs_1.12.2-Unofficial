package noppes.npcs.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.IMerchant;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraft.util.ResourceLocation;
import net.minecraft.village.MerchantRecipe;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.client.Client;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerMerchantAdd;

import javax.annotation.Nonnull;
import java.util.Objects;

@SideOnly(Side.CLIENT)
public class GuiMerchantAdd
extends GuiContainer {

	@SideOnly(Side.CLIENT)
	static class MerchantButton extends GuiButton {

		private final boolean forward;

		public MerchantButton(int buttonId, int x, int y, boolean isForward) {
			super(buttonId, x, y, 12, 19, "");
			forward = isForward;
		}

		public void drawButton(@Nonnull Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
			if (visible) {
				minecraft.getTextureManager().bindTexture(GuiMerchantAdd.merchantGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				boolean flag = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
				int k = 0;
				int l = 176;
				if (!enabled) {
					l += width * 2;
				} else if (flag) {
					l += width;
				}
				if (!forward) {
					k += height;
				}
				drawTexturedModalRect(x, y, l, k, width, height);
			}
		}
	}

	private static final ResourceLocation merchantGuiTextures = new ResourceLocation("textures/gui/container/villager.png");

	private int currentRecipeIndex;
	private final String field_94082_v;
	private MerchantButton nextRecipeButtonIndex;

	private MerchantButton previousRecipeButtonIndex;

	private final IMerchant theIMerchant;

	public GuiMerchantAdd() {
		super(new ContainerMerchantAdd(Minecraft.getMinecraft().player, ServerEventsHandler.Merchant, Minecraft.getMinecraft().world));
		theIMerchant = ServerEventsHandler.Merchant;
		field_94082_v = I18n.format("entity.Villager.name");
	}

	protected void actionPerformed(@Nonnull GuiButton guiButton) {
		boolean flag = false;
		Minecraft mc = Minecraft.getMinecraft();
		if (guiButton == nextRecipeButtonIndex) {
			++currentRecipeIndex;
			flag = true;
		} else if (guiButton == previousRecipeButtonIndex) {
			--currentRecipeIndex;
			flag = true;
		}
		if (guiButton.id == 4) {
			MerchantRecipeList merchantrecipelist = theIMerchant.getRecipes(mc.player);
            if (merchantrecipelist != null && currentRecipeIndex < merchantrecipelist.size()) {
				merchantrecipelist.remove(currentRecipeIndex);
				if (currentRecipeIndex > 0) {
					--currentRecipeIndex;
				}
				Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist);
			}
		}
		if (guiButton.id == 5) {
			ItemStack item1 = inventorySlots.getSlot(0).getStack();
			ItemStack item2 = inventorySlots.getSlot(1).getStack();
			ItemStack sold = inventorySlots.getSlot(2).getStack();
            item1 = item1.copy();
            sold = sold.copy();
            item2 = item2.copy();
            MerchantRecipe recipe = new MerchantRecipe(item1, item2, sold);
            recipe.increaseMaxTradeUses(2147483639);
            MerchantRecipeList merchantrecipelist = theIMerchant.getRecipes(mc.player);
			if (merchantrecipelist == null) { return; }
			merchantrecipelist.add(recipe);
            Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist);
        }
		if (flag) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeInt(currentRecipeIndex);
			Objects.requireNonNull(mc.getConnection()).sendPacket(new CPacketCustomPayload("MC|TrSel", packetbuffer));
		}
	}

	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiMerchantAdd.merchantGuiTextures);
		int k = (width - xSize) / 2;
		int l = (height - ySize) / 2;
		drawTexturedModalRect(k, l, 0, 0, xSize, ySize);
		MerchantRecipeList merchantrecipelist = theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
			int i1 = currentRecipeIndex;
			MerchantRecipe merchantrecipe = merchantrecipelist.get(i1);
			if (merchantrecipe.isRecipeDisabled()) {
				mc.getTextureManager().bindTexture(GuiMerchantAdd.merchantGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.disableLighting();
				drawTexturedModalRect(guiLeft + 83, guiTop + 21, 212, 0, 28, 21);
				drawTexturedModalRect(guiLeft + 83, guiTop + 51, 212, 0, 28, 21);
			}
		}
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		fontRenderer.drawString(field_94082_v, xSize / 2 - fontRenderer.getStringWidth(field_94082_v) / 2, 6, CustomNpcResourceListener.DefaultTextColor);
		fontRenderer.drawString(I18n.format("container.inventory"), 8, ySize - 96 + 2, CustomNpcResourceListener.DefaultTextColor);
	}

	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		Minecraft mc = Minecraft.getMinecraft();
		MerchantRecipeList merchantrecipelist = theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
			int k = (width - xSize) / 2;
			int l = (height - ySize) / 2;
			int i1 = currentRecipeIndex;
			MerchantRecipe merchantrecipe = merchantrecipelist.get(i1);
			GlStateManager.pushMatrix();
			ItemStack itemstack = merchantrecipe.getItemToBuy();
			ItemStack itemstack2 = merchantrecipe.getSecondItemToBuy();
			ItemStack itemstack3 = merchantrecipe.getItemToSell();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableColorMaterial();
			GlStateManager.enableLighting();
			itemRender.zLevel = 100.0f;
			itemRender.renderItemAndEffectIntoGUI(itemstack, k + 36, l + 24);
			itemRender.renderItemOverlays(fontRenderer, itemstack, k + 36, l + 24);
            itemRender.renderItemAndEffectIntoGUI(itemstack2, k + 62, l + 24);
            itemRender.renderItemOverlays(fontRenderer, itemstack2, k + 62, l + 24);
            itemRender.renderItemAndEffectIntoGUI(itemstack3, k + 120, l + 24);
			itemRender.renderItemOverlays(fontRenderer, itemstack3, k + 120, l + 24);
			itemRender.zLevel = 0.0f;
			GlStateManager.disableLighting();
			if (isPointInRegion(36, 24, 16, 16, par1, par2)) {
				renderToolTip(itemstack, par1, par2);
			} else if (isPointInRegion(62, 24, 16, 16, par1, par2)) {
				renderToolTip(itemstack2, par1, par2);
			} else if (isPointInRegion(120, 24, 16, 16, par1, par2)) {
				renderToolTip(itemstack3, par1, par2);
			}
			GlStateManager.popMatrix();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
		}
	}

	public void initGui() {
		super.initGui();
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		addButton(nextRecipeButtonIndex = new MerchantButton(1, i + 120 + 27, j + 24 - 1, true));
		addButton(previousRecipeButtonIndex = new MerchantButton(2, i + 36 - 19, j + 24 - 1, false));
		addButton(new GuiNpcButton(4, i + xSize, j + 20, 60, 20, "gui.remove"));
		addButton(new GuiNpcButton(5, i + xSize, j + 50, 60, 20, "gui.add"));
		nextRecipeButtonIndex.enabled = false;
		previousRecipeButtonIndex.enabled = false;
	}

	public void updateScreen() {
		super.updateScreen();
		Minecraft mc = Minecraft.getMinecraft();
		MerchantRecipeList merchantrecipelist = theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null) {
			nextRecipeButtonIndex.enabled = currentRecipeIndex < merchantrecipelist.size() - 1;
			previousRecipeButtonIndex.enabled = currentRecipeIndex > 0;
		}
	}
}
