package noppes.npcs.api.constants;

public enum MarkType {

	NONE(0),
	QUESTION(1),
	EXCLAMATION(2),
	POINTER(3),
	SKULL(4),
	CROSS(5),
	STAR(6);

	final int type;

	MarkType(int t) { type = t; }

	public int get() { return type; }

}
