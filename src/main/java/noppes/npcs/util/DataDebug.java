package noppes.npcs.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.util.*;

public class DataDebug {

	public static class Debug {

		private long max = 0L;
		private final Map<String, Long> starters = new HashMap<>(); // temp time [unique key, start time]
		private final Map<String, Map<String, Long[]>> times = new HashMap<>(); // Name, [count, all time in work]

		public void end(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) { return; }
			String key = Thread.currentThread().getId() + ":" + eventName + ":" + eventTarget;
			if (!starters.containsKey(key)) {
				//LogWriter.debug("Double ending debug \""+key+"\"");
				return;
			}
			if (!times.containsKey(eventName)) { times.put(eventName, new HashMap<>()); }
			if (!times.get(eventName).containsKey(eventTarget)) { times.get(eventName).put(eventTarget, new Long[] { 0L, 0L }); }
			Long[] arr = times.get(eventName).get(eventTarget);
			arr[0]++;
			long r = System.currentTimeMillis() - starters.get(key);
			if (arr[1] < r) { arr[1] = r; }
			if (max < r) { max = r; }
			times.get(eventName).put(eventTarget, arr);
			starters.remove(key);
		}

		public void start(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) { return; }
			String key = Thread.currentThread().getId() + ":" + eventName + ":" + eventTarget;
			//if (starters.containsKey(key)) { LogWriter.debug("Double starting debug \""+key+"\""); }
			starters.put(key, System.currentTimeMillis());
		}

	}

	public long started = 0L;
	public long startedTicks = 0L;
	private final Map<Side, Debug> data = new HashMap<>();

	private static @Nonnull String getKey(Object target) {
		if (target == null) { return ""; }
		if (target instanceof String) { return (String) target; }
		if (target instanceof EntityPlayer) { return "Players"; }
		if (target instanceof EntityNPCInterface) { return "NPC"; }
		if (target instanceof Entity) { return "MOBs"; }
		if (target instanceof Class<?>) { return ((Class<?>) target).getSimpleName(); }
		return target.getClass().getSimpleName();
	}

	public void end(Object target) {
		//if (!CustomNpcs.VerboseDebug) { return; }
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		String obj = caller.getClassName();
		String methodName = caller.getMethodName() + "()";
		if (methodName.startsWith("lambda$")) {
			methodName = methodName.substring(methodName.indexOf("$") + 1);
			if (methodName.contains("$")) {
				methodName = methodName.substring(0, methodName.indexOf("$"));
			}
		}
		int dotPos = obj.lastIndexOf(".") + 1;
		if (dotPos > 0) { obj = obj.substring(dotPos); }
		Side side = caller.getMethodName().equals("findChunksForSpawning") || caller.getMethodName().equals("performWorldGenSpawning") ?
				Side.SERVER :
				Util.instance.getSide();
		String trg = getKey(target);
		if (!trg.isEmpty()) { trg = "_" + trg; }
		data.get(side).end(getKey(obj), methodName + trg);
	}

	public void end(Object target, String addedToMethodName) {
		//if (!CustomNpcs.VerboseDebug) { return; }
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		String obj = caller.getClassName();
		String methodName = caller.getMethodName() + "(" + addedToMethodName + ")";
		if (methodName.startsWith("lambda$")) {
			methodName = methodName.substring(methodName.indexOf("$") + 1);
			if (methodName.contains("$")) {
				methodName = methodName.substring(0, methodName.indexOf("$"));
			}
		}
		int dotPos = obj.lastIndexOf(".") + 1;
		if (dotPos > 0) { obj = obj.substring(dotPos); }
		Side side = caller.getMethodName().equals("findChunksForSpawning") || caller.getMethodName().equals("performWorldGenSpawning") ?
				Side.SERVER :
				Util.instance.getSide();
		String trg = getKey(target);
		if (!trg.isEmpty()) { trg = "_" + trg; }
		data.get(side).end(getKey(obj), methodName + trg);
	}

	public void start(Object target) {
		//if (!CustomNpcs.VerboseDebug) { return; }
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		String obj = caller.getClassName();
		String methodName = caller.getMethodName() + "()";
		if (methodName.startsWith("lambda$")) {
			methodName = methodName.substring(methodName.indexOf("$") + 1);
			if (methodName.contains("$")) {
				methodName = methodName.substring(0, methodName.indexOf("$"));
			}
		}
		int dotPos = obj.lastIndexOf(".") + 1;
		if (dotPos > 0) { obj = obj.substring(dotPos); }
		Side side = caller.getMethodName().equals("findChunksForSpawning") || caller.getMethodName().equals("performWorldGenSpawning") ?
				Side.SERVER :
				Util.instance.getSide();
		String trg = getKey(target);
		if (!trg.isEmpty()) { trg = "_" + trg; }
		data.get(side).start(getKey(obj), methodName + trg);
	}

	public void start(Object target, String addedToMethodName) {
		//if (!CustomNpcs.VerboseDebug) { return; }
		StackTraceElement caller = Thread.currentThread().getStackTrace()[2];
		String obj = caller.getClassName();
		String methodName = caller.getMethodName() + "(" + addedToMethodName + ")";
		if (methodName.startsWith("lambda$")) {
			methodName = methodName.substring(methodName.indexOf("$") + 1);
			if (methodName.contains("$")) {
				methodName = methodName.substring(0, methodName.indexOf("$"));
			}
		}
		int dotPos = obj.lastIndexOf(".") + 1;
		if (dotPos > 0) { obj = obj.substring(dotPos); }
		Side side = caller.getMethodName().equals("findChunksForSpawning") || caller.getMethodName().equals("performWorldGenSpawning") ?
				Side.SERVER :
				Util.instance.getSide();
		String trg = getKey(target);
		if (!trg.isEmpty()) { trg = "_" + trg; }
		data.get(side).start(getKey(obj), methodName + trg);
	}

	public DataDebug() { clear(); }

	public void stop() {
		//if (!CustomNpcs.VerboseDebug) { return; }
		for (Side side : data.keySet()) {
			for (String k : data.get(side).starters.keySet()) {
				data.get(side).end(k.substring(0, k.indexOf(':')), k.substring(k.indexOf(':') + 1));
			}
		}
	}

	public void clear() {
		stop();
		data.put(Side.SERVER, new Debug());
		data.put(Side.CLIENT, new Debug());
	}

	public List<String> logging() {
		StringBuilder tempInfo;
		String temp;
		LogWriter.info("Output full debug info " + CustomNpcs.MODNAME + ":");
		LogWriter.info("Showing Monitoring results for ANY side. { Side: [Target.Method names; Runs; Average time] }:");
		StringBuilder maxInfo = new StringBuilder("Output maximums from debug info ").append(CustomNpcs.MODNAME).append(":");
		stop();
		boolean start = false;
		List<String> list = new ArrayList<>();
		for (Side side : data.keySet()) {
			if (start) { LogWriter.info("----   ----   ----"); }
			LogWriter.info("Side: " + side.name());
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
					if (target.lastIndexOf("_") != -1) {
						log.append("  [").append(target, 0, target.lastIndexOf("_"))
								.append(", ").append(target.substring(target.lastIndexOf("_") + 1));
					}
					else { log.append("  [").append(target); }
					log.append(", ")
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
				LogWriter.info(" [" + (i + 1) + "/" + events.size() + "] - \"" + eventName + "\": " + log);
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
		LogWriter.info("Caches:");
		if (started != 0) {
			long time = (System.currentTimeMillis() - started) / 50L;
			long ticks;
			String side;
			if (CustomNpcs.Server != null) {
				side = "Server";
				ticks = CustomNpcs.Server.getWorld(0).getTotalWorldTime() - startedTicks;
			} else {
				side = "Client";
				ticks = ClientTickHandler.ticks - startedTicks;
			}
			tempInfo = new StringBuilder(TextFormatting.GRAY.toString())
					.append(side)
					.append(" system running time ")
					.append(Util.instance.ticksToElapsedTime(time, false, true, false))
					.append(TextFormatting.GRAY)
					.append(", game running time ")
					.append(Util.instance.ticksToElapsedTime(ticks, false, true, false))
					.append(TextFormatting.GRAY)
					.append(" (")
					.append(TextFormatting.BLUE)
					.append(Math.round((double) Math.abs(time - ticks) / (double) time * 1000.0d) /1000.0d)
					.append(TextFormatting.GRAY)
					.append("%)");
			list.add(tempInfo.toString());
			LogWriter.info(Util.instance.deleteColor(tempInfo.toString()));
		}

		MemoryUsage memUsage = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		tempInfo = new StringBuilder(TextFormatting.GRAY.toString())
				.append("Current total memory: ")
				.append(TextFormatting.RESET)
				.append(Util.instance.getTextReducedNumber(memUsage.getUsed(), false, true, true))
				.append(TextFormatting.GRAY)
				.append(" / ")
				.append(TextFormatting.RESET)
				.append(Util.instance.getTextReducedNumber(memUsage.getMax(), false, true, true))
				.append(TextFormatting.GRAY)
				.append(" byte");
		list.add(tempInfo.toString());
		LogWriter.info(Util.instance.deleteColor(tempInfo.toString()));

		LogWriter.info(maxInfo);
		return list;
	}

}
