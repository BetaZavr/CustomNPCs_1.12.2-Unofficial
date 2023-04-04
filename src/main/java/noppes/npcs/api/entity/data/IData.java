package noppes.npcs.api.entity.data;

public interface IData {
	
	void clear();

	Object get(String key);

	String[] getKeys();

	boolean has(String key);

	void put(String key, Object value);

	void remove(String key);
}
