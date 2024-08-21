package noppes.npcs.client.gui;

import java.util.Arrays;
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
        this.id = id;
        this.stack = item;
        this.xSize = 176;
        this.ySize = 166;
        this.setBackground("followerhire.png");
        this.closeOnEsc = true;

    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {

        GlStateManager.translate(0.0f, 0.0f, -300.0f);
        super.drawScreen(mouseX, mouseY, partialTicks);


        List<String> list = null;
        int x = this.guiLeft + 79;
        int y = this.guiTop + 38;
        this.hoverPos = -2;

        GlStateManager.pushMatrix();
        this.mc.renderEngine.bindTexture(GuiNPCInterface.RESOURCE_SLOT);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        this.drawTexturedModalRect(x, y, 0, 0, 18, 18);
        if (this.isMouseHover(mouseX, mouseY, x, y, 16, 16)) {
            this.hoverPos = -1;
            Gui.drawRect(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
            if (stack != null && !stack.isEmpty()) { list = stack.getTooltip(this.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL); }
        }
        if (stack != null && !stack.isEmpty()) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1.0f, y + 1.0f, 0.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderHelper.enableStandardItemLighting();
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawString(this.mc.fontRenderer, "" + stack.getCount(), 16 - this.mc.fontRenderer.getStringWidth("" + stack.getCount()), 9, 0xFFFFFFFF);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }

        for (int i = 0; i < this.player.inventory.mainInventory.size(); i ++) {
            ItemStack st = this.player.inventory.mainInventory.get(i);
            x = this.guiLeft + 7 + (i % 9) * 18;
            y = this.guiTop + 83 + (i / 9) * 18;
            if (i < 9) { y += 58; } else { y -= 18; }
            if (this.isMouseHover(mouseX, mouseY, x, y, 16, 16)) {
                this.hoverPos = i;
                Gui.drawRect(x + 1, y + 1, x + 17, y + 17, 0x80FFFFFF);
                //list
                if (!st.isEmpty()) { list = st.getTooltip(this.player, this.mc.gameSettings.advancedItemTooltips ? TooltipFlags.ADVANCED : TooltipFlags.NORMAL); }
            }
            if (st.isEmpty()) { continue; }
            GlStateManager.pushMatrix();
            GlStateManager.translate(x + 1.0f, y + 1.0f, 0.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            RenderHelper.enableStandardItemLighting();
            this.mc.getRenderItem().renderItemAndEffectIntoGUI(st, 0, 0);
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            this.drawString(this.mc.fontRenderer, "" + st.getCount(), 16 - this.mc.fontRenderer.getStringWidth("" + st.getCount()), 9, 0xFFFFFFFF);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
        if (list != null && !list.isEmpty()) {
            this.hoverText = list.toArray(new String[0]);
            this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
            this.hoverText = null;
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (this.hoverPos == -1) {
            this.stack = ItemStack.EMPTY;
        } else if (this.hoverPos >= 0) {
            this.stack = this.player.inventory.mainInventory.get(this.hoverPos);
        }
    }

}
