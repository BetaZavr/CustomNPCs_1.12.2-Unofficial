package noppes.npcs.roles;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.role.IJobFollower;
import noppes.npcs.entity.EntityNPCInterface;

public class JobFollower extends JobInterface implements IJobFollower {

	public EntityNPCInterface following;
	public String name;

	public JobFollower(EntityNPCInterface npc) {
		super(npc);
		this.following = null;
		this.name = "";
		this.type = JobType.FOLLOWER;
	}

	@Override
	public boolean aiShouldExecute() {
		if (this.npc.isAttacking()) {
			return false;
		}
		if (this.following != null) {
			if (this.following.isKilled()) {
				this.following = null;
				return false;
			}
			double dist = this.npc.getDistance(this.following);
			if (dist <= 1.5d) {
				if (!this.npc.getNavigator().noPath()) {
					this.npc.getNavigator().clearPath();
				}
				return true;
			} else if (dist <= this.getRange()) {
				boolean bo = this.npc.getNavigator().tryMoveToEntityLiving(this.following, 1.0d);
				if (!bo) {
					this.following = null;
				}
			} else {
				this.following = null;
			}
			if (this.following != null) {
				return true;
			}
		}
		this.following = null;
		List<EntityNPCInterface> list = this.npc.world.getEntitiesWithinAABB(EntityNPCInterface.class,
				this.npc.getEntityBoundingBox().grow(this.getRange(), this.getRange(), this.getRange()));
		for (EntityNPCInterface entity : list) {
			if (entity == this.npc || entity.isKilled()) {
				continue;
			}
			if (entity.display.getName().equalsIgnoreCase(this.name)) {
				this.following = entity;
				break;
			}
		}
		return false;
	}

	@Override
	public void aiUpdateTask() {
		this.npc.getLookHelper().setLookPosition(this.following.posX,
				this.following.posY + this.following.getEyeHeight(), this.following.posZ, 10.0f,
				this.npc.getVerticalFaceSpeed());
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
		if (this.npc.stats.aggroRange > CustomNpcs.NpcNavRange) {
			return CustomNpcs.NpcNavRange;
		}
		return this.npc.stats.aggroRange;
	}

	public boolean hasOwner() {
		return !this.name.isEmpty() && isFollowing();
	}

	@Override
	public boolean isFollowing() {
		return this.following != null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.FOLLOWER;
		this.name = compound.getString("FollowingEntityName");
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
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.FOLLOWER.get());
		compound.setString("FollowingEntityName", this.name);
		return compound;
	}
}
