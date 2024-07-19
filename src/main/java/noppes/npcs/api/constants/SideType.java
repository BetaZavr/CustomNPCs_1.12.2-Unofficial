package noppes.npcs.api.constants;

public enum SideType {

	DOWN(0), EAST(5), NORTH(2), SOUTH(3), UP(1), WEST(4);

	final int type;

	SideType(int t) {
		this.type = t;
	}

	public int get() {
		return this.type;
	}

}
