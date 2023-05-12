package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import com.google.common.collect.Lists;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.schematics.SchematicWrapper;

public class TileBuilder
extends TileEntity
implements ITickable {
	
	public static List<BlockPos> DrawPoses = Lists.<BlockPos>newArrayList();
	public Availability availability;
	public boolean enabled;
	public boolean finished;
	private Stack<Integer> positions;
	private Stack<Integer> positionsSecond;
	public int rotation;
	private SchematicWrapper schematic;
	public boolean started;
	private int ticks;
	public int yOffest;

	public static void SetDrawPos(BlockPos pos) {
		if (!TileBuilder.has(pos)) {
			TileBuilder.DrawPoses.add(pos);
		}
	}
	public static boolean has(BlockPos pos) {
		if (pos==null) { return false; }
		for (BlockPos p : TileBuilder.DrawPoses) {
			if (p!=null && p.equals(pos)) { return true; }
		}
		return false;
	}

	public TileBuilder() {
		this.schematic = null;
		this.rotation = 0;
		this.yOffest = 0;
		this.enabled = false;
		this.started = false;
		this.finished = false;
		this.availability = new Availability();
		this.positions = new Stack<Integer>();
		this.positionsSecond = new Stack<Integer>();
		this.ticks = 20;
	}

	public Stack<BlockData> getBlock() {
		if (!this.enabled || this.finished || !this.hasSchematic()) {
			return null;
		}
		boolean bo = this.positions.isEmpty();
		Stack<BlockData> list = new Stack<BlockData>();
		int size = this.schematic.schema.getWidth() * this.schematic.schema.getLength() / 4;
		if (size > 30) {
			size = 30;
		}
		for (int i = 0; i < size; ++i) {
			if ((this.positions.isEmpty() && !bo) || (this.positionsSecond.isEmpty() && bo)) {
				return list;
			}
			int pos = bo ? this.positionsSecond.pop() : (this.positions.pop());
			if (pos < this.schematic.size) {
				int x = pos % this.schematic.schema.getWidth();
				int z = (pos - x) / this.schematic.schema.getWidth() % this.schematic.schema.getLength();
				int y = ((pos - x) / this.schematic.schema.getWidth() - z) / this.schematic.schema.getLength();
				IBlockState state = this.schematic.schema.getBlockState(x, y, z);
				if (!state.isFullBlock() && !bo && state.getBlock() != Blocks.AIR) {
					this.positionsSecond.add(0, pos);
				} else {
					BlockPos blockPos = this.getPos().add(1, this.yOffest, 1)
							.add((Vec3i) this.schematic.rotatePos(x, y, z, this.rotation));
					IBlockState original = this.world.getBlockState(blockPos);
					if (Block.getStateId(state) != Block.getStateId(original)) {
						state = this.schematic.rotationState(state, this.rotation);
						NBTTagCompound tile = null;
						if (state.getBlock() instanceof ITileEntityProvider) {
							tile = this.schematic.getTileEntity(x, y, z, blockPos);
						}
						list.add(0, new BlockData(blockPos, state, tile));
					}
				}
			}
		}
		return list;
	}

	private List<EntityPlayer> getPlayerList() {
		return this.world.getEntitiesWithinAABB(EntityPlayer.class,
				new AxisAlignedBB(this.pos.getX(), this.pos.getY(), this.pos.getZ(), (this.pos.getX() + 1),
						(this.pos.getY() + 1), (this.pos.getZ() + 1)).grow(10.0, 10.0, 10.0));
	}

	public SchematicWrapper getSchematic() {
		return this.schematic;
	}

	public boolean hasSchematic() {
		return this.schematic != null;
	}

	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("SchematicName")) {
			this.schematic = SchematicController.Instance.load(compound.getString("SchematicName"));
		}
		Stack<Integer> positions = new Stack<Integer>();
		positions.addAll(NBTTags.getIntegerList(compound.getTagList("Positions", 10)));
		this.positions = positions;
		positions = new Stack<Integer>();
		positions.addAll(NBTTags.getIntegerList(compound.getTagList("PositionsSecond", 10)));
		this.positionsSecond = positions;
		this.readPartNBT(compound);
	}

	public void readPartNBT(NBTTagCompound compound) {
		this.rotation = compound.getInteger("Rotation");
		this.yOffest = compound.getInteger("YOffset");
		this.enabled = compound.getBoolean("Enabled");
		this.started = compound.getBoolean("Started");
		this.finished = compound.getBoolean("Finished");
		this.availability.readFromNBT(compound.getCompoundTag("Availability"));
	}

	@SideOnly(Side.CLIENT)
	public void setDrawSchematic(SchematicWrapper schematics) {
		this.schematic = schematics;
	}

	public void setSchematic(SchematicWrapper schematics) {
		this.schematic = schematics;
		if (schematics == null) {
			this.positions.clear();
			this.positionsSecond.clear();
			return;
		}
		Stack<Integer> positions = new Stack<Integer>();
		for (int y = 0; y < schematics.schema.getHeight(); ++y) {
			for (int z = 0; z < schematics.schema.getLength() / 2; ++z) {
				for (int x = 0; x < schematics.schema.getWidth() / 2; ++x) {
					positions.add(0, this.xyzToIndex(x, y, z));
				}
			}
			for (int z = 0; z < schematics.schema.getLength() / 2; ++z) {
				for (int x = schematics.schema.getWidth() / 2; x < schematics.schema.getWidth(); ++x) {
					positions.add(0, this.xyzToIndex(x, y, z));
				}
			}
			for (int z = schematics.schema.getLength() / 2; z < schematics.schema.getLength(); ++z) {
				for (int x = 0; x < schematics.schema.getWidth() / 2; ++x) {
					positions.add(0, this.xyzToIndex(x, y, z));
				}
			}
			for (int z = schematics.schema.getLength() / 2; z < schematics.schema.getLength(); ++z) {
				for (int x = schematics.schema.getWidth() / 2; x < schematics.schema.getWidth(); ++x) {
					positions.add(0, this.xyzToIndex(x, y, z));
				}
			}
		}
		this.positions = positions;
		this.positionsSecond.clear();
	}

	public void update() {
		if (this.world.isRemote || !this.hasSchematic() || this.finished) {
			return;
		}
		--this.ticks;
		if (this.ticks > 0) {
			return;
		}
		this.ticks = 200;
		if (this.positions.isEmpty() && this.positionsSecond.isEmpty()) {
			this.finished = true;
			return;
		}
		if (!this.started) {
			for (EntityPlayer player : this.getPlayerList()) {
				if (this.availability.isAvailable(player)) {
					this.started = true;
					break;
				}
			}
			if (!this.started) {
				return;
			}
		}
		List<EntityNPCInterface> list = this.world.getEntitiesWithinAABB(EntityNPCInterface.class,
				new AxisAlignedBB(this.getPos(), this.getPos()).grow(32.0, 32.0, 32.0));
		for (EntityNPCInterface npc : list) {
			if (npc.advanced.jobInterface instanceof JobBuilder) {
				JobBuilder job = (JobBuilder) npc.advanced.jobInterface;
				if (job.build != null) {
					continue;
				}
				job.build = this;
			}
		}
	}

	public NBTTagCompound writePartNBT(NBTTagCompound compound) {
		compound.setInteger("Rotation", this.rotation);
		compound.setInteger("YOffset", this.yOffest);
		compound.setBoolean("Enabled", this.enabled);
		compound.setBoolean("Started", this.started);
		compound.setBoolean("Finished", this.finished);
		compound.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
		return compound;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (this.schematic != null) {
			compound.setString("SchematicName", this.schematic.schema.getName());
		}
		compound.setTag("Positions", NBTTags.nbtIntegerCollection(new ArrayList<Integer>(this.positions)));
		compound.setTag("PositionsSecond", NBTTags.nbtIntegerCollection(new ArrayList<Integer>(this.positionsSecond)));
		this.writePartNBT(compound);
		return compound;
	}

	public int xyzToIndex(int x, int y, int z) {
		return (y * this.schematic.schema.getLength() + z) * this.schematic.schema.getWidth() + x;
	}

}
