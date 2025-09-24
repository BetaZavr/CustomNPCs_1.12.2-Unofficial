package noppes.npcs.client.gui.player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.containers.ContainerChestCustom;

public class GuiCustomContainer extends GuiContainer {

	protected static final ResourceLocation backTexture = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	protected static final ResourceLocation tabsTexture = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	protected static final ResourceLocation rowTexture = new ResourceLocation("textures/gui/container/creative_inventory/tab_items.png");
	protected static final ResourceLocation lockTexture = new ResourceLocation("textures/gui/widgets.png");
	protected final ContainerChestCustom inventorySlots;
	protected final int guiColor;
	protected final int maxRows;
	protected final int step;
	protected final int[] guiColorArr;
	protected final boolean isMany;
	protected boolean isScrolling;
	protected int row;
	protected int yPos;
	protected boolean hoverScroll;
	protected final String lock;
	public int mouseWheel;

	public GuiCustomContainer(ContainerChestCustom container) {
		super(container);
		inventorySlots = container;
		allowUserInput = false;
		ySize = 114 + inventorySlots.height;

		isMany = container.customChest.getSizeInventory() > 45;
		lock = !container.customChest.getLockCode().isEmpty() ? container.customChest.getLockCode().getLock() : "";
		row = 0;
		maxRows = (int) Math.ceil((double) container.customChest.inventory.size() / 9.0d) - 5;
		guiColor = container.customChest.guiColor;
		guiColorArr = container.customChest.guiColorArr;
		step = maxRows > 0 ? (int) (73.0f / (float) maxRows) : 0;
		isScrolling = false;
		resetSlots();
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		if (guiColor == -1) { GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F); }
		else { GlStateManager.color((float) (guiColor >> 16 & 255) / 255.0F, (float) (guiColor >> 8 & 255) / 255.0F, (float) (guiColor & 255) / 255.0F, 1.0F); }
		mc.getTextureManager().bindTexture(backTexture);
		int u = (width - xSize) / 2;
		int v = (height - ySize) / 2;
		int h = inventorySlots.height + 107;
		// Main
		if (guiColorArr != null && guiColorArr.length > 1) {
			float r, g, b;
			float r0 = (float) (guiColorArr[0] >> 16 & 255) / 255.0F;
			float g0 = (float) (guiColorArr[0] >> 8 & 255) / 255.0F;
			float b0 = (float) (guiColorArr[0] & 255) / 255.0F;
			float r1 = (float) (guiColorArr[1] >> 16 & 255) / 255.0F;
			float g1 = (float) (guiColorArr[1] >> 8 & 255) / 255.0F;
			float b1 = (float) (guiColorArr[1] & 255) / 255.0F;
			float s = 1.0f / (float) h;
			for (int i = 0; i < h; i++) {
				float sd = i * s;
				r = r0 * (1.0f - sd) + r1 * sd;
				g = g0 * (1.0f - sd) + g1 * sd;
				b = b0 * (1.0f - sd) + b1 * sd;
				GlStateManager.color(r, g, b, 1.0f);
				if (i <= h - 4) {
					drawTexturedModalRect(u, v + i, 0, i, 176, 1);
					if (isMany) { drawTexturedModalRect(u + 172, v + i, 156, i, 20, 1); }
				} else {
					drawTexturedModalRect(u, v + i, 0, 222 + i - h, 176, 1);
					if (isMany) { drawTexturedModalRect(u + 172, v + i, 156, 222 + i - h, 20, 1); }
				}
			}
		} else {
			drawTexturedModalRect(u, v, 0, 0, 176, h - 4);
			drawTexturedModalRect(u, v + h - 4, 0, 218, 176, 4);
		}
		mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		for (int s = 0; s < inventorySlots.inventorySlots.size(); s++) {
			Slot slot = inventorySlots.getSlot(s);
			if (slot.xPos > 0 && slot.yPos > 0) {
				drawTexturedModalRect(u + slot.xPos - 1, v + slot.yPos - 1, 0, 0, 18, 18);
			}
		}
		if (isMany) {
			mc.getTextureManager().bindTexture(rowTexture);
			drawTexturedModalRect(u + 172, v + 17, 174, 17, 14, 86);
			drawTexturedModalRect(u + 172, v + 103, 174, 125, 14, 4);
		}
		if (!lock.isEmpty()) {
			mc.getTextureManager().bindTexture(lockTexture);
			drawTexturedModalRect(u + 164 + (isMany ? 16 : 0), v - 8, 0, 146, 20, 20);
		}
	}

	@Override
	protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
		ITextComponent text = new TextComponentTranslation(inventorySlots.customChest.getName());
		text.getStyle().setBold(true);
		int color = 0xFFFFFFFF;
		if (guiColor != -1) {
			int r = guiColor >> 16 & 255;
			int g = guiColor >> 8 & 255;
			int b = guiColor & 255;
			if (r + g + b > 384) { color = 0xFF000000; }
		}
		if (guiColorArr != null) {
			int r = guiColorArr[0] >> 16 & 255;
			int g = guiColorArr[0] >> 8 & 255;
			int b = guiColorArr[0] & 255;
			if (r + g + b > 384) { color = 0xFF000000; }
		}
		fontRenderer.drawString(text.getFormattedText(), 8, 6, color);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		mouseWheel = Mouse.getDWheel();
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		hoverScroll = false;
		if (isMany) {
			GlStateManager.pushMatrix();
			GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
			int u = (width - xSize) / 2;
			int v = (height - ySize) / 2;
			mc.getTextureManager().bindTexture(tabsTexture);
            float currentScroll = (float) row / (float) maxRows;
			int h = (int) (currentScroll * 73.0f);
			u += 173;
			v += 18 + h;
			hoverScroll = mouseX >= u && mouseX <= u + 12 && mouseY >= v && mouseY <= v + 15;
			drawTexturedModalRect(u, v, (hoverScroll ? 244 : 232), 0, 12, 15);
			GlStateManager.popMatrix();
			if (mouseWheel > 0) { resetRow(false); }
			else if (mouseWheel < 0) { resetRow(true); }
		}
		renderHoveredToolTip(mouseX, mouseY);
		if (!lock.isEmpty()) {
			int u = (width - xSize) / 2 + 164 + (isMany ? 16 : 0);
			int v = (height - ySize) / 2 - 8;
			if (mouseX >= u && mouseX <= u + 20 && mouseY >= v && mouseY <= v + 20) {
				List<String> textLines = new ArrayList<>();
				textLines.add(new TextComponentTranslation("companion.owner").getFormattedText() + ":");
				textLines.add(lock.length() < 1000 ? lock : lock.substring(0, 1000) + "...");
				drawHoveringText(textLines, mouseX, mouseY);
			}
		}
	}

	@Override
	public void handleMouseInput() throws IOException {
		if (isScrolling) {
			int mouseY = height - Mouse.getEventY() * height / mc.displayHeight - 1;
			if (mouseY - yPos >= step) {
				resetRow(true);
				yPos = mouseY;
			}
			else if (((mouseY - yPos) * -1) >= step) {
				resetRow(false);
				yPos = mouseY;
			}
		}
		super.handleMouseInput();
	}

	@Override
	public void initGui() {
		super.initGui();
		resetSlots();
	}

	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == Keyboard.KEY_UP || keyCode == mc.gameSettings.keyBindForward.getKeyCode()) { resetRow(false); }
		if (keyCode == Keyboard.KEY_DOWN || keyCode == mc.gameSettings.keyBindBack.getKeyCode()) { resetRow(true); }
		super.keyTyped(typedChar, keyCode);
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
		if (hoverScroll) {
			yPos = mouseY;
			isScrolling = true;
		}
		else if (isMany) {
			int u = 173 + (width - xSize) / 2;
			int v = 18 + (height - ySize) / 2;
			if (mouseX >= u && mouseX <= u + 11 && mouseY >= v && mouseY <= v + 88) {
				int h = mouseY - v, r;
				if (h <= 7) { r = 0; }
				else if (h >= 81) { r = maxRows; }
				else { r = (int) ((double) maxRows * (double) h / 88.0d); }
				int old = row;
				if (r < 0) { r = 0; }
				if (r > maxRows) { r = maxRows; }
				if (old != r) {
					row = r;
					resetSlots();
				}
			}
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	@Override
	public void mouseReleased(int mouseX, int mouseY, int state) {
		if (isScrolling) { isScrolling = false; }
		super.mouseReleased(mouseX, mouseY, state);
	}

	private void resetRow(boolean bo) {
		if (!isMany) { return; }
		int old = row;
		if (bo) { row++; }
		else { row--; }
		if (row < 0) { row = 0; }
		if (row > maxRows) { row = maxRows; }
		if (old != row) { resetSlots(); }
	}

	private void resetSlots() {
		if (!isMany) { return; }
		int m = row * 9, n = m + 45, i = -1;
		int t = inventorySlots.customChest.getSizeInventory(), u = 0, e = t;
		if (t % 9 != 0) { e -= t % 9; }
		for (int s = 0; s < t; s++) {
			Slot slot = inventorySlots.getSlot(s);
            if (s < m || s >= n) {
				slot.xPos = -5000;
				slot.yPos = -5000;
				continue;
			}
			i++;
			if (s >= e) { u = (int) (((9.0d - ((double) t % 9.0d)) / 2.0d) * 18.0d); }
			slot.xPos = 8 + u + (i % 9) * 18;
			slot.yPos = 18 + (i / 9) * 18;
		}
	}

}
