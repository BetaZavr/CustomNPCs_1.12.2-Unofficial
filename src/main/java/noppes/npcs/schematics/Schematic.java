package noppes.npcs.schematics;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.NoppesUtilServer;

public class Schematic implements ISchematic {
	public static Schematic Create(World world, String name, BlockPos pos, short height, short width, short length) {
		Schematic schema = new Schematic(name);
		schema.height = height;
		schema.width = width;
		schema.length = length;
		int size = height * width * length;
		schema.blockArray = new short[size];
		schema.blockDataArray = new byte[size];
		NoppesUtilServer.NotifyOPs("Creating schematic at: " + pos + " might lag slightly", new Object[0]);
		schema.tileList = new NBTTagList();
		for (int i = 0; i < size; ++i) {
			int x = i % width;
			int z = (i - x) / width % length;
			int y = ((i - x) / width - z) / length;
			IBlockState state = world.getBlockState(pos.add(x, y, z));
			if (state.getBlock() != Blocks.AIR) {
				if (state.getBlock() != CustomItems.copy) {
					schema.blockArray[i] = (short) Block.REGISTRY.getIDForObject(state.getBlock());
					schema.blockDataArray[i] = (byte) state.getBlock().getMetaFromState(state);
					if (state.getBlock() instanceof ITileEntityProvider) {
						TileEntity tile = world.getTileEntity(pos.add(x, y, z));
						NBTTagCompound compound = new NBTTagCompound();
						tile.writeToNBT(compound);
						compound.setInteger("x", x);
						compound.setInteger("y", y);
						compound.setInteger("z", z);
						schema.tileList.appendTag(compound);
					}
				}
			}
		}
		return schema;
	}

	public short[] blockArray;
	public byte[] blockDataArray;
	private NBTTagList entityList;
	public short height;
	public short length;
	public String name;
	public NBTTagList tileList;

	public short width;

	public Schematic(String name) {
		this.name = name;
	}

	public byte[][] getBlockBytes() {
		byte[] blocks = new byte[this.blockArray.length];
		byte[] addBlocks = null;
		for (int i = 0; i < blocks.length; ++i) {
			short id = this.blockArray[i];
			if (id > 255) {
				if (addBlocks == null) {
					addBlocks = new byte[(blocks.length >> 1) + 1];
				}
				if ((i & 0x1) == 0x0) {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF0) | (id >> 8 & 0xF));
				} else {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF) | (id >> 8 & 0xF) << 4);
				}
			}
			blocks[i] = (byte) id;
		}
		if (addBlocks == null) {
			return new byte[][] { blocks };
		}
		return new byte[][] { blocks, addBlocks };
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getBlockState(int i) {
		Block b = Block.getBlockById(this.blockArray[i]);
		if (b == null) {
			return Blocks.AIR.getDefaultState();
		}
		return b.getStateFromMeta(this.blockDataArray[i]);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		int i = this.xyzToIndex(x, y, z);
		Block b = Block.getBlockById(this.blockArray[i]);
		if (b == null) {
			return Blocks.AIR.getDefaultState();
		}
		return b.getStateFromMeta(this.blockDataArray[i]);
	}

	@Override
	public short getHeight() {
		return this.height;
	}

	@Override
	public short getLength() {
		return this.length;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setShort("Width", this.width);
		compound.setShort("Height", this.height);
		compound.setShort("Length", this.length);
		byte[][] arr = this.getBlockBytes();
		compound.setByteArray("Blocks", arr[0]);
		if (arr.length > 1) {
			compound.setByteArray("AddBlocks", arr[1]);
		}
		compound.setByteArray("Data", this.blockDataArray);
		compound.setTag("TileEntities", this.tileList);
		return compound;
	}

	@Override
	public NBTTagCompound getTileEntity(int i) {
		return this.entityList.getCompoundTagAt(i);
	}

	@Override
	public int getTileEntitySize() {
		if (this.entityList == null) {
			return 0;
		}
		return this.entityList.tagCount();
	}

	@Override
	public short getWidth() {
		return this.width;
	}

	public void load(NBTTagCompound compound) {
		this.width = compound.getShort("Width");
		this.height = compound.getShort("Height");
		this.length = compound.getShort("Length");
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		this.setBlockBytes(compound.getByteArray("Blocks"), addId);
		this.blockDataArray = compound.getByteArray("Data");
		this.entityList = compound.getTagList("Entities", 10);
		this.tileList = compound.getTagList("TileEntities", 10);
	}

	public void setBlockBytes(byte[] blockId, byte[] addId) {
		this.blockArray = new short[blockId.length];
		for (int index = 0; index < blockId.length; ++index) {
			short id = (short) (blockId[index] & 0xFF);
			if (index >> 1 < addId.length) {
				if ((index & 0x1) == 0x0) {
					id += ((addId[index >> 1] & 0xF) << 8);
				} else {
					id += ((addId[index >> 1] & 0xF0) << 4);
				}
			}
			this.blockArray[index] = id;
		}
	}

	public int xyzToIndex(int x, int y, int z) {
		return (y * this.length + z) * this.width + x;
	}
}
