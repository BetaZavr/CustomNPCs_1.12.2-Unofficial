package noppes.npcs.client.util;

import com.google.common.collect.Lists;
import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.List;

// Custom RecipeItemHelper
public class NPCRecipePicker {

    // Player inventory items: unique ID, total amount
    public final Int2IntMap itemToCount;
    private final IRecipe recipe;
    private final List<Ingredient> ingredients = Lists.newArrayList();
    private final int ingredientCount;
    private final int[] possessedIngredientStacks;
    private final int possessedIngredientStackCount;
    private final BitSet data;
    private final IntList path = new IntArrayList();

    public NPCRecipePicker(Int2IntMap itemCount, IRecipe iRecipe) {
        itemToCount = itemCount;
        recipe = iRecipe;
        ingredients.addAll(iRecipe.getIngredients());
        ingredients.removeIf((ingredient) -> ingredient == Ingredient.EMPTY);
        ingredientCount = this.ingredients.size();
        possessedIngredientStacks = this.getUniqueAvailIngredientItems();
        possessedIngredientStackCount = this.possessedIngredientStacks.length;
        data = new BitSet(this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + this.ingredientCount * this.possessedIngredientStackCount);
        for (int i = 0; i < this.ingredients.size(); ++i) {
            IntList intlist = this.ingredients.get(i).getValidItemStacksPacked();
            for (int j = 0; j < this.possessedIngredientStackCount; ++j) {
                if (intlist.contains(this.possessedIngredientStacks[j])) { this.data.set(this.getIndex(true, j, i)); }
            }
        }
    }

    public void tryTake(int countPos, int maximum) {
        int stackCount = this.itemToCount.get(countPos);
        if (stackCount >= maximum) {
            itemToCount.put(countPos, stackCount - maximum);
        }
    }

    private void increment(int pathInt, int amount) {
        this.itemToCount.put(pathInt, this.itemToCount.get(pathInt) + amount);
    }

    private int[] getUniqueAvailIngredientItems() {
        IntCollection intcollection = new IntAVLTreeSet();
        for (Ingredient ingredient : this.ingredients) {
            intcollection.addAll(ingredient.getValidItemStacksPacked());
        }
        IntIterator intiterator = intcollection.iterator();
        while (intiterator.hasNext()) {
            if (itemToCount.get(intiterator.nextInt()) <= 0) { intiterator.remove(); }
        }
        return intcollection.toIntArray();
    }

    private int getIndex(boolean isNow, int pathNowInt, int pathNextInt) {
        int k = isNow ? pathNowInt * this.ingredientCount + pathNextInt : pathNextInt * this.ingredientCount + pathNowInt;
        return this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + 2 * k;
    }

    public int getBiggestCraftableStack(int stackLimitCount) { // tryPickAll
        int i = 0;
        int min = this.getMinIngredientCount(stackLimitCount) + 1;
        while (true) {
            int count = (i + min) / 2;
            if (this.tryPick(count, null)) {
                if (min - i <= 1) {
                    if (count > 0) { tryPick(count, null); }
                    return count;
                }
                i = count;
            }
            else { min = count; }
        }
    }

    private int getMinIngredientCount(int stackLimitCount) {
        int count = Integer.MAX_VALUE;
        for (Ingredient ingredient : this.ingredients) {
            int max = stackLimitCount;
            // Parent code changed here:
            IntList intlist = ingredient.getValidItemStacksPacked();
            for (int i = 0; i < intlist.size(); i++) {
                if (!itemToCount.containsKey(intlist.get(i))) { continue; }
                ItemStack stack = ingredient.getMatchingStacks()[i];
                max = Math.max(max, itemToCount.get(intlist.get(i)) / stack.getCount());
                if (stack.getMaxStackSize() < stackLimitCount) {
                    stackLimitCount = stack.getMaxStackSize();
                    max = stackLimitCount;
                    i = -1;
                }
                else if (stack.getCount() > 1) {
                    int maxStack = Math.max(1, (int) Math.floor((double) stack.getMaxStackSize() / (double) stack.getCount()));
                    if (max > maxStack) { max = maxStack; }
                }
            }
            if (count > 0) { count = Math.min(count, max); }
        }
        return count;
    }

