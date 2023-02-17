package noppes.npcs.api.constants;

import net.minecraft.util.EnumParticleTypes;

public class ParticleType {
	public static int CRIT = 8;
	public static int ENCHANT = 7;
	public static int LARGE_SMOKE = 5;
	public static int LIGHTNING = 4;
	public static int MAGIC = 6;
	public static int NONE = 0;
	public static int PORTAL = 2;
	public static int REDSTONE = 3;
	public static int SMOKE = 1;

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
