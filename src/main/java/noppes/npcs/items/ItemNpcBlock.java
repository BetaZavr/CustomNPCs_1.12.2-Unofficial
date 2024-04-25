package noppes.npcs.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.blocks.CustomBlockSlab.CustomBlockSlabDouble;
import noppes.npcs.blocks.CustomBlockSlab.CustomBlockSlabSingle;
import noppes.npcs.blocks.CustomDoor;

public class ItemNpcBlock extends ItemBlock {

	public static void placeDoor(World worldIn, BlockPos pos, EnumFacing facing, Block door, boolean isRightHinge) {
		BlockPos blockpos = pos.offset(facing.rotateY());
		BlockPos blockpos1 = pos.offset(facing.rotateYCCW());
		int i = (worldIn.getBlockState(blockpos1).isNormalCube() ? 1 : 0)
				+ (worldIn.getBlockState(blockpos1.up()).isNormalCube() ? 1 : 0);
		int j = (worldIn.getBlockState(blockpos).isNormalCube() ? 1 : 0)
				+ (worldIn.getBlockState(blockpos.up()).isNormalCube() ? 1 : 0);
		boolean flag = worldIn.getBlockState(blockpos1).getBlock() == door
				|| worldIn.getBlockState(blockpos1.up()).getBlock() == door;
		boolean flag1 = worldIn.getBlockState(blockpos).getBlock() == door
				|| worldIn.getBlockState(blockpos.up()).getBlock() == door;
		if ((!flag || flag1) && j <= i) {
			if (flag1 && !flag || j < i) {
				isRightHinge = false;
			}
		} else {
			isRightHinge = true;
		}
		BlockPos posUp = pos.up();
		boolean flag2 = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(posUp);
		IBlockState iblockstate = door.getDefaultState().withProperty(BlockDoor.FACING, facing)
				.withProperty(BlockDoor.HINGE,
						isRightHinge ? BlockDoor.EnumHingePosition.RIGHT : BlockDoor.EnumHingePosition.LEFT)
				.withProperty(BlockDoor.POWERED, Boolean.valueOf(flag2))
				.withProperty(BlockDoor.OPEN, Boolean.valueOf(flag2));
		worldIn.setBlockState(pos, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.LOWER), 2);
		worldIn.setBlockState(posUp, iblockstate.withProperty(BlockDoor.HALF, BlockDoor.EnumDoorHalf.UPPER), 2);
		worldIn.notifyNeighborsOfStateChange(pos, door, false);
		worldIn.notifyNeighborsOfStateChange(posUp, door, false);
	}
	public CustomBlockSlabSingle singleSlab;

	public CustomBlockSlabDouble doubleSlab;

	public ItemNpcBlock(Block block) {
		super(block);
		String name = block.getUnlocalizedName().substring(5);
		this.setRegistryName(name);
		this.setUnlocalizedName(name);
		if (block instanceof CustomBlockSlabSingle) {
			this.singleSlab = (CustomBlockSlabSingle) block;
			this.doubleSlab = ((CustomBlockSlabSingle) block).doubleBlock;
		}
		if (block instanceof CustomBlockSlabDouble) {
			this.singleSlab = ((CustomBlockSlabDouble) block).singleBlock;
			this.doubleSlab = (CustomBlockSlabDouble) block;
		}
		if (block instanceof CustomDoor) {
			this.setFull3D();
			this.maxStackSize = 3;
		}
	}

	@SideOnly(Side.CLIENT)
	public boolean canPlaceBlockOnSide(World worldIn, BlockPos pos, EnumFacing side, EntityPlayer player,
			ItemStack stack) {
		if (this.singleSlab == null || this.doubleSlab == null) {
			return super.canPlaceBlockOnSide(worldIn, pos, side, player, stack);
		}
		// ItemSlab
		BlockPos blockpos = pos;
		IProperty<?> iproperty = this.singleSlab.getVariantProperty();
		Comparable<?> comparable = this.singleSlab.getTypeForItem(stack);
		IBlockState iblockstate = worldIn.getBlockState(pos);
		if (iblockstate.getBlock() == this.singleSlab) {
			boolean flag = iblockstate.getValue(BlockSlab.HALF) == BlockSlab.EnumBlockHalf.TOP;
			if ((side == EnumFacing.UP && !flag || side == EnumFacing.DOWN && flag)
					&& comparable == iblockstate.getValue(iproperty)) {
				return true;
			}
		}
		pos = pos.offset(side);
		IBlockState iblockstate1 = worldIn.getBlockState(pos);
		return iblockstate1.getBlock() == this.singleSlab && comparable == iblockstate1.getValue(iproperty) ? true
				: super.canPlaceBlockOnSide(worldIn, blockpos, side, player, stack);
	}

	@SuppressWarnings("unchecked")
	protected <T extends Comparable<T>> IBlockState makeState(IProperty<T> iproperty, Comparable<?> comparable) {
		return this.doubleSlab.getDefaultState().withProperty(iproperty, (T) comparable);
	}

	public EnumActionResult onItemUse(EntityPlayer player, World worldIn, BlockPos pos, EnumHand hand,
			EnumFacing facing, float hitX, float hitY, float hitZ) {
		if (this.singleSlab == null && this.doubleSlab == null && !(this.block instanceof CustomDoor)) {
			return super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
		ItemStack itemstack = player.getHeldItem(hand);
		// ItemDoor
		if (this.block instanceof CustomDoor) {
			if (facing != EnumFacing.UP) {
				return EnumActionResult.FAIL;
			}
			IBlockState iblockstate = worldIn.getBlockState(pos);
			Block block = iblockstate.getBlock();
			if (!block.isReplaceable(worldIn, pos)) {
				pos = pos.offset(facing);
			}
			if (player.canPlayerEdit(pos, facing, itemstack) && this.block.canPlaceBlockAt(worldIn, pos)) {
				EnumFacing enumfacing = EnumFacing.fromAngle((double) player.rotationYaw);
				int i = enumfacing.getFrontOffsetX();
				int j = enumfacing.getFrontOffsetZ();
				boolean flag = i < 0 && hitZ < 0.5F || i > 0 && hitZ > 0.5F || j < 0 && hitX > 0.5F
						|| j > 0 && hitX < 0.5F;
				ItemNpcBlock.placeDoor(worldIn, pos, enumfacing, this.block, flag);
				SoundType soundtype = worldIn.getBlockState(pos).getBlock().getSoundType(worldIn.getBlockState(pos),
						worldIn, pos, player);
				worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
						(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
				itemstack.shrink(1);
				return EnumActionResult.SUCCESS;
			}
			return EnumActionResult.FAIL;
		}
		// ItemSlab
		if (!itemstack.isEmpty() && player.canPlayerEdit(pos.offset(facing), facing, itemstack)) {
			Comparable<?> comparable = this.singleSlab.getTypeForItem(itemstack);
			IBlockState iblockstate = worldIn.getBlockState(pos);
			if (iblockstate.getBlock() == this.singleSlab) {
				IProperty<?> iproperty = this.singleSlab.getVariantProperty();
				Comparable<?> comparable1 = iblockstate.getValue(iproperty);
				BlockSlab.EnumBlockHalf blockslab$enumblockhalf = (BlockSlab.EnumBlockHalf) iblockstate
						.getValue(BlockSlab.HALF);
				if ((facing == EnumFacing.UP && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.BOTTOM
						|| facing == EnumFacing.DOWN && blockslab$enumblockhalf == BlockSlab.EnumBlockHalf.TOP)
						&& comparable1 == comparable) {
					IBlockState iblockstate1 = this.makeState(iproperty, comparable1);
					AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
					if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
							&& worldIn.setBlockState(pos, iblockstate1, 11)) {
						SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, player);
						worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
								(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
						itemstack.shrink(1);
						if (player instanceof EntityPlayerMP) {
							CriteriaTriggers.PLACED_BLOCK.trigger((EntityPlayerMP) player, pos, itemstack);
						}
					}
					return EnumActionResult.SUCCESS;
				}
			}
			return this.tryPlace(player, itemstack, worldIn, pos.offset(facing), comparable) ? EnumActionResult.SUCCESS
					: super.onItemUse(player, worldIn, pos, hand, facing, hitX, hitY, hitZ);
		}
		return EnumActionResult.FAIL;
	}

	public String toString() {
		return "ItemNpcBlock: " + this.getRegistryName();
	}

	private boolean tryPlace(EntityPlayer player, ItemStack stack, World worldIn, BlockPos pos, Object itemSlabType) {
		IBlockState iblockstate = worldIn.getBlockState(pos);
		if (iblockstate.getBlock() == this.singleSlab) {
			Comparable<?> comparable = iblockstate.getValue(this.singleSlab.getVariantProperty());
			if (comparable == itemSlabType) {
				IBlockState iblockstate1 = this.makeState(this.singleSlab.getVariantProperty(), comparable);
				AxisAlignedBB axisalignedbb = iblockstate1.getCollisionBoundingBox(worldIn, pos);
				if (axisalignedbb != Block.NULL_AABB && worldIn.checkNoEntityCollision(axisalignedbb.offset(pos))
						&& worldIn.setBlockState(pos, iblockstate1, 11)) {
					SoundType soundtype = this.doubleSlab.getSoundType(iblockstate1, worldIn, pos, player);
					worldIn.playSound(player, pos, soundtype.getPlaceSound(), SoundCategory.BLOCKS,
							(soundtype.getVolume() + 1.0F) / 2.0F, soundtype.getPitch() * 0.8F);
					stack.shrink(1);
				}
				return true;
			}
		}
		return false;
	}
}
