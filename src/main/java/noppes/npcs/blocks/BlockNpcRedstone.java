package noppes.npcs.blocks;

import net.minecraft.block.material.Material;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
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

public class BlockNpcRedstone extends BlockInterface implements IPermission {
	public static PropertyBool ACTIVE = PropertyBool.create("active");;

	public BlockNpcRedstone() {
		super(Material.ROCK);
		this.setName("npcredstoneblock");
		this.setHardness(50.0f);
		this.setResistance(2000.0f);
		this.setCreativeTab((CreativeTabs) CustomRegisters.tab);
	}

	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, new IProperty[] { BlockNpcRedstone.ACTIVE });
	}

	public TileEntity createNewTileEntity(World var1, int var2) {
		return new TileRedstoneBlock();
	}

	@SideOnly(Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.CUTOUT;
	}

	public int getMetaFromState(IBlockState state) {
		return ((boolean) state.getValue(BlockNpcRedstone.ACTIVE)) ? 1 : 0;
	}

	public EnumBlockRenderType getRenderType(IBlockState state) {
		return EnumBlockRenderType.MODEL;
	}

	public IBlockState getStateFromMeta(int meta) {
		return this.getDefaultState().withProperty(BlockNpcRedstone.ACTIVE, false);
	}

	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		return this.isActivated(state);
	}

	public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return this.isActivated(state);
	}

	public int isActivated(IBlockState state) {
		return state.getValue(BlockNpcRedstone.ACTIVE) ? 15 : 0;
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SaveTileEntity;
	}

	public boolean isFullCube(IBlockState state) {
		return false;
	}

	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}

	public boolean onBlockActivated(World par1World, BlockPos pos, IBlockState state, EntityPlayer player,
			EnumHand hand, EnumFacing side, float hitX, float hitY, float hitZ) {
		if (par1World.isRemote) {
			return false;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null && currentItem.getItem() == CustomRegisters.wand
				&& CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.RedstoneBlock, null, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		return false;
	}

	public void onBlockAdded(World par1World, BlockPos pos, IBlockState state) {
		par1World.notifyNeighborsOfStateChange(pos, this, false);
		par1World.notifyNeighborsOfStateChange(pos.down(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.up(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.west(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.east(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.south(), this, false);
		par1World.notifyNeighborsOfStateChange(pos.north(), this, false);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entityliving,
			ItemStack item) {
		if (entityliving instanceof EntityPlayer && !world.isRemote) {
			NoppesUtilServer.sendOpenGui((EntityPlayer) entityliving, EnumGuiType.RedstoneBlock, null, pos.getX(),
					pos.getY(), pos.getZ());
		}
	}

	public void onPlayerDestroy(World par1World, BlockPos pos, IBlockState state) {
		this.onBlockAdded(par1World, pos, state);
	}

}
