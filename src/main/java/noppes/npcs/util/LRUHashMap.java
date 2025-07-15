package noppes.npcs.util;

import java.util.*;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 2L;
	private final int maxSize;

	public LRUHashMap(int size) {
		super(size, 0.75f, true);
		maxSize = size;
	}

	@Override
	protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
		return size() > maxSize;
	}

}
