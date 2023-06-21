package noppes.npcs.api.constants;

import net.minecraft.init.MobEffects;
import net.minecraft.potion.Potion;

public class PotionEffectType {
	
	public static int BLINDNESS = 7;
	public static int FIRE = 1;
	public static int HUNGER = 3;
	public static int NAUSEA = 6;
	public static int NONE = 0;
	public static int POISON = 2;
	public static int SLOWNESS = 5;
	public static int WEAKNESS = 4;
	public static int WITHER = 8;

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
}
