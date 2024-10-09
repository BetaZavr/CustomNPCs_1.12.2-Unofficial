package noppes.npcs.mixin.impl.init;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import noppes.npcs.CustomRegisters;
import noppes.npcs.items.CustomItemLingeringPotion;
import noppes.npcs.items.CustomItemPotion;
import noppes.npcs.items.CustomItemSplashPotion;
import noppes.npcs.items.CustomItemTippedArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Items.class)
public class ItemsMixin {

    @Inject(method = "getRegisteredItem", at = @At("HEAD"), cancellable = true)
    private static void npcs$getRegisteredItem(String name, CallbackInfoReturnable<Item> cir) {
        switch (name) {
            case "tipped_arrow":
                if (CustomRegisters.itemTippedArrow == null) {
                    CustomRegisters.itemTippedArrow = new CustomItemTippedArrow();
                }
                cir.setReturnValue(CustomRegisters.itemTippedArrow);
                cir.cancel();
                break;
            case "potion":
                if (CustomRegisters.itemPotion == null) {
                    CustomRegisters.itemPotion = new CustomItemPotion();
                }
                cir.setReturnValue(CustomRegisters.itemPotion);
                cir.cancel();
                break;
            case "splash_potion":
                if (CustomRegisters.itemSplashPotion == null) {
                    CustomRegisters.itemSplashPotion = new CustomItemSplashPotion();
                }
                cir.setReturnValue(CustomRegisters.itemSplashPotion);
                cir.cancel();
                break;
            case "lingering_potion":
                if (CustomRegisters.itemLingeringPotion == null) {
                    CustomRegisters.itemLingeringPotion = new CustomItemLingeringPotion();
                }
                cir.setReturnValue(CustomRegisters.itemLingeringPotion);
                cir.cancel();
                break;
        }
    }

}
