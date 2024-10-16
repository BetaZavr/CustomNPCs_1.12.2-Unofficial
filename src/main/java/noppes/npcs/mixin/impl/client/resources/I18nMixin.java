package noppes.npcs.mixin.impl.client.resources;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.resources.Locale;
import noppes.npcs.mixin.client.resources.II18nMixin;
import noppes.npcs.mixin.client.resources.ILocaleMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = I18n.class)
public class I18nMixin implements II18nMixin {

    @Shadow
    private static Locale i18nLocale;

    @Override
    public Map<String, String> npcs$getProperties() {
        return ((ILocaleMixin) i18nLocale).npcs$getProperties();
    }

}
