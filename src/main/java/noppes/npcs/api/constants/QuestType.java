package noppes.npcs.api.constants;

public enum QuestType {
	
	AREA_KILL(4),
	DIALOG(1),
	ITEM(0),
	KILL(2),
	LOCATION(3),
	MANUAL(5),
	CRAFT(6);
	
	int type = -1;
	
	QuestType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
