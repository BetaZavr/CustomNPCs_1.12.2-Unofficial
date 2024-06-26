package noppes.npcs.blocks;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockBanner;
import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.properties.IProperty;
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

		protected BlockStateContainer createBlockState() {
			return new BlockStateContainer(this, new IProperty[] { FACING });
		}

		public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
			switch ((EnumFacing) state.getValue(FACING)) {
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

		public int getMetaFromState(IBlockState state) {
			return ((EnumFacing) state.getValue(FACING)).getIndex();
		}

		public IBlockState getStateFromMeta(int meta) {
			EnumFacing enumfacing = EnumFacing.getFront(meta);
			if (enumfacing.getAxis() == EnumFacing.Axis.Y) {
				enumfacing = EnumFacing.NORTH;
			}
			return this.getDefaultState().withProperty(FACING, enumfacing);
		}

		@SuppressWarnings("deprecation")
		public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
			EnumFacing enumfacing = (EnumFacing) state.getValue(FACING);
			if (!worldIn.getBlockState(pos.offset(enumfacing.getOpposite())).getMaterial().isSolid()) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
			super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		}

		public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
			return state.withRotation(mirrorIn.toRotation((EnumFacing) state.getValue(FACING)));
		}

		public IBlockState withRotation(IBlockState state, Rotation rot) {
			return state.withProperty(FACING, rot.rotate((EnumFacing) state.getValue(FACING)));
		}

	}

	public static class BlockBannerStanding extends BlockCustomBanner {

		public BlockBannerStanding(BlockBanner parent) {
			super(parent);
			this.setDefaultState(this.blockState.getBaseState().withProperty(ROTATION, Integer.valueOf(0)));
		}

		protected BlockStateContainer createBlockState() {
			return new BlockStateContainer(this, new IProperty[] { ROTATION });
		}

		public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
			return STANDING_AABB;
		}

		public int getMetaFromState(IBlockState state) {
			return ((Integer) state.getValue(ROTATION)).intValue();
		}

		public IBlockState getStateFromMeta(int meta) {
			return this.getDefaultState().withProperty(ROTATION, Integer.valueOf(meta));
		}

		@SuppressWarnings("deprecation")
		public void neighborChanged(IBlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos) {
			if (!worldIn.getBlockState(pos.down()).getMaterial().isSolid()) {
				this.dropBlockAsItem(worldIn, pos, state, 0);
				worldIn.setBlockToAir(pos);
			}
			super.neighborChanged(state, worldIn, pos, blockIn, fromPos);
		}

		public IBlockState withMirror(IBlockState state, Mirror mirrorIn) {
			return state.withProperty(ROTATION,
					Integer.valueOf(mirrorIn.mirrorRotation(((Integer) state.getValue(ROTATION)).intValue(), 16)));
		}

		public IBlockState withRotation(IBlockState state, Rotation rot) {
			return state.withProperty(ROTATION,
					Integer.valueOf(rot.rotate(((Integer) state.getValue(ROTATION)).intValue(), 16)));
		}

	}
	public static final PropertyDirection FACING = BlockHorizontal.FACING;
	public static final PropertyInteger ROTATION = PropertyInteger.create("rotation", 0, 15);

	protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(0.25D, 0.0D, 0.25D, 0.75D, 1.0D, 0.75D);

	protected BlockBanner parent;

	protected BlockCustomBanner(BlockBanner parent) {
		super();
		this.parent = parent;
		this.setRegistryName(parent.getRegistryName());
		this.setUnlocalizedName(parent.getUnlocalizedName());
	}

	public boolean canPlaceBlockAt(World worldIn, BlockPos pos) {
		return !this.hasInvalidNeighbor(worldIn, pos) && super.canPlaceBlockAt(worldIn, pos);
	}

	public boolean canSpawnInBlock() {
		return true;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileEntityCustomBanner();
	}

	public void dropBlockAsItemWithChance(World worldIn, BlockPos pos, IBlockState state, float chance, int fortune) {
		super.dropBlockAsItemWithChance(worldIn, pos, state, chance, fortune);
	}

	public BlockFaceShape getBlockFaceShape(IBlockAccess worldIn, IBlockState state, BlockPos pos, EnumFacing face) {
		return BlockFaceShape.UNDEFINED;
	}

	@Nullable
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		return NULL_AABB;
	}

	@Override
	public void getDrops(net.minecraft.util.NonNullList<ItemStack> drops, IBlockAccess world, BlockPos pos,
			IBlockState state, int fortune) {
		TileEntity te = world.getTileEntity(pos);
		if (te instanceof TileEntityBanner) {
			TileEntityBanner tileentitybanner = (TileEntityBanner) te;
			ItemStack itemstack = tileentitybanner.getItem();
			drops.add(itemstack);
		} else {
			drops.add(new ItemStack(Items.BANNER, 1, 0));
		}
	}

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		ItemStack itemstack = this.getTileDataItemStack(worldIn, pos);
		return itemstack.isEmpty() ? new ItemStack(Items.BANNER) : itemstack;
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return Items.BANNER;
	}

	public String getLocalizedName() {
		return new TextComponentTranslation("item.banner.white.name").getFormattedText();
	}

	private ItemStack getTileDataItemStack(World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getTileEntity(pos);
		return tileentity instanceof TileEntityBanner ? ((TileEntityBanner) tileentity).getItem() : ItemStack.EMPTY;
	}

	public void harvestBlock(World worldIn, EntityPlayer player, BlockPos pos, IBlockState state,
			@Nullable TileEntity te, ItemStack stack) {
		if (te instanceof TileEntityBanner) {
			TileEntityBanner tileentitybanner = (TileEntityBanner) te;
			ItemStack itemstack = tileentitybanner.getItem();
			spawnAsEntity(worldIn, pos, itemstack);
		} else {
			super.harvestBlock(worldIn, player, pos, state, (TileEntity) null, stack);
		}
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
		return true;
	}

}
