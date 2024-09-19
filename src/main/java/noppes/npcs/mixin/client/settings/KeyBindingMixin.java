package noppes.npcs.mixin.client.settings;

import net.minecraft.client.settings.KeyBinding;
import noppes.npcs.mixin.api.client.settings.KeyBindingAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = KeyBinding.class)
public class KeyBindingMixin implements KeyBindingAPIMixin {

    @Mutable
    @Final
    @Shadow(aliases = "keyDescription")
    private String keyDescription;

    @Mutable
    @Final
    @Shadow(aliases = "keyCodeDefault")
    private int keyCodeDefault;

    @Mutable
    @Final
    @Shadow(aliases = "keyCategory")
    private String keyCategory;

    @Override
    public void npcs$setKeyDescription(String newKeyDescription) {
        if (newKeyDescription == null || newKeyDescription.isEmpty()) { return; }
        keyDescription = newKeyDescription;
    }

    @Override
    public void npcs$setKeyCodeDefault(int newKeyCodeDefault) {
        if (newKeyCodeDefault < 0) { newKeyCodeDefault *= -1; }
        if (newKeyCodeDefault > 400) { newKeyCodeDefault = 400; }
        keyCodeDefault = newKeyCodeDefault;
    }

    @Override
    public void npcs$setKeyCategory(String newKeyCategory) {
        if (newKeyCategory == null || newKeyCategory.isEmpty()) { return; }
        keyCategory = newKeyCategory;
    }
}
