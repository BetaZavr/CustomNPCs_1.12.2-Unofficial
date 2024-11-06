package noppes.npcs.roles;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.role.IJobFollower;
import noppes.npcs.entity.EntityNPCInterface;

public class JobFollower extends JobInterface implements IJobFollower {

	public EntityNPCInterface following = null;
	public String name = "";

	public JobFollower(EntityNPCInterface npc) {
		super(npc);
		type = JobType.FOLLOWER;
	}

	@Override
	public boolean isWorking() {
		return isFollowing();
	}

	@Override
	public boolean aiShouldExecute() {
		if (npc.isAttacking()) {
			return false;
		}
		if (following != null) {
			if (following.isKilled()) {
				following = null;
				return false;
			}
			double dist = npc.getDistance(following);
			if (dist <= 1.5d) {
				if (!npc.getNavigator().noPath()) {
					npc.getNavigator().clearPath();
				}
				return true;
			} else if (dist <= getRange()) {
				boolean bo = npc.getNavigator().tryMoveToEntityLiving(following, 1.0d);
				if (!bo) {
					following = null;
				}
			} else {
				following = null;
			}
			if (following != null) {
				return true;
			}
		}
        List<EntityNPCInterface> list = npc.world.getEntitiesWithinAABB(EntityNPCInterface.class,
				npc.getEntityBoundingBox().grow(getRange(), getRange(), getRange()));
		for (EntityNPCInterface entity : list) {
			if (entity == npc || entity.isKilled()) {
				continue;
			}
			if (entity.display.getName().equalsIgnoreCase(name)) {
				following = entity;
				break;
			}
		}
		return false;
	}

	@Override
	public void aiUpdateTask() {
		npc.getLookHelper().setLookPosition(following.posX,
				following.posY + following.getEyeHeight(), following.posZ, 10.0f,
				npc.getVerticalFaceSpeed());
	}

	@Override
	public String getFollowing() {
		return name;
	}

	@Override
	public ICustomNpc<?> getFollowingNpc() {
		if (following == null) {
			return null;
		}
		return following.wrappedNPC;
	}

	private int getRange() {
		if (npc.stats.aggroRange > CustomNpcs.NpcNavRange) {
			return CustomNpcs.NpcNavRange;
		}
		return npc.stats.aggroRange;
	}

	public boolean hasOwner() {
		return !name.isEmpty() && isFollowing();
	}

	@Override
	public boolean isFollowing() {
		return following != null;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		type = JobType.FOLLOWER;
		name = compound.getString("FollowingEntityName");
	}

    @Override
	public void resetTask() {
		following = null;
	}

	@Override
	public void setFollowing(String n) {
		name = n;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.FOLLOWER.get());
		compound.setString("FollowingEntityName", name);
		return compound;
	}
}
