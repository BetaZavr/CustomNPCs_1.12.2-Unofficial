package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.entity.data.INPCJob;

@SuppressWarnings("all")
public interface IJobFollower extends INPCJob {

	String getFollowing();

	ICustomNpc<?> getFollowingNpc();

	boolean isFollowing();

	void setFollowing(@ParamName("name") String name);

}
