package noppes.npcs.items.crafting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.RecipeController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.util.ObfuscationHelper;

public class NpcShapelessRecipes extends ShapelessRecipes implements INpcRecipe, IRecipe // Changed
{
	/** Is the ItemStack that you get when craft the recipe. */
	public ItemStack recipeOutput;
    /** Is a List of ItemStack that composes the recipe. */
    public NonNullList<Ingredient> recipeItems;
    public boolean isSimple;
	public Availability availability;
	public boolean global;
	public String group;
	public int id;
	public boolean ignoreDamage;
	public boolean ignoreNBT;
	public boolean known;
	public String name;
	private int recipeHeight;

	private int recipeWidth;

	public boolean savesRecipe;

	public NpcShapelessRecipes() {
		super("customnpcs", ItemStack.EMPTY, NonNullList.create());
        this.group = "customnpcs";
        this.recipeOutput = ItemStack.EMPTY;
        this.recipeItems = NonNullList.create();
        boolean simple = true;
        for (Ingredient i : this.recipeItems)
            simple &= i.isSimple();
        this.isSimple = simple;
		this.id = -1;
		this.availability = new Availability();
		this.global = false;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		this.name = "";
		this.known = true;
	}

	public NpcShapelessRecipes(String group, String name, NonNullList<Ingredient> ingredients, ItemStack result) {
		super(group, result, ingredients);
        this.group = group;
        this.recipeOutput = result;
        this.recipeItems = ingredients;
        boolean simple = true;
        for (Ingredient i : ingredients)
            simple &= i.isSimple();
        this.isSimple = simple;
		this.id = -1;
		this.name = name;
		this.availability = new Availability();
		this.global = false;
		this.ignoreDamage = false;
		this.ignoreNBT = false;
		this.savesRecipe = true;
		this.known = true;
		this.recipeWidth = ingredients.isEmpty() || ingredients.size() == 1 ? 1
				: ingredients.size() <= 4 ? 4 : ingredients.size() <= 9 ? 9 : 16;
		this.recipeHeight = this.recipeWidth;
		if (this.getRegistryName() == null) {
			String key = this.group.toLowerCase() + "_" + this.name.toLowerCase();
			while (key.indexOf(" ") != -1) {
				key = key.replace(" ", "_");
			}
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
	}
	
	public static INpcRecipe createRecipe(String group, String name, boolean global, ItemStack stack, Object... map) {
		String allRows = "";
		int objPos = 0;
		if (map[objPos] instanceof String[]) {
			String[] var8;
			String[] var7 = var8 = (String[]) map[objPos++];
			for (int var9 = var7.length, var10 = 0; var10 < var9; ++var10) {
				String row = var8[var10];
				allRows += row;
			}
		} else {
			while (map[objPos] instanceof String) {
				String row = (String) map[objPos++];
				allRows += row;
			}
		}
		Map<Character, ItemStack> mapIngredients = new HashMap<Character, ItemStack>();
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
					ingredients.add(slot++, Ingredient.fromStacks(new ItemStack[] { s }));
				}
			}
		}
		NpcShapelessRecipes newrecipe = new NpcShapelessRecipes(group, name, ingredients, stack);
		newrecipe.global = global;
		return newrecipe;
	}

	public static NpcShapelessRecipes read(NBTTagCompound compound) {
		NpcShapelessRecipes recipe = new NpcShapelessRecipes(compound.getString("Group"), compound.getString("Name"),
				NBTTags.getIngredientList(compound.getTagList("Materials", 10)),
				new ItemStack(compound.getCompoundTag("Item")));
		recipe.id = compound.getInteger("ID");
		recipe.availability.readFromNBT(compound.getCompoundTag("Availability"));
		recipe.ignoreDamage = compound.getBoolean("IgnoreDamage");
		recipe.ignoreNBT = compound.getBoolean("IgnoreNBT");
		recipe.global = compound.getBoolean("Global");
		recipe.known = compound.getBoolean("IsKnown");
		if (recipe.getRegistryName() == null) {
			String key = recipe.group.toLowerCase() + "_" + recipe.name.toLowerCase();
			while (key.indexOf(" ") != -1) {
				key = key.replace(" ", "_");
			}
			recipe.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
		return recipe;
	}

	public boolean apply(@Nullable Ingredient ingredient, @Nullable ItemStack stack) {
		if (stack == null) {
			return false;
		} else {
			ItemStack[] stakcs = ((ItemStack[]) ObfuscationHelper.getValue(Ingredient.class, ingredient, 2));
			if (stakcs.length == 0 && stack.isEmpty()) {
				return true;
			}
			for (ItemStack ingStack : stakcs) {
				if (ingStack.getItem() == stack.getItem()) {
					if (!ingStack.isEmpty() && !stack.isEmpty()
							&& NoppesUtilPlayer.compareItems(stack, ingStack, this.ignoreDamage, this.ignoreNBT)) {
						return true;
					}
				}
			}
			return false;
		}
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
			while (key.indexOf(" ") != -1) {
				key = key.replace(" ", "_");
			}
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, key));
		}
	}

	@Override
	public void delete() {
		RecipeController.instance.delete(this.id);
	}

	public boolean equals(INpcRecipe recipe) {
		return recipe.isShaped() == false && this.id == recipe.getId() && recipe.isGlobal() != this.global
				&& recipe.getName().equals(this.name) && recipe.getNpcGroup().equals(this.group);
	}

	@Override
	public IAvailability getAvailability() {
		return this.availability;
	}

	public ItemStack getCraftingItem(int slotId) {
		if (this.recipeItems == null || slotId >= this.recipeItems.size()) {
			return ItemStack.EMPTY;
		}
		Ingredient ingredients = this.recipeItems.get(slotId);
		if (ingredients.getMatchingStacks().length == 0) {
			return ItemStack.EMPTY;
		}
		return ingredients.getMatchingStacks()[0];
	}

	@Override
	public String getNpcGroup() {
		return this.group;
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
	public ItemStack[][] getRecipe() {
		ItemStack[][] allStacks = new ItemStack[this.recipeItems.size()][];
		for (int i = 0; i < this.recipeItems.size(); i++) {
			allStacks[i] = (ItemStack[]) ObfuscationHelper.getValue(Ingredient.class, this.recipeItems.get(i), 2); // matchingStacks
		}
		return allStacks;
	}

	@Override
	public ItemStack getResult() {
		return this.recipeOutput;
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
			String key = this.group.toLowerCase() + "_" + this.name.toLowerCase();
			while (key.indexOf(" ") != -1) {
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
		if (this.recipeItems.size() == 0 || this.recipeOutput.isEmpty()) {
			return false;
		}
		for (Ingredient ing : this.recipeItems) {
			if (ing.getMatchingStacks().length != 0) {
				return true;
			}
		}
		return false;
	}

	public boolean matches(InventoryCrafting inv, World worldIn) {
		if (this.recipeItems.isEmpty() || (inv.getWidth() == 3 && !this.global)
				|| (inv.getWidth() == 4 && this.global)) {
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
		List<Ingredient> ings = new ArrayList<Ingredient>();
		for (Ingredient ing : this.recipeItems) {
			ings.add(ing);
		}

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

		return ings.size() == 0 && inputs.size() == 0;
	}

	@Override
	public boolean saves() {
		return this.savesRecipe;
	}

	@Override
	public void saves(boolean bo) {
		this.savesRecipe = bo;
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
	public void setIsGlobal(boolean bo) {
		this.global = bo;
	}

	@Override
	public void setKnown(boolean bo) {
		this.known = bo;
	}

	@Override
	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("ID", this.id);
		compound.setInteger("Width", this.recipeWidth);
		compound.setInteger("Height", this.recipeHeight);
		if (this.recipeOutput != null) {
			compound.setTag("Item", (NBTBase) this.recipeOutput.writeToNBT(new NBTTagCompound()));
		}
		compound.setTag("Materials", NBTTags.nbtIngredientList((NonNullList<Ingredient>) this.recipeItems));
		compound.setTag("Availability", (NBTBase) this.availability.writeToNBT(new NBTTagCompound()));
		compound.setString("Name", this.name);
		compound.setBoolean("Global", this.global);
		compound.setBoolean("IgnoreDamage", this.ignoreDamage);
		compound.setBoolean("IgnoreNBT", this.ignoreNBT);
		compound.setString("Group", this.group);
		compound.setBoolean("IsKnown", this.known);
		compound.setBoolean("IsShaped", false);
		return compound;
	}

	@Override
	public boolean equal(INpcRecipe recipe) {
		return recipe.getClass()==NpcShapelessRecipes.class && recipe.getNpcGroup().equals(this.group) && recipe.getName().equals(this.name) && ItemStack.areItemStacksEqualUsingNBTShareTag(recipe.getProduct(), this.recipeOutput);
	}

	@Override
	public ItemStack getProduct() { return this.recipeOutput; }

}
