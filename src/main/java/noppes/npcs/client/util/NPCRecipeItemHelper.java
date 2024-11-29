package noppes.npcs.client.util;

import it.unimi.dsi.fastutil.ints.*;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import noppes.npcs.items.crafting.NpcShapedRecipes;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class NPCRecipeItemHelper extends RecipeItemHelper {

    public final Int2IntMap itemToCount = new Int2IntOpenHashMap();

    public void accountStack(@Nonnull ItemStack stack)
    {
        this.accountStack(stack, -1);
    }

    public void accountStack(ItemStack stack, int forceCount)
    {
        if (!stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName())
        {
            int i = pack(stack);
            int j = forceCount == -1 ? stack.getCount() : forceCount;
            this.increment(i, j);
        }
    }

    public static int pack(ItemStack stack) {
        Item item = stack.getItem();
        int i = item.getHasSubtypes() ? stack.getMetadata() : 0;
        return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
    }

    public boolean containsItem(int pos) {
        return this.itemToCount.get(pos) > 0;
    }

    public int tryTake(int countPos, int maximum) {
        int stackPos = this.itemToCount.get(countPos);
        if (stackPos >= maximum) {
            itemToCount.put(countPos, stackPos - maximum);
            return countPos;
        }
        return 0;
    }

    private void increment(int pathInt, int amount) {
        this.itemToCount.put(pathInt, this.itemToCount.get(pathInt) + amount);
    }

    public boolean canCraft(@Nonnull IRecipe recipe, @Nullable IntList list) {
        return this.canCraft(recipe, list, 1);
    }

    public boolean canCraft(@Nonnull IRecipe recipe, @Nullable IntList list, int pos) {
        return (new NPCRecipeItemHelper.NPCRecipePicker(recipe)).tryPick(pos, list);
    }

    public int getBiggestCraftableStack(@Nonnull IRecipe recipe, @Nullable IntList list) {
        return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, list);
    }

    public int getBiggestCraftableStack(@Nonnull IRecipe recipe, int pos, @Nullable IntList list) {
        return (new NPCRecipeItemHelper.NPCRecipePicker(recipe)).tryPickAll(pos, list);
    }

    public static @Nonnull ItemStack unpack(int pos) {
        return pos == 0 ? ItemStack.EMPTY : new ItemStack(Item.getItemById(pos >> 16 & 65535), 1, pos & 65535);
    }

    public void clear() {
        itemToCount.clear();
    }

    class NPCRecipePicker {

        private final IRecipe recipe;
        private final List<Ingredient> ingredients = new ArrayList<>();
        private final int ingredientCount;
        private final int[] possessedIngredientStacks;
        private final int possessedIngredientStackCount;
        private final BitSet data;
        private final IntList path = new IntArrayList();

        public NPCRecipePicker(IRecipe iRecipe) {
            this.recipe = iRecipe;
            this.ingredients.addAll(iRecipe.getIngredients());
            this.ingredients.removeIf((p_194103_0_) -> p_194103_0_ == Ingredient.EMPTY);
            this.ingredientCount = this.ingredients.size();
            this.possessedIngredientStacks = this.getUniqueAvailIngredientItems();
            this.possessedIngredientStackCount = this.possessedIngredientStacks.length;
            this.data = new BitSet(this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + this.ingredientCount * this.possessedIngredientStackCount);
            for (int i = 0; i < this.ingredients.size(); ++i) {
                IntList intlist = this.ingredients.get(i).getValidItemStacksPacked();
                for (int j = 0; j < this.possessedIngredientStackCount; ++j) {
                    if (intlist.contains(this.possessedIngredientStacks[j])) {
                        this.data.set(this.getIndex(true, j, i));
                    }
                }
            }
        }

        @SuppressWarnings("unchecked")
        public boolean tryPick(int maximum, @Nullable IntList listIn) {
            if (maximum <= 0) { return true; }
            int k;
            for (k = 0; this.dfs(maximum); ++k) {
                tryTake(this.possessedIngredientStacks[this.path.getInt(0)], maximum);
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
            List<Ingredient> list = recipe.getIngredients();
            if (recipe instanceof NpcShapedRecipes) {
                Object[] objs = ((NpcShapedRecipes) recipe).getGrid();
                list = (NonNullList<Ingredient>) objs[2];
            }
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

        private boolean isSatisfied(int pathInt) {
            return this.data.get(this.getSatisfiedIndex(pathInt));
        }

        private void setSatisfied(int pathInt) {
            this.data.set(this.getSatisfiedIndex(pathInt));
        }

        private int getSatisfiedIndex(int pathInt) {
            return this.ingredientCount + this.possessedIngredientStackCount + pathInt;
        }

        private boolean hasConnection(boolean isNow, int pathNowInt, int pathInt) {
            return this.data.get(this.getIndex(isNow, pathNowInt, pathInt));
        }

        private boolean hasResidual(boolean isNow, int pathNowInt, int pathInt) {
            return isNow != this.data.get(1 + this.getIndex(isNow, pathNowInt, pathInt));
        }

        private void toggleResidual(boolean isNow, int pathNowInt, int pathNextInt) {
            this.data.flip(1 + this.getIndex(isNow, pathNowInt, pathNextInt));
        }

        private int getIndex(boolean isNow, int pathNowInt, int pathNextInt) {
            int k = isNow ? pathNowInt * this.ingredientCount + pathNextInt : pathNextInt * this.ingredientCount + pathNowInt;
            return this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + 2 * k;
        }

        private void visit(boolean notIngredientCount, int pathInt) {
            this.data.set(this.getVisitedIndex(notIngredientCount, pathInt));
            this.path.add(pathInt);
        }

        private boolean hasVisited(boolean notIngredientCount, int pathInt) {
            return this.data.get(this.getVisitedIndex(notIngredientCount, pathInt));
        }

        private int getVisitedIndex(boolean notIngredientCount, int pathInt) {
            return (notIngredientCount ? 0 : this.ingredientCount) + pathInt;
        }

        public int tryPickAll(int stackLimitCount, @Nullable IntList list) {
            int i = 0;
            int min = getMinIngredientCount(stackLimitCount) + 1;
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

    }

}
