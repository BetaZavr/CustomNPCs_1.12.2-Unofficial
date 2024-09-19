package noppes.npcs.mixin.api.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = RenderPlayer.class)
public interface RenderPlayerAPIMixin {

    @Accessor(value="smallArms")
    boolean npcs$getSmallArms();

}
