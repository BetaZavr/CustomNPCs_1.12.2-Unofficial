package noppes.npcs.mixin.impl.client.world.storage;

import net.minecraft.world.storage.WorldInfo;
import net.minecraft.world.storage.WorldSummary;
import noppes.npcs.mixin.client.world.storage.IWorldSummaryMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(WorldSummary.class)
public class WorldSummaryMixin implements IWorldSummaryMixin {

    @Unique
    public String npcs$agreementName = "";

    @Override
    public String npcs$getAgreementName() {
        return npcs$agreementName;
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    public void init(WorldInfo info, String fileNameIn, String displayNameIn, long sizeOnDiskIn, boolean requiresConversionIn, CallbackInfo ci) {
        this.npcs$agreementName = info.getWorldName() + ";" + info.getSeed();
    }
}
