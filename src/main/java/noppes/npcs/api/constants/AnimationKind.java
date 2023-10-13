package noppes.npcs.api.constants;

import java.util.List;

import com.google.common.collect.Lists;

public enum AnimationKind {

	ATTACKING(0, false),
	DIES(1, false),
	FLY_STAND(2, false),
	FLY_WALK(3, true),
	INIT(4, false),
	JUMP(5, false),
	STANDING(6, false),
	WALKING(7, true),
	WATER_STAND(8, false),
	WATER_WALK(9, true),
	REVENGE_STAND(10, false),
	REVENGE_WALK(11, true),
	HIT(12, true);
	
	boolean isMoving;
	int type;
	
	AnimationKind(int i, boolean m) {
		this.isMoving = m;
		this.type = i;
	}

	public static String[] getNames() {
		List<String> list = Lists.<String>newArrayList();
		for (AnimationKind eat : AnimationKind.values()) { list.add("puppet."+eat.name().toLowerCase().replace("_", "")); }
		return list.toArray(new String[list.size()]);
	}
	
	public int get() { return this.type; }

	public boolean isMoving() { return this.isMoving; }

	public static AnimationKind get(int type) {
		for (AnimationKind ak : AnimationKind.values()) {
			if (ak.type==type) { return ak; }
		}
		return AnimationKind.STANDING;
	}
	
}
