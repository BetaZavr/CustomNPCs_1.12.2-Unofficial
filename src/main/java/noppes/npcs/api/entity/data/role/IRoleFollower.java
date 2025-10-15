package noppes.npcs.api.entity.data.role;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.INPCRole;

@SuppressWarnings("all")
public interface IRoleFollower extends INPCRole {

	void addDays(@ParamName("days") int days);

	int getDays();

	IPlayer<?> getFollowing();

	boolean getGuiDisabled();

	boolean getInfinite();

	boolean getRefuseSoulstone();

	boolean isFollowing();

	void reset();

	void setFollowing(@ParamName("player") IPlayer<?> player);

	void setGuiDisabled(@ParamName("disabled") boolean disabled);

	void setInfinite(@ParamName("infinite") boolean infinite);

	void setRefuseSoulstone(@ParamName("refuse") boolean refuse);

}
