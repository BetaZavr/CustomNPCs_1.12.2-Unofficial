package noppes.npcs.mixin.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMPMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityPlayerMP.class)
public class EntityPlayerMPMixin implements IEntityPlayerMPMixin {

    @Shadow
    private String language;

    @Override
    public String npcs$getLanguage() { return language; }

}
