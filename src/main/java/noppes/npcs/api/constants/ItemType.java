package noppes.npcs.api.constants;

public enum ItemType {

	NORMAL(0),
	BOOK(1),
	BLOCK(2),
	ARMOR(3),
	SWORD(4),
	SEEDS(5),
	SCRIPTED(6);

	final int type;

	ItemType(int t) {
		this.type = t;
	}

	public int get() {
		return this.type;
	}

}
