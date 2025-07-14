package noppes.npcs.util;

import java.util.*;

public class LRUHashMap<K, V> extends LinkedHashMap<K, V> {

	private static final long serialVersionUID = 2L;
	private final int maxSize;

	public LRUHashMap(int size) {
		super(size, 0.75f, true);
		this.maxSize = size;
	}

	@Override
	public V put(K key, V value) {
		if (size() > maxSize) {
			try {
				for (K k : new HashSet<>(keySet())) {
					remove(k);
					if (size() < maxSize) { break; }
				}
			}
			catch (Exception e) { clear(); }
        }
		return super.put(key, value);
	}

}
