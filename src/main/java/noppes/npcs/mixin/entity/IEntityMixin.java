package noppes.npcs.mixin.entity;

import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;

public interface IEntityMixin {

    EntityDataManager npcs$getDataManager();

    DataParameter<Byte> npcs$getFLAGS();

    CapabilityDispatcher npcs$getCapabilities();

    void npcs$setCapabilities(CapabilityDispatcher newCapabilities);

}
