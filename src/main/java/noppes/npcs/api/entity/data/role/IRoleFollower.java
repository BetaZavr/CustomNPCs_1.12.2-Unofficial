package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.INPCRole;

public interface IRoleFollower extends INPCRole {

	void addDays(int days);

	int getDays();

	IPlayer<?> getFollowing();

	boolean getGuiDisabled();

	boolean getInfinite();

	boolean getRefuseSoulstone();

	boolean isFollowing();

	void reset();

	void setFollowing(IPlayer<?> player);

	void setGuiDisabled(boolean disabled);

	void setInfinite(boolean infinite);

	void setRefuseSoulstone(boolean refuse);

}
