package noppes.npcs.mixin.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.api.mixin.inv.ISlotMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = Slot.class)
public class SlotMixin implements ISlotMixin {

    @Mutable
    @Final
    @Shadow
    private int slotIndex;

    @Final
    @Shadow
    public IInventory inventory;

    @Override
    public void npcs$setSlotIndex(int slotID) {
        if (slotID < 0 || slotID >= inventory.getSizeInventory()) { return; }
        slotIndex = slotID;
    }

}
