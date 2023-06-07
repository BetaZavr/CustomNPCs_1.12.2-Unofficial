package noppes.npcs.constants;

import java.util.List;

import com.google.common.collect.Lists;

public enum EnumAnimationType {

	attacking(false, true),
	dies(true, false),
	flystand(true, false),
	flywalk(true, true),
	init(false, false),
	jump(false, false),
	standing(true, false),
	walking(true, true),
	waterstand(true, false),
	waterwalk(true, true);
	
	boolean cyclical, moving;
	
	EnumAnimationType(boolean c, boolean m) {
		this.cyclical = c;
		this.moving = m;
	}

	public static String[] getNames() {
		List<String> list = Lists.<String>newArrayList();
		for (EnumAnimationType eat : EnumAnimationType.values()) { list.add("puppet."+eat.name()); }
		return list.toArray(new String[list.size()]);
	}

	public boolean isCyclical() { return this.cyclical; }

	public boolean isMoving() { return this.moving; }
	
}
