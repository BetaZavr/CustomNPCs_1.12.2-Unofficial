package noppes.npcs.mixin.api.util.text.translation;

import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = I18n.class)
public interface I18nAPIMixin {

    @Accessor(value="localizedName")
    LanguageMap npcs$getLocalizedName();

}
