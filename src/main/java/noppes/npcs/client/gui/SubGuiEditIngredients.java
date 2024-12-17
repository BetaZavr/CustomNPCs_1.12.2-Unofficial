package noppes.npcs.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.SubGuiInterface;

import java.awt.*;
import java.util.Arrays;

public class SubGuiEditIngredients
extends SubGuiInterface {

    public final ItemStack[] stacks;
    private int hover;

    public SubGuiEditIngredients(int buttonID, ItemStack[] itemStacks) {
        id = buttonID;
        closeOnEsc = true;
        setBackground("smallbg.png");
        xSize = 176;
        ySize = 76;
        stacks = Arrays.copyOf(itemStacks, itemStacks.length);
    }

    @Override
    public void buttonEvent(GuiNpcButton button) {
        close();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        super.drawScreen(mouseX, mouseY, partialTicks);
        hover = -1;
        mc.getTextureManager().bindTexture(background);
        drawTexturedModalRect(guiLeft, guiTop + ySize, 0, 219, xSize, 3);

        GlStateManager.pushMatrix();
        mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
        GlStateManager.translate(guiLeft + 7.0f, guiTop + 16.0f, 0.0f);
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 9; j++) {
                drawTexturedModalRect(j * 18, i * 18, 0, 0, 18, 18);
            }
        }
        GlStateManager.popMatrix();

        for (int i = 0 ; i < stacks.length; i++) {
            if (stacks[i] == null || stacks[i].isEmpty()) { continue; }
            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            int x = (int) (guiLeft + 8.0d + (i % 9) * 18.0d);
            int y = (int) (guiTop + 17.0d + Math.floor(i / 9.0d) * 18.0d);
            GlStateManager.translate(x, y, 0.0f);
            mc.getRenderItem().renderItemAndEffectIntoGUI(stacks[i], 0, 0);
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            drawString(mc.fontRenderer, "" + stacks[i].getCount(), 16 - mc.fontRenderer.getStringWidth("" + stacks[i].getCount()), 9, new Color(0xFFFFFFFF).getRGB());
            RenderHelper.disableStandardItemLighting();
            if (isMouseHover(mouseX, mouseY, x, y, 18, 18)) {
                GlStateManager.translate(-x, -y + 32.0f, 0.0f);
                drawHoveringText(stacks[i].getTooltip(player, player.capabilities.isCreativeMode ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL), mouseX, mouseY, fontRenderer);
                hover = i;
            }
            GlStateManager.popMatrix();
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        addLabel(new GuiNpcLabel(0, "gui.recipe.del", guiLeft + 8, guiTop + 5));
        addButton( new GuiNpcButton(66, guiLeft + 57, guiTop + 54, 60, 20, "gui.done"));
    }

    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (hover != -1) {
            if (stacks == null || hover >= stacks.length) { return; }
            stacks[hover] = ItemStack.EMPTY;
        }
    }

}
