package noppes.npcs.api.wrapper;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.crafting.IShapedRecipe;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.data.Availability;

import java.util.Map;
import java.util.TreeMap;

public class WrapperRecipe {

    public boolean global = true;
    public boolean known = false;
    public boolean ignoreDamage = false;
    public boolean ignoreNBT = false;
    public boolean isShaped = true;
    public boolean main = false;
    public int id = -1;
    public int width = 3;
    public int height = 3;
    public String group = "";
    public String name = "";
    public String domen = "minecraft";
    // recipeItems -> ItemStack[].length == 0 ... 16 max (not null)
    public final Map<Integer, ItemStack[]> recipeItems = new TreeMap<>();
    public ItemStack product = new ItemStack(Blocks.COBBLESTONE);
    public final Availability availability = new Availability();
    public IRecipe parent = null;

    public WrapperRecipe() {
        recipeItems.clear();
        recipeItems.put(0, new ItemStack[] { new ItemStack(Blocks.COBBLESTONE) });
    }

    public void clear() {
        known = false;
        ignoreDamage = false;
        ignoreNBT = false;
        isShaped = true;
        id = -1;
        width = 3;
        height = 3;
        group = "";
        name = "";
        recipeItems.clear();
        recipeItems.put(0, new ItemStack[]{ new ItemStack(Blocks.COBBLESTONE) });
        product = new ItemStack(Blocks.COBBLESTONE);
        availability.clear();
        main = false;
    }

    public NBTTagCompound getNbt() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("ID", id);
        compound.setInteger("Width", width);
        compound.setInteger("Height", height);
        if (product != null) { compound.setTag("Item", product.writeToNBT(new NBTTagCompound())); }
        // NBTTags.nbtIngredientList(recipeItems)
        NBTTagList nbttaglist = new NBTTagList();
        for (int slot = 0; slot < (global ? 9 : 16); slot++) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte) slot);
            NBTTagList ingredients = new NBTTagList();
            if (recipeItems.get(slot) != null) {
                for (ItemStack ing : recipeItems.get(slot)) {
                    ingredients.appendTag(ing.writeToNBT(new NBTTagCompound()));
                }
            }
            nbttagcompound.setTag("Ingredients", ingredients);
            nbttaglist.appendTag(nbttagcompound);
        }

        compound.setTag("Materials", nbttaglist);
        compound.setTag("Availability", availability.save(new NBTTagCompound()));
        compound.setString("Name", name);
        compound.setString("Domen", domen);
        compound.setBoolean("Global", global);
        compound.setBoolean("IgnoreDamage", ignoreDamage);
        compound.setBoolean("IgnoreNBT", ignoreNBT);
        compound.setString("Group", group);
        compound.setBoolean("IsKnown", known);
        compound.setBoolean("IsShaped", isShaped);
        return compound;
    }

    public boolean isValid() {
        boolean hasStack = false;
        for (ItemStack[] array : recipeItems.values()) {
            if (array == null) { continue; }
            for (ItemStack stack : array) {
                if (stack != null && !stack.isEmpty()) {
                    hasStack = true;
                    break;
                }
            }
            if (hasStack) { break; }
        }
        return hasStack && (domen.equals("minecraft") || !group.isEmpty() && !name.isEmpty());
    }

    public void copyFrom(WrapperRecipe wrapper) {
        global = wrapper.global;
        known = wrapper.known;
        ignoreDamage = wrapper.ignoreDamage;
        ignoreNBT = wrapper.ignoreNBT;
        isShaped = wrapper.isShaped;
        main = wrapper.main;
        id = wrapper.id;
        width = wrapper.width;
        height = wrapper.height;
        group = wrapper.group;
        name = wrapper.name;
        domen = wrapper.domen;
        recipeItems.clear();
        recipeItems.putAll(wrapper.recipeItems);
        product = wrapper.product;
        availability.load(wrapper.availability.save(new NBTTagCompound()));
        parent = wrapper.parent;
    }

    public void copyFrom(IRecipe recipe, int recipeId) {
        if (recipe instanceof INpcRecipe) {
            copyFrom(((INpcRecipe) recipe).getWrapperRecipe());
            id = recipeId;
            return;
        }
        clear();
        ResourceLocation location = recipe.getRegistryName();
        if (location == null) { location = new ResourceLocation("default"); }
        parent = recipe;
        global = true;
        known = false;
        ignoreDamage = false;
        ignoreNBT = false;
        isShaped = recipe instanceof ShapedRecipes;
        main = true;
        id = recipeId;

        int pos = 0;
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        if (recipe instanceof IShapedRecipe) {
            width = ((IShapedRecipe) recipe).getRecipeWidth();
            height = ((IShapedRecipe) recipe).getRecipeHeight();
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    ItemStack[] rawMatchingStacks = ingredients.get(index).getMatchingStacks();
                    ItemStack[] array = new ItemStack[rawMatchingStacks.length];
                    for (int j = 0; j < rawMatchingStacks.length; j++) {
                        array[j] = rawMatchingStacks[j].copy();
                    }
                    int slotIndex = y * 3 + x;
                    recipeItems.put(slotIndex, array);
                }
            }
            for (int i = 0; i < 9; i++) {
                if (recipeItems.containsKey(i)) { continue; }
                recipeItems.put(i, new ItemStack[0]);
            }
        } else {
            for (Ingredient ingr : ingredients) {
                ItemStack[] rawMatchingStacks = ingr.getMatchingStacks();
                ItemStack[] array = new ItemStack[rawMatchingStacks.length];
                for (int j = 0; j < rawMatchingStacks.length; j++) {
                    array[j] = rawMatchingStacks[j].copy();
                }
                recipeItems.put(pos, array);
                pos ++;
            }
        }
        width = 3;
        height = 3;
        group = ((char) 167) + "7" + recipe.getGroup();
        name = ((char) 167) + "7" + location.getResourcePath();
        product = recipe.getRecipeOutput();
        availability.clear();
        if (product != null && !product.isEmpty() && product.getItem().getRegistryName() != null) {
            domen = product.getItem().getRegistryName().getResourceDomain();
        }
    }

    public String getName() { return name; }

}
