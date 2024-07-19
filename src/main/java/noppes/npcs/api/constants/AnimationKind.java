package noppes.npcs.api.constants;

public enum AnimationKind {

	ATTACKING(0, false, false, true, false),
	DIES(1, false, true, false, false),
	FLY_STAND(2, true, false, false, false),
	FLY_WALK(3, true, false, false, false),
	INIT(4, false, false, false, true),
	JUMP(5, false, true, true, true),
	STANDING(6, true, false, false, false),
	WALKING(7, true, false, false, false),
	WATER_STAND(8, true, false, false, false),
	WATER_WALK(9, true, false, false, false),
	REVENGE_STAND(10, true, false, false, false),
	REVENGE_WALK(11, true, false, false, false),
	HIT(12, false, false, true, true),
	BASE(13, true, false, false, false),
	SHOOT(14, false, false, true, true),
	AIM(15, true, true, true, false),
	SWING(16, false, false, true, true),
	INTERACT(17, false, false, false, true),
	BLOCKED(18, false, false, true, true),
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

	final boolean isMoving;
	final boolean isRepeat;
	final boolean isFastReturn;
	final boolean itStartsOver;
	final int type;

	AnimationKind(int i, boolean moving, boolean repeat, boolean fastReturn, boolean startsOver) {
		this.type = i;
		this.isMoving = moving;
		this.isRepeat = repeat;
		this.isFastReturn = fastReturn;
		this.itStartsOver = startsOver;
	}

	public int get() { return this.type; }

	public boolean isMoving() { return this.isMoving; }

	public boolean isRepeat() { return this.isRepeat; }

	public boolean isFastReturn() { return this.isFastReturn; }

	public boolean itStartsOver() { return this.itStartsOver; }

}
