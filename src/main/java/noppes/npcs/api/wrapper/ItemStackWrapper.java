package noppes.npcs.api.wrapper;

import java.util.*;
import java.util.Map.Entry;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBook;
import net.minecraft.item.ItemEnchantedBook;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.handler.capability.IItemStackWrapperHandler;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.data.Data;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemScripted;

import javax.annotation.Nonnull;

@SuppressWarnings("rawtypes")
public class ItemStackWrapper
implements IItemStackWrapperHandler, IItemStack, ICapabilityProvider, ICapabilitySerializable {

	protected static final EntityEquipmentSlot[] VALID_EQUIPMENT_SLOTS = new EntityEquipmentSlot[] { EntityEquipmentSlot.HEAD, EntityEquipmentSlot.CHEST, EntityEquipmentSlot.LEGS, EntityEquipmentSlot.FEET };
	protected static final ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "itemscripteddata");

	@CapabilityInject(IItemStackWrapperHandler.class)
	public static Capability<IItemStackWrapperHandler> ITEM_SCRIPTED_DATA_CAPABILITY = null;
	public static ItemStackWrapper AIR = new ItemStackWrapper(ItemStack.EMPTY);

	protected final Data tempdata;
	protected final Data storeddata;
	protected IEntity<?> owner = null;
	public ItemStack item;

	public ItemStackWrapper() { this(ItemStack.EMPTY); }

	private static ItemStackWrapper createNew(ItemStack item) {
		if (item == null || item.isEmpty()) {
			return ItemStackWrapper.AIR;
		}
		if (item.getItem() instanceof ItemScripted) {
			return new ItemScriptedWrapper(item);
		}
		if (item.getItem() == Items.WRITTEN_BOOK || item.getItem() == Items.WRITABLE_BOOK
				|| item.getItem() instanceof ItemWritableBook || item.getItem() instanceof ItemWrittenBook) {
			return new ItemBookWrapper(item);
		}
		if (item.getItem() instanceof ItemArmor) {
			return new ItemArmorWrapper(item);
		}
		Block block = Block.getBlockFromItem(item.getItem());
		if (block != Blocks.AIR) {
			return new ItemBlockWrapper(item);
		}
		return new ItemStackWrapper(item);
	}

	public static ItemStack MCItem(IItemStack item) {
		if (item == null) {
			return ItemStack.EMPTY;
		}
		return item.getMCItemStack();
	}

	public static void register(AttachCapabilitiesEvent<ItemStack> event) {
		ItemStackWrapper wrapper = createNew(event.getObject());
		event.addCapability(ItemStackWrapper.key, wrapper);
	}

	protected ItemStackWrapper(ItemStack stack) {
		if (stack.isEmpty()) {
			tempdata = null;
			storeddata = null;
		} else {
			tempdata = new Data();
			storeddata = new Data();
		}
		item = stack;
	}

	@Override
	public void addEnchantment(int id, int level) {
		Enchantment ench = Enchantment.getEnchantmentByID(id);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant id:" + id);
		}
		this.item.addEnchantment(ench, level);
	}

	@Override
	public void addEnchantment(String name, int strenght) {
		Enchantment ench = Enchantment.getEnchantmentByLocation(name);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant name:" + name);
		}
		this.item.addEnchantment(ench, strenght);
	}

	@Override
	public boolean compare(IItemStack item, boolean ignoreNBT) {
		if (item == null) {
			item = ItemStackWrapper.AIR;
		}
		return NoppesUtilPlayer.compareItems(this.getMCItemStack(), item.getMCItemStack(), false, ignoreNBT);
	}

	@Override
	public IItemStack copy() {
		return createNew(this.item.copy());
	}

	@Override
	public void damageItem(int damage, IEntityLiving living) {
		if (living == null) { return; }
		this.item.damageItem(damage, living.getMCEntity());
	}

	public void deserializeNBT(NBTBase nbt) {
		this.setMCNbt((NBTTagCompound) nbt);
	}

	@Override
	public double getAttackDamage() {
		HashMultimap map = (HashMultimap) this.item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
		Iterator iterator = map.entries().iterator();
		double damage = 0.0;
		while (iterator.hasNext()) {
			Map.Entry entry = (Entry) iterator.next();
			if (entry.getKey().equals(SharedMonsterAttributes.ATTACK_DAMAGE.getName())) {
				try {
					AttributeModifier mod = (AttributeModifier) entry.getValue();
					damage = mod.getAmount();
				}
				catch (Exception e) { LogWriter.error(e); }
			}
		}
		damage += EnchantmentHelper.getModifierForCreature(this.item, EnumCreatureAttribute.UNDEFINED);
		return damage;
	}

	@Override
	public double getAttribute(String name) {
		NBTTagCompound compound = this.item.getTagCompound();
		if (compound == null) {
			return 0.0;
		}
		Multimap<String, AttributeModifier> map = this.item.getAttributeModifiers(EntityEquipmentSlot.MAINHAND);
		for (Map.Entry<String, AttributeModifier> entry : map.entries()) {
			if (entry.getKey().equals(name)) {
				AttributeModifier mod = entry.getValue();
				return mod.getAmount();
			}
		}
		return 0.0;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public String getDisplayName() {
		return this.item.getDisplayName();
	}

	@Override
	public int getFoodLevel() {
		if (this.item.getItem() instanceof ItemFood) {
			return ((ItemFood) this.item.getItem()).getHealAmount(this.item);
		}
		return 0;
	}

	@Override
	public int getItemDamage() {
		return this.item.getItemDamage();
	}

	@Override
	public String getItemName() {
		return this.item.getItem().getItemStackDisplayName(this.item);
	}

	@Override
	public INbt getItemNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		this.item.writeToNBT(compound);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public String[] getLore() {
		NBTTagCompound compound = this.item.getSubCompound("display");
		if (compound == null || compound.getTagId("Lore") != 9) {
			return new String[0];
		}
		NBTTagList nbttaglist = compound.getTagList("Lore", 8);
		if (nbttaglist.tagCount() == 0) {
			return new String[0];
		}
		List<String> lore = new ArrayList<>();
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			lore.add(nbttaglist.getStringTagAt(i));
		}
		return lore.toArray(new String[0]);
	}

	@Override
	public int getMaxItemDamage() {
		return this.item.getMaxDamage();
	}

	@Override
	public int getMaxStackSize() {
		return this.item.getMaxStackSize();
	}

	@Override
	public ItemStack getMCItemStack() {
		return this.item;
	}

	public NBTTagCompound getMCNbt() {
		NBTTagCompound compound = new NBTTagCompound();
		if (storeddata != null) { compound.setTag("StoredData", storeddata.getNbt().getMCNBT()); }
		return compound;
	}

	@Override
	public String getName() {
		return Item.REGISTRY.getNameForObject(this.item.getItem()) + "";
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound compound = this.item.getTagCompound();
		if (compound == null) {
			this.item.setTagCompound(compound = new NBTTagCompound());
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public int getStackSize() {
		return this.item.getCount();
	}

	@Override
	public IData getStoreddata() {
		return this.storeddata;
	}

	@Override
	public IData getTempdata() {
		return this.tempdata;
	}

	@Override
	public int getType() {
		if (this.item.getItem() instanceof IPlantable) {
			return ItemType.SEEDS.get();
		}
		if (this.item.getItem() instanceof ItemSword) {
			return ItemType.SWORD.get();
		}
		return ItemType.NORMAL.get();
	}

	@Override
	public boolean hasAttribute(String name) {
		NBTTagCompound compound = this.item.getTagCompound();
		if (compound == null) {
			return false;
		}
		NBTTagList nbttaglist = compound.getTagList("AttributeModifiers", 10);
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound c = nbttaglist.getCompoundTagAt(i);
			if (c.getString("AttributeName").equals(name)) {
				return true;
			}
		}
		return false;
	}

	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == ItemStackWrapper.ITEM_SCRIPTED_DATA_CAPABILITY;
	}

	@Override
	public boolean hasCustomName() {
		return this.item.hasDisplayName();
	}

	@Override
	public boolean hasEnchant(int id) {
		Enchantment ench = Enchantment.getEnchantmentByID(id);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant id:" + id);
		}
		if (!this.isEnchanted()) {
			return false;
		}
		int enchId = Enchantment.getEnchantmentID(ench);
		NBTTagList list = this.item.getEnchantmentTagList();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			if (compound.getShort("id") == enchId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasEnchant(String name) {
		Enchantment ench = Enchantment.getEnchantmentByLocation(name);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant name:" + name);
		}
		if (!this.isEnchanted()) {
			return false;
		}
		int enchId = Enchantment.getEnchantmentID(ench);
		NBTTagList list = this.item.getEnchantmentTagList();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			if (compound.getShort("id") == enchId) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean hasNbt() {
		NBTTagCompound compound = this.item.getTagCompound();
		return compound != null && !compound.getKeySet().isEmpty();
	}

	@Override
	public boolean isBlock() {
		return Block.getBlockFromItem(this.item.getItem()) != Blocks.AIR;
	}

	@Override
	public boolean isBook() {
		return this.item.getItem() instanceof ItemBook || this.item.getItem() instanceof ItemEnchantedBook
				|| this.item.getItem() instanceof ItemWritableBook || this.item.getItem() instanceof ItemWrittenBook;
	}

	@Override
	public boolean isEmpty() {
		return item.isEmpty();
	}

	@Override
	public boolean isEnchanted() {
		return this.item.isItemEnchanted();
	}

	@Override
	public boolean isWearable() {
		for (EntityEquipmentSlot slot : ItemStackWrapper.VALID_EQUIPMENT_SLOTS) {
			if (this.item.getItem().isValidArmor(this.item, slot, EntityNPCInterface.CommandPlayer)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeEnchant(int id) {
		Enchantment ench = Enchantment.getEnchantmentByID(id);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant id:" + id);
		}
		if (!this.isEnchanted()) {
			return false;
		}
		int enchId = Enchantment.getEnchantmentID(ench);
		NBTTagList list = this.item.getEnchantmentTagList();
		NBTTagList newList = new NBTTagList();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			if (compound.getShort("id") != enchId) {
				newList.appendTag(compound);
			}
		}
		if (list.tagCount() == newList.tagCount()) {
			return false;
		}

		NBTTagCompound compound;
		if (this.item.hasTagCompound()) { compound = this.item.getTagCompound(); }
		else {
			compound = new NBTTagCompound();
			this.item.setTagCompound(compound);
		}
		if (compound != null) { compound.setTag("ench", newList); }
		return true;
	}

	@Override
	public boolean removeEnchant(String name) {
		Enchantment ench = Enchantment.getEnchantmentByLocation(name);
		if (ench == null) {
			throw new CustomNPCsException("Unknown enchant name:" + name);
		}
		if (!this.isEnchanted()) {
			return false;
		}
		int enchId = Enchantment.getEnchantmentID(ench);
		NBTTagList list = this.item.getEnchantmentTagList();
		NBTTagList newList = new NBTTagList();
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound compound = list.getCompoundTagAt(i);
			if (compound.getShort("id") != enchId) {
				newList.appendTag(compound);
			}
		}
		if (list.tagCount() == newList.tagCount()) {
			return false;
		}
		NBTTagCompound compound;
		if (this.item.hasTagCompound()) { compound = this.item.getTagCompound(); }
		else {
			compound = new NBTTagCompound();
			this.item.setTagCompound(compound);
		}
		if (compound != null) { compound.setTag("ench", newList); }
		return true;
	}

	@Override
	public void removeNbt() {
		this.item.setTagCompound(null);
	}

	public NBTBase serializeNBT() {
		return getMCNbt();
	}

	@Override
	public void setAttribute(String name, double value) {
		this.setAttribute(name, value, -1);
	}

	@Override
	public void setAttribute(String name, double value, int slot) {
		if (slot < -1 || slot > 5) {
			throw new CustomNPCsException("Slot has to be between -1 and 5, given was: " + slot);
		}
		NBTTagCompound compound = this.item.getTagCompound();
		if (compound == null) {
			this.item.setTagCompound(compound = new NBTTagCompound());
		}
		NBTTagList nbttaglist = compound.getTagList("AttributeModifiers", 10);
		NBTTagList newList = new NBTTagList();
		for (int i = 0; i < nbttaglist.tagCount(); ++i) {
			NBTTagCompound c = nbttaglist.getCompoundTagAt(i);
			if (!c.getString("AttributeName").equals(name)) {
				newList.appendTag(c);
			}
		}
		if (value != 0.0) {
			NBTTagCompound nbttagcompound = SharedMonsterAttributes
					.writeAttributeModifierToNBT(new AttributeModifier(name, value, 0));
			nbttagcompound.setString("AttributeName", name);
			if (slot >= 0) {
				nbttagcompound.setString("Slot", EntityEquipmentSlot.values()[slot].getName());
			}
			newList.appendTag(nbttagcompound);
		}
		compound.setTag("AttributeModifiers", newList);
	}

	@Override
	public void setCustomName(String name) {
		this.item.setStackDisplayName(name);
	}

	@Override
	public void setItemDamage(int value) {
		this.item.setItemDamage(value);
	}

	@Override
	public void setLore(String[] lore) {
		NBTTagCompound compound = this.item.getOrCreateSubCompound("display");
		if (lore == null || lore.length == 0) {
			compound.removeTag("Lore");
			return;
		}
		NBTTagList nbtList = new NBTTagList();
		for (String s : lore) {
			nbtList.appendTag(new NBTTagString(s));
		}
		compound.setTag("Lore", nbtList);
	}

	public void setMCNbt(NBTTagCompound compound) {
		if (storeddata == null) { return; }
		if (compound == null) {
			storeddata.clear();
		} else {
			storeddata.setNbt(compound.getCompoundTag("StoredData"));
		}
	}

	@Override
	public void setStackSize(int size) {
		if (size > this.getMaxStackSize()) {
			throw new CustomNPCsException("Can't set the stack size bigger than Max Stack size");
		}
		this.item.setCount(size);
	}

	// New from Unofficial (BetaZavr)
	@Override
	public IEntity<?> getOwner() { return owner; }

	@Override
	public void setOwner(IEntity<?> iEntity) { owner = iEntity; }

}
