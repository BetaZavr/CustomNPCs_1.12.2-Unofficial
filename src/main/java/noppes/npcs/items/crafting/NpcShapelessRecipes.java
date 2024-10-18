package noppes.npcs.items.crafting;

import java.util.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

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
import noppes.npcs.mixin.item.crafting.IShapelessRecipesMixin;
import noppes.npcs.util.Util;

public class NpcShapelessRecipes extends ShapelessRecipes implements INpcRecipe, IRecipe // Changed
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
		recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
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
	public ItemStack recipeOutput;
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

	// checks item in crafting slot against ingredient item variants
	public boolean apply(@Nullable Ingredient ingredient, @Nullable ItemStack stack) {
		if (stack != null && ingredient != null) {
			ItemStack[] stacks = ingredient.getMatchingStacks();
            if (stacks.length == 0 && stack.isEmpty()) { return true; } // is Air
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
			return width * height >= this.recipeItems.size();
		}
		return width == this.recipeWidth && height == this.recipeHeight;
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
		((IShapelessRecipesMixin) this).npcs$setGroup(recipe.getNpcGroup());
		this.known = recipe.isKnown();
		this.recipeWidth = recipe.getWidth();
		this.recipeHeight = recipe.getHeight();
		int w = this.global ? 3 : 4;
		if (recipeWidth != w || recipeHeight != w) {
			recipeWidth = w;
			recipeHeight = w;
		}
		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
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
	public int getHeight() {
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
		compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
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
	public int getWidth() {
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

	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World worldIn) {
		if (this.recipeItems.isEmpty() || (inv.getWidth() == 3 && !this.global) || (inv.getWidth() == 4 && this.global)) {
			return false;
		}
		List<ItemStack> inputs = Lists.newArrayList();
		for (int i = 0; i < inv.getHeight(); ++i) {
			for (int j = 0; j < inv.getWidth(); ++j) {
				ItemStack itemstack = inv.getStackInRowAndColumn(j, i);
				if (!itemstack.isEmpty()) {
					inputs.add(itemstack);
				}
			}
		}
		if (inputs.size() != this.recipeItems.size()) {
			return false;
		}
        List<Ingredient> ings = new ArrayList<>(this.recipeItems);
		for (int i = 0; i < ings.size(); i++) {
			for (int j = 0; j < inputs.size(); j++) {
				if (this.apply(ings.get(i), inputs.get(j))) {
					ings.remove(ings.get(i));
					inputs.remove(inputs.get(j));
					i = -1;
					break;
				}
			}
		}

		return ings.isEmpty() && inputs.isEmpty();
	}

	@Override
	public void setIgnoreDamage(boolean bo) {
		this.ignoreDamage = bo;
	}

	@Override
	public void setIgnoreNBT(boolean bo) {
		this.ignoreNBT = bo;
	}

	@Override
	public void setKnown(boolean bo) {
		this.known = bo;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.copy(NpcShapedRecipes.read(nbt.getMCNBT()));
	}

}
