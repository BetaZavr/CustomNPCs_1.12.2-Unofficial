package noppes.npcs.entity.data;

import java.util.*;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.api.wrapper.NBTWrapper;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

public class DropSet implements IInventory, ICustomDrop {

	private final Map<String, Integer> attributeSlotsName;
	private final DataInventory npcInv;
	public List<AttributeSet> attributes = new ArrayList<>();
	public List<EnchantSet> enchants = new ArrayList<>();
	public List<DropNbtSet> tags = new ArrayList<>();
	public IItemStack item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(ItemStack.EMPTY);
	public int questId = 0;
	public int pos = 0;
	public int npcLevel;
	public int[] amount = new int[] { 1, 1 };
	public float damage = 1.0f;
	public double chance = 100.0d; // 0-100
	public boolean lootMode = false; // dropped or get to player
	public boolean tiedToLevel = false;

	public DropSet(DataInventory ni) {
		this.npcInv = ni;
		Map<String, Integer> sln = new HashMap<>();
		sln.put("mainhand", 0);
		sln.put("offhand", 1);
		sln.put("feet", 2);
		sln.put("legs", 3);
		sln.put("chest", 4);
		sln.put("head", 5);
		this.attributeSlotsName = sln;
		this.npcLevel = ni == null ? 1 : ni.npc.stats.getLevel();
	}

	public IAttributeSet addAttribute(IAttributeSet attribute) {
		this.attributes.add((AttributeSet) attribute);
		return attribute;
	}

	@Override
	public IAttributeSet addAttribute(String attributeName) {
		AttributeSet newAS = new AttributeSet(this);
		newAS.setAttribute(attributeName);
		this.attributes.add(newAS);
		return newAS;
	}

	public IDropNbtSet addDropNbtSet(IDropNbtSet nbtDS) {
		this.tags.add((DropNbtSet) nbtDS);
		return nbtDS;
	}

	@Override
	public IDropNbtSet addDropNbtSet(int type, double chance, String path, String[] values) {
		DropNbtSet dns = new DropNbtSet(this);
		dns.setType(type);
		dns.setChance(chance);
		dns.setPath(path);
		dns.setValues(values);
		this.tags.add(dns);
		return dns;
	}

	public IEnchantSet addEnchant(Enchantment enchant) {
		if (enchant != null) {
			EnchantSet newES = new EnchantSet(this);
			newES.setEnchant(enchant);
			this.enchants.add(newES);
			return newES;
		}
		return null;
	}

