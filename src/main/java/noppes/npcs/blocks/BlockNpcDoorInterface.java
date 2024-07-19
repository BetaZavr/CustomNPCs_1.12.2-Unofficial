package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.BlockDoor;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.blocks.tiles.TileDoor;

import javax.annotation.Nonnull;

public abstract class BlockNpcDoorInterface extends BlockDoor implements ITileEntityProvider {

	public BlockNpcDoorInterface() {
		super(Material.WOOD);
		this.setRegistryName(CustomNpcs.MODID, "npcscripteddoor");
		this.setUnlocalizedName("npcscripteddoor");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomRegisters.tab);
		this.hasTileEntity = true;
	}

	public void breakBlock(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	public TileEntity createNewTileEntity(@Nonnull World worldIn, int meta) {
		return new TileDoor();
	}

	public @Nonnull IBlockState getActualState(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos) {
		if (state.getValue(BlockNpcDoorInterface.HALF) == BlockDoor.EnumDoorHalf.LOWER) {
			IBlockState iblockstate1 = worldIn.getBlockState(pos.up());
			if (iblockstate1.getBlock() == this) {
				state = state
						.withProperty(BlockNpcDoorInterface.HINGE, iblockstate1.getValue(BlockNpcDoorInterface.HINGE))
						.withProperty(BlockNpcDoorInterface.POWERED,
								iblockstate1.getValue(BlockNpcDoorInterface.POWERED));
			}
		} else {
			IBlockState iblockstate1 = worldIn.getBlockState(pos.down());
			if (iblockstate1.getBlock() == this) {
				state = state
						.withProperty(BlockNpcDoorInterface.FACING, iblockstate1.getValue(BlockNpcDoorInterface.FACING))
						.withProperty(BlockNpcDoorInterface.OPEN, iblockstate1.getValue(BlockNpcDoorInterface.OPEN));
			}
		}
		return state;
	}

	public @Nonnull ItemStack getItem(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		return new ItemStack(CustomRegisters.scriptedDoorTool, 1, this.damageDropped(state));
	}

	public @Nonnull Item getItemDropped(@Nonnull IBlockState state, @Nonnull Random rand, int fortune) {
		return ItemStack.EMPTY.getItem();
	}

	public boolean hasTileEntity(@Nonnull IBlockState state) {
		return true;
	}

}
