package noppes.npcs.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.resources.I18n;
import net.minecraftforge.fml.client.FMLClientHandler;
import noppes.npcs.controllers.ScriptController;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiMultiplayer.class, priority = 499)
public class GuiMultiplayerMixin {

    /*
     * Before starting a connect to game, you must check
     * the user agreement for the use of possible scripts
     */
    @Inject(method = "connectToServer", at = @At("HEAD"), cancellable = true)
    private void npcs$connectToServer(ServerData server, CallbackInfo ci) {
        String agreementName = server.serverName + ";" + server.gameVersion + ";" + server.serverIP + ";" + server.isOnLAN();
        if (ScriptController.Instance.notAgreement(agreementName)) {
            ci.cancel();
            Minecraft client = Minecraft.getMinecraft();
            GuiScreen parent = client.currentScreen;
            client.displayGuiScreen(new GuiYesNo((result, id) -> {
                if (result) {
                    ScriptController.Instance.setAgreement(agreementName, true);
                    FMLClientHandler.instance().connectToServer(parent, server);
                }
                else { client.displayGuiScreen(parent); }
            },
                    I18n.format("system.check.scripts.agree"),
                    I18n.format("system.check.scripts.title"),
                    I18n.format("gui.agree"),
                    I18n.format("gui.cancel"),
                    1));
        }
    }

}
