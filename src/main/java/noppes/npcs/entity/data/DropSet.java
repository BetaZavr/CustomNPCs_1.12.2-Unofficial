package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.text.ITextComponent;
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
import noppes.npcs.util.ValueUtil;

public class DropSet implements IInventory, ICustomDrop {

	private Map<String, Integer> attributeSlotsName;
	private DataInventory npcInv;
	public List<AttributeSet> attributes;
	public List<EnchantSet> enchants;
	public List<DropNbtSet> tags;
	public IItemStack item;
	public int questId;
	public int[] amount;
	public float damage;
	public double chance;
	public boolean lootMode; // dropped or get to player
	public boolean tiedToLevel;

	public DropSet(DataInventory ni) {
		this.npcInv = ni;
		this.item = NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
		this.amount = new int[] { 1, 1 };
		this.chance = 100.0d;
		this.damage = 1.0f;
		this.lootMode = false;
		this.tiedToLevel = false;
		this.enchants = new ArrayList<EnchantSet>();
		this.attributes = new ArrayList<AttributeSet>();
		this.tags = new ArrayList<DropNbtSet>();
		Map<String, Integer> sln = new HashMap<String, Integer>();
		sln.put("mainhand", 0);
		sln.put("offhand", 1);
		sln.put("feet", 2);
		sln.put("legs", 3);
		sln.put("chest", 4);
		sln.put("head", 5);
		this.attributeSlotsName = sln;
		this.questId = 0;
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
		return (IAttributeSet) newAS;
	}

	public IDropNbtSet addDropNbtSet(IDropNbtSet nbtDS) {
		this.tags.add((DropNbtSet) nbtDS);
		return nbtDS;
	}

	@Override
	public IDropNbtSet addDropNbtSet(int type, double chance, String paht, String[] values) {
		DropNbtSet dns = new DropNbtSet(this);
		dns.setType(type);
		dns.setChance(chance);
		dns.setPath(paht);
		dns.setValues(values);
		this.tags.add(dns);
		return dns;
	}

