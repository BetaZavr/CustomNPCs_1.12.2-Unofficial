package noppes.npcs.blocks;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.EventHooks;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.IPermission;

public class BlockScripted
extends BlockInterface
implements IPermission {
	
	public static AxisAlignedBB AABB = new AxisAlignedBB(0.0010000000474974513, 0.0010000000474974513,
			0.0010000000474974513, 0.9980000257492065, 0.9980000257492065, 0.9980000257492065);
	public static AxisAlignedBB AABB_EMPTY = new AxisAlignedBB(0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

	public BlockScripted() {
		super(Material.ROCK);
		this.setName("npcscripted");
		this.setHardness(5.0f);
		this.setResistance(10.0f);
		this.setCreativeTab((CreativeTabs) CustomItems.tab);
		this.setSoundType(SoundType.STONE);
	}

	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (!(tile instanceof TileScripted)) {
				super.breakBlock(world, pos, state);
				return;
			}
			EventHooks.onScriptBlockBreak((TileScripted) tile);
		}
		super.breakBlock(world, pos, state);
	}

	public boolean canCreatureSpawn(IBlockState state, IBlockAccess world, BlockPos pos,
			EntityLiving.SpawnPlacementType type) {
		return true;
	}

	public boolean canEntityDestroy(IBlockState state, IBlockAccess world, BlockPos pos, Entity entity) {
		return super.canEntityDestroy(state, world, pos, entity);
	}

	public boolean canProvidePower(IBlockState state) {
		return true;
	}

	public TileEntity createNewTileEntity(World worldIn, int meta) {
		return new TileScripted();
	}

	public void fillWithRain(World world, BlockPos pos) {
		if (world.isRemote) {
			return;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return; }
		EventHooks.onScriptBlockRainFill((TileScripted) tile);
	}

	@SuppressWarnings("deprecation")
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.getBlockHardness(state, world, pos); }
		return ((TileScripted) tile).blockHardness;
	}

	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess world, BlockPos pos) {
		return BlockScripted.AABB;
	}

	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return BlockScripted.AABB_EMPTY; }
		if (((TileScripted) tile).isPassible) {
			return BlockScripted.AABB_EMPTY;
		}
		return BlockScripted.AABB;
	}

	public float getEnchantPowerBonus(World world, BlockPos pos) {
		return super.getEnchantPowerBonus(world, pos);
	}

	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.getExplosionResistance(world, pos, exploder, explosion); }
		return ((TileScripted) world.getTileEntity(pos)).blockResistance;
	}

	public Item getItemDropped(IBlockState state, Random rand, int fortune) {
		return null;
	}

	public int getLightValue(IBlockState state, IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.getLightValue(state, world, pos); }
		return ((TileScripted) tile).lightValue;
	}

	@SuppressWarnings("deprecation")
	public int getStrongPower(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.getStrongPower(state, world, pos, side); }
		return ((TileScripted) tile).activePowering;
	}

	public int getWeakPower(IBlockState state, IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return this.getStrongPower(state, worldIn, pos, side);
	}

	@Override
	public boolean isAllowed(EnumPacketServer e) {
		return e == EnumPacketServer.SaveTileEntity || e == EnumPacketServer.ScriptBlockDataSave;
	}

	public boolean isLadder(IBlockState state, IBlockAccess world, BlockPos pos, EntityLivingBase entity) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.isLadder(state, world, pos, entity); }
		return ((TileScripted) tile).isLadder;
	}

	public boolean isPassable(IBlockAccess world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return false; }
		return ((TileScripted) tile).isPassible;
	}

	@SuppressWarnings("deprecation")
	public void neighborChanged(IBlockState state, World world, BlockPos pos, Block neighborBlock, BlockPos pos2) {
		if (world.isRemote) {
			return;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) {
			super.neighborChanged(state, world, pos, neighborBlock, pos2);
			return;
		}
		EventHooks.onScriptBlockNeighborChanged((TileScripted) tile, pos2);
		int power = 0;
		for (EnumFacing enumfacing : EnumFacing.values()) {
			int p = world.getRedstonePower(pos.offset(enumfacing), enumfacing);
			if (p > power) {
				power = p;
			}
		}
		if (((TileScripted) tile).prevPower != power && ((TileScripted) tile).powering <= 0) {
			((TileScripted) tile).newPower = power;
		}
	}

	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand,
			EnumFacing side, float hitX, float hitY, float hitZ) {
		if (world.isRemote) {
			return true;
		}
		ItemStack currentItem = player.inventory.getCurrentItem();
		if (currentItem != null
				&& (currentItem.getItem() == CustomItems.wand || currentItem.getItem() == CustomItems.scripter)) {
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.ScriptBlock, null, pos.getX(), pos.getY(), pos.getZ());
			return true;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return super.onBlockActivated(world, pos, state, player, hand, side, hitX, hitY, hitZ); }
		return !EventHooks.onScriptBlockInteract((TileScripted) tile, player, side.getIndex(), hitX, hitY, hitZ);
	}

	public void onBlockClicked(World world, BlockPos pos, EntityPlayer player) {
		if (world.isRemote) {
			return;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return; }
		EventHooks.onScriptBlockClicked((TileScripted) tile, player);
	}

	public void onBlockExploded(World world, BlockPos pos, Explosion explosion) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (!(tile instanceof TileScripted)) {
				super.onBlockExploded(world, pos, explosion);
				return;
			}
			if (EventHooks.onScriptBlockExploded((TileScripted) tile)) {
				return;
			}
		}
		super.onBlockExploded(world, pos, explosion);
	}

	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase entity,
			ItemStack stack) {
		if (entity instanceof EntityPlayer && !world.isRemote) {
			NoppesUtilServer.sendOpenGui((EntityPlayer) entity, EnumGuiType.ScriptBlock, null, pos.getX(), pos.getY(),
					pos.getZ());
		}
	}

	public void onEntityCollision(World world, BlockPos pos, IBlockState state, Entity entityIn) {
		if (world.isRemote) {
			return;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) { return; }
		EventHooks.onScriptBlockCollide((TileScripted) tile, entityIn);
	}

	public void onFallenUpon(World world, BlockPos pos, Entity entity, float fallDistance) {
		if (world.isRemote) {
			return;
		}
		TileEntity tile = world.getTileEntity(pos);
		if (!(tile instanceof TileScripted)) {
			super.onFallenUpon(world, pos, entity, fallDistance);
			return;
		}
		fallDistance = EventHooks.onScriptBlockFallenUpon((TileScripted) tile, entity, fallDistance);
		super.onFallenUpon(world, pos, entity, fallDistance);
	}

	public boolean removedByPlayer(IBlockState state, World world, BlockPos pos, EntityPlayer player,
			boolean willHarvest) {
		if (!world.isRemote) {
			TileEntity tile = world.getTileEntity(pos);
			if (!(tile instanceof TileScripted)) { return super.removedByPlayer(state, world, pos, player, willHarvest); }
			if (EventHooks.onScriptBlockHarvest((TileScripted) tile, player)) {
				return false;
			}
		}
		return super.removedByPlayer(state, world, pos, player, willHarvest);
	}
	
	public boolean isOpaqueCube(IBlockState state) { return false; }
	
	public boolean isFullCube(IBlockState state) { return false; }
	
}
