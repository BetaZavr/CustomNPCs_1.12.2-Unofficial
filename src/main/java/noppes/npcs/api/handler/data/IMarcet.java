package noppes.npcs.api.handler.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IMarcet {

	IDeal[] getAllDeals();

	IDeal[] getDeals(@ParamName("section") int section);

	int getId();

	String getName();

	boolean isLimited();

	void setIsLimited(@ParamName("limited") boolean limited);

	void setName(@ParamName("name") String name);

	void updateNew();

}
