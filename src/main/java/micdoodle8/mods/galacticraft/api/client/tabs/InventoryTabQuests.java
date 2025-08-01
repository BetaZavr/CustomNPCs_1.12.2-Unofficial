package micdoodle8.mods.galacticraft.api.client.tabs;

import java.awt.*;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class InventoryTabQuests extends AbstractTab {
	public InventoryTabQuests() {
		super(0, 0, 0, new ItemStack(Items.BOOK));
		this.displayString = NoppesStringUtils.translate("quest.quests") + " (" + GameSettings.getKeyDisplayString(ClientProxy.QuestLog.getKeyCode()) + ")";
	}

	private static void run() {
		CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.QuestLog, Minecraft.getMinecraft().player);
	}

	@Override
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.enabled || !this.visible) {
			super.drawButton(mc, mouseX, mouseY, partialTicks);
			return;
		}
		boolean hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
				&& mouseY < this.y + this.height;
		if (hovered) {
			int x = mouseX + mc.fontRenderer.getStringWidth(this.displayString);
			GlStateManager.translate(x, (this.y + 2), 0.0f);
			this.drawHoveringText(Collections.singletonList(this.displayString), 0, 0, mc.fontRenderer);
			GlStateManager.translate((-x), (-(this.y + 2)), 0.0f);
		}
		super.drawButton(mc, mouseX, mouseY, partialTicks);
	}

	protected void drawHoveringText(List<String> list, int x, int y, FontRenderer font) {
		if (list.isEmpty()) {
			return;
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.disableLighting();
		GlStateManager.disableDepth();
		int k = 0;
		for (String s : list) {
			int l = font.getStringWidth(s);
			if (l > k) {
				k = l;
			}
		}
		int j2 = x + 12;
		int k2 = y - 12;
		int i1 = 8;
		if (list.size() > 1) {
			i1 += 2 + (list.size() - 1) * 10;
		}
		if (j2 + k > this.width) {
			j2 -= 28 + k;
		}
		if (k2 + i1 + 6 > this.height) {
			k2 = this.height - i1 - 6;
		}
		this.zLevel = 300.0f;
		this.itemRender.zLevel = 300.0f;
		int j3 = new Color(0xF0100010).getRGB();
		this.drawGradientRect(j2 - 3, k2 - 4, j2 + k + 3, k2 - 3, j3, j3);
		this.drawGradientRect(j2 - 3, k2 + i1 + 3, j2 + k + 3, k2 + i1 + 4, j3, j3);
		this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 + i1 + 3, j3, j3);
		this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + i1 + 3, j3, j3);
		this.drawGradientRect(j2 + k + 3, k2 - 3, j2 + k + 4, k2 + i1 + 3, j3, j3);
		int k3 = new Color(0x505000FF).getRGB();
		int l2 = (k3 & new Color(0xFEFEFE).getRGB()) >> 1 | (k3 & new Color(0xFF000000).getRGB());
		this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + i1 + 3 - 1, k3, l2);
		this.drawGradientRect(j2 + k + 2, k2 - 3 + 1, j2 + k + 3, k2 + i1 + 3 - 1, k3, l2);
		this.drawGradientRect(j2 - 3, k2 - 3, j2 + k + 3, k2 - 3 + 1, k3, k3);
		this.drawGradientRect(j2 - 3, k2 + i1 + 2, j2 + k + 3, k2 + i1 + 3, l2, l2);
		for (int i2 = 0; i2 < list.size(); ++i2) {
			String s2 = list.get(i2);
			font.drawStringWithShadow(s2, j2, k2, -1);
			if (i2 == 0) {
				k2 += 2;
			}
			k2 += 10;
		}
		this.zLevel = 0.0f;
		this.itemRender.zLevel = 0.0f;
		GlStateManager.enableLighting();
		GlStateManager.enableDepth();
		GlStateManager.enableRescaleNormal();
	}

	@Override
	public void onTabClicked() {
		CustomNPCsScheduler.runTack(InventoryTabQuests::run);
	}

	@Override
	public boolean shouldAddToList() {
		return true;
	}

}
