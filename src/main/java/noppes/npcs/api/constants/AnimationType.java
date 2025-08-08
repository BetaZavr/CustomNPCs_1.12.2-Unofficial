package noppes.npcs.api.constants;

public enum AnimationType {

	NORMAL(0),
	SIT(1),
	SLEEP(2),
	HUG(3),
	SNEAK(4),
	DANCE(5),
	AIM(6),
	CRAWL(7),
	POINT(8),
	CRY(9),
	WAVE(10),
	BOW(11),
	NO(12),
	YES(13),
	DEATH(14);

	final int type;

	AnimationType(int t) { type = t; }

	public int get() { return type; }

	public static int get(String name) {
		for (AnimationType atEnum : AnimationType.values()) {
			if (atEnum.name().equalsIgnoreCase(name)) { return atEnum.get(); }
		}
		return NORMAL.get();
	}

}
