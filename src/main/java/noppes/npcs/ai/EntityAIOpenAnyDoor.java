package noppes.npcs.ai;

import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockDoor;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityAIOpenAnyDoor extends EntityAIBase {
	private int closeDoorTemporisation;
	private Block door;
	private float entityX;
	private float entityZ;
	private boolean hasStoppedDoorInteraction;
	private EntityNPCInterface npc;
	private BlockPos position;
	private IProperty<?> property;

	public EntityAIOpenAnyDoor(EntityNPCInterface npc) {
		this.npc = npc;
	}

	public Block getDoor(BlockPos pos) {
		IBlockState state = this.npc.world.getBlockState(pos);
		Block block = state.getBlock();
		if (state.isFullBlock() || block == Blocks.IRON_DOOR) {
			return null;
		}
		if (block instanceof BlockDoor) {
			return block;
		}
		Set<IProperty<?>> set = (Set<IProperty<?>>) state.getProperties().keySet();
		for (IProperty<?> prop : set) {
			if (prop instanceof PropertyBool && prop.getName().equals("open")) {
				this.property = prop;
				return block;
			}
		}
		return null;
	}

	public void resetTask() {
		this.setDoorState(this.door, this.position, false);
	}

	public void setDoorState(Block block, BlockPos position, boolean open) {
		if (block instanceof BlockDoor) {
			((BlockDoor) block).toggleDoor(this.npc.world, position, open);
		} else {
			IBlockState state = this.npc.world.getBlockState(position);
			if (state.getBlock() != block) {
				return;
			}
			this.npc.world.setBlockState(position, state.withProperty((PropertyBool) this.property, open));
			this.npc.world.playEvent((EntityPlayer) null, open ? 1003 : 1006, position, 0);
		}
	}

	public boolean shouldContinueExecuting() {
		return this.closeDoorTemporisation > 0 && !this.hasStoppedDoorInteraction;
	}

	public boolean shouldExecute() {
		if (!this.npc.collidedHorizontally) {
			return false;
		}
		Path pathentity = this.npc.getNavigator().getPath();
		if (pathentity != null && !pathentity.isFinished()) {
			for (int i = 0; i < Math.min(pathentity.getCurrentPathIndex() + 2,
					pathentity.getCurrentPathLength()); ++i) {
				PathPoint pathpoint = pathentity.getPathPointFromIndex(i);
				this.position = new BlockPos(pathpoint.x, pathpoint.y + 1, pathpoint.z);
				if (this.npc.getDistanceSq(this.position.getX(), this.npc.posY, this.position.getZ()) <= 2.25) {
					this.door = this.getDoor(this.position);
					if (this.door != null) {
						return true;
					}
				}
			}
			this.position = new BlockPos(this.npc).up();
			this.door = this.getDoor(this.position);
			return this.door != null;
		}
		return false;
	}

	public void startExecuting() {
		this.hasStoppedDoorInteraction = false;
		this.entityX = (float) (this.position.getX() + 0.5f - this.npc.posX);
		this.entityZ = (float) (this.position.getZ() + 0.5f - this.npc.posZ);
		this.closeDoorTemporisation = 20;
		this.setDoorState(this.door, this.position, true);
	}

	public void updateTask() {
		--this.closeDoorTemporisation;
		float f = (float) (this.position.getX() + 0.5f - this.npc.posX);
		float f2 = (float) (this.position.getZ() + 0.5f - this.npc.posZ);
		float f3 = this.entityX * f + this.entityZ * f2;
		if (f3 < 0.0f) {
			this.hasStoppedDoorInteraction = true;
		}
	}
}
