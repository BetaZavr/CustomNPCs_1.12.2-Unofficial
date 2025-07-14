package noppes.npcs.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.entity.EntityNPCInterface;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DataDebug {

	public static class Debug {

		public long max = 0L;
		Map<String, Long> starters = new HashMap<>(); // temp time [Name, start time]
		public Map<String, Map<String, Long[]>> times = new HashMap<>(); // Name, [count, all time in work]

		public void end(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) { return; }
			String key = eventName + ":" + eventTarget;
			if (!starters.containsKey(key) || starters.get(key) <= 0L) { return; }
			if (!times.containsKey(eventName)) { times.put(eventName, new HashMap<>()); }
			if (!times.get(eventName).containsKey(eventTarget)) { times.get(eventName).put(eventTarget, new Long[] { 0L, 0L }); }
			Long[] arr = times.get(eventName).get(eventTarget);
			arr[0]++;
			long r = System.currentTimeMillis() - starters.get(key);
			if (arr[1] < r) { arr[1] = r; }
			if (max < r) { max = r; }
			times.get(eventName).put(eventTarget, arr);
			starters.put(key, 0L);
		}

		public void start(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) { return; }
			String key = eventName + ":" + eventTarget;
			if (!starters.containsKey(key)) { starters.put(key, 0L); }
			if (starters.get(key) > 0L) { return; }
			starters.put(key, System.currentTimeMillis());
		}

	}

	private static String getKey(Object target) {
		if (target == null) { return "Mod"; }
		if (target instanceof String) { return (String) target; }
		if (target instanceof EntityPlayer) { return "Players"; }
		if (target instanceof EntityNPCInterface) { return "NPC"; }
		if (target instanceof Entity) { return "MOBs"; }
		if (target instanceof Class<?>) { return ((Class<?>) target).getSimpleName(); }
		return target.getClass().getSimpleName();
	}

	private final Map<Side, Debug> data = new HashMap<>();

	public void end(Object target, Object obj, String methodName) {
		if (!CustomNpcs.FreezesDebug) { return; }
		try {
			Side side = methodName.equals("findChunksForSpawning") || methodName.equals("performWorldGenSpawning") ?
					Side.SERVER :
					Util.instance.getSide();
			if (!data.containsKey(side)) { return; }
			data.get(side).end(getKey(obj), (methodName.isEmpty() ? "" : methodName + "_") + getKey(target));
		}
		catch (Exception e) { LogWriter.error("Error end debug method:", e); }
	}

	public void start(Object target, Object obj, String methodName) {
		if (!CustomNpcs.FreezesDebug) { return; }
		try {
			Side side = methodName.equals("findChunksForSpawning") || methodName.equals("performWorldGenSpawning") ?
					Side.SERVER :
					Util.instance.getSide();
			if (!data.containsKey(side)) { data.put(side, new Debug()); }
			data.get(side).start(getKey(obj), (methodName.isEmpty() ? "" : methodName + "_") + getKey(target));
		}
		catch (Exception e) { LogWriter.error("Error start debug method:", e); }
	}

	public void stop() {
		if (!CustomNpcs.FreezesDebug) { return; }
		for (Side side : data.keySet()) {
			for (String k : data.get(side).starters.keySet()) {
				data.get(side).end(k.substring(0, k.indexOf(':')), k.substring(k.indexOf(':') + 1));
			}
		}
	}

	public void clear() {
		stop();
		data.clear();
	}

	public List<String> logging(Logger logger) {
		if (!CustomNpcs.FreezesDebug || data.isEmpty()) { return new ArrayList<>(); }
		String temp = CustomNpcs.MODNAME + " debug information output:";
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
		stop();
		boolean start = false;
		List<String> list = new ArrayList<>();
		for (Side side : data.keySet()) {
			if (start) {
				temp = "----   ----   ----";
				if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
			}
			temp = "Showing Monitoring results for \"" + side.name() + "\" side. |Number - EventName: { [Target name, Runs, Average time] }|:";
			if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
			List<String> events = new ArrayList<>(data.get(side).times.keySet());
			Collections.sort(events);
			int i = 0;
			long max = Long.MIN_VALUE;
			String[] maxName = new String[] { "", "", "", "" };
			for (String eventName : events) {
				DataDebug.Debug dd = data.get(side);
				List<String> targets = new ArrayList<>(data.get(side).times.get(eventName).keySet());
				Collections.sort(targets);
				StringBuilder log = new StringBuilder();
				if (targets.size() > 1) { log.append("\n"); }
				int s = 0;
				for (String target : targets) {
					Long[] time = data.get(side).times.get(eventName).get(target);
					if (time[0] <= 0) { time[0] = 1L; }
					log.append("  [")
							.append(target)
							.append(", ")
							.append(time[0])
							.append(", ")
							.append(Util.instance.ticksToElapsedTime(time[1], true, false, false))
							.append("]");
					if (s < targets.size() - 1) { log.append(";\n"); }
					if (time[1] == dd.max) {
						maxName[0] = "\"" + eventName + "|" + target + "\": " + time[0] + " runs; time \"" + Util.instance.ticksToElapsedTime(time[1], true, false, false)+"\"";
					}
					if (max < time[0]) {
						max = time[0];
						maxName[1] = "\"" + eventName + "|" + target + "\": " + time[0] + " runs; time \"" + Util.instance.ticksToElapsedTime(time[1], true, false, false)+"\"";
					}
					s++;
				}
				temp = " ["+ (i + 1) + "/" + events.size() + "] - \"" + eventName + "\": " + log;
				if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
				i++;
			}
			temp = " \"" + side.name() + "\" a long time [" + maxName[0] + "]";
			list.add(temp);
			if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
			temp = " \"" + side.name() + "\" most often: [" + maxName[1] + "]";
			list.add(temp);
			if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
			start = true;
		}
		temp = "BlockWrapper.blockCache: " + BlockWrapper.blockCache.size();
		list.add(temp);
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
		temp = "WrapperNpcAPI.worldCache: " + WrapperNpcAPI.worldCache.size();
		list.add(temp);
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
		temp = "CommonProxy.downloadableFiles: " + CommonProxy.downloadableFiles.size();
		list.add(temp);
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
		temp = "CommonProxy.availabilityStacks: " + CommonProxy.availabilityStacks.size();
		list.add(temp);
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}
		temp = "NoppesUtilPlayer.delaySendMap: " + NoppesUtilPlayer.delaySendMap.size();
		list.add(temp);
		if (logger != null) { logger.info(temp); } else { LogWriter.info(temp);}

		return list;
	}

}
