package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.collect.Maps;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.ICustomDrop;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class DataInventory
implements IInventory,
INPCInventory {
	
	public Map<Integer, IItemStack> armor;
	public Map<Integer, DropSet> drops;
	// New
	public boolean lootMode;
	private int maxAmount;

	private int maxExp;

	private int minExp;

	// public int lootMode; Changed
	EntityNPCInterface npc;

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
		this.drops = new TreeMap<Integer, DropSet>();
		this.lootMode = true;
		this.maxAmount = 0;
	}

	public ICustomDrop addDropItem(IItemStack item, double chance) {
		if (this.drops.size() >= 32) {
			throw new CustomNPCsException("Bad maximum size: " + this.drops.size() + " (32 slots maximum)");
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
		Map<Integer, DropSet> newDrop = Maps.newTreeMap();
		boolean del = false;
		int j = 0;
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot) == (DropSet) drop) { del = true; continue; }
			newDrop.put(j, this.drops.get(slot));
			j++;
		}
		if (del) { this.drops = newDrop; }
		return del;
	}

	public boolean removeDrop(int slot) {
		if (this.drops.containsKey(slot)) {
			this.drops.remove(slot);
			Map<Integer, DropSet> newDrop = Maps.newTreeMap();
			int j = 0;
			for (int s : this.drops.keySet()) {
				if (s==slot) { continue; }
				newDrop.put(j, this.drops.get(slot));
				j++;
			}
			this.drops = newDrop;
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
		int j = 0;
		for (int slot : this.drops.keySet()) {
			if (this.drops.get(slot)==null) { continue; }
			NBTTagCompound nbt = this.drops.get(slot).getNBT();
			nbt.setInteger("Slot", j);
			dropList.appendTag(nbt);
			j++;
		}
		nbttagcompound.setTag("NpcInv", dropList);
		nbttagcompound.setBoolean("LootMode", this.lootMode);
		nbttagcompound.setInteger("MaxAmount", this.maxAmount);
		return nbttagcompound;
	}

}
