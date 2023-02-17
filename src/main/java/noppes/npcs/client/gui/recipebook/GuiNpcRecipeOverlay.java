package noppes.npcs.client.gui.recipebook;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.recipebook.GuiRecipeOverlay;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.stats.RecipeBook;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;

@SideOnly(Side.CLIENT)
public class GuiNpcRecipeOverlay extends GuiRecipeOverlay {
	@SideOnly(Side.CLIENT)
	class Button extends GuiButton {
		private final boolean isCraftable;
		private boolean isGlobal;
		private final IRecipe recipe;

		public Button(int x, int y, IRecipe recipe, boolean craftable, boolean global) {
			super(0, x, y, "");
			this.width = global ? 24 : 32;
			this.height = global ? 24 : 32;
			this.recipe = recipe;
			this.isCraftable = craftable;
			this.isGlobal = global;
		}

		public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks) {
			RenderHelper.enableGUIStandardItemLighting();
			GlStateManager.enableAlpha();
			mc.getTextureManager().bindTexture(GuiNpcRecipeOverlay.RECIPE_BOOK_TEXTURE);
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height;
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
				this.drawTexturedModalRect(this.x, this.y, i, j, this.width, this.height);
			} else {
				this.drawTexturedModalRect(this.x, this.y, i, j, 16, 16);
				this.drawTexturedModalRect(this.x + 16, this.y, i + 9, j, 15, 16);
				this.drawTexturedModalRect(this.x, this.y + 16, i, j + 9, 16, 16);
				this.drawTexturedModalRect(this.x + 16, this.y + 16, i + 9, j + 9, 15, 15);
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
						ItemStack[] aitemstack = ((Ingredient) iterator.next()).getMatchingStacks();
						if (aitemstack.length != 0) {
							int l1 = 3 + k1 * 7;
							GlStateManager.pushMatrix();
							int i2 = (int) ((float) (this.x + l1) / 0.42F - 3.0F);
							int j2 = (int) ((float) (this.y + j1) / 0.42F - 3.0F);
							GlStateManager.scale(0.42F, 0.42F, 1.0F);
							GlStateManager.enableLighting();
							mc.getRenderItem().renderItemAndEffectIntoGUI(
									aitemstack[MathHelper.floor(GuiNpcRecipeOverlay.this.time / 30.0F)
											% aitemstack.length],
									i2, j2);
							GlStateManager.disableLighting();
							GlStateManager.popMatrix();
						}
					}
				}
			}
			GlStateManager.disableAlpha();
			RenderHelper.disableStandardItemLighting();
		}

	}

	private static final ResourceLocation RECIPE_BOOK_TEXTURE = new ResourceLocation("textures/gui/recipe_book.png");
	private static final ResourceLocation SPRITE = new ResourceLocation(CustomNpcs.MODID, "textures/gui/bgfilled.png");
	private final List<GuiNpcRecipeOverlay.Button> buttonList = Lists.<GuiNpcRecipeOverlay.Button>newArrayList();
	public boolean isGlobal;
	private IRecipe lastRecipeClicked;
	private Minecraft mc;
	private RecipeList recipeList;
	private float time;
	private boolean visible;
	private int x;

	private int y;

	public GuiNpcRecipeOverlay(boolean global) {
		this.isGlobal = global;
	}

	public boolean buttonClicked(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton != 0) {
			return false;
		}
		for (GuiNpcRecipeOverlay.Button guirecipeoverlay$button : this.buttonList) {
			if (guirecipeoverlay$button.mousePressed(this.mc, mouseX, mouseY)) {
				this.lastRecipeClicked = guirecipeoverlay$button.recipe;
				return true;
			}
		}
		return false;
	}

	public IRecipe getLastRecipeClicked() {
		return this.lastRecipeClicked;
	}

	public RecipeList getRecipeList() {
		return this.recipeList;
	}

	public void init(Minecraft mcIn, RecipeList recipeListIn, int x, int y, int width, int height, float partialTicks,
			RecipeBook book) {
		this.mc = mcIn;
		this.recipeList = recipeListIn;
		boolean flag = book.isFilteringCraftable();
		List<IRecipe> list = recipeListIn.getDisplayRecipes(true);
		List<IRecipe> list1 = flag ? Collections.emptyList() : recipeListIn.getDisplayRecipes(false);
		int i = list.size();
		int j = i + list1.size();
		int k = j <= 16 ? 4 : 5;
		int l = (int) Math.ceil((double) ((float) j / (float) k));
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
			this.buttonList
					.add(new GuiNpcRecipeOverlay.Button(this.x + 4 + offset * (j1 % k), this.y + 5 + offset * (j1 / k),
							flag1 ? (IRecipe) list.get(j1) : (IRecipe) list1.get(j1 - i), flag1, this.isGlobal));
		}
		this.lastRecipeClicked = null;
	}

	/*
	 * Changed private void nineInchSprite(int rows, int columns, int width, int
	 * p_191846_4_, int u, int v) { this.drawTexturedModalRect(this.x, this.y, u, v,
	 * p_191846_4_, p_191846_4_); this.drawTexturedModalRect(this.x + p_191846_4_ *
	 * 2 + rows * width, this.y, u + width + p_191846_4_, v, p_191846_4_,
	 * p_191846_4_); this.drawTexturedModalRect(this.x, this.y + p_191846_4_ * 2 +
	 * columns * width, u, v + width + p_191846_4_, p_191846_4_, p_191846_4_);
	 * this.drawTexturedModalRect(this.x + p_191846_4_ * 2 + rows * width, this.y +
	 * p_191846_4_ * 2 + columns * width, u + width + p_191846_4_, v + width +
	 * p_191846_4_, p_191846_4_, p_191846_4_);
	 * 
	 * for (int i = 0; i < rows; ++i) { this.drawTexturedModalRect(this.x +
	 * p_191846_4_ + i * width, this.y, u + p_191846_4_, v, width, p_191846_4_);
	 * this.drawTexturedModalRect(this.x + p_191846_4_ + (i + 1) * width, this.y, u
	 * + p_191846_4_, v, p_191846_4_, p_191846_4_);
	 * 
	 * for (int j = 0; j < columns; ++j) { if (i == 0) {
	 * this.drawTexturedModalRect(this.x, this.y + p_191846_4_ + j * width, u, v +
	 * p_191846_4_, p_191846_4_, width); this.drawTexturedModalRect(this.x, this.y +
	 * p_191846_4_ + (j + 1) * width, u, v + p_191846_4_, p_191846_4_, p_191846_4_);
	 * }
	 * 
	 * this.drawTexturedModalRect(this.x + p_191846_4_ + i * width, this.y +
	 * p_191846_4_ + j * width, u + p_191846_4_, v + p_191846_4_, width, width);
	 * this.drawTexturedModalRect(this.x + p_191846_4_ + (i + 1) * width, this.y +
	 * p_191846_4_ + j * width, u + p_191846_4_, v + p_191846_4_, p_191846_4_,
	 * width); this.drawTexturedModalRect(this.x + p_191846_4_ + i * width, this.y +
	 * p_191846_4_ + (j + 1) * width, u + p_191846_4_, v + p_191846_4_, width,
	 * p_191846_4_); this.drawTexturedModalRect(this.x + p_191846_4_ + (i + 1) *
	 * width - 1, this.y + p_191846_4_ + (j + 1) * width - 1, u + p_191846_4_, v +
	 * p_191846_4_, p_191846_4_ + 1, p_191846_4_ + 1);
	 * 
	 * if (i == rows - 1) { this.drawTexturedModalRect(this.x + p_191846_4_ * 2 +
	 * rows * width, this.y + p_191846_4_ + j * width, u + width + p_191846_4_, v +
	 * p_191846_4_, p_191846_4_, width); this.drawTexturedModalRect(this.x +
	 * p_191846_4_ * 2 + rows * width, this.y + p_191846_4_ + (j + 1) * width, u +
	 * width + p_191846_4_, v + p_191846_4_, p_191846_4_, p_191846_4_); } }
	 * this.drawTexturedModalRect(this.x + p_191846_4_ + i * width, this.y +
	 * p_191846_4_ * 2 + columns * width, u + p_191846_4_, v + width + p_191846_4_,
	 * width, p_191846_4_); this.drawTexturedModalRect(this.x + p_191846_4_ + (i +
	 * 1) * width, this.y + p_191846_4_ * 2 + columns * width, u + p_191846_4_, v +
	 * width + p_191846_4_, p_191846_4_, p_191846_4_); } }
	 */

	public boolean isVisible() {
		return this.visible;
	}

	public void render(int mouseX, int mouseY, float partialTicks) {

		if (!this.visible) {
			return;
		}
		this.time += partialTicks;
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.enableBlend();
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.mc.getTextureManager().bindTexture(SPRITE);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0F, 0.0F, 170.0F);
		int i = this.buttonList.size() <= 16 ? 4 : 5;
		int rows = Math.min(this.buttonList.size(), i);
		int columns = MathHelper.ceil((float) this.buttonList.size() / (float) i);
		int offset = this.isGlobal ? 24 : 32;
		int border = 7;

		this.drawTexturedModalRect(this.x, this.y, 0, 0, rows * offset + border, columns * offset + border);
		this.drawTexturedModalRect(this.x + rows * offset + border, this.y, 252, 0, 4, columns * offset + border);
		this.drawTexturedModalRect(this.x, this.y + columns * offset + border, 0, 252, rows * offset + border, 4);
		this.drawTexturedModalRect(this.x + rows * offset + border, this.y + columns * offset + border, 252, 252, 4, 4);

		GlStateManager.disableBlend();
		RenderHelper.disableStandardItemLighting();

		for (GuiNpcRecipeOverlay.Button guirecipeoverlay$button : this.buttonList) {
			guirecipeoverlay$button.drawButton(this.mc, mouseX, mouseY, partialTicks);
		}

		GlStateManager.popMatrix();
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

}