package noppes.npcs.api.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

public interface IEntityMixin {

    EntityDataManager npcs$getDataManager();

    DataParameter<Byte> npcs$getFLAGS();

    CapabilityDispatcher npcs$getCapabilities();

    BlockPos npcs$getLastPortalPos();

    Vec3d npcs$getLastPortalVec();

    EnumFacing npcs$getTeleportDirection();

    void npcs$setCapabilities(CapabilityDispatcher newCapabilities);

    void npcs$copyDataFromOld(Entity entity);
}
