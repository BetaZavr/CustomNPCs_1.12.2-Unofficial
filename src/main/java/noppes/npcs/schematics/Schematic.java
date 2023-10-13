package noppes.npcs.schematics;

import java.io.File;
import java.io.FileOutputStream;
import java.util.List;
import java.util.Map;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityHanging;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.NBTJsonUtil;

public class Schematic implements ISchematic {

	public short[] blockIdsArray = new short[0];
	public byte[] blockMetadataArray = new byte[0];
	public NBTTagList tileList = new NBTTagList();
	public NBTTagList entityList = new NBTTagList();
	public short height = 0; // Y axis
	public short length = 0; // Z axis
	public short width = 0; // X axis
	public String name = "";
	public BlockPos offset = BlockPos.ORIGIN;

	public Schematic(String name) { this.name = name; }
	
	public static Schematic create(World world, EnumFacing fase, String name, Map<Integer, BlockPos> schMap) {
		BlockPos p = schMap.get(0); // offset
		BlockPos m = schMap.get(1); // min
		BlockPos n = schMap.get(2); // max
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
					x = i / length - y * width;
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
			IBlockState state = SchematicWrapper.rotationState(world.getBlockState(pos.add(x, y, z)), rot);
			schema.blockIdsArray[i] = (short) Block.REGISTRY.getIDForObject(state.getBlock());
			schema.blockMetadataArray[i] = (byte) state.getBlock().getMetaFromState(state);
			if (state.getBlock() instanceof ITileEntityProvider) {
				TileEntity tile = world.getTileEntity(pos.add(x, y, z));
				NBTTagCompound nbtTile = new NBTTagCompound();
				if (tile!=null) {
					tile.writeToNBT(nbtTile);
					int newX = i % schema.width;
					int newZ = (i - newX) / schema.width % schema.length;
					nbtTile.setInteger("x", newX);
					nbtTile.setInteger("y", y);
					nbtTile.setInteger("z", newZ);
				}
				schema.tileList.appendTag(nbtTile);
			}
		}
		
