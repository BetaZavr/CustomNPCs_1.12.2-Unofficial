package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.AdditionalMethods;

public class GuiNpcFollowerHire extends GuiContainerNPCInterface {

	public ContainerNPCFollowerHire container;
	public EntityNPCInterface npc;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/followerhire.png");
	private RoleFollower role;

	public GuiNpcFollowerHire(EntityNPCInterface npc, ContainerNPCFollowerHire container) {
		super(npc, container);
		this.container = container;
		this.npc = npc;
		this.role = (RoleFollower) npc.advanced.roleInterface;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerHire, button.id);
		this.close();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		int l = (this.width - this.xSize) / 2;
		int i2 = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(l, i2, 0, 0, this.xSize, this.ySize);
		int index = 0;
		for (int slot = 0; slot < this.role.rentalItems.items.size(); ++slot) {
			ItemStack itemstack = this.role.rentalItems.items.get(slot);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				int days = 1;
				if (this.role.rates.containsKey(slot)) {
					days = this.role.rates.get(slot);
				}
				int yOffset = index * 18;
				int x = this.guiLeft + 78;
				int y = this.guiTop + yOffset + 10;
				GlStateManager.enableRescaleNormal();
				RenderHelper.enableGUIStandardItemLighting();
				this.itemRender.renderItemAndEffectIntoGUI(itemstack, x + 11, y);
				this.itemRender.renderItemOverlays(this.fontRenderer, itemstack, x + 11, y);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				String daysS = days + " "
						+ ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
								: new TextComponentTranslation("follower.days").getFormattedText());
				this.fontRenderer.drawString(" = " + daysS, x + 27, y + 4, CustomNpcResourceListener.DefaultTextColor);
				if (this.isPointInRegion(x - this.guiLeft + 11, y - this.guiTop, 16, 16, this.mouseX, this.mouseY)) {
					this.renderToolTip(itemstack, this.mouseX, this.mouseY);
				}
				++index;
			}
		}
		if (this.role.rates.containsKey(3) && this.role.rentalMoney > 0) {
			int days = this.role.rates.get(3);
			String daysS = days + " " + ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
					: new TextComponentTranslation("follower.days").getFormattedText());
			String money = AdditionalMethods.getTextReducedNumber(this.role.rentalMoney, true, true, false) + " "
					+ CustomNpcs.CharCurrencies;
			this.fontRenderer.drawString(money + " = " + daysS, this.guiLeft + 90, this.guiTop + 68,
					CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < 3; ++i) {
			if (this.getButton(i) == null) {
				continue;
			}
			this.getButton(i).setEnabled(this.mc.player.capabilities.isCreativeMode || AdditionalMethods.canRemoveItems(
					this.mc.player.inventory.mainInventory, this.role.rentalItems.getStackInSlot(i), false, false));
		}
		if (this.getButton(3) != null) {
			this.getButton(3).setEnabled(this.mc.player.capabilities.isCreativeMode
					|| ClientProxy.playerData.game.getMoney() >= this.role.rentalMoney);
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		for (int i = 0; i < 4; ++i) {
			if (this.getButton(i) != null && this.getButton(i).isMouseOver()) {
				ITextComponent mes = new TextComponentTranslation("follower.hover.hire.info");
				if (this.role.disableGui) {
					mes.appendSibling(new TextComponentString("<br>" + ((char) 167) + "7"));
					mes.appendSibling(new TextComponentTranslation("follower.hover.disable.gui"));
				}
				if (this.role.infiniteDays) {
					mes.appendSibling(new TextComponentString("<br>" + ((char) 167) + "7"));
					mes.appendSibling(new TextComponentTranslation("follower.hover.infinite"));
				}
				this.setHoverText(mes.getFormattedText());
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 26;
		int y = this.guiTop - 7;
		for (int i = 0; i < 3; ++i) {
			if (this.role.rentalItems.getStackInSlot(i).isEmpty()) {
				continue;
			}
			this.addButton(new GuiNpcButton(i, x, y += 18, 50, 14,
					new TextComponentTranslation("follower.hire").getFormattedText()));
		}
		if (this.role.rates.containsKey(3) && this.role.rentalMoney > 0) {
			this.addButton(new GuiNpcButton(3, x, y += 18, 50, 14,
					new TextComponentTranslation("follower.hire").getFormattedText()));
		}
	}

	@Override
	public void save() {
	}
}
