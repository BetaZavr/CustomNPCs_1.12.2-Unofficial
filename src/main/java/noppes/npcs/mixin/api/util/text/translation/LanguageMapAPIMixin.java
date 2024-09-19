package noppes.npcs.mixin.api.util.text.translation;

import net.minecraft.util.text.translation.LanguageMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = LanguageMap.class)
public interface LanguageMapAPIMixin {

    @Accessor(value="languageList")
    Map<String, String> npcs$getLanguageList();

}
