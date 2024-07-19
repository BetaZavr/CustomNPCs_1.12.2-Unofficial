package noppes.npcs.client.gui;

import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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
public class GuiMerchantAdd extends GuiContainer {

	@SideOnly(Side.CLIENT)
	static class MerchantButton extends GuiButton {

		private final boolean forward;

		public MerchantButton(int buttonId, int x, int y, boolean forward) {
			super(buttonId, x, y, 12, 19, "");
			this.forward = forward;
		}

		public void drawButton(@Nonnull Minecraft minecraft, int mouseX, int mouseY, float partialTicks) {
			if (this.visible) {
				minecraft.renderEngine.bindTexture(GuiMerchantAdd.merchantGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
						&& mouseY < this.y + this.height;
				int k = 0;
				int l = 176;
				if (!this.enabled) {
					l += this.width * 2;
				} else if (flag) {
					l += this.width;
				}
				if (!this.forward) {
					k += this.height;
				}
				this.drawTexturedModalRect(this.x, this.y, l, k, this.width, this.height);
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
		this.theIMerchant = ServerEventsHandler.Merchant;
		this.field_94082_v = I18n.format("entity.Villager.name");
	}

	protected void actionPerformed(@Nonnull GuiButton par1GuiButton) {
		boolean flag = false;
		Minecraft mc = Minecraft.getMinecraft();
		if (par1GuiButton == this.nextRecipeButtonIndex) {
			++this.currentRecipeIndex;
			flag = true;
		} else if (par1GuiButton == this.previousRecipeButtonIndex) {
			--this.currentRecipeIndex;
			flag = true;
		}
		if (par1GuiButton.id == 4) {
			MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.player);
            if (merchantrecipelist != null && this.currentRecipeIndex < merchantrecipelist.size()) {
				merchantrecipelist.remove(this.currentRecipeIndex);
				if (this.currentRecipeIndex > 0) {
					--this.currentRecipeIndex;
				}
				Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist);
			}
		}
		if (par1GuiButton.id == 5) {
			ItemStack item1 = this.inventorySlots.getSlot(0).getStack();
			ItemStack item2 = this.inventorySlots.getSlot(1).getStack();
			ItemStack sold = this.inventorySlots.getSlot(2).getStack();
            item1 = item1.copy();
            sold = sold.copy();
            item2 = item2.copy();
            MerchantRecipe recipe = new MerchantRecipe(item1, item2, sold);
            recipe.increaseMaxTradeUses(2147483639);
            MerchantRecipeList merchantrecipelist2 = this.theIMerchant.getRecipes(mc.player);
			if (merchantrecipelist2 == null) { return; }
            merchantrecipelist2.add(recipe);
            Client.sendData(EnumPacketServer.MerchantUpdate, ServerEventsHandler.Merchant.getEntityId(), merchantrecipelist2);
        }
		if (flag) {
			PacketBuffer packetbuffer = new PacketBuffer(Unpooled.buffer());
			packetbuffer.writeInt(this.currentRecipeIndex);
			Objects.requireNonNull(this.mc.getConnection()).sendPacket(new CPacketCustomPayload("MC|TrSel", packetbuffer));
		}
	}

	protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.renderEngine.bindTexture(GuiMerchantAdd.merchantGuiTextures);
		int k = (this.width - this.xSize) / 2;
		int l = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(k, l, 0, 0, this.xSize, this.ySize);
		MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
			int i1 = this.currentRecipeIndex;
			MerchantRecipe merchantrecipe = merchantrecipelist.get(i1);
			if (merchantrecipe.isRecipeDisabled()) {
				mc.renderEngine.bindTexture(GuiMerchantAdd.merchantGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				GlStateManager.disableLighting();
				this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 21, 212, 0, 28, 21);
				this.drawTexturedModalRect(this.guiLeft + 83, this.guiTop + 51, 212, 0, 28, 21);
			}
		}
	}

	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		this.fontRenderer.drawString(this.field_94082_v, this.xSize / 2 - this.fontRenderer.getStringWidth(this.field_94082_v) / 2, 6, CustomNpcResourceListener.DefaultTextColor);
		this.fontRenderer.drawString(I18n.format("container.inventory"), 8, this.ySize - 96 + 2, CustomNpcResourceListener.DefaultTextColor);
	}

	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		Minecraft mc = Minecraft.getMinecraft();
		MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null && !merchantrecipelist.isEmpty()) {
			int k = (this.width - this.xSize) / 2;
			int l = (this.height - this.ySize) / 2;
			int i1 = this.currentRecipeIndex;
			MerchantRecipe merchantrecipe = merchantrecipelist.get(i1);
			GlStateManager.pushMatrix();
			ItemStack itemstack = merchantrecipe.getItemToBuy();
			ItemStack itemstack2 = merchantrecipe.getSecondItemToBuy();
			ItemStack itemstack3 = merchantrecipe.getItemToSell();
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableRescaleNormal();
			GlStateManager.enableColorMaterial();
			GlStateManager.enableLighting();
			this.itemRender.zLevel = 100.0f;
			this.itemRender.renderItemAndEffectIntoGUI(itemstack, k + 36, l + 24);
			this.itemRender.renderItemOverlays(this.fontRenderer, itemstack, k + 36, l + 24);
            this.itemRender.renderItemAndEffectIntoGUI(itemstack2, k + 62, l + 24);
            this.itemRender.renderItemOverlays(this.fontRenderer, itemstack2, k + 62, l + 24);
            this.itemRender.renderItemAndEffectIntoGUI(itemstack3, k + 120, l + 24);
			this.itemRender.renderItemOverlays(this.fontRenderer, itemstack3, k + 120, l + 24);
			this.itemRender.zLevel = 0.0f;
			GlStateManager.disableLighting();
			if (this.isPointInRegion(36, 24, 16, 16, par1, par2)) {
				this.renderToolTip(itemstack, par1, par2);
			} else if (this.isPointInRegion(62, 24, 16, 16, par1, par2)) {
				this.renderToolTip(itemstack2, par1, par2);
			} else if (this.isPointInRegion(120, 24, 16, 16, par1, par2)) {
				this.renderToolTip(itemstack3, par1, par2);
			}
			GlStateManager.popMatrix();
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
		}
	}

	public void initGui() {
		super.initGui();
		int i = (this.width - this.xSize) / 2;
		int j = (this.height - this.ySize) / 2;
		this.buttonList.add(this.nextRecipeButtonIndex = new MerchantButton(1, i + 120 + 27, j + 24 - 1, true));
		this.buttonList.add(this.previousRecipeButtonIndex = new MerchantButton(2, i + 36 - 19, j + 24 - 1, false));
		this.buttonList.add(new GuiNpcButton(4, i + this.xSize, j + 20, 60, 20, "gui.remove"));
		this.buttonList.add(new GuiNpcButton(5, i + this.xSize, j + 50, 60, 20, "gui.add"));
		this.nextRecipeButtonIndex.enabled = false;
		this.previousRecipeButtonIndex.enabled = false;
	}

	public void updateScreen() {
		super.updateScreen();
		Minecraft mc = Minecraft.getMinecraft();
		MerchantRecipeList merchantrecipelist = this.theIMerchant.getRecipes(mc.player);
		if (merchantrecipelist != null) {
			this.nextRecipeButtonIndex.enabled = (this.currentRecipeIndex < merchantrecipelist.size() - 1);
			this.previousRecipeButtonIndex.enabled = (this.currentRecipeIndex > 0);
		}
	}
}
