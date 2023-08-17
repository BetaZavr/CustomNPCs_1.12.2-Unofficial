package noppes.npcs.api.constants;

import noppes.npcs.constants.EnumNpcJob;

public enum JobType {
	
	BARD(EnumNpcJob.BARD.ordinal()),
	BUILDER(EnumNpcJob.BUILDER.ordinal()),
	CHUNKLOADER(EnumNpcJob.CHUNK_LOADER.ordinal()),
	CONVERSATION(EnumNpcJob.CONVERSATION.ordinal()),
	FARMER(EnumNpcJob.FARMER.ordinal()),
	FOLLOWER(EnumNpcJob.FOLLOWER.ordinal()),
	GUARD(EnumNpcJob.GUARD.ordinal()),
	HEALER(EnumNpcJob.HEALER.ordinal()),
	ITEMGIVER(EnumNpcJob.ITEM_GIVER.ordinal()),
	MAXSIZE(EnumNpcJob.values().length),
	NONE(EnumNpcJob.DEFAULT.ordinal()),
	SPAWNER(EnumNpcJob.SPAWNER.ordinal());
	
	int type = -1; 
	
	JobType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
