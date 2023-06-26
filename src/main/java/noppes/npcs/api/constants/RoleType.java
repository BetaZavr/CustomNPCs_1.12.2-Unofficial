package noppes.npcs.api.constants;

public enum RoleType {
	
	BANK(3),
	COMPANION(6),
	DIALOG(7),
	FOLLOWER(2),
	MAILMAN(5),
	MAXSIZE(8),
	NONE(0),
	TRADER(1),
	TRANSPORTER(4);
	
	int type = -1;
	
	RoleType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
