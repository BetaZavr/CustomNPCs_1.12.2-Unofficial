package noppes.npcs.api.constants;

public enum SideType {

	DOWN(0),
	UP(1),
	NORTH(2),
	SOUTH(3),
	WEST(4),
	EAST(5);

	final int type;

	SideType(int t) { type = t; }

	public int get() { return type; }

}
