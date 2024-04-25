package noppes.npcs.constants;

public enum EnumCompanionStage {
	BABY(0, 7, "companion.baby"), CHILD(72000, 0, "companion.child"), TEEN(180000, 0,
			"companion.teenager"), ADULT(324000, 0, "companion.adult"), FULLGROWN(450000, 0, "companion.fullgrown");

	public int matureAge;
	public int animation;
	public String name;

	private EnumCompanionStage(int age, int animation, String name) {
		this.matureAge = age;
		this.animation = animation;
		this.name = name;
	}
}
