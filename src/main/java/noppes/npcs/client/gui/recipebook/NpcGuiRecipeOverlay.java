package noppes.npcs.client.gui.recipebook;

import java.util.*;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.recipebook.GuiRecipeOverlay;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.client.config.GuiUtils;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;

// Displaying variations of one recipe in the GUI recipe window
@SideOnly(Side.CLIENT)
public class NpcGuiRecipeOverlay extends GuiRecipeOverlay {

    @SideOnly(Side.CLIENT)
    class Button extends GuiButton {
        private final boolean isCraftable;
        private final boolean isGlobal;
        private final IRecipe recipe;

        public Button(int x, int y, IRecipe recipe, boolean craftable, boolean global) {
            super(0, x, y, "");
            this.width = global ? 24 : 32;
            this.height = global ? 24 : 32;
            this.recipe = recipe;
            this.isCraftable = craftable;
            this.isGlobal = global;
        }

        public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.enableAlpha();
            mc.renderEngine.bindTexture(NpcGuiRecipeOverlay.RECIPE_BOOK_TEXTURE);
            this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
            int i = 152;
            if (!this.isCraftable) {
                i += 26;
            }
            int j = 78;
            if (this.hovered) {
                j += 26;
            }
            GlStateManager.color(2.0F, 2.0F, 2.0F, 1.0F);
            if (this.isGlobal) {
                drawTexturedModalRect(x, y, i, j, this.width, this.height);
            }
            else {
                drawTexturedModalRect(x, y, i, j, 16, 16);
                drawTexturedModalRect(x + 16, y, i + 9, j, 15, 16);
                drawTexturedModalRect(x, y + 16, i, j + 9, 16, 16);
                drawTexturedModalRect(x + 16, y + 16, i + 9, j + 9, 15, 15);
            }
            int k = 3;
            int l = 3;
            if (this.recipe instanceof net.minecraftforge.common.crafting.IShapedRecipe) {
                net.minecraftforge.common.crafting.IShapedRecipe shapedrecipes = (net.minecraftforge.common.crafting.IShapedRecipe) this.recipe;
                k = shapedrecipes.getRecipeWidth();
                l = shapedrecipes.getRecipeHeight();
            }
            Iterator<Ingredient> iterator = this.recipe.getIngredients().iterator();
            for (int i1 = 0; i1 < l; ++i1) {
                int j1 = 3 + i1 * 7;
                for (int k1 = 0; k1 < k; ++k1) {
                    if (iterator.hasNext()) {
                        ItemStack[] aitemstack = iterator.next().getMatchingStacks();
                        if (aitemstack.length != 0) {
                            int l1 = 3 + k1 * 7;
                            GlStateManager.pushMatrix();
                            int i2 = (int) ((float) (this.x + l1) / 0.42F - 3.0F);
                            int j2 = (int) ((float) (this.y + j1) / 0.42F - 3.0F);
                            GlStateManager.scale(0.42F, 0.42F, 1.0F);
                            GlStateManager.enableLighting();
                            mc.getRenderItem().renderItemAndEffectIntoGUI(aitemstack[MathHelper.floor(NpcGuiRecipeOverlay.this.time / 30.0F) % aitemstack.length], i2, j2);
                            GlStateManager.disableLighting();
                            GlStateManager.popMatrix();
                        }
                    }
                }
            }
            GlStateManager.disableAlpha();
            RenderHelper.disableStandardItemLighting();
            if (this.hovered) {
                drawCenteredString(mc.fontRenderer, new TextComponentTranslation("item.craft.type."+(recipe instanceof ShapedRecipes)).getFormattedText(), x + width / 2, y - 12, 0xFFFFFFFF);
            }
        }

