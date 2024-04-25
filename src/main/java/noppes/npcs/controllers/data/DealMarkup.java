package noppes.npcs.controllers.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;

public class DealMarkup {

	public ItemStack main = ItemStack.EMPTY;
	public int count = 0;
	public final Map<ItemStack, Integer> baseItems = Maps.<ItemStack, Integer>newLinkedHashMap(); // base ItemStacks
	public Map<ItemStack, Integer> buyItems = Maps.<ItemStack, Integer>newLinkedHashMap(); // ItemStacks required for
																							// purchase
	public Map<ItemStack, Integer> sellItems = Maps.<ItemStack, Integer>newLinkedHashMap(); // ItemStacks received upon
																							// sale
	public final Map<ItemStack, Boolean> baseHasPlayerItems = Maps.<ItemStack, Boolean>newLinkedHashMap(); // base
																											// ItemStacks
	public Map<ItemStack, Boolean> buyHasPlayerItems = Maps.<ItemStack, Boolean>newLinkedHashMap(); // ItemStacks
																									// required for
																									// purchase
	public Map<ItemStack, Boolean> sellHasPlayerItems = Maps.<ItemStack, Boolean>newLinkedHashMap(); // ItemStacks
																										// received upon
																										// sale
	public long baseMoney = 0L, buyMoney = 0L, sellMoney = 0L;
	public boolean baseOneOfEach = true, buyOneOfEach = true, sellOneOfEach = true, ignoreNBT = false,
			ignoreDamage = false;
	public Deal deal = null;

	public void cheak(NonNullList<ItemStack> inventory) {
		this.baseHasPlayerItems.clear();
		this.buyHasPlayerItems.clear();
		this.sellHasPlayerItems.clear();
		for (ItemStack stack : this.baseItems.keySet()) {
			int count = 0;
			for (int i = 0; i < inventory.size(); ++i) {
				ItemStack s = inventory.get(i);
				if (NoppesUtilServer.IsItemStackNull(s)) {
					continue;
				}
				if (NoppesUtilPlayer.compareItems(stack, s, this.ignoreDamage, this.ignoreNBT)) {
					count += s.getCount();
				}
			}
			this.baseHasPlayerItems.put(stack, count >= this.baseItems.get(stack));
			this.buyHasPlayerItems.put(stack, this.buyItems.containsKey(stack) && count >= this.buyItems.get(stack));
			this.sellHasPlayerItems.put(stack, this.sellItems.containsKey(stack) && count >= this.sellItems.get(stack));
		}
		this.buyOneOfEach = true;
		for (int count : this.buyItems.values()) {
			if (count > 1) {
				this.buyOneOfEach = false;
				break;
			}
		}
		this.sellOneOfEach = true;
		for (int count : this.sellItems.values()) {
			if (count > 1) {
				this.sellOneOfEach = false;
				break;
			}
		}
	}

	public void reset(boolean isBuy, float coff) {
		if (!this.baseItems.isEmpty()) {
			for (ItemStack stack : this.baseItems.keySet()) {
				int count = (int) ((float) this.baseItems.get(stack) * coff);
				if (count <= 0) {
					count = 1;
				}
				if (isBuy) {
					this.buyItems.put(stack, count);
				} else {
					this.sellItems.put(stack, count);
				}
			}
		}
		for (ItemStack stack : this.buyItems.keySet()) {
			if (this.buyItems.get(stack) > 1) {
				if (isBuy) {
					this.buyOneOfEach = false;
				} else {
					this.sellOneOfEach = false;
				}
				break;
			}
		}
		if (isBuy) {
			this.buyMoney = (long) ((float) this.baseMoney * coff);
		} else {
			this.sellMoney = (long) ((float) this.baseMoney * coff);
		}
		if (this.buyMoney <= 0 && this.baseMoney > 0) {
			this.buyMoney = 1;
		}
		if (this.sellMoney <= 0 && this.baseMoney > 0) {
			this.sellMoney = 1;
		}
	}

	public void set(Deal deal) {
		this.deal = deal;
		this.main = deal.getProduct().getMCItemStack().copy();
		this.count = deal.getProduct().getStackSize();
		this.ignoreNBT = deal.getIgnoreNBT();
		this.ignoreDamage = deal.getIgnoreDamage();
		this.baseMoney = deal.getMoney();
		this.set(deal.getMCInventoryCurrency());
	}

	public void set(IInventory inventory) {
		this.baseItems.clear();
		if (inventory == null || !inventory.isEmpty()) {
			Map<ItemStack, Integer> map = Maps.<ItemStack, Integer>newHashMap();
			for (int slot = 0; slot < inventory.getSizeInventory(); slot++) {
				ItemStack stack = inventory.getStackInSlot(slot);
				if (NoppesUtilServer.IsItemStackNull(stack)) {
					continue;
				}
				boolean has = false;
				for (ItemStack s : map.keySet()) {
					if (NoppesUtilPlayer.compareItems(stack, s, this.ignoreDamage, this.ignoreNBT)) {
						has = true;
						map.put(s, map.get(s) + stack.getCount());
						this.baseOneOfEach = false;
						break;
					}
				}
				if (!has) {
					map.put(stack, stack.getCount());
					if (stack.getCount() > 1) {
						this.baseOneOfEach = false;
					}
				}
			}
			List<Entry<ItemStack, Integer>> list = Lists.newArrayList(map.entrySet());
			Collections.sort(list, new Comparator<Entry<ItemStack, Integer>>() {
				public int compare(Entry<ItemStack, Integer> st_0, Entry<ItemStack, Integer> st_1) {
					return ((Integer) st_1.getValue()).compareTo((Integer) st_0.getValue());
				}
			});
			for (Entry<ItemStack, Integer> entry : list) {
				this.baseItems.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public void set(MarkupData md) {
		if (md == null) {
			return;
		}
		this.buyItems.clear();
		this.sellItems.clear();
		this.buyMoney = this.baseMoney;
		this.sellMoney = this.baseMoney;
		if (md.buy != 0.0f) {
			this.reset(true, (100.0f + md.buy * 100.0f) / 100.0f);
		} else {
			for (ItemStack stack : this.baseItems.keySet()) {
				this.buyItems.put(stack, this.baseItems.get(stack));
			}
		}
		if (md.sell != 0.0f) {
			this.reset(false, (100.0f + md.sell * -100.0f) / 100.0f);
		} else {
			for (ItemStack stack : this.baseItems.keySet()) {
				this.sellItems.put(stack, this.baseItems.get(stack));
			}
		}
		if (!this.sellItems.isEmpty() && this.sellItems.size() <= this.baseItems.size() && this.sellMoney > 0) {
			int bc = 0, sc = 0;
			for (int c : this.baseItems.values()) {
				bc += c;
			}
			for (int c : this.sellItems.values()) {
				sc += c;
			}
			float coff = 1.0f;
			if (md.sell != 0.0f) {
				coff = (100.0f + md.sell * -100.0f) / 100.0f;
			}
			if (sc <= this.sellItems.size() || bc * coff < sc) {
				this.sellItems.clear();
			}
		}
	}

}
