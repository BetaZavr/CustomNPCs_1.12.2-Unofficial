package noppes.npcs.mixin.client.renderer.entity;

import net.minecraft.client.renderer.entity.RenderPlayer;
import noppes.npcs.mixin.api.client.renderer.entity.RenderPlayerAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = RenderPlayer.class)
public class RenderPlayerMixin implements RenderPlayerAPIMixin {

    @Final
    @Shadow(aliases = "smallArms")
    private boolean smallArms;

    @Override
    public boolean npcs$getSmallArms() { return smallArms; }

}
