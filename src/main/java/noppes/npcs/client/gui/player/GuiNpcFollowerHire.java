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
import noppes.npcs.util.Util;

public class GuiNpcFollowerHire extends GuiContainerNPCInterface {

	private static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/followerhire.png");

	private final RoleFollower role;
	public ContainerNPCFollowerHire container;
	public EntityNPCInterface npc;

	public GuiNpcFollowerHire(EntityNPCInterface npc, ContainerNPCFollowerHire container) {
		super(npc, container);
		this.container = container;
		this.npc = npc;
		role = (RoleFollower) npc.advanced.roleInterface;
		closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerHire, button.id);
		this.close();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(resource);
		int w = (width - xSize) / 2;
		int h = (height - ySize) / 2;
		this.drawTexturedModalRect(w, h, 0, 0, xSize, ySize);
		int index = 0;
		for (int slot = 0; slot < role.rentalItems.items.size(); ++slot) {
			ItemStack itemstack = role.rentalItems.items.get(slot);
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
		if (role.rates.containsKey(3) && role.rentalMoney > 0) {
			int days = this.role.rates.get(3);
			String daysS = days + " " + ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText() : new TextComponentTranslation("follower.days").getFormattedText());
			String money = Util.instance.getTextReducedNumber(role.rentalMoney, true, true, false) + " " + CustomNpcs.displayCurrencies;
			fontRenderer.drawString(money + " = " + daysS, guiLeft + 90, guiTop + 68, CustomNpcResourceListener.DefaultTextColor);
		}
	}

    @Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < 3; ++i) {
			if (getButton(i) == null) {
				continue;
			}
			getButton(i).setEnabled(mc.player.capabilities.isCreativeMode || Util.instance.canRemoveItems(mc.player.inventory.mainInventory, this.role.rentalItems.getStackInSlot(i), false, false));
		}
		if (getButton(3) != null) {
			getButton(3).setEnabled(mc.player.capabilities.isCreativeMode || ClientProxy.playerData.game.getMoney() >= role.rentalMoney);
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
		int x = guiLeft + 26;
		int y = guiTop - 7;
		for (int i = 0; i < 3; ++i) {
			if (role.rentalItems.getStackInSlot(i).isEmpty()) { continue; }
			addButton(new GuiNpcButton(i, x, y += 18, 50, 14, new TextComponentTranslation("follower.hire").getFormattedText()));
		}
		if (role.rates.containsKey(3) && role.rentalMoney > 0) {
			addButton(new GuiNpcButton(3, x, y + 18, 50, 14, new TextComponentTranslation("follower.hire").getFormattedText()));
		}
	}

	@Override
	public void save() {
	}

}
