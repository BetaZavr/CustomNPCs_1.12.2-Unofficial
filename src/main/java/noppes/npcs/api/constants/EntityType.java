package noppes.npcs.api.constants;

public enum EntityType {

	ANY(-1),
	UNKNOWN(0),
	PLAYER(1),
	NPC(2),
	MONSTER(3),
	ANIMAL(4),
	LIVING(5),
	ITEM(6),
	PROJECTILE(7),
	PIXELMON(8),
	VILLAGER(9),
	ARROW(10),
	THROWABLE(11);

	final int type;

	EntityType(int t) { type = t; }

	public int get() { return type; }

}
