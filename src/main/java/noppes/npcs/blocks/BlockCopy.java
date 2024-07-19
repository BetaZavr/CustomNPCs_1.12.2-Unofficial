package noppes.npcs.blocks;

import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileCopy;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

import javax.annotation.Nonnull;

public class BlockCopy extends BlockInterface implements IPermission {
	public BlockCopy() {
		super(Material.ROCK);
		this.setName("npccopyblock");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab(CustomRegisters.tab);
		this.setSoundType(SoundType.STONE);
	}

	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return new TileCopy();
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.GetTileEntity || e == EnumPacketServer.SchematicStore
				|| e == EnumPacketServer.SchematicsTile || e == EnumPacketServer.SchematicsTileSave
				|| e == EnumPacketServer.SaveTileEntity;
	}

	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (par1World.isRemote) {
			return true;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem.getItem() == CustomRegisters.wand) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.CopyBlock, null, pos.getX(), pos.getY(), pos.getZ());
		}
		return true;
	}

	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase entity, @Nonnull ItemStack stack) {
		if (entity instanceof EntityPlayer && !world.isRemote) {
			NoppesUtilServer.sendOpenGui((EntityPlayer) entity, EnumGuiType.CopyBlock, null, pos.getX(), pos.getY(),
					pos.getZ());
		}
	}
}
