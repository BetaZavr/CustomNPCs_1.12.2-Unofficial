package noppes.npcs.client.gui.recipebook;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.GuiRecipeBook;
import net.minecraft.client.gui.recipebook.IRecipeUpdateListener;
import net.minecraft.client.gui.recipebook.RecipeBookPage;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.api.mixin.stats.IRecipeBookMixin;
import noppes.npcs.client.gui.player.GuiNpcCarpentryBench;
import noppes.npcs.util.Util;

// Displaying a recipe page in the GUI recipe window
@SideOnly(Side.CLIENT)
public class NpcRecipeBookPage extends RecipeBookPage {

    private GuiButtonToggle backButton;
    private final List<NpcGuiButtonRecipe> buttons = new ArrayList<>(20);
    private int currentPage;
    private GuiButtonToggle forwardButton;
    private NpcGuiButtonRecipe hoveredButton;
    private IRecipe lastClickedRecipe;
    private RecipeList lastClickedRecipeList;
    private final List<IRecipeUpdateListener> listeners = new ArrayList<>();
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
        recipeBook = ((IRecipeBookMixin) mc.player.getRecipeBook()).npcs$copyToNew(isGlobal, mc.player);
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
        lastClickedRecipe = null;
        lastClickedRecipeList = null;
        if (overlay.isVisible()) {
            if (overlay.buttonClicked(mouseX, mouseY, mouseButton)) {
                lastClickedRecipe = overlay.getLastRecipeClicked();
                lastClickedRecipeList = overlay.getRecipeList();
            } else {
                overlay.setVisible(false);
            }
            return true;
        }
        else if (forwardButton.mousePressed(minecraft, mouseX, mouseY) && mouseButton == 0) {
            forwardButton.playPressSound(minecraft.getSoundHandler());
            ++currentPage;
            updateButtonsForPage();
            return true;
        }
        else if (backButton.mousePressed(minecraft, mouseX, mouseY) && mouseButton == 0) {
            backButton.playPressSound(minecraft.getSoundHandler());
            --currentPage;
            updateButtonsForPage();
            return true;
        }
        else {
            for (NpcGuiButtonRecipe button : buttons) {
                if (button.mousePressed(minecraft, mouseX, mouseY)) {
                    button.playPressSound(minecraft.getSoundHandler());
                    if (mouseButton == 0) {
                        lastClickedRecipe = button.getRecipe();
                        lastClickedRecipeList = button.getList();
                    }
                    else if (!overlay.isVisible() && !button.isOnlyOption()) {
                        overlay.init(minecraft, button.getList(), button.x,
                                button.y, x + width / 2, y + 13 + height / 2,
                                (float) button.getButtonWidth(), recipeBook);
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
    public void render(int x, int y, int mouseX, int mouseY, float partialTicks) {
        if (this.totalPages > 1) {
            String s = this.currentPage + 1 + "/" + this.totalPages;
            int i = this.minecraft.fontRenderer.getStringWidth(s);
            this.minecraft.fontRenderer.drawString(s, x - i / 2 + 73, y + 141, -1);
        }
        this.hoveredButton = null;
        for (NpcGuiButtonRecipe guibuttonrecipe : this.buttons) {
            guibuttonrecipe.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
            if (guibuttonrecipe.visible && guibuttonrecipe.isMouseOver()) {
                this.hoveredButton = guibuttonrecipe;
            }
        }
        this.backButton.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
        this.forwardButton.drawButton(this.minecraft, mouseX, mouseY, partialTicks);
        this.overlay.render(mouseX, mouseY, partialTicks);
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
