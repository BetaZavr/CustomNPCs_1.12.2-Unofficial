package noppes.npcs.client.gui.player;

import java.io.IOException;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.containers.ContainerChestCustom;

public class GuiCustomContainer // GuiChest
extends GuiContainer {

	private static final ResourceLocation backTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	private static final ResourceLocation slotTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");
	private static final ResourceLocation tabsTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private static final ResourceLocation rowTexture = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
	private ContainerChestCustom inventorySlots;
	
	private int guiColor, row, maxRows, yPos, step;
	private int[] guiColorArr;
	private boolean isMany, hoverScroll;
	
	private float currentScroll;
    private boolean isScrolling;
	
	public GuiCustomContainer(ContainerChestCustom container) {
		super(container);
		this.mc = Minecraft.getMinecraft();
		this.inventorySlots = container;
		this.isMany = container.customChest.getSizeInventory() > 45;
		this.allowUserInput = false;
		this.row = 0;
		this.maxRows = (int) Math.ceil((double) container.customChest.inventory.size() / 9.0d) - 5;
		this.guiColor = container.customChest.guiColor;
		this.guiColorArr = container.customChest.guiColorArr;
		this.ySize = 114 + this.inventorySlots.height;
		this.step = this.maxRows> 0 ? (int) (73.0f / (float) this.maxRows) : 0;
		this.isScrolling = false;
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
		this.hoverScroll = false;
		if (this.isMany) {
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
			int u = (this.width - this.xSize) / 2;
			int v = (this.height - this.ySize) / 2;
			this.mc.getTextureManager().bindTexture(tabsTexture);
			this.currentScroll = (float) this.row / (float) this.maxRows;
			int h = (int) (this.currentScroll * 73.0f);
			u += 173;
			v += 18 + h;
			this.hoverScroll = mouseX >= u && mouseX <= u + 12 && mouseY >= v && mouseY <= v + 15;
			this.drawTexturedModalRect(u, v, (this.hoverScroll ? 244 : 232), 0, 12, 15);
			GlStateManager.popMatrix();
			int dwm = Mouse.getDWheel();
			if (dwm>0) { this.resetRow(false);}
			else if (dwm<0){ this.resetRow(true); }
		}
		this.renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (this.guiColor==-1) { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
		else { GlStateManager.color((float)(this.guiColor >> 16 & 255) / 255.0F, (float)(this.guiColor >> 8 & 255) / 255.0F, (float)(this.guiColor & 255) / 255.0F, 1.0F); }
		this.mc.getTextureManager().bindTexture(backTexture);
		int u = (this.width - this.xSize) / 2;
		int v = (this.height - this.ySize) / 2;
		int h = this.inventorySlots.height + 107;
		// Main
		if (this.guiColorArr!=null && this.guiColorArr.length>1) {
			float r, g, b;
			float r0 = (float)(this.guiColorArr[0] >> 16 & 255) / 255.0F;
	        float g0 = (float)(this.guiColorArr[0] >> 8 & 255) / 255.0F;
	        float b0 = (float)(this.guiColorArr[0] & 255) / 255.0F;
	        float r1 = (float)(this.guiColorArr[1] >> 16 & 255) / 255.0F;
	        float g1 = (float)(this.guiColorArr[1] >> 8 & 255) / 255.0F;
	        float b1 = (float)(this.guiColorArr[1] & 255) / 255.0F;
	        float s = 1.0f / (float) h;
	        for (int i = 0; i < h; i++) {
	        	float sd = i * s;
	        	r = r0 * (1.0f - sd) + r1 * sd;
	        	g = g0 * (1.0f - sd) + g1 * sd;
	        	b = b0 * (1.0f - sd) + b1 * sd;
	        	GlStateManager.color(r, g, b, 1.0f);
	        	if (i<=h - 4) {
	        		this.drawTexturedModalRect(u, v + i, 0, i, 176, 1);
	        		if (this.isMany) { this.drawTexturedModalRect(u+172, v + i, 156, i, 20, 1); }
	        	}
	        	else {
	        		this.drawTexturedModalRect(u, v + i, 0, 222 + i - h, 176, 1);
	        		if (this.isMany) { this.drawTexturedModalRect(u+172, v + i, 156, 222 + i - h, 20, 1); }
	        	}
	        }
		} else {
			this.drawTexturedModalRect(u, v, 0, 0, 176, h - 4);
			this.drawTexturedModalRect(u, v + h - 4, 0, 218, 176, 4);
		}
		this.mc.getTextureManager().bindTexture(slotTexture);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		for (int s = 0; s < this.inventorySlots.inventorySlots.size(); s++) {
			Slot slot = this.inventorySlots.getSlot(s);
			if (slot!=null && slot.xPos > 0 && slot.yPos > 0) {
				this.drawTexturedModalRect(u + slot.xPos - 1, v + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		if (this.isMany) {
			this.mc.getTextureManager().bindTexture(rowTexture);
			this.drawTexturedModalRect(u +172, v + 17, 174, 17, 14, 86);
			this.drawTexturedModalRect(u +172, v + 103, 174, 125, 14, 4);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		ITextComponent text = new TextComponentTranslation(this.inventorySlots.customChest.getName());
		text.getStyle().setBold(true);
		int color = 0xFFFFFFFF;
		if (this.guiColor!=-1) {
			int r = this.guiColor >> 16 & 255;
	        int g = this.guiColor >> 8 & 255;
	        int b = this.guiColor & 255;
	        if (r + g + b > 384) { color = 0xFF000000; }
		}
		if (this.guiColorArr!=null) {
			int r = this.guiColorArr[0] >> 16 & 255;
	        int g = this.guiColorArr[0] >> 8 & 255;
	        int b = this.guiColorArr[0] & 255;
	        if (r + g + b > 384) { color = 0xFF000000; }
		}
		this.fontRenderer.drawString(text.getFormattedText(), 8, 6, color);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (this.hoverScroll) {
			this.yPos = mouseY;
			this.isScrolling = true;
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}
	
	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (this.isScrolling) { this.isScrolling = false; }
		super.mouseReleased(mouseX, mouseY, state);
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (this.isScrolling) {
	        int mouseY = this.height - Mouse.getEventY() * this.height / this.mc.displayHeight - 1;
	        if (mouseY - this.yPos >= this.step) {
	        	this.resetRow(true);
	        	this.yPos = mouseY;
	        } else if (((mouseY - this.yPos) * -1) >= this.step) {
	        	this.resetRow(false);
	        	this.yPos = mouseY;
	        }
		}
		super.handleMouseInput();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode==200 || keyCode==ClientProxy.frontButton.getKeyCode()) { this.resetRow(false); }
		if (keyCode==208 || keyCode==ClientProxy.backButton.getKeyCode()) { this.resetRow(true); }
		super.keyTyped(typedChar, keyCode);
	}

	private void resetRow(boolean bo) {
		if (!this.isMany) { return; }
		int old = 0 + this.row;
		if (bo) { this.row++; } else { this.row--; }
		if (this.row<0) { this.row = 0; }
		if (this.row>this.maxRows) { this.row = this.maxRows; }
		if (old != this.row) { this.resetSlots(); }
	}
	
	private void resetSlots() {
		if (!this.isMany) { return; }
		int m = this.row * 9, n = m + 45, i = -1;
		int t = this.inventorySlots.customChest.getSizeInventory(), u = 0, e = t;
		if (t % 9 != 0) { e -= t % 9; }
		for (int s = 0; s < t; s++) {
			Slot slot = this.inventorySlots.getSlot(s);
			if (slot==null) { continue; }
			if (s < m || s >= n) {
				slot.xPos = -5000;
				slot.yPos = -5000;
				continue;
			}
			i++;
			if (s>=e) { u = (int) (((9.0d - ((double) t % 9.0d)) / 2.0d) * 18.0d); }
			slot.xPos = 8 + u + (i % 9) * 18;
			slot.yPos = 18 + (i / 9) * 18;
		}
	}
	
}