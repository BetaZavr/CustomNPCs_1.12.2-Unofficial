package noppes.npcs.api.constants;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.collect.Lists;

import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;
import noppes.npcs.roles.JobBuilder;
import noppes.npcs.roles.JobChunkLoader;
import noppes.npcs.roles.JobConversation;
import noppes.npcs.roles.JobFarmer;
import noppes.npcs.roles.JobFollower;
import noppes.npcs.roles.JobGuard;
import noppes.npcs.roles.JobHealer;
import noppes.npcs.roles.JobInterface;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.roles.JobSpawner;

public enum JobType {

	DEFAULT(JobInterface.class, "none", 0, false),
	BARD(JobBard.class, "bard", 1, true),
	HEALER(JobHealer.class, "healer", 2, true),
	GUARD(JobGuard.class, "guard", 3, true),
	ITEM_GIVER(JobItemGiver.class, "itemgiver", 4, true),
	FOLLOWER(JobFollower.class, "follower", 5, true),
	SPAWNER(JobSpawner.class, "spawner", 6, true),
	CONVERSATION(JobConversation.class, "conversation", 7, true),
	CHUNK_LOADER(JobChunkLoader.class, "chunkloader", 8, false),
	BUILDER(JobBuilder.class, "builder", 10, false),
	FARMER(JobFarmer.class, "farmer", 11, true);

	private int type;
	public String name;
	public boolean hasSettings;
	private Class<?> parent;
	
	JobType(Class<?> clazz, String named, int t, boolean hasSet) {
		this.type = t;
		this.parent = clazz;
		this.name = "job."+named;
		this.hasSettings = hasSet;
	}
	
	public void setToNpc(EntityNPCInterface npc) {
		try {
			npc.advanced.jobInterface = (JobInterface) parent.getConstructor(EntityNPCInterface.class).newInstance(npc);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}


	public static String[] getNames() {
		List<String> list = Lists.newArrayList();
		for (JobType ej : JobType.values()) { list.add(ej.name); }
		return list.toArray(new String[list.size()]);
	}

	public boolean isClass(JobInterface jobInterface) { return jobInterface.getClass() == parent; }
	
	public int get() { return this.type; }

	public static JobType get(int id) {
		for (JobType ej : JobType.values()) { if (ej.type==id) { return ej; } }
		return JobType.DEFAULT;
	}

}