        protected void drawHoveringText(List<String> textLines, int mouseX, int mouseY, FontRenderer font) {
            if (textLines == null || textLines.isEmpty()) { return; }
            GlStateManager.pushMatrix();
            GlStateManager.enableLighting();
            GuiUtils.drawHoveringText(textLines, mouseX, mouseY, width, height, -1, font);
            GlStateManager.disableLighting();
            GlStateManager.popMatrix();
        }
    }

    private static final ResourceLocation RECIPE_BOOK_TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
    private static final ResourceLocation SPRITE = new ResourceLocation(CustomNpcs.MODID, "textures/gui/bgfilled.png");
    private final List<NpcGuiRecipeOverlay.Button> buttonList = Lists.newArrayList();
    private boolean isGlobal = false;
    private IRecipe lastRecipeClicked;
    private Minecraft mc;
    private RecipeList recipeList;
    private float time;
    private boolean visible;
    private int x;
    private int y;

    public void setGlobal(boolean global) {
        this.isGlobal = global;
    }

    public boolean buttonClicked(int mouseX, int mouseY, int mouseButton) {
        if (mouseButton != 0) {
            return false;
        }
        for (NpcGuiRecipeOverlay.Button guirecipeoverlay$button : this.buttonList) {
            if (guirecipeoverlay$button.mousePressed(this.mc, mouseX, mouseY)) {
                this.lastRecipeClicked = guirecipeoverlay$button.recipe;
                return true;
            }
        }
        return false;
    }

    @Nonnull
    public IRecipe getLastRecipeClicked() {
        return this.lastRecipeClicked;
    }

    @Nonnull
    public RecipeList getRecipeList() {
        return this.recipeList;
    }

    public void init(@Nonnull Minecraft mcIn, @Nonnull RecipeList recipeListIn, int x, int y, int width, int height, float partialTicks, @Nonnull RecipeBook book) {
        this.mc = mcIn;
        this.recipeList = recipeListIn;
        boolean flag = book.isFilteringCraftable();
        List<IRecipe> list = recipeListIn.getDisplayRecipes(true);
        List<IRecipe> list1 = flag ? Collections.emptyList() : recipeListIn.getDisplayRecipes(false);
        int i = list.size();
        int j = i + list1.size();
        int k = j <= 16 ? 4 : 5;
        int l = (int) Math.ceil((float) j / (float) k);
        this.x = x;
        this.y = y;
        int offset = this.isGlobal ? 25 : 33;
        float f = (float) (this.x + Math.min(j, k) * offset);
        float f1 = (float) (width + 50);
        if (f > f1) {
            this.x = (int) ((float) this.x - partialTicks * (float) ((int) ((f - f1) / partialTicks)));
        }
        float f2 = (float) (this.y + l * offset);
        float f3 = (float) (height + 50);
        if (f2 > f3) {
            this.y = (int) ((float) this.y - partialTicks * (float) MathHelper.ceil((f2 - f3) / partialTicks));
        }
        float f4 = (float) this.y;
        float f5 = (float) (height - 100);
        if (f4 < f5) {
            this.y = (int) ((float) this.y - partialTicks * (float) MathHelper.ceil((f4 - f5) / partialTicks));
        }
        this.visible = true;
        this.buttonList.clear();
        for (int j1 = 0; j1 < j; ++j1) {
            boolean flag1 = j1 < i;
            this.buttonList.add(new NpcGuiRecipeOverlay.Button(this.x + 4 + offset * (j1 % k), this.y + 5 + offset * (j1 / k), flag1 ? list.get(j1) : list1.get(j1 - i), flag1, isGlobal));
        }
        this.lastRecipeClicked = null;
    }


    @Override
    public void setVisible(boolean isVisible) { this.visible = isVisible; }

    @Override
    public boolean isVisible() { return this.visible; }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        if (!this.visible || mc == null) { return; }
        time += partialTicks;
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.enableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(SPRITE);
        GlStateManager.pushMatrix();
        GlStateManager.translate(0.0F, 0.0F, 170.0F);
        int i = buttonList.size() <= 16 ? 4 : 5;
        int rows = Math.min(buttonList.size(), i);
        int columns = MathHelper.ceil((float) buttonList.size() / (float) i);
        int offset = isGlobal ? 24 : 32;
        int border = 7;

        this.drawTexturedModalRect(x, y, 0, 0, rows * offset + border, columns * offset + border);
        this.drawTexturedModalRect(x + rows * offset + border, y, 252, 0, 4, columns * offset + border);
        this.drawTexturedModalRect(x, y + columns * offset + border, 0, 252, rows * offset + border, 4);
        this.drawTexturedModalRect(x + rows * offset + border, y + columns * offset + border, 252, 252, 4, 4);

        GlStateManager.disableBlend();
        RenderHelper.disableStandardItemLighting();

        for (NpcGuiRecipeOverlay.Button guirecipeoverlay$button : buttonList) {
            guirecipeoverlay$button.drawButton(mc, mouseX, mouseY, partialTicks);
        }

        GlStateManager.popMatrix();
    }

}
