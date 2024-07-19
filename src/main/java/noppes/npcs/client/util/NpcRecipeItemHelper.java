package noppes.npcs.client.util;

import java.util.BitSet;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.Lists;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import net.minecraft.client.util.RecipeItemHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;

public class NpcRecipeItemHelper extends RecipeItemHelper {

	class RecipePicker {
		private final IRecipe recipe;
		private final List<Ingredient> ingredients = Lists.newArrayList();
		private final int ingredientCount;
		private final int[] possessedIngredientStacks;
		private final int possessedIngredientStackCount;
		private final BitSet data;
		private final IntList path = new IntArrayList();

		public RecipePicker(IRecipe p_i47608_2_) {
			this.recipe = p_i47608_2_;
			this.ingredients.addAll(p_i47608_2_.getIngredients());
			this.ingredients.removeIf((p_194103_0_) -> p_194103_0_ == Ingredient.EMPTY);
			this.ingredientCount = this.ingredients.size();
			this.possessedIngredientStacks = this.getUniqueAvailIngredientItems();
			this.possessedIngredientStackCount = this.possessedIngredientStacks.length;
			this.data = new BitSet(this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount
					+ this.ingredientCount * this.possessedIngredientStackCount);

			for (int i = 0; i < this.ingredients.size(); ++i) {
				IntList intlist = this.ingredients.get(i).getValidItemStacksPacked();
				for (int j = 0; j < this.possessedIngredientStackCount; ++j) {
					if (intlist.contains(this.possessedIngredientStacks[j])) {
						this.data.set(this.getIndex(true, j, i));
					}
				}
			}
		}

		private boolean dfs(int p_194098_1_) {
			int k = this.possessedIngredientStackCount;

			for (int l = 0; l < k; ++l) {
				if (NpcRecipeItemHelper.this.itemToCount.get(this.possessedIngredientStacks[l]) >= p_194098_1_) {
					this.visit(false, l);

					while (!this.path.isEmpty()) {
						int i1 = this.path.size();
						boolean flag = (i1 & 1) == 1;
						int j1 = this.path.getInt(i1 - 1);

						if (!flag && !this.isSatisfied(j1)) {
							break;
						}

						int k1 = flag ? this.ingredientCount : k;

						for (int l1 = 0; l1 < k1; ++l1) {
							if (!this.hasVisited(flag, l1) && this.hasConnection(flag, j1, l1)
									&& this.hasResidual(flag, j1, l1)) {
								this.visit(flag, l1);
								break;
							}
						}

						int i2 = this.path.size();

						if (i2 == i1) {
							this.path.removeInt(i2 - 1);
						}
					}

					if (!this.path.isEmpty()) {
						return true;
					}
				}
			}

			return false;
		}

		private int getIndex(boolean p_194095_1_, int p_194095_2_, int p_194095_3_) {
			int k = p_194095_1_ ? p_194095_2_ * this.ingredientCount + p_194095_3_
					: p_194095_3_ * this.ingredientCount + p_194095_2_;
			return this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount + 2 * k;
		}

		private int getMinIngredientCount() {
			int k = Integer.MAX_VALUE;

			for (Ingredient ingredient : this.ingredients) {
				int l = 0;
				int i1;

				for (IntListIterator intlistiterator = ingredient.getValidItemStacksPacked().iterator(); intlistiterator
						.hasNext(); l = Math.max(l, NpcRecipeItemHelper.this.itemToCount.get(i1))) {
					i1 = intlistiterator.next();
				}

				if (k > 0) {
					k = Math.min(k, l);
				}
			}
			return k;
		}

		private int getSatisfiedIndex(int p_194094_1_) {
			return this.ingredientCount + this.possessedIngredientStackCount + p_194094_1_;
		}

		private int[] getUniqueAvailIngredientItems() {
			IntCollection intcollection = new IntAVLTreeSet();

			for (Ingredient ingredient : this.ingredients) {
				intcollection.addAll(ingredient.getValidItemStacksPacked());
			}

			IntIterator intiterator = intcollection.iterator();

			while (intiterator.hasNext()) {
				if (!NpcRecipeItemHelper.this.containsItem(intiterator.nextInt())) {
					intiterator.remove();
				}
			}

			return intcollection.toIntArray();
		}

		private int getVisitedIndex(boolean p_194099_1_, int p_194099_2_) {
			return (p_194099_1_ ? 0 : this.ingredientCount) + p_194099_2_;
		}

		private boolean hasConnection(boolean p_194093_1_, int p_194093_2_, int p_194093_3_) {
			return this.data.get(this.getIndex(p_194093_1_, p_194093_2_, p_194093_3_));
		}

		private boolean hasResidual(boolean p_194100_1_, int p_194100_2_, int p_194100_3_) {
			return p_194100_1_ != this.data.get(1 + this.getIndex(p_194100_1_, p_194100_2_, p_194100_3_));
		}

		private boolean hasVisited(boolean p_194101_1_, int p_194101_2_) {
			return this.data.get(this.getVisitedIndex(p_194101_1_, p_194101_2_));
		}

		private boolean isSatisfied(int p_194091_1_) {
			return this.data.get(this.getSatisfiedIndex(p_194091_1_));
		}

