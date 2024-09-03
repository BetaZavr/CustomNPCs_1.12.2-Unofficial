package noppes.npcs.items;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.Util;
import noppes.npcs.util.ObfuscationHelper;

import javax.annotation.Nonnull;

public class CustomWeapon extends ItemSword implements ICustomElement {

	protected NBTTagCompound nbtData;
	protected Set<Block> effectiveBlocks = Sets.newHashSet();
	protected float efficiency = 1.0f;
	protected Material collectionMaterial = null;
	protected float speedCollectionMaterial = 1.0f;
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;

	private double attackSpeed = -2.4000000953674316D;

	public CustomWeapon(Item.ToolMaterial material, NBTTagCompound nbtItem) {
		super(material);
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_" + nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_" + nbtItem.getString("RegistryName"));
		if (nbtItem.hasKey("SpeedAttack", 6)) {
			this.attackSpeed = nbtItem.getDouble("SpeedAttack");
		}
		if (nbtItem.hasKey("EntityDamage", 6)) {
			ObfuscationHelper.setValue(ItemSword.class, this, (float) nbtItem.getDouble("EntityDamage"), float.class);
		}
		if (nbtItem.getInteger("MaxStackDamage") > 1) {
			this.setMaxDamage(nbtItem.getInteger("MaxStackDamage"));
		}

		if (nbtItem.hasKey("CollectionMaterial", 10)) {
			this.collectionMaterial = CustomItem
					.getMaterial(nbtItem.getCompoundTag("collectionMaterial").getString("Material"));
			this.speedCollectionMaterial = nbtItem.getCompoundTag("collectionMaterial").getFloat("Speed");
		}
		if (nbtItem.hasKey("CollectionBlocks", 9)) {
			for (int i = 0; i < nbtItem.getTagList("CollectionBlocks", 8).tagCount(); i++) {
				Block block = Block.getBlockFromName(nbtItem.getTagList("CollectionBlocks", 8).getStringTagAt(i));
				if (block != null) {
					this.effectiveBlocks.add(block);
				}
			}
		}
		if (nbtItem.hasKey("Efficiency", 5)) {
			this.efficiency = nbtItem.getFloat("Efficiency");
		}
		if (nbtItem.hasKey("RepairItem", 10)) {
			this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem"));
		} else {
			this.repairItemStack = material.getRepairItemStack();
		}
		if (nbtItem.hasKey("Enchantability", 3)) {
			this.enchantability = nbtItem.getInteger("Enchantability");
		}
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) {
			this.setFull3D();
		}

		this.setCreativeTab(CustomRegisters.tabItems);
	}

	public boolean canHarvestBlock(@Nonnull IBlockState blockIn) {
		return blockIn.getBlock() == Blocks.WEB
				|| (this.collectionMaterial != null && blockIn.getMaterial() == this.collectionMaterial);
	}

	public float getAttackDamage() {
		return ObfuscationHelper.getValue(ItemSword.class, this, float.class);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	public float getDestroySpeed(@Nonnull ItemStack stack, @Nonnull IBlockState state) {
		if (state.getMaterial() == this.collectionMaterial) {
			return this.speedCollectionMaterial;
		} else if (this.effectiveBlocks.contains(state.getBlock())) {
			for (String type : getToolClasses(stack)) {
				if (state.getBlock().isToolEffective(type, state)) {
					return this.efficiency;
				}
			}
			return this.efficiency;
		} else if (state.getBlock() == Blocks.WEB) {
			return 15.0F;
		}
		return 1.0f;
	}

	public boolean getIsRepairable(@Nonnull ItemStack toRepair, @Nonnull ItemStack repair) {
		ItemStack mat = this.repairItemStack;
		if (this.repairItemStack.isEmpty()) {
			mat = ObfuscationHelper.getValue(ItemSword.class, this, Item.ToolMaterial.class).getRepairItemStack();
		}
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
			return true;
		}
		return super.getIsRepairable(toRepair, repair);
	}

	@Override
	public @Nonnull Multimap<String, AttributeModifier> getItemAttributeModifiers(@Nonnull EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(), new AttributeModifier(ATTACK_DAMAGE_MODIFIER,
					"Weapon modifier", (double) ObfuscationHelper.getValue(ItemSword.class, this, float.class), 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
					new AttributeModifier(ATTACK_SPEED_MODIFIER, "Weapon modifier", this.attackSpeed, 0));
		}
		return multimap;
	}

	public int getItemEnchantability() {
		if (this.enchantability > 0) {
			return this.enchantability;
		}
		return super.getItemEnchantability();
	}

	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabItems) { Util.instance.sort(items); }
	}

	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return this.bFull3D;
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return this.nbtData.getByte("BlockType"); }
		return 1;
	}

}
