package noppes.npcs.client.gui;

import java.awt.*;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag.TooltipFlags;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiSelectItemStack
extends SubGuiInterface {

    public ItemStack stack;
    private int hoverPos = -2;

    public SubGuiSelectItemStack(int id, ItemStack item) {
        xSize = 176;
        ySize = 166;
        setBackground("followerhire.png");

        closeOnEsc = true;
        this.id = id;
        stack = item;

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GlStateManager.translate(0.0f, 0.0f, -300.0f);
        super.drawScreen(mouseX, mouseY, partialTicks);


        List<String> list = null;
        int x = guiLeft + 79;
        int y = guiTop + 38;
        hoverPos = -2;

        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        drawTexturedModalRect(x, y, 0, 0, 18, 18);
        if (isMouseHover(mouseX, mouseY, x, y, 16, 16)) {
            hoverPos = -1;
            Gui.drawRect(x + 1, y + 1, x + 17, y + 17, new Color(0x80FFFFFF).getRGB());
            if (stack != null && !stack.isEmpty()) { list = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL); }
        }
        if (stack != null && !stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1.0f, y + 1.0f, 0.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderHelper.enableStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            drawString(mc.fontRenderer, "" + stack.getCount(), 16 - mc.fontRenderer.getStringWidth("" + stack.getCount()), 9, new Color(0xFFFFFFFF).getRGB());
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < player.inventory.mainInventory.size(); i ++) {
            ItemStack st = player.inventory.mainInventory.get(i);
            x = guiLeft + 7 + (i % 9) * 18;
            y = guiTop + 83 + (i / 9) * 18;
            if (i < 9) { y += 58; } else { y -= 18; }
            if (isMouseHover(mouseX, mouseY, x, y, 16, 16)) {
                hoverPos = i;
                Gui.drawRect(x + 1, y + 1, x + 17, y + 17, new Color(0x80FFFFFF).getRGB());
                //list
                if (!st.isEmpty()) { list = st.getTooltip(player, mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL); }
            }
            if (st.isEmpty()) { continue; }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1.0f, y + 1.0f, 0.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            mc.getRenderItem().renderItemAndEffectIntoGUI(st, 0, 0);
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            drawString(mc.fontRenderer, "" + st.getCount(), 16 - mc.fontRenderer.getStringWidth("" + st.getCount()), 9, new Color(0xFFFFFFFF).getRGB());
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        if (list != null && !list.isEmpty()) {
            setHoverText(list);
            drawHoverText(null);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (hoverPos == -1) {
            stack = ItemStack.EMPTY;
        } else if (hoverPos >= 0) {
            stack = player.inventory.mainInventory.get(hoverPos);
        }
    }

}
