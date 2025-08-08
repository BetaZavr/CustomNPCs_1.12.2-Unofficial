package noppes.npcs.api.constants;

import net.minecraft.util.EnumParticleTypes;

public enum ParticleType {

	NONE(0),
	SMOKE(1),
	PORTAL(2),
	REDSTONE(3),
	LIGHTNING(4),
	LARGE_SMOKE(5),
	MAGIC(6),
	ENCHANT(7),
	CRIT(8);

	public static EnumParticleTypes getMCType(int type) {
		if (type == 1) {
			return EnumParticleTypes.SMOKE_NORMAL;
		}
		if (type == 2) {
			return EnumParticleTypes.PORTAL;
		}
		if (type == 3) {
			return EnumParticleTypes.REDSTONE;
		}
		if (type == 4) {
			return EnumParticleTypes.CRIT_MAGIC;
		}
		if (type == 5) {
			return EnumParticleTypes.SMOKE_LARGE;
		}
		if (type == 6) {
			return EnumParticleTypes.SPELL_WITCH;
		}
		if (type == 7) {
			return EnumParticleTypes.ENCHANTMENT_TABLE;
		}
		if (type == 8) {
			return EnumParticleTypes.CRIT;
		}
		return null;
	}

	final int type;

	ParticleType(int t) { type = t; }

	public int get() { return type; }

}
