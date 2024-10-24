package noppes.npcs.items.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
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
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperRecipe;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.mixin.item.crafting.IIngredientMixin;
import noppes.npcs.mixin.item.crafting.IShapedRecipesMixin;
import noppes.npcs.util.Util;

public class NpcShapedRecipes extends ShapedRecipes implements INpcRecipe, IRecipe
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
        return new NpcShapedRecipes(group, name, global, ingredients, stack);
	}
	public static NpcShapedRecipes read(NBTTagCompound compound) {
		NpcShapedRecipes recipe = new NpcShapedRecipes(Util.instance.getResourceName(compound.getString("Group")),
				Util.instance.getResourceName(compound.getString("Name")),
				compound.getBoolean("Global"),
				NBTTags.getIngredientList(compound.getTagList("Materials", 10)),
				new ItemStack(compound.getCompoundTag("Item")));
		recipe.recipeWidth = compound.getInteger("Width");
		recipe.recipeHeight = compound.getInteger("Height");
		recipe.id = compound.getInteger("ID");
		recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
		recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
		recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
		recipe.known = compound.getBoolean("IsKnown");
		recipe.main = compound.getBoolean("IsMain");
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
	public Availability availability;
	private boolean global;
	// New
	public int id;
	public boolean ignoreDamage;
	public boolean ignoreNBT;
	public boolean known;
	public boolean main = false;

	public String name;

	public boolean savesRecipe;
	private final WrapperRecipe wrapper = new WrapperRecipe();

	public NpcShapedRecipes(String group, String name, boolean isGlobal, NonNullList<Ingredient> ingredients, ItemStack result) {
		super(Util.instance.getResourceName(group), isGlobal ? 3 : 4, isGlobal ? 3 : 4, ingredients, result);
		this.recipeWidth = isGlobal ? 3 : 4;
		this.recipeHeight = isGlobal ? 3 : 4;
		if (ingredients.isEmpty()) { ingredients.add(Ingredient.fromStacks(new ItemStack(Blocks.COBBLESTONE))); }
		this.recipeItems = ingredients;
		if (result.isEmpty()) { result = new ItemStack(Blocks.COBBLESTONE); }
		this.recipeOutput = result;
		this.id = -1;
		this.name = Util.instance.getResourceName(name);
		this.availability = new Availability();
		this.global = isGlobal;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		// New
		this.known = true;
		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
	}

	// checks item in crafting slot against ingredient item variants
	public boolean apply(@Nullable Ingredient ingredient, @Nullable ItemStack stack) { // New
        if (stack != null && ingredient != null) {
            ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length == 0 && stack.isEmpty()) { return true; }
            for (ItemStack ingStack : stacks) {
				if (ingStack.getItem() != stack.getItem() || ingStack.isEmpty() || stack.isEmpty()) { continue; }
				if (NoppesUtilPlayer.compareItems(stack, ingStack, this.ignoreDamage, this.ignoreNBT) && ingStack.getCount() <= stack.getCount()) {
					return true;
				}
            }
        }
        return false;
    }

	@Override
	public boolean canFit(int width, int height) {
		if (global) {
			return width >= this.recipeWidth && height >= this.recipeHeight;
		}
		return width == this.recipeWidth && height == this.recipeHeight;
	}

	private boolean checkMatch(InventoryCrafting inv, int width, int height, boolean isReversion) {
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
		for (int w = 0; w < inv.getWidth(); ++w) {
			for (int h = 0; h < inv.getHeight(); ++h) {
				int k = w - width;
				int l = h - height;
				Ingredient ingredient = Ingredient.EMPTY;
				if (k >= 0 && l >= 0 && k < this.recipeWidth && l < this.recipeHeight) {
					int id;
					if (isReversion) { id = this.recipeWidth - k - 1 + l * this.recipeWidth; }
					else { id =k + l * this.recipeWidth; }
					ingredient = this.recipeItems.get(id);
				}
				if (!this.apply(ingredient, inv.getStackInRowAndColumn(w, h))) {
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
		this.recipeOutput = recipe instanceof NpcShapelessRecipes ? ((NpcShapelessRecipes) recipe).recipeOutput : ((NpcShapedRecipes) recipe).recipeOutput;
		NonNullList<Ingredient> ingredients = recipe instanceof NpcShapelessRecipes ? ((NpcShapelessRecipes) recipe).recipeItems : ((NpcShapedRecipes) recipe).recipeItems;
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
		((IShapedRecipesMixin) this).npcs$setGroup(recipe.getNpcGroup());
		this.known = recipe.isKnown();
		this.recipeWidth = recipe.getWidth();
		this.recipeHeight = recipe.getHeight();
		int w = this.global ? 3 : 4;
		if (this.recipeWidth != w) {
			this.recipeWidth = w;
			this.recipeHeight = w;
		}
		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
	}

	public void delete() {
		//RecipeController.getInstance().delete(this.id);
	}

	@Override
	public boolean equal(INpcRecipe recipe) {
		return recipe.getClass() == NpcShapedRecipes.class && recipe.getNpcGroup().equals(this.getGroup()) && recipe.getName().equals(this.name) && ItemStack.areItemStacksEqualUsingNBTShareTag(recipe.getProduct().getMCItemStack(), this.recipeOutput);
	}

	public boolean equals(INpcRecipe recipe) {
		return recipe.isShaped() && this.id == recipe.getId() && recipe.isGlobal() == this.global && recipe.getName().equals(this.name) && recipe.getNpcGroup().equals(this.getGroup());
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
		compound.setString("Group", this.getGroup());
		compound.setBoolean("IsKnown", this.known);
		compound.setBoolean("IsShaped", true);
		compound.setBoolean("IsMain", this.main);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public String getNpcGroup() {
		return this.getGroup();
	}

	@Override
	public @Nonnull ItemStack getRecipeOutput() { return recipeOutput; }

	@Override
	public IItemStack getProduct() {
		return (IItemStack) getRecipeOutput().getCapability(ItemStackWrapper.ITEM_SCRIPTED_DATA_CAPABILITY, null);
	}

	// New
	@Override
	public IItemStack[][] getRecipe() {
		IItemStack[][] allStacks = new IItemStack[this.recipeItems.size()][];
		for (int i = 0; i < this.recipeItems.size(); i++) {
			ItemStack[] arr = recipeItems.get(i).getMatchingStacks();
            allStacks[i] = new IItemStack[arr.length];
            for (int j = 0; j < arr.length; j++) {
                allStacks[i][j] = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(arr[j]);
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
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
		if (this.getGroup().isEmpty()) {
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
	public boolean isRecipeItemsEmpty() {
		for (Ingredient ingredient : recipeItems) {
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				if (stack != null && !stack.isEmpty()) { return false; }
			}
		}
		return true;
	}

	@Override
	public WrapperRecipe getWrapperRecipe() {
		wrapper.parent = this;
		wrapper.isShaped = true;
		wrapper.global = global;
		wrapper.known = known;
		wrapper.ignoreDamage = ignoreDamage;
		wrapper.ignoreNBT = ignoreNBT;
		wrapper.id = id;
		wrapper.width = recipeWidth;
		wrapper.height = recipeHeight;
		wrapper.group = getGroup();
		wrapper.domen = CustomNpcs.MODID;
		wrapper.name = (main ? ((char)167) + "b" : "") + name;
		wrapper.product = recipeOutput.copy();
		wrapper.availability.readFromNBT(availability.writeToNBT(new NBTTagCompound()));
		wrapper.main = main;

		wrapper.recipeItems.clear();
		int pos = 0;
		for (Ingredient ingr : recipeItems) {
			ItemStack[] rawMatchingStacks = ((IIngredientMixin) ingr).npcs$getRawMatchingStacks();
			ItemStack[] array = new ItemStack[rawMatchingStacks.length];
			for (int j = 0; j < rawMatchingStacks.length; j++) {
				array[j] = rawMatchingStacks[j].copy();
			}
			wrapper.recipeItems.put(pos, array);
			pos ++;
		}
		return wrapper;
	}

	@Override
	public boolean isMain() { return main; }

	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World world) {
		if (recipeItems.isEmpty() || (inv.getWidth() == 3 && !global) || (inv.getWidth() == 4 && global)) {
			return false;
		}
		for (int width = 0; width <= inv.getWidth() - this.recipeWidth; ++width) {
			for (int height = 0; height <= inv.getHeight() - this.recipeHeight; ++height) {
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
