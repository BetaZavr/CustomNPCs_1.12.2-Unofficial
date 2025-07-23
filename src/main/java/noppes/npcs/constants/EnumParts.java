package noppes.npcs.constants;

public enum EnumParts
{
	EARS("ears", -1), 
	HORNS("horns", -1), 
	HAIR("hair", -1), 
	MOHAWK("mohawk", -1), 
	SNOUT("snout", -1), 
	BEARD("beard", -1), 
	TAIL("tail", -1), 
	CLAWS("claws", -1), 
	LEGS("legs", 4), 
	FIN("fin", -1), 
	SKIRT("skirt", -1), 
	WINGS("wings", -1), 
	HEAD("head", 0), 
	BODY("body", 3), 
	BREASTS("breasts", -1), 
	PARTICLES("particles", -1), 
	ARM_LEFT("armleft", 1), 
	ARM_RIGHT("armright", 2), 
	WRIST_LEFT("wristleft", 1), 
	WRIST_RIGHT("wristright", 2), 
	LEFT_STACK("left_stack", 6), 
	RIGHT_STACK("right_stack", 7), 
	LEG_LEFT("legleft", 4), 
	LEG_RIGHT("legright", 5), 
	FOOT_LEFT("footleft", 4), 
	FOOT_RIGHT("footright", 5), 
	EYES("eyes", -1),
	BELT("belt", -1), 
	FEET_LEFT("bootleft", 4), 
	FEET_RIGHT("bootright", 5),
	CUSTOM("custom", -1),
	CUSTOM_LAYERS("layers", -1);
	
	public final String name;
	public final int patterns;
	
	EnumParts(String name, int id) {
		this.patterns = id;
		this.name = name;
	}
	
	public static EnumParts FromName(String name) {
		for (EnumParts e : values()) {
			if (e.name.equals(name)) {
				return e;
			}
		}
		return null;
	}

	public static EnumParts get(int part) {
		for (EnumParts e : values()) {
			if (e.patterns == part) { return e; }
		}
		return EnumParts.CUSTOM;
	}

	public static EnumParts getMainModel(int ordinal) {
		EnumParts[] set = EnumParts.values();
		if (ordinal < 0) { ordinal *= -1; }
		EnumParts ep = set[ordinal % set.length];
		if (ep == EnumParts.HEAD || ep == EnumParts.BODY ||
				ep == EnumParts.ARM_LEFT || ep == EnumParts.ARM_RIGHT ||
				ep == EnumParts.LEG_LEFT || ep == EnumParts.LEG_RIGHT ||
				ep == EnumParts.WRIST_LEFT || ep == EnumParts.WRIST_RIGHT ||
				ep == EnumParts.FOOT_LEFT || ep == EnumParts.FOOT_RIGHT)
		{ return ep; }
		return EnumParts.HEAD;
	}
}
