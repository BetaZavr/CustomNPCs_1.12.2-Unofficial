package noppes.npcs.mixin.api.client.settings;

import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KeyBinding.class)
public interface KeyBindingAPIMixin {

    @Mutable
    @Accessor(value="keyDescription")
    void npcs$setKeyDescription(String newKeyDescription);

    @Mutable
    @Accessor(value="keyCodeDefault")
    void npcs$setKeyCodeDefault(int newKeyCodeDefault);

    @Mutable
    @Accessor(value="keyCategory")
    void npcs$setKeyCategory(String newKeyCategory);

}
