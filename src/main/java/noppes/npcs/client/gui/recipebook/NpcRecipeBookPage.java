package noppes.npcs.client.gui.recipebook;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeUpdateListener;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.mixin.client.stats.IRecipeBookMixin;
import noppes.npcs.util.Util;

@SideOnly(Side.CLIENT)
public class NpcRecipeBookPage extends RecipeBookPage {

    private GuiButtonToggle backButton;
    private final List<NpcGuiButtonRecipe> buttons = Lists.newArrayListWithCapacity(20);
    private int currentPage;
    private GuiButtonToggle forwardButton;
    private NpcGuiButtonRecipe hoveredButton;
    private IRecipe lastClickedRecipe;
    private RecipeList lastClickedRecipeList;
    private final List<IRecipeUpdateListener> listeners = Lists.newArrayList();
    private Minecraft minecraft;
    private final NpcGuiRecipeOverlay overlay = new NpcGuiRecipeOverlay();
    private RecipeBook recipeBook;
    private List<RecipeList> recipeLists;
    private int totalPages;

    public NpcRecipeBookPage() {
        for (int i = 0; i < 20; ++i) {
            this.buttons.add(new NpcGuiButtonRecipe());
        }
    }

    public void addListener(@Nonnull GuiRecipeBook guiBook) {
        this.listeners.remove(guiBook);
        this.listeners.add(guiBook);
    }

    @Nullable
    public IRecipe getLastClickedRecipe() {
        return this.lastClickedRecipe;
    }

    @Nullable
    public RecipeList getLastClickedRecipeList() {
        return this.lastClickedRecipeList;
    }

    @Override
    public void init(Minecraft mc, int u, int v) {
        boolean isGlobal = !(mc.currentScreen instanceof GuiNpcCarpentryBench);
        overlay.setGlobal(isGlobal);
        minecraft = mc;
        recipeBook = ((IRecipeBookMixin) mc.player.getRecipeBook()).npcs$copyToNew(isGlobal);
        for (int i = 0; i < buttons.size(); ++i) {
            buttons.get(i).setPosition(u + 11 + 25 * (i % 5), v + 31 + 25 * (i / 5));
        }
        ResourceLocation texture = Util.RECIPE_BOOK;
        forwardButton = new GuiButtonToggle(0, u + 93, v + 137, 12, 17, false);
        forwardButton.initTextureValues(1, 208, 13, 18, texture);
        backButton = new GuiButtonToggle(0, u + 38, v + 137, 12, 17, true);
        backButton.initTextureValues(1, 208, 13, 18, texture);
    }

    @Override
    public boolean mouseClicked(int mouseX, int mouseY, int mouseButton, int x, int y, int width, int height) {
        this.lastClickedRecipe = null;
        this.lastClickedRecipeList = null;
        if (this.overlay.isVisible()) {
            if (this.overlay.buttonClicked(mouseX, mouseY, mouseButton)) {
                this.lastClickedRecipe = this.overlay.getLastRecipeClicked();
                this.lastClickedRecipeList = this.overlay.getRecipeList();
            } else {
                this.overlay.setVisible(false);
            }
            return true;
        }
        else if (this.forwardButton.mousePressed(this.minecraft, mouseX, mouseY) && mouseButton == 0) {
            this.forwardButton.playPressSound(this.minecraft.getSoundHandler());
            ++this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        else if (this.backButton.mousePressed(this.minecraft, mouseX, mouseY) && mouseButton == 0) {
            this.backButton.playPressSound(this.minecraft.getSoundHandler());
            --this.currentPage;
            this.updateButtonsForPage();
            return true;
        }
        else {
            for (NpcGuiButtonRecipe button : this.buttons) {
                if (button.mousePressed(this.minecraft, mouseX, mouseY)) {
                    button.playPressSound(this.minecraft.getSoundHandler());
                    if (mouseButton == 0) {
                        this.lastClickedRecipe = button.getRecipe();
                        this.lastClickedRecipeList = button.getList();
                    }
                    else if (!this.overlay.isVisible() && !button.isOnlyOption()) {
                        this.overlay.init(this.minecraft, button.getList(), button.x,
                                button.y, x + width / 2, y + 13 + height / 2,
                                (float) button.getButtonWidth(), this.recipeBook);
                    }
                    return true;
                }
            }
            return false;
        }
    }

    @Override
    public void recipesShown(@Nonnull List<IRecipe> recipeList) {
        for (IRecipeUpdateListener irecipeupdatelistener : this.listeners) {
            irecipeupdatelistener.recipesShown(recipeList);
        }
    }

    @Override
    public void render(int p_194191_1_, int p_194191_2_, int p_194191_3_, int p_194191_4_, float p_194191_5_) {
        if (this.totalPages > 1) {
            String s = this.currentPage + 1 + "/" + this.totalPages;
            int i = this.minecraft.fontRenderer.getStringWidth(s);
            this.minecraft.fontRenderer.drawString(s, p_194191_1_ - i / 2 + 73, p_194191_2_ + 141, -1);
        }
        RenderHelper.disableStandardItemLighting();
        this.hoveredButton = null;
        for (NpcGuiButtonRecipe guibuttonrecipe : this.buttons) {
            guibuttonrecipe.drawButton(this.minecraft, p_194191_3_, p_194191_4_, p_194191_5_);
            if (guibuttonrecipe.visible && guibuttonrecipe.isMouseOver()) {
                this.hoveredButton = guibuttonrecipe;
            }
        }
        this.backButton.drawButton(this.minecraft, p_194191_3_, p_194191_4_, p_194191_5_);
        this.forwardButton.drawButton(this.minecraft, p_194191_3_, p_194191_4_, p_194191_5_);
        this.overlay.render(p_194191_3_, p_194191_4_, p_194191_5_);
    }

    public void renderTooltip(int mouseButton, int p_193721_2_) {
        if (this.minecraft == null) {
            this.minecraft = Minecraft.getMinecraft();
        }
        if (this.minecraft.currentScreen != null && this.hoveredButton != null && this.overlay != null && !this.overlay.isVisible()) {
            this.minecraft.currentScreen.drawHoveringText(this.hoveredButton.getToolTipText(this.minecraft.currentScreen), mouseButton, p_193721_2_);
        }
    }

    private void updateArrowButtons() {
        this.forwardButton.visible = this.totalPages > 1 && this.currentPage < this.totalPages - 1;
        this.backButton.visible = this.totalPages > 1 && this.currentPage > 0;
    }

    private void updateButtonsForPage() {
        int i = 20 * this.currentPage;
        for (int j = 0; j < this.buttons.size(); ++j) {
            NpcGuiButtonRecipe guibuttonrecipe = this.buttons.get(j);
            if (i + j < this.recipeLists.size()) {
                RecipeList recipelist = this.recipeLists.get(i + j);
                guibuttonrecipe.init(recipelist, this, this.recipeBook);
                guibuttonrecipe.visible = true;
            } else {
                guibuttonrecipe.visible = false;
            }
        }
        this.updateArrowButtons();
    }

    @Override
    public void updateLists(List<RecipeList> p_194192_1_, boolean p_194192_2_) {
        this.recipeLists = p_194192_1_;
        this.totalPages = (int) Math.ceil((double) p_194192_1_.size() / 20.0D);
        if (this.totalPages <= this.currentPage || p_194192_2_) {
            this.currentPage = 0;
        }
        this.updateButtonsForPage();
    }

}
