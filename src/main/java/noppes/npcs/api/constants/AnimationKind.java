package noppes.npcs.api.constants;

public enum AnimationKind {

	ATTACKING(0, false, true),
	DIES(1, false, true),
	FLY_STAND(2, true, false),
	FLY_WALK(3, true, false),
	INIT(4, false, true),
	JUMP(5, false, true),
	STANDING(6, true, false),
	WALKING(7, true, false),
	WATER_STAND(8, true, false),
	WATER_WALK(9, true, false),
	REVENGE_STAND(10, true, false),
	REVENGE_WALK(11, true, false),
	HIT(12, false, true),
	BASE(13, true, false),
	SHOOT(14, false, true),
	AIM(15, true, false),
	SWING(16, false, true),
	INTERACT(17, false, false),
	BLOCKED(18, false, true),
	EDITING(18, false, true),
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

	final boolean isMovement;
	final boolean isQuickStart;
	final int type;

	AnimationKind(int i, boolean movement, boolean quickStart) {
		this.type = i;
		this.isMovement = movement;
		this.isQuickStart = quickStart;
	}

	public int get() { return this.type; }

	public boolean isMovement() { return this.isMovement; }

	public boolean isQuickStart() { return this.isQuickStart; }

}
