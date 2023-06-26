package noppes.npcs.api.constants;

public enum ItemType {
	
	ARMOR(3),
	BLOCK(2),
	BOOK(1),
	NORMAL(0),
	SCRIPTED(6),
	SEEDS(5),
	SWORD(4);
	
	int type = -1;
	
	ItemType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
