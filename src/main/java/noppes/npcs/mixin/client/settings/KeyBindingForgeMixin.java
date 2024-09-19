package noppes.npcs.mixin.client.settings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import noppes.npcs.mixin.api.client.settings.KeyBindingForgeAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = KeyBinding.class, remap = false)
public class KeyBindingForgeMixin implements KeyBindingForgeAPIMixin {

    @Shadow(aliases = "keyModifier")
    private KeyModifier keyModifier;

    @Override
    public void npcs$setModifier(KeyModifier newKeyModifier) {
        if (newKeyModifier == null) { return; }
        keyModifier = newKeyModifier;
    }

}
