package noppes.npcs.schematics;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.NBTJsonUtil;

public class Schematic implements ISchematic {

	public short[] blockIdsArray;
	public byte[] blockMetadataArray;
	public NBTTagList entityList = new NBTTagList();
	public short height;
	public short length;
	public String name;

	public short width;
	public int[] offset; // New

	public Schematic(String name) { this.name = name; }
	
	// New
	public static Schematic Create(World world, EnumFacing fase, String name, Map<Integer, BlockPos> schMap) {
		BlockPos p = schMap.get(0);
		BlockPos m = schMap.get(1);
		BlockPos n = schMap.get(2);
		AxisAlignedBB bb = new AxisAlignedBB(m, n);
		short height = (short) (Math.abs(bb.maxY-bb.minY)+1);
		short width = (short) (Math.abs(bb.maxX-bb.minX)+1);
		short length = (short) (Math.abs(bb.maxZ-bb.minZ)+1);
		BlockPos pos = new BlockPos(bb.minX, bb.minY, bb.minZ);
		
		Schematic schema = new Schematic(name);
		schema.height = height;
		schema.width = (fase==EnumFacing.EAST || fase==EnumFacing.WEST) ? length : width;
		schema.length = (fase==EnumFacing.EAST || fase==EnumFacing.WEST) ? width : length;
		int size = height * width * length;
		schema.blockIdsArray = new short[size];
		schema.blockMetadataArray = new byte[size];
		int rot = 0;
		switch(fase) {
			case EAST: { rot = 1; break; }
			case NORTH: { rot = 2; break; }
			case WEST: { rot = 3; break; }
			default: { break; }
		}
		for (int i = 0; i < size; ++i) {
			int x, z;
			int y = i / (width * length);
			switch(fase) {
				case EAST: {
					x = i/length-y*width;
					z = length - 1 - i % length;
					break;
				}
				case NORTH: {
					x = width - 1 - i % width;
					z = length - 1 - (i / width) % length;
					break;
				}
				case WEST: {
					x = width - 1 - (i / length) % width;
					z = i % length;
					break;
				}
				default: { // SOUTH
					x = i % width;
					z = (i - x) / width % length;
					break;
				}
			}
			IBlockState state = world.getBlockState(pos.add(x, y, z));
			schema.blockIdsArray[i] = (short) Block.REGISTRY.getIDForObject(state.getBlock());
			schema.blockMetadataArray[i] = (byte) state.getBlock().getMetaFromState(state);
			
			if (rot!=0) { schema.blockMetadataArray[i] = SchematicController.rotate(state, rot); }
			if (state.getBlock() instanceof ITileEntityProvider) {
				TileEntity tile = world.getTileEntity(pos.add(x, y, z));
				NBTTagCompound compound = new NBTTagCompound();
				tile.writeToNBT(compound);
				compound.setInteger("x", x);
				compound.setInteger("y", y);
				compound.setInteger("z", z);
				schema.entityList.appendTag(compound);
			}
		}
		/** Added by mod */
		schema.offset = new int[] { (int) (bb.minX-p.getX()), 1 + (int) (bb.minY-p.getY()), (int) (bb.minZ-p.getZ()) };
		switch(fase) {
			case EAST: {
				schema.offset = new int[] { (int) (p.getZ()-bb.maxZ), (int) (bb.minY-p.getY()), (int) (bb.minX-p.getX()) };
				break;
			}
			case NORTH: {
				schema.offset = new int[] { (int) (p.getX()-bb.maxX), (int) (bb.minY-p.getY()), (int) (p.getZ()-bb.maxZ) };
				break;
			}
			case WEST: {
				schema.offset = new int[] { (int) (bb.minZ-p.getZ()), (int) (bb.minY-p.getY()), (int) (p.getX()-bb.maxX) };
				break;
			}
			default: { // SOUTH
				schema.offset = new int[] { (int) (bb.minX-p.getX()), (int) (bb.minY-p.getY()), (int) (bb.minZ-p.getZ()) };
				break;
			}
		}
		// Get Entitys:
		schema.entityList = new NBTTagList();
		AxisAlignedBB bbE = new AxisAlignedBB(bb.minX-0.25d, bb.minY-0.25d, bb.minZ-0.25d, bb.maxX+0.25d, bb.maxY+0.25d, bb.maxZ+0.25d);
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, bbE);
		for (Entity e : list) {
			if (e instanceof EntityThrowable || e instanceof EntityProjectile || e instanceof EntityArrow || e instanceof EntityPlayer) { continue; }
			NBTTagCompound nbtEntity = e.writeToNBT(new NBTTagCompound());
			if (!nbtEntity.hasKey("UUID", 8)) { nbtEntity.setString("UUID", e.getUniqueID().toString()); }
			NBTTagList posList = new NBTTagList();
			if (nbtEntity.getString("id").equals("minecraft:painting") || nbtEntity.getString("id").equals("minecraft:item_frame")) {
				nbtEntity.setInteger("TileX", nbtEntity.getInteger("TileX")-p.getX());
				nbtEntity.setInteger("TileY", nbtEntity.getInteger("TileY")-p.getY());
				nbtEntity.setInteger("TileZ", nbtEntity.getInteger("TileZ")-p.getZ());
				posList.appendTag(new NBTTagDouble(nbtEntity.getInteger("TileX")));
				posList.appendTag(new NBTTagDouble(nbtEntity.getInteger("TileY")));
				posList.appendTag(new NBTTagDouble(nbtEntity.getInteger("TileZ")));
			}
			else {
				posList.appendTag(new NBTTagDouble(e.getPosition().getX()-p.getX()-0.5d));
				posList.appendTag(new NBTTagDouble(e.getPosition().getY()-p.getY()));
				posList.appendTag(new NBTTagDouble(e.getPosition().getZ()-p.getZ()-0.5d));
			}
			nbtEntity.setTag("Pos", posList);
			schema.entityList.appendTag(nbtEntity);
		}
		return schema;
	}

	// Parent
	public static Schematic Create(World world, String name, BlockPos pos, short height, short width, short length) {
		Schematic schema = new Schematic(name);
		schema.offset = new int[] { 0, 0, 0, 0, 0, 0 };
		schema.height = height;
		schema.width = width;
		schema.length = length;
		int size = height * width * length;
		schema.blockIdsArray = new short[size];
		schema.blockMetadataArray = new byte[size];
		NoppesUtilServer.NotifyOPs("Creating schematic at: " + pos + " might lag slightly", new Object[0]);
		schema.entityList = new NBTTagList();
		for (int i = 0; i < size; ++i) {
			int x = i % width;
			int z = (i - x) / width % length;
			int y = ((i - x) / width - z) / length;
			IBlockState state = world.getBlockState(pos.add(x, y, z));
			if (state.getBlock() != Blocks.AIR) {
				if (state.getBlock() != CustomItems.copy) {
					schema.blockIdsArray[i] = (short) Block.REGISTRY.getIDForObject(state.getBlock());
					schema.blockMetadataArray[i] = (byte) state.getBlock().getMetaFromState(state);
					if (state.getBlock() instanceof ITileEntityProvider) {
						TileEntity tile = world.getTileEntity(pos.add(x, y, z));
						NBTTagCompound compound = new NBTTagCompound();
						tile.writeToNBT(compound);
						compound.setInteger("x", x);
						compound.setInteger("y", y);
						compound.setInteger("z", z);
						schema.entityList.appendTag(compound);
					}
				}
			}
		}
		return schema;
	}

	public byte[][] getBlockBytes() {
		byte[] blocks = new byte[this.blockIdsArray.length];
		byte[] addBlocks = null;
		for (int i = 0; i < blocks.length; ++i) {
			short id = this.blockIdsArray[i];
			if (id > 255) {
				if (addBlocks == null) { addBlocks = new byte[(blocks.length >> 1) + 1]; }
				if ((i & 0x1) == 0x0) {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF0) | (id >> 8 & 0xF));
				} else {
					addBlocks[i >> 1] = (byte) ((addBlocks[i >> 1] & 0xF) | (id >> 8 & 0xF) << 4);
				}
			}
			blocks[i] = (byte) id;
		}
		if (addBlocks == null) { return new byte[][] { blocks }; }
		return new byte[][] { blocks, addBlocks };
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getBlockState(int i) {
		Block b = Block.getBlockById(this.blockIdsArray[i]);
		if (b == null) { return Blocks.AIR.getDefaultState(); }
		if (i<b.getBlockState().getValidStates().size()) {
			return b.getBlockState().getValidStates().get(i);
		}
		return b.getStateFromMeta(this.blockMetadataArray[i]);
	}

	@SuppressWarnings("deprecation")
	@Override
	public IBlockState getBlockState(int x, int y, int z) {
		int i = this.xyzToIndex(x, y, z);
		Block b = Block.getBlockById(this.blockIdsArray[i]);
		if (b == null) {
			return Blocks.AIR.getDefaultState();
		}
		return b.getStateFromMeta(this.blockMetadataArray[i]);
	}

	@Override
	public short getHeight() { return this.height; }

	@Override
	public short getLength() { return this.length; }

	@Override
	public String getName() { return this.name; }

	@Override
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setShort("Width", this.width);
		compound.setShort("Height", this.height);
		compound.setShort("Length", this.length);
		byte[][] arr = this.getBlockBytes();
		compound.setByteArray("Blocks", arr[0]);
		if (arr.length > 1) { compound.setByteArray("AddBlocks", arr[1]); }
		compound.setByteArray("Data", this.blockMetadataArray);
		compound.setTag("TileEntities", this.entityList);
		// New
		compound.setIntArray("Offset", this.offset);
		compound.setString("Name", this.name);
		return compound;
	}

	@Override
	public NBTTagCompound getTileEntity(int i) { return this.entityList.getCompoundTagAt(i); }

	@Override
	public int getTileEntitySize() {
		if (this.entityList == null) { return 0; }
		return this.entityList.tagCount();
	}

	@Override
	public short getWidth() { return this.width; }

	public void load(NBTTagCompound compound) {
		this.width = compound.getShort("Width");
		this.height = compound.getShort("Height");
		this.length = compound.getShort("Length");
		byte[] addId = compound.hasKey("AddBlocks") ? compound.getByteArray("AddBlocks") : new byte[0];
		this.setBlockBytes(compound.getByteArray("Blocks"), addId);
		this.blockMetadataArray = compound.getByteArray("Data");
		this.entityList = compound.getTagList("TileEntities", 10);
		// New
		this.offset = compound.getIntArray("Offset");
		this.name = compound.getString("Name");
	}

	public void setBlockBytes(byte[] blockId, byte[] addId) {
		this.blockIdsArray = new short[blockId.length];
		for (int index = 0; index < blockId.length; ++index) {
			short id = (short) (blockId[index] & 0xFF);
			if (index >> 1 < addId.length) {
				if ((index & 0x1) == 0x0) {
					id += ((addId[index >> 1] & 0xF) << 8);
				} else {
					id += ((addId[index >> 1] & 0xF0) << 4);
				}
			}
			this.blockIdsArray[index] = id;
		}
	}

	public int xyzToIndex(int x, int y, int z) { return (y * this.length + z) * this.width + x; }

	public void save(EntityPlayer player) {
		if (player==null || player.world==null || !player.world.isRemote) { return; }
		try {
			File file = new File(SchematicController.getDir(), this.name);
			CompressedStreamTools.writeCompressed(this.getNBT(), new FileOutputStream(file));
			ITextComponent component = new TextComponentString("Save Schematic file: \""+file+"\"");
			component.getStyle().setColor(TextFormatting.GRAY);
			player.sendMessage(component);
			if (SchematicController.Instance.map.containsKey(this.name)) {
				SchematicController.Instance.map.put(this.name, new SchematicWrapper(this));
			}
			
			if (CustomNpcs.VerboseDebug) {
				file = new File(SchematicController.getDir(), this.name.replace(".schematic", "")+".json");
				NBTJsonUtil.SaveFile(file, this.getNBT());
			}
		}
		catch (Exception e) { }
	}
	
}
