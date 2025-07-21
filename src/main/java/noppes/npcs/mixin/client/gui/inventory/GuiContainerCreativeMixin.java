package noppes.npcs.mixin.client.gui.inventory;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientGuiEventHandler;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(value = GuiContainerCreative.class, priority = 499)
public class GuiContainerCreativeMixin {

    @Shadow
    private static int selectedTabIndex;

    @Unique
    private static final ResourceLocation CREATIVE_TABS = new ResourceLocation("textures/gui/container/creative_inventory/tabs.png");

    @Inject(method = "initGui", at = @At("TAIL"))
    public void npcs$initGui(CallbackInfo ci) {
        if (CustomNpcs.InventoryGuiEnabled) {
            GuiContainerCreative parent = (GuiContainerCreative) (Object) this;
            int x = parent.getGuiLeft() - 30;
            int y = parent.getGuiTop() + 4;
            parent.buttonList.add(new GuiNpcButton(150, x, y, 32, 28, 0, 128, CREATIVE_TABS));
            parent.buttonList.add(new GuiNpcButton(151, x, y + 28, 32, 28, 0, 128, CREATIVE_TABS));
        }
    }

    @Inject(method = "drawScreen", at = @At("TAIL"))
    public void npcs$drawScreenPost(int mouseX, int mouseY, float partialTicks, CallbackInfo ci) {
        GuiContainerCreative parent = (GuiContainerCreative) (Object) this;
        if (CustomNpcs.InventoryGuiEnabled) {
            int x = parent.getGuiLeft() - 30;
            int y = parent.getGuiTop() + 4;

            RenderHelper.enableGUIStandardItemLighting();
            GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);

            GlStateManager.pushMatrix();
            parent.mc.getTextureManager().bindTexture(CREATIVE_TABS);
            GlStateManager.translate(x, y + 28, 0.0f);
            GlStateManager.rotate(-90.0f, 0.0f, 0.0f, 1.0f);
            int mx = mouseX - x;
            int my = mouseY - y;
            if (mx > 0 && mx <= 32 && my > 0 && my <= 28) { parent.drawTexturedModalRect(0, 2, 28, 32, 28, 32); }
            else { parent.drawTexturedModalRect(0, 0, 28, 0, 28, 30); }
            GlStateManager.translate(-28.0f, 0.0f, 0.0f);
            my -= 28;
            if (mx > 0 && mx <= 32 && my > 0 && my <= 28) { parent.drawTexturedModalRect(0, 2, 28, 32, 28, 32); }
            else { parent.drawTexturedModalRect(0, 0, 28, 0, 28, 30); }
            GlStateManager.popMatrix();

            GlStateManager.pushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            String pos = String.valueOf(31L - (System.currentTimeMillis() / 100L) % 32L);
            if (pos.length() < 2) { pos = "0" + pos; }
            parent.mc.getTextureManager().bindTexture(new ResourceLocation("textures/items/compass_" + pos + ".png"));
            GlStateManager.translate(x + 10, y + 6, 0.0f);
            float s = 16.0f / 256.0f;
            GlStateManager.scale(s, s, s);
            parent.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.translate(0.0f, 28.0f / s, 0.0f);
            parent.mc.getTextureManager().bindTexture(new ResourceLocation("textures/items/book_normal.png"));
            parent.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }
        if (CustomNpcs.ShowMoney && selectedTabIndex == 11) {
            String text = Util.instance
                    .getTextReducedNumber(CustomNpcs.proxy.getPlayerData(parent.mc.player).game.getMoney(), true, true, false)
                    + CustomNpcs.displayCurrencies;
            GlStateManager.pushMatrix();
            GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
            int x = parent.getGuiLeft() + 129;
            int y = parent.getGuiTop() + 32;
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

    @Inject(method = "actionPerformed", at = @At("TAIL"))
    protected void npcs$actionPerformed(GuiButton button, CallbackInfo ci) {
        if (CustomNpcs.InventoryGuiEnabled) {
            GuiContainerCreative parent = (GuiContainerCreative) (Object) this;
            if (button.id == 150) { CustomNpcs.proxy.openGui(2, 0, 0, EnumGuiType.QuestLog, parent.mc.player); }
            else if (button.id == 151) { CustomNpcs.proxy.openGui(0, 0, 0, EnumGuiType.QuestLog, parent.mc.player); }
        }
    }

}
