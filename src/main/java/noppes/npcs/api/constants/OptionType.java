package noppes.npcs.api.constants;

public enum OptionType {

	COMMAND_BLOCK(4), DIALOG_OPTION(1), DISABLED(2), QUIT_OPTION(0), ROLE_OPTION(3);

	public static OptionType get(int id) {
		for (OptionType ot : OptionType.values()) {
			if (ot.get() == id) {
				return ot;
			}
		}
		return DISABLED;
	}

	int type = -1;

	OptionType(int t) {
		this.type = t;
	}

	public int get() {
		return this.type;
	}

}
