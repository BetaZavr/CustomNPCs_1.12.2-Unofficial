package noppes.npcs.mixin.impl.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.crafting.IRecipeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Since custom recipes can contain more than 1 count ingredient,
 * it is necessary to check the recipes whenever the inventory changes.
 */
@Mixin(value = Container.class)
public class ContainerMixin {

    @Inject(method = "slotClick", at = @At("TAIL"))
    public void npcs$slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (slotId < 0) { return; }
        InventoryCrafting craftMatrix = null;
        if (((Object) this) instanceof ContainerWorkbench) { craftMatrix = ((ContainerWorkbench) (Object) this).craftMatrix; }
        else if (((Object) this) instanceof ContainerPlayer) { craftMatrix = ((ContainerPlayer) (Object) this).craftMatrix; }
        else if (this instanceof IRecipeContainer) { craftMatrix = ((IRecipeContainer) this).getCraftMatrix(); }
        if (craftMatrix != null) { ((Container) (Object) this).onCraftMatrixChanged(craftMatrix); }
    }

}
