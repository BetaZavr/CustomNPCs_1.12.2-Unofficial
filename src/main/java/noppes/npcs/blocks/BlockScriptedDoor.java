package noppes.npcs.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.blocks.tiles.TileScriptedDoor;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

import javax.annotation.Nonnull;
import java.util.Objects;

public class BlockScriptedDoor extends BlockNpcDoorInterface implements IPermission {

	@Override
	public void breakBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		BlockPos blockpos1 = (state.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) ? pos
				: pos.down();
		IBlockState iblockstate1 = pos.equals(blockpos1) ? state : world.getBlockState(blockpos1);
		if (!world.isRemote && iblockstate1.getBlock() == this) {
			TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(pos);
			if (tile != null) { EventHooks.onScriptBlockBreak(tile); }
		}
		super.breakBlock(world, pos, state);
	}

	@Override
	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new TileScriptedDoor();
	}

	public float getBlockHardness(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos) {
		return ((TileScriptedDoor) Objects.requireNonNull(world.getTileEntity(pos))).blockHardness;
	}

	public float getExplosionResistance(@Nonnull World world, @Nonnull BlockPos pos, Entity exploder, @Nonnull Explosion explosion) {
		return ((TileScriptedDoor) Objects.requireNonNull(world.getTileEntity(pos))).blockResistance;
	}

	public @Nonnull EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
		return EnumBlockRenderType.INVISIBLE;
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.ScriptDoorDataSave;
	}

	public void neighborChanged(@Nonnull IBlockState state, @Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull Block neighborBlock, @Nonnull BlockPos pos2) {
		if (state.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.UPPER) {
			BlockPos blockpos1 = pos.down();
			IBlockState iblockstate1 = worldIn.getBlockState(blockpos1);
			if (iblockstate1.getBlock() != this) {
				worldIn.setBlockToAir(pos);
			} else if (neighborBlock != this) {
				this.neighborChanged(iblockstate1, worldIn, blockpos1, neighborBlock, blockpos1);
			}
		} else {
			BlockPos blockpos2 = pos.up();
			IBlockState iblockstate2 = worldIn.getBlockState(blockpos2);
			if (iblockstate2.getBlock() != this) {
				worldIn.setBlockToAir(pos);
			} else {
				TileScriptedDoor tile = (TileScriptedDoor) worldIn.getTileEntity(pos);
				if (!worldIn.isRemote && tile != null) {
					EventHooks.onScriptBlockNeighborChanged(tile, pos2);
				}
				boolean flag = worldIn.isBlockPowered(pos) || worldIn.isBlockPowered(blockpos2);
				if ((flag || neighborBlock.getDefaultState().canProvidePower()) && neighborBlock != this
						&& flag != iblockstate2.getValue(BlockScriptedDoor.POWERED)) {
					worldIn.setBlockState(blockpos2, iblockstate2.withProperty(BlockScriptedDoor.POWERED, flag), 2);
					if (flag != state.getValue(BlockScriptedDoor.OPEN)) {
						this.toggleDoor(worldIn, pos, flag);
					}
				}
				int power = 0;
				for (EnumFacing enumfacing : EnumFacing.values()) {
					int p = worldIn.getRedstonePower(pos.offset(enumfacing), enumfacing);
					if (p > power) {
						power = p;
					}
				}
                if (tile != null) {
					tile.newPower = power;
				}
			}
		}
	}

	public boolean onBlockActivated(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		BlockPos blockpos1 = (state.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) ? pos
				: pos.down();
		IBlockState iblockstate1 = pos.equals(blockpos1) ? state : world.getBlockState(blockpos1);
		if (iblockstate1.getBlock() != this) {
			return false;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem.getItem() == CustomRegisters.wand || currentItem.getItem() == CustomRegisters.scripter || currentItem.getItem() == CustomRegisters.scriptedDoorTool) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptDoor, null, blockpos1.getX(), blockpos1.getY(),
					blockpos1.getZ());
			return true;
		}
		TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(blockpos1);
		if (tile != null && EventHooks.onScriptBlockInteract(tile, player, side.getIndex(), hitX, hitY, hitZ)) {
			return false;
		}
		this.toggleDoor(world, blockpos1, iblockstate1.getValue(BlockDoor.OPEN).equals(false));
		return true;
	}

	public void onBlockClicked(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer playerIn) {
		if (world.isRemote) {
			return;
		}
		IBlockState state = world.getBlockState(pos);
		BlockPos blockpos1 = (state.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) ? pos : pos.down();
		IBlockState iblockstate1 = pos.equals(blockpos1) ? state : world.getBlockState(blockpos1);
		if (iblockstate1.getBlock() != this) {
			return;
		}
		TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(blockpos1);
		if (tile != null) { EventHooks.onScriptBlockClicked(tile, playerIn); }
	}

	public void onBlockHarvested(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player) {
		BlockPos blockpos1 = (state.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER) ? pos
				: pos.down();
		IBlockState iblockstate1 = pos.equals(blockpos1) ? state : world.getBlockState(blockpos1);
		if (player.capabilities.isCreativeMode
				&& iblockstate1.getValue(BlockScriptedDoor.HALF) == BlockDoor.EnumDoorHalf.LOWER
				&& iblockstate1.getBlock() == this) {
			world.setBlockToAir(blockpos1);
		}
	}

	public void onEntityCollidedWithBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entityIn) {
		if (world.isRemote) {
			return;
		}
		TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(pos);
		if (tile != null) { EventHooks.onScriptBlockCollide(tile, entityIn); }
	}

	public boolean removedByPlayer(@Nonnull IBlockState state, @Nonnull World world, @Nonnull BlockPos pos, @Nonnull EntityPlayer player, boolean willHarvest) {
		if (!world.isRemote) {
			TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(pos);
			if (tile != null && EventHooks.onScriptBlockHarvest(tile, player)) {
				return false;
			}
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}

	public void toggleDoor(@Nonnull World world, @Nonnull BlockPos pos, boolean open) {
		TileScriptedDoor tile = (TileScriptedDoor) world.getTileEntity(pos);
		if (tile != null && EventHooks.onScriptBlockDoorToggle(tile)) {
			return;
		}
		IBlockState iblockstate = world.getBlockState(pos);
		if (iblockstate.getBlock() == this) {
			BlockPos blockpos = iblockstate.getValue(HALF) == BlockDoor.EnumDoorHalf.LOWER ? pos : pos.down();
			IBlockState iblockstate1 = pos == blockpos ? iblockstate : world.getBlockState(blockpos);
			if (iblockstate1.getBlock() == this && iblockstate1.getValue(OPEN) != open) {
				world.setBlockState(blockpos, iblockstate1.withProperty(OPEN, open), 10);
				world.markBlockRangeForRenderUpdate(blockpos, pos);
				if (tile != null) {
					String sound = open ? tile.openSound : tile.closeSound;
					if (sound != null && !sound.isEmpty()) {
						Server.sendRangedData(world, pos, 32, EnumPacketClient.FORCE_PLAY_SOUND,
								SoundCategory.NEUTRAL.ordinal(), sound, pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
					} else {
						world.playEvent(null, open ? this.blockMaterial == Material.IRON ? 1005 : 1006 : this.blockMaterial == Material.IRON ? 1011 : 1012, pos, 0);
					}
				}
			}
		}
	}

}