    public boolean tryPick(int maximum, @Nullable IntList listIn) {
        if (maximum <= 0) { return true; }
        int k;
        for (k = 0; this.dfs(maximum); ++k) {
            this.tryTake(this.possessedIngredientStacks[this.path.getInt(0)], maximum);
            int l = this.path.size() - 1;
            this.setSatisfied(this.path.getInt(l));
            for (int i1 = 0; i1 < l; ++i1) { this.toggleResidual((i1 & 1) == 0, this.path.get(i1), this.path.get(i1 + 1)); }
            this.path.clear();
            this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount);
        }
        boolean flag = k == this.ingredientCount;
        boolean flag1 = flag && listIn != null;
        if (flag1) { listIn.clear(); }
        this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount);
        int j1 = 0;
        List<Ingredient> list = this.recipe.getIngredients();
        for (Ingredient ingredient : list) {
            if (flag1 && ingredient == Ingredient.EMPTY) { listIn.add(0); }
            else {
                for (int l1 = 0; l1 < this.possessedIngredientStackCount; ++l1) {
                    if (this.hasResidual(false, j1, l1)) {
                        this.toggleResidual(true, l1, j1);
                        increment(this.possessedIngredientStacks[l1], maximum);
                        if (flag1) {
                            listIn.add(this.possessedIngredientStacks[l1]);
                        }
                    }
                }
                ++j1;
            }
        }
        return flag;
    }

    private boolean dfs(int minCount) {
        int k = this.possessedIngredientStackCount;
        for (int l = 0; l < k; ++l) {
            if (itemToCount.get(this.possessedIngredientStacks[l]) >= minCount) {
                this.visit(false, l);
                while (!this.path.isEmpty()) {
                    int i1 = this.path.size();
                    boolean notIngredientCount = (i1 & 1) == 1;
                    int j1 = this.path.getInt(i1 - 1);
                    if (!notIngredientCount && !this.isSatisfied(j1)) { break; }
                    int k1 = notIngredientCount ? this.ingredientCount : k;
                    for (int l1 = 0; l1 < k1; ++l1) {
                        if (!this.hasVisited(notIngredientCount, l1) && this.hasConnection(notIngredientCount, j1, l1) && this.hasResidual(notIngredientCount, j1, l1)) {
                            this.visit(notIngredientCount, l1);
                            break;
                        }
                    }
                    int i2 = this.path.size();
                    if (i2 == i1) { this.path.removeInt(i2 - 1); }
                }
                if (!this.path.isEmpty()) { return true; }
            }
        }
        return false;
    }

    private void visit(boolean notIngredientCount, int pathInt) {
        this.data.set(this.getVisitedIndex(notIngredientCount, pathInt));
        this.path.add(pathInt);
    }

    private int getVisitedIndex(boolean notIngredientCount, int pathInt) {
        return (notIngredientCount ? 0 : this.ingredientCount) + pathInt;
    }

    private boolean hasVisited(boolean notIngredientCount, int pathInt) {
        return this.data.get(this.getVisitedIndex(notIngredientCount, pathInt));
    }

    private boolean hasConnection(boolean isNow, int pathNowInt, int pathInt) {
        return this.data.get(this.getIndex(isNow, pathNowInt, pathInt));
    }

    private boolean hasResidual(boolean isNow, int pathNowInt, int pathInt) {
        return isNow != this.data.get(1 + this.getIndex(isNow, pathNowInt, pathInt));
    }

    private boolean isSatisfied(int pathInt) {
        return this.data.get(this.getSatisfiedIndex(pathInt));
    }

    private int getSatisfiedIndex(int pathInt) {
        return this.ingredientCount + this.possessedIngredientStackCount + pathInt;
    }

    private void setSatisfied(int pathInt) {
        this.data.set(this.getSatisfiedIndex(pathInt));
    }

    private void toggleResidual(boolean isNow, int pathNowInt, int pathNextInt) {
        this.data.flip(1 + this.getIndex(isNow, pathNowInt, pathNextInt));
    }

}
