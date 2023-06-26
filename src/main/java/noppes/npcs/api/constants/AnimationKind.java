package noppes.npcs.api.constants;

import java.util.List;

import com.google.common.collect.Lists;

public enum AnimationKind {

	ATTACKING(0, false, true),
	DIES(1, true, false),
	FLY_STAND(2, true, false),
	FLY_WALK(3, true, true),
	INIT(4, false, false),
	JUMP(5, false, false),
	STANDING(6, true, false),
	WALKING(7, true, true),
	WATER_STAND(8, true, false),
	WATER_WALK(9, true, true);
	
	boolean cyclical, moving;
	int type;
	
	AnimationKind(int i, boolean c, boolean m) {
		this.cyclical = c;
		this.moving = m;
		this.type = i;
	}

	public static String[] getNames() {
		List<String> list = Lists.<String>newArrayList();
		for (AnimationKind eat : AnimationKind.values()) { list.add("puppet."+eat.name().toLowerCase().replace("_", "")); }
		return list.toArray(new String[list.size()]);
	}
	
	public int get() { return this.type; }
	
	public boolean isCyclical() { return this.cyclical; }

	public boolean isMoving() { return this.moving; }
	
}
