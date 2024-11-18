package noppes.npcs.mixin.util.text.translation;

import net.minecraft.util.text.translation.LanguageMap;
import noppes.npcs.api.mixin.util.text.translation.ILanguageMapMixin;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(value = LanguageMap.class)
public class LanguageMapMixin implements ILanguageMapMixin {

    @Final
    @Shadow
    private Map<String, String> languageList;

    @Override
    public Map<String, String> npcs$getLanguageList() { return languageList; }


    @Inject(method = "tryTranslateKey", at = @At("HEAD"), cancellable = true)
    private void npcs$tryTranslateKey(String key, CallbackInfoReturnable<String> cir) {
        if (key.startsWith("enchantment.level.")) {
            try {
                int level = Integer.parseInt(key.replace("enchantment.level.", ""));
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
