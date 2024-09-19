package noppes.npcs.mixin.util.text.translation;

import net.minecraft.util.text.translation.LanguageMap;
import noppes.npcs.mixin.api.util.text.translation.LanguageMapAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = LanguageMap.class)
public class LanguageMapMixin implements LanguageMapAPIMixin {

    @Final
    @Shadow(aliases = "languageList")
    private Map<String, String> languageList;

    @Override
    public Map<String, String> npcs$getLanguageList() { return languageList; }

}
