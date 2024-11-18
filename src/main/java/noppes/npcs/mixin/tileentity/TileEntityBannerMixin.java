package noppes.npcs.mixin.tileentity;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityBanner;
import noppes.npcs.api.mixin.tileentity.ITileEntityBanner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TileEntityBanner.class)
public class TileEntityBannerMixin implements ITileEntityBanner {

    @Unique
    public int npcs$factionId = -1;

    public int npcs$getFactionId(){
        return npcs$factionId;
    }

    @Override
    public void npcs$setFactionId(int newFactionId) {
        if (newFactionId < -1) { newFactionId = -1; }
        npcs$factionId = newFactionId;
    }

    @Inject(method = "readFromNBT", at = @At("TAIL"))
    public void npcs$readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        if (compound.hasKey("FactionID", 3)) {
            npcs$factionId = compound.getInteger("FactionID");
        }
    }

    @Inject(method = "setItemValues", at = @At("TAIL"))
    public void npcs$setItemValues(ItemStack stack, boolean p_175112_2_, CallbackInfo ci) {
        npcs$factionId = -1;
        NBTTagCompound nbt = stack.getTagCompound();
        if (nbt != null && nbt.hasKey("BlockEntityTag", 10)
                && nbt.getCompoundTag("BlockEntityTag").hasKey("FactionID", 3)) {
            npcs$factionId = nbt.getCompoundTag("BlockEntityTag").getInteger("FactionID");
        }
    }

    @Inject(method = "writeToNBT", at = @At("TAIL"))
    public void npcs$writeToNBT(NBTTagCompound compound, CallbackInfoReturnable<NBTTagCompound> cir) {
        compound.setInteger("FactionID", npcs$factionId);
    }

}
