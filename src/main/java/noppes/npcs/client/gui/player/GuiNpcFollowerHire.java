package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleFollower;

public class GuiNpcFollowerHire
extends GuiContainerNPCInterface {
	
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
		super.buttonEvent(button);
		if (button.id == 5) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.FollowerHire, new Object[0]);
			this.close();
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		int l = (this.width - this.xSize) / 2;
		int i2 = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(l, i2, 0, 0, this.xSize, this.ySize);
		int index = 0;
		for (int slot = 0; slot < this.role.inventory.items.size(); ++slot) {
			ItemStack itemstack = this.role.inventory.items.get(slot);
			if (!NoppesUtilServer.IsItemStackNull(itemstack)) {
				int days = 1;
				if (this.role.rates.containsKey(slot)) {
					days = this.role.rates.get(slot);
				}
				int yOffset = index * 26;
				int x = this.guiLeft + 78;
				int y = this.guiTop + yOffset + 10;
				GlStateManager.enableRescaleNormal();
				RenderHelper.enableGUIStandardItemLighting();
				this.itemRender.renderItemAndEffectIntoGUI(itemstack, x + 11, y);
				this.itemRender.renderItemOverlays(this.fontRenderer, itemstack, x + 11, y);
				RenderHelper.disableStandardItemLighting();
				GlStateManager.disableRescaleNormal();
				String daysS = days + " " + ((days == 1) ? new TextComponentTranslation("follower.day").getFormattedText()
						: new TextComponentTranslation("follower.days").getFormattedText());
				this.fontRenderer.drawString(" = " + daysS, x + 27, y + 4, CustomNpcResourceListener.DefaultTextColor);
				if (this.isPointInRegion(x - this.guiLeft + 11, y - this.guiTop, 16, 16, this.mouseX, this.mouseY)) {
					this.renderToolTip(itemstack, this.mouseX, this.mouseY);
				}
				++index;
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int par1, int par2) {
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(5, this.guiLeft + 26, this.guiTop + 60, 50, 20,
				new TextComponentTranslation("follower.hire").getFormattedText()));
	}

	@Override
	public void save() {
	}
}
