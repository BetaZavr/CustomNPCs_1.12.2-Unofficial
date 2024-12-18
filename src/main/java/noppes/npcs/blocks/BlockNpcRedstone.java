package noppes.npcs.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.CustomRegisters;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileRedstoneBlock;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

import javax.annotation.Nonnull;

public class BlockNpcRedstone
extends BlockInterface
implements IPermission {

	public static PropertyBool ACTIVE = PropertyBool.create("active");

	public BlockNpcRedstone() {
		super(Material.ROCK);
		this.setName("npcredstoneblock");
		this.setHardness(50.0f);
		this.setResistance(2000.0f);
		this.setCreativeTab(CustomRegisters.tab);
	}

	public boolean canProvidePower(@Nonnull IBlockState state) {
		return true;
	}

	protected @Nonnull BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, BlockNpcRedstone.ACTIVE);
	}

	public TileEntity createNewTileEntity(@Nonnull World var1, int var2) {
		return new TileRedstoneBlock();
	}

	@SideOnly(Side.CLIENT)
	public @Nonnull BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public int getMetaFromState(@Nonnull IBlockState state) {
		return state.getValue(BlockNpcRedstone.ACTIVE) ? 1 : 0;
	}

	public @Nonnull EnumBlockRenderType getRenderType(@Nonnull IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public @Nonnull IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockNpcRedstone.ACTIVE, false);
	}

	public int getStrongPower(@Nonnull IBlockState state, @Nonnull IBlockAccess world, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.isActivated(state);
	}

	public int getWeakPower(@Nonnull IBlockState state, @Nonnull IBlockAccess worldIn, @Nonnull BlockPos pos, @Nonnull EnumFacing side) {
		return this.isActivated(state);
	}

	public int isActivated(IBlockState state) {
		return state.getValue(BlockNpcRedstone.ACTIVE) ? 15 : 0;
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SaveTileEntity;
	}

	public boolean isFullCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(@Nonnull IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityPlayer player, @Nonnull EnumHand hand, @Nonnull EnumFacing side, float hitX, float hitY, float hitZ) {
		if (par1World.isRemote) {
			return false;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem.getItem() == CustomRegisters.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.RedstoneBlock, null, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}

	public void onBlockAdded(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		par1World.notifyNeighborsOfStateChange(pos, this, false);
		par1World.notifyNeighborsOfStateChange(pos.down(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.up(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.west(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.east(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.south(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.north(), this, false);
	}

	public void onBlockPlacedBy(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull EntityLivingBase entityliving, @Nonnull ItemStack item) {
		if (entityliving instanceof EntityPlayer && !world.isRemote) {
			NoppesUtilServer.sendOpenGui((EntityPlayer) entityliving, EnumGuiType.RedstoneBlock, null, pos.getX(),
					pos.getY(), pos.getZ());
		}
	}

	public void onBlockDestroyedByPlayer(@Nonnull World par1World, @Nonnull BlockPos pos, @Nonnull IBlockState state) {
		this.onBlockAdded(par1World, pos, state);
	}

}
