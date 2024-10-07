package noppes.npcs.mixin.impl.util.text.translation;

import net.minecraft.util.text.translation.LanguageMap;
import noppes.npcs.mixin.util.text.translation.ILanguageMapMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = LanguageMap.class)
public class LanguageMapMixin implements ILanguageMapMixin {

    @Final
    @Shadow(aliases = "languageList")
    private Map<String, String> languageList;

    @Override
    public Map<String, String> npcs$getLanguageList() { return languageList; }

}
