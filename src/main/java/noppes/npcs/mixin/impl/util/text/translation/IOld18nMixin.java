package noppes.npcs.mixin.impl.util.text.translation;

import com.google.common.collect.Maps;
import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import noppes.npcs.mixin.util.text.translation.ILanguageMapMixin;
import noppes.npcs.mixin.util.text.translation.IOldI18nMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = I18n.class)
public class IOld18nMixin implements IOldI18nMixin {

    @Final
    @Shadow
    private static LanguageMap localizedName;

    @Override
    public Map<String, String> npcs$getLocalizedName() {
        if (localizedName != null) {
            return ((ILanguageMapMixin) localizedName).npcs$getLanguageList();
        }
        return Maps.newHashMap();
    }

}

