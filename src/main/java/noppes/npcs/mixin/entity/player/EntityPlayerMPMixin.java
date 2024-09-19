package noppes.npcs.mixin.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.mixin.api.entity.player.EntityPlayerMPAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = EntityPlayerMP.class)
public class EntityPlayerMPMixin implements EntityPlayerMPAPIMixin {

    @Shadow(aliases = "language")
    private String language;

    @Override
    public String npcs$getLanguage() { return language; }

}
