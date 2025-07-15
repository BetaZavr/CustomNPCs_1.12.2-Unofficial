package noppes.npcs.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.entity.EntityNPCInterface;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;

public class DataDebug {

	private static final Logger LOGGER = LogManager.getLogger(DataDebug.class);

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
		if (!CustomNpcs.VerboseDebug) { return; }
		try {
			Side side = methodName.equals("findChunksForSpawning") || methodName.equals("performWorldGenSpawning") ?
					Side.SERVER :
					Util.instance.getSide();
			if (!data.containsKey(side)) { return; }
			data.get(side).end(getKey(obj), (methodName.isEmpty() ? "" : methodName + "()") + "_" + getKey(target));
		}
		catch (Exception e) { LOGGER.error("Error end debug method:", e); }
	}

	public void start(Object target, Object obj, String methodName) {
		if (!CustomNpcs.VerboseDebug) { return; }
		try {
			Side side = methodName.equals("findChunksForSpawning") || methodName.equals("performWorldGenSpawning") ?
					Side.SERVER :
					Util.instance.getSide();
			if (!data.containsKey(side)) { data.put(side, new Debug()); }
			data.get(side).start(getKey(obj), (methodName.isEmpty() ? "" : methodName + "()") + "_" + getKey(target));
		}
		catch (Exception e) { LOGGER.error("Error start debug method:", e); }
	}

	public void stop() {
		if (!CustomNpcs.VerboseDebug) { return; }
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
		if (!CustomNpcs.VerboseDebug || data.isEmpty()) { return new ArrayList<>(); }
		StringBuilder tempInfo;
		String temp;
		StringBuilder fullInfo = new StringBuilder("Output full debug info ").append(CustomNpcs.MODNAME).append(":");
		StringBuilder maxInfo = new StringBuilder("Output maximums from debug info ").append(CustomNpcs.MODNAME).append(":");
		fullInfo.append("\nShowing Monitoring results for ANY side. { Side: [Target.Method names; Runs; Average time] }:");
		stop();
		boolean start = false;
		List<String> list = new ArrayList<>();
		for (Side side : data.keySet()) {
			if (start) { fullInfo.append("\n----   ----   ----"); }
			fullInfo.append("\n").append("Side: ").append(side.name());
			List<String> events = new ArrayList<>(data.get(side).times.keySet());
			Collections.sort(events);
			int i = 0;
			long max = Long.MIN_VALUE;
			Object[][] maxInSide = new Object[2][4];
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
						maxInSide[0][0] = eventName;
						maxInSide[0][1] = target;
						maxInSide[0][2] = time[0];
						maxInSide[0][3] = time[1];
					}
					if (max < time[0]) {
						max = time[0];
						maxInSide[1][0] = eventName;
						maxInSide[1][1] = target;
						maxInSide[1][2] = time[0];
						maxInSide[1][3] = time[1];
					}
					s++;
				}
				fullInfo.append("\n [").append(i + 1).append("/").append(events.size()).append("] - \"").append(eventName).append("\": ").append(log);
				i++;
			}
			// long time
			if (maxInSide[0][0] != null) {
				tempInfo = new StringBuilder(TextFormatting.GRAY.toString())
						.append(" \"")
						.append(TextFormatting.RESET)
						.append(side.name())
						.append(TextFormatting.GRAY)
						.append("\" a long time [")
						.append(TextFormatting.BLUE)
						.append(maxInSide[0][0])
						.append(TextFormatting.GRAY)
						.append(".")
						.append(TextFormatting.DARK_GREEN)
						.append(maxInSide[0][1])
						.append(TextFormatting.GRAY)
						.append("; runs: ")
						.append(TextFormatting.GOLD)
						.append(maxInSide[0][2])
						.append(TextFormatting.GRAY)
						.append("; time = ")
						.append(TextFormatting.RESET)
						.append(Util.instance.ticksToElapsedTime((long) maxInSide[0][3], true, true, false))
						.append(TextFormatting.GRAY)
						.append("]");
				temp = "\n" + Util.instance.deleteColor(tempInfo.toString());
				maxInfo.append(temp);
				list.add(tempInfo.toString());
			}
			// max count
			if (maxInSide[1][0] != null) {
				tempInfo = new StringBuilder(TextFormatting.DARK_GRAY.toString())
						.append(" \"")
						.append(TextFormatting.RESET)
						.append(side.name())
						.append(TextFormatting.GRAY)
						.append("\" most often [")
						.append(TextFormatting.BLUE)
						.append(maxInSide[1][0])
						.append(TextFormatting.GRAY)
						.append(".")
						.append(TextFormatting.DARK_GREEN)
						.append(maxInSide[1][1])
						.append(TextFormatting.GRAY)
						.append("; runs: ")
						.append(TextFormatting.GOLD)
						.append(maxInSide[1][2])
						.append(TextFormatting.GRAY)
						.append("; time = ")
						.append(TextFormatting.RESET)
						.append(Util.instance.ticksToElapsedTime((long) maxInSide[1][3], true, true, false))
						.append(TextFormatting.GRAY)
						.append("]");
				temp = "\n" + Util.instance.deleteColor(tempInfo.toString());
				maxInfo.append(temp);
				list.add(tempInfo.toString());
			}
			start = true;
		}
		// Caches
		list.add("Caches:");
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString()).append("BlockWrapper.blockCache: ").append(TextFormatting.GOLD).append(BlockWrapper.blockCache.size());
		list.add(tempInfo.toString());
		fullInfo.append("\n").append(Util.instance.deleteColor(tempInfo.toString()));
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString()).append("WrapperNpcAPI.worldCache: ").append(TextFormatting.GOLD).append(WrapperNpcAPI.worldCache.size());
		list.add(tempInfo.toString());
		fullInfo.append("\n").append(Util.instance.deleteColor(tempInfo.toString()));
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString()).append("CommonProxy.downloadableFiles: ").append(TextFormatting.GOLD).append(CommonProxy.downloadableFiles.size());
		list.add(tempInfo.toString());
		fullInfo.append("\n").append(Util.instance.deleteColor(tempInfo.toString()));
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString()).append("CommonProxy.availabilityStacks: ").append(TextFormatting.GOLD).append(CommonProxy.availabilityStacks.size());
		list.add(tempInfo.toString());
		fullInfo.append("\n").append(Util.instance.deleteColor(tempInfo.toString()));
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString()).append("NoppesUtilPlayer.delaySendMap: ").append(TextFormatting.GOLD).append(NoppesUtilPlayer.delaySendMap.size());
		list.add(tempInfo.toString());
		fullInfo.append("\n").append(Util.instance.deleteColor(tempInfo.toString()));

		if (logger != null) {
			logger.info(fullInfo);
			logger.info(maxInfo);
		} else {
			LOGGER.info(fullInfo);
			LOGGER.info(maxInfo);
		}
		return list;
	}

}
