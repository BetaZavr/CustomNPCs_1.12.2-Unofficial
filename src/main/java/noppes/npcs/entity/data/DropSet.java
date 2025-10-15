package noppes.npcs.entity.data;

import java.util.*;

import com.google.common.collect.HashMultimap;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

public class DropSet implements IInventory, ICustomDrop {

	protected final Map<String, Integer> attributeSlotsName = new HashMap<>();
	protected final DataInventory npcInv;
	protected final Deal parentDeal;

	public Availability availability = new Availability();
	public List<AttributeSet> attributes = new ArrayList<>();
	public List<EnchantSet> enchants = new ArrayList<>();
	public List<DropNbtSet> tags = new ArrayList<>();
	public ItemStack item = ItemStack.EMPTY;
	public int pos = -1;
	public int npcLevel;
	public int[] amount = new int[] { 1, 1 };
	public float damage = 1.0f;
	public double chance = 100.0d; // 0-100
	public int lootMode = 0; // 0: normal; 1: drop to Player; 2: inventory
	public boolean tiedToLevel = false;

	public DropSet(DataInventory npcInvIn, Deal dealIn) {
		npcInv = npcInvIn;
		parentDeal = dealIn;
		npcLevel = npcInvIn != null ? npcInvIn.npc.stats.getLevel() : dealIn != null ? 0 : 1;
		attributeSlotsName.put("mainhand", 0);
		attributeSlotsName.put("offhand", 1);
		attributeSlotsName.put("feet", 2);
		attributeSlotsName.put("legs", 3);
		attributeSlotsName.put("chest", 4);
		attributeSlotsName.put("head", 5);
	}

	@SuppressWarnings("all")
	public IAttributeSet addAttribute(IAttributeSet attribute) {
		attributes.add((AttributeSet) attribute);
		return attribute;
	}

	@Override
	public IAttributeSet addAttribute(String attributeName) {
		AttributeSet newAS = new AttributeSet(this);
		newAS.setAttribute(attributeName);
		attributes.add(newAS);
		return newAS;
	}

	@SuppressWarnings("all")
	public IDropNbtSet addDropNbtSet(IDropNbtSet nbtDS) {
		tags.add((DropNbtSet) nbtDS);
		return nbtDS;
	}

	@Override
	public IDropNbtSet addDropNbtSet(int type, double chance, String path, String[] values) {
		DropNbtSet dns = new DropNbtSet(this);
		dns.setType(type);
		dns.setChance(chance);
		dns.setPath(path);
		dns.setValues(values);
		tags.add(dns);
		return dns;
	}

	public IEnchantSet addEnchant(Enchantment enchant) {
		if (enchant != null) {
			EnchantSet newES = new EnchantSet(this);
			newES.setEnchant(enchant);
			enchants.add(newES);
			return newES;
		}
		return null;
	}

	@SuppressWarnings("all")
	public IEnchantSet addEnchant(IEnchantSet enchant) {
		if (enchant != null) {
			enchants.add((EnchantSet) enchant);
			return enchant;
		}
		return null;
	}

	@Override
	public IEnchantSet addEnchant(int enchantId) {
		return addEnchant(Enchantment.getEnchantmentByID(enchantId));
	}

	@Override
	public IEnchantSet addEnchant(String enchantName) {
		return addEnchant(Enchantment.getEnchantmentByLocation(enchantName));
	}

	@Override
	public void clear() { item = ItemStack.EMPTY; }

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public @Nonnull IItemStack createLoot(double addChance) {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(createMCLoot(addChance));
	}

