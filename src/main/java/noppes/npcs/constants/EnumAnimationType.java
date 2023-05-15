package noppes.npcs.constants;

import java.util.List;

import com.google.common.collect.Lists;

public enum EnumAnimationType {

	attacking,
	standing,
	dies,
	walking;

	public static String[] getNames() {
		List<String> list = Lists.<String>newArrayList();
		for (EnumAnimationType eat : EnumAnimationType.values()) { list.add("puppet."+eat.name()); }
		return list.toArray(new String[list.size()]);
	}
	
}
