package noppes.npcs.items;

import java.util.Set;

import com.google.common.collect.Multimap;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.util.ObfuscationHelper;

public class CustomTool extends ItemTool implements ICustomElement {

	protected NBTTagCompound nbtData = new NBTTagCompound();
	protected Material collectionMaterial = null;
	protected float speedCollectionMaterial = 1.0f;
	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected int enchantability = 0;

	protected int harvestLevel = 0;

	public CustomTool(float attackDamageIn, float attackSpeedIn, Item.ToolMaterial materialIn,
			Set<Block> effectiveBlocksIn, NBTTagCompound nbtItem) {
		super(attackDamageIn, attackSpeedIn, materialIn, effectiveBlocksIn);
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_" + nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_" + nbtItem.getString("RegistryName"));
		if (nbtItem.hasKey("IsFull3D", 1) && nbtItem.getBoolean("IsFull3D")) {
			this.setFull3D();
		}
		if (nbtItem.getInteger("MaxStackDamage") > 1) {
			this.setMaxDamage(nbtItem.getInteger("MaxStackDamage"));
		}
		if (nbtItem.hasKey("CollectionMaterial", 10)) {
			this.collectionMaterial = CustomItem
					.getMaterial(nbtItem.getCompoundTag("collectionMaterial").getString("Material"));
			this.speedCollectionMaterial = nbtItem.getCompoundTag("collectionMaterial").getFloat("Speed");
		}
		if (nbtItem.hasKey("Efficiency", 5)) {
			this.efficiency = nbtItem.getFloat("Efficiency");
		}
		if (nbtItem.hasKey("RepairItem", 10)) {
			this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem"));
		} else {
			this.repairItemStack = materialIn.getRepairItemStack();
		}
		if (nbtItem.hasKey("Enchantability", 3)) {
			this.enchantability = nbtItem.getInteger("Enchantability");
		}

		if (nbtItem.hasKey("EntityDamage", 6)) {
			this.attackDamage = (float) nbtItem.getDouble("EntityDamage");
		}
		if (nbtItem.hasKey("HarvestLevel", 3)) {
			this.harvestLevel = nbtItem.getInteger("HarvestLevel");
		}
		if (nbtItem.hasKey("ToolClass", 8)) {
			ObfuscationHelper.setValue(ItemTool.class, this, nbtItem.getString("ToolClass"), String.class);
		}
		this.setCreativeTab((CreativeTabs) CustomRegisters.tabItems);
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return NpcAPI.Instance().getINbt(this.nbtData);
	}

	public float getDestroySpeed(ItemStack stack, IBlockState state) {
		if (state.getMaterial() == this.collectionMaterial) {
			return this.speedCollectionMaterial;
		}
		return super.getDestroySpeed(stack, state);
	}

	@Override
	public int getHarvestLevel(ItemStack stack, String toolClass,
			@javax.annotation.Nullable net.minecraft.entity.player.EntityPlayer player,
			@javax.annotation.Nullable IBlockState blockState) {
		if (this.harvestLevel > -1) {
			return this.harvestLevel;
		}
		return super.getHarvestLevel(stack, toolClass, player, blockState);
	}

	public boolean getIsRepairable(ItemStack toRepair, ItemStack repair) {
		ItemStack mat = this.repairItemStack;
		if (this.repairItemStack.isEmpty()) {
			mat = this.toolMaterial.getRepairItemStack();
		}
		if (!mat.isEmpty() && net.minecraftforge.oredict.OreDictionary.itemMatches(mat, repair, false)) {
			return true;
		}
		return super.getIsRepairable(toRepair, repair);
	}

	public Multimap<String, AttributeModifier> getItemAttributeModifiers(EntityEquipmentSlot equipmentSlot) {
		Multimap<String, AttributeModifier> multimap = super.getItemAttributeModifiers(equipmentSlot);
		if (equipmentSlot == EntityEquipmentSlot.MAINHAND) {
			multimap.put(SharedMonsterAttributes.ATTACK_DAMAGE.getName(),
					new AttributeModifier(ATTACK_DAMAGE_MODIFIER, "Tool modifier", this.attackDamage, 0));
			multimap.put(SharedMonsterAttributes.ATTACK_SPEED.getName(),
					new AttributeModifier(ATTACK_SPEED_MODIFIER, "Tool modifier", this.attackSpeed, 0));
		}
		return multimap;
	}

	public int getItemEnchantability() {
		if (this.enchantability > 0) {
			return this.enchantability;
		}
		return super.getItemEnchantability();
	}

	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems || (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
				&& !this.nbtData.getBoolean("ShowInCreative"))) {
			return;
		}
		items.add(new ItemStack(this));
	}

	@SideOnly(Side.CLIENT)
	public boolean isFull3D() {
		return this.bFull3D;
	}

}
