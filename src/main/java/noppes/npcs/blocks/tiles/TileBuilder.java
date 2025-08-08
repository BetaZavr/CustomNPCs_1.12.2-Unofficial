package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class TileBuilder extends TileEntity implements ITickable {

	public static List<BlockPos> DrawPoses = new ArrayList<>();

	public static boolean has(BlockPos pos) {
		if (pos == null) {
			return false;
		}
		for (BlockPos p : TileBuilder.DrawPoses) {
			if (p != null && p.equals(pos)) {
				return true;
			}
		}
		return false;
	}
	public static void SetDrawPos(BlockPos pos) {
		if (!TileBuilder.has(pos)) {
			TileBuilder.DrawPoses.add(pos);
		}
	}
	public Availability availability;
	public boolean enabled;
	public boolean finished;
	private Stack<Integer> positions;
	private Stack<Integer> positionsSecond;
	public int rotation;
	private SchematicWrapper schematic;
	public boolean started;

	private int ticks;

	public int yOffset;

	public TileBuilder() {
		this.schematic = null;
		this.rotation = 0;
		this.yOffset = 0;
		this.enabled = false;
		this.started = false;
		this.finished = false;
		this.availability = new Availability();
		this.positions = new Stack<>();
		this.positionsSecond = new Stack<>();
		this.ticks = 20;
	}

	public Stack<BlockData> getBlock() {
		if (!this.enabled || this.finished || !this.hasSchematic()) {
			return null;
		}
		boolean bo = this.positions.isEmpty();
		Stack<BlockData> list = new Stack<>();
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
					BlockPos blockPos = this.getPos().add(1, this.yOffset, 1).add(this.schematic.rotatePos(x, y, z, this.rotation));
					IBlockState original = this.world.getBlockState(blockPos);
					if (Block.getStateId(state) != Block.getStateId(original)) {
						state = SchematicWrapper.rotationState(state, this.rotation);
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

	public SchematicWrapper getSchematic() {
		return this.schematic;
	}

	public boolean hasSchematic() {
		return this.schematic != null;
	}

	public void readPartNBT(NBTTagCompound compound) {
		this.rotation = compound.getInteger("Rotation");
		this.yOffset = compound.getInteger("YOffset");
		this.enabled = compound.getBoolean("Enabled");
		this.started = compound.getBoolean("Started");
		this.finished = compound.getBoolean("Finished");
		this.availability.load(compound.getCompoundTag("Availability"));
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
		Stack<Integer> positions = new Stack<>();
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
			for (EntityPlayer player : Util.instance.getEntitiesWithinDist(EntityPlayer.class, world, pos, 10.0d)) {
				if (this.availability.isAvailable(player)) {
					this.started = true;
					break;
				}
			}
			if (!this.started) {
				return;
			}
		}
		for (EntityNPCInterface npc : Util.instance.getEntitiesWithinDist(EntityNPCInterface.class, world, getPos(), 32.0d)) {
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
		compound.setInteger("YOffset", this.yOffset);
		compound.setBoolean("Enabled", this.enabled);
		compound.setBoolean("Started", this.started);
		compound.setBoolean("Finished", this.finished);
		compound.setTag("Availability", this.availability.save(new NBTTagCompound()));
		return compound;
	}

	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		if (compound.hasKey("SchematicName")) {
			this.schematic = SchematicController.Instance.load(compound.getString("SchematicName"));
		}
		Stack<Integer> positions = new Stack<>();
		positions.addAll(NBTTags.getIntegerList(compound.getTagList("Positions", 10)));
		this.positions = positions;
		positions = new Stack<>();
		positions.addAll(NBTTags.getIntegerList(compound.getTagList("PositionsSecond", 10)));
		this.positionsSecond = positions;
		this.readPartNBT(compound);
	}

	public @Nonnull NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		super.writeToNBT(compound);
		if (this.schematic != null) {
			compound.setString("SchematicName", this.schematic.schema.getName());
		}
		compound.setTag("Positions", NBTTags.nbtIntegerCollection(new ArrayList<>(this.positions)));
		compound.setTag("PositionsSecond", NBTTags.nbtIntegerCollection(new ArrayList<>(this.positionsSecond)));
		this.writePartNBT(compound);
		return compound;
	}

	public int xyzToIndex(int x, int y, int z) {
		return (y * this.schematic.schema.getLength() + z) * this.schematic.schema.getWidth() + x;
	}

}
