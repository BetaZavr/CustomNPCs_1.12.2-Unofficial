package noppes.npcs.api.constants;

import java.util.ArrayList;
import java.util.List;

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

	DEFAULT("none", 0, false),
	BARD("bard", 1, true),
	HEALER("healer", 2, true),
	GUARD("guard", 3, true),
	ITEM_GIVER("itemgiver", 4, true),
	FOLLOWER("follower", 5, true),
	SPAWNER("spawner", 6, true),
	CONVERSATION("conversation", 7, true),
	CHUNK_LOADER("chunkloader", 8, false),
	BUILDER("builder", 10, false),
	FARMER("farmer", 11, true);

	public static JobType get(int id) {
		for (JobType ej : JobType.values()) {
			if (ej.type == id) {
				return ej;
			}
		}
		return JobType.DEFAULT;
	}
	public static String[] getNames() {
		List<String> list = new ArrayList<>();
		for (JobType ej : JobType.values()) {
			list.add(ej.name);
		}
		return list.toArray(new String[0]);
	}
	private final int type;
	public final String name;
	public final boolean hasSettings;

	JobType(String named, int t, boolean hasSet) {
		type = t;
		name = "job." + named;
		hasSettings = hasSet;
	}

	public int get() { return type; }

	public void setToNpc(EntityNPCInterface npc) {
		switch (this) {
			case DEFAULT: npc.advanced.jobInterface = new JobInterface(npc); break;
			case BARD: npc.advanced.jobInterface = new JobBard(npc); break;
			case HEALER: npc.advanced.jobInterface = new JobHealer(npc); break;
			case GUARD: npc.advanced.jobInterface = new JobGuard(npc); break;
			case ITEM_GIVER: npc.advanced.jobInterface = new JobItemGiver(npc); break;
			case FOLLOWER: npc.advanced.jobInterface = new JobFollower(npc); break;
			case SPAWNER: npc.advanced.jobInterface = new JobSpawner(npc); break;
			case CONVERSATION: npc.advanced.jobInterface = new JobConversation(npc); break;
			case CHUNK_LOADER: npc.advanced.jobInterface = new JobChunkLoader(npc); break;
			case BUILDER: npc.advanced.jobInterface = new JobBuilder(npc); break;
			case FARMER: npc.advanced.jobInterface = new JobFarmer(npc); break;
		}
    }

}
