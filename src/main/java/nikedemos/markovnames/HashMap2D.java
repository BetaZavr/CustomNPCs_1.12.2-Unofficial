package nikedemos.markovnames;

import java.util.HashMap;
import java.util.Map;

public class HashMap2D<T1, T2, T3> {
	public Map<T1, Map<T2, T3>> mMap;

	public HashMap2D() {
		this.mMap = new HashMap<T1, Map<T2, T3>>();
	}

	public void clear() {
		this.mMap.clear();
	}

	public boolean containsKeys(T1 key1, T2 key2) {
		return this.mMap.containsKey(key1) && this.mMap.get(key1).containsKey(key2);
	}

	public T3 get(T1 key1, T2 key2) {
		if (this.mMap.containsKey(key1)) {
			return this.mMap.get(key1).get(key2);
		}
		return null;
	}

	public T3 put(T1 key1, T2 key2, T3 value) {
		Map<T2, T3> map;
		if (this.mMap.containsKey(key1)) {
			map = this.mMap.get(key1);
		} else {
			map = new HashMap<T2, T3>();
			this.mMap.put(key1, map);
		}
		return map.put(key2, value);
	}
}
