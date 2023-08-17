package noppes.npcs.api.wrapper;

import java.util.Map;

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
import net.minecraft.world.IBlockAccess;
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
import noppes.npcs.api.wrapper.data.StoredData;
import noppes.npcs.api.wrapper.data.TempData;
import noppes.npcs.blocks.BlockScripted;
import noppes.npcs.blocks.BlockScriptedDoor;
import noppes.npcs.blocks.tiles.TileNpcEntity;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.LRUHashMap;

public class BlockWrapper
implements IBlock {
	
	public static Map<String, BlockWrapper> blockCache = new LRUHashMap<String, BlockWrapper>(400);

	public static void clearCache() {
		BlockWrapper.blockCache.clear();
	}

	protected Block block;
	protected BlockPosWrapper bPos;
	protected BlockPos pos;
	public TileNpcEntity storage;
	private IData storeddata;
	private IData tempdata;

	public TileEntity tile;
	protected IWorld world;

	@SuppressWarnings("deprecation")
	public BlockWrapper(World world, Block block, BlockPos pos) {
		this.tempdata = new TempData(this);
		this.storeddata = new StoredData(this);

		if (world instanceof WorldServer) {
			this.world = NpcAPI.Instance().getIWorld((WorldServer) world);
		} else if (world != null) {
			WorldWrapper w = WrapperNpcAPI.worldCache.get(world.provider.getDimension());
			if (w != null) { w.world = world; }
			else { WrapperNpcAPI.worldCache.put(world.provider.getDimension(), w = WorldWrapper.createNew(world)); }
			this.world = w;
		}
		this.block = block;
		this.pos = pos;
		this.bPos = new BlockPosWrapper(pos);
		this.setTile(world.getTileEntity(pos));
	}
	
	/** Need convert to BlockState */
	@Deprecated
	public static IBlock createNew(World world, BlockPos pos, IBlockState state) {
		Block block = state.getBlock();
		String key = state.toString() + pos.toString();
		BlockWrapper b = null;
		if (!BlockWrapper.blockCache.containsKey(key)) {
			if (block instanceof BlockScripted) { b = new BlockScriptedWrapper(world, block, pos); }
			else if (block instanceof BlockScriptedDoor) { b = new BlockScriptedDoorWrapper(world, block, pos); }
			else if (block instanceof BlockFluidBase) { b = new BlockFluidContainerWrapper(world, block, pos); }
			else { b = new BlockWrapper(world, block, pos); }
			BlockWrapper.blockCache.put(key, b);
		}
		if (b != null) { b.setTile(world.getTileEntity(pos)); }
		return b;
	}

	@Override
	public void blockEvent(int type, int data) {
		this.world.getMCWorld().addBlockEvent(this.pos, this.block, type, data);
	}

	@Override
	public IContainer getContainer() {
		if (!this.isContainer()) {
			throw new CustomNPCsException("This block is not a container", new Object[0]);
		}
		return NpcAPI.Instance().getIContainer((IInventory) this.tile);
	}

	@Override
	public String getDisplayName() {
		if (this.tile == null) {
			return this.getName();
		}
		return this.tile.getDisplayName().getUnformattedText();
	}

	@Override
	public Block getMCBlock() {
		return this.block;
	}

	@Override
	public IBlockState getMCBlockState() {
		return this.world.getMCWorld().getBlockState(this.pos);
	}

	@Override
	public TileEntity getMCTileEntity() {
		return this.tile;
	}

	@Override
	public int getMetadata() {
		return this.block.getMetaFromState(this.world.getMCWorld().getBlockState(this.pos));
	}

	@Override
	public String getName() {
		return Block.REGISTRY.getNameForObject(this.block) + "";
	}

	@Override
	public IPos getPos() {
		return this.bPos;
	}

	@Override
	public IData getStoreddata() {
		return this.storeddata;
	}

	@Override
	public IData getTempdata() {
		return this.tempdata;
	}

	@Override
	public INbt getTileEntityNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		this.tile.writeToNBT(compound);
		return NpcAPI.Instance().getINbt(compound);
	}

	@Override
	public IWorld getWorld() {
		return this.world;
	}

	@Override
	public int getX() {
		return this.pos.getX();
	}

	@Override
	public int getY() {
		return this.pos.getY();
	}

	@Override
	public int getZ() {
		return this.pos.getZ();
	}

	@Override
	public boolean hasTileEntity() {
		return this.tile != null;
	}

	@Override
	public void interact(int side) {
		EntityPlayer player = (EntityPlayer) EntityNPCInterface.GenericPlayer;
		World w = this.world.getMCWorld();
		player.setWorld(w);
		player.setPosition(this.pos.getX(), this.pos.getY(), this.pos.getZ());
		this.block.onBlockActivated(w, this.pos, w.getBlockState(this.pos), (EntityPlayer) EntityNPCInterface.CommandPlayer, EnumHand.MAIN_HAND, EnumFacing.values()[side], 0.0f, 0.0f, 0.0f);
	}

	@Override
	public boolean isAir() {
		return this.block.isAir(this.world.getMCWorld().getBlockState(this.pos), (IBlockAccess) this.world.getMCWorld(),
				this.pos);
	}

	@Override
	public boolean isContainer() {
		return this.tile != null && this.tile instanceof IInventory && ((IInventory) this.tile).getSizeInventory() > 0;
	}

	@Override
	public boolean isRemoved() {
		IBlockState state = this.world.getMCWorld().getBlockState(this.pos);
		return state == null || state.getBlock() != this.block;
	}

	@Override
	public void remove() {
		this.world.getMCWorld().setBlockToAir(this.pos);
	}

	@Override
	public BlockWrapper setBlock(IBlock block) {
		this.world.getMCWorld().setBlockState(this.pos, block.getMCBlock().getDefaultState());
		return new BlockWrapper(this.world.getMCWorld(), block.getMCBlock(), this.pos);
	}

	@Override
	public BlockWrapper setBlock(String name) {
		Block block = Block.REGISTRY.getObject(new ResourceLocation(name));
		if (block == null) {
			return this;
		}
		this.world.getMCWorld().setBlockState(this.pos, block.getDefaultState());
		return new BlockWrapper(this.world.getMCWorld(), block, this.pos);
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setMetadata(int i) {
		this.world.getMCWorld().setBlockState(this.pos, this.block.getStateFromMeta(i), 3);
	}

	public void setTile(TileEntity tile) {
		this.tile = tile;
		if (tile instanceof TileNpcEntity) {
			this.storage = (TileNpcEntity) tile;
		}
	}

	@Override
	public void setTileEntityNBT(INbt nbt) {
		this.tile.readFromNBT(nbt.getMCNBT());
		this.tile.markDirty();
		IBlockState state = this.world.getMCWorld().getBlockState(this.pos);
		this.world.getMCWorld().notifyBlockUpdate(this.pos, state, state, 3);
	}

}
