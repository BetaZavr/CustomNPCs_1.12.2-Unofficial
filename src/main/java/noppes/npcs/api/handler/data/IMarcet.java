package noppes.npcs.api.handler.data;

public interface IMarcet {

	IDeal[] getAllDeals();

	IDeal[] getDeals(int section);

	int getId();

	String getName();

	boolean isLimited();

	void setIsLimited(boolean limited);

	void setName(String name);

	void updateNew();

}
