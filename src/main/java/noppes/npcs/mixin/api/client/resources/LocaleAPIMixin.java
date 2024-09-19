package noppes.npcs.mixin.api.client.resources;

import net.minecraft.client.resources.Locale;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(value = Locale.class)
public interface LocaleAPIMixin {

    @Accessor(value="properties")
    Map<String, String> npcs$getProperties();

}
