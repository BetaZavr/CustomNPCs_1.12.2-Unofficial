package noppes.npcs.api.constants;

public enum OptionType {

	QUIT_OPTION(0),
	DIALOG_OPTION(1),
	DISABLED(2),
	ROLE_OPTION(3),
	COMMAND_BLOCK(4);

	public static OptionType get(int id) {
		for (OptionType ot : OptionType.values()) {
			if (ot.get() == id) { return ot; }
		}
		return DISABLED;
	}

	final int type;

	OptionType(int t) { type = t; }

	public int get() { return type; }

}
