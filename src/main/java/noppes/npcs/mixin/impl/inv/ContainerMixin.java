package noppes.npcs.mixin.impl.inv;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.*;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.network.play.server.SPacketSetSlot;
import net.minecraft.world.World;
import net.minecraftforge.common.crafting.IRecipeContainer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.util.CustomNPCsScheduler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/*
 * Since custom recipes can contain more than 1 count ingredient,
 * it is necessary to check the recipes whenever the inventory changes.
 */
@Mixin(value = Container.class)
public class ContainerMixin {

    @Shadow
    public int windowId;

    @Inject(method = "slotClick", at = @At("TAIL"))
    public void npcs$slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player, CallbackInfoReturnable<ItemStack> cir) {
        if (slotId < 0) { return; }
        InventoryCrafting craftMatrix = null;
        if (((Object) this) instanceof ContainerWorkbench) { craftMatrix = ((ContainerWorkbench) (Object) this).craftMatrix; }
        else if (((Object) this) instanceof ContainerPlayer) { craftMatrix = ((ContainerPlayer) (Object) this).craftMatrix; }
        else if (this instanceof IRecipeContainer) { craftMatrix = ((IRecipeContainer) this).getCraftMatrix(); }
        if (craftMatrix != null) { ((Container) (Object) this).onCraftMatrixChanged(craftMatrix); }
    }

    @Inject(method = "slotChangedCraftingGrid", at = @At("HEAD"), cancellable = true)
    protected void npcs$slotChangedCraftingGrid(World world, EntityPlayer player, InventoryCrafting invCrafting, InventoryCraftResult invCraftResult, CallbackInfo ci) {
        ci.cancel();
        if (world.isRemote) { return; }
        // Changed system of filling the crafting inventory is a little behind (1 tick)
        CustomNPCsScheduler.runTack(() -> {
            EntityPlayerMP playerMP = (EntityPlayerMP) player;
            IRecipe irecipe = CraftingManager.findMatchingRecipe(invCrafting, world);
            if (irecipe instanceof INpcRecipe && !((Availability) ((INpcRecipe) irecipe).getAvailability()).isAvailable(player)) {
                invCraftResult.setInventorySlotContents(0, ItemStack.EMPTY);
                playerMP.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, ItemStack.EMPTY));
                return;
            }
            ItemStack itemstack = ItemStack.EMPTY;
            if (irecipe != null && (irecipe.isDynamic() || !world.getGameRules().getBoolean("doLimitedCrafting") || playerMP.getRecipeBook().isUnlocked(irecipe)))  {
                invCraftResult.setRecipeUsed(irecipe);
                itemstack = irecipe.getCraftingResult(invCrafting);
            }
            invCraftResult.setInventorySlotContents(0, itemstack);
            playerMP.connection.sendPacket(new SPacketSetSlot(this.windowId, 0, itemstack));
        }, 50);
    }

}
