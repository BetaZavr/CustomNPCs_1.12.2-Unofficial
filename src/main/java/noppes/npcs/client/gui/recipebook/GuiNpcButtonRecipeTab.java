package noppes.npcs.client.gui.recipebook;

import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButtonToggle;
import net.minecraft.client.gui.recipebook.RecipeList;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.util.RecipeBookClient;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.stats.RecipeBook;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomRegisters;
import noppes.npcs.client.ClientProxy;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class GuiNpcButtonRecipeTab extends GuiButtonToggle {

	private float animationTime;
	private final CreativeTabs category;
	private final boolean isGlobal;

	public GuiNpcButtonRecipeTab(int buttonId, CreativeTabs tab, boolean globalRecipes) {
		super(buttonId, 0, 0, 35, 27, false);
		this.category = tab;
		this.initTextureValues(153, 2, 35, 0, GuiNpcRecipeBook.RECIPE_BOOK);
		this.isGlobal = globalRecipes;
	}

	/**
	 * Draws this button to the screen.
	 */
	public void drawButton(@Nonnull Minecraft mc, int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			return;
		}
		if (this.animationTime > 0.0F) {
			float f = 1.0F + 0.1F * (float) Math.sin(this.animationTime / 15.0F * (float) Math.PI);
			GlStateManager.pushMatrix();
			GlStateManager.translate((float) (this.x + 8), (float) (this.y + 12), 0.0F);
			GlStateManager.scale(1.0F, f, 1.0F);
			GlStateManager.translate((float) (-(this.x + 8)), (float) (-(this.y + 12)), 0.0F);
		}

		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
				&& mouseY < this.y + this.height;
		mc.renderEngine.bindTexture(this.resourceLocation);
		GlStateManager.disableDepth();
		int k = this.xTexStart;
		int i = this.yTexStart;
		if (this.stateTriggered) {
			k += this.xDiffTex;
		}
		if (this.hovered) {
			i += this.yDiffTex;
		}
		int j = this.x;
		if (this.stateTriggered) {
			j -= 2;
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawTexturedModalRect(j, this.y, k, i, this.width, this.height);
		GlStateManager.enableDepth();
		RenderHelper.enableGUIStandardItemLighting();
		GlStateManager.disableLighting();
		this.renderIcon(mc.getRenderItem());
		GlStateManager.enableLighting();
		RenderHelper.disableStandardItemLighting();
		if (this.animationTime > 0.0F) {
			GlStateManager.popMatrix();
			this.animationTime -= partialTicks;
		}
	}

	public CreativeTabs getCategory() {
		return this.category;
	}

	private void renderIcon(RenderItem render) {
		ItemStack itemstack = this.category.getIconItemStack();
		if (this.category == CreativeTabs.TOOLS) {
			render.renderItemAndEffectIntoGUI(itemstack, this.x + 3, this.y + 5);
			render.renderItemAndEffectIntoGUI(CreativeTabs.COMBAT.getIconItemStack(), this.x + 14, this.y + 5);
		} else if (this.category == CreativeTabs.MISC) {
			render.renderItemAndEffectIntoGUI(itemstack, this.x + 3, this.y + 5);
			render.renderItemAndEffectIntoGUI(CreativeTabs.FOOD.getIconItemStack(), this.x + 14, this.y + 5);
		} else if (this.category == CustomRegisters.tab) {
			if (this.isGlobal) {
				render.renderItemAndEffectIntoGUI(itemstack, this.x + 3, this.y + 5);
				render.renderItemAndEffectIntoGUI(new ItemStack(CustomRegisters.cloner), this.x + 14, this.y + 5);
			} else {
				render.renderItemAndEffectIntoGUI(new ItemStack(CustomRegisters.carpentyBench), this.x + 9, this.y + 5);
			}
		} else {
			render.renderItemAndEffectIntoGUI(itemstack, this.x + 9, this.y + 5);
		}
		GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
	}

	public void startAnimation(Minecraft mc) {
		RecipeBook recipebook = mc.player.getRecipeBook();
		label21: for (RecipeList recipelist : (this.isGlobal ? RecipeBookClient.RECIPES_BY_TAB.get(this.category)
				: ClientProxy.MOD_RECIPES_BY_TAB.get(this.category))) {
			Iterator<IRecipe> iterator = recipelist.getRecipes(recipebook.isFilteringCraftable()).iterator();
			while (true) {
				if (!iterator.hasNext()) {
					continue label21;
				}
				IRecipe irecipe = iterator.next();
				if (recipebook.isNew(irecipe)) {
					break;
				}
			}
			this.animationTime = 15.0F;
			return;
		}
	}

	public boolean updateVisibility() {
		if (this.category == CustomRegisters.tab) {
			this.visible = true;
		} else {
			List<RecipeList> list = RecipeBookClient.RECIPES_BY_TAB.get(this.category);
			if (!this.isGlobal) {
				list = ClientProxy.MOD_RECIPES_BY_TAB.get(this.category);
			}
			this.visible = false;
			for (RecipeList recipelist : list) {
				if (recipelist.isNotEmpty() && recipelist.containsValidRecipes()) {
					this.visible = true;
					break;
				}
			}
		}
		return this.visible;
	}

}