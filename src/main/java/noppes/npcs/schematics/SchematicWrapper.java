package noppes.npcs.schematics;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

public class SchematicWrapper {
	public static int buildSize = 10000;
	public int buildPos;
	public boolean firstLayer;
	public boolean isBuilding;
	public int rotation;
	public ISchematic schema;
	public int size;
	private BlockPos start;
	private Map<ChunkPos, NBTTagCompound>[] tileEntities;
	private World world;

	@SuppressWarnings("unchecked")
	public SchematicWrapper(ISchematic schematic) {
		this.start = BlockPos.ORIGIN;
		this.rotation = 0;
		this.isBuilding = false;
		this.firstLayer = true;
		this.schema = schematic;
		this.size = schematic.getWidth() * schematic.getHeight() * schematic.getLength();
		this.tileEntities = new Map[schematic.getHeight()];
		for (int i = 0; i < schematic.getTileEntitySize(); ++i) {
			NBTTagCompound teTag = schematic.getTileEntity(i);
			int x = teTag.getInteger("x");
			int y = teTag.getInteger("y");
			int z = teTag.getInteger("z");
			Map<ChunkPos, NBTTagCompound> map = this.tileEntities[y];
			if (map == null) {
				map = (this.tileEntities[y] = new HashMap<ChunkPos, NBTTagCompound>());
			}
			map.put(new ChunkPos(x, z), teTag);
		}
	}

	public void build() {
		if (this.world == null || !this.isBuilding) {
			return;
		}
		long endPos = this.buildPos + 10000;
		if (endPos > this.size) {
			endPos = this.size;
		}
		while (this.buildPos < endPos) {
			int x = this.buildPos % this.schema.getWidth();
			int z = (this.buildPos - x) / this.schema.getWidth() % this.schema.getLength();
			int y = ((this.buildPos - x) / this.schema.getWidth() - z) / this.schema.getLength();
			if (this.firstLayer) {
				this.place(x, y, z, 1);
			} else {
				this.place(x, y, z, 2);
			}
			++this.buildPos;
		}
		if (this.buildPos >= this.size) {
			if (this.firstLayer) {
				this.firstLayer = false;
				this.buildPos = 0;
			} else {
				this.isBuilding = false;
			}
		}
	}

	public NBTTagCompound getNBTSmall() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setShort("Width", this.schema.getWidth());
		compound.setShort("Height", this.schema.getHeight());
		compound.setShort("Length", this.schema.getLength());
		compound.setString("SchematicName", this.schema.getName());
		NBTTagList list = new NBTTagList();
		for (int i = 0; i < this.size && i < 25000; ++i) {
			IBlockState state = this.schema.getBlockState(i);
			if (state.getBlock() == Blocks.AIR || state.getBlock() == Blocks.STRUCTURE_VOID) {
				list.appendTag(new NBTTagCompound());
			} else {
				list.appendTag(NBTUtil.writeBlockState(new NBTTagCompound(), this.schema.getBlockState(i)));
			}
		}
		compound.setTag("Data", list);
		return compound;
	}

	public int getPercentage() {
		double l = this.buildPos + (this.firstLayer ? 0 : this.size);
		return (int) (l / this.size * 50.0);
	}

	public NBTTagCompound getTileEntity(int x, int y, int z, BlockPos pos) {
		if (y >= this.tileEntities.length || this.tileEntities[y] == null) {
			return null;
		}
		NBTTagCompound compound = this.tileEntities[y].get(new ChunkPos(x, z));
		if (compound == null) {
			return null;
		}
		compound = compound.copy();
		compound.setInteger("x", pos.getX());
		compound.setInteger("y", pos.getY());
		compound.setInteger("z", pos.getZ());
		return compound;
	}

	public void init(BlockPos pos, World world, int rotation) {
		this.start = pos;
		this.world = world;
		this.rotation = rotation;
	}

	public void load(Schematic s) {
	}

	public void offset(int x, int y, int z) {
		new BlockPos(x, y, z);
	}

	public void place(int x, int y, int z, int flag) {
		IBlockState state = this.schema.getBlockState(x, y, z);
		if (state == null || (flag == 1 && !state.isFullBlock() && state.getBlock() != Blocks.AIR)
				|| (flag == 2 && (state.isFullBlock() || state.getBlock() == Blocks.AIR))) {
			return;
		}
		int rotation = this.rotation / 90;
		BlockPos pos = this.start.add((Vec3i) this.rotatePos(x, y, z, rotation));
		state = this.rotationState(state, rotation);
		this.world.setBlockState(pos, state, 2);
		if (state.getBlock() instanceof ITileEntityProvider) {
			TileEntity tile = this.world.getTileEntity(pos);
			if (tile != null) {
				NBTTagCompound comp = this.getTileEntity(x, y, z, pos);
				if (comp != null) {
					tile.readFromNBT(comp);
				}
			}
		}
	}

	public BlockPos rotatePos(int x, int y, int z, int rotation) {
		if (rotation == 1) {
			return new BlockPos(this.schema.getLength() - z - 1, y, x);
		}
		if (rotation == 2) {
			return new BlockPos(this.schema.getWidth() - x - 1, y, this.schema.getLength() - z - 1);
		}
		if (rotation == 3) {
			return new BlockPos(z, y, this.schema.getWidth() - x - 1);
		}
		return new BlockPos(x, y, z);
	}

	@SuppressWarnings("unchecked")
	public IBlockState rotationState(IBlockState state, int rotation) {
		if (rotation == 0) {
			return state;
		}
		Set<IProperty<?>> set = (Set<IProperty<?>>) state.getProperties().keySet();
		for (@SuppressWarnings("rawtypes")
		IProperty prop : set) {
			if (!(prop instanceof PropertyDirection)) {
				continue;
			}
			EnumFacing direction = (EnumFacing) state.getValue(prop);
			if (direction == EnumFacing.UP) {
				continue;
			}
			if (direction == EnumFacing.DOWN) {
				continue;
			}
			for (int i = 0; i < rotation; ++i) {
				direction = direction.rotateY();
			}
			return state.withProperty(prop, direction);
		}
		return state;
	}
}
