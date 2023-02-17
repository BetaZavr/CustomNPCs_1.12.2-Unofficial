package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;

public class GuiButtonNextPage extends GuiNpcButton {
	// private static String __OBFID = "CL_00000745";
	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	private boolean isForward;

	public GuiButtonNextPage(int par1, int par2, int par3, boolean par4) {
		super(par1, par2, par3, 23, 13, "");
		this.isForward = par4;
	}

	public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (this.visible) {
			boolean flag = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			mc.getTextureManager().bindTexture(GuiButtonNextPage.bookGuiTextures);
			int k = 0;
			int l = 192;
			if (flag) {
				k += 23;
			}
			if (!this.isForward) {
				l += 13;
			}
			this.drawTexturedModalRect(this.x, this.y, k, l, 23, 13);
		}
	}

}