	public IEnchantSet addEnchant(Enchantment enchant) {
		if (enchant != null) {
			EnchantSet newES = new EnchantSet(this);
			newES.setEnchant(enchant);
			this.enchants.add(newES);
			return (IEnchantSet) newES;
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
		this.item = NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
	}

	@Override
	public void closeInventory(EntityPlayer player) {
	}

	@Override
	public IItemStack createLoot(double addChance) {
		ItemStack dItem = this.item.getMCItemStack().copy();
		int lv = this.npcInv.npc.stats.getLevel();
		// Amount
		int a = this.amount[0];
		if (this.amount[0] != this.amount[1]) {
			if (this.tiedToLevel) { a = (int) Math.round((double) amount[0] + (double) (amount[1] - amount[0]) * (double) lv / (double) CustomNpcs.maxLv); }
			else { a = (int) Math.round((double) amount[0] + (double) (amount[1] - amount[0]) * Math.random()); }
		}
		dItem.setCount(a);
		// Damage
		if (dItem.getMaxDamage() > 0 && (this.damage < 1.0f)) {
			int d = 0, max = dItem.getMaxDamage();
			if (this.tiedToLevel) {
				d = (int) Math .round((1.0f - this.damage) * (float) max * (float) lv / (float) CustomNpcs.maxLv);
			} else {
				d = (int) Math.round((1.0f - this.damage) * (float) max * Math.random());
			}
			dItem.setItemDamage(d);
		}
		// Enchants
		if (this.enchants.size() > 0) {
			for (EnchantSet es : this.enchants) {
				if (es.chance>=1.0d || es.chance*addChance / 100.0d > Math.random()) {
					int lvlM = es.getMinLevel();
					int lvlN = es.getMaxLevel();
					if (lvlM==0 && lvlN==0) { continue; }
					int lvl = lvlM;
					if (lvlM != lvlN) {
						if (this.tiedToLevel) {
							lvl = (int) Math.round((double) lvlM + (double) (lvlN - lvlM) * (double) lv / (double) CustomNpcs.maxLv);
						} else {
							lvl = (int) Math.round((double) lvlM + (double) (lvlN - lvlM) * Math.random());
						}
					}
					dItem.addEnchantment(es.ench, lvl);
				}
			}

		}
		// Attributes
		if (this.attributes.size() > 0) {
			for (AttributeSet as : this.attributes) {
				if (as.chance>=1.0d || as.chance*addChance / 100.0d > Math.random()) {
					double vM = as.getMinValue();
					double vN = as.getMaxValue();
					if (vM==0.0d && vN==0.0d) { continue; }
					double v = vM;
					if (vM != vN) {
						if (this.tiedToLevel) {
							v = Math.round((vM + (vN - vM) * (double) lv / (double) CustomNpcs.maxLv) * 10000.0d)
									/ 10000.0d;
						} else {
							v = Math.round((vM + (vN - vM) * Math.random()) * 10000.0d) / 10000.0d;
						}
					}
					(NpcAPI.Instance().getIItemStack(dItem)).setAttribute(as.getAttribute(), v, as.getSlot());
				}
			}
		}
		// Tags
		if (this.tags.size() > 0) {
			NBTTagCompound tag = dItem.getTagCompound();
			if (tag == null) {
				dItem.setTagCompound(tag = new NBTTagCompound());
			}
			for (DropNbtSet dns : this.tags) {
				if (dns.values.length>0 && (dns.chance>=1.0d || dns.chance*addChance / 100.0d > Math.random())) {
					tag = dns.getConstructoredTag(new NBTWrapper(tag)).getMCNBT();
				}
			}
		}
		return NpcAPI.Instance().getIItemStack(dItem);
	}

	@Override
	public ItemStack decrStackSize(int index, int count) {
		if (index == 0) {
			ItemStack it;
			if (this.item.getMCItemStack().getCount() <= count) {
				it = this.item.getMCItemStack().copy();
				this.item = NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
			} else {
				this.item.getMCItemStack().splitStack(count);
				it = this.item.getMCItemStack().copy();
				if (this.item.getMCItemStack().getCount() == 0) {
					this.item = NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
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
			ass[i] = (IAttributeSet) as;
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
	public ITextComponent getDisplayName() {
		return null;
	}

	@Override
	public IDropNbtSet[] getDropNbtSets() {
		IDropNbtSet[] nts = new IDropNbtSet[this.tags.size()];
		int i = 0;
		for (DropNbtSet ts : this.tags) {
			nts[i] = (IDropNbtSet) ts;
			i++;
		}
		return nts;
	}

	@Override
	public IEnchantSet[] getEnchantSets() {
		IEnchantSet[] ess = new IEnchantSet[this.enchants.size()];
		int i = 0;
		for (EnchantSet es : this.enchants) {
			ess[i] = (IEnchantSet) es;
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

	public String getKey() {
		String keyName = "";
		char c = ((char) 167);
		if (this.item == null) {
			return "null";
		}
		if (this.item.isEmpty()) {
			return "type.empty";
		}
		int pos = this.npcInv.getDropSlot(this);
		keyName = c+"7"+(pos >= 0 ? (pos + 1) + ":" : "");
		double ch = Math.round(this.chance*10.0d) / 10.d;
		String chance = String.valueOf(ch).replace(".", ",");
		if (ch == (int) ch) { chance = String.valueOf((int) ch); }
		chance += "%";
		keyName += c + "e" + chance;
		if (this.amount[0] == this.amount[1]) {
			keyName += c + "7[" + c + "6" + this.amount[0] + c + "7]";
		} else {
			keyName += c + "7[" + c + "6" + this.amount[0] + c + "7-" + c + "6" + this.amount[1] + c + "7]";
		}
		String effs = "";
		if (!this.enchants.isEmpty()) { effs = c + "7 |" + c + "bE" + c + "7|"; }
		if (!this.attributes.isEmpty()) {
			if (effs.isEmpty()) { effs += c + "7 |"; }
			effs += c + "aA" + c + "7|";
		}
		if (!this.tags.isEmpty()) {
			if (effs.isEmpty()) { effs += c + "7 |"; }
			effs += c + "cT" + c + "7|";
		}
		keyName += effs + " " + c + "r" + this.item.getDisplayName();
		if (pos < 0) {
			keyName += new String(Character.toChars(0x00A7)) + "8 ID:"
					+ this.toString().substring(this.toString().indexOf("@") + 1);
		}
		return keyName;
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
	public String getName() {
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
		NBTTagList cnts = new NBTTagList();
		for (int i : this.amount) {
			cnts.appendTag(new NBTTagInt(i));
		}
		nbtDS.setTag("Amount", cnts);
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
	public ItemStack getStackInSlot(int index) {
		if (index == 0) {
			return ItemStackWrapper.MCItem(this.item);
		}
		return ItemStackWrapper.MCItem(NpcAPI.Instance().getIItemStack(ItemStack.EMPTY));
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
		if (!NoppesUtilServer.IsItemStackNull(this.item.getMCItemStack()) && !item.isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	public boolean isItemValidForSlot(int index, ItemStack stack) {
		return true;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	public void load(NBTTagCompound nbtDS) {
		this.item = NpcAPI.Instance().getIItemStack(new ItemStack(nbtDS.getCompoundTag("Item")));
		this.chance = nbtDS.getDouble("Chance");
		this.damage = nbtDS.getFloat("DamageToItem");
		this.lootMode = nbtDS.getBoolean("LootMode");
		this.tiedToLevel = nbtDS.getBoolean("TiedToLevel");
		this.questId = nbtDS.getInteger("QuestId");
		int[] cnts = new int[2];
		for (int i = 0; i < 2; i++) {
			cnts[i] = nbtDS.getTagList("Amount", 3).getIntAt(i);
		}
		this.amount = cnts;
		List<EnchantSet> ench = new ArrayList<EnchantSet>();
		for (NBTBase ne : nbtDS.getTagList("EnchantSettings", 10)) {
			EnchantSet es = new EnchantSet(this);
			es.load((NBTTagCompound) ne);
			ench.add(es);
		}
		this.enchants = ench;
		List<AttributeSet> attr = new ArrayList<AttributeSet>();
		for (NBTBase na : nbtDS.getTagList("AttributeSettings", 10)) {
			AttributeSet as = new AttributeSet(this);
			as.load((NBTTagCompound) na);
			attr.add(as);
		}
		this.attributes = attr;
		List<DropNbtSet> tgsl = new ArrayList<DropNbtSet>();
		for (NBTBase na : nbtDS.getTagList("TagSettings", 10)) {
			DropNbtSet ts = new DropNbtSet(this);
			ts.load((NBTTagCompound) na);
			tgsl.add(ts);
		}
		this.tags = tgsl;
	}

	@Override
	public void markDirty() {
	}

	@Override
	public void openInventory(EntityPlayer player) {
	}

	@Override
	public void remove() {
		this.npcInv.removeDrop((ICustomDrop) this);
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
	public ItemStack removeStackFromSlot(int index) {
		if (index == 0) {
			ItemStack it = this.item.getMCItemStack();
			this.item = NpcAPI.Instance().getIItemStack(ItemStack.EMPTY);
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
		this.enchants = new ArrayList<EnchantSet>();
		this.attributes = new ArrayList<AttributeSet>();
		this.tags = new ArrayList<DropNbtSet>();
		// Item Damage
		if (item.getAttackDamage() > 1.0d) {
			if (item.getItemDamage() == item.getMaxItemDamage()) {
				this.damage = 1.0f;
			} else {
				this.damage = (float) Math
						.round((double) item.getItemDamage() / (double) item.getMaxItemDamage() * 100.0d) / 100.0f;
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
			ch /= (double) itemNbt.getTagList("ench", 10).tagCount();
			for (NBTBase nbtEnch : itemNbt.getTagList("ench", 10)) {
				IEnchantSet es = addEnchant((int) ((NBTTagCompound) nbtEnch).getShort("id"));
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
			ch /= (double) itemNbt.getTagList("AttributeModifiers", 10).tagCount();
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
		IItemStack newItem = NpcAPI.Instance().getIItemStack(new ItemStack(itemFromNbt));
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
		} else if (newMin > this.item.getMaxStackSize()) {
			newMin = this.item.getMaxStackSize();
		}
		if (newMax < newMin) {
			newMax = newMin;
		} else if (newMax > this.item.getMaxStackSize()) {
			newMax = this.item.getMaxStackSize();
		}
		this.amount = new int[] { newMin, newMax };
	}

	@Override
	public void setChance(double chance) {
		double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
	}

	@Override
	public void setDamage(float dam) {
		this.damage = ValueUtil.correctFloat(dam, 0, 1);
	}

	@Override
	public void setField(int id, int value) {
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		if (index == 0) {
			this.item = NpcAPI.Instance().getIItemStack(stack);
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
		List<Integer> ids = new ArrayList<Integer>();
		for (IQuestCategory cat : NpcAPI.Instance().getQuests().categories()) {
			for (IQuest q : cat.quests()) {
				ids.add(q.getId());
			}
		}
		if (ids.contains(id)) {
			this.questId = (int) id;
		}
	}

	@Override
	public void setTiedToLevel(boolean tied) {
		this.tiedToLevel = tied;
	}

}