	public IEnchantSet addEnchant(IEnchantSet enchant) {
		if (enchant != null) {
			this.enchants.add((EnchantSet) enchant);
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
	public void clear() {
		this.item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(ItemStack.EMPTY);
	}

	@Override
	public void closeInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public IItemStack createLoot(double addChance) {
		ItemStack dItem = this.item.getMCItemStack().copy();
		// Amount
		int a = this.amount[0];
		if (this.amount[0] != this.amount[1]) {
			if (this.tiedToLevel) {
				a = (int) Math.round((double) amount[0]
						+ (double) (amount[1] - amount[0]) * (double) this.npcLevel / (double) CustomNpcs.MaxLv);
			} else {
				a = (int) Math.round((double) amount[0] + (double) (amount[1] - amount[0]) * Math.random());
			}
		}
		dItem.setCount(a);
		// Damage
		if (dItem.getMaxDamage() > 0 && (damage < 1.0f)) {
			int d, max = dItem.getMaxDamage();
			if (this.tiedToLevel) {
				d = Math.round((1.0f - this.damage) * (float) max * (float) this.npcLevel / (float) CustomNpcs.MaxLv);
			} else {
				d = (int) Math.round((1.0f - this.damage) * (float) max * Math.random());
			}
			dItem.setItemDamage(d);
		}
		// Enchants
		if (!this.enchants.isEmpty()) {
			for (EnchantSet es : this.enchants) {
				if (es.chance >= 1.0d || es.chance * addChance / 100.0d < Math.random()) {
					int lvlM = es.getMinLevel();
					int lvlN = es.getMaxLevel();
					if (lvlM == 0 && lvlN == 0) {
						continue;
					}
					int lvl = lvlM;
					if (lvlM != lvlN) {
						if (this.tiedToLevel) {
							lvl = (int) Math.round((double) lvlM
									+ (double) (lvlN - lvlM) * (double) this.npcLevel / (double) CustomNpcs.MaxLv);
						} else {
							lvl = (int) Math.round((double) lvlM + (double) (lvlN - lvlM) * Math.random());
						}
					}
					dItem.addEnchantment(es.ench, lvl);
				}
			}

		}
		// Attributes
		if (!this.attributes.isEmpty()) {
			for (AttributeSet as : this.attributes) {
				if (as.chance >= 1.0d || as.chance * addChance / 100.0d < Math.random()) {
					double vM = as.getMinValue();
					double vN = as.getMaxValue();
					if (vM == 0.0d && vN == 0.0d) {
						continue;
					}
					double v = vM;
					if (vM != vN) {
						if (this.tiedToLevel) {
							v = Math.round(
									(vM + (vN - vM) * (double) this.npcLevel / (double) CustomNpcs.MaxLv) * 10000.0d)
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
		if (!this.tags.isEmpty()) {
			NBTTagCompound tag = dItem.getTagCompound();
			if (tag == null) {
				dItem.setTagCompound(tag = new NBTTagCompound());
			}
			for (DropNbtSet dns : this.tags) {
				if (dns.values.length > 0 && (dns.chance >= 1.0d || dns.chance * addChance / 100.0d < Math.random())) {
					tag = dns.getConstructoredTag(new NBTWrapper(tag)).getMCNBT();
				}
			}
		}
		if (dItem.hasTagCompound()) {
			if (dItem.getTagCompound() != null && dItem.getTagCompound().getKeySet().isEmpty()) {
				dItem.setTagCompound(null);
			}
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(dItem);
	}

	@Override
	public @Nonnull ItemStack decrStackSize(int index, int count) {
		if (index == 0) {
			ItemStack it;
			if (this.item.getMCItemStack().getCount() <= count) {
				it = this.item.getMCItemStack().copy();
				this.item = ItemStackWrapper.AIR;
			} else {
				this.item.getMCItemStack().splitStack(count);
				it = this.item.getMCItemStack().copy();
				if (this.item.getMCItemStack().getCount() == 0) {
					this.item = ItemStackWrapper.AIR;
				}
			}
			return it;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public IAttributeSet[] getAttributeSets() {
		IAttributeSet[] ass = new IAttributeSet[this.attributes.size()];
		int i = 0;
		for (AttributeSet as : this.attributes) {
			ass[i] = as;
			i++;
		}
		return ass;
	}

	@Override
	public double getChance() {
		return Math.round(this.chance * 10000.0d) / 10000.0d;
	}

	@Override
	public float getDamage() {
		return this.damage;
	}

	@Override
	public @Nonnull ITextComponent getDisplayName() {
		return new TextComponentString(getName());
	}

	@Override
	public IDropNbtSet[] getDropNbtSets() {
		IDropNbtSet[] nts = new IDropNbtSet[this.tags.size()];
		int i = 0;
		for (DropNbtSet ts : this.tags) {
			nts[i] = ts;
			i++;
		}
		return nts;
	}

	@Override
	public IEnchantSet[] getEnchantSets() {
		IEnchantSet[] ess = new IEnchantSet[this.enchants.size()];
		int i = 0;
		for (EnchantSet es : this.enchants) {
			ess[i] = es;
			i++;
		}
		return ess;
	}

	@Override
	public int getField(int id) {
		return 0;
	}

	@Override
	public int getFieldCount() {
		return 0;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public IItemStack getItem() {
		return this.item;
	}

	@Override
	public boolean getLootMode() {
		return this.lootMode;
	}

	@Override
	public int getMaxAmount() {
		return this.amount[1];
	}

	@Override
	public int getMinAmount() {
		return this.amount[0];
	}

	// inventory
	@Override
	public @Nonnull String getName() {
		return "NPC Drop";
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound nbtDS = new NBTTagCompound();
		nbtDS.setTag("Item", this.item.getMCItemStack().writeToNBT(new NBTTagCompound()));
		nbtDS.setDouble("Chance", this.chance);
		nbtDS.setDouble("DamageToItem", this.damage);
		nbtDS.setBoolean("LootMode", this.lootMode);
		nbtDS.setBoolean("TiedToLevel", this.tiedToLevel);
		nbtDS.setInteger("QuestId", this.questId);
		nbtDS.setIntArray("Amount", this.amount);
		NBTTagList ench = new NBTTagList();
		for (EnchantSet es : this.enchants) {
			ench.appendTag(es.getNBT());
		}
		nbtDS.setTag("EnchantSettings", ench);
		NBTTagList attr = new NBTTagList();
		for (AttributeSet as : this.attributes) {
			attr.appendTag(as.getNBT());
		}
		nbtDS.setTag("AttributeSettings", attr);
		NBTTagList tgsl = new NBTTagList();
		for (DropNbtSet ts : this.tags) {
			tgsl.appendTag(ts.getNBT());
		}
		nbtDS.setTag("TagSettings", tgsl);
		nbtDS.setInteger("Slot", this.pos);
		return nbtDS;
	}

	@Override
	public int getQuestID() {
		return this.questId;
	}

	@Override
	public int getSizeInventory() {
		return 1;
	}

	@Override
	public @Nonnull ItemStack getStackInSlot(int index) {
		if (index == 0) {
			return ItemStackWrapper.MCItem(this.item);
		}
		return ItemStack.EMPTY;
	}

	@Override
	public boolean getTiedToLevel() {
		return this.tiedToLevel;
	}

	@Override
	public boolean hasCustomName() {
		return false;
	}

	@Override
	public boolean isEmpty() {
        return NoppesUtilServer.IsItemStackNull(this.item.getMCItemStack()) || item.isEmpty();
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
		this.item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtDS.getCompoundTag("Item")));
		this.chance = nbtDS.getDouble("Chance");
		this.damage = nbtDS.getFloat("DamageToItem");
		this.lootMode = nbtDS.getBoolean("LootMode");
		this.tiedToLevel = nbtDS.getBoolean("TiedToLevel");
		this.questId = nbtDS.getInteger("QuestId");
		int[] cnts = nbtDS.getIntArray("Amount");
		if (nbtDS.hasKey("Amount", 9)) {
			cnts = new int[2];
			for (int i = 0; i < 2; i++) {
				cnts[i] = nbtDS.getTagList("Amount", 3).getIntAt(i);
			}
		}
		if (cnts.length != 2) {
			int m = 1, n = 1;
			if (cnts.length >= 1) {
				m = cnts[0];
			}
			if (cnts.length >= 2) {
				n = cnts[1];
			}
			cnts = new int[] { m, n };
		}
		List<EnchantSet> ench = new ArrayList<>();
		for (NBTBase ne : nbtDS.getTagList("EnchantSettings", 10)) {
			EnchantSet es = new EnchantSet(this);
			es.load((NBTTagCompound) ne);
			ench.add(es);
		}
		this.enchants = ench;
		List<AttributeSet> attr = new ArrayList<>();
		for (NBTBase na : nbtDS.getTagList("AttributeSettings", 10)) {
			AttributeSet as = new AttributeSet(this);
			as.load((NBTTagCompound) na);
			attr.add(as);
		}
		this.attributes = attr;
		List<DropNbtSet> tgsl = new ArrayList<>();
		for (NBTBase na : nbtDS.getTagList("TagSettings", 10)) {
			DropNbtSet ts = new DropNbtSet(this);
			ts.load((NBTTagCompound) na);
			tgsl.add(ts);
		}
		this.tags = tgsl;
		this.pos = nbtDS.getInteger("Slot");
		this.setAmount(cnts[0], cnts[1]);
	}

	@Override
	public void markDirty() {
	}

	@Override
	public void openInventory(@Nonnull EntityPlayer player) {
	}

	@Override
	public void remove() {
		if (this.npcInv != null) {
			this.npcInv.removeDrop(this);
		}
	}

	@Override
	public void removeAttribute(IAttributeSet attribute) {
		this.attributes.remove((AttributeSet) attribute);
	}

	@Override
	public void removeDropNbt(IDropNbtSet nbt) {
		this.tags.remove((DropNbtSet) nbt);
	}

	@Override
	public void removeEnchant(IEnchantSet enchant) {
		this.enchants.remove((EnchantSet) enchant);
	}

	@Override
	public @Nonnull ItemStack removeStackFromSlot(int index) {
		if (index == 0) {
			ItemStack it = this.item.getMCItemStack();
			this.item = ItemStackWrapper.AIR;
			return it;
		}
		return ItemStack.EMPTY;
	}

	@Override
	public void resetTo(IItemStack item) {
		if (item == null) {
			return;
		}
		if (item.isEmpty()) {
			return;
		}
		double ch = 85.0d;
		this.damage = 1.0f;
		this.lootMode = false;
		this.tiedToLevel = false;
		this.enchants = new ArrayList<>();
		this.attributes = new ArrayList<>();
		this.tags = new ArrayList<>();
		// Item Damage
		if (item.getAttackDamage() > 1.0d) {
			if (item.getItemDamage() == item.getMaxItemDamage()) {
				this.damage = 1.0f;
			} else {
				this.damage = (float) Math.round((double) item.getItemDamage() / (double) item.getMaxItemDamage() * 100.0d) / 100.0f;
			}
		}
		this.amount = new int[] { 1, 1 };
		// Amount
		if (item.getStackSize() > 1) {
			this.amount[1] = item.getStackSize();
		}
		NBTTagCompound itemNbt = item.getNbt().getMCNBT();
		// Enchants
		if (itemNbt.hasKey("ench")) {
			this.lootMode = true;
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
		if (itemNbt.hasKey("AttributeModifiers")) {
			this.lootMode = true;
			ch /= itemNbt.getTagList("AttributeModifiers", 10).tagCount();
			for (NBTBase nbtAttr : itemNbt.getTagList("AttributeModifiers", 10)) {
				IAttributeSet as = addAttribute(((NBTTagCompound) nbtAttr).getString("AttributeName"));
				if (as != null) {
					int slot = -1;
					if (this.attributeSlotsName.containsKey(((NBTTagCompound) nbtAttr).getString("Slot"))) {
						slot = this.attributeSlotsName.get(((NBTTagCompound) nbtAttr).getString("Slot"));
					}
					as.setSlot(slot);
					double value = ((NBTTagCompound) nbtAttr).getDouble("Amount");
					if (value < 0.0d) {
						as.setValues(value, 0.0d);
					} else if (value > 0.0d) {
						as.setValues(0.0d, value);
					} else {
						as.setValues(0.0d, 0.05d);
					}
					as.setChance(85.0d / (double) itemNbt.getTagList("AttributeModifiers", 10).tagCount());
				}
			}
			itemNbt.removeTag("AttributeModifiers");
		}
		// Chance
		setChance(ch);
		// Simple Item Set
		NBTTagCompound itemFromNbt = new NBTTagCompound();
		item.getMCItemStack().writeToNBT(itemFromNbt);
		itemFromNbt.setTag("tag", itemNbt);
		IItemStack newItem = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(itemFromNbt));
		newItem.setStackSize(1);
		if (newItem.getAttackDamage() > 1) {
			newItem.setItemDamage(0);
		}
		this.item = newItem;
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
		if (newMin > this.item.getMaxStackSize()) {
			newMin = this.item.getMaxStackSize();
		}
		if (newMax < newMin) {
			newMax = newMin;
		}
		if (newMax > this.item.getMaxStackSize()) {
			newMax = this.item.getMaxStackSize();
		}
		this.amount[0] = newMin;
		this.amount[1] = newMax;
	}

	@Override
	public void setChance(double chance) {
		double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
	}

	@Override
	public void setDamage(float dam) {
		this.damage = ValueUtil.correctFloat(dam, 0.0f, 1.0f);
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public void setInventorySlotContents(int index, @Nonnull ItemStack stack) {
		if (index == 0) {
			this.item = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack);
		}
	}

	@Override
	public void setItem(IItemStack item) {
		this.item = item;
	}

	@Override
	public void setLootMode(boolean mode) {
		this.lootMode = mode;
	}

	@Override
	public void setQuestID(int id) {
		List<Integer> ids = new ArrayList<>();
		for (IQuestCategory cat : Objects.requireNonNull(NpcAPI.Instance()).getQuests().categories()) {
			for (IQuest q : cat.quests()) {
				ids.add(q.getId());
			}
		}
		if (ids.contains(id)) {
			this.questId = id;
		}
	}

	@Override
	public void setTiedToLevel(boolean tied) {
		this.tiedToLevel = tied;
	}


	public String getKey() {
		String keyName;
		char c = ((char) 167);
		if (item == null) {
			return "null";
		}
		if (item.isEmpty()) {
			return "type.empty";
		}
		keyName = c + "7" + (pos + 1) + ": ";

		double ch = Math.round(chance * 10.0d) / 10.d;
		String chance = String.valueOf(ch).replace(".", ",");
		if (ch == (int) ch) { chance = String.valueOf((int) ch); }
		chance += "%";
		keyName += c + "e" + chance;

		if (amount[0] == amount[1]) {
			keyName += c + "7[" + c + "6" + amount[0] + c + "7]";
		} else {
			keyName += c + "7[" + c + "6" + amount[0] + c + "7-" + c + "6" + amount[1] + c + "7]";
		}
		String effs = "";
		if (!enchants.isEmpty()) {
			effs = c + "7 |" + c + "bE" + c + "7|";
		}
		if (!attributes.isEmpty()) {
			if (effs.isEmpty()) {
				effs += c + "7 |";
			}
			effs += c + "aA" + c + "7|";
		}
		if (!tags.isEmpty()) {
			if (effs.isEmpty()) {
				effs += c + "7 |";
			}
			effs += c + "cT" + c + "7|";
		}
		keyName += effs + " " + c + "r" + item.getDisplayName();
		if (pos < 0) {
			keyName += c + "8 ID:" + toString().substring(toString().indexOf("@") + 1);
		}
		return keyName;
	}

	public String[] getHover(EntityPlayer player) {
		List<String> list = new ArrayList<>();
		char c = ((char) 167);
		// pos
		if (pos < 0) { list.add(c + "7-" + c + "8 ID:" + toString().substring(toString().indexOf("@") + 1)); }
		else { list.add(c + "7- ID: " + c + "r" + pos); }
		// stack
		if (item == null) { list.add(c + "7- Item: " + c + "4null"); }
		else if (item.isEmpty()) { list.add(c + "7- Item: " + c + "cEmpty"); }
		else { list.add(c + "7- Item: " + c + "r" + item.getMCItemStack().getItem().getRegistryName()); }
		// amount
		if (amount[0] == amount[1]) { list.add(c + "7- Amount: " + c + "6" + amount[0]); }
		else { list.add(c + "7- Amount: " + c + "7[min:" + c + "6" + amount[0] + c + "7; max:" + c + "6" + amount[1] + c + "7]"); }
		// chance
		if (chance == (int) chance) { list.add(c + "7- Chance: " + c + "e" + ((int) chance) + c + "7%"); }
		else { list.add(c + "7- Chance: " + c + "e" + ("" + chance).replace(".", ",") + c + "7%"); }
		// loot mode
		if (lootMode) { list.add(c + "7- Loot: " + c + "r" + Util.instance.translateGoogle(player, "Will fall to the ground")); }
		else { list.add(c + "7- Loot: " + c + "r" + Util.instance.translateGoogle(player, "Awarded to the player who killed this NPC")); }
   		// damage
		if (damage == 1.0f) { list.add(c + "7- Max. breakdown (meta): " + c + "r" + Util.instance.translateGoogle(player, "Doesn't change")); }
		else { list.add(c + "7- Max. breakdown (meta): " + c + "6" + ("" + Math.round(damage * 10000.0f) / 100.0f).replace(".", ",") + c + "7% from " + c + "6" + (item == null ? "0" : item.getMaxItemDamage())); }
		// tiedToLevel
		if (tiedToLevel) { list.add(c + "7- " + Util.instance.translateGoogle(player, "NPC level is taken into account. Average: ") + c + "6" + npcLevel); }
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "NPC level does not affect parameters")); }
		// quest
		if (questId > 0) {
			String quest = c + "7- Quest ID: " + c + "2" + questId;
			Quest q = QuestController.instance.quests.get(questId);
			if (q != null) { quest += c + "7; Name: " + c + "r" + q.getTitle(); }
			list.add(quest);
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "Quest ID not specified")); }
		// enchants
		if (!enchants.isEmpty()) {
			StringBuilder ench = new StringBuilder();
			for (EnchantSet es : enchants) {
				if (ench.length() > 0) { ench.append(c).append("7, "); }
				ench.append(es.ench == null ? "null" : c + "7id: " + c + "b" + Enchantment.getEnchantmentID(es.ench));
			}
			list.add(c + "7- Enchants: [" + ench + c + "7]");
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "Enchants not specified")); }
		// enchants
		if (!attributes.isEmpty()) {
			StringBuilder attr = new StringBuilder();
			for (AttributeSet as : attributes) {
				if (attr.length() > 0) { attr.append(c).append("7, "); }
				attr.append(as.attr == null ? "null" : c + "9" + as.attr.getName());
			}
			list.add(c + "7- Attributes: [" + attr + c + "7]");
		}
		else { list.add(c + "7- " + Util.instance.translateGoogle(player, "Attributes not specified")); }
		// tags
		if (!tags.isEmpty()) {
			StringBuilder nbt = new StringBuilder();
			for (DropNbtSet ns : tags) {
				if (nbt.length() > 0) { nbt.append(c).append("7, "); }
				nbt.append(ns.path == null ? "null" : c + "a" + ns.path);
			}
			list.add(c + "7- Tags: [" + nbt + c + "7]");
		}
		else { list.add(c + "7- NBT " + Util.instance.translateGoogle(player, "tags not specified")); }
		return list.toArray(new String[0]);
	}

}
