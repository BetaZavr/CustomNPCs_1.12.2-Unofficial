package noppes.npcs.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.recipebook.*;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.stats.RecipeBook;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.mixin.stats.IRecipeBookMixin;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.client.gui.recipebook.NpcGhostRecipe;
import noppes.npcs.client.gui.recipebook.NpcGuiButtonRecipeTab;
import noppes.npcs.client.gui.recipebook.NpcRecipeBookPage;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nonnull;
import java.util.List;

@Mixin(value = GuiRecipeBook.class)
public class GuiRecipeBookMixin {

    @Mutable
    @Final
    @Shadow
    private GhostRecipe ghostRecipe = new NpcGhostRecipe();

    @Mutable
    @Final
    @Shadow
    private List<GuiButtonRecipeTab> recipeTabs;

    @Mutable
    @Final
    @Shadow
    private RecipeBookPage recipeBookPage = new NpcRecipeBookPage();

    @Shadow
    private RecipeBook recipeBook;

    @Shadow
    private int width;

    @Shadow
    private int height;

    @Shadow
    private GuiButtonRecipeTab currentTab;

    @Shadow
    private InventoryCrafting craftingSlots;

    @Shadow
    private Minecraft mc;

    @Shadow
    private int timesInventoryChanged;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void npcs$init(CallbackInfo ci) {
        ghostRecipe = new NpcGhostRecipe();
        recipeBookPage = new NpcRecipeBookPage();
    }

    @Inject(method = "recipesUpdated", at = @At("HEAD"))
    public void NPCS$recipesUpdated(CallbackInfo ci) {
        boolean isGlobal = !(mc.currentScreen instanceof GuiNpcCarpentryBench);
        if (!isGlobal) {
            recipeBook = ((IRecipeBookMixin) mc.player.getRecipeBook()).npcs$copyToNew(false, mc.player);
        }
    }

    // The "func_194303_a" method is essentially the same as "initGui"
    @Inject(method = "func_194303_a", at = @At("HEAD"), cancellable = true)
    public void npcs$func_194303_a(int w, int h, @Nonnull Minecraft minecraft, boolean widthTooNarrow, @Nonnull InventoryCrafting inv, CallbackInfo ci) {
        boolean isGlobal = !(minecraft.currentScreen instanceof GuiNpcCarpentryBench);
        if (isGlobal) {
            if (recipeTabs.size() != 6) {
                recipeTabs.clear();
                recipeTabs.add(new NpcGuiButtonRecipeTab(0, CreativeTabs.SEARCH, true));
                recipeTabs.add(new NpcGuiButtonRecipeTab(1, CreativeTabs.TOOLS, true));
                recipeTabs.add(new NpcGuiButtonRecipeTab(2, CreativeTabs.BUILDING_BLOCKS, true));
                recipeTabs.add(new NpcGuiButtonRecipeTab(3, CreativeTabs.MISC, true));
                recipeTabs.add(new NpcGuiButtonRecipeTab(4, CreativeTabs.REDSTONE, true));
                recipeTabs.add(new NpcGuiButtonRecipeTab(5, CustomRegisters.tab, true));
            }
        } else {
            if (recipeTabs.size() != 2) {
                recipeTabs.clear();
                recipeTabs.add(new NpcGuiButtonRecipeTab(0, CreativeTabs.SEARCH, false));
                recipeTabs.add(new NpcGuiButtonRecipeTab(1, CustomRegisters.tabItems, false));
            }
        }

        mc = minecraft;
        width = w;
        height = h;
        craftingSlots = inv;
        recipeBook = ((IRecipeBookMixin) mc.player.getRecipeBook()).npcs$copyToNew(isGlobal, mc.player);
        timesInventoryChanged = minecraft.player.inventory.getTimesChanged();
        currentTab = recipeTabs.get(0);
        currentTab.setStateTriggered(true);

        GuiRecipeBook parent = (GuiRecipeBook) (Object)this;
        if (parent.isVisible()) {
            parent.initVisuals(widthTooNarrow, inv);
        }
        Keyboard.enableRepeatEvents(true);
        ci.cancel();
    }

}
