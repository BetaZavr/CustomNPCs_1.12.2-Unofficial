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
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.blocks.tiles.TileDoor;

public abstract class BlockNpcDoorInterface extends BlockDoor implements ITileEntityProvider {
	public BlockNpcDoorInterface() {
		super(Material.WOOD);
		this.setRegistryName(CustomNpcs.MODID, "npcscripteddoor");
		this.setUnlocalizedName("npcscripteddoor");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomItems.tab);
		this.hasTileEntity = true;
	}

	public void breakBlock(World worldIn, BlockPos pos, IBlockState state) {
		super.breakBlock(worldIn, pos, state);
		worldIn.removeTileEntity(pos);
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileDoor();
	}

	public IBlockState getActualState(IBlockState state, IBlockAccess worldIn, BlockPos pos) {
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

	public ItemStack getItem(World worldIn, BlockPos pos, IBlockState state) {
		return new ItemStack(CustomItems.scriptedDoorTool, 1, this.damageDropped(state));
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	public boolean hasTileEntity(IBlockState state) {
		return true;
	}

}
