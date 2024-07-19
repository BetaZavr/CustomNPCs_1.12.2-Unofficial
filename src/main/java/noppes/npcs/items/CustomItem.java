package noppes.npcs.items;

import java.util.Objects;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.IPermission;

import javax.annotation.Nonnull;

public class CustomItem extends Item implements IPermission, ICustomElement {

	public static Material getMaterial(String materialName) {
		switch (materialName.toLowerCase()) {
		case "air":
			return Material.AIR;
		case "grass":
			return Material.GRASS;
		case "ground":
			return Material.GROUND;
		case "wood":
			return Material.WOOD;
		case "iron":
			return Material.IRON;
		case "anvil":
			return Material.ANVIL;
		case "water":
			return Material.WATER;
		case "lava":
			return Material.LAVA;
		case "leaves":
			return Material.LEAVES;
		case "plants":
			return Material.PLANTS;
		case "vine":
			return Material.VINE;
		case "sponge":
			return Material.SPONGE;
		case "cloth":
			return Material.CLOTH;
		case "fire":
			return Material.FIRE;
		case "sand":
			return Material.SAND;
		case "circuits":
			return Material.CIRCUITS;
		case "carpet":
			return Material.CARPET;
		case "glass":
			return Material.GLASS;
		case "redstone_light":
			return Material.REDSTONE_LIGHT;
		case "tnt":
			return Material.TNT;
		case "coral":
			return Material.CORAL;
		case "ice":
			return Material.ICE;
		case "packed_ice":
			return Material.PACKED_ICE;
		case "snow":
			return Material.SNOW;
		case "crafted_snow":
			return Material.CRAFTED_SNOW;
		case "cactus":
			return Material.CACTUS;
		case "clay":
			return Material.CLAY;
		case "gourd":
			return Material.GOURD;
		case "dragon_egg":
			return Material.DRAGON_EGG;
		case "portal":
			return Material.PORTAL;
		case "cake":
			return Material.CAKE;
		case "web":
			return Material.WEB;
		case "piston":
			return Material.PISTON;
		case "barrier":
			return Material.BARRIER;
		case "structure_void":
			return Material.STRUCTURE_VOID;
		default:
			return Material.ROCK;
		}
	}

	public static Item.ToolMaterial getMaterialTool(NBTTagCompound nbtItem) {
		String materialName = nbtItem.hasKey("Material", 8) ? nbtItem.getString("Material").toLowerCase() : "stone";
		switch (materialName) {
		case "wood":
			return Item.ToolMaterial.WOOD;
		case "iron":
			return Item.ToolMaterial.IRON;
		case "diamond":
			return Item.ToolMaterial.DIAMOND;
		case "gold":
			return Item.ToolMaterial.GOLD;
		default:
			return Item.ToolMaterial.STONE;
		}
	}
	protected NBTTagCompound nbtData;
	protected int enchantability = 10;

	protected ItemStack repairItemStack = ItemStack.EMPTY;
	protected Item.ToolMaterial toolMaterial;
	protected Material collectionMaterial = null;

	protected float speedCollectionMaterial = 1.0f;
	protected float efficiency = 1.0f;
	protected Set<Block> effectiveBlocks = Sets.newHashSet();

	protected double attackDamage = 0.0f;

	protected double attackSpeed = -2.4d;

	public CustomItem(NBTTagCompound nbtItem) {
		this.nbtData = nbtItem;
		this.setRegistryName(CustomNpcs.MODID, "custom_" + nbtItem.getString("RegistryName"));
		this.setUnlocalizedName("custom_" + nbtItem.getString("RegistryName"));
		this.maxStackSize = nbtItem.hasKey("MaxStackSize", 3) ? nbtItem.getInteger("MaxStackSize") : 64;
		if (this.maxStackSize > 64) {
			this.maxStackSize = 64;
		}

		if (nbtItem.hasKey("SpeedAttack", 6)) {
			this.attackSpeed = nbtItem.getDouble("SpeedAttack");
		}
		if (nbtItem.hasKey("EntityDamage", 6)) {
			this.attackDamage = nbtItem.getDouble("EntityDamage");
		}
		if (nbtItem.hasKey("Efficiency", 5)) {
			this.efficiency = nbtItem.getFloat("Efficiency");
		}
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
		if (nbtItem.hasKey("Enchantability", 3)) {
			this.enchantability = nbtItem.getInteger("Enchantability");
		}
		if (nbtItem.hasKey("RepairItem", 10)) {
			this.repairItemStack = new ItemStack(nbtItem.getCompoundTag("RepairItem"));
		}
		this.toolMaterial = CustomItem.getMaterialTool(nbtItem);
		if (nbtItem.hasKey("CollectionBlocks", 9)) {
			for (int j = 0; j < nbtItem.getTagList("CollectionBlocks", 8).tagCount(); j++) {
				Block block = Block.getBlockFromName(nbtItem.getTagList("CollectionBlocks", 8).getStringTagAt(j));
				if (block != null) {
					this.effectiveBlocks.add(block);
				}
			}
		}
		this.setCreativeTab(CustomRegisters.tabItems);
		this.setHasSubtypes(true);
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

	@Override
	public double getDurabilityForDisplay(@Nonnull ItemStack stack) {
		if (this.nbtData != null && this.nbtData.hasKey("DurabilityValue", 6)) {
			return 1.0 - this.nbtData.getDouble("DurabilityValue");
		}
		return super.getDurabilityForDisplay(stack);
	}

	public void getSubItems(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
		if (tab != CustomRegisters.tabItems && tab != CreativeTabs.SEARCH) { return; }
		if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1) && !this.nbtData.getBoolean("ShowInCreative")) { return; }
		items.add(new ItemStack(this));
		if (tab == CustomRegisters.tabItems) { AdditionalMethods.instance.sort(items); }
	}

	@Override
	public boolean isAllowed(EnumPacketServer enumPacket) {
		return true;
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("ItemType", 1)) { return this.nbtData.getByte("ItemType"); }
		return 9;
	}

}
