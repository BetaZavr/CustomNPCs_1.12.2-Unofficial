package noppes.npcs.api.handler.data;

public interface IMarcet {
	
	int getId();
	
	int[] getDealIDs();
	
	IDeal[] getDeals();

	String getName();

	void setName(String name);

	void updateNew();

	boolean isLimited();

	void setIsLimited(boolean limited);
	
}
