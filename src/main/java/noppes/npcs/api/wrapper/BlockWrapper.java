package noppes.npcs.api.wrapper;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.BlockFluidBase;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.wrapper.data.Data;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileNpcEntity;
import noppes.npcs.entity.EntityNPCInterface;

public class BlockWrapper implements IBlock {

	/*
	 * Used in:
	 * A large number of Forge events
	 * When checking vision when an NPC is looking at a target
	 * Mod events and scripts
	 */
	public static volatile ConcurrentHashMap<Long, BlockWrapper> blockCache = new ConcurrentHashMap<>(25000);

	public static void clearCache() { blockCache.clear(); }

	public static void checkClearCache() {
		if (blockCache.size() > 25000) {
			blockCache.keySet().stream()
					.limit(blockCache.size() - 25000)
					.forEach(blockCache::remove);
		}
	}

	/** Need convert to BlockState */
	public static IBlock createNew(World world, BlockPos pos, IBlockState state) {
		Long key = makeKey(state, pos);
		BlockWrapper wrapper = blockCache.get(key);
		if (wrapper == null) {
			wrapper = createBlockWrapper(world, state, pos);
			blockCache.put(key, wrapper);
		}
        return wrapper;
	}

	private static Long makeKey(IBlockState state, BlockPos pos) {
		return (pos.toLong() << 32) | Objects.requireNonNull(state.getBlock().getRegistryName()).hashCode();
	}

	private static BlockWrapper createBlockWrapper(World world, IBlockState state, BlockPos pos) {
		Block block = state.getBlock();
		BlockWrapper wrapper;
		if (block instanceof BlockScripted) { wrapper = new BlockScriptedWrapper(world, block, pos); }
		else if (block instanceof BlockScriptedDoor) { wrapper = new BlockScriptedDoorWrapper(world, block, pos); }
		else if (block instanceof BlockFluidBase) { wrapper = new BlockFluidContainerWrapper(world, block, pos); }
		else { wrapper = new BlockWrapper(world, block, pos); }
		wrapper.setTile(world.getTileEntity(pos));
		return wrapper;
	}

	protected Block block;
	protected BlockPosWrapper bPos;
	protected BlockPos pos;
	public TileNpcEntity storage;
	private IData storeddata = new Data();
	private IData tempdata = new Data();
	public TileEntity tile;

	protected IWorld world;

	@SuppressWarnings("deprecation")
	public BlockWrapper(World worldIn, Block blockIn, BlockPos posIn) {
		if (worldIn instanceof WorldServer) {
			world = Objects.requireNonNull(NpcAPI.Instance()).getIWorld(worldIn);
		}
		else if (worldIn != null) {
			WorldWrapper w = WrapperNpcAPI.worldCache.get(worldIn.provider.getDimension());
			if (w != null) {
				if (w.world == null) {
					w.world = worldIn;
				}
			} else {
				w = WorldWrapper.createNew(worldIn);
				WrapperNpcAPI.worldCache.put(worldIn.provider.getDimension(), w);
			}
			world = w;
		}
		block = blockIn;
		pos = posIn;
		bPos = new BlockPosWrapper(posIn);
        if (worldIn != null) { setTile(worldIn.getTileEntity(posIn)); }
	}

	@Override
	public void blockEvent(int type, int data) {
		world.getMCWorld().addBlockEvent(pos, block, type, data);
	}

	@Override
	public IContainer getContainer() {
		if (!isContainer()) {
			throw new CustomNPCsException("This block is not a container");
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIContainer((IInventory) tile);
	}

	@Override
	public String getDisplayName() {
		if (tile == null) {
			return getName();
		}
		return Objects.requireNonNull(tile.getDisplayName()).getUnformattedText();
	}

	@Override
	public Block getMCBlock() {
		return block;
	}

	@Override
	public IBlockState getMCBlockState() {
		return world.getMCWorld().getBlockState(pos);
	}

	@Override
	public TileEntity getMCTileEntity() {
		return tile;
	}

	@Override
	public int getMetadata() {
		return block.getMetaFromState(world.getMCWorld().getBlockState(pos));
	}

	@Override
	public String getName() {
		return Block.REGISTRY.getNameForObject(block) + "";
	}

	@Override
	public IPos getPos() {
		return bPos;
	}

	@Override
	public IData getStoreddata() {
		return storeddata;
	}

	@Override
	public IData getTempdata() {
		return tempdata;
	}

	@Override
	public INbt getTileEntityNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		tile.writeToNBT(compound);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(compound);
	}

	@Override
	public IWorld getWorld() {
		return world;
	}

	@Override
	public int getX() {
		return pos.getX();
	}

	@Override
	public int getY() {
		return pos.getY();
	}

	@Override
	public int getZ() {
		return pos.getZ();
	}

	@Override
	public boolean hasTileEntity() {
		return tile != null;
	}

	@Override
	public void interact(int side) {
		EntityPlayer player = EntityNPCInterface.GenericPlayer;
		World w = world.getMCWorld();
		player.setWorld(w);
		player.setPosition(pos.getX(), pos.getY(), pos.getZ());
		block.onBlockActivated(w, pos, w.getBlockState(pos),
                EntityNPCInterface.CommandPlayer, EnumHand.MAIN_HAND, EnumFacing.values()[side], 0.0f,
				0.0f, 0.0f);
	}

	@Override
	public boolean isAir() {
		return block.isAir(world.getMCWorld().getBlockState(pos), world.getMCWorld(), pos);
	}

	@Override
	public boolean isContainer() {
		return tile != null && tile instanceof IInventory && ((IInventory) tile).getSizeInventory() > 0;
	}

	@Override
	public boolean isRemoved() {
		return world.getMCWorld().getBlockState(pos).getBlock() != block;
	}

	@Override
	public void remove() {
		world.getMCWorld().setBlockToAir(pos);
	}

	@Override
	public BlockWrapper setBlock(IBlock block) {
		world.getMCWorld().setBlockState(pos, block.getMCBlock().getDefaultState());
		return new BlockWrapper(world.getMCWorld(), block.getMCBlock(), pos);
	}

	@Override
	public BlockWrapper setBlock(String name) {
		Block block = Block.REGISTRY.getObject(new ResourceLocation(name));
        world.getMCWorld().setBlockState(pos, block.getDefaultState());
		return new BlockWrapper(world.getMCWorld(), block, pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setMetadata(int i) {
		world.getMCWorld().setBlockState(pos, block.getStateFromMeta(i), 3);
	}

	public void setTile(TileEntity tileIn) {
		tile = tileIn;
		if (tile instanceof TileNpcEntity) {
			storage = (TileNpcEntity) tile;
			tempdata = storage.tempData;
			storeddata = storage.storedData;
		}
	}

	@Override
	public void setTileEntityNBT(INbt nbt) {
		tile.readFromNBT(nbt.getMCNBT());
		tile.markDirty();
		IBlockState state = world.getMCWorld().getBlockState(pos);
		world.getMCWorld().notifyBlockUpdate(pos, state, state, 3);
	}

}
