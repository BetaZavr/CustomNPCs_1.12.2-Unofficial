package noppes.npcs.api.constants;

public enum AnimationKind {

	ATTACKING(0, false, true, false),
	DIES(1, true, true, false),
	FLY_STAND(2, true, false, false),
	FLY_WALK(3, true, false, false),
	INIT(4, false, false, false),
	JUMP(5, false, true, false),
	STANDING(6, true, false, false),
	WALKING(7, true, false, false),
	WATER_STAND(8, true, false, false),
	WATER_WALK(9, true, false, false),
	REVENGE_STAND(10, true, false, false),
	REVENGE_WALK(11, true, false, false),
	HIT(12, false, true, false),
	BASE(13, true, false, false),
	SHOOT(14, false, true, true),
	AIM(15, true, false, false),
	SWING(16, false, true, true),
	INTERACT(17, false, false, false),
	BLOCKED(18, false, true, true),
	; // -> GuiNpcAnimation

	public static AnimationKind get(int type) {
		for (AnimationKind ak : AnimationKind.values()) {
			if (ak.type == type) {
				return ak;
			}
		}
		return AnimationKind.STANDING;
	}

	public static String[] getNames() {
		String[] list = new String[AnimationKind.values().length];
		for (AnimationKind enm : AnimationKind.values()) {
			list[enm.type] = "puppet." + enm.name().toLowerCase().replace("_", "");
		}
		return list;
	}

	final boolean isCyclical;
	final boolean isQuickStart;
	final boolean isQuickEnd;
	final int type;

	AnimationKind(int i, boolean cyclical, boolean quickStart, boolean quickEnd) {
		this.type = i;
		this.isCyclical = cyclical;
		this.isQuickStart = quickStart;
		this.isQuickEnd = quickEnd;
	}

	public int get() { return this.type; }

	public boolean isCyclical() { return this.isCyclical; }

	public boolean isQuickStart() { return this.isQuickStart; }

	public boolean isQuickEnd() { return this.isQuickEnd; }

}
