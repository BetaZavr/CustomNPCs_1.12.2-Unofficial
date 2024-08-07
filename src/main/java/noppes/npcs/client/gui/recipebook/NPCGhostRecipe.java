package noppes.npcs.client.gui.recipebook;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class NPCGhostRecipe extends GhostRecipe {

	private IRecipe recipe;
	private final List<GhostRecipe.GhostIngredient> ingredients = Lists.newArrayList();

    public void addIngredient(@Nonnull Ingredient p_194187_1_, int p_194187_2_, int p_194187_3_) {
		this.ingredients.add(new GhostRecipe.GhostIngredient(p_194187_1_, p_194187_2_, p_194187_3_));
	}

	public void clear() {
		this.recipe = null;
		this.ingredients.clear();
    }

	@Nonnull
	public GhostRecipe.GhostIngredient get(int p_192681_1_) {
		return this.ingredients.get(p_192681_1_);
	}

	@Nullable
	public IRecipe getRecipe() {
		return this.recipe;
	}

	public void render(@Nonnull Minecraft p_194188_1_, int p_194188_2_, int p_194188_3_, boolean p_194188_4_, float p_194188_5_) {
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();

		for (int i = 0; i < this.ingredients.size(); ++i) {
			GhostRecipe.GhostIngredient ghostrecipe$ghostingredient = this.ingredients.get(i);
			int j = ghostrecipe$ghostingredient.getX() + p_194188_2_;
			int k = ghostrecipe$ghostingredient.getY() + p_194188_3_;

			if (i == 0 && p_194188_4_) {
				Gui.drawRect(j - 4, k - 4, j + 20, k + 20, 822018048);
			} else {
				Gui.drawRect(j, k, j + 16, k + 16, 822018048);
			}

			GlStateManager.disableLighting();
			ItemStack itemstack = ghostrecipe$ghostingredient.getItem();
			RenderItem renderitem = p_194188_1_.getRenderItem();
			renderitem.renderItemIntoGUI(itemstack, j, k);
			if (itemstack.getCount() > 1) {
				renderitem.renderItemOverlayIntoGUI(p_194188_1_.fontRenderer, itemstack, j, k,
						"x" + itemstack.getCount());
			}
			GlStateManager.depthFunc(516);
			Gui.drawRect(j, k, j + 16, k + 16, 822083583);
			GlStateManager.depthFunc(515);

			if (i == 0) {
				renderitem.renderItemOverlays(p_194188_1_.fontRenderer, itemstack, j, k);
			}

			GlStateManager.enableLighting();
		}

		RenderHelper.disableStandardItemLighting();
	}

	public void setRecipe(@Nonnull IRecipe p_192685_1_) {
		this.recipe = p_192685_1_;
	}

	public int size() {
		return this.ingredients.size();
	}

}
