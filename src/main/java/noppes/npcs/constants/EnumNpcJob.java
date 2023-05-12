package noppes.npcs.constants;

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

public enum EnumNpcJob {
	
	DEFAULT(JobInterface.class, "none", false),
	BARD(JobBard.class, "bard", true),
	HEALER(JobHealer.class, "healer", true),
	GUARD(JobGuard.class, "guard", true),
	ITEM_GIVER(JobItemGiver.class, "itemgiver", true),
	FOLLOWER(JobFollower.class, "follower", true),
	SPAWNER(JobSpawner.class, "spawner", true),
	CONVERSATION(JobConversation.class, "conversation", true),
	CHUNK_LOADER(JobChunkLoader.class, "chunkloader", false),
	BUILDER(JobBuilder.class, "builder", false),
	FARMER(JobFarmer.class, "farmer", true);

	public String name;
	public boolean hasSettings;
	Class<?> parent;
	
	EnumNpcJob(Class<?> clazz, String named, boolean hasSet) {
		parent = clazz;
		name = "job."+named;
		hasSettings = hasSet;
	}
	
	public void setToNpc(EntityNPCInterface npc) {
		try {
			npc.advanced.jobInterface = (JobInterface) parent.getConstructor(EntityNPCInterface.class).newInstance(npc);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}


	public static String[] getNames() {
		List<String> list = Lists.newArrayList();
		for (EnumNpcJob ej : EnumNpcJob.values()) { list.add(ej.name); }
		return list.toArray(new String[list.size()]);
	}

	public boolean isClass(JobInterface jobInterface) { return jobInterface.getClass() == parent; }
	
}
