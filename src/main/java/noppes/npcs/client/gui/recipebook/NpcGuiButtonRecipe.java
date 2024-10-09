package noppes.npcs.client.gui.recipebook;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.recipebook.GuiButtonRecipe;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.resources.I18n;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;

// Displaying a crafting item from a recipe in the GUI Recipes window
@SideOnly(Side.CLIENT)
public class NpcGuiButtonRecipe extends GuiButtonRecipe {

    private static final ResourceLocation RECIPE_BOOK = new ResourceLocation("textures/gui/recipe_book.png");
    private float animationTime;
    private RecipeBook book;
    private int currentIndex;
    private RecipeList list;
    private float time;

    public NpcGuiButtonRecipe() {
        super();
    }

    public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
        if (!this.visible) {
            return;
        }
        if (!GuiScreen.isCtrlKeyDown()) {
            this.time += partialTicks;
        }
        this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
        RenderHelper.enableGUIStandardItemLighting();
        mc.renderEngine.bindTexture(RECIPE_BOOK);
        GlStateManager.disableLighting();
        int i = 29;
        if (!this.list.containsCraftableRecipes()) {
            i += 25;
        }
        int j = 206;
        if (this.list.getRecipes(this.book.isFilteringCraftable()).size() > 1) {
            j += 25;
        }
        boolean flag = this.animationTime > 0.0F;
        if (flag) {
            float f = 1.0F + 0.1F * (float) Math.sin(this.animationTime / 15.0F * (float) Math.PI);
            GlStateManager.pushMatrix();
            GlStateManager.translate((float) (this.x + 8), (float) (this.y + 12), 0.0F);
            GlStateManager.scale(f, f, 1.0F);
            GlStateManager.translate((float) (-(this.x + 8)), (float) (-(this.y + 12)), 0.0F);
            this.animationTime -= partialTicks;
        }
        this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);
        List<IRecipe> list = this.getOrderedRecipes();
        this.currentIndex = MathHelper.floor(this.time / 30.0F) % list.size();
        ItemStack itemstack = (list.get(this.currentIndex)).getRecipeOutput();
        int k = 4;
        if (list.size() > 1) {
            ItemStack itemstack1 = list.get(0).getRecipeOutput();
            for (IRecipe rec : list) {
                ItemStack stack = rec.getRecipeOutput();
                if (!NoppesUtilServer.IsItemStackNull(stack) && !NoppesUtilPlayer.compareItems(itemstack, stack, false, false)) {
                    itemstack1 = stack;
                    break;
                }
            }
            if (!NoppesUtilServer.IsItemStackNull(itemstack1) && !NoppesUtilPlayer.compareItems(itemstack, itemstack1, false, false)) {
                mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack1, this.x + k + 1, this.y + k + 1);
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                --k;
            }
        }
        mc.getRenderItem().renderItemAndEffectIntoGUI(itemstack, this.x + k, this.y + k);
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        if (flag) {
            GlStateManager.popMatrix();
        }
        GlStateManager.enableLighting();
        RenderHelper.disableStandardItemLighting();
    }

    public @Nonnull RecipeList getList() {
        return this.list;
    }

    private List<IRecipe> getOrderedRecipes() {
        List<IRecipe> list = this.list.getDisplayRecipes(true);
        if (!this.book.isFilteringCraftable()) {
            list.addAll(this.list.getDisplayRecipes(false));
        }
        return list;
    }

    public @Nonnull IRecipe getRecipe() {
        List<IRecipe> list = this.getOrderedRecipes();
        return list.get(this.currentIndex);
    }

    public @Nonnull List<String> getToolTipText(GuiScreen guiScreen) {
        ItemStack itemstack = this.getOrderedRecipes().get(this.currentIndex).getRecipeOutput();
        List<String> list = guiScreen.getItemToolTip(itemstack);
        if (this.list.getRecipes(this.book.isFilteringCraftable()).size() > 1) {
            list.add(I18n.format("gui.recipebook.moreRecipes"));
        }
        return list;
    }

    public void init(RecipeList recipeList, NpcRecipeBookPage bookPage, RecipeBook recipeBook) {
        this.list = recipeList;
        this.book = recipeBook;
        List<IRecipe> list = recipeList.getRecipes(recipeBook.isFilteringCraftable());
        for (IRecipe irecipe : list) {
            if (recipeBook.isNew(irecipe)) {
                bookPage.recipesShown(list);
                this.animationTime = 15.0F;
                break;
            }
        }
    }

    public boolean isOnlyOption() {
        return false; //this.getOrderedRecipes().size() == 1;
    }

}
