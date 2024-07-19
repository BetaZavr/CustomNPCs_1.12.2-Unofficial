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
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ICustomElement;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.constants.CustomBlockTypes;
import noppes.npcs.items.CustomItem;
import noppes.npcs.util.AdditionalMethods;

import javax.annotation.Nonnull;
import java.util.Objects;

public abstract class CustomBlockSlab extends BlockSlab implements ICustomElement {

	public static class CustomBlockSlabDouble extends CustomBlockSlab {

		public CustomBlockSlabSingle singleBlock;

		public CustomBlockSlabDouble(NBTTagCompound nbtBlock) {
			super(nbtBlock);
			String name = "custom_double_" + nbtBlock.getString("RegistryName");
			this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
			this.setUnlocalizedName(name.toLowerCase());
		}

		public void getSubBlocks(@Nonnull CreativeTabs itemIn, @Nonnull NonNullList<ItemStack> items) {
		}

		@Override
		public boolean isDouble() {
			return true;
		}

		public void setSingle(CustomBlockSlabSingle block) {
			this.singleBlock = block;
		}

	}

	public static class CustomBlockSlabSingle extends CustomBlockSlab {

		public CustomBlockSlabDouble doubleBlock;

		public CustomBlockSlabSingle(NBTTagCompound nbtBlock, CustomBlockSlabDouble addblock) {
			super(nbtBlock);
			String name = "custom_" + nbtBlock.getString("RegistryName");
			this.setRegistryName(CustomNpcs.MODID, name.toLowerCase());
			this.setUnlocalizedName(name.toLowerCase());
			this.doubleBlock = addblock;
		}

		@Override
		public void getSubBlocks(@Nonnull CreativeTabs tab, @Nonnull NonNullList<ItemStack> items) {
			if (tab != CustomRegisters.tabBlocks && tab != CreativeTabs.SEARCH) { return; }
			if (this.nbtData != null && this.nbtData.hasKey("ShowInCreative", 1)
					&& !this.nbtData.getBoolean("ShowInCreative")) {
				return;
			}
			items.add(new ItemStack(this, 1, 0));
			if (tab == CustomRegisters.tabBlocks) { AdditionalMethods.instance.sort(items); }
		}

		@Override
		public boolean getUseNeighborBrightness(@Nonnull IBlockState state) {
			return true;
		}

		@Override
		public boolean isDouble() {
			return false;
		}
	}

	public static final PropertyEnum<CustomBlockTypes.TreeType> VARIANT = PropertyEnum.create("type",
			CustomBlockTypes.TreeType.class);
	protected NBTTagCompound nbtData;

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

		if (nbtBlock.hasKey("Hardness", 5)) {
			this.setHardness(nbtBlock.getFloat("Hardness"));
		}
		if (nbtBlock.hasKey("Resistance", 5)) {
			this.setResistance(nbtBlock.getFloat("Resistance"));
		}
		if (nbtBlock.hasKey("LightLevel", 5)) {
			this.setLightLevel(nbtBlock.getFloat("LightLevel"));
		}

		this.setSoundType(CustomBlock.getNbtSoundType(nbtBlock.getString("SoundType")));
		this.setCreativeTab(CustomRegisters.tabBlocks);
	}

	protected @Nonnull BlockStateContainer createBlockState() {
		return this.isDouble() ? new BlockStateContainer(this, VARIANT) : new BlockStateContainer(this, HALF, VARIANT);
	}

	public int damageDropped(@Nonnull IBlockState state) {
		return state.getValue(VARIANT).ordinal();
	}

	@Override
	public String getCustomName() {
		return this.nbtData.getString("RegistryName");
	}

	@Override
	public INbt getCustomNbt() {
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(this.nbtData);
	}

	public @Nonnull MapColor getMapColor(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return state.getValue(VARIANT).getMapColor();
	}

	public int getMetaFromState(@Nonnull IBlockState state) {
		int i = 0;
		i = i | state.getValue(VARIANT).ordinal();
		if (!isDouble() && state.getValue(HALF) == BlockSlab.EnumBlockHalf.TOP) {
			i |= 8;
		}
		return i;
	}

	@Override
	public @Nonnull ItemStack getPickBlock(@Nonnull IBlockState state, @Nonnull RayTraceResult target, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player) {
        return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	public @Nonnull IBlockState getStateForPlacement(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing facing, float hitX, float hitY, float hitZ, int meta, @Nonnull EntityLivingBase placer) {
		if (this.isDouble()) {
			return this.getDefaultState();
		}
		IBlockState iblockstate = super.getStateForPlacement(worldIn, pos, facing, hitX, hitY, hitZ, meta, placer)
				.withProperty(HALF, BlockSlab.EnumBlockHalf.BOTTOM);
		return facing != EnumFacing.DOWN && (facing == EnumFacing.UP || (double) hitY <= 0.5D) ? iblockstate
				: iblockstate.withProperty(HALF, BlockSlab.EnumBlockHalf.TOP);
	}

	public @Nonnull IBlockState getStateFromMeta(int meta) {
		IBlockState iblockstate = getDefaultState();
		try {
			iblockstate = iblockstate.withProperty(VARIANT, CustomBlockTypes.TreeType.values()[meta & 7]);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		if (!isDouble()) {
			iblockstate = iblockstate.withProperty(HALF,
					(meta & 8) == 0 ? BlockSlab.EnumBlockHalf.BOTTOM : BlockSlab.EnumBlockHalf.TOP);
		}
		return iblockstate;
	}

	public @Nonnull Comparable<?> getTypeForItem(@Nonnull ItemStack stack) {
		return CustomBlockTypes.TreeType.values()[stack.getItemDamage() & 7];
	}

	@Override
	public @Nonnull String getUnlocalizedName(int meta) {
		return this.getUnlocalizedName() + this.getStateFromMeta(meta).getValue(VARIANT).getName().toLowerCase();
	}

	public @Nonnull IProperty<?> getVariantProperty() {
		return VARIANT;
	}

	@Override
	public int getType() {
		if (this.nbtData != null && this.nbtData.hasKey("BlockType", 1)) { return this.nbtData.getByte("BlockType"); }
		return 4;
	}

}
