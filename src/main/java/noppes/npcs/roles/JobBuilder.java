package noppes.npcs.roles;

import java.util.Objects;
import java.util.Stack;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBuilder;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.tiles.TileBuilder;
import noppes.npcs.controllers.data.BlockData;
import noppes.npcs.entity.EntityNPCInterface;

public class JobBuilder extends JobInterface implements IJobBuilder {

	public TileBuilder build;
	private BlockData placing;
	private Stack<BlockData> placingList;
	private BlockPos possibleBuildPos;
	private int ticks;
	private int tryTicks;

	public JobBuilder(EntityNPCInterface npc) {
		super(npc);
		this.build = null;
		this.possibleBuildPos = null;
		this.placingList = null;
		this.placing = null;
		this.tryTicks = 0;
		this.ticks = 0;
		this.overrideMainHand = true;
		this.type = JobType.BUILDER;
	}

	@Override
	public boolean aiShouldExecute() {
		if (this.possibleBuildPos != null) {
			TileEntity tile = this.npc.world.getTileEntity(this.possibleBuildPos);
			if (tile instanceof TileBuilder) {
				this.build = (TileBuilder) tile;
			} else {
				this.placingList.clear();
			}
			this.possibleBuildPos = null;
		}
		return this.build != null;
	}

	@Override
	public boolean isWorking() {
		return build == null || (build.finished && placingList == null) || !build.enabled || build.isInvalid();
	}

	@Override
	public void aiUpdateTask() {
		if ((build.finished && placingList == null) || !build.enabled || build.isInvalid()) {
			build = null;
			npc.getNavigator().tryMoveToXYZ(npc.getStartXPos(), npc.getStartYPos(), npc.getStartZPos(), 1.0);
			return;
		}
		if (this.ticks++ < 10) {
			return;
		}
		this.ticks = 0;
		if ((this.placingList == null || this.placingList.isEmpty()) && this.placing == null) {
			this.placingList = this.build.getBlock();
			this.npc.setJobData("");
			return;
		}
		if (this.placing == null) {
			this.placing = this.placingList.pop();
			if (this.placing.state.getBlock() == Blocks.STRUCTURE_VOID) {
				this.placing = null;
				return;
			}
			this.tryTicks = 0;
			this.npc.setJobData(this.blockToString(this.placing));
		}
		npc.getNavigator().tryMoveToXYZ(placing.pos.getX(), (placing.pos.getY() + 1), placing.pos.getZ(), 1.0);
		if (tryTicks++ > 40 || npc.nearPosition(placing.pos)) {
			BlockPos blockPos = placing.pos;
			placeBlock();
			if (tryTicks > 40) {
				blockPos = NoppesUtilServer.GetClosePos(blockPos, this.npc.world);
				npc.setPositionAndUpdate(blockPos.getX() + 0.5, blockPos.getY(), blockPos.getZ() + 0.5);
			}
		}
	}

	private String blockToString(BlockData data) {
		if (data.state.getBlock() == Blocks.AIR) {
			return Objects.requireNonNull(Items.IRON_PICKAXE.getRegistryName()).toString();
		}
		return this.itemToString(data.getStack());
	}

	@Override
	public IItemStack getMainhand() {
		String name = this.npc.getJobData();
		ItemStack item = this.stringToItem(name);
		if (item.isEmpty()) {
			return this.npc.inventory.weapons.get(0);
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item);
	}

	@Override
	public boolean isBuilding() {
		return this.build != null && this.build.enabled && !this.build.finished && this.build.started;
	}

	public void placeBlock() {
		if (this.placing == null) {
			return;
		}
		this.npc.getNavigator().clearPath();
		this.npc.swingArm(EnumHand.MAIN_HAND);
		this.npc.world.setBlockState(this.placing.pos, this.placing.state, 2);
		if (this.placing.state.getBlock() instanceof ITileEntityProvider && this.placing.tile != null) {
			TileEntity tile = this.npc.world.getTileEntity(this.placing.pos);
			if (tile != null) {
				try {
					tile.readFromNBT(this.placing.tile);
				} catch (Exception e) { LogWriter.error(e); }
			}
		}
		this.placing = null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.BUILDER;
		if (compound.hasKey("BuildX")) {
			this.possibleBuildPos = new BlockPos(compound.getInteger("BuildX"), compound.getInteger("BuildY"),
					compound.getInteger("BuildZ"));
		}
		if (this.possibleBuildPos != null && compound.hasKey("Placing")) {
			Stack<BlockData> placing = new Stack<>();
			NBTTagList list = compound.getTagList("Placing", 10);
			for (int i = 0; i < list.tagCount(); ++i) {
				BlockData data = BlockData.getData(list.getCompoundTagAt(i));
				if (data != null) {
					placing.add(data);
				}
			}
			this.placingList = placing;
		}
		this.npc.ais.doorInteract = 1;
	}

	@Override
	public void reset() {
		this.build = null;
		this.npc.setJobData("");
	}

	@Override
	public void resetTask() {
		this.reset();
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.BUILDER.get());
		if (this.build != null) {
			compound.setInteger("BuildX", this.build.getPos().getX());
			compound.setInteger("BuildY", this.build.getPos().getY());
			compound.setInteger("BuildZ", this.build.getPos().getZ());
			if (this.placingList != null && !this.placingList.isEmpty()) {
				NBTTagList list = new NBTTagList();
				for (BlockData data : this.placingList) {
					list.appendTag(data.getNBT());
				}
				if (this.placing != null) {
					list.appendTag(this.placing.getNBT());
				}
				compound.setTag("Placing", list);
			}
		}
		return compound;
	}
}
