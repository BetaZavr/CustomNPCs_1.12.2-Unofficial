package noppes.npcs.util;

import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.BlockSlab;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicBlockData;

public class BuilderData {

	// General
	private int type; // 0:remover; 1:builder; 2:replace; 3:placer; 4:saver
	private int id;
	
	public int[] region = new int[] { 5, 2, 3 };
	public int facing = 0;
	public NpcMiscInventory inv = new NpcMiscInventory(10);
	public EntityPlayer player = null;
	public boolean addAir = false, replaceAir = false, isSolid = false;
	public Map<Integer, Integer> chances = new TreeMap<>();
	private final Random rnd = new Random();
	// Schematic
	public Map<Integer, BlockPos> schMap = new TreeMap<>();
	public String schematicName = "";
	// undo / redo
	public int doPos = 0;
	public Map<Integer, List<SchematicBlockData>> doMap = new TreeMap<>();
	public Map<Integer, List<Entity>> enMap = new TreeMap<>();
	// technical
	private long lastWork = 0L, lastMessage = 0L;

	public BuilderData(int id, int type) {
		this.id = id;
		this.type = type;
	}

	public void add(List<SchematicBlockData> listB, List<Entity> listE) {
		if (this.doPos == 9) {
			this.doMap.remove(0);
			this.enMap.remove(0);
			Map<Integer, List<SchematicBlockData>> db = new TreeMap<>();
			Map<Integer, List<Entity>> de = new TreeMap<>();
			for (int i = 0; i < 9; i++) {
				db.put(i, this.doMap.get(i + 1));
				de.put(i, this.enMap.get(i + 1));
			}
			this.doMap = db;
			this.enMap = de;
		} else {
			this.doPos++;
			if (this.doMap.containsKey(this.doPos + 1)) {
				for (int i = this.doPos + 1; this.doMap.containsKey(i); i++) {
					this.doMap.remove(i);
					this.enMap.remove(i);
				}
			}
		}
		this.doMap.put(this.doPos, listB);
		this.enMap.put(this.doPos, listE);
	}

