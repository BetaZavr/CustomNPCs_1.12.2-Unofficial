package noppes.npcs.mixin.impl.inv;

import com.google.common.collect.Lists;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCraftResult;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.ForgeHooks;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.INpcRecipe;
import noppes.npcs.controllers.data.Availability;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// methods are mixed, as there are now recipes with more than 1 count ingredient
@Mixin(value = SlotCrafting.class)
public class SlotCraftingMixin {

    @Final
    @Shadow
    private InventoryCrafting craftMatrix;
    @Final
    @Shadow
    private EntityPlayer player;
    @Shadow
    private int amountCrafted;

    @Inject(method = "onTake", at = @At("HEAD"), cancellable = true)
    public void npcs$onTake(EntityPlayer thePlayer, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        cir.cancel();

        InventoryCraftResult inventorycraftresult = (InventoryCraftResult) ((Slot) (Object) this).inventory;
        IRecipe recipe = inventorycraftresult.getRecipeUsed();

        boolean ignoreDamage = false;
        boolean ignoreNBT = false;
        // Availability
        if (recipe instanceof INpcRecipe) {
            if (!((Availability) ((INpcRecipe) recipe).getAvailability()).isAvailable(player)) {
                if (!player.world.isRemote) {
                    player.sendMessage(new TextComponentTranslation("item.craft.not.availability"));
                    player.inventory.setItemStack(ItemStack.EMPTY);
                    player.openContainer.detectAndSendChanges();
                }
                cir.setReturnValue(ItemStack.EMPTY);
                return;
            }
            ignoreDamage = ((INpcRecipe) recipe).getIgnoreDamage();
            ignoreNBT = ((INpcRecipe) recipe).getIgnoreNBT();
        }

        npcs$onCrafting(stack);
        ForgeHooks.setCraftingPlayer(thePlayer);
        NonNullList<ItemStack> nonnulllist = CraftingManager.getRemainingItems(this.craftMatrix, thePlayer.world);
        ForgeHooks.setCraftingPlayer(null);

        for (int i = 0; i < nonnulllist.size(); ++i) {
            ItemStack craftStack = this.craftMatrix.getStackInSlot(i);
            ItemStack extraStack = nonnulllist.get(i);
            int count = 1;
            if (recipe != null) {
                ItemStack[] ingStacks = recipe.getIngredients().get(i).getMatchingStacks();
                for (ItemStack st : ingStacks) { // for variants
                    if (st.isEmpty()) { continue; }
                    if (NoppesUtilPlayer.compareItems(st, craftStack, ignoreDamage, ignoreNBT)) {
                        count = st.getCount();
                        break;
                    }
                }
            }
            if (!craftStack.isEmpty()) {
                this.craftMatrix.decrStackSize(i, count);
                craftStack = this.craftMatrix.getStackInSlot(i);
            }
            if (!extraStack.isEmpty()) {
                if (craftStack.isEmpty()) { this.craftMatrix.setInventorySlotContents(i, extraStack); }
                else if (ItemStack.areItemsEqual(craftStack, extraStack) && ItemStack.areItemStackTagsEqual(craftStack, extraStack)) {
                    extraStack.grow(craftStack.getCount());
                    this.craftMatrix.setInventorySlotContents(i, extraStack);
                }
                else if (!this.player.inventory.addItemStackToInventory(extraStack)) { this.player.dropItem(extraStack, false); }
            }
        }
        thePlayer.openContainer.detectAndSendChanges();
        cir.setReturnValue(stack);
    }

    @Unique
    protected void npcs$onCrafting(ItemStack stack) {
        if (this.amountCrafted > 0) {
            stack.onCrafting(this.player.world, this.player, this.amountCrafted);
            net.minecraftforge.fml.common.FMLCommonHandler.instance().firePlayerCraftingEvent(this.player, stack, craftMatrix);
        }
        this.amountCrafted = 0;
        InventoryCraftResult inventorycraftresult = (InventoryCraftResult) ((Slot) (Object) this).inventory;
        IRecipe irecipe = inventorycraftresult.getRecipeUsed();
        if (irecipe != null && !irecipe.isDynamic()) {
            this.player.unlockRecipes(Lists.newArrayList(irecipe));
            inventorycraftresult.setRecipeUsed(null);
        }
    }

}
