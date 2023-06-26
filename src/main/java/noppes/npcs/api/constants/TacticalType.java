package noppes.npcs.api.constants;

public enum TacticalType {
	
	AMBUSH(4),
	DEFAULT(0),
	DODGE(1),
	HITNRUN(3),
	NONE(6),
	STALK(5),
	SURROUND(2);
	
	int type = -1;
	
	TacticalType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
