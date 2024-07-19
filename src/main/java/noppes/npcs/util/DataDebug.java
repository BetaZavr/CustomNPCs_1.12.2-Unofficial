package noppes.npcs.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.entity.EntityNPCInterface;

public class DataDebug {

	public static class Debug {

		public long max = 0L;
		Map<String, Long> starters = Maps.newHashMap(); // temp time [Name, start time]
		public Map<String, Map<String, Long[]>> times = Maps.newHashMap(); // Name, [count, all time in work]

		public void end(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) {
				return;
			}
			String key = eventName + ":" + eventTarget;
			if (!this.starters.containsKey(key) || this.starters.get(key) <= 0L) {
				return;
			}
			if (!this.times.containsKey(eventName)) {
				this.times.put(eventName, Maps.newHashMap());
			}
			if (!this.times.get(eventName).containsKey(eventTarget)) {
				this.times.get(eventName).put(eventTarget, new Long[] { 0L, 0L });
			}
			Long[] arr = this.times.get(eventName).get(eventTarget);
			arr[0]++;
			long r = System.currentTimeMillis() - this.starters.get(key);
			arr[1] += r;
			if (this.max < arr[1]) {
				this.max = arr[1];
			}
			this.times.get(eventName).put(eventTarget, arr);
			this.starters.put(key, 0L);
		}

		public void start(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) {
				return;
			}
			String key = eventName + ":" + eventTarget;
			if (!this.starters.containsKey(key)) {
				this.starters.put(key, 0L);
			}
			if (this.starters.get(key) > 0L) {
				return;
			}
			this.starters.put(key, System.currentTimeMillis());
		}
	}

	public Map<String, Debug> data = Maps.newHashMap();

	public void clear() {
		this.data.clear();
	}

	public void endDebug(String side, Object target, String classMethod) {
		if (!CustomNpcs.VerboseDebug) {
			return;
		}
		try {
			if (!this.data.containsKey(side)) {
				return;
			}
			String t = "MOBs";
			if (target == null) {
				t = "Mod";
			} else if (target instanceof String) {
				t = target.toString();
			} else if (target instanceof EntityPlayer) {
				t = "Players";
			} else if (target instanceof EntityNPCInterface) {
				t = "NPC";
			}
			this.data.get(side).end(classMethod, t);
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void startDebug(String side, Object target, String classMethod) {
		if (!CustomNpcs.VerboseDebug) {
			return;
		}
		try {
			if (!this.data.containsKey(side)) {
				this.data.put(side, new Debug());
			}
			String t = "MOBs";
			if (target == null) {
				t = "Mod";
			} else if (target instanceof String) {
				t = target.toString();
			} else if (target instanceof EntityPlayer) {
				t = "Players";
			} else if (target instanceof EntityNPCInterface) {
				t = "NPC";
			}
			this.data.get(side).start(classMethod, t);
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}

	public void stopAll() {
		if (!CustomNpcs.VerboseDebug) {
			return;
		}
		for (String side : this.data.keySet()) {
			for (String k : this.data.get(side).starters.keySet()) {
				this.data.get(side).end(k.substring(0, k.indexOf(':')), k.substring(k.indexOf(':') + 1));
			}
		}
	}

}
