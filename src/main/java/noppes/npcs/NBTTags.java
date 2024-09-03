package noppes.npcs;

import java.util.*;

import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;

public class NBTTags {

	public static HashMap<Integer, Boolean> getBooleanList(NBTTagList tagList) {
		HashMap<Integer, Boolean> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getBoolean("Boolean"));
		}
		return list;
	}

	public static Map<Integer, IItemStack> getIItemStackMap(NBTTagList tagList) {
		Map<Integer, IItemStack> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			ItemStack item = new ItemStack(nbttagcompound);
			if (!item.isEmpty()) {
				try {
					list.put(nbttagcompound.getByte("Slot") & 0xFF, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
				} catch (ClassCastException e) {
					list.put(nbttagcompound.getInteger("Slot"), Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
				}
			}
		}
		return list;
	}

	public static NonNullList<Ingredient> getIngredientList(NBTTagList tagList) {
		NonNullList<Ingredient> list = NonNullList.create();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbtStack = tagList.getCompoundTagAt(i);
			list.add(nbtStack.getByte("Slot") & 0xFF,
					Ingredient.fromStacks(new ItemStack(nbtStack)));
		}
		return list;
	}

	public static ArrayList<int[]> getIntegerArraySet(NBTTagList tagList) {
		ArrayList<int[]> set = new ArrayList<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound compound = tagList.getCompoundTagAt(i);
			set.add(compound.getIntArray("Array"));
		}
		return set;
	}

	public static HashMap<Integer, Integer> getIntegerIntegerMap(NBTTagList tagList) {
		HashMap<Integer, Integer> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getInteger("Integer"));
		}
		return list;
	}

	public static List<Integer> getIntegerList(NBTTagList tagList) {
		List<Integer> list = new ArrayList<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.add(nbttagcompound.getInteger("Integer"));
		}
		return list;
	}

	public static HashMap<Integer, Long> getIntegerLongMap(NBTTagList tagList) {
		HashMap<Integer, Long> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getLong("Long"));
		}
		return list;
	}

	public static HashSet<Integer> getIntegerSet(NBTTagList tagList) {
		HashSet<Integer> list = new HashSet<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.add(nbttagcompound.getInteger("Integer"));
		}
		return list;
	}

	public static HashMap<Integer, String> getIntegerStringMap(NBTTagList tagList) {
		HashMap<Integer, String> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getInteger("Slot"), nbttagcompound.getString("Value"));
		}
		return list;
	}

	public static HashMap<ItemStack, Integer> getItemStackIntegerMap(NBTTagList tagList) {
		HashMap<ItemStack, Integer> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(new ItemStack(nbttagcompound.getCompoundTag("Item")), nbttagcompound.getInteger("Value"));
		}
		return list;
	}

	public static void getItemStackList(NBTTagList tagList, NonNullList<ItemStack> items) {
		items.clear();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbtStack = tagList.getCompoundTagAt(i);
			try {
				items.set(nbtStack.getByte("Slot") & 0xFF, new ItemStack(nbtStack));
			} catch (ClassCastException e) {
				items.set(nbtStack.getInteger("Slot"), new ItemStack(nbtStack));
			}
		}
	}

	public static TreeMap<Long, String> GetLongStringMap(NBTTagList tagList) {
		TreeMap<Long, String> list = new TreeMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getLong("Long"), nbttagcompound.getString("String"));
		}
		return list;
	}

	public static List<ScriptContainer> GetScript(NBTTagList list, IScriptHandler handler, boolean isClient) {
		List<ScriptContainer> scripts = new ArrayList<>();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			ScriptContainer script = new ScriptContainer(handler, isClient);
			script.readFromNBT(compound, isClient);
			scripts.add(script);
		}
		return scripts;
	}

	public static HashMap<String, Integer> getStringIntegerMap(NBTTagList tagList) {
		HashMap<String, Integer> list = new HashMap<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			list.put(nbttagcompound.getString("Slot"), nbttagcompound.getInteger("Value"));
		}
		return list;
	}

	public static List<String> getStringList(NBTTagList tagList) {
		List<String> list = new ArrayList<>();
		for (int i = 0; i < tagList.tagCount(); ++i) {
			NBTTagCompound nbttagcompound = tagList.getCompoundTagAt(i);
			String line = nbttagcompound.getString("Line");
			list.add(line);
		}
		return list;
	}

	public static NBTTagList nbtDoubleList(double... values) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int i = values.length, j = 0; j < i; ++j) {
			nbttaglist.appendTag(new NBTTagDouble(values[j]));
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIItemStackMap(Map<Integer, IItemStack> inventory) {
		NBTTagList nbttaglist = new NBTTagList();
		if (inventory == null) {
			return nbttaglist;
		}
		for (int slot : inventory.keySet()) {
			IItemStack item = inventory.get(slot);
			if (item == null) {
				continue;
			}
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setByte("Slot", (byte) slot);
			item.getMCItemStack().writeToNBT(nbttagcompound);
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIngredientList(NonNullList<Ingredient> inventory) {
		NBTTagList nbttaglist = new NBTTagList();
		if (inventory == null) {
			return nbttaglist;
		}
		for (int slot = 0; slot < inventory.size(); ++slot) {
			Ingredient ingredient = inventory.get(slot);
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setByte("Slot", (byte) slot);
			if (ingredient.getMatchingStacks().length > 0) {
				ingredient.getMatchingStacks()[0].writeToNBT(nbttagcompound);
			}
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIntegerArraySet(List<int[]> set) {
		NBTTagList nbttaglist = new NBTTagList();
		if (set == null) {
			return nbttaglist;
		}
		for (int[] arr : set) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setIntArray("Array", arr);
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIntegerCollection(Collection<Integer> set) {
		NBTTagList nbttaglist = new NBTTagList();
		if (set == null) {
			return nbttaglist;
		}
		for (int slot : set) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Integer", slot);
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIntegerIntegerMap(Map<Integer, Integer> lines) {
		NBTTagList nbttaglist = new NBTTagList();
		if (lines == null) {
			return nbttaglist;
		}
		for (int slot : lines.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Slot", slot);
			nbttagcompound.setInteger("Integer", lines.get(slot));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtIntegerLongMap(HashMap<Integer, Long> lines) {
		NBTTagList nbttaglist = new NBTTagList();
		if (lines == null) {
			return nbttaglist;
		}
		for (int slot : lines.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Slot", slot);
			nbttagcompound.setLong("Long", lines.get(slot));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTBase nbtIntegerStringMap(Map<Integer, String> map) {
		NBTTagList nbttaglist = new NBTTagList();
		if (map == null) {
			return nbttaglist;
		}
		for (int slot : map.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Slot", slot);
			nbttagcompound.setString("Value", map.get(slot));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtItemStackIntegerMap(Map<ItemStack, Integer> map) {
		NBTTagList nbttaglist = new NBTTagList();
		if (map == null) {
			return nbttaglist;
		}
		for (ItemStack item : map.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setTag("Item", item.writeToNBT(new NBTTagCompound()));
			nbttagcompound.setInteger("Value", map.get(item));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtItemStackList(NonNullList<ItemStack> inventory) {
		NBTTagList nbttaglist = new NBTTagList();
		for (int slot = 0; slot < inventory.size(); ++slot) {
			ItemStack item = inventory.get(slot);
			if (!item.isEmpty()) {
				NBTTagCompound nbttagcompound = new NBTTagCompound();
				nbttagcompound.setByte("Slot", (byte) slot);
				item.writeToNBT(nbttagcompound);
				nbttaglist.appendTag(nbttagcompound);
			}
		}
		return nbttaglist;
	}

	public static NBTTagList NBTLongStringMap(Map<Long, String> map) {
		NBTTagList nbttaglist = new NBTTagList();
		if (map == null) {
			return nbttaglist;
		}
		for (long slot : map.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setLong("Long", slot);
			nbttagcompound.setString("String", map.get(slot));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagCompound NBTMerge(NBTTagCompound data, NBTTagCompound merge) {
		NBTTagCompound compound = data.copy();
		Set<String> names = merge.getKeySet();
		for (String name : names) {
			NBTBase base = merge.getTag(name);
			if (base.getId() == 10) {
				base = NBTMerge(compound.getCompoundTag(name), (NBTTagCompound) base);
			}
			compound.setTag(name, base);
		}
		return compound;
	}

	public static NBTTagList NBTScript(List<ScriptContainer> scripts) {
		NBTTagList list = new NBTTagList();
		for (ScriptContainer script : scripts) {
			NBTTagCompound compound = new NBTTagCompound();
			script.writeToNBT(compound);
			list.appendTag(compound);
		}
		return list;
	}

	public static NBTTagList nbtStringIntegerMap(Map<String, Integer> map) {
		NBTTagList nbttaglist = new NBTTagList();
		if (map == null) {
			return nbttaglist;
		}
		for (String slot : map.keySet()) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setString("Slot", slot);
			nbttagcompound.setInteger("Value", map.get(slot));
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

	public static NBTTagList nbtStringList(List<String> list) {
		NBTTagList nbttaglist = new NBTTagList();
		for (String s : list) {
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setString("Line", s);
			nbttaglist.appendTag(nbttagcompound);
		}
		return nbttaglist;
	}

}
