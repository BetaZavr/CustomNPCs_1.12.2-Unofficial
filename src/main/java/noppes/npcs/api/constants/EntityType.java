package noppes.npcs.api.constants;

public enum EntityType {
	
	ANIMAL(4),
	ANY(-1),
	ARROW(10),
	ITEM(6),
	LIVING(5),
	MONSTER(3),
	NPC(2),
	PIXELMON(8),
	PLAYER(1),
	PROJECTILE(7),
	THROWABLE(11),
	UNKNOWN(0),
	VILLAGER(9);
	
	int type = -1;
	
	EntityType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
