package noppes.npcs.api.constants;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public enum PotionEffectType {

	NONE(0),
	FIRE(1),
	POISON(2),
	HUNGER(3),
	WEAKNESS(4),
	SLOWNESS(5),
	NAUSEA(6),
	BLINDNESS(7),
	WITHER(8);

	public static Potion getMCType(int effect) {
		switch (effect) {
			case 2: return MobEffects.POISON;
			case 3: return MobEffects.HUNGER;
			case 4: return MobEffects.WEAKNESS;
			case 5: return MobEffects.SLOWNESS;
			case 6: return MobEffects.NAUSEA;
			case 7: return MobEffects.BLINDNESS;
			case 8: return MobEffects.WITHER;
			default: return null;
		}
	}

	final int type;

	PotionEffectType(int t) { type = t; }

	public int get() { return type; }

}
