package noppes.npcs.client.gui.util;

import java.util.Collections;
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

public class GuiMenuTopIconButton extends GuiMenuTopButton {
	protected static RenderItem itemRender;
	private static final ResourceLocation resource = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");
	private final ItemStack item;

	public GuiMenuTopIconButton(int i, GuiButton parent, String s, ItemStack item) {
		super(i, parent, s);
		this.width = 28;
		this.height = 28;
		this.item = item;
	}

	public GuiMenuTopIconButton(int i, int x, int y, String s, ItemStack item) {
		super(i, x, y, s);
		this.width = 28;
		this.height = 28;
		this.item = item;
		GuiMenuTopIconButton.itemRender = Minecraft.getMinecraft().getRenderItem();
	}

	@Override
	public void drawButton(@Nonnull Minecraft minecraft, int i, int j, float partialTicks) {
		if (!this.getVisible()) {
			return;
		}
        this.item.getItem();
        this.hover = (i >= this.x && j >= this.y && i < this.x + this.getWidth() && j < this.y + this.height);
		Minecraft mc = Minecraft.getMinecraft();
		if (this.hover && !this.active) {
			int x = i + mc.fontRenderer.getStringWidth(this.displayString);
			GlStateManager.translate(x, (this.y + 2), 0.0f);
			this.drawHoveringText(Collections.singletonList(this.displayString), 0, 0, mc.fontRenderer);
			GlStateManager.translate((-x), (-(this.y + 2)), 0.0f);
		}
		mc.renderEngine.bindTexture(GuiMenuTopIconButton.resource);
		GlStateManager.pushMatrix();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();
		GlStateManager.disableLighting();
		this.drawTexturedModalRect(this.x, this.y + (this.active ? 2 : 0), 0, this.active ? 32 : 0, 28, 28);
		this.zLevel = 100.0f;
		GuiMenuTopIconButton.itemRender.zLevel = 100.0f;
		GlStateManager.enableLighting();
		GlStateManager.enableRescaleNormal();
		RenderHelper.enableGUIStandardItemLighting();
		GuiMenuTopIconButton.itemRender.renderItemAndEffectIntoGUI(this.item, this.x + 6, this.y + 10);
		GuiMenuTopIconButton.itemRender.renderItemOverlays(mc.fontRenderer, this.item, this.x + 6, this.y + 10);
		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableLighting();
		GuiMenuTopIconButton.itemRender.zLevel = 0.0f;
		this.zLevel = 0.0f;
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
			if (j2 + k > this.width) {
				j2 -= 28 + k;
			}
			if (k2 + i1 + 6 > this.height) {
				k2 = this.height - i1 - 6;
			}
			this.zLevel = 300.0f;
			GuiMenuTopIconButton.itemRender.zLevel = 300.0f;
			int j3 = -267386864;
			this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j3, j3);
			this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j3, j3);
			this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j3, j3);
			this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j3, j3);
			this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j3, j3);
			int k3 = 1347420415;
			int l2 = (k3 & 0xFEFEFE) >> 1 | (k3 & 0xFF000000);
			this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k3, l2);
			this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k3, l2);
			this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k3, k3);
			this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l2, l2);
			for (int i2 = 0; i2 < textLines.size(); ++i2) {
				String s2 = (String) textLines.get(i2);
				font.drawStringWithShadow(s2, j2, k2, -1);
				if (i2 == 0) {
					k2 += 2;
				}
				k2 += 10;
			}
			this.zLevel = 0.0f;
			GuiMenuTopIconButton.itemRender.zLevel = 0.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableDepth();
			RenderHelper.enableStandardItemLighting();
			GlStateManager.enableRescaleNormal();
		}
	}

}
