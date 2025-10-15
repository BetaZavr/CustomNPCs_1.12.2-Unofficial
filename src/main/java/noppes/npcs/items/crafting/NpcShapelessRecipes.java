package noppes.npcs.items.crafting;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.WrapperRecipe;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.reflection.item.crafting.IngredientReflection;
import noppes.npcs.reflection.item.crafting.ShapelessRecipesReflection;
import noppes.npcs.util.Util;

public class NpcShapelessRecipes extends ShapelessRecipes implements INpcRecipe, IRecipe
{
	public static INpcRecipe createRecipe(String group, String name, boolean global, ItemStack stack, Object... map) {
		StringBuilder allRows = new StringBuilder();
		int objPos = 0;
		if (map[objPos] instanceof String[]) {
			String[] var8;
			String[] var7 = var8 = (String[]) map[objPos++];
			for (int var9 = var7.length, var10 = 0; var10 < var9; ++var10) {
				String row = var8[var10];
				allRows.append(row);
			}
		} else {
			while (map[objPos] instanceof String) {
				String row = (String) map[objPos++];
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
		for (int i = 0, slot = 0; i < allRows.length(); ++i) {
			char c = allRows.charAt(i);
			if (mapIngredients.containsKey(c)) {
				ItemStack s = mapIngredients.get(c).copy();
				if (!s.isEmpty()) {
					ingredients.add(slot++, Ingredient.fromStacks(s));
				}
			}
		}
        return new NpcShapelessRecipes(group, name, global, ingredients, stack);
	}

	public static NpcShapelessRecipes read(NBTTagCompound compound) {
		NpcShapelessRecipes recipe = new NpcShapelessRecipes(Util.instance.getResourceName(compound.getString("Group")),
				Util.instance.getResourceName(compound.getString("Name")),
				compound.getBoolean("Global"),
				NBTTags.getIngredientList(compound.getTagList("Materials", 10)),
				new ItemStack(compound.getCompoundTag("Item")));
		recipe.id = compound.getInteger("ID");
		recipe.availability.load(compound.getCompoundTag("Availability"));
		recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
		recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
		recipe.known = compound.getBoolean("IsKnown");
		recipe.main = compound.getBoolean("IsMain");
		if (recipe.getRegistryName() == null) {
			String key = recipe.getNpcGroup().toLowerCase() + "_" + recipe.name.toLowerCase();
			while (key.contains(" ")) {
				key = key.replace(" ", "_");
			}
			recipe.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
		return recipe;
	}
	/** Is the ItemStack that you get when craft the recipe. */
	private ItemStack recipeOutput;
	/** Is a List of ItemStack that composes the recipe. */
	public NonNullList<Ingredient> recipeItems;
	public boolean isSimple;
	public Availability availability;
	private boolean global;
	public int id;
	public boolean ignoreDamage;
	public boolean ignoreNBT;
	public boolean known;
	public boolean main = false;

	public String name;

	private int recipeHeight;
	private int recipeWidth;

	public boolean savesRecipe;
	private final WrapperRecipe wrapper = new WrapperRecipe();

	public NpcShapelessRecipes(String group, String name, boolean isGlobal, NonNullList<Ingredient> ingredients, ItemStack result) {
		super(Util.instance.getResourceName(group), result, ingredients);
		if (result.isEmpty()) { result = new ItemStack(Blocks.COBBLESTONE); }
		this.recipeOutput = result;
		if (ingredients.isEmpty()) { ingredients.add(Ingredient.fromStacks(new ItemStack(Blocks.COBBLESTONE))); }
		this.recipeItems = ingredients;
		boolean simple = true;
		for (Ingredient i : ingredients)
			simple &= i.isSimple();
		this.isSimple = simple;
		this.id = -1;
		this.name = Util.instance.getResourceName(name);
		this.availability = new Availability();
		this.global = isGlobal;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		this.known = true;
		this.recipeWidth = isGlobal ? 3 : 4;
		this.recipeHeight = isGlobal ? 3 : 4;
		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
	}

	private NonNullList<Ingredient> getGrid() {
		NonNullList<Ingredient> newIngredient = NonNullList.create();
        for (Ingredient ingredient : recipeItems) {
            if (ingredient.getMatchingStacks().length == 0) { continue; }
            boolean added = false;
            for (ItemStack stack : ingredient.getMatchingStacks()) {
                if (!NoppesUtilServer.IsItemStackNull(stack)) {
                    added = true;
                    break;
                }
            }
            if (added) {
                newIngredient.add(ingredient);
            }
        }
		return newIngredient;
	}

	// checks item in crafting slot against ingredient item variants
	public boolean apply(@Nullable Ingredient ingredient, @Nullable ItemStack stack) {
		if (stack != null && ingredient != null) {
			ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length == 0 && stack.isEmpty()) { return true; } // is Air
            for (ItemStack ingStack : stacks) {
				if (ingStack.getItem() != stack.getItem() || ingStack.isEmpty() || stack.isEmpty()) { continue; }
				if (NoppesUtilPlayer.compareItems(stack, ingStack, ignoreDamage, ignoreNBT) && ingStack.getCount() <= stack.getCount()) {
					return true;
				}
            }
        }
        return false;
    }

	@Override
	public boolean canFit(int width, int height) {
		if (!global && (width != 4 || height != 4)) { return false; }
		return width * height >= getGrid().size();
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
		this.recipeOutput = recipe.getProduct().getMCItemStack();
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
		ShapelessRecipesReflection.setGroup(this, recipe.getNpcGroup());
		this.known = recipe.isKnown();
		this.recipeWidth = recipe.getWidthRecipe();
		this.recipeHeight = recipe.getHeightRecipe();
		int w = this.global ? 3 : 4;
		if (recipeWidth != w || recipeHeight != w) {
			recipeWidth = w;
			recipeHeight = w;
		}
		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
		savesRecipe = true;
	}

	@Override
	public void delete() {
		//RecipeController.getInstance().delete(this.id);
	}

	@Override
	public boolean equal(INpcRecipe recipe) {
		return recipe.getClass() == NpcShapelessRecipes.class && recipe.getNpcGroup().equals(this.getGroup()) && recipe.getName().equals(this.name) && ItemStack.areItemStacksEqualUsingNBTShareTag(recipe.getProduct().getMCItemStack(), this.recipeOutput);
	}

	public boolean equals(INpcRecipe recipe) {
		return !recipe.isShaped() && this.id == recipe.getId() && recipe.isGlobal() != this.global && recipe.getName().equals(this.name) && recipe.getNpcGroup().equals(this.getGroup());
	}

	@Override
	public IAvailability getAvailability() {
		return this.availability;
	}

	@Override
	public int getHeightRecipe() {
		return this.recipeHeight;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public boolean getIgnoreDamage() {
		return this.ignoreDamage;
	}

	@Override
	public boolean getIgnoreNBT() {
		return this.ignoreNBT;
	}

	@Override
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
		compound.setTag("Availability", this.availability.save(new NBTTagCompound()));
		compound.setString("Name", this.name);
		compound.setBoolean("Global", this.global);
		compound.setBoolean("IgnoreDamage", this.ignoreDamage);
		compound.setBoolean("IgnoreNBT", this.ignoreNBT);
		compound.setString("Group", this.getGroup());
		compound.setBoolean("IsKnown", this.known);
		compound.setBoolean("IsShaped", false);
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

	@Override
	public int getWidthRecipe() {
		return this.recipeWidth;
	}

	@Override
	public boolean isGlobal() {
		return this.global;
	}

	@Override
	public boolean isKnown() {
		return this.known;
	}

	@Override
	public boolean isShaped() {
		return false;
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
		wrapper.isShaped = false;
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
		wrapper.availability.load(availability.save(new NBTTagCompound()));
		wrapper.main = main;

		wrapper.recipeItems.clear();
		int pos = 0;
		for (Ingredient ingr : recipeItems) {
			ItemStack[] rawMatchingStacks = IngredientReflection.getRawMatchingStacks(ingr);
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
	public boolean isChanged() {
		return savesRecipe;
	}

	@Override
	public void setRecipeOutput(ItemStack item) {
		if (item == null || item.isEmpty()) { return; }
		recipeOutput = item.copy();
	}

	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World worldIn) {
		if (recipeItems.isEmpty()) { return false; }
		if (global && inv.getHeight() > 3 && inv.getHeight() > 3) { return false; }
		if (!global && inv.getWidth() == 4 && inv.getHeight() == 4) { return false; }
		List<Ingredient> ings = new ArrayList<>();
		for (Ingredient ingredient : recipeItems) {
			if (ingredient.getMatchingStacks().length == 0) { continue; }
			boolean isEmpty = true;
			for (ItemStack stack : ingredient.getMatchingStacks()) {
				if (!stack.isEmpty()) {
					isEmpty = false;
					break;
				}
			}
			if (isEmpty) { continue; }
			ings.add(ingredient);
		}
		List<ItemStack> inputs = new ArrayList<>();
		for (int i = 0; i < inv.getHeight(); ++i) {
			for (int j = 0; j < inv.getWidth(); ++j) {
				ItemStack itemstack = inv.getStackInRowAndColumn(j, i);
				if (!itemstack.isEmpty()) {
					inputs.add(itemstack);
				}
			}
		}
		if (inputs.size() < ings.size()) {
			return false;
		}
		for (int i = 0; i < ings.size(); i++) {
			for (int j = 0; j < inputs.size(); j++) {
				if (apply(ings.get(i), inputs.get(j))) {
					ings.remove(ings.get(i));
					inputs.remove(inputs.get(j));
					i = -1;
					break;
				}
			}
			if (ings.isEmpty() || inputs.isEmpty()) { break; }
		}
		return ings.isEmpty() && inputs.isEmpty();
	}

	@Override
	public void setIgnoreDamage(boolean ignoreDamage) {
		this.ignoreDamage = ignoreDamage;
		savesRecipe = true;
	}

	@Override
	public void setIgnoreNBT(boolean ignoreNBT) {
		this.ignoreNBT = ignoreNBT;
		savesRecipe = true;
	}

	@Override
	public void setKnown(boolean bo) {
		known = bo;
		savesRecipe = true;
	}

	@Override
	public void setNbt(INbt nbt) {
		copy(NpcShapedRecipes.read(nbt.getMCNBT()));
	}

}
