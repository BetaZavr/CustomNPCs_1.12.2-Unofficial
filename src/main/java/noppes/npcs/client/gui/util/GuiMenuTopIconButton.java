package noppes.npcs.client.gui.util;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class GuiMenuTopIconButton
extends GuiMenuTopButton {

	protected static RenderItem itemRender;
	private static final ResourceLocation resource = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private final ItemStack item;

	public GuiMenuTopIconButton(int i, GuiButton parent, String s, ItemStack item) {
		super(i, parent, s);
		width = 28;
		height = 28;
		this.item = item;
		offsetW = 12;
	}

	public GuiMenuTopIconButton(int i, int x, int y, String s, ItemStack item) {
		super(i, x, y, s);
		width = 28;
		height = 28;
		this.item = item;
		offsetW = 12;
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!isVisible()) {
			return;
		}
		super.drawButton(mc, mouseX, mouseY, partialTicks);
        item.getItem();
		mc.getTextureManager().bindTexture(GuiMenuTopIconButton.resource);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		drawTexturedModalRect(x, y + (active ? 2 : 0), 0, active ? 32 : 0, 28, 28);
		zLevel = 100.0f;
		itemRender = mc.getRenderItem();
		itemRender.zLevel = 100.0f;
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		itemRender.renderItemAndEffectIntoGUI(item, x + 6, y + 10);
		itemRender.renderItemOverlays(mc.fontRenderer, item, x + 6, y + 10);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		itemRender.zLevel = 0.0f;
		zLevel = 0.0f;
		GlStateManager.popMatrix();
	}

	protected void drawHoveringText(List<?> textLines, int mouseX, int mouseY, FontRenderer font) {
		if (!textLines.isEmpty()) {
			GlStateManager.disableRescaleNormal();
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.disableDepth();
			int k = 0;
			for (Object s : textLines) {
				int l = font.getStringWidth(s.toString());
				if (l > k) {
					k = l;
				}
			}
			int j2 = mouseX + 12;
			int k2 = mouseY - 12;
			int i1 = 8;
			if (textLines.size() > 1) {
				i1 += 2 + (textLines.size() - 1) * 10;
			}
			if (j2 + k > width) {
				j2 -= 28 + k;
			}
			if (k2 + i1 + 6 > height) {
				k2 = height - i1 - 6;
			}
			zLevel = 300.0f;
			itemRender.zLevel = 300.0f;
			int j3 = -267386864;
			drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j3, j3);
			drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j3, j3);
			drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j3, j3);
			drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j3, j3);
			drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j3, j3);
			int k3 = 0x505000FF;
			int l2 = (k3 & 0xFEFEFE) >> 1 | (k3 & 0xFF000000);
			drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k3, l2);
			drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k3, l2);
			drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k3, k3);
			drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l2, l2);
			for (int i2 = 0; i2 < textLines.size(); ++i2) {
				String s2 = (String) textLines.get(i2);
				font.drawStringWithShadow(s2, j2, k2, -1);
				if (i2 == 0) {
					k2 += 2;
				}
				k2 += 10;
			}
			zLevel = 0.0f;
			itemRender.zLevel = 0.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

}
