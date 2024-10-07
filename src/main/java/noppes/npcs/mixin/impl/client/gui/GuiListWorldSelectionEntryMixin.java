package noppes.npcs.mixin.impl.client.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.AnvilConverterException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.*;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.SoundEvents;
import net.minecraft.world.storage.ISaveFormat;
import net.minecraft.world.storage.WorldSummary;
import net.minecraftforge.fml.client.FMLClientHandler;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.mixin.client.world.storage.IWorldSummaryMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = GuiListWorldSelectionEntry.class)
public class GuiListWorldSelectionEntryMixin {

    @Final
    @Shadow
    private Minecraft client;

    @Final
    @Shadow
    private GuiWorldSelection worldSelScreen;

    @Final
    @Shadow
    private WorldSummary worldSummary;

    /*
     * Before starting a single-player game, you must check
     * the user agreement for the use of possible scripts
     */
    @Inject(method = "loadWorld", at = @At("HEAD"), cancellable = true)
    private void npcs$loadWorld(CallbackInfo ci) {
        try {
            String agreementName = ((IWorldSummaryMixin) worldSummary).npcs$getAgreementName();
            if (ScriptController.Instance.notAgreement(agreementName)) {
                ci.cancel();
                client.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                if (client.getSaveLoader().canLoadWorld(this.worldSummary.getFileName())) {
                    client.displayGuiScreen(new GuiYesNo((result, id) -> {
                        if (result) {
                            ScriptController.Instance.setAgreement(agreementName, true);
                            FMLClientHandler.instance().tryLoadExistingWorld(worldSelScreen, worldSummary);
                        }
                        else { client.displayGuiScreen(worldSelScreen); }
                    },
                            I18n.format("system.check.scripts.agree"),
                            I18n.format("system.check.scripts.title"),
                            I18n.format("gui.agree"),
                            I18n.format("gui.cancel"),
                            1));
                }
            }
        }
        catch (Exception e) {
            LogWriter.error("Error while checking user agreement: ");
        }
    }

    /*
     * Check all worlds.
     * If it was deleted, then the agreement is cancelled
     */
    @Inject(method = "deleteWorld", at = @At("TAIL"))
    private void npcs$deleteWorld(CallbackInfo ci) {
        ScriptController.Instance.checkAgreements(npcs$getCheckList());
    }

    @Unique
    private static List<String> npcs$getCheckList() {
        ISaveFormat isaveformat = Minecraft.getMinecraft().getSaveLoader();
        List<WorldSummary> list;
        try { list = isaveformat.getSaveList(); }
        catch (AnvilConverterException anvilconverterexception) { return null; }

        List<String> checkList = Lists.newArrayList();
        for (WorldSummary worldsummary : list) { checkList.add(((IWorldSummaryMixin) worldsummary).npcs$getAgreementName()); }
        return checkList;
    }

}
