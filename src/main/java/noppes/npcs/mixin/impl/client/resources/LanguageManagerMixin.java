package noppes.npcs.mixin.impl.client.resources;

import net.minecraft.client.resources.LanguageManager;
import net.minecraft.client.resources.Locale;
import noppes.npcs.mixin.client.resources.ILanguageManagerMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = LanguageManager.class)
public class LanguageManagerMixin implements ILanguageManagerMixin {

    @Final
    @Shadow
    protected static Locale CURRENT_LOCALE;

    @Shadow
    private String currentLanguage;

    @Override
    public Locale npcs$getCurrentLocate() { return CURRENT_LOCALE; }

    @Override
    public String npcs$getCurrentLanguage() { return currentLanguage; }

}