		private void setSatisfied(int p_194096_1_) {
			this.data.set(this.getSatisfiedIndex(p_194096_1_));
		}

		private void toggleResidual(boolean p_194089_1_, int p_194089_2_, int p_194089_3_) {
			this.data.flip(1 + this.getIndex(p_194089_1_, p_194089_2_, p_194089_3_));
		}

		public boolean tryPick(int p_194092_1_, @Nullable IntList itemIDs) {
			if (p_194092_1_ <= 0) {
				return true;
			} else {
				int k;

				for (k = 0; this.dfs(p_194092_1_); ++k) {
					NpcRecipeItemHelper.this.tryTake(this.possessedIngredientStacks[this.path.getInt(0)], p_194092_1_);
					int l = this.path.size() - 1;
					this.setSatisfied(this.path.getInt(l));

					for (int i1 = 0; i1 < l; ++i1) {
						this.toggleResidual((i1 & 1) == 0, this.path.get(i1), this.path.get(i1 + 1));
					}

					this.path.clear();
					this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount);
				}

				boolean flag = k == this.ingredientCount;
				boolean flag1 = flag && itemIDs != null;

				if (flag1) {
					itemIDs.clear();
				}

				this.data.clear(0, this.ingredientCount + this.possessedIngredientStackCount + this.ingredientCount);
				int j1 = 0;
				List<Ingredient> list = this.recipe.getIngredients();

                for (Ingredient ingredient : list) {
                    if (flag1 && ingredient == Ingredient.EMPTY) {
                        itemIDs.add(0);
                    } else {
                        for (int l1 = 0; l1 < this.possessedIngredientStackCount; ++l1) {
                            if (this.hasResidual(false, j1, l1)) {
                                this.toggleResidual(true, l1, j1);
                                NpcRecipeItemHelper.this.increment(this.possessedIngredientStacks[l1], p_194092_1_);

                                if (flag1) {
                                    itemIDs.add(this.possessedIngredientStacks[l1]);
                                }
                            }
                        }

                        ++j1;
                    }
                }

				return flag;
			}
		}

		public int tryPickAll(int p_194102_1_, @Nullable IntList list) {
			int k = 0;
			int l = Math.min(p_194102_1_, this.getMinIngredientCount()) + 1;

			while (true) {
				int i1 = (k + l) / 2;

				if (this.tryPick(i1, null)) {
					if (l - k <= 1) {
						if (i1 > 0) {
							this.tryPick(i1, list);
						}

						return i1;
					}

					k = i1;
				} else {
					l = i1;
				}
			}
		}

		private void visit(boolean p_194088_1_, int p_194088_2_) {
			this.data.set(this.getVisitedIndex(p_194088_1_, p_194088_2_));
			this.path.add(p_194088_2_);
		}
	}

	public static int pack(@Nonnull ItemStack stack) {
		Item item = stack.getItem();
		int i = item.getHasSubtypes() ? stack.getMetadata() : 0;
		return Item.REGISTRY.getIDForObject(item) << 16 | i & 65535;
	}

	/** Map from {@link #pack} packed ids to counts */
	public final Int2IntMap itemToCount = new Int2IntOpenHashMap();

	public void accountStack(@Nonnull ItemStack stack) {
		this.accountStack(stack, -1);
	}

	public void accountStack(@Nonnull ItemStack stack, int forceCount) {
		if (!stack.isEmpty() && !stack.isItemDamaged() && !stack.isItemEnchanted() && !stack.hasDisplayName()) {
			int i = pack(stack);
			int j = forceCount == -1 ? stack.getCount() : forceCount;
			this.increment(i, j);
		}
	}

	public boolean canCraft(@Nonnull IRecipe recipe, @Nullable IntList itemIDs) {
		return this.canCraft(recipe, itemIDs, 1);
	}

	public boolean canCraft(@Nonnull IRecipe recipe, @Nullable IntList itemIDs, int p_194118_3_) {
		return (new NpcRecipeItemHelper.RecipePicker(recipe)).tryPick(p_194118_3_, itemIDs);
	}

	public void clear() {
		this.itemToCount.clear();
	}

	public boolean containsItem(int p_194120_1_) {
		return this.itemToCount.get(p_194120_1_) > 0;
	}

	public int getBiggestCraftableStack(@Nonnull IRecipe recipe, int p_194121_2_, @Nullable IntList p_194121_3_) {
		return (new NpcRecipeItemHelper.RecipePicker(recipe)).tryPickAll(p_194121_2_, p_194121_3_);
	}

	public int getBiggestCraftableStack(@Nonnull IRecipe recipe, @Nullable IntList p_194114_2_) {
		return this.getBiggestCraftableStack(recipe, Integer.MAX_VALUE, p_194114_2_);
	}

	private void increment(int p_194117_1_, int amount) {
		this.itemToCount.put(p_194117_1_, this.itemToCount.get(p_194117_1_) + amount);
	}

	public int tryTake(int p_194122_1_, int maximum) {
		int i = this.itemToCount.get(p_194122_1_);

		if (i >= maximum) {
			this.itemToCount.put(p_194122_1_, i - maximum);
			return p_194122_1_;
		} else {
			return 0;
		}
	}

}
