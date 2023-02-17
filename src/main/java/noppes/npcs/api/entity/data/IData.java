package noppes.npcs.api.entity.data;

public interface IData {
	void clear();

	Object get(String p0);

	String[] getKeys();

	boolean has(String p0);

	void put(String p0, Object p1);

	void remove(String p0);
}
