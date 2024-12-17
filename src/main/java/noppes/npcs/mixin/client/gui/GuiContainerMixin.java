package noppes.npcs.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class)
public class GuiContainerMixin {

    @Unique
    private static final ResourceLocation NPCS$LOCATION = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

    /**
     * @author BetaZavr
     * @reason Shows the rarity of the item in the inventory slot
     */
    @Inject(method = "drawSlot", at = @At("HEAD"))
    public void npcs$drawSlot(Slot slotIn, CallbackInfo ci) {
        if (!CustomNpcs.ShowRarityItem || slotIn.getStack().isEmpty()) { return; }
        int index = slotIn.getStack().getItem().getForgeRarity(slotIn.getStack()).getColor().getColorIndex();
        if (index > 14) { return; }
        float[] color = Util.instance.getColorF(index);
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.translate(slotIn.xPos, slotIn.yPos, 1.0F);
        GlStateManager.scale(0.5f, 0.5f, 1.0f);
        GlStateManager.color(color[0], color[1], color[2], color[3]);
        Minecraft.getMinecraft().getTextureManager().bindTexture(NPCS$LOCATION);
        npcs$drawTexturedModalRect();
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Unique
    public void npcs$drawTexturedModalRect() {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(0.0d, 32.0d, 0.0d).tex(0.734375f, 0.125f).endVertex(); // [188, 32]
        bufferbuilder.pos(32.0d, 32.0d, 0.0d).tex(0.859375f, 0.125f).endVertex(); // [188 + 32, 32]
        bufferbuilder.pos(32.0d, 0.0d, 0.0d).tex(0.859375f, 0.0f).endVertex(); // [188 + 32, 0]
        bufferbuilder.pos(0.0d, 0.0d, 0.0d).tex(0.734375f, 0.0f).endVertex(); // [188, 0]
        tessellator.draw();
    }

}
