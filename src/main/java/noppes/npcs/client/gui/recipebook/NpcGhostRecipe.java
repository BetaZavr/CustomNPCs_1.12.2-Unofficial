package noppes.npcs.client.gui.recipebook;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.api.handler.data.INpcRecipe;

// Display recipe as translucent items in crafting grid
// SPacketPlaceGhostRecipe <--> CPacketPlaceRecipe
@SideOnly(Side.CLIENT)
public class NpcGhostRecipe
extends GhostRecipe {

    private IRecipe recipe;
    private final List<GhostRecipe.GhostIngredient> ingredients = new ArrayList<>();

    public void addIngredient(@Nonnull Ingredient p_194187_1_, int p_194187_2_, int p_194187_3_) {
        this.ingredients.add(new GhostRecipe.GhostIngredient(p_194187_1_, p_194187_2_, p_194187_3_));
    }

    public void clear() {
        this.recipe = null;
        this.ingredients.clear();
    }

    @Nonnull
    public GhostRecipe.GhostIngredient get(int position) {
        return this.ingredients.get(position);
    }

    @Nullable
    public IRecipe getRecipe() {
        return this.recipe;
    }

    public void render(@Nonnull Minecraft mc, int guiLeft, int guiTop, boolean select, float partialTicks) {
        if (recipe != null && recipe instanceof INpcRecipe && !((INpcRecipe) recipe).isGlobal()) {
            guiLeft -= 77;
        }
        GlStateManager.disableLighting();
        for (int i = 0; i < this.ingredients.size(); ++i) {
            GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ingredients.get(i);
            int j = ghostrecipe$ghostingredient.getX() + guiLeft;
            int k = ghostrecipe$ghostingredient.getY() + guiTop;
            if (i == 0 && select) { Gui.drawRect(j - 4, k - 4, j + 20, k + 20, 822018048); }
            else { Gui.drawRect(j, k, j + 16, k + 16, 822018048); }
            GlStateManager.disableLighting();
            ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
            RenderItem renderitem = mc.getRenderItem();
            renderitem.renderItemIntoGUI(itemstack, j, k);
            if (itemstack.getCount() > 1) { renderitem.renderItemOverlayIntoGUI(mc.fontRenderer, itemstack, j, k, "x" + itemstack.getCount()); }
            GlStateManager.depthFunc(516);
            Gui.drawRect(j, k, j + 16, k + 16, 822083583);
            GlStateManager.depthFunc(515);
            if (i == 0) { renderitem.renderItemOverlays(mc.fontRenderer, itemstack, j, k); }
            GlStateManager.enableLighting();
        }
    }


    public void setRecipe(@Nonnull IRecipe newRecipe) {
        this.recipe = newRecipe;
    }

    public int size() {
        return this.ingredients.size();
    }

}
