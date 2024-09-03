package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.Util;

public class GuiNpcFollower extends GuiContainerNPCInterface implements IGuiData {

	private final EntityNPCInterface npc;
	private final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/follower.png");
	private final RoleFollower role;
	private EntityNPCInterface displayNPC;

	public GuiNpcFollower(EntityNPCInterface npc, ContainerNPCFollowerHire container) {
		super(npc, container);
		this.ySize = 224;
		this.npc = npc;
		this.role = (RoleFollower) npc.advanced.roleInterface;
		this.closeOnEsc = true;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
		NBTTagCompound npcNbt = new NBTTagCompound();
		npc.writeEntityToNBT(npcNbt);
		npc.writeToNBTOptional(npcNbt);
		Entity e = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
		if (e instanceof EntityNPCInterface) {
			this.displayNPC = (EntityNPCInterface) e;
			this.displayNPC.display.setShowName(1);
			MarkData.get(this.displayNPC).marks.clear();
			this.displayNPC.rotationYaw = npc.rotationYaw;
			this.displayNPC.rotationPitch = npc.rotationPitch;
			this.displayNPC.ais.orientation = npc.ais.orientation;
			this.displayNPC.ais.setStandingType(1);
			if (npc instanceof EntityCustomNpc && displayNPC instanceof EntityCustomNpc
					&& ((EntityCustomNpc) npc).modelData != null
					&& ((EntityCustomNpc) displayNPC).modelData != null) {
				((EntityCustomNpc) displayNPC).modelData.entity = ((EntityCustomNpc) npc).modelData.entity;
			}
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id < 4) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerExtend, button.id);
		} else {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerState, button.id - 5);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		int index = 0;
		if (!this.role.infiniteDays) {
			for (int slot = 0; slot < this.role.rentalItems.items.size(); ++slot) {
				ItemStack itemstack = this.role.rentalItems.items.get(slot);
				if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
					int days = 1;
					if (this.role.rates.containsKey(slot)) {
						days = this.role.rates.get(slot);
					}
					int yOffset = index * 16;
					int x = this.guiLeft + 68;
					int y = this.guiTop + yOffset + 4;
					GlStateManager.enableRescaleNormal();
					RenderHelper.enableGUIStandardItemLighting();
					this.itemRender.renderItemAndEffectIntoGUI(itemstack, x + 11, y);
					this.itemRender.renderItemOverlays(this.fontRenderer, itemstack, x + 11, y);
					RenderHelper.disableStandardItemLighting();
					GlStateManager.disableRescaleNormal();
					String daysS = days + " "
							+ ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
									: new TextComponentTranslation("follower.days").getFormattedText());
					this.fontRenderer.drawString(" = " + daysS, x + 27, y + 4,
							CustomNpcResourceListener.DefaultTextColor);
					if (this.isPointInRegion(x - this.guiLeft + 11, y - this.guiTop, 16, 16, this.mouseX,
							this.mouseY)) {
						this.renderToolTip(itemstack, this.mouseX, this.mouseY);
					}
					++index;
				}
			}
		}
		int size = this.role.inventory.getSizeInventory();
		if (size >= 0) {
			int s = (size == 2 || size == 4) ? 2 : 3;
			GlStateManager.pushMatrix();
			this.mc.renderEngine.bindTexture(this.resource);
			GlStateManager.translate(this.guiLeft + 172, this.guiTop + 135, 0.0f);
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			this.drawTexturedModalRect(3, 0, 118, 0, 58, 1);
			this.drawTexturedModalRect(2, 1, 117, 1, 59, 1);
			this.drawTexturedModalRect(1, 2, 116, 2, 60, 1);
			this.drawTexturedModalRect(0, 3, 115, 3, 61, 82);
			this.drawTexturedModalRect(0, 85, 115, 220, 61, 4);
			GlStateManager.popMatrix();

			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft + 173, this.guiTop + 141, 0.0f);
			this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
			for (int slotId = 0; slotId < size; slotId++) {
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect((slotId % s) * 18, (slotId / s) * 18, 0, 0, 18, 18);
			}
			GlStateManager.popMatrix();
		}
		if (this.role.rates.containsKey(3) && this.role.rentalMoney > 0) {
			int days = this.role.rates.get(3);
			String daysS = days + " " + ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
					: new TextComponentTranslation("follower.days").getFormattedText());
			String money = Util.instance.getTextReducedNumber(this.role.rentalMoney, true, true, false) + " "
					+ CustomNpcs.displayCurrencies;
			this.fontRenderer.drawString(money + " = " + daysS, this.guiLeft + 80, this.guiTop + 56,
					CustomNpcResourceListener.DefaultTextColor);
		}
		if (displayNPC != null) {
			this.drawNpc(displayNPC, 33, 131, 1.0f, 0, 0, 1);
		} else {
			this.drawNpc(33, 131);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
		long time = (System.currentTimeMillis() - this.role.hiredTime) / 50L;
		this.fontRenderer.drawString(new TextComponentTranslation("follower.health").getFormattedText() + ": "
				+ this.npc.getHealth() + "/" + this.npc.getMaxHealth(), 62, 70,
				CustomNpcResourceListener.DefaultTextColor);
		if (!this.role.infiniteDays) {
			this.fontRenderer.drawString(
					new TextComponentTranslation("follower.daysleft").getFormattedText() + " " + Util.instance
							.ticksToElapsedTime((this.role.getDays() * 28800L) - time, false, true, false),
					62, 82, CustomNpcResourceListener.DefaultTextColor);
		}
		this.fontRenderer.drawString(
				new TextComponentTranslation("follower.lastday").getFormattedText() + ": "
						+ Util.instance.ticksToElapsedTime(time, false, true, false),
				62, 94, CustomNpcResourceListener.DefaultTextColor);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < 3; ++i) {
			if (this.getButton(i) == null) {
				continue;
			}
			this.getButton(i).setEnabled(this.mc.player.capabilities.isCreativeMode || Util.instance.canRemoveItems(
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
		if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.move").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("follower.hover.fire").getFormattedText());
		} else {
			for (int i = 0; i < 4; ++i) {
				if (this.getButton(i) != null && this.getButton(i).isMouseOver()) {
					this.setHoverText(new TextComponentTranslation("follower.hover.extend").getFormattedText());
				}
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.buttonList.clear();
		int x = this.guiLeft + 12;
		int y = this.guiTop - 11;
		if (!this.role.infiniteDays) {
			for (int i = 0; i < 3; ++i) {
				if (this.role.rentalItems.getStackInSlot(i).isEmpty()) {
					continue;
				}
				this.addButton(new GuiNpcButton(i, x, y += 16, 60, 13,
						new TextComponentTranslation("follower.extend").getFormattedText()));
			}
		}
		if (this.role.rates.containsKey(3) && this.role.rentalMoney > 0) {
			this.addButton(new GuiNpcButton(3, x, y + 16, 60, 13, new TextComponentTranslation("follower.extend").getFormattedText()));
		}
		x += 52;
		y = this.guiTop + 105;
		this.addButton(new GuiNpcButton(5, x, y, 50, 14,
				new String[] { new TextComponentTranslation("follower.waiting").getFormattedText(),
						new TextComponentTranslation("follower.following").getFormattedText() },
				(this.role.isFollowing ? 0 : 1)));
		this.addButton(new GuiNpcButton(6, x + 54, y, 50, 14,
				new TextComponentTranslation("follower.fire").getFormattedText()));
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.npc.advanced.roleInterface.readFromNBT(compound);
		this.initGui();
	}
}
