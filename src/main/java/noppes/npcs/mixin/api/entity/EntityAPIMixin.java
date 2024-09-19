package noppes.npcs.mixin.api.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = Entity.class)
public interface EntityAPIMixin {

    @Accessor(value="dataManager")
    EntityDataManager npcs$getDataManager();

    @Accessor(value="FLAGS")
    DataParameter<Byte> npcs$getFLAGS();

    @Accessor(value="capabilities", remap = false)
    CapabilityDispatcher npcs$getCapabilities();

    @Mutable
    @Accessor(value="capabilities", remap = false)
    void npcs$setCapabilities(CapabilityDispatcher newCapabilities);
}
