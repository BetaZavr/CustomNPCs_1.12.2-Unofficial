package noppes.npcs.api.mixin.client.settings;

import net.minecraftforge.client.settings.KeyModifier;

public interface IKeyBindingForgeMixin {

    void npcs$setModifier(KeyModifier newKeyModifier);

}
