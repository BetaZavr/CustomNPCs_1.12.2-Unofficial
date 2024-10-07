package noppes.npcs.mixin.impl.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.WorldSettings;
import noppes.npcs.controllers.ScriptController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = Minecraft.class)
public class MinecraftMixin {

    /*
     * When a user starts a single player game,
     * it means that he has either already given his consent,
     * or he is creating a new world and consent is not needed
     */
    @Inject(method = "launchIntegratedServer", at = @At("TAIL"))
    public void npcs$launchIntegratedServer(String folderName, String worldName, WorldSettings worldSettingsIn, CallbackInfo ci) {
        ScriptController.Instance.setAgreement(folderName + ";" + worldSettingsIn.getSeed(), true);
    }

}
