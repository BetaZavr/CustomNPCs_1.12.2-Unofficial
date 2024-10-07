package noppes.npcs.mixin.impl.util.text.translation;

import net.minecraft.util.text.translation.I18n;
import net.minecraft.util.text.translation.LanguageMap;
import noppes.npcs.mixin.util.text.translation.II18nMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = I18n.class)
public class I18nMixin implements II18nMixin {

    @Final
    @Shadow(aliases = "localizedName")
    private static LanguageMap localizedName;

    @Override
    public LanguageMap npcs$getLocalizedName() { return localizedName; }

}

