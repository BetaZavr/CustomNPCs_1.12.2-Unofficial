package noppes.npcs.api.handler.data;

public interface IMarcet {
	
	int getId();
	
	IDeal[] getDeals(int section);
	
	IDeal[] getAllDeals();

	String getName();

	void setName(String name);

	void updateNew();

	boolean isLimited();

	void setIsLimited(boolean limited);
	
}