	public int[] getDirections(EntityPlayer player) { // startX, startY, startZ
		int[] d = new int[] { 0, 0, 0, 0, 0, 0 };
		if (player == null) {
			return d;
		}
		int vertical = player.rotationPitch < -45 ? 1 : player.rotationPitch > 45 ? 2 : 0;
		switch (player.getHorizontalFacing()) {
		case SOUTH: {
			if (vertical == 1) { // down
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
				}
				}
			} else if (vertical == 2) { // up
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
				}
				}
			} else { // wall
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * this.region[1] + 1;
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
				}
				}
			}
			break;
		}
		case EAST: {
			if (vertical == 1) { // down
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
				}
				}
			} else if (vertical == 2) { // up
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
				}
				}
			} else { // wall
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * this.region[1] + 1;
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
				}
				}
			}
			break;
		}
		case NORTH: {
			if (vertical == 1) { // down
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
				}
				}
			} else if (vertical == 2) { // up
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[1];
					d[5] = this.region[2];
				}
				}
			} else { // wall
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * this.region[1] + 1;
					d[3] = this.region[0];
					d[4] = this.region[2];
					d[5] = this.region[1];
				}
				}
			}
			break;
		}
		case WEST: {
			if (vertical == 1) { // down
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
				}
				}
			} else if (vertical == 2) { // up
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[0] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[1] = -1 * this.region[1] + 1;
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[2];
					d[4] = this.region[1];
					d[5] = this.region[0];
				}
				}
			} else { // wall
				switch (this.facing) {
				case 1: { // center
					d[0] = -1 * (int) Math.floor((double) this.region[1] / 2.0d);
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
					break;
				}
				case 2: { // on yourself
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
					break;
				}
				default: { // away
					d[0] = -1 * this.region[1] + 1;
					d[1] = -1 * (int) Math.floor((double) this.region[2] / 2.0d);
					d[2] = -1 * (int) Math.floor((double) this.region[0] / 2.0d);
					d[3] = this.region[1];
					d[4] = this.region[2];
					d[5] = this.region[0];
				}
				}
			}
			break;
		}
		default: {
		}
		}
		return d;
	}

	public NBTTagCompound getNbt() {
		NBTTagCompound nbtData = new NBTTagCompound();
		nbtData.setInteger("BuilderType", this.type);
		nbtData.setInteger("BuilderFasing", this.facing);
		nbtData.setIntArray("Region", this.region);
		nbtData.setInteger("ID", this.id);
		nbtData.setBoolean("AddAir", this.addAir);
		nbtData.setBoolean("ReplaceAir", this.replaceAir);
		nbtData.setBoolean("IsSolid", this.isSolid);

		NBTTagCompound sch = new NBTTagCompound();
		sch.setString("FileName", this.schematicName);
		NBTTagList selectMap = new NBTTagList();
		for (BlockPos pos : this.schMap.values()) {
			selectMap.appendTag(new NBTTagIntArray(new int[] { pos.getX(), pos.getY(), pos.getZ() }));
		}
		sch.setTag("SelectMap", selectMap);
		nbtData.setTag("Schematic", sch);

		NBTTagList chList = new NBTTagList();
		for (int slot : this.chances.keySet()) {
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("Slot", slot);
			c.setInteger("Value", this.chances.get(slot));
			chList.appendTag(c);
		}
		nbtData.setTag("Chances", chList);
		if (this.type < 3) {
			nbtData.setTag("Inventory", this.inv.getToNBT());
		}
		nbtData.setString("PlayerName", this.player == null ? "null" : this.player.getName());
		return nbtData;
	}

	public void read(NBTTagCompound nbtData) {
		if (nbtData.hasKey("BuilderType", 3)) {
			this.type = nbtData.getInteger("BuilderType");
		}
		if (nbtData.hasKey("BuilderFasing", 3)) {
			this.facing = nbtData.getInteger("BuilderFasing");
		}
		if (nbtData.hasKey("Region", 11)) {
			this.region = nbtData.getIntArray("Region");
		}
		if (nbtData.hasKey("ID", 8)) {
			this.id = nbtData.getInteger("ID");
		}
		if (nbtData.hasKey("AddAir", 1)) {
			this.addAir = nbtData.getBoolean("AddAir");
		}
		if (nbtData.hasKey("ReplaceAir", 1)) {
			this.replaceAir = nbtData.getBoolean("ReplaceAir");
		}
		if (nbtData.hasKey("IsSolid", 1)) {
			this.isSolid = nbtData.getBoolean("IsSolid");
		}
		if (nbtData.hasKey("Schematic", 10)) {
			NBTTagCompound sch = nbtData.getCompoundTag("Schematic");
			if (sch.hasKey("FileName", 8)) {
				this.schematicName = sch.getString("FileName");
			}
			if (sch.hasKey("SelectMap", 9)) {
				this.schMap.clear();
				for (int i = 0; i < sch.getTagList("SelectMap", 11).tagCount(); i++) {
					int[] pos = sch.getTagList("SelectMap", 11).getIntArrayAt(i);
					this.schMap.put(i, new BlockPos(pos[0], pos[1], pos[2]));
				}
			}
		}
		if (nbtData.hasKey("Chances", 9)) {
			this.chances.clear();
			for (int i = 0; i < nbtData.getTagList("Chances", 10).tagCount(); i++) {
				NBTTagCompound c = nbtData.getTagList("Chances", 10).getCompoundTagAt(i);
				this.chances.put(c.getInteger("Slot"), c.getInteger("Value"));
			}
		}
		if (nbtData.hasKey("Inventory", 10)) {
			this.inv.setFromNBT(nbtData.getCompoundTag("Inventory"));
		}
	}

	public void redo() {
		if (this.doPos < 0) {
			this.doPos = 0;
		}
		if (!this.doMap.containsKey(this.doPos + 1)) {
			return;
		}
		List<SchematicBlockData> listB = new ArrayList<>();
		List<Entity> listE = new ArrayList<>();
		// Get Zone
		int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE, mz = Integer.MAX_VALUE;
		int nx = Integer.MIN_VALUE, ny = Integer.MIN_VALUE, nz = Integer.MIN_VALUE;
		World world = null;
		if (this.player != null) {
			world = this.player.world;
		}
		for (SchematicBlockData bd : this.doMap.get(this.doPos + 1)) {
			if (world == null && bd.world != null) {
				world = bd.world;
			}
			if (mx > bd.pos.getX()) {
				mx = bd.pos.getX();
			}
			if (nx < bd.pos.getX()) {
				nx = bd.pos.getX();
			}
			if (my > bd.pos.getY()) {
				my = bd.pos.getY();
			}
			if (ny < bd.pos.getY()) {
				ny = bd.pos.getY();
			}
			if (mz > bd.pos.getZ()) {
				mz = bd.pos.getZ();
			}
			if (nz < bd.pos.getZ()) {
				nz = bd.pos.getZ();
			}
		}
		// remove Entity
		if (world != null) {
			List<Entity> list = new ArrayList<>();
			try {
				list = world.getEntitiesWithinAABB(Entity.class,
						new AxisAlignedBB(mx - 0.5d, my - 0.5d, mz - 0.5d, nx + 0.5d, ny + 1.5d, nz + 1.5d));
			}
			catch (Exception ignored) { }
			for (Entity e : list) {
				if (e instanceof EntityThrowable || e instanceof EntityArrow || e instanceof EntityPlayer) {
					continue;
				}
				listE.add(e);
				e.isDead = true;
			}
		}
		// Set Blocks
		for (SchematicBlockData bd : this.doMap.get(this.doPos + 1)) {
			listB.add(new SchematicBlockData(bd.world, bd.world.getBlockState(bd.pos), bd.pos));
			bd.set(bd.pos);
		}
		// Spawn Entities
		if (world != null) {
			for (Entity entity : this.enMap.get(this.doPos + 1)) {
				entity.isDead = false;
				UUID uuid = entity.getUniqueID();
				while (uuid != null) {
					boolean has = false;
					for (Entity e : world.loadedEntityList) {
						if (e.getUniqueID().equals(entity.getUniqueID())) {
							uuid = UUID.randomUUID();
							entity.setUniqueId(uuid);
							has = true;
							break;
						}
					}
					if (has) {
						continue;
					}
					uuid = null;
				}
				world.spawnEntity(entity);
			}
		}
		this.enMap.put(this.doPos + 1, listE);
		this.doMap.put(this.doPos + 1, listB);
		if (this.player != null) {
			this.player.sendMessage(
					new TextComponentTranslation("builder.end.redo", "" + (this.doPos + 2), "" + listB.size()));
		}
		this.doPos++;
	}

	public void saveBlocks(EntityPlayerMP player, BlockPos pos, int size) { // Schematic Save
		if (this.schematicName.isEmpty()) {
			this.sendMessage("builder.err.file.name");
			return;
		}
		if (this.schMap.size() != 3) {
			String x = "" + pos.getX();
			String y = "" + pos.getY();
			String z = "" + pos.getZ();
			switch (this.schMap.size()) {
			case 1: {
				this.schMap.put(1, pos);
				player.sendMessage(new TextComponentTranslation("builder.set.point.1", x, y, z, this.schematicName));
				break;
			}
			case 2: {
				BlockPos p = this.schMap.get(1);
				if (p.equals(pos)) {
					return;
				}
				player.sendMessage(new TextComponentTranslation("builder.set.point.2", x, y, z, this.schematicName));
				this.schMap.put(2, pos);
				break;
			}
			default: {
				player.sendMessage(new TextComponentTranslation("builder.set.point.0", x, y, z, this.schematicName));
				this.schMap.put(0, pos);
			}
			}
			this.lastWork = System.currentTimeMillis();
			Server.sendData(player, EnumPacketClient.SYNC_UPDATE, EnumSync.BuilderData, this.getNbt());
			return;
		}
		this.lastWork = System.currentTimeMillis() - size;
		Schematic schema = Schematic.create(player.world, player.getHorizontalFacing(), this.schematicName + ".schematic", this.schMap);
		Server.sendData(player, EnumPacketClient.SAVE_SCHEMATIC, schema.getNBT());
	}

	public void sendMessage(String text, Object... obj) {
		if (this.lastMessage + 1000 > System.currentTimeMillis() || this.player == null) {
			return;
		}
		this.lastMessage = System.currentTimeMillis();
		this.player.sendMessage(new TextComponentTranslation(text, obj));
	}

	public void setBlocks(EntityPlayer player, BlockPos pos) { // Del
		int[] d = this.getDirections(player);
		int cx = 0, cy = 0, cz = 0;
		int size = this.region[0] * this.region[1] * this.region[2];
		List<SchematicBlockData> listB = new ArrayList<>();
		List<Entity> listE = new ArrayList<>();
		// remove Entity
		List<Entity> list = new ArrayList<>();
		try {
			list = player.world.getEntitiesWithinAABB(Entity.class,
					new AxisAlignedBB(d[0] - 0.25d, d[1] - 0.25d, d[2] - 0.25d, d[3] + 0.25d, d[4] + 0.25d, d[5] + 0.25d).offset(pos));
		}
		catch (Exception ignored) { }
		for (Entity e : list) {
			if (e instanceof EntityThrowable || e instanceof EntityArrow || e instanceof EntityPlayer) {
				continue;
			}
			listE.add(e);
			e.isDead = true;
		}
		// Create block data to work
		Map<Integer, SchematicBlockData> tempBlocks = new HashMap<>();
		SchematicBlockData main = null;
		if (this.type != 0) {
			int total = 0, mPos = -1, max = -1;
			Map<Integer, Integer> bls = new HashMap<>(); // [slot, chance]
			if (!this.inv.getStackInSlot(0).isEmpty()) {
                Block.getBlockFromItem(this.inv.getStackInSlot(0).getItem());
                main = new SchematicBlockData(player.world, this.inv.getStackInSlot(0));
            }
			for (int i = 1; i < 10; i++) {
				ItemStack stack = this.inv.getStackInSlot(i);
				if (stack.isEmpty()) {
					continue;
				} else {
                    Block.getBlockFromItem(stack.getItem());
                }
                int c = 100;
				if (this.chances.containsKey(i)) {
					c = this.chances.get(i);
				}
				total += c;
				if (max < c) {
					max = c;
					mPos = i;
				}
				bls.put(i, c);
			}
			if (this.addAir) {
				int airV = 100;
				if (!bls.isEmpty()) {
					airV = total / bls.size();
				}
				total += airV;
				bls.put(mPos + 1, airV);
			}
			if (bls.isEmpty() && (this.type == 1 || this.type == 2)) {
				this.sendMessage("builder.err.not.blocks");
				return;
			}
			// now bls [slot, count block]
			int fix = 0;
			for (int slot : bls.keySet()) {
				int v = size * bls.get(slot) / total;
				fix += v;
				bls.put(slot, v);
			}
			if (fix < size && mPos >= 0) {
				bls.put(mPos, bls.get(mPos) + size - fix);
			}
			Map<Integer, SchematicBlockData> amount = new HashMap<>(); // [slot, block]
			List<Integer> slots = new ArrayList<>();
			for (int slot : bls.keySet()) {
				SchematicBlockData bd;
				if (slot >= 10) { // Air
					bd = new SchematicBlockData(player.world, new ItemStack(Blocks.AIR));
				} else {
					bd = new SchematicBlockData(player.world, this.inv.getStackInSlot(slot));
				}
				amount.put(slot, bd);
				slots.add(slot);
			}
			for (int i = 0; i < size; i++) {
				int slot = slots.get(this.rnd.nextInt(slots.size()));
				SchematicBlockData bd = amount.get(slot);
				bls.put(slot, bls.get(slot) - 1);
				if (bls.get(slot) <= 0) {
					slots.remove((Integer) slot);
				}
				tempBlocks.put(i, bd);
			}
		} else {
			for (int i = 1; i < 10; i++) {
				ItemStack stack = this.inv.getStackInSlot(i);
				if (stack.isEmpty()) {
					continue;
				} else {
                    Block.getBlockFromItem(stack.getItem());
                }
                tempBlocks.put(i, new SchematicBlockData(player.world, stack));
			}
		}
		if (tempBlocks.isEmpty() && this.type != 0) {
			this.sendMessage("builder.err.not.blocks");
			return;
		}
		int sum = 0;
		// Try set blocks
		while (cy < d[4]) {
			while (cz < d[5]) {
				while (cx < d[3]) {
					BlockPos p = new BlockPos(pos.getX() + d[0] + cx, pos.getY() + d[1] + cy, pos.getZ() + d[2] + cz);
					IBlockState state = player.world.getBlockState(p);
					cx++;
					sum++;
					if (this.type == 0) { // delete
						if (state.getBlock() == Blocks.AIR) {
							continue;
						}
						if (!tempBlocks.isEmpty()) {
							for (SchematicBlockData bd : tempBlocks.values()) {
								if (bd.state.getBlock() == state.getBlock()) {
									listB.add(new SchematicBlockData(player.world, state, p));
									player.world.setBlockState(p, Blocks.AIR.getDefaultState());
									break;
								}
							}
						} else {
							listB.add(new SchematicBlockData(player.world, state, p));
							player.world.setBlockState(p, Blocks.AIR.getDefaultState());
						}
					} else if (this.type == 1) { // set
						SchematicBlockData bd = tempBlocks.get(sum - 1);
						listB.add(new SchematicBlockData(player.world, state, p));
						bd.pos = new BlockPos(p);
						bd.world = player.world;
						bd.set(bd.pos);
					} else if (this.type == 2) { // replace
						if (!this.replaceAir && state.getBlock() == Blocks.AIR) {
							continue;
						}
						if (main != null && !main.state.getBlock().equals(state.getBlock())) {
							continue;
						}
						if (!tempBlocks.isEmpty()) {
							SchematicBlockData bd = tempBlocks.get(this.rnd.nextInt(tempBlocks.size()));
							listB.add(new SchematicBlockData(player.world, state, p));
							bd.pos = new BlockPos(p);
							bd.world = player.world;
							try {
								if (state.getBlock() instanceof BlockSlab) {
									bd.state.withProperty(BlockSlab.HALF, state.getValue(BlockSlab.HALF));
								}
							} catch (Exception e) { LogWriter.error("Error:", e); }
							bd.set(bd.pos);
						} else {
							SchematicBlockData bd = new SchematicBlockData(player.world, ItemStack.EMPTY);
							listB.add(new SchematicBlockData(player.world, state, p));
							bd.pos = new BlockPos(p);
							bd.world = player.world;
							bd.set(bd.pos);
						}
					}
				}
				cz++;
				cx = 0;
			}
			cy++;
			cz = 0;
		}
		this.sendMessage("builder.end.work." + (!listB.isEmpty()), "" + listB.size());
		if (!listB.isEmpty() || !listE.isEmpty()) {
			this.add(listB, listE);
		}
	}

	public void undo() {
		if (this.doPos > 9) {
			this.doPos = 9;
		}
		if (!this.doMap.containsKey(this.doPos)) {
			return;
		}
		List<SchematicBlockData> listB = new ArrayList<>();
		List<Entity> listE = new ArrayList<>();
		// Get Zone
		int mx = Integer.MAX_VALUE, my = Integer.MAX_VALUE, mz = Integer.MAX_VALUE;
		int nx = Integer.MIN_VALUE, ny = Integer.MIN_VALUE, nz = Integer.MIN_VALUE;
		World world = null;
		if (this.player != null) {
			world = this.player.world;
		}
		for (SchematicBlockData bd : this.doMap.get(this.doPos)) {
			if (world == null && bd.world != null) {
				world = bd.world;
			}
			if (mx > bd.pos.getX()) {
				mx = bd.pos.getX();
			}
			if (nx < bd.pos.getX()) {
				nx = bd.pos.getX();
			}
			if (my > bd.pos.getY()) {
				my = bd.pos.getY();
			}
			if (ny < bd.pos.getY()) {
				ny = bd.pos.getY();
			}
			if (mz > bd.pos.getZ()) {
				mz = bd.pos.getZ();
			}
			if (nz < bd.pos.getZ()) {
				nz = bd.pos.getZ();
			}
		}
		// remove Entity
		if (world != null) {
			List<Entity> list = new ArrayList<>();
			try {
				list = world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(mx - 0.5d, my - 0.5d, mz - 0.5d, nx + 0.5d, ny + 1.5d, nz + 1.5d));
			}
			catch (Exception ignored) { }
			for (Entity e : list) {
				if (e instanceof EntityThrowable || e instanceof EntityArrow || e instanceof EntityPlayer) {
					continue;
				}
				listE.add(e);
				e.isDead = true;
			}
		}
		// Set Blocks
		for (SchematicBlockData bd : this.doMap.get(this.doPos)) {
			listB.add(new SchematicBlockData(bd.world, bd.world.getBlockState(bd.pos), bd.pos));
			bd.set(bd.pos);
		}
		// Spawn Entities
		if (world != null) {
			for (Entity entity : this.enMap.get(this.doPos)) {
				entity.isDead = false;
				UUID uuid = entity.getUniqueID();
				while (uuid != null) {
					boolean has = false;
					for (Entity e : world.loadedEntityList) {
						if (e.getUniqueID().equals(entity.getUniqueID())) {
							uuid = UUID.randomUUID();
							entity.setUniqueId(uuid);
							has = true;
							break;
						}
					}
					if (has) {
						continue;
					}
					uuid = null;
				}
				world.spawnEntity(entity);
			}
		}
		this.enMap.put(this.doPos, listE);
		this.doMap.put(this.doPos, listB);
		this.doPos--;
		if (this.player != null) {
			this.player.sendMessage(
					new TextComponentTranslation("builder.end.undo", "" + (this.doPos + 1), "" + listB.size()));
		}
	}

	public void work(BlockPos pos, EntityPlayerMP player) {
		this.player = player;
		int size = this.region[0] * this.region[1] * this.region[2];
		if (size > 2000) {
			size = 2000;
		}
		size = (int) (0.875d * (double) size + 250.0d);
		if (this.lastWork + size > System.currentTimeMillis()) {
			this.sendMessage("builder.wait", Util.instance.ticksToElapsedTime(this.lastWork + size - System.currentTimeMillis(), true, true, false));
			return;
		}
		this.lastWork = System.currentTimeMillis();
		if (this.type == 3) {
			this.lastWork = System.currentTimeMillis() - size;
			Server.sendData(player, EnumPacketClient.GET_SCHEMATIC);
		} else if (this.type == 4) {
			this.saveBlocks(player, pos, size);
		} else {
			this.setBlocks(player, pos);
		}
	}

	public int getID() { return this.id; }
	
	public int getType() { return this.type; }

}