		/** Added by mod */
		schema.offset = new BlockPos(bb.minX-p.getX(), 1 + (int) (bb.minY-p.getY()), (int) (bb.minZ-p.getZ()));
		switch(fase) {
			case EAST: {
				schema.offset = new BlockPos(p.getZ()-bb.maxZ, (int) (bb.minY-p.getY()), (int) (bb.minX-p.getX()));
				break;
			}
			case NORTH: {
				schema.offset = new BlockPos(p.getX()-bb.maxX, (int) (bb.minY-p.getY()), (int) (p.getZ()-bb.maxZ));
				break;
			}
			case WEST: {
				schema.offset = new BlockPos(bb.minZ-p.getZ(), (int) (bb.minY-p.getY()), (int) (p.getX()-bb.maxX));
				break;
			}
			default: { // SOUTH
				schema.offset = new BlockPos(bb.minX-p.getX(), (int) (bb.minY-p.getY()), (int) (bb.minZ-p.getZ()));
				break;
			}
		}
		// Get Entitys:
		schema.entityList = new NBTTagList();
		AxisAlignedBB bbE = new AxisAlignedBB(bb.minX-0.25d, bb.minY-0.25d, bb.minZ-0.25d, bb.maxX+0.25d, bb.maxY+0.25d, bb.maxZ+0.25d);
		List<Entity> list = world.getEntitiesWithinAABB(Entity.class, bbE);
		for (Entity e : list) {
			if (e instanceof EntityThrowable || e instanceof EntityProjectile || e instanceof EntityArrow || e instanceof EntityPlayer) { continue; }
			NBTTagCompound nbtEntity = new NBTTagCompound();
			if (!e.writeToNBTAtomically(nbtEntity)) {
				nbtEntity = e.writeToNBT(new NBTTagCompound());
				ResourceLocation regName = EntityList.getKey(e);
				if (regName==null) { continue; }
				nbtEntity.setString("id", regName.toString());
			}
			if (!nbtEntity.hasKey("UUID", 8)) { nbtEntity.setString("UUID", e.getUniqueID().toString()); }
			NBTTagList posList = new NBTTagList();
			double[] d = new double[] { e.posX-p.getX() - 1.0d, e.posY-p.getY(), e.posZ-p.getZ() - 1.0d };
			double[] ed = new double[] { d[0], d[1], d[2] };
			if (e instanceof EntityHanging) {
				d = new double[] { e.getPosition().getX()-p.getX(), e.getPosition().getY() - 1 - p.getY(), e.getPosition().getZ()-p.getZ() };
				ed = new double[] { d[0], d[1], d[2] };
				float er = nbtEntity.getTagList("Rotation", 5).getFloatAt(0);
				byte f = nbtEntity.getByte("Facing");
				switch(rot) {
					case 1:
						f += 1;
						er += 90.0f;
						ed[0] = d[2];
						ed[2] = d[0];
					break;
					case 2:
						f += 2;
						er += 180.0f;
						ed[0] *= -1.0d;
						ed[2] *= -1.0d;
						break;
					case 3:
						f += 3;
						er += 270.0f;
						ed[0] = d[2] * -1.0d;
						ed[2] = d[0] * -1.0d;
						break;
					default: break;
				}
				f %= (byte) 4;
				nbtEntity.setByte("Facing", f);
				nbtEntity.getTagList("Rotation", 5).set(0, new NBTTagFloat(er % 360.0f));
				nbtEntity.setInteger("TileX", (int) ed[0]);
				nbtEntity.setInteger("TileY", (int) ed[1]);
				nbtEntity.setInteger("TileZ", (int) ed[2]);
				posList.appendTag(new NBTTagDouble(ed[0]));
				posList.appendTag(new NBTTagDouble(ed[1]));
				posList.appendTag(new NBTTagDouble(ed[2]));
			}
			else {
				switch(rot) {
					case 1:
						ed[0] = d[2];
						ed[2] = d[0];
					break;
					case 2:
						ed[0] *= -1.0d;
						ed[0] -= 1.0d;
						ed[2] *= -1.0d;
						ed[2] -= 1.0d;
						break;
					case 3:
						ed[0] = d[2] * -1.0d;
						ed[0] -= 1.0d;
						ed[2] = d[0] * -1.0d;
						ed[2] -= 1.0d;
						break;
					default: break;
				}
				posList.appendTag(new NBTTagDouble(ed[0]-0.5d));
				posList.appendTag(new NBTTagDouble(ed[1]));
				posList.appendTag(new NBTTagDouble(ed[2]-0.5d));
			}
			nbtEntity.setTag("Pos", posList);
			schema.entityList.appendTag(nbtEntity);
		}
		return schema;
	}

	public static Schematic create(World world, String name, BlockPos pos, short height, short width, short length) {
		Schematic schema = new Schematic(name);
		schema.offset = BlockPos.ORIGIN;
		schema.height = height;
		schema.width = width;
		schema.length = length;
		int size = height * width * length;
		schema.blockIdsArray = new short[size];
		schema.blockMetadataArray = new byte[size];
		NoppesUtilServer.NotifyOPs("Creating schematic at: " + pos + " might lag slightly", new Object[0]);
		schema.tileList = new NBTTagList();
		for (int i = 0; i < size; ++i) {
			int x = i % width;
			int z = (i - x) / width % length;
			int y = ((i - x) / width - z) / length;
			IBlockState state = world.getBlockState(pos.add(x, y, z));
			if (state.getBlock() != Blocks.AIR) {
				if (state.getBlock() != CustomRegisters.copy) {
					schema.blockIdsArray[i] = (short) Block.REGISTRY.getIDForObject(state.getBlock());
					schema.blockMetadataArray[i] = (byte) state.getBlock().getMetaFromState(state);
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
		compound.setTag("TileEntities", this.tileList);
		compound.setTag("Entities", this.entityList);
		// New
		compound.setIntArray("Offset", new int[] { this.offset.getX(), this.offset.getY(), this.offset.getZ() });
		compound.setString("Name", this.name);
		return compound;
	}

	@Override
	public NBTTagCompound getTileEntity(int i) { return this.tileList.getCompoundTagAt(i); }

	@Override
	public int getTileEntitySize() {
		if (this.tileList == null) { return 0; }
		return this.tileList.tagCount();
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
		this.tileList = compound.getTagList("TileEntities", 10);
		this.entityList = compound.getTagList("Entities", 10);
		// New
		int[] arr = compound.getIntArray("Offset");
		if (arr!=null && arr.length>=3) { this.offset = new BlockPos(arr[0], arr[1], arr[2]); }
		else { this.offset = BlockPos.ORIGIN; }
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
	
	public boolean equals(Object obj) {
		if (obj==null || !(obj instanceof Schematic)) { return false; }
		Schematic s = (Schematic) obj;
		if (!this.name.equals(s.name) ||
				this.height!=s.height ||
				this.length!=s.length ||
				this.width!=s.width ||
				!this.offset.equals(s.offset) ||
				this.blockIdsArray.length!=s.blockIdsArray.length ||
				this.blockMetadataArray.length!=s.blockMetadataArray.length ||
				this.tileList.tagCount()!=s.tileList.tagCount() ||
				this.entityList.tagCount()!=s.entityList.tagCount()) { return false; }
		for (int i = 0; i < this.blockIdsArray.length; i++) {
			if (this.blockIdsArray[i]!=s.blockIdsArray[i]) { return false; }
		}
		for (int i = 0; i < this.blockMetadataArray.length; i++) {
			if (this.blockMetadataArray[i]!=s.blockMetadataArray[i]) { return false; }
		}
		for (int i = 0; i < this.tileList.tagCount(); i++) {
			if (!this.tileList.getCompoundTagAt(i).equals(s.tileList.getCompoundTagAt(i))) { return false; }
		}
		for (int i = 0; i < this.entityList.tagCount(); i++) {
			if (!this.entityList.getCompoundTagAt(i).equals(s.entityList.getCompoundTagAt(i))) { return false; }
		}
		return true;
	}

	@Override
	public boolean hasEntitys() { return this.entityList!=null && this.entityList.tagCount()>0; }

	@Override
	public NBTTagList getEntitys() { return this.entityList; }

	@Override
	public BlockPos getOffset() { return this.offset; }
	
}
