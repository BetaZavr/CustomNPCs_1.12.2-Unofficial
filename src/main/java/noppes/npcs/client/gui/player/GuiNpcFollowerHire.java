package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
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

import javax.annotation.Nonnull;

public class GuiNpcFollowerHire extends GuiContainerNPCInterface {

	protected final RoleFollower role;

	public GuiNpcFollowerHire(EntityNPCInterface npc, ContainerNPCFollowerHire cont) {
		super(npc, cont);
		setBackground("followerhire.png");
		closeOnEsc = true;

		role = (RoleFollower) npc.advanced.roleInterface;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerHire, button.getID());
		onClosed();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		int index = 0;
		for (int slot = 0; slot < role.rentalItems.items.size(); ++slot) {
			ItemStack itemstack = role.rentalItems.items.get(slot);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				int days = 1;
				if (role.rates.containsKey(slot)) { days = role.rates.get(slot); }
				int yOffset = index * 18;
				int x = guiLeft + 78;
				int y = guiTop + yOffset + 10;
				GlStateManager.enableRescaleNormal();
				RenderHelper.enableGUIStandardItemLighting();
				itemRender.renderItemAndEffectIntoGUI(itemstack, x + 11, y);
				itemRender.renderItemOverlays(fontRenderer, itemstack, x + 11, y);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				String daysS = days + " "
						+ ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
								: new TextComponentTranslation("follower.days").getFormattedText());
				fontRenderer.drawString(" = " + daysS, x + 27, y + 4, CustomNpcResourceListener.DefaultTextColor);
				if (isPointInRegion(x - guiLeft + 11, y - guiTop, 16, 16, mouseX, mouseY)) {
					renderToolTip(itemstack, mouseX, mouseY);
				}
				++index;
			}
		}
		if (role.rates.containsKey(3) && role.rentalMoney > 0) {
			int days = role.rates.get(3);
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
			getButton(i).setIsEnable(mc.player.capabilities.isCreativeMode || Util.instance.canRemoveItems(mc.player.inventory.mainInventory, role.rentalItems.getStackInSlot(i), false, false));
		}
		if (getButton(3) != null) {
			getButton(3).setIsEnable(mc.player.capabilities.isCreativeMode || ClientProxy.playerData.game.getMoney() >= role.rentalMoney);
		}
		for (int i = 0; i < 4; ++i) {
			if (getButton(i) != null && getButton(i).isHovered()) {
				ITextComponent mes = new TextComponentTranslation("follower.hover.hire.info");
				if (role.disableGui) {
					mes.appendSibling(new TextComponentString("<br>" + ((char) 167) + "7"));
					mes.appendSibling(new TextComponentTranslation("follower.hover.disable.gui"));
				}
				if (role.infiniteDays) {
					mes.appendSibling(new TextComponentString("<br>" + ((char) 167) + "7"));
					mes.appendSibling(new TextComponentTranslation("follower.hover.infinite"));
				}
				putHoverText(mes.getFormattedText());
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
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
			addButton(new GuiNpcButton(3, x, guiTop + 65, 50, 14, new TextComponentTranslation("follower.hire").getFormattedText()));
		}
	}

}
