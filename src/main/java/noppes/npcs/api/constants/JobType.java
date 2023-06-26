package noppes.npcs.api.constants;

public enum JobType {
	
	BARD(1),
	BUILDER(10),
	CHUNKLOADER(8),
	CONVERSATION(7),
	FARMER(11),
	FOLLOWER(5),
	GUARD(3),
	HEALER(2),
	ITEMGIVER(4),
	MAXSIZE(12),
	NONE(0),
	PUPPET(9),
	SPAWNER(6);
	
	int type = -1;
	
	JobType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
