package noppes.npcs.items.crafting;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

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
import noppes.npcs.reflection.item.crafting.ShapedRecipesReflection;
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

	public Object[] getGrid() {
		int startW = -1;
		int startH = -1;
		int maxW = 0;
		int maxH = 0;
		Map<Integer, Ingredient> map = new TreeMap<>();
		for (int i = 0; i < recipeItems.size(); i++) {
			Ingredient ingredient = recipeItems.get(i);
			if (ingredient.getMatchingStacks().length > 0) {
				boolean hasStack = false;
				for (ItemStack stack : ingredient.getMatchingStacks()) {
					if (!NoppesUtilServer.IsItemStackNull(stack)) {
						hasStack = true;
						break;
					}
				}
				if (hasStack) {
					int iW = i % recipeWidth;
					int iH = i / recipeHeight;
					if (startW == -1) {
						startW = iW;
						startH = iH;
					}
					map.put(i, ingredient);
					if (maxW < iW - startW + 1) { maxW = iW - startW + 1; }
					if (maxH < iH - startH + 1) { maxH = iH - startH + 1; }
				}
			}
		}
		NonNullList<Ingredient> newIngredient = recipeItems;
		if (startW != -1 && maxW != 0 && maxH != 0 && (recipeWidth != maxW || recipeHeight != maxH)) {
			newIngredient = NonNullList.create();
			for (int y = 0; y < maxH; y++) {
				for (int x = 0; x < maxW; x++) {
					int slotIndex = (y + startH) * recipeWidth + (x + startW);
                    newIngredient.add(map.getOrDefault(slotIndex, Ingredient.EMPTY));
				}
			}
		} else {
			maxW = recipeWidth;
			maxH = recipeHeight;
		}
		return new Object[] { maxW, maxH, newIngredient };
	}

	/** How many horizontal slots this recipe is wide. */
	public int recipeWidth;
	/** How many vertical slots this recipe uses. */
	public int recipeHeight;
	/** Is array of ItemStack that composes the recipe. */
	public NonNullList<Ingredient> recipeItems;
	/** Is the ItemStack that you get when craft the recipe. */
	private ItemStack recipeOutput;
	public Availability availability;
	private boolean global;
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
        if (stack == null || ingredient == null) { return false; }
		ItemStack[] stacks = ingredient.getMatchingStacks();
		if (stacks.length == 0 && stack.isEmpty()) { return true; }
		for (ItemStack ingStack : stacks) {
			if (ingStack.getItem() != stack.getItem() || ingStack.isEmpty() || stack.isEmpty()) { continue; }
			if (NoppesUtilPlayer.compareItems(stack, ingStack, this.ignoreDamage, this.ignoreNBT) && ingStack.getCount() <= stack.getCount()) {
				return true;
			}
		}
        return false;
    }

	@Override
	public boolean canFit(int width, int height) {
		if (!global && (width != 4 || height != 4)) { return false; }
		Object[] objs = getGrid();
		int recipeW = (int) objs[0];
		int recipeH = (int) objs[1];
		return width >= recipeW && height >= recipeH;
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
		ShapedRecipesReflection.setGroup(this, recipe.getNpcGroup());
		this.known = recipe.isKnown();
		this.recipeWidth = recipe.getWidthRecipe();
		this.recipeHeight = recipe.getHeightRecipe();
		int w = this.global ? 3 : 4;
		if (recipeWidth > w) { recipeWidth = w; }
		if (recipeHeight > w) { recipeHeight = w; }

		if (this.getRegistryName() == null) {
			this.setRegistryName(new ResourceLocation(CustomNpcs.MODID, this.getGroup() + "_" + this.name));
		}
		this.savesRecipe = true;
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

	public int getHeightRecipe() {
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

	public int getWidthRecipe() {
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
	public void setRecipeOutput(ItemStack cms) {
		if (cms == null || cms.isEmpty()) { return; }
		recipeOutput = cms.copy();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean matches(@Nonnull InventoryCrafting inv, @Nullable World world) {
		if (recipeItems.isEmpty() || (!global && (inv.getWidth() != 4) || (inv.getHeight() != 4))) {
			return false;
		}
		int startW = -1, startH = -1;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			if (!inv.getStackInSlot(i).isEmpty()) {
				startW = i % inv.getWidth();
				startH = i / inv.getHeight();
				break;
			}
		}
		Object[] objs = getGrid();
		int recipeW = (int) objs[0];
		int recipeH = (int) objs[1];
		NonNullList<Ingredient> ingredients = (NonNullList<Ingredient>) objs[2];
		for (int r = 0; r < 2; ++r) {
			int ings = recipeW * recipeH;
			for (int h = 0; h <= inv.getHeight() - recipeH; ++h) {
				for (int w = 0; w <= inv.getWidth() - recipeW; ++w) {
					int index = h * recipeW + (r == 1 ? recipeW - w - 1 : w);
					if (index >= ingredients.size()) {
						continue;
					}
					int slotIndex = (h + startH) * inv.getWidth() + (w + startW);
					Ingredient ingredient = ingredients.get(index);
					if (apply(ingredient, inv.getStackInSlot(slotIndex))) { ings--; }
				}
			}
			if (ings == 0) { return true; }
		}
		return false;
	}

	public void setIgnoreDamage(boolean bo) {
		ignoreDamage = bo;
		savesRecipe = true;
	}

	public void setIgnoreNBT(boolean bo) {
		ignoreNBT = bo;
		savesRecipe = true;
	}

	@Override
	public void setKnown(boolean bo) {
		known = bo;
		savesRecipe = true;
	}

	@Override
	public void setNbt(INbt nbt) { copy(NpcShapedRecipes.read(nbt.getMCNBT())); }

}
