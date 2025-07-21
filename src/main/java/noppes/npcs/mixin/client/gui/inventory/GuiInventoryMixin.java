package noppes.npcs.mixin.client.gui.inventory;

import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuiInventory.class, priority = 499)
public class GuiInventoryMixin {


    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void npcs$drawScreenPost(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        if (CustomNpcs.ShowMoney) {
            GuiInventory parent = (GuiInventory) (Object) this;
            String text = Util.instance
                    .getTextReducedNumber(CustomNpcs.proxy.getPlayerData(parent.mc.player).game.getMoney(), true, true, false)
                    + CustomNpcs.displayCurrencies;
            GlStateManager.pushMatrix();
            GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
            int x = parent.getGuiLeft() + 122;
            int y = parent.getGuiTop() + 61;
            GlStateManager.translate(x, y, 0.0f);
            parent.mc.getTextureManager().bindTexture(ClientGuiEventHandler.COIN_NPC);
            float s = 16.0f / 250.f;
            GlStateManager.scale(s, s, s);
            GlStateManager.enableBlend();
            GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
            parent.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            parent.mc.fontRenderer.drawString(text, x + 15, y + 8.0f / 2.0f, CustomNpcs.LableColor.getRGB(), false);
            GlStateManager.popMatrix();
            if (mouseX > x && mouseY > y && mouseX < x + 50 && mouseY < y + 12) {
                List<String> hoverText = new ArrayList<>();
                hoverText.add(new TextComponentTranslation("inventory.hover.currency").getFormattedText());
                hoverText.add("" + CustomNpcs.proxy.getPlayerData(parent.mc.player).game.getMoney());
                parent.drawHoveringText(hoverText, mouseX, mouseY);
            }
        }
    }

}
