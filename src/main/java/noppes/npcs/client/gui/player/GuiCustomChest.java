package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerCustomChest;

public class GuiCustomChest extends GuiContainer {

	protected static final ResourceLocation CHEST_GUI_TEXTURE = new ResourceLocation("textures/gui/container/generic_54.png");
	protected final int inventoryRows;
	public String title;

	public GuiCustomChest(ContainerCustomChest container) {
		super(container);
		title = null;

		inventoryRows = container.rows;
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(GuiCustomChest.CHEST_GUI_TEXTURE);
		int i = (width - xSize) / 2;
		int j = (height - ySize) / 2;
		drawTexturedModalRect(i, j, 0, 0, xSize, inventoryRows * 18 + 17);
		drawTexturedModalRect(i, j + inventoryRows * 18 + 17, 0, 126, xSize, 96);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (title != null && !title.isEmpty()) {
			fontRenderer.drawString(title, (width - fontRenderer.getStringWidth(title)) / 2,
					(height - ySize) / 2 + 5, CustomNpcResourceListener.DefaultTextColor);
		}
		renderHoveredToolTip(mouseX, mouseY);
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CloseGui);
	}

}
