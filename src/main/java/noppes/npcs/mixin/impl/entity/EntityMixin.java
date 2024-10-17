package noppes.npcs.mixin.impl.entity;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import noppes.npcs.mixin.entity.IEntityMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Entity.class)
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

}
