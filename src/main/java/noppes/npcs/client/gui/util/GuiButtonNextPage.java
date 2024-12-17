package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GuiButtonNextPage
extends GuiNpcButton {

	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	private final boolean isForward;

	public GuiButtonNextPage(int id, int x, int y, boolean forward) {
		super(id, x, y, 23, 13, "");
		isForward = forward;
	}

	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (visible) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(GuiButtonNextPage.bookGuiTextures);
			int k = 0;
			int l = 192;
			boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
			if (hovered) {
				k += 23;
			}
			if (!isForward) {
				l += 13;
			}
			drawTexturedModalRect(x, y, k, l, 23, 13);
		}
	}

}
