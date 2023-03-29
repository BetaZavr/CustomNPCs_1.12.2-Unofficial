package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttribute;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.ai.attributes.RangedAttribute;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IAttributeSet;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.IDropNbtSet;
import noppes.npcs.api.entity.data.IEnchantSet;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestCategory;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataInventory
implements IInventory,
INPCInventory {
	
	public class AttributeSet implements IAttributeSet {

		public IAttribute attr;
		public double chance;
		public DropSet parent;
		public int slot;
		public double[] values;

		public AttributeSet(DropSet p) {
			this.parent = p;
			this.values = new double[] { 0.0d, 0.05d };
			this.attr = SharedMonsterAttributes.MAX_HEALTH;
			this.chance = 100.0d;
			this.slot = 0;
		}

		@Override
		public String getAttribute() {
			return this.attr.getName();
		}

		@Override
		public double getChance() {
			return Math.round(this.chance * 10000.0d) / 10000.0d;
		}

		public String getKey() {
			String vs = " [" + this.values[0] + "]";
			if (this.values[0] != this.values[1]) {
				vs = " [" + this.values[0] + "<->" + this.values[1] + "]";
			}
			String name = new TextComponentTranslation("attribute.name." + this.attr.getName(), new Object[0])
					.getFormattedText();
			if (name.equals("attribute.name." + this.attr.getName()) || name.equals("attribute.name.")) {
				name = this.attr.getName();
			}
			String keyName = name + vs + " (" + this.getChance() + ")";
			keyName += " #" + this.toString().substring(this.toString().indexOf("@") + 1);
			return keyName;
		}

		@Override
		public double getMaxValue() {
			return this.values[1];
		}

		@Override
		public double getMinValue() {
			return this.values[0];
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound nbtAS = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagDouble(this.values[0]));
			list.appendTag(new NBTTagDouble(this.values[1]));
			nbtAS.setTag("Values", list);
			nbtAS.setString("Name", this.attr.getName());
			nbtAS.setDouble("Chance", this.chance);
			nbtAS.setInteger("Slot", this.slot);
			return nbtAS;
		}

		@Override
		public int getSlot() {
			return this.slot;
		}

		public void load(NBTTagCompound nbtAS) {
			double[] newVs = new double[2];
			for (int i = 0; i < 2; i++) {
				newVs[i] = nbtAS.getTagList("Values", 6).getDoubleAt(i);
			}
			this.values = newVs;
			setAttribute(nbtAS.getString("Name"));
			this.chance = nbtAS.getDouble("Chance");
			this.slot = nbtAS.getInteger("Slot");
		}

		@Override
		public void remove() {
			this.parent.removeAttribute((AttributeSet) this);
		}

		public void setAttribute(IAttribute attribute) {
			this.attr = attribute;
		}

		@Override
		public void setAttribute(String name) {
			if (name.equals("generic.maxHealth")) {
				this.attr = SharedMonsterAttributes.MAX_HEALTH;
			} else if (name.equals("generic.followRange")) {
				this.attr = SharedMonsterAttributes.FOLLOW_RANGE;
			} else if (name.equals("generic.knockbackResistance")) {
				this.attr = SharedMonsterAttributes.KNOCKBACK_RESISTANCE;
			} else if (name.equals("generic.movementSpeed")) {
				this.attr = SharedMonsterAttributes.MOVEMENT_SPEED;
			} else if (name.equals("generic.attackDamage")) {
				this.attr = SharedMonsterAttributes.ATTACK_DAMAGE;
			} else if (name.equals("generic.attackSpeed")) {
				this.attr = SharedMonsterAttributes.ATTACK_SPEED;
			} else if (name.equals("generic.armor")) {
				this.attr = SharedMonsterAttributes.ARMOR;
			} else if (name.equals("generic.luck")) {
				this.attr = SharedMonsterAttributes.LUCK;
			} else { // new
				this.attr = (IAttribute) (new RangedAttribute((IAttribute) null, name, 0.0D, -1024.0D, 1024.0D))
						.setShouldWatch(true);
			}
		}

		@Override
		public void setChance(double chance) {
			double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
			this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
		}

		@Override
		public void setSlot(int slot) {
			if (slot < -1 || slot > 5) {
				throw new CustomNPCsException("Slot has to be between -1 and 5, given was: " + slot, new Object[0]);
			}
			this.slot = slot;
		}

		@Override
		public void setValues(double min, double max) {
			double newMin = min;
			double newMax = max;
			if (min > max) {
				newMin = max;
				newMax = min;
			}
			this.values = new double[] { newMin, newMax };
		}
	}

	public class DropNbtSet implements IDropNbtSet {

		public double chance;
		private DropSet parent;
		public String path;
		public int type;
		public int typeList;
		private String[] values;

		public DropNbtSet(DropSet ds) {
			this.parent = ds;
			this.path = "";
			this.values = new String[0];
			this.type = 0;
			this.typeList = 0;
			this.chance = 100.0d;
		}

		public String cheakValue(String value, int type) {
			switch (type) {
			case 0: { // boolean
				try {
					boolean b = Boolean.valueOf(value);
					return String.valueOf(b);
				} catch (Exception e) {
				}
				break;
			}
			case 1: { // byte
				try {
					byte b = Byte.valueOf(value);
					return String.valueOf(b);
				} catch (Exception e) {
				}
				break;
			}
			case 2: { // short
				try {
					short s = Short.valueOf(value);
					return String.valueOf(s);
				} catch (Exception e) {
				}
				break;
			}
			case 3: { // integer
				try {
					int b = Integer.valueOf(value);
					return String.valueOf(b);
				} catch (Exception e) {
				}
				break;
			}
			case 4: { // long
				try {
					long l = Long.valueOf(value);
					return String.valueOf(l);
				} catch (Exception e) {
				}
				break;
			}
			case 5: { // float
				try {
					float f = Float.valueOf(value);
					return String.valueOf(f);
				} catch (Exception e) {
				}
				break;
			}
			case 6: { // double
				try {
					double d = Double.valueOf(value);
					return String.valueOf(d);
				} catch (Exception e) {
				}
				break;
			}
			case 7: { // byte array
				String[] br = value.split(",");
				String text = "";
				for (String str : br) {
					try {
						byte b = Byte.valueOf(str);
						if (text.length() > 0) {
							text += ",";
						}
						text += String.valueOf(b);
					} catch (Exception e) {
					}
				}
				if (text.length() > 0) {
					return text;
				}
				break;
			}
			case 8: { // string
				return value;
			}
			case 9: { // list
				String[] br = value.split(",");
				String text = "";
				for (String str : br) {
					try {
						String sc = cheakValue(str, this.typeList);
						if (sc != null) {
							if (text.length() > 0) {
								text += ",";
							}
							text += sc;
						}
					} catch (Exception e) {
					}
				}
				if (text.length() > 0) {
					return text;
				}
				break;
			}
			case 11: { // integer array
				String[] br = value.split(",");
				String text = "";
				for (String str : br) {
					try {
						int i = Integer.valueOf(str);
						if (text.length() > 0) {
							if (type == this.type) {
								text += ",";
							}
							{
								text += ";";
							}
						}
						text += String.valueOf(i);
					} catch (Exception e) {
					}
				}
				if (text.length() > 0) {
					return text;
				}
				break;
			}
			}
			return null;
		}

		@Override
		public double getChance() {
			return Math.round(this.chance * 10000.0d) / 10000.0d;
		}

		@Override
		public NBTTagCompound getConstructoredTag(NBTTagCompound nbt) {
			NBTTagCompound pos = nbt;
			String key = this.path;
			if (this.path.indexOf(".") != -1) {
				String keyName = "";
				while (key.indexOf(".") != -1) {
					keyName = key.substring(0, key.indexOf("."));
					if (!pos.hasKey(keyName, 10)) {
						pos.setTag(keyName, new NBTTagCompound());
					}
					pos = pos.getCompoundTag(keyName);
					key = key.substring(key.indexOf(".") + 1);
				}
			}
			int idx = (int) ((double) this.values.length * Math.random());
			if (idx >= this.values.length) {
				idx = this.values.length - 1;
			}
			String value = this.values[idx];
			switch (this.type) {
			case 0: { // booleab
				pos.setBoolean(key, Boolean.valueOf(value));
				break;
			}
			case 1: { // byte
				pos.setByte(key, Byte.valueOf(value));
				break;
			}
			case 2: { // short
				pos.setShort(key, Short.valueOf(value));
				break;
			}
			case 3: { // integer
				pos.setInteger(key, Integer.valueOf(value));
				break;
			}
			case 4: { // long
				pos.setLong(key, Long.valueOf(value));
				break;
			}
			case 5: { // float
				pos.setFloat(key, Float.valueOf(value));
				break;
			}
			case 6: { // double
				pos.setDouble(key, Double.valueOf(value));
				break;
			}
			case 7: { // byte array
				String[] brs = value.split(",");
				byte[] br = new byte[brs.length];
				for (int i = 0; i < brs.length; i++) {
					br[i] = Byte.valueOf(brs[i]);
				}
				pos.setByteArray(key, br);
				break;
			}
			case 8: { // string
				pos.setString(key, value);
				break;
			}
			case 9: { // list
				String[] brs = value.split(",");
				NBTTagList list = new NBTTagList();
				for (int i = 0; i < brs.length; i++) {
					if (this.typeList == 3) {
						list.appendTag(new NBTTagInt(Integer.valueOf(brs[i])));
					} else if (this.typeList == 5) {
						list.appendTag(new NBTTagFloat(Float.valueOf(brs[i])));
					} else if (this.typeList == 6) {
						list.appendTag(new NBTTagDouble(Double.valueOf(brs[i])));
					} else if (this.typeList == 8) {
						list.appendTag(new NBTTagString(brs[i]));
					} else if (this.typeList == 11) {
						String[] ints = brs[i].split(";");
						int[] is = new int[ints.length];
						for (int j = 0; j < ints.length; j++) {
							is[j] = Integer.valueOf(ints[j]);
						}
						list.appendTag(new NBTTagIntArray(is));
					}
				}
				pos.setTag(key, list);
				break;
			}
			case 11: { // integer array
				String[] ints = value.split(",");
				int[] is = new int[ints.length];
				for (int i = 0; i < ints.length; i++) {
					is[i] = Integer.valueOf(ints[i]);
				}
				pos.setIntArray(key, is);
				break;
			}
			}
			return nbt;
		}

		public String getKey() {
			String key = this.path;
			if (this.path.indexOf(".") != -1) {
				String keyName = "" + this.path;
				List<String> keys = new ArrayList<String>();
				String preKey = "";
				while (keyName.indexOf(".") != -1) {
					preKey = keyName.substring(0, keyName.indexOf("."));
					keys.add("" + preKey);
					keyName = keyName.substring(keyName.indexOf(".") + 1);
				}
				keys.add(keyName);
				if (keys.size() > 2) {
					key = "...";
				} else {
					key = "";
				}
				key += preKey + "." + keyName;
			}
			key += " (" + this.getChance() + ")";
			key += " #" + this.toString().substring(this.toString().indexOf("@") + 1);
			return key;
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound nbtDS = new NBTTagCompound();
			nbtDS.setInteger("Type", this.type);
			nbtDS.setInteger("TypeList", this.typeList);
			nbtDS.setString("Path", this.path);
			nbtDS.setDouble("Chance", this.chance);
			NBTTagList vs = new NBTTagList();
			for (String s : this.values) {
				if (s != null) {
					vs.appendTag(new NBTTagString(s));
				}
			}
			nbtDS.setTag("Values", vs);
			return nbtDS;
		}

		@Override
		public String getPath() {
			return this.path;
		}

		@Override
		public int getType() {
			return this.type;
		}

		@Override
		public int getTypeList() {
			return this.typeList;
		}

		@Override
		public String[] getValues() {
			return this.values;
		}

		public void load(NBTTagCompound nbtDS) {
			this.type = nbtDS.getInteger("Type");
			this.typeList = nbtDS.getInteger("TypeList");
			this.path = nbtDS.getString("Path");
			this.chance = nbtDS.getDouble("Chance");
			String[] vs = new String[nbtDS.getTagList("Values", 8).tagCount()];
			for (int i = 0; i < nbtDS.getTagList("Values", 8).tagCount(); i++) {
				String ch = cheakValue(nbtDS.getTagList("Values", 8).getStringTagAt(i), this.type);
				if (ch != null) {
					vs[i] = ch;
				}
			}
			this.values = vs;
		}

		@Override
		public void remove() {
			this.parent.removeDropNbt((DropNbtSet) this);
		}

		@Override
		public void setChance(double chance) {
			double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
			this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
		}

		@Override
		public void setPath(String paht) {
			this.path = paht;
		}

		@Override
		public void setType(int type) {
			if (type == 0 || type == 1 || type == 2 || type == 3 || type == 4 || type == 5 || type == 6 || type == 7
					|| type == 8 || type == 9 || type == 11) {
				this.type = type;
			}
		}

		@Override
		public void setTypeList(int type) {
			if (type == 3 || type == 5 || type == 6 || type == 8 || type == 11) {
				this.typeList = type;
			}
		}

		@Override
		public void setValues(String values) {
			if (values.indexOf("|") != -1) {
				List<String> nal = new ArrayList<String>();
				while (values.indexOf("|") != -1) {
					String key = cheakValue(values.substring(0, values.indexOf("|")), this.type);
					if (key != null) {
						nal.add(key);
					}
					values = values.substring(values.indexOf("|") + 1);
				}
				nal.add(values);
				String[] svs = new String[nal.size()];
				for (int i = 0; i < nal.size(); i++) {
					svs[i] = nal.get(i);
				}
				this.values = svs;
			} else {
				String ch = cheakValue(values, this.type);
				if (ch != null) {
					this.values = new String[] { ch };
				}
			}
		}

		@Override
		public void setValues(String[] values) {
			List<String> nal = new ArrayList<String>();
			for (String str : values) {
				String key = cheakValue(str, this.type);
				if (key != null) {
					nal.add(key);
				}
			}
			String[] svs = new String[nal.size()];
			for (int i = 0; i < nal.size(); i++) {
				svs[i] = nal.get(i);
			}
			this.values = svs;
		}
	}

	public class DropSet implements IInventory, ICustomDrop {

		public int[] amount;
		public List<AttributeSet> attributes;
		private Map<String, Integer> attributeSlotsName;
		public double chance;
		public float damage;
		public List<EnchantSet> enchants;
		public IItemStack item;
		public boolean lootMode;
		private DataInventory npcInv;
		public int questId;
		public List<DropNbtSet> tags;
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
			int a = amount[0];
			if (this.amount[0] != this.amount[1]) {
				if (this.tiedToLevel) {
					a = (int) Math.round((double) amount[0]
							+ (double) (amount[1] - amount[0]) * (double) lv / (double) CustomNpcs.maxLv);
				} else {
					a = (int) Math.round((double) amount[0] + (double) (amount[1] - amount[0]) * Math.random());
				}
			}
			dItem.setCount(a);
			// Damage
			int max = dItem.getMaxDamage();
			int d = 0;
			if (max > 0) {
				if (this.damage < 1.0f) {
					if (this.tiedToLevel) {
						d = (int) Math
								.round((1.0f - this.damage) * (float) max * (float) lv / (float) CustomNpcs.maxLv);
					} else {
						d = (int) Math.round((1.0f - this.damage) * (float) max * Math.random());
					}
				}
				dItem.setItemDamage(d);
			}
			// Enchants
			if (this.enchants.size() > 0) {
				for (EnchantSet es : this.enchants) {
					if (es.chance*addChance / 100.0d > Math.random()) {
						int lvlM = es.getMinLevel();
						int lvlN = es.getMaxLevel();
						int lvl = lvlM;
						if (lvlM != lvlN) {
							if (this.tiedToLevel) {
								lvl = (int) Math.round((double) lvlM
										+ (double) (lvlN - lvlM) * (double) lv / (double) CustomNpcs.maxLv);
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
					if (as.chance*addChance / 100.0d > Math.random()) {
						double vM = as.getMinValue();
						double vN = as.getMaxValue();
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
					if (dns.chance*addChance / 100.0d > Math.random()) {
						tag = dns.getConstructoredTag(tag);
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
			if (this.item == null) {
				return "null";
			}
			if (this.item.isEmpty()) {
				return "type.empty";
			}
			int pos = npcInv.getDropSlot(this);
			keyName = (pos >= 0 ? (pos + 1) + " - " : "") + this.item.getDisplayName();
			keyName += " (" + String.valueOf(Math.round(this.chance*10.0d)/10.d).replace(".", ",") + ")";
			if (this.amount[0] == this.amount[1]) {
				if (this.amount[0] > 1) {
					keyName += " [" + this.amount[0] + "]";
				}
			} else {
				keyName += " [" + this.amount[0] + "<->" + this.amount[1] + "]";
			}
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

	public class EnchantSet implements IEnchantSet {

		public double chance;
		public Enchantment ench;
		public int[] lvls;
		public DropSet parent;

		public EnchantSet(DropSet p) {
			this.parent = p;
			this.lvls = new int[] { 0, 1 };
			this.ench = Enchantment.getEnchantmentByID(0);
			this.chance = 100.0d;
		}

		@Override
		public double getChance() {
			return Math.round(this.chance * 10000.0d) / 10000.0d;
		}

		@Override
		public String getEnchant() {
			return this.ench.getName();
		}

		public String getKey() {
			String lvl = " [" + this.lvls[0] + "]";
			if (this.lvls[0] != this.lvls[1]) {
				lvl = " [" + this.lvls[0] + "<->" + this.lvls[1] + "]";
			}
			String keyName = new TextComponentTranslation(this.ench.getName(), new Object[0]).getFormattedText() + lvl
					+ " (" + this.getChance() + ")";
			keyName += " #" + this.toString().substring(this.toString().indexOf("@") + 1);
			return keyName;
		}

		@Override
		public int getMaxLevel() {
			return this.lvls[1];
		}

		@Override
		public int getMinLevel() {
			return this.lvls[0];
		}

		public NBTTagCompound getNBT() {
			NBTTagCompound nbtES = new NBTTagCompound();
			NBTTagList list = new NBTTagList();
			list.appendTag(new NBTTagInt(this.lvls[0]));
			list.appendTag(new NBTTagInt(this.lvls[1]));
			nbtES.setTag("Levels", list);
			nbtES.setInteger("ID", Enchantment.getEnchantmentID(this.ench));
			nbtES.setDouble("Chance", this.chance);
			return nbtES;
		}

		public void load(NBTTagCompound nbtES) {
			int[] newLv = new int[2];
			for (int i = 0; i < 2; i++) {
				newLv[i] = nbtES.getTagList("Levels", 3).getIntAt(i);
			}
			this.lvls = newLv;
			this.ench = Enchantment.getEnchantmentByID(nbtES.getInteger("ID"));
			this.chance = nbtES.getDouble("Chance");
		}

		@Override
		public void remove() {
			this.parent.removeEnchant((EnchantSet) this);
		}

		@Override
		public void setChance(double chance) {
			double newChance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
			this.chance = Math.round(newChance * 10000.0d) / 10000.0d;
		}

		@Override
		public void setEnchant(Enchantment enchant) {
			if (enchant == null) {
				this.parent.removeEnchant((EnchantSet) this);
				return;
			}
			this.ench = enchant;
		}

		@Override
		public boolean setEnchant(int id) {
			Enchantment newEnch = Enchantment.getEnchantmentByID(id);
			if (newEnch != null) {
				this.ench = newEnch;
				return true;
			}
			return false;
		}

		@Override
		public boolean setEnchant(String name) {
			Enchantment newEnch = Enchantment.getEnchantmentByLocation(name);
			if (newEnch != null) {
				this.ench = newEnch;
				return true;
			}
			return false;
		}

		@Override
		public void setLevels(int min, int max) {
			int newMin = min;
			int newMax = max;
			if (min > max) {
				newMin = max;
				newMax = min;
			}
			this.lvls = new int[] { newMin, newMax };
		}
	}

	public Map<Integer, IItemStack> armor;
	public Map<Integer, DropSet> drops;
	// New
	public boolean lootMode;
	private int maxAmount;

	private int maxExp;

	private int minExp;

	// public int lootMode; Changed
	private EntityNPCInterface npc;

	// public Map<Integer, IItemStack> drops; Changed
	// public Map<Integer, Integer> dropchance; Changed
	public Map<Integer, IItemStack> weapons;

	public DataInventory(EntityNPCInterface npc) {
		// this.drops = new HashMap<Integer, IItemStack>(); Changed
		// this.dropchance = new HashMap<Integer, Integer>(); Changed
		this.weapons = new HashMap<Integer, IItemStack>();
		this.armor = new HashMap<Integer, IItemStack>();
		this.minExp = 0;
		this.maxExp = 0;
		// this.lootMode = 0; Changed
		this.npc = npc;
		// New
		this.drops = new HashMap<Integer, DropSet>();
		this.lootMode = true;
		this.maxAmount = 0;
	}

	public ICustomDrop addDropItem(IItemStack item, double chance) {
		if (this.drops.size() >= 32) {
			throw new CustomNPCsException("Bad maximum size: " + this.drops.size() + " (32 slots maximum)",
					new Object[0]);
		}
		chance = ValueUtil.correctDouble(chance, 0.0001d, 100.0d);
		DropSet ds = new DropSet(this);
		ds.item = item;
		ds.chance = chance;
		this.drops.put(this.drops.size(), ds);
		return (ICustomDrop) ds;
	}

	public void clear() {
	}

	public void closeInventory(EntityPlayer player) {
	}

	private IItemStack[] createDrops(boolean mode, EntityLivingBase attacking) {
		List<IItemStack> prelist = new ArrayList<IItemStack>();
		double ch = 1.0d;
		if (attacking!=null) {
			IAttributeInstance l = attacking.getEntityAttribute(SharedMonsterAttributes.LUCK);
			if (l!=null) { ch += l.getAttributeValue() / 20.d; }
			ItemStack held = attacking.getHeldItemMainhand();
			if (held.isItemEnchanted()) {
				Enchantment ench = Enchantment.getEnchantmentByLocation("looting");
				if (ench!=null) {
					int id = Enchantment.getEnchantmentID(ench);
					for (int i =0; i<held.getEnchantmentTagList().tagCount(); i++) {
						NBTTagCompound nbt = held.getEnchantmentTagList().getCompoundTagAt(i);
						if ((int) nbt.getShort("id")==id) {
							ch += (double) nbt.getShort("lvl")/100.0d;
						}
					}
				}
			}
		}
		for (DropSet ds : this.drops.values()) {
			if (ds.item == null || ds.item.isEmpty() || mode == ds.lootMode || ds.chance*ch / 100.0d > Math.random()) { continue; }
			if (ds.getQuestID() > 0) {
				if (attacking instanceof EntityPlayer) {
					IPlayer<?> player = (IPlayer<?>) NpcAPI.Instance().getIEntity(attacking);
					for (IQuest q : player.getActiveQuests()) {
						if (q.getId() == ds.getQuestID()) {
							boolean needAdd = false;
							for (IQuestObjective objQ : q.getObjectives(player)) {
								if (!objQ.isCompleted()) {
									needAdd = true;
									break;
								}
							}
							if (needAdd) {
								prelist.add(ds.createLoot(ch));
							}
							break;
						}
					}
				}
			} else {
				prelist.add(ds.createLoot(ch));
			}
		}
		if (this.maxAmount > 0 && prelist.size() > this.maxAmount) {
			List<IItemStack> list = new ArrayList<IItemStack>();
			int max = this.maxAmount;
			for (int i = 0; i < max; i++) {
				int index = (int) Math.round((double) prelist.size() * Math.random());
				if (index == prelist.size()) {
					index--;
				}
				list.add(prelist.get(index).copy());
				prelist.remove(index);
			}
			return list.toArray(new IItemStack[list.size()]);
		}
		return prelist.toArray(new IItemStack[prelist.size()]);
	}

	public ItemStack decrStackSize(int par1, int par2) { // Changed
		int i = 0;
		Map<Integer, IItemStack> var3 = new HashMap<Integer, IItemStack>(); // Changed
		ItemStack var4 = null;
		/*
		 * Changed if (par1 >= 7) { var3 = this.drops; par1 -= 7; } else
		 */if (par1 >= 4) {
			var3 = this.weapons;
			par1 -= 4;
			i = 1;
		} else {
			var3 = this.armor;
			i = 2;
		}

		if (var3.get(par1) != null) {
			if (var3.get(par1).getMCItemStack().getCount() <= par2) {
				var4 = var3.get(par1).getMCItemStack();
				var3.put(par1, null);
			} else {
				var4 = var3.get(par1).getMCItemStack().splitStack(par2);
				if (var3.get(par1).getMCItemStack().getCount() == 0) {
					var3.put(par1, null);
				}
			}
		}
		if (i == 1) {
			this.weapons = var3;
		}
		if (i == 2) {
			this.armor = var3;
		}
		if (var4 == null) {
			return ItemStack.EMPTY;
		}
		return var4;
	}

	public void dropStuff(NpcEvent.DiedEvent event, Entity entity, DamageSource damagesource) { // Changed
		ArrayList<EntityItem> list = new ArrayList<EntityItem>();
		/*
		 * Changed if (event.droppedItems != null) { for (IItemStack item :
		 * event.droppedItems) { EntityItem e =
		 * this.getEntityItem(item.getMCItemStack().copy()); if (e != null) {
		 * list.add(e); } } }
		 */
		int enchant = 0;
		if (damagesource.getTrueSource() instanceof EntityPlayer) {
			enchant = EnchantmentHelper.getLootingModifier((EntityLivingBase) damagesource.getTrueSource());
		}
		if (!ForgeHooks.onLivingDrops(this.npc, damagesource, list, enchant, true)) {
			/*
			 * Changed for (EntityItem item2 : list) { if (this.lootMode == 1 && entity
			 * instanceof EntityPlayer) { EntityPlayer player = (EntityPlayer)entity;
			 * item2.setPickupDelay(2); this.npc.world.spawnEntity(item2); ItemStack stack =
			 * item2.getItem(); int i = stack.getCount(); if
			 * (!player.inventory.addItemStackToInventory(stack)) { continue; }
			 * entity.world.playSound((EntityPlayer)null, player.posX, player.posY,
			 * player.posZ, SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
			 * ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) *
			 * 2.0f); player.onItemPickup(item2, i); if (stack.getCount() > 0) { continue; }
			 * item2.setDead(); } else { this.npc.world.spawnEntity(item2); } }
			 */
			if (event.droppedItems != null) {
				for (IItemStack itemD : event.droppedItems) {
					if (itemD == null || itemD.isEmpty()) {
						continue;
					}
					EntityItem e = this.getEntityItem(itemD.getMCItemStack().copy());
					if (e == null) {
						continue;
					}
					this.npc.world.spawnEntity(e);
				}
			}
			if (event.lootedItems != null) {
				for (IItemStack itemL : event.lootedItems) {
					if (itemL == null || itemL.isEmpty()) {
						continue;
					}
					EntityItem e = this.getEntityItem(itemL.getMCItemStack().copy());
					if (e == null) {
						continue;
					}
					if (entity instanceof EntityPlayer) {
						EntityPlayer player = (EntityPlayer) entity;
						e.setPickupDelay(2);
						this.npc.world.spawnEntity((Entity) e);
						ItemStack stack = e.getItem();
						int i = stack.getCount();
						if (!player.inventory.addItemStackToInventory(stack)) {
							continue;
						}
						entity.world.playSound((EntityPlayer) null, player.posX, player.posY, player.posZ,
								SoundEvents.ENTITY_ITEM_PICKUP, SoundCategory.PLAYERS, 0.2f,
								((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						player.onItemPickup((Entity) e, i);
						if (stack.getCount() > 0) {
							continue;
						}
						e.setDead();
					} else {
						this.npc.world.spawnEntity((Entity) e);
					}
				}
			}
		}
		int exp = event.expDropped;
		while (exp > 0) {
			int var2 = EntityXPOrb.getXPSplit(exp);
			exp -= var2;
			if (/* Changed this.lootMode == 1 */ this.lootMode && entity instanceof EntityPlayer) {
				this.npc.world.spawnEntity(new EntityXPOrb(entity.world, entity.posX, entity.posY, entity.posZ, var2));
			} else {
				this.npc.world.spawnEntity(
						new EntityXPOrb(this.npc.world, this.npc.posX, this.npc.posY, this.npc.posZ, var2));
			}
		}
	}

	/*
	 * Changed public IItemStack getDropItem(int slot) { if (slot < 0 || slot > 8) {
	 * throw new CustomNPCsException("Bad slot number: " + slot, new Object[0]); }
	 * IItemStack item = this.npc.inventory.drops.get(slot); if (item == null) {
	 * return null; } return NpcAPI.Instance().getIItemStack(item.getMCItemStack());
	 * }
	 * 
	 * public void setDropItem(int slot, IItemStack item, int chance) { if (slot < 0
	 * || slot > 8) { throw new CustomNPCsException("Bad slot number: " + slot, new
	 * Object[0]); } chance = ValueUtil.correctInt(chance, 1, 100); if (item == null
	 * || item.isEmpty()) { this.dropchance.remove(slot); this.drops.remove(slot); }
	 * else { this.dropchance.put(slot, chance); this.drops.put(slot, item); } }
	 * 
	 * public IItemStack[] getItemsRNG() { ArrayList<IItemStack> list = new
	 * ArrayList<IItemStack>(); for (int i : this.drops.keySet()) { IItemStack item
	 * = this.drops.get(i); if (item != null) { if (item.isEmpty()) { continue; }
	 * int dchance = 100; if (this.dropchance.containsKey(i)) { dchance =
	 * this.dropchance.get(i); } int chance = this.npc.world.rand.nextInt(100) +
	 * dchance; if (chance < 100) { continue; } list.add(item); } } return
	 * list.toArray(new IItemStack[list.size()]); }
	 */

	public IItemStack getArmor(int slot) {
		return this.armor.get(slot);
	}

	public ITextComponent getDisplayName() {
		return null;
	}

	public ICustomDrop getDrop(int slot) {
		if (slot < 0 || slot >= this.drops.size()) {
			throw new CustomNPCsException("Bad slot number: " + slot + " in " + this.drops.size() + " maximum",
					new Object[0]);
		}
		DropSet g = this.drops.get(slot);
		return (ICustomDrop) g;
	}

	// New
	public ICustomDrop[] getDrops() {
		ICustomDrop[] dss = new ICustomDrop[this.drops.size()];
		int i = 0;
		for (DropSet ds : this.drops.values()) {
			dss[i] = (ICustomDrop) ds;
			i++;
		}
		return dss;
	}

	public int getDropSlot(DropSet drop) {
		if (!this.drops.containsValue(drop)) {
			return -1;
		}
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot) == drop) {
				return slot;
			}
		}
		return -1;
	}

	public EntityItem getEntityItem(ItemStack itemstack) {
		if (itemstack == null || itemstack.isEmpty()) {
			return null;
		}
		EntityItem entityitem = new EntityItem(this.npc.world, this.npc.posX,
				this.npc.posY - 0.30000001192092896 + this.npc.getEyeHeight(), this.npc.posZ, itemstack);
		entityitem.setPickupDelay(40);
		float f2 = this.npc.getRNG().nextFloat() * 0.5f;
		float f3 = this.npc.getRNG().nextFloat() * 3.141593f * 2.0f;
		entityitem.motionX = -MathHelper.sin(f3) * f2;
		entityitem.motionZ = MathHelper.cos(f3) * f2;
		entityitem.motionY = 0.20000000298023224;
		return entityitem;
	}

	public int getExpMax() {
		return this.npc.inventory.maxExp;
	}

	public int getExpMin() {
		return this.npc.inventory.minExp;
	}

	public int getExpRNG() {
		int exp = this.minExp;
		if (this.maxExp - this.minExp > 0) {
			exp += this.npc.world.rand.nextInt(this.maxExp - this.minExp);
		}
		return exp;
	}

	public int getField(int id) {
		return 0;
	}

	public int getFieldCount() {
		return 0;
	}

	public int getInventoryStackLimit() {
		return 64;
	}

	public IItemStack[] getItemsRNG(EntityLivingBase attacking) {
		return createDrops(false, attacking);
	}

	public IItemStack[] getItemsRNGL(EntityLivingBase attacking) {
		return createDrops(true, attacking);
	}

	public IItemStack getLeftHand() {
		return this.weapons.get(2);
	}

	public int getMaxAmount() {
		return this.npc.inventory.maxAmount;
	}

	public String getName() {
		return "NPC Inventory";
	}

	public IItemStack getProjectile() {
		return this.weapons.get(1);
	}

	public IItemStack getRightHand() {
		return this.weapons.get(0);
	}

	public int getSizeInventory() {
		return /* 15 Changed */ 6;
	}

	public ItemStack getStackInSlot(int i) {
		if (i < 4) {
			return ItemStackWrapper.MCItem(this.getArmor(i));
		}
		if (i < 7) {
			return ItemStackWrapper.MCItem(this.weapons.get(i - 4));
		}
		// return ItemStackWrapper.MCItem(this.drops.get(i - 7)); Changed
		// New
		return ItemStackWrapper.MCItem(NpcAPI.Instance().getIItemStack(ItemStack.EMPTY));
	}

	public boolean getXPLootMode() {
		return this.lootMode;
	}

	public boolean hasCustomName() {
		return true;
	}

	public boolean isEmpty() {
		for (int slot = 0; slot < this.getSizeInventory(); ++slot) {
			ItemStack item = this.getStackInSlot(slot);
			if (!NoppesUtilServer.IsItemStackNull(item) && !item.isEmpty()) {
				return false;
			}
		}
		return true;
	}

	public boolean isItemValidForSlot(int i, ItemStack itemstack) {
		return true;
	}

	public boolean isUsableByPlayer(EntityPlayer var1) {
		return true;
	}

	public void markDirty() {
	}

	public void openInventory(EntityPlayer player) {
	}

	public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
		this.minExp = nbttagcompound.getInteger("MinExp");
		this.maxExp = nbttagcompound.getInteger("MaxExp");
		// this.drops = NBTTags.getIItemStackMap(nbttagcompound.getTagList("NpcInv",
		// 10)); Changed
		this.armor = NBTTags.getIItemStackMap(nbttagcompound.getTagList("Armor", 10));
		this.weapons = NBTTags.getIItemStackMap(nbttagcompound.getTagList("Weapons", 10));
		// this.dropchance =
		// NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("DropChance", 10));
		// Changed
		// this.lootMode = nbttagcompound.getInteger("LootMode"); Changed
		// New
		Map<Integer, DropSet> drs = new HashMap<Integer, DropSet>();
		if (nbttagcompound.hasKey("DropChance", 9)) { // if old items
			Map<Integer, IItemStack> d_old = NBTTags.getIItemStackMap(nbttagcompound.getTagList("NpcInv", 10));
			Map<Integer, Integer> dc_old = NBTTags.getIntegerIntegerMap(nbttagcompound.getTagList("DropChance", 10));
			for (int slot : d_old.keySet()) {
				if (dc_old.get(slot) <= 0) {
					continue;
				}
				DropSet ds = new DropSet(this);
				ds.item = d_old.get(slot);
				ds.chance = (double) dc_old.get(slot);
				ds.amount = new int[] { ds.item.getStackSize(), ds.item.getStackSize() };
				drs.put(slot, ds);
			}
		} else { // new data

			for (int i = 0; i < nbttagcompound.getTagList("NpcInv", 10).tagCount(); i++) {
				DropSet ds = new DropSet(this);
				ds.load(nbttagcompound.getTagList("NpcInv", 10).getCompoundTagAt(i));
				drs.put(nbttagcompound.getTagList("NpcInv", 10).getCompoundTagAt(i).getInteger("Slot"), ds);
			}
		}
		this.drops = drs;
		this.lootMode = nbttagcompound.getBoolean("LootMode");
		this.maxAmount = nbttagcompound.getInteger("MaxAmount");
	}

	public boolean removeDrop(ICustomDrop drop) {
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot) == (DropSet) drop) {
				this.drops.remove(slot);
				return true;
			}
		}
		return false;
	}

	public boolean removeDrop(int slot) {
		if (this.drops.containsKey(slot)) {
			this.drops.remove(slot);
			return true;
		}
		return false;
	}

	public ItemStack removeStackFromSlot(int par1) { // Changed
		int i = 0;
		Map<Integer, IItemStack> var2 = new HashMap<Integer, IItemStack>();
		// Changed
		/*
		 * Changed if (par1 >= 7) { var2 = this.drops; par1 -= 7; } else
		 */if (par1 >= 4) {
			var2 = this.weapons;
			par1 -= 4;
			i = 1;
		} else {
			var2 = this.armor;
			i = 2;
		}
		if (var2.get(par1) != null) {
			ItemStack var3 = var2.get(par1).getMCItemStack();
			var2.put(par1, null);
			if (i == 1) {
				this.weapons = var2;
			}
			if (i == 2) {
				this.armor = var2;
			}
			return var3;
		}
		return ItemStack.EMPTY;
	}

	public void setArmor(int slot, IItemStack item) {
		this.armor.put(slot, item);
		this.npc.updateClient = true;
	}

	public void setExp(int min, int max) {
		min = Math.min(min, max);
		this.npc.inventory.minExp = min;
		this.npc.inventory.maxExp = max;
	}

	public void setField(int id, int value) {
	}

	public void setInventorySlotContents(int par1, ItemStack par2ItemStack) { // Changed
		int i = 0;
		Map<Integer, IItemStack> var3 = new HashMap<Integer, IItemStack>();
		// Changed
		/*
		 * Changed if (par1 >= 7) { var3 = this.drops; par1 -= 7; } else
		 */if (par1 >= 4) {
			var3 = this.weapons;
			par1 -= 4;
			i = 1;
		} else {
			var3 = this.armor;
			i = 2;
		}
		var3.put(par1, NpcAPI.Instance().getIItemStack(par2ItemStack));
		if (i == 1) {
			this.weapons = var3;
		}
		if (i == 2) {
			this.armor = var3;
		}
	}

	public void setLeftHand(IItemStack item) {
		this.weapons.put(2, item);
		this.npc.updateClient = true;
	}

	public void setMaxAmount(int amount) {
		int newAmount = amount;
		if (amount < 0 || amount >= this.drops.size()) {
			newAmount = 0;
		}
		this.npc.inventory.maxAmount = newAmount;
	}

	public void setProjectile(IItemStack item) {
		this.weapons.put(1, item);
		this.npc.updateAI = true;
	}

	public void setRightHand(IItemStack item) {
		this.weapons.put(0, item);
		this.npc.updateClient = true;
	}

	public void setXPLootMode(boolean mode) {
		this.lootMode = mode;
	}

	public NBTTagCompound writeEntityToNBT(NBTTagCompound nbttagcompound) {
		nbttagcompound.setInteger("MinExp", this.minExp);
		nbttagcompound.setInteger("MaxExp", this.maxExp);
		// nbttagcompound.setTag("NpcInv", NBTTags.nbtIItemStackMap(this.drops));
		// Changed
		nbttagcompound.setTag("Armor", NBTTags.nbtIItemStackMap(this.armor));
		nbttagcompound.setTag("Weapons", NBTTags.nbtIItemStackMap(this.weapons));
		// nbttagcompound.setTag("DropChance",
		// NBTTags.nbtIntegerIntegerMap(this.dropchance)); Changed
		// nbttagcompound.setInteger("LootMode", this.lootMode); Changed
		// New
		NBTTagList dropList = new NBTTagList();
		for (int slot : this.drops.keySet()) {
			NBTTagCompound nbt = this.drops.get(slot).getNBT();
			nbt.setInteger("Slot", slot);
			dropList.appendTag(nbt);
		}
		nbttagcompound.setTag("NpcInv", dropList);
		nbttagcompound.setBoolean("LootMode", this.lootMode);
		nbttagcompound.setInteger("MaxAmount", this.maxAmount);
		return nbttagcompound;
	}

}
