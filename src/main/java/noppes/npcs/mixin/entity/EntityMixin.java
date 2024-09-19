package noppes.npcs.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import noppes.npcs.mixin.api.entity.EntityAPIMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Entity.class)
public class EntityMixin implements EntityAPIMixin {

    @Shadow(aliases = "dataManager")
    protected EntityDataManager dataManager;

    @Final
    @Shadow(aliases = "FLAGS")
    protected DataParameter<Byte> FLAGS;

    @Shadow(aliases = "capabilities", remap = false)
    @Mutable
    private CapabilityDispatcher capabilities;

    @Override
    public EntityDataManager npcs$getDataManager() { return dataManager; }

    @Override
    public DataParameter<Byte> npcs$getFLAGS() { return FLAGS; }

    @Override
    public CapabilityDispatcher npcs$getCapabilities() { return capabilities; }

    @Override
    public void npcs$setCapabilities(CapabilityDispatcher newCapabilities) { capabilities = newCapabilities; }

}
