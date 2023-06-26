package noppes.npcs.api.constants;

import net.minecraft.util.EnumParticleTypes;

public enum ParticleType {
	
	CRIT(8),
	ENCHANT(7),
	LARGE_SMOKE(5),
	LIGHTNING(4),
	MAGIC(6),
	NONE(0),
	PORTAL(2),
	REDSTONE(3),
	SMOKE(1);
	
	int type = -1;
	
	ParticleType(int t) { this.type= t; }
	
	public int get() { return this.type; }

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
}
