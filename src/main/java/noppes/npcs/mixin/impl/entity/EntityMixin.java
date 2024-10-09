package noppes.npcs.mixin.impl.entity;

import net.minecraft.entity.Entity;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.EntityDataManager;
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

    @Override
    public EntityDataManager npcs$getDataManager() { return dataManager; }

    @Override
    public DataParameter<Byte> npcs$getFLAGS() { return FLAGS; }

    @Override
    public CapabilityDispatcher npcs$getCapabilities() { return capabilities; }

    @Override
    public void npcs$setCapabilities(CapabilityDispatcher newCapabilities) { capabilities = newCapabilities; }

}
