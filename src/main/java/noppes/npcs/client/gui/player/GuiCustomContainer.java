package noppes.npcs.client.gui.player;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.containers.ContainerChestCustom;

public class GuiCustomContainer // GuiChest
extends GuiContainer {

	private static final ResourceLocation backTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation slotTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private static final ResourceLocation tabsTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private ContainerChestCustom inventorySlots;
	
	private int guiColor, row, maxRows, w, h;
	private int[] guiColorArr;
	private boolean isMany;
	
	private float currentScroll;
    private boolean isScrolling;
	
	public GuiCustomContainer(ContainerChestCustom container) {
		super(container);
		this.mc = Minecraft.getMinecraft();
		this.inventorySlots = container;
		this.isMany = container.customChest.getSizeInventory() > 45;
		this.allowUserInput = false;
		this.row = 0;
		this.maxRows = (int) Math.ceil((double) container.inventorySlots.size() / 9.0d) - 5;
		this.guiColor = container.customChest.guiColor;
		this.guiColorArr = container.customChest.guiColorArr;
		this.ySize = 114 + this.inventorySlots.height;
        this.resetSlots();
	}

	@Override
	public void initGui() {
        super.initGui();
        this.resetSlots();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (this.guiColor==-1) { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
		else { GlStateManager.color((float)(this.guiColor >> 16 & 255) / 255.0F, (float)(this.guiColor >> 8 & 255) / 255.0F, (float)(this.guiColor & 255) / 255.0F, 1.0F); }
		this.mc.getTextureManager().bindTexture(backTexture);
		int u = (this.width - this.xSize) / 2;
		int v = (this.height - this.ySize) / 2;
		this.h = this.inventorySlots.height + 107;
		this.w = this.isMany ? 10 : 0;
		// Main
		if (this.guiColorArr!=null && this.guiColorArr.length>1) {
			float r, g, b;
			float r0 = (float)(this.guiColorArr[0] >> 16 & 255) / 255.0F;
	        float g0 = (float)(this.guiColorArr[0] >> 8 & 255) / 255.0F;
	        float b0 = (float)(this.guiColorArr[0] & 255) / 255.0F;
	        float r1 = (float)(this.guiColorArr[1] >> 16 & 255) / 255.0F;
	        float g1 = (float)(this.guiColorArr[1] >> 8 & 255) / 255.0F;
	        float b1 = (float)(this.guiColorArr[1] & 255) / 255.0F;
	        float s = 1.0f / (float) this.h;
	        for (int i = 0; i < this.h; i++) {
	        	float sd = i * s;
	        	r = r0 * (1.0f - sd) + r1 * sd;
	        	g = g0 * (1.0f - sd) + g1 * sd;
	        	b = b0 * (1.0f - sd) + b1 * sd;
	        	GlStateManager.color(r, g, b, 1.0f);
	        	if (i<=this.h-4) {
	        		this.drawTexturedModalRect(u, v + i, 0, i, 176, 1);
	        		if (this.isMany) { this.drawTexturedModalRect(u+172, v + i, 156, i, 20, 1); }
	        	}
	        	else {
	        		this.drawTexturedModalRect(u, v + i, 0, 222 + i - this.h, 176, 1);
	        		if (this.isMany) { this.drawTexturedModalRect(u+172, v + i, 156, 222 + i - this.h, 20, 1); }
	        	}
	        }
		} else {
			this.drawTexturedModalRect(u, v, 0, 0, 176, this.h - 4);
			this.drawTexturedModalRect(u, v + this.h - 4, 0, 218, 176, 4);
		}
		this.mc.getTextureManager().bindTexture(slotTexture);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		for (int s = 0; s < this.inventorySlots.inventorySlots.size(); s++) {
			Slot slot = this.inventorySlots.getSlot(s);
			if (slot!=null && slot.xPos > 0 && slot.yPos > 0) {
				this.drawTexturedModalRect(u + slot.xPos - 1, v + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		this.fontRenderer.drawString(new TextComponentTranslation(this.inventorySlots.customChest.getName()).getFormattedText(), 8, 6, 0xFF404040);
	}

	private void resetSlots() {
		if (!this.isMany) { return; }
		int m = this.row * 9, n = m + 45, i = -1;
		int t = this.inventorySlots.customChest.getSizeInventory(), u = 0, e = t;
		if (t % 9 != 0) { e -= t % 9; }
		//System.out.println("isMany: "+m+", "+n+"; e: "+e);
		for (int s = 0; s < t; s++) {
			Slot slot = this.inventorySlots.getSlot(s);
			i++;
			if (slot==null) { continue; }
			if (s < m || s >= n) {
				slot.xPos = -5000;
				slot.yPos = -5000;
				continue;
			}
			if (s>=e) { u = (int) (((9.0d - ((double) t % 9.0d)) / 2.0d) * 18.0d); }
			slot.xPos = 8 + u + (i % 9) * 18;
			slot.yPos = 18 + (i / 9) * 18;
			//System.out.println("slot: "+s+"/"+i+" - ["+slot.xPos+", "+slot.yPos+"]");
		}
	}
	
}
