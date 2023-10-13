package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCBankInterface;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCBankChest
extends GuiContainerNPCInterface
implements IGuiData {
	
	private int availableSlots;
	private ContainerNPCBankInterface container;
	private ItemStack currency;
	private int maxSlots;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/bankchest.png");
	private ResourceLocation edit = new ResourceLocation(CustomNpcs.MODID, "textures/gui/largebg.png");
	private int unlockedSlots;

	public GuiNPCBankChest(EntityNPCInterface npc, ContainerNPCBankInterface container) {
		super(npc, container);
		this.availableSlots = 0;
		this.maxSlots = 1;
		this.unlockedSlots = 1;
		this.container = container;
		this.title = "";
		this.allowUserInput = false;
		this.ySize = 235;
		this.closeOnEsc = true;
	}

	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		String name = "";
		if (ContainerNPCBankInterface.editBank!=null) {
			name = ContainerNPCBankInterface.editBank.editPlayer;
		}
		if (id < 6) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankSlotOpen, id, this.container.bankid, name);
		}
		else if (id == 8) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankUnlock, name);
		}
		else if (id == 9) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankUpgrade, name);
		}
		else if (id == 10) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.BankRegrade, name);
		}
		else if (id == 11) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.Banklock, name, this.container.slot);
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		int l = (this.width - this.xSize) / 2;
		int i2 = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(l, i2, 0, 0, this.xSize, 6);
		if (!this.container.isAvailable() && ContainerNPCBankInterface.editBank==null) {
			this.drawTexturedModalRect(l, i2 + 6, 0, 6, this.xSize, 64);
			this.drawTexturedModalRect(l, i2 + 70, 0, 124, this.xSize, 98);
			int x = this.guiLeft + 30;
			int y = this.guiTop + 8;
			this.fontRenderer.drawString(new TextComponentTranslation("bank.unlockCosts").getFormattedText() + ":", x, y + 4, CustomNpcResourceListener.DefaultTextColor);
			this.drawItem(x + 90, y, this.currency, i, j);
		} else if (this.container.isUpgraded()) {
			this.drawTexturedModalRect(l, i2 + 60, 0, 60, this.xSize, 162);
			this.drawTexturedModalRect(l, i2 + 6, 0, 60, this.xSize, 64);
		} else if (this.container.canBeUpgraded()) {
			this.drawTexturedModalRect(l, i2 + 6, 0, 6, this.xSize, 216);
			int x = this.guiLeft + 30;
			int y = this.guiTop + 8;
			if (ContainerNPCBankInterface.editBank==null) {
				this.fontRenderer.drawString(new TextComponentTranslation("bank.upgradeCosts").getFormattedText() + ":", x, y + 4, CustomNpcResourceListener.DefaultTextColor);
				this.drawItem(x + 90, y, this.currency, i, j);
			} else {
				this.mc.renderEngine.bindTexture(this.edit);
				this.drawTexturedModalRect(l, i2 + 6, 0, 60, this.xSize - 4, 60);
			}
		} else {
			this.drawTexturedModalRect(l, i2 + 6, 0, 60, this.xSize, 162);
			if (ContainerNPCBankInterface.editBank!=null && !this.container.isAvailable()) {
				this.mc.renderEngine.bindTexture(this.edit);
				this.drawTexturedModalRect(l, i2 + 6, 0, 60, this.xSize - 4, 67);
			}
		}
		super.drawGuiContainerBackgroundLayer(f, i, j);
	}

	private void drawItem(int x, int y, ItemStack item, int mouseX, int mouseY) {
		if (NoppesUtilServer.IsItemStackNull(item)) {
			return;
		}
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		this.itemRender.renderItemAndEffectIntoGUI(item, x, y);
		this.itemRender.renderItemOverlays(this.fontRenderer, item, x, y);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		if (this.isPointInRegion(x - this.guiLeft, y - this.guiTop, 16, 16, mouseX, mouseY)) {
			this.renderToolTip(item, mouseX, mouseY);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.availableSlots = 0;
		if (this.maxSlots > 1) {
			for (int i = 0; i < this.maxSlots; ++i) {
				GuiNpcButton button = new GuiNpcButton(i, this.guiLeft - 50, this.guiTop + 10 + i * 24, 50, 20, new TextComponentTranslation("gui.tab").getFormattedText() + " " + (i + 1));
				if (i > this.unlockedSlots) {
					button.setEnabled(false);
				}
				this.addButton(button);
				++this.availableSlots;
			}
			if (this.availableSlots == 1) {
				this.buttonList.clear();
			}
		}
		if (!this.container.isAvailable()) {
			this.addButton(new GuiNpcButton(8, this.guiLeft + 48, this.guiTop + 48, 80, 20, new TextComponentTranslation("bank.unlock").getFormattedText()));
		} else if (this.container.canBeUpgraded()) {
			this.addButton(new GuiNpcButton(9, this.guiLeft + 48, this.guiTop + 48, 80, 20, new TextComponentTranslation("bank.upgrade").getFormattedText()));
		}
		if (this.maxSlots > 1) {
			this.getButton(this.container.slot).visible = true;
			this.getButton(this.container.slot).setEnabled(true);
			this.getButton(this.container.slot).layerColor = 0xFF00FFFF;
		}
		String bankName = "";
		Bank bank = BankController.getInstance().banks.get(this.container.bankid);
		if (bank!=null) { bankName = ((char) 167) + "l" + new TextComponentTranslation(bank.name).getFormattedText(); }
		if (ContainerNPCBankInterface.editBank!=null) {
			bankName += " ("+new TextComponentTranslation("display.player").getFormattedText() + ": " + ((char) 167) + "l" + ContainerNPCBankInterface.editBank.editPlayer + ((char) 167) + "r)";
			this.addButton(new GuiNpcButton(10, this.guiLeft + this.xSize, this.guiTop + 34, 90, 20, "bank.regrade"));
			this.getButton(10).enabled = this.container.isAvailable() && bank.canBeUpgraded(this.container.slot);
			this.addButton(new GuiNpcButton(11, this.guiLeft + this.xSize, this.guiTop + 56, 90, 20, "bank.lock"));
			this.getButton(11).enabled = this.container.isAvailable();
			if (this.getButton(9)!=null) {
				this.getButton(9).x = this.guiLeft + this.xSize;
				this.getButton(9).y = this.guiTop + 12;
				this.getButton(9).width = 90;
				this.getButton(9).enabled = this.container.canBeUpgraded();
			} else if (this.getButton(8)!=null) {
				this.getButton(8).x = this.guiLeft + this.xSize;
				this.getButton(8).y = this.guiTop + 12;
				this.getButton(8).width = 90;
				this.getButton(8).enabled = !this.container.isAvailable();
			}
		}
		this.addLabel(new GuiNpcLabel(0, bankName, this.guiLeft + (this.xSize - this.fontRenderer.getStringWidth(bankName)) / 2, this.guiTop + 3));
	}

	@Override
	public void close() {
		super.close();
		if (ContainerNPCBankInterface.editBank!=null) {
			GuiNpcManagePlayerData dataGui = new GuiNpcManagePlayerData(this.npc, new GuiNPCGlobalMainMenu(this.npc));
			dataGui.selectedPlayer = ContainerNPCBankInterface.editBank.editPlayer;
			NoppesUtil.openGUI((EntityPlayer) this.player, dataGui);
		}
	}
	
	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.maxSlots = compound.getInteger("MaxSlots");
		this.unlockedSlots = compound.getInteger("UnlockedSlots");
		if (compound.hasKey("Currency")) {
			this.currency = new ItemStack(compound.getCompoundTag("Currency"));
		} else {
			this.currency = ItemStack.EMPTY;
		}
		if (this.container.currency != null) {
			this.container.currency.item = this.currency;
		}
		this.initGui();
	}
}
