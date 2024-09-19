package noppes.npcs.mixin.api.client.settings;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.client.settings.KeyModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = KeyBinding.class, remap = false)
public interface KeyBindingForgeAPIMixin {

    @Mutable
    @Accessor(value="keyModifierDefault")
    void npcs$setModifier(KeyModifier newKeyModifier);

}
