package noppes.npcs.mixin.impl.client.settings;

import net.minecraft.client.settings.KeyBinding;
import noppes.npcs.mixin.client.settings.IKeyBindingMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = KeyBinding.class)
public class KeyBindingMixin implements IKeyBindingMixin {

    @Mutable
    @Final
    @Shadow
    private String keyDescription;

    @Mutable
    @Final
    @Shadow
    private int keyCodeDefault;

    @Mutable
    @Final
    @Shadow
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
