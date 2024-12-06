package noppes.npcs.mixin.client.renderer;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(value = RenderItem.class)
public class RenderItemMixin {

    @Final
    @Shadow
    private TextureManager textureManager;

    @Unique
    private static final ResourceLocation NPCS$LOCATION = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

    /**
     * @author BetaZavr
     * @reason Shows the rarity of the item in the inventory slot
     */
    @Inject(method = "renderItemAndEffectIntoGUI(Lnet/minecraft/entity/EntityLivingBase;Lnet/minecraft/item/ItemStack;II)V", at = @At("HEAD"))
    public void npcs$renderItemAndEffectIntoGUI(@Nullable EntityLivingBase entity, final ItemStack stack, int xPos, int yPos, CallbackInfo ci) {
        if (!CustomNpcs.ShowRarityItem || stack.isEmpty()) { return; }
        int index = stack.getItem().getForgeRarity(stack).getColor().getColorIndex();
        if (index > 14) { return; }
        float[] color = Util.instance.getColorF(index);
        GlStateManager.pushMatrix();
        GlStateManager.translate(xPos, yPos, 1.0F);
        GlStateManager.scale(0.5f, 0.5f, 1.0f);
        GlStateManager.color(color[0], color[1], color[2], color[3]);
        textureManager.bindTexture(NPCS$LOCATION);
        npcs$drawTexturedModalRect();
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
