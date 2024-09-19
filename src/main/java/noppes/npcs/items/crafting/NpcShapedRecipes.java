package noppes.npcs.items.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.mixin.api.item.crafting.IngredientAPIMixin;

public class NpcShapedRecipes extends ShapedRecipes implements INpcRecipe, IRecipe // Changed
{
	public static INpcRecipe createRecipe(String group, String name, boolean global, ItemStack stack, Object... map) {
		StringBuilder allRows = new StringBuilder();
		int objPos = 0;
		int width = 0;
		int height = 0;
		if (map[objPos] instanceof String[]) {
			String[] var8;
			String[] var7 = var8 = (String[]) map[objPos++];
			for (int var9 = var7.length, var10 = 0; var10 < var9; ++var10) {
				String row = var8[var10];
				++height;
				width = row.length();
				allRows.append(row);
			}
		} else {
			while (map[objPos] instanceof String) {
				String row = (String) map[objPos++];
				++height;
				width = row.length();
				allRows.append(row);
			}
		}
		Map<Character, ItemStack> mapIngredients = new HashMap<>();
		while (objPos < map.length) {
			Character c = (Character) map[objPos];
			ItemStack st = ItemStack.EMPTY;
			if (map[objPos + 1] instanceof Item) {
				st = new ItemStack((Item) map[objPos + 1]);
			} else if (map[objPos + 1] instanceof Block) {
				st = new ItemStack((Block) map[objPos + 1], 1, -1);
			} else if (map[objPos + 1] instanceof ItemStack) {
				st = (ItemStack) map[objPos + 1];
			}
			mapIngredients.put(c, st);
			objPos += 2;
		}
		NonNullList<Ingredient> ingredients = NonNullList.create();
		for (int slot = 0; slot < width * height; ++slot) {
			char c = allRows.charAt(slot);
			if (mapIngredients.containsKey(c)) {
				ingredients.add(slot, Ingredient.fromStacks(mapIngredients.get(c).copy()));
			} else {
				ingredients.add(slot, Ingredient.EMPTY);
			}
		}
		NpcShapedRecipes newrecipe = new NpcShapedRecipes(group, name, width, height, ingredients, stack);
		newrecipe.global = global;
		return newrecipe;
	}
	public static NpcShapedRecipes read(NBTTagCompound compound) {
		NpcShapedRecipes recipe = new NpcShapedRecipes(compound.getString("Group"), compound.getString("Name"),
				compound.getInteger("Width"), compound.getInteger("Height"),
				NBTTags.getIngredientList(compound.getTagList("Materials", 10)),
				new ItemStack(compound.getCompoundTag("Item")));
		recipe.id = compound.getInteger("ID");
		recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
		recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
		recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
		recipe.global = compound.getBoolean("Global");
		// New
		recipe.known = compound.getBoolean("IsKnown");
		return recipe;
	}
	/** How many horizontal slots this recipe is wide. */
	public int recipeWidth;
	/** How many vertical slots this recipe uses. */
	public int recipeHeight;
	/** Is array of ItemStack that composes the recipe. */
	public NonNullList<Ingredient> recipeItems;
	/** Is the ItemStack that you get when craft the recipe. */
	public ItemStack recipeOutput;
	public String group;
	public Availability availability;
	public boolean global;
	// New
	public int id;
	public boolean ignoreDamage;
	public boolean ignoreNBT;
	public boolean known;

	public String name;

	public boolean savesRecipe;

	public NpcShapedRecipes() {
		super("customnpcs", 3, 3, NonNullList.create(), ItemStack.EMPTY);
		this.group = "customnpcs";
		this.recipeWidth = 3;
		this.recipeHeight = 3;
		this.recipeItems = NonNullList.create();
		this.recipeOutput = ItemStack.EMPTY;
		this.id = -1;
		this.availability = new Availability();
		this.global = false;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		this.name = "";
		// New
		this.known = true;
	}

