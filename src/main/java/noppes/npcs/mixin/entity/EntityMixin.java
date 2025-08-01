package noppes.npcs.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import noppes.npcs.api.mixin.entity.IEntityMixin;
import noppes.npcs.api.wrapper.data.Data;
import noppes.npcs.entity.EntityNPCInterface;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Entity.class, priority = 499)
public class EntityMixin implements IEntityMixin {

    @Shadow
    protected EntityDataManager dataManager;

    @Final
    @Shadow
    protected static DataParameter<Byte> FLAGS;

    @Mutable
    @Shadow(remap = false)
    private CapabilityDispatcher capabilities;

    @Shadow
    public int timeUntilPortal;

    @Shadow
    protected BlockPos lastPortalPos;

    @Shadow
    protected Vec3d lastPortalVec;

    @Shadow
    protected EnumFacing teleportDirection;

    @Unique
    protected final Data npcs$storeddata = new Data();

    @Override
    public EntityDataManager npcs$getDataManager() { return dataManager; }

    @Override
    public DataParameter<Byte> npcs$getFLAGS() { return FLAGS; }

    @Override
    public CapabilityDispatcher npcs$getCapabilities() { return capabilities; }

    @Override
    public void npcs$setCapabilities(CapabilityDispatcher newCapabilities) { capabilities = newCapabilities; }

    @Override
    public BlockPos npcs$getLastPortalPos() { return lastPortalPos; }

    @Override
    public Vec3d npcs$getLastPortalVec() { return lastPortalVec; }

    @Override
    public EnumFacing npcs$getTeleportDirection() { return teleportDirection; }

    @Override
    public void npcs$copyDataFromOld(Entity entity) {
        NBTTagCompound nbttagcompound = entity.writeToNBT(new NBTTagCompound());
        nbttagcompound.removeTag("Dimension");
        ((Entity) (Object) this).readFromNBT(nbttagcompound);
        timeUntilPortal = entity.timeUntilPortal;
        lastPortalPos = ((IEntityMixin) entity).npcs$getLastPortalPos();
        lastPortalVec = ((IEntityMixin) entity).npcs$getLastPortalVec();
        teleportDirection = ((IEntityMixin) entity).npcs$getTeleportDirection();
    }

    @Inject(method = "writeToNBT", at = @At("RETURN"), cancellable = true)
    public void npcs$writeToNBT(CallbackInfoReturnable<NBTTagCompound> cir) {
        NBTTagCompound compound = cir.getReturnValue();
        compound.setTag("CustomStoredData", npcs$storeddata.getNbt().getMCNBT());
        cir.setReturnValue(compound);
    }

    @Inject(method = "readFromNBT", at = @At("RETURN"))
    public void npcs$readFromNBT(NBTTagCompound compound, CallbackInfo ci) {
        npcs$storeddata.setNbt(compound.getCompoundTag("CustomStoredData"));
    }

    @Inject(method = "applyEntityCollision", at = @At("RETURN"), cancellable = true)
    public void npcs$applyEntityCollision(Entity entityIn, CallbackInfo ci) {
        if (entityIn instanceof EntityNPCInterface && ((EntityNPCInterface) entityIn).display.getHitboxState() == 2) {
            Entity parent = (Entity) (Object) this;
            if (!(parent instanceof EntityNPCInterface) || ((EntityNPCInterface) parent).display.getHitboxState() != 2) { ci.cancel(); }
        }
    }

    @Override
    public Data npcs$getStoredData() { return npcs$storeddata; }

}