	public @Nonnull ItemStack createMCLoot(double addChance) {
		ItemStack dItem = item.copy();
		// Amount
		int a = amount[0];
		if (amount[0] != amount[1]) {
			if (tiedToLevel) {
				a = (int) Math.round((double) amount[0]
						+ (double) (amount[1] - amount[0]) * (double) npcLevel / (double) CustomNpcs.MaxLv);
			} else {
				a = (int) Math.round((double) amount[0] + (double) (amount[1] - amount[0]) * Math.random());
			}
		}
		dItem.setCount(a);
		// Damage
		if (dItem.getMaxDamage() > 0 && (damage < 1.0f)) {
			int d, max = dItem.getMaxDamage();
			if (tiedToLevel) {
				d = Math.round((1.0f - damage) * (float) max * (float) npcLevel / (float) CustomNpcs.MaxLv);
			} else {
				d = (int) Math.round((1.0f - damage) * (float) max * Math.random());
			}
			dItem.setItemDamage(d);
		}
		// Enchants
		if (!enchants.isEmpty()) {
			for (EnchantSet es : enchants) {
				if (es.chance >= 1.0d || es.chance * addChance / 100.0d < Math.random()) {
					int lvlM = es.getMinLevel();
					int lvlN = es.getMaxLevel();
					if (lvlM == 0 && lvlN == 0) {
						continue;
					}
					int lvl = lvlM;
					if (lvlM != lvlN) {
						if (tiedToLevel) {
							lvl = (int) Math.round((double) lvlM
									+ (double) (lvlN - lvlM) * (double) npcLevel / (double) CustomNpcs.MaxLv);
						} else {
							lvl = (int) Math.round((double) lvlM + (double) (lvlN - lvlM) * Math.random());
						}
					}
					dItem.addEnchantment(es.ench, lvl);
				}
			}

		}
		// Attributes
		if (!attributes.isEmpty()) {
			for (AttributeSet as : attributes) {
				if (as.chance >= 1.0d || as.chance * addChance / 100.0d < Math.random()) {
					double vM = as.getMinValue();
					double vN = as.getMaxValue();
					if (vM == 0.0d && vN == 0.0d) {
						continue;
					}
					double v = vM;
					if (vM != vN) {
						if (tiedToLevel) {
							v = Math.round(
									(vM + (vN - vM) * (double) npcLevel / (double) CustomNpcs.MaxLv) * 10000.0d)
									/ 10000.0d;
						} else {
							v = Math.round((vM + (vN - vM) * Math.random()) * 10000.0d) / 10000.0d;
						}
					}
					(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(dItem)).setAttribute(as.getAttribute(), v, as.getSlot());
				}
			}
		}
		// Tags
		if (!tags.isEmpty()) {
			NBTTagCompound tag = dItem.getTagCompound();
			if (tag == null) {
				dItem.setTagCompound(tag = new NBTTagCompound());
			}
			for (DropNbtSet dns : tags) {
				if (dns.values.length > 0 && (dns.chance >= 1.0d || dns.chance * addChance / 100.0d < Math.random())) {
					tag = dns.getConstructorTag(new NBTWrapper(tag)).getMCNBT();
				}
			}
		}
		if (dItem.hasTagCompound()) {
			if (dItem.getTagCompound() != null && dItem.getTagCompound().getKeySet().isEmpty()) {
				dItem.setTagCompound(null);
			}
		}
		return dItem;
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int index, int count) {
		if (index == 0) {
			ItemStack it;
			if (item.getCount() <= count) {
				it = item.copy();
				item = ItemStack.EMPTY;
			}
			else {
				item.splitStack(count);
				it = item.copy();
				if (item.getCount() == 0) { item = ItemStack.EMPTY; }
			}
			return it;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public IAttributeSet[] getAttributeSets() {
		IAttributeSet[] ass = new IAttributeSet[attributes.size()];
		int i = 0;
		for (AttributeSet as : attributes) {
			ass[i] = as;
			i++;
		}
		return ass;
	}

	@Override
	public double getChance() { return Math.round(chance * 10000.0d) / 10000.0d; }

	@Override
	public float getDamage() { return damage; }

	@Override
	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public IDropNbtSet[] getDropNbtSets() {
		IDropNbtSet[] nts = new IDropNbtSet[tags.size()];
		int i = 0;
		for (DropNbtSet ts : tags) {
			nts[i] = ts;
			i++;
		}
		return nts;
	}

	@Override
	public IEnchantSet[] getEnchantSets() {
		IEnchantSet[] ess = new IEnchantSet[enchants.size()];
		int i = 0;
		for (EnchantSet es : enchants) {
			ess[i] = es;
			i++;
		}
		return ess;
	}

	@Override
	public int getField(int id) { return 0; }

	@Override
	public int getFieldCount() { return 0; }

	@Override
	public int getInventoryStackLimit() { return 64; }

	@Override
	public IItemStack getItem() { return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item); }

	@Override
	public int getLootMode() { return lootMode; }

	@Override
	public int getMaxAmount() { return amount[1]; }

	@Override
	public int getMinAmount() { return amount[0]; }

	@Override
	public Availability getAvailability() { return availability; }

	// inventory
	@Override
	public @Nonnull String getName() {
		return "NPC Drop";
	}

	public NBTTagCompound save() {
		NBTTagCompound nbtDS = new NBTTagCompound();
		nbtDS.setTag("Item", item.writeToNBT(new NBTTagCompound()));
		nbtDS.setDouble("Chance", chance);
		nbtDS.setDouble("DamageToItem", damage);
		nbtDS.setInteger("LootMode", lootMode);
		nbtDS.setBoolean("TiedToLevel", tiedToLevel);
		nbtDS.setTag("Availability", availability.save(new NBTTagCompound()));
		nbtDS.setIntArray("Amount", amount);
		NBTTagList ench = new NBTTagList();
		for (EnchantSet es : enchants) { ench.appendTag(es.getNBT()); }
		nbtDS.setTag("EnchantSettings", ench);
		NBTTagList attr = new NBTTagList();
		for (AttributeSet as : attributes) { attr.appendTag(as.getNBT()); }
		nbtDS.setTag("AttributeSettings", attr);
		NBTTagList tgsl = new NBTTagList();
		for (DropNbtSet ts : tags) { tgsl.appendTag(ts.getNBT()); }
		nbtDS.setTag("TagSettings", tgsl);
		nbtDS.setInteger("Slot", pos);
		return nbtDS;
	}

	@Override
	public int getSizeInventory() { return 1; }

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index == 0) { return item; }
		return ItemStack.EMPTY;
	}

	@Override
	public boolean getTiedToLevel() {
		return tiedToLevel;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public boolean isEmpty() {
        return NoppesUtilServer.IsItemStackNull(item) || item.isEmpty();
    }

	@Override
	public boolean isItemValidForSlot(int index, @Nonnull ItemStack stack) {
		return true;
	}

	@Override
	public boolean isUsableByPlayer(@Nonnull EntityPlayer player) {
		return true;
	}

	public void load(NBTTagCompound nbtDS) {
		item = new ItemStack(nbtDS.getCompoundTag("Item"));
		chance = nbtDS.getDouble("Chance");
		damage = nbtDS.getFloat("DamageToItem");
		if (nbtDS.hasKey("LootMode", 1)) { lootMode = nbtDS.getBoolean("LootMode") ? 1 : 0;}
		else if (nbtDS.hasKey("LootMode", 3)) { lootMode = nbtDS.getInteger("LootMode");}
		tiedToLevel = nbtDS.getBoolean("TiedToLevel");
		if (nbtDS.hasKey("Availability", 10)) { availability.load(nbtDS.getCompoundTag("Availability")); }
		else if (nbtDS.hasKey("Availability", 10)) { // OLD
			availability.clear();
			int questId = nbtDS.getInteger("QuestId");
			if (questId > 0) { availability.setQuest(questId, EnumAvailabilityQuest.Active.ordinal()); }
		}
		int[] cnts = nbtDS.getIntArray("Amount");
		if (nbtDS.hasKey("Amount", 9)) {
			cnts = new int[2];
			for (int i = 0; i < 2; i++) { cnts[i] = nbtDS.getTagList("Amount", 3).getIntAt(i); }
		}
		if (cnts.length != 2) {
			int m = 1, n = 1;
			if (cnts.length >= 1) { m = cnts[0]; }
			if (cnts.length >= 2) { n = cnts[1]; }
			cnts = new int[] { m, n };
		}
		List<EnchantSet> ench = new ArrayList<>();
		for (NBTBase ne : nbtDS.getTagList("EnchantSettings", 10)) {
			EnchantSet es = new EnchantSet(this);
			es.load((NBTTagCompound) ne);
			ench.add(es);
		}
		enchants = ench;
		List<AttributeSet> attr = new ArrayList<>();
		for (NBTBase na : nbtDS.getTagList("AttributeSettings", 10)) {
			AttributeSet as = new AttributeSet(this);
			as.load((NBTTagCompound) na);
			attr.add(as);
		}
		attributes = attr;
		List<DropNbtSet> tgsl = new ArrayList<>();
		for (NBTBase na : nbtDS.getTagList("TagSettings", 10)) {
			DropNbtSet ts = new DropNbtSet(this);
			ts.load((NBTTagCompound) na);
			tgsl.add(ts);
		}
		tags = tgsl;
		pos = nbtDS.getInteger("Slot");
		setAmount(cnts[0], cnts[1]);
	}

	@Override
	public void markDirty() { }

	@Override
	public void openInventory(@Nonnull EntityPlayer player) { }

	@Override
	public void remove() {
		if (npcInv != null) { npcInv.removeDrop(this); }
		if (parentDeal != null) { parentDeal.removeCaseItem(this); }
	}

	@Override
	public void removeAttribute(IAttributeSet attribute) {
		attributes.remove((AttributeSet) attribute);
	}

	@Override
	public void removeDropNbt(IDropNbtSet nbt) { tags.remove((DropNbtSet) nbt); }

	@Override
	public void removeEnchant(IEnchantSet enchant) { enchants.remove((EnchantSet) enchant); }

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		ItemStack stack = item;
		item = ItemStack.EMPTY;
		return stack;
	}

	@Override
	public void resetTo(IItemStack itemIn) {
		if (itemIn == null) { return; }
		resetTo(itemIn.getMCItemStack());
	}

	public void resetTo(ItemStack itemIn) {
		if (itemIn == null || itemIn.isEmpty()) { return; }
		double ch = 85.0d;
		damage = 1.0f;
		lootMode = 0;
		tiedToLevel = false;
		enchants = new ArrayList<>();
		attributes = new ArrayList<>();
		tags = new ArrayList<>();
		// Item Damage
		HashMultimap<String, AttributeModifier> map = (HashMultimap<String, AttributeModifier>) this.item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
		Iterator<Map.Entry<String, AttributeModifier>> iterator = map.entries().iterator();
		double d = 0.0;
		while (iterator.hasNext()) {
			Map.Entry<String, AttributeModifier> entry = iterator.next();
			if (entry.getKey().equals(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
				try {
					AttributeModifier mod = entry.getValue();
					d = mod.getAmount();
				}
				catch (Exception e) { LogWriter.error(e); }
			}
		}
		d += EnchantmentHelper.getModifierForCreature(this.item, EnumCreatureAttribute.UNDEFINED);
		if (d > 1.0d) {
			if (itemIn.getItemDamage() == itemIn.getMaxDamage()) { damage = 1.0f; }
			else { damage = (float) Math.round((double) itemIn.getItemDamage() / (double) itemIn.getMaxDamage() * 100.0d) / 100.0f; }
		}
		amount = new int[] { 1, 1 };
		// Amount
		if (itemIn.getCount() > 1) { amount[1] = itemIn.getCount(); }
		NBTTagCompound itemNbt = itemIn.getTagCompound();
		// Enchants
		if (itemNbt != null && itemNbt.hasKey("ench")) {
			lootMode = 2;
			ch /= itemNbt.getTagList("ench", 10).tagCount();
			for (NBTBase nbtEnch : itemNbt.getTagList("ench", 10)) {
				IEnchantSet es = addEnchant(((NBTTagCompound) nbtEnch).getShort("id"));
				if (es != null) {
					es.setLevels(0, ((NBTTagCompound) nbtEnch).getShort("lvl"));
					es.setChance(85.0d / (double) itemNbt.getTagList("ench", 10).tagCount());
				}
			}
			itemNbt.removeTag("ench");
		}
		// Attributes
		if (itemNbt != null && itemNbt.hasKey("AttributeModifiers")) {
			lootMode = 2;
			ch /= itemNbt.getTagList("AttributeModifiers", 10).tagCount();
			for (NBTBase nbtAttr : itemNbt.getTagList("AttributeModifiers", 10)) {
				IAttributeSet as = addAttribute(((NBTTagCompound) nbtAttr).getString("AttributeName"));
				if (as != null) {
					int slot = -1;
					if (attributeSlotsName.containsKey(((NBTTagCompound) nbtAttr).getString("Slot"))) { slot = attributeSlotsName.get(((NBTTagCompound) nbtAttr).getString("Slot")); }
					as.setSlot(slot);
					double value = ((NBTTagCompound) nbtAttr).getDouble("Amount");
					if (value < 0.0d) { as.setValues(value, 0.0d); }
					else if (value > 0.0d) { as.setValues(0.0d, value); }
					else { as.setValues(0.0d, 0.05d); }
					as.setChance(85.0d / (double) itemNbt.getTagList("AttributeModifiers", 10).tagCount());
				}
			}
			itemNbt.removeTag("AttributeModifiers");
		}
		// Chance
		setChance(ch);
		// Simple Item Set
		NBTTagCompound itemFromNbt = new NBTTagCompound();
		itemIn.writeToNBT(itemFromNbt);
		if (itemNbt != null) { itemFromNbt.setTag("tag", itemNbt); }
		ItemStack newItem = new ItemStack(itemFromNbt);
		newItem.setCount(1);
		if (d > 1) { newItem.setItemDamage(0); }
		item = newItem;
	}

	@Override
	public void setAmount(int min, int max) {
		int newMin = min;
		int newMax = max;
		if (min > max) {
			newMin = max;
			newMax = min;
		}
		if (newMin < 1) {
			newMin = 1;
		}
		if (newMin > item.getMaxStackSize()) {
			newMin = item.getMaxStackSize();
		}
		if (newMax < newMin) {
			newMax = newMin;
		}
		if (newMax > item.getMaxStackSize()) {
			newMax = item.getMaxStackSize();
		}
		amount[0] = newMin;
		amount[1] = newMax;
	}

	@Override
	public void setChance(double chanceIn) { chance = Math.round(ValueUtil.correctDouble(chanceIn, 0.0001d, 100.0d) * 10000.0d) / 10000.0d; }

	@Override
	public void setDamage(float damage) { this.damage = ValueUtil.correctFloat(damage, 0.0f, 1.0f); }

	@Override
	public void setField(int id, int value) { }

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (index == 0) { item = stack; }
	}

	@Override
	public void setItem(IItemStack itemIn) { item = itemIn.getMCItemStack(); }

	@Override
	public void setLootMode(int mode) { lootMode = mode % 3; }

	@Override
	public void setTiedToLevel(boolean tied) { tiedToLevel = tied; }

	public String getKey() {
		String keyName;
		char c = ((char) 167);
		if (item == null) { return "null"; }
		if (item.isEmpty()) { return "type.empty"; }
		keyName = c + "7" + (pos + 1) + ": ";

		double ch = Math.round(chance * 10.0d) / 10.d;
		String chance = String.valueOf(ch).replace(".", ",");
		if (ch == (int) ch) { chance = String.valueOf((int) ch); }
		chance += "%";
		keyName += c + "e" + chance;

		if (amount[0] == amount[1]) { keyName += c + "7[" + c + "6" + amount[0] + c + "7]"; }
		else { keyName += c + "7[" + c + "6" + amount[0] + c + "7-" + c + "6" + amount[1] + c + "7]"; }
		String effs = "";
		if (!enchants.isEmpty()) { effs = c + "7 |" + c + "bE" + c + "7|"; }
		if (!attributes.isEmpty()) {
			if (effs.isEmpty()) { effs += c + "7 |"; }
			effs += c + "aA" + c + "7|";
		}
		if (!tags.isEmpty()) {
			if (effs.isEmpty()) { effs += c + "7 |"; }
			effs += c + "cT" + c + "7|";
		}
		keyName += effs + " " + c + "r" + item.getDisplayName();
		if (pos < 0) { keyName += c + "8 ID:" + toString().substring(toString().indexOf("@") + 1); }
		return keyName;
	}

	public List<String> getHover(EntityPlayer player) {
		List<String> list = new ArrayList<>();
		char c = ((char) 167);
		// pos
		if (pos < 0) { list.add(c + "7-" + c + "8 ID:" + toString().substring(toString().indexOf("@") + 1)); }
		else { list.add(c + "7- ID: " + c + "r" + pos); }
		// stack
		String itemString = Util.instance.translateGoogle(player, "Item");
		if (item == null) { list.add(c + "7- " + itemString + ": " + c + "4null"); }
		else if (item.isEmpty()) { list.add(c + "7- " + itemString + ": " + c + "cEmpty"); }
		else { list.add(c + "7- " + itemString + ": " + c + "r" + item.getItem().getRegistryName()); }
		// amount
		String amountString = Util.instance.translateGoogle(player, "Amount");
		if (amount[0] == amount[1]) { list.add(c + "7- " + amountString + ": " + c + "6" + amount[0]); }
		else { list.add(c + "7- " + amountString + ": " + c + "7[min:" + c + "6" + amount[0] + c + "7; max:" + c + "6" + amount[1] + c + "7]"); }
		// chance
		String chanceString = Util.instance.translateGoogle(player, "Chance");
		if (chance == (int) chance) { list.add(c + "7- " + chanceString + ": " + c + "e" + ((int) chance) + c + "7%"); }
		else { list.add(c + "7- " + chanceString + ": " + c + "e" + ("" + chance).replace(".", ",") + c + "7%"); }
		// loot mode
		String lootString = Util.instance.translateGoogle(player, "Loot");
		if (lootMode == 1) { list.add(c + "7- " + lootString + ": " + c + "r" + Util.instance.translateGoogle(player, "Will fall to the ground")); }
		else if (lootMode == 2) { list.add(c + "7- " + lootString + ": " + c + "r" + Util.instance.translateGoogle(player, "Will be placed in the inventory available when the NPC dies.")); }
		else { list.add(c + "7- " + lootString + ": " + c + "r" + Util.instance.translateGoogle(player, "Will fall to the player who killed this NPC")); }
   		// damage
		String damageString = Util.instance.translateGoogle(player, "Broken");
		if (damage == 1.0f) { list.add(c + "7- " + damageString + ": (max. meta): " + c + "r" + Util.instance.translateGoogle(player, "Doesn't change")); }
		else { list.add(c + "7- " + damageString + " (max. meta): " + c + "6" + ("" + Math.round(damage * 10000.0f) / 100.0f).replace(".", ",") + c + "7% from " + c + "6" + (item == null ? "0" : item.getMaxDamage())); }
		// tiedToLevel
		if (tiedToLevel) { list.add(c + "7- " + Util.instance.translateGoogle(player, "NPC level is taken into account. Average: ") + c + "6" + npcLevel); }
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "NPC level does not affect parameters")); }
		// availability
		if (availability.hasOptions()) { list.add(c + "7- " + Util.instance.translateGoogle(player, "There are accessibility settings")); }
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "Availability not specified")); }
		// enchants
		if (!enchants.isEmpty()) {
			StringBuilder ench = new StringBuilder();
			for (EnchantSet es : enchants) {
				if (ench.length() > 0) { ench.append(c).append("7, "); }
				ench.append(es.ench == null ? "null" : c + "7id: " + c + "b" + Enchantment.getEnchantmentID(es.ench));
			}
			list.add(c + "7- " + Util.instance.translateGoogle(player, "Enchants") + ": [" + ench + c + "7]");
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "\"Enchants\" - not specified")); }
		// enchants
		if (!attributes.isEmpty()) {
			StringBuilder attr = new StringBuilder();
			for (AttributeSet as : attributes) {
				if (attr.length() > 0) { attr.append(c).append("7, "); }
				attr.append(as.attr == null ? "null" : c + "9" + as.attr.getName());
			}
			list.add(c + "7- " + Util.instance.translateGoogle(player, "Attributes") + ": [" + attr + c + "7]");
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "\"Attributes\" - not specified")); }
		// tags
		if (!tags.isEmpty()) {
			StringBuilder nbt = new StringBuilder();
			for (DropNbtSet ns : tags) {
				if (nbt.length() > 0) { nbt.append(c).append("7, "); }
				nbt.append(ns.path == null ? "null" : c + "a" + ns.path);
			}
			list.add(c + "7- " + Util.instance.translateGoogle(player, "Tags") + ": [" + nbt + c + "7]");
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "\"NBT tags\" - not specified")); }
		return list;
	}

	public DropSet copy() {
		DropSet drop = new DropSet(npcInv, parentDeal);
		drop.load(save());
		return drop;
	}

}
