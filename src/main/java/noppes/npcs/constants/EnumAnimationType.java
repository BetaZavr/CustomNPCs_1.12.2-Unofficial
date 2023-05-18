package noppes.npcs.constants;

import java.util.List;

import com.google.common.collect.Lists;

public enum EnumAnimationType {

	attacking(false),
	dies(true),
	flystand(true),
	flywalk(true),
	init(false),
	jump(false),
	standing(true),
	walking(true),
	waterstand(true),
	waterwalk(true);
	
	boolean cyclical;
	
	EnumAnimationType(boolean c) {
		this.cyclical = c;
	}

	public static String[] getNames() {
		List<String> list = Lists.<String>newArrayList();
		for (EnumAnimationType eat : EnumAnimationType.values()) { list.add("puppet."+eat.name()); }
		return list.toArray(new String[list.size()]);
	}

	public boolean isCyclical() { return this.cyclical; }
	
}
