package noppes.npcs.roles;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.role.IJobFollower;
import noppes.npcs.entity.EntityNPCInterface;

public class JobFollower
extends JobInterface
implements IJobFollower {
	
	public EntityNPCInterface following;
	public String name;
	private int range;
	private int ticks;

	public JobFollower(EntityNPCInterface npc) {
		super(npc);
		this.following = null;
		this.ticks = 40;
		this.range = 20;
		this.name = "";
		this.type = JobType.FOLLOWER;
	}

	@Override
	public boolean aiShouldExecute() {
		if (this.npc.isAttacking()) {
			return false;
		}
		--this.ticks;
		if (this.ticks > 0) {
			return false;
		}
		this.ticks = 10;
		this.following = null;
		List<EntityNPCInterface> list = this.npc.world.getEntitiesWithinAABB(EntityNPCInterface.class,
				this.npc.getEntityBoundingBox().grow(this.getRange(), this.getRange(), this.getRange()));
		for (EntityNPCInterface entity : list) {
			if (entity != this.npc) {
				if (entity.isKilled()) {
					continue;
				}
				if (entity.display.getName().equalsIgnoreCase(this.name)) {
					this.following = entity;
					break;
				}
				continue;
			}
		}
		return false;
	}

	@Override
	public String getFollowing() {
		return this.name;
	}

	@Override
	public ICustomNpc<?> getFollowingNpc() {
		if (this.following == null) {
			return null;
		}
		return this.following.wrappedNPC;
	}

	private int getRange() {
		if (this.range > CustomNpcs.NpcNavRange) {
			return CustomNpcs.NpcNavRange;
		}
		return this.range;
	}

	public boolean hasOwner() {
		return !this.name.isEmpty();
	}

	@Override
	public boolean isFollowing() {
		return this.following != null;
	}

	@Override
	public void reset() {
	}

	@Override
	public void resetTask() {
		this.following = null;
	}

	@Override
	public void setFollowing(String name) {
		this.name = name;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.FOLLOWER;
		this.name = compound.getString("FollowingEntityName");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.FOLLOWER.get());
		compound.setString("FollowingEntityName", this.name);
		return compound;
	}
}
