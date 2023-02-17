package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.INPCJob;

public interface IJobFollower extends INPCJob {
	String getFollowing();

	ICustomNpc<?> getFollowingNpc();

	boolean isFollowing();

	void setFollowing(String p0);
}
