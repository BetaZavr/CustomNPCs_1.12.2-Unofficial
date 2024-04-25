package noppes.npcs.api.constants;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public enum PotionEffectType {

	BLINDNESS(7), FIRE(1), HUNGER(3), NAUSEA(6), NONE(0), POISON(2), SLOWNESS(5), WEAKNESS(4), WITHER(8);

	public static Potion getMCType(int effect) {
		switch (effect) {
		case 2: {
			return MobEffects.POISON;
		}
		case 3: {
			return MobEffects.HUNGER;
		}
		case 4: {
			return MobEffects.WEAKNESS;
		}
		case 5: {
			return MobEffects.SLOWNESS;
		}
		case 6: {
			return MobEffects.NAUSEA;
		}
		case 7: {
			return MobEffects.BLINDNESS;
		}
		case 8: {
			return MobEffects.WITHER;
		}
		default: {
			return null;
		}
		}
	}

	int type = -1;

	PotionEffectType(int t) {
		this.type = t;
	}

	public int get() {
		return this.type;
	}
}
