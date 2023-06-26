package noppes.npcs.api.constants;

public enum OptionType {
	
	COMMAND_BLOCK(4),
	DIALOG_OPTION(1),
	DISABLED(2),
	QUIT_OPTION(0),
	ROLE_OPTION(3);
	
	int type = -1;
	
	OptionType(int t) { this.type= t; }
	
	public int get() { return this.type; }
	
}
