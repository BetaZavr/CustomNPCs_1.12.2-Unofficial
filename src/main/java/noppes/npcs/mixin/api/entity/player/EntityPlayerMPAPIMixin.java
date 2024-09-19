package noppes.npcs.mixin.api.entity.player;

import net.minecraft.entity.player.EntityPlayerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityPlayerMP.class)
public interface EntityPlayerMPAPIMixin {

    @Accessor(value="language")
    String npcs$getLanguage();

}
