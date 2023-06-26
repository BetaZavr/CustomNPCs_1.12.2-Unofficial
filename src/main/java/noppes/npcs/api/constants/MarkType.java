package noppes.npcs.api.constants;

public enum MarkType {
	
	CROSS(5),
	EXCLAMATION(2),
	NONE(0),
	POINTER(3),
	QUESTION(1),
	SKULL(4),
	STAR(6);
	
	int type = -1;
	
	MarkType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
