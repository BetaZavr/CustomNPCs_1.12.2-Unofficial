package noppes.npcs.mixin.client.resources;

import net.minecraft.client.resources.I18n;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = I18n.class)
public class I18nMixin {

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

}
