package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;

public class GuiHoverText
extends GuiScreen {
	
	protected static ResourceLocation buttonTextures = new ResourceLocation(CustomNpcs.MODID, "textures/gui/info.png");
	public int id;
	private String text;
	private int x;
	private int y;

	public GuiHoverText(int id, String text, int x, int y) {
		this.text = text;
		this.id = id;
		this.x = x;
		this.y = y;
	}

	public void drawScreen(int par1, int par2, float par3) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiHoverText.buttonTextures);
		this.drawTexturedModalRect(this.x, this.y, 0, 0, 12, 12);
		if (this.inArea(this.x, this.y, 12, 12, par1, par2)) {
			List<String> lines = new ArrayList<String>();
			lines.add(this.text);
			this.drawHoveringText(lines, this.x + 8, this.y + 6, this.fontRenderer);
			GlStateManager.disableLighting();
		}
	}

	public boolean inArea(int x, int y, int width, int height, int mouseX, int mouseY) {
		return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
	}

}
