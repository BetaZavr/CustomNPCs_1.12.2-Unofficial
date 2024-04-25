package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.items.crafting.NpcShapedRecipes;
import noppes.npcs.items.crafting.NpcShapelessRecipes;

public class ContainerManageRecipes extends Container {
	public InventoryBasic craftingMatrix;
	public INpcRecipe recipe;
	public int size = 0;
	public int width = 0;

	public ContainerManageRecipes(EntityPlayer player, int size) {
		if (size > 4) {
			size = 4;
		}
		this.size = size * size;
		this.width = size;
		this.craftingMatrix = new InventoryBasic("crafting", false, this.size + 1);
		this.recipe = new NpcShapedRecipes();
		this.addSlotToContainer(new Slot((IInventory) this.craftingMatrix, 0, 87, 61));
		for (int i = 0; i < size; ++i) {
			for (int j = 0; j < size; ++j) {
				this.addSlotToContainer(
						new Slot((IInventory) this.craftingMatrix, i * this.width + j + 1, j * 18 + 8, i * 18 + 35));
			}
		}
		for (int i2 = 0; i2 < 3; ++i2) {
			for (int l1 = 0; l1 < 9; ++l1) {
				this.addSlotToContainer(
						new Slot((IInventory) player.inventory, l1 + i2 * 9 + 9, 8 + l1 * 18, 113 + i2 * 18));
			}
		}
		for (int j2 = 0; j2 < 9; ++j2) {
			this.addSlotToContainer(new Slot((IInventory) player.inventory, j2, 8 + j2 * 18, 171));
		}
	}

	public boolean canInteractWith(EntityPlayer entityplayer) {
		return true;
	}

	public void saveRecipe(String group, String name, boolean shaped) {
		if (group.isEmpty()) {
			group = "default";
		}
		if (name.isEmpty()) {
			name = "default";
		}
		if (this.craftingMatrix.getSizeInventory() != this.size + 1) {
			return;
		}
		NonNullList<Ingredient> ingredients = NonNullList.create();
		for (int i = 1, j = 0; i <= this.size; i++) {
			ItemStack stack = this.craftingMatrix.getStackInSlot(i);
			Ingredient ing = stack.isEmpty() ? Ingredient.EMPTY
					: Ingredient.fromStacks(new ItemStack[] { stack.copy() });
			if (shaped) {
				ingredients.add(i - 1, ing);
			} else if (ing != Ingredient.EMPTY) {
				ingredients.add(j, ing);
				j++;
			}
		}
		INpcRecipe recipe;
		if (shaped) {
			recipe = new NpcShapedRecipes(group, name, this.width, this.width, ingredients,
					this.craftingMatrix.getStackInSlot(0).copy());
			((NpcShapedRecipes) recipe).global = this.width == 3;
		} else {
			recipe = new NpcShapelessRecipes(group, name, ingredients, this.craftingMatrix.getStackInSlot(0).copy());
			((NpcShapelessRecipes) recipe).global = this.width == 3;
		}
		this.recipe = recipe;
	}

	public void setRecipe(INpcRecipe recipe) {
		this.recipe = recipe;
		this.craftingMatrix.setInventorySlotContents(0, this.recipe.getProduct().getMCItemStack());
		NonNullList<Ingredient> ings = null;
		if (this.recipe.isShaped()) {
			ings = ((NpcShapedRecipes) this.recipe).getIngredients();
		} else {
			ings = ((NpcShapelessRecipes) this.recipe).getIngredients();
		}
		for (int i = 0; i < ings.size() && i < (1 + this.size); i++) {
			if (ings.get(i).getMatchingStacks().length == 0) {
				this.craftingMatrix.setInventorySlotContents((i + 1), ItemStack.EMPTY);
				continue;
			}
			this.craftingMatrix.setInventorySlotContents((i + 1), ings.get(i).getMatchingStacks()[0]);
		}
		this.detectAndSendChanges();
	}

	public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int i) {
		return ItemStack.EMPTY;
	}

}
