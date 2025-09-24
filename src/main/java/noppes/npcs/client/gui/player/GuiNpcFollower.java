package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiNpcFollower extends GuiContainerNPCInterface implements IGuiData {

	protected final RoleFollower role;
	protected EntityNPCInterface displayNPC;

	public GuiNpcFollower(EntityNPCInterface npc, ContainerNPCFollowerHire container) {
		super(npc, container);
		setBackground("follower.png");
		closeOnEsc = true;
		ySize = 224;

		role = (RoleFollower) npc.advanced.roleInterface;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
		NBTTagCompound npcNbt = new NBTTagCompound();
		npc.writeEntityToNBT(npcNbt);
		npc.writeToNBTOptional(npcNbt);
		Entity e = EntityList.createEntityFromNBT(npcNbt, mc.world);
		if (e instanceof EntityNPCInterface) {
			displayNPC = (EntityNPCInterface) e;
			displayNPC.display.setShowName(1);
			MarkData.get(displayNPC).marks.clear();
			displayNPC.rotationYaw = npc.rotationYaw;
			displayNPC.rotationPitch = npc.rotationPitch;
			displayNPC.ais.orientation = npc.ais.orientation;
			displayNPC.ais.setStandingType(1);
			if (npc instanceof EntityCustomNpc && displayNPC instanceof EntityCustomNpc
					&& ((EntityCustomNpc) npc).modelData != null
					&& ((EntityCustomNpc) displayNPC).modelData != null) {
				((EntityCustomNpc) displayNPC).modelData.entity = ((EntityCustomNpc) npc).modelData.entity;
			}
		}
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() < 4) { NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerExtend, button.getID()); }
		else {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerState, button.getID() - 5);
			if (button.getID() == 6) { onClosed(); }
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		int index = 0;
		if (!role.infiniteDays) {
			for (int slot = 0; slot < role.rentalItems.items.size(); ++slot) {
				ItemStack itemstack = role.rentalItems.items.get(slot);
				if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
					int days = 1;
					if (role.rates.containsKey(slot)) { days = role.rates.get(slot); }
					int yOffset = index * 16;
					int x = guiLeft + 68;
					int y = guiTop + yOffset + 4;
					GlStateManager.enableRescaleNormal();
					RenderHelper.enableGUIStandardItemLighting();
					itemRender.renderItemAndEffectIntoGUI(itemstack, x + 11, y);
					itemRender.renderItemOverlays(fontRenderer, itemstack, x + 11, y);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.disableRescaleNormal();
					String daysS = days + " "
							+ ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
									: new TextComponentTranslation("follower.days").getFormattedText());
					fontRenderer.drawString(" = " + daysS, x + 27, y + 4,
							CustomNpcResourceListener.DefaultTextColor);
					if (isPointInRegion(x - guiLeft + 11, y - guiTop, 16, 16, mouseX,
							mouseY)) {
						renderToolTip(itemstack, mouseX, mouseY);
					}
					++index;
				}
			}
		}
		int size = role.inventory.getSizeInventory();
		if (size > 0) {
			int s = (size == 2 || size == 4) ? 2 : 3;
			GlStateManager.pushMatrix();
			mc.getTextureManager().bindTexture(background);
			GlStateManager.translate(guiLeft + 172, guiTop + 135, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			drawTexturedModalRect(3, 0, 118, 0, 58, 1);
			drawTexturedModalRect(2, 1, 117, 1, 59, 1);
			drawTexturedModalRect(1, 2, 116, 2, 60, 1);
			drawTexturedModalRect(0, 3, 115, 3, 61, 82);
			drawTexturedModalRect(0, 85, 115, 220, 61, 4);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft + 173, guiTop + 141, 0.0f);
			mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			for (int slotId = 0; slotId < size; slotId++) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect((slotId % s) * 18, (slotId / s) * 18, 0, 0, 18, 18);
			}
			GlStateManager.popMatrix();
		}
		if (role.rates.containsKey(3) && role.rentalMoney > 0) {
			int days = role.rates.get(3);
			String daysS = days + " " + ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText() : new TextComponentTranslation("follower.days").getFormattedText());
			String money = Util.instance.getTextReducedNumber(role.rentalMoney, true, true, false) + " " + CustomNpcs.displayCurrencies;
			fontRenderer.drawString(money + " = " + daysS, guiLeft + 80, guiTop + 56, CustomNpcResourceListener.DefaultTextColor);
		}
		if (displayNPC != null) { drawNpc(displayNPC, 33, 131, 1.0f, 0, 0, 1); }
		else { drawNpc(33, 131); }
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		long time = (System.currentTimeMillis() - role.hiredTime) / 50L;
		fontRenderer.drawString(new TextComponentTranslation("follower.health").getFormattedText() + ": " + npc.getHealth() + "/" + npc.getMaxHealth(), 62, 70, CustomNpcResourceListener.DefaultTextColor);
		if (!role.infiniteDays) {
			fontRenderer.drawString(new TextComponentTranslation("follower.daysleft").getFormattedText() + " " + Util.instance.ticksToElapsedTime((role.getDays() * 28800L) - time, false, true, false), 62, 82, CustomNpcResourceListener.DefaultTextColor);
		}
		fontRenderer.drawString(new TextComponentTranslation("follower.lastday").getFormattedText() + ": " + Util.instance.ticksToElapsedTime(time, false, true, false), 62, 94, CustomNpcResourceListener.DefaultTextColor);
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
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 12;
		int y = guiTop - 11;
		GuiNpcButton button;
		if (!role.infiniteDays) {
			for (int i = 0; i < 3; ++i) {
				if (role.rentalItems.getStackInSlot(i).isEmpty()) { continue; }
				button = new GuiNpcButton(i, x, y += 16, 60, 13, new TextComponentTranslation("follower.extend").getFormattedText());
				button.setHoverText("follower.hover.extend");
				addButton(button);
			}
		}
		if (role.rates.containsKey(3) && role.rentalMoney > 0) {
			button = new GuiNpcButton(3, x, guiTop + 53, 60, 13, new TextComponentTranslation("follower.extend").getFormattedText());
			button.setHoverText("follower.hover.extend");
			addButton(button);
		}
		x += 52;
		y = guiTop + 105;
		button = new GuiNpcButton(5, x, y, 50, 14, new String[] { new TextComponentTranslation("follower.waiting").getFormattedText(), new TextComponentTranslation("follower.following").getFormattedText() }, (role.isFollowing ? 0 : 1));
		button.setHoverText("follower.hover.move");
		addButton(button);
		button = new GuiNpcButton(6, x + 54, y, 50, 14, new TextComponentTranslation("follower.fire").getFormattedText());
		button.setHoverText("follower.hover.fire");
		addButton(button);
	}

    @Override
	public void setGuiData(NBTTagCompound compound) {
		npc.advanced.roleInterface.load(compound);
		initGui();
	}

}
