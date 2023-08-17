package noppes.npcs.api.constants;

import noppes.npcs.constants.EnumNpcRole;

public enum RoleType {
	
	BANK(EnumNpcRole.BANK.ordinal()),
	COMPANION(EnumNpcRole.COMPANION.ordinal()),
	DIALOG(EnumNpcRole.DIALOG.ordinal()),
	FOLLOWER(EnumNpcRole.FOLLOWER.ordinal()),
	MAILMAN(EnumNpcRole.POSTMAN.ordinal()),
	MAXSIZE(EnumNpcRole.values().length),
	NONE(EnumNpcRole.DEFAULT.ordinal()),
	TRADER(EnumNpcRole.TRADER.ordinal()),
	TRANSPORTER(EnumNpcRole.TRANSPORTER.ordinal());
	
	int type = -1;
	
	RoleType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
