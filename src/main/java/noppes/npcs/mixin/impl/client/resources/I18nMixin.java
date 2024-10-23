package noppes.npcs.mixin.impl.client.resources;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import noppes.npcs.mixin.client.resources.II18nMixin;
import noppes.npcs.mixin.client.resources.ILocaleMixin;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = I18n.class)
public class I18nMixin implements II18nMixin {

    @Shadow
    private static Locale i18nLocale;

    @Inject(method = "format", at = @At("TAIL"), cancellable = true)
    private static void npcs$format(String translateKey, Object[] parameters, CallbackInfoReturnable<String> cir) {
        if (translateKey.startsWith("enchantment.level.")) {
            try {
                int level = Integer.parseInt(translateKey.replace("enchantment.level.", ""));
                if (level < 100) {
                    cir.cancel();
                    cir.setReturnValue(Util.instance.getTextNumberToRoman(level + 1));
                }
                if (level > 100) {
                    cir.cancel();
                    cir.setReturnValue("" + level);
                }
            }
            catch (Exception ignored) { }
        }
    }

    @Override
    public Map<String, String> npcs$getProperties() {
        return ((ILocaleMixin) i18nLocale).npcs$getProperties();
    }

}
