package noppes.npcs.blocks;

import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.constants.CustomBlockTypes;
import noppes.npcs.items.CustomItem;

public abstract class CustomBlockSlab
extends BlockSlab
implements ICustomElement {
	
	public static class CustomBlockSlabDouble extends CustomBlockSlab {

		public CustomBlockSlabSingle singleBlock;
		
		public CustomBlockSlabDouble(NBTTagCompound nbtBlock) {
			super(nbtBlock);
			String name = "custom_double_"+nbtBlock.getString("RegistryName");
			this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
			this.setUnlocalizedName(name.toLowerCase());
		}

		@Override
		public boolean isDouble() { return true; }

		public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) { }
		
		public void setSingle(CustomBlockSlabSingle block) {
			this.singleBlock = block;
		}
		
	}

	public static class CustomBlockSlabSingle extends CustomBlockSlab {
		
		public CustomBlockSlabDouble doubleBlock;
		
		public CustomBlockSlabSingle(NBTTagCompound nbtBlock, CustomBlockSlabDouble addblock) {
			super(nbtBlock);
			String name = "custom_"+nbtBlock.getString("RegistryName");
			this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
			this.setUnlocalizedName(name.toLowerCase());
			this.doubleBlock = addblock;
		}

		@Override
		public boolean isDouble() {
			return false;
		}

		@Override
		public void getSubBlocks(CreativeTabs itemIn, NonNullList<ItemStack> items) {
			items.add(new ItemStack(this, 1, 0));
		}

		@Override
		public boolean getUseNeighborBrightness(IBlockState state) {
			return true;
		}
	}

	private NBTTagCompound nbtData;
	public static final PropertyEnum<CustomBlockTypes.TreeType> VARIANT = PropertyEnum.create("type", CustomBlockTypes.TreeType.class);
	
	public CustomBlockSlab(NBTTagCompound nbtBlock) {
		super(CustomItem.getMaterial(nbtBlock.getString("Material")));
		this.useNeighborBrightness = !this.isDouble();

		IBlockState iblockstate = this.blockState.getBaseState();

		if (!this.isDouble()) {
			iblockstate = iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		}
		this.setDefaultState(iblockstate.withProperty(VARIANT, CustomBlockTypes.TreeType.NORMAL));
		
		this.nbtData = nbtBlock;
		
		this.enableStats = true;
		this.blockSoundType = SoundType.STONE;
		this.blockParticleGravity = 1.0F;
		this.lightOpacity = this.fullBlock ? 255 : 0;
		this.translucent = !this.blockMaterial.blocksLight();
		this.setHardness(0.0f);
		this.setResistance(10.0f);

		if (nbtBlock.hasKey("Hardness", 5)) { this.setHardness(nbtBlock.getFloat("Hardness")); }
		if (nbtBlock.hasKey("Resistance", 5)) { this.setResistance(nbtBlock.getFloat("Resistance")); }
		if (nbtBlock.hasKey("LightLevel", 5)) { this.setLightLevel(nbtBlock.getFloat("LightLevel")); }
		
		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setCreativeTab((CreativeTabs) CustomItems.tabBlocks);
	}

	@Override
	public String getUnlocalizedName(int meta) {
		return this.getUnlocalizedName() + this.getStateFromMeta(meta).getValue(VARIANT).getName().toLowerCase();
	}

	public MapColor getMapColor(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
		return state.getValue(VARIANT).getMapColor();
	}

	public IProperty<?> getVariantProperty() { return VARIANT; }

	public Comparable<?> getTypeForItem(ItemStack stack) {
		return CustomBlockTypes.TreeType.values()[stack.getItemDamage() & 7];
	}

	public IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = getDefaultState().withProperty(VARIANT, CustomBlockTypes.TreeType.values()[meta & 7]);
		if (!isDouble()) {
			iblockstate = iblockstate.withProperty(HALF, (meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
		}
		return iblockstate;
	}

	public int getMetaFromState(IBlockState state) {
		int i = 0;
		i = i | state.getValue(VARIANT).ordinal();
		if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) { i |= 8; }
		return i;
	}
	
	protected BlockStateContainer createBlockState() {
		return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
	}

	public int damageDropped(IBlockState state) { return state.getValue(VARIANT).ordinal(); }

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
		ItemStack stack = super.getPickBlock(state, target, world, pos, player);
		return stack;
	}

	@Override
	public IBlockState getStateForPlacement(World worldIn, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer) {
		if (this.isDouble()) { return this.getDefaultState(); }
		IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer).withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double)hitY <= 0.5D) ? iblockstate : iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP);
	}
	
	@Override
	public INbt getCustomNbt() { return NpcAPI.Instance().getINbt(this.nbtData); }

	@Override
	public String getCustomName() { return this.nbtData.getString("RegistryName"); }
	
}
