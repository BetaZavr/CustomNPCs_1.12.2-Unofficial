package noppes.npcs.blocks;

import java.util.Objects;
import java.util.Random;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockFaceShape;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Mirror;
import net.minecraft.util.NonNullList;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.blocks.tiles.TileEntityCustomBanner;

public class BlockCustomBanner extends BlockBanner {

	public static class BlockBannerHanging extends BlockCustomBanner {

		protected static final AxisAlignedBB NORTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.875D, 1.0D, 0.78125D, 1.0D);
		protected static final AxisAlignedBB SOUTH_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 1.0D, 0.78125D, 0.125D);
		protected static final AxisAlignedBB WEST_AABB = new AxisAlignedBB(0.875D, 0.0D, 0.0D, 1.0D, 0.78125D, 1.0D);
		protected static final AxisAlignedBB EAST_AABB = new AxisAlignedBB(0.0D, 0.0D, 0.0D, 0.125D, 0.78125D, 1.0D);

		public BlockBannerHanging(BlockBanner parent) {
			super(parent);
			this.setDefaultState(this.blockState.getBaseState().withProperty(FACING, EnumFacing.NORTH));
		}

		protected @Nonnull BlockStateContainer createBlockState() {
			return new BlockStateContainer(this, FACING);
		}

		public @Nonnull AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
			switch (state.getValue(FACING)) {
				case NORTH:
				default:
					return NORTH_AABB;
				case SOUTH:
					return SOUTH_AABB;
				case WEST:
					return WEST_AABB;
				case EAST:
					return EAST_AABB;
			}
		}

		public int getMetaFromState(@Nonnull IBlockState state) {
			return state.getValue(FACING).getIndex();
		}

		public @Nonnull IBlockState getStateFromMeta(int meta) {
			EnumFacing enumfacing = EnumFacing.getFront(meta);
			if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
				enumfacing = EnumFacing.NORTH;
			}
			return this.getDefaultState().withProperty(FACING, enumfacing);
		}

		@SuppressWarnings("deprecation")
		public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
			EnumFacing enumfacing = state.getValue(FACING);
			if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getMaterial().isSolid()) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
			super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		}

		public @Nonnull IBlockState withMirror(@Nonnull IBlockState state, @Nonnull Mirror mirrorIn) {
			return state.withRotation(mirrorIn.toRotation(state.getValue(FACING)));
		}

		public @Nonnull IBlockState withRotation(@Nonnull IBlockState state, @Nonnull Rotation rot) {
			return state.withProperty(FACING, rot.rotate(state.getValue(FACING)));
		}

	}

	public static class BlockBannerStanding extends BlockCustomBanner {

		public BlockBannerStanding(BlockBanner parent) {
			super(parent);
			this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, 0));
		}

		protected @Nonnull BlockStateContainer createBlockState() {
			return new BlockStateContainer(this, ROTATION);
		}

		public @Nonnull AxisAlignedBB getBoundingBox(@Nonnull IBlockState state, @Nonnull IBlockAccess source, @Nonnull BlockPos pos) {
			return STANDING_AABB;
		}

		public int getMetaFromState(@Nonnull IBlockState state) {
			return state.getValue(ROTATION);
		}

		public @Nonnull IBlockState getStateFromMeta(int meta) {
			return this.getDefaultState().withProperty(ROTATION, meta);
		}

		@SuppressWarnings("deprecation")
		public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block blockIn, @Nonnull BlockPos fromPos) {
			if (!worldIn.getBlockState(pos.down()).getMaterial().isSolid()) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
			super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		}

		public @Nonnull IBlockState withMirror(@Nonnull IBlockState state, @Nonnull Mirror mirrorIn) {
			return state.withProperty(ROTATION, mirrorIn.mirrorRotation(state.getValue(ROTATION), 16));
		}

		public @Nonnull IBlockState withRotation(@Nonnull IBlockState state, @Nonnull Rotation rot) {
			return state.withProperty(ROTATION, rot.rotate(state.getValue(ROTATION), 16));
		}

	}
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);

	protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);

	protected BlockBanner parent;

	protected BlockCustomBanner(BlockBanner parent) {
		super();
		this.parent = parent;
		this.setRegistryName(Objects.requireNonNull(parent.getRegistryName()));
		this.setUnlocalizedName(parent.getUnlocalizedName());
	}

	public boolean canPlaceBlockAt(@Nonnull World worldIn, @Nonnull BlockPos pos) {
		return !this.hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
	}

    public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new TileEntityCustomBanner();
	}

	public void dropBlockAsItemWithChance(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state, float chance, int fortune) {
		super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
	}

	public @Nonnull BlockFaceShape getBlockFaceShape(@Nonnull IBlockAccess worldIn, @Nonnull IBlockState state, @Nonnull BlockPos pos, @Nonnull EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	public @Nullable AxisAlignedBB getCollisionBoundingBox(@Nonnull IBlockState blockState, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void getDrops(@Nonnull NonNullList<ItemStack> drops, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull IBlockState state, int fortune) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBanner) {
			TileEntityBanner tileentitybanner = (TileEntityBanner) te;
			ItemStack itemstack = tileentitybanner.getItem();
			drops.add(itemstack);
		} else {
			drops.add(new ItemStack(Items.BANNER, 1, 0));
		}
	}

	public @Nonnull ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		ItemStack itemstack = this.getTileDataItemStack(worldIn, pos);
		return itemstack.isEmpty() ? new ItemStack(Items.BANNER) : itemstack;
	}

	public @Nonnull Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
		return Items.BANNER;
	}

	public @Nonnull String getLocalizedName() {
		return new TextComponentTranslation("item.banner.white.name").getFormattedText();
	}

	private ItemStack getTileDataItemStack(World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityBanner ? ((TileEntityBanner) tileentity).getItem() : ItemStack.EMPTY;
	}

	public void harvestBlock(@Nonnull World worldIn, @Nonnull EntityPlayer player, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nullable TileEntity te, @Nonnull ItemStack stack) {
		if (te instanceof TileEntityBanner) {
			TileEntityBanner tileentitybanner = (TileEntityBanner) te;
			ItemStack itemstack = tileentitybanner.getItem();
			spawnAsEntity(worldIn, pos, itemstack);
		} else {
			super.harvestBlock(worldIn, player, pos, state, null, stack);
		}
	}

	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isPassable(@Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		return true;
	}

}
