package noppes.npcs.mixin.impl.client.resources;

import net.minecraft.client.resources.Locale;
import noppes.npcs.mixin.client.resources.ILocaleMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = Locale.class)
public class LocaleMixin implements ILocaleMixin {

    @Shadow
    Map<String, String> properties;

    @Override
    public Map<String, String> npcs$getProperties() { return properties; }

}
