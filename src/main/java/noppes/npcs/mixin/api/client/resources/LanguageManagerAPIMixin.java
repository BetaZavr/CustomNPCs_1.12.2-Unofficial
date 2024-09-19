package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = LanguageManager.class)
public interface LanguageManagerAPIMixin {

    @Accessor(value="CURRENT_LOCALE")
    Locale npcs$getCurrentLocate();

    @Accessor(value="currentLanguage")
    String npcs$getCurrentLanguage();

}