	public NpcShapedRecipes(String group, String name, int width, int height, NonNullList<Ingredient> ingredients,
			ItemStack result) {
		super(CustomNpcs.MODID, width, height, ingredients, result);
		this.group = group;
		this.recipeWidth = width;
		this.recipeHeight = height;
		this.recipeItems = ingredients;
		this.recipeOutput = result;
		this.id = -1;
		this.name = name;
		this.availability = new Availability();
		this.global = false;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		// New
		this.known = true;
		if (this.getRegistryName() == null) {
			String key = this.group.toLowerCase() + "_" + this.name.toLowerCase();
			while (key.contains(" ")) {
				key = key.replace(" ", "_");
			}
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
	}

	public boolean apply(@Nullable Ingredient ingredient, @Nullable ItemStack stack) { // New
        if (stack != null && ingredient != null) {
            ItemStack[] stacks = ((IngredientAPIMixin) ingredient).npcs$getMatchingStacks();
            if ((stacks == null || stacks.length == 0) && stack.isEmpty()) {
                return true;
            }
            if (stacks != null) {
                for (ItemStack ingStack : stacks) {
                    if (ingStack.getItem() == stack.getItem()) {
                        if (!ingStack.isEmpty() && !stack.isEmpty()
                                && NoppesUtilPlayer.compareItems(stack, ingStack, this.ignoreDamage, this.ignoreNBT)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

	@Override
	public boolean canFit(int width, int height) {
		return width >= this.recipeWidth && height >= this.recipeHeight;
	}

	private boolean checkMatch(InventoryCrafting inv, int width, int height, boolean bo) {
		int ingSize = 0;
		for (Ingredient ingredient : this.recipeItems) {
			boolean has = false;
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				if (!stack.isEmpty()) {
					has = true;
					break;
				}
			}
			if (has) {
				ingSize++;
			}
		}
		for (int i = 0; i < inv.getWidth(); ++i) {
			for (int j = 0; j < inv.getHeight(); ++j) {
				int k = i - width;
				int l = j - height;
				Ingredient ingredient = Ingredient.EMPTY;
				if (k >= 0 && l >= 0 && k < this.recipeWidth && l < this.recipeHeight) {
					if (bo) {
						ingredient = this.recipeItems.get(this.recipeWidth - k - 1 + l * this.recipeWidth);
					} else {
						ingredient = this.recipeItems.get(k + l * this.recipeWidth);
					}
				}
				if (!this.apply(ingredient, inv.getStackInRowAndColumn(i, j))) {
					return false;
				} // Changed
				if (ingredient.getMatchingStacks().length > 0) {
					ingSize--;
				}
			}
		}
		return ingSize == 0;
	}

	public void copy(INpcRecipe recipe) {
		if (recipe == null || this == recipe) {
			return;
		}
		this.id = recipe.getId();
		this.name = recipe.getName();
		this.availability = (Availability) recipe.getAvailability();
		this.global = recipe.isGlobal();
		this.ignoreDamage = recipe.getIgnoreDamage();
		this.ignoreNBT = recipe.getIgnoreNBT();
		this.recipeOutput = recipe instanceof NpcShapelessRecipes ? ((NpcShapelessRecipes) recipe).recipeOutput
				: ((NpcShapedRecipes) recipe).recipeOutput;
		NonNullList<Ingredient> ingredients = recipe instanceof NpcShapelessRecipes
				? ((NpcShapelessRecipes) recipe).recipeItems
				: ((NpcShapedRecipes) recipe).recipeItems;
		if (this.recipeItems != ingredients) {
			this.recipeItems.clear();
			for (Ingredient ing : ingredients) {
				if (ing.getMatchingStacks().length == 0) {
					this.recipeItems.add(Ingredient.EMPTY);
				} else {
					this.recipeItems.add(Ingredient.fromStacks(ing.getMatchingStacks()));
				}
			}
		}
		this.group = recipe.getNpcGroup();
		this.known = recipe.isKnown();
		this.recipeWidth = recipe.getWidth();
		this.recipeHeight = recipe.getHeight();
		int w = this.global ? 3 : 4;
		if (this.recipeWidth != w) {
			this.recipeWidth = w;
			this.recipeHeight = w;
		}
		if (this.getRegistryName() == null) {
			String key = this.group.toLowerCase() + "_" + this.name.toLowerCase();
			while (key.contains(" ")) {
				key = key.replace(" ", "_");
			}
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
	}

	public void delete() {
		RecipeController.getInstance().delete(this.id);
	}

	@Override
	public boolean equal(INpcRecipe recipe) {
		return recipe.getClass() == NpcShapedRecipes.class && recipe.getNpcGroup().equals(this.group)
				&& recipe.getName().equals(this.name) && ItemStack
						.areItemStacksEqualUsingNBTShareTag(recipe.getProduct().getMCItemStack(), this.recipeOutput);
	}

	public boolean equals(INpcRecipe recipe) {
		return recipe.isShaped() && this.id == recipe.getId() && recipe.isGlobal() == this.global
				&& recipe.getName().equals(this.name) && recipe.getNpcGroup().equals(this.group);
	}

	@Override
	public IAvailability getAvailability() {
		return this.availability;
	}

	@Override
	public @Nonnull ItemStack getCraftingResult(@Nonnull InventoryCrafting inv) {
		if (this.recipeOutput.isEmpty()) {
			return ItemStack.EMPTY;
		}
		return this.recipeOutput.copy();
	}

	public int getHeight() {
		return this.recipeHeight;
	}

	public int getId() {
		return this.id;
	}

	public boolean getIgnoreDamage() {
		return this.ignoreDamage;
	}

	public boolean getIgnoreNBT() {
		return this.ignoreNBT;
	}

	public String getName() {
		return this.name;
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("ID", this.id);
		compound.setInteger("Width", this.recipeWidth);
		compound.setInteger("Height", this.recipeHeight);
		if (this.recipeOutput != null) {
			compound.setTag("Item", this.recipeOutput.writeToNBT(new NBTTagCompound()));
		}
		compound.setTag("Materials", NBTTags.nbtIngredientList(this.recipeItems));
		compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
		compound.setString("Name", this.name);
		compound.setBoolean("Global", this.global);
		compound.setBoolean("IgnoreDamage", this.ignoreDamage);
		compound.setBoolean("IgnoreNBT", this.ignoreNBT);
		// New
		compound.setString("Group", this.group);
		compound.setBoolean("IsKnown", this.known);
		compound.setBoolean("IsShaped", true);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public String getNpcGroup() {
		return this.group;
	}

	@Override
	public IItemStack getProduct() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.recipeOutput);
	}

	// New
	@Override
	public IItemStack[][] getRecipe() {
		IItemStack[][] allStacks = new IItemStack[this.recipeItems.size()][];
		for (int i = 0; i < this.recipeItems.size(); i++) {
			ItemStack[] arr = ((IngredientAPIMixin) this.recipeItems.get(i)).npcs$getMatchingStacks();
            if (arr != null) {
				allStacks[i] = new IItemStack[arr.length];
				for (int j = 0; j < arr.length; j++) {
					allStacks[i][j] = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(arr[j]);
				}
			}
		}
		return allStacks;
	}

	@Override
	public @Nonnull NonNullList<ItemStack> getRemainingItems(@Nonnull InventoryCrafting inventoryCrafting) {
		NonNullList<ItemStack> list = NonNullList.withSize(inventoryCrafting.getSizeInventory(), ItemStack.EMPTY);
		for (int i = 0; i < list.size(); ++i) {
			ItemStack itemstack = inventoryCrafting.getStackInSlot(i);
			list.set(i, ForgeHooks.getContainerItem(itemstack));
		}
		return list;
	}

	public int getWidth() {
		return this.recipeWidth;
	}

	public boolean isGlobal() {
		return this.global;
	}

	@Override
	public boolean isKnown() {
		return this.known;
	}

	@Override
	public boolean isShaped() {
		return true;
	}

	public boolean isValid() {
		if (this.getRegistryName() == null) {
			String key = this.group.toLowerCase() + "_" + this.name.toLowerCase();
			while (key.contains(" ")) {
				key = key.replace(" ", "_");
			}
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
		if (this.group == null || this.group.isEmpty()) {
			return false;
		}
		if (this.name == null || this.name.isEmpty()) {
			return false;
		}
		if (this.recipeItems.isEmpty() || this.recipeOutput.isEmpty()) {
			return false;
		}
		for (Ingredient ing : this.recipeItems) {
			if (ing.getMatchingStacks().length != 0) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World world) {
		if (this.recipeItems.isEmpty() || (inv.getWidth() == 3 && !this.global)
				|| (inv.getWidth() == 4 && this.global)) {
			return false;
		}
		for (int width = 0; width <= 4 - this.recipeWidth; ++width) {
			for (int height = 0; height <= 4 - this.recipeHeight; ++height) {
				if (this.checkMatch(inv, width, height, true)) {
					return true;
				}
				if (this.checkMatch(inv, width, height, false)) {
					return true;
				}
			}
		}
		return false;
	}
	public void setIgnoreDamage(boolean bo) {
		this.ignoreDamage = bo;
	}

	public void setIgnoreNBT(boolean bo) {
		this.ignoreNBT = bo;
	}

	@Override
	public void setKnown(boolean known) {
		this.known = known;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.copy(NpcShapedRecipes.read(nbt.getMCNBT()));
	}

}
