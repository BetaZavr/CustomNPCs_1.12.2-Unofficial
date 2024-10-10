package noppes.npcs.api.wrapper;

import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
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
    public int id = -1;
    public int width = 3;
    public int height = 3;
    public String group = "";
    public String name = "";
    public String domen = "minecraft";
    public final Map<Integer, ItemStack[]> recipeItems = new TreeMap<>();
    public ItemStack product = new ItemStack(Blocks.COBBLESTONE);
    public final Availability availability = new Availability();
    public IRecipe parent = null;

    public WrapperRecipe() {
        recipeItems.clear();
        recipeItems.put(0, new ItemStack[]{ new ItemStack(Blocks.COBBLESTONE) });
    }

    public void clear() {
        global = true;
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
    }

    public NBTTagCompound getNbt() {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setInteger("ID", id);
        compound.setInteger("Width", width);
        compound.setInteger("Height", height);
        if (product != null) { compound.setTag("Item", product.writeToNBT(new NBTTagCompound())); }
        // NBTTags.nbtIngredientList(recipeItems)
        NBTTagList nbttaglist = new NBTTagList();
        for (int slot : recipeItems.keySet()) {
            NBTTagCompound nbttagcompound = new NBTTagCompound();
            nbttagcompound.setByte("Slot", (byte) slot);
            NBTTagList ingredients = new NBTTagList();
            ItemStack[] ings = recipeItems.get(slot);
            for (ItemStack ing : ings) { ingredients.appendTag(ing.writeToNBT(new NBTTagCompound())); }
            nbttagcompound.setTag("Ingredients", ingredients);
            nbttaglist.appendTag(nbttagcompound);
        }
        compound.setTag("Materials", nbttaglist);
        compound.setTag("Availability", availability.writeToNBT(new NBTTagCompound()));
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
        id = wrapper.id;
        width = wrapper.width;
        height = wrapper.height;
        group = wrapper.group;
        name = wrapper.name;
        domen = wrapper.domen;
        recipeItems.clear();
        recipeItems.putAll(wrapper.recipeItems);
        product = wrapper.product;
        availability.readFromNBT(wrapper.availability.writeToNBT(new NBTTagCompound()));
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
        id = recipeId;
        width = 3;
        height = 3;
        group = ((char) 167) + "7" + recipe.getGroup();
        name = ((char) 167) + "7" + location.getResourcePath();

        recipeItems.clear();
        int pos = 0;
        for (Ingredient ingr : recipe.getIngredients()) {
            ItemStack[] rawMatchingStacks = ingr.getMatchingStacks();
            ItemStack[] array = new ItemStack[rawMatchingStacks.length];
            for (int j = 0; j < rawMatchingStacks.length; j++) {
                array[j] = rawMatchingStacks[j].copy();
            }
            recipeItems.put(pos, array);
            pos ++;
        }
        product = recipe.getRecipeOutput();
        availability.clear();
    }

}
