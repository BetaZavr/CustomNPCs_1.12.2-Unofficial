package noppes.npcs.client.layer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHandSide;
import noppes.npcs.CustomRegisters;
import noppes.npcs.client.model.ModelNpcAlt;
import noppes.npcs.client.model.part.LayerModel;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

public class LayerCustomModels<T extends EntityLivingBase> extends LayerInterface<T> {

    public LayerCustomModels(RenderLiving<?> render) { super(render); }

    @Override
    public void doRenderLayer(@Nonnull EntityLivingBase entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
        npc = (EntityCustomNpc) entity;
        if (npc.isInvisibleToPlayer(Minecraft.getMinecraft().player)) { return; }
        playerdata = npc.modelData;
        if (!(render.getMainModel() instanceof ModelBiped)) { return; }
        model = (ModelBiped) render.getMainModel();

        Map<EnumParts, List<LayerModel>> map = playerdata.getRenderLayers();
        Minecraft mc = Minecraft.getMinecraft();
        boolean isInvisible = false;
        if (npc.display.getVisible() == 1) { isInvisible = npc.display.getAvailability().isAvailable(Minecraft.getMinecraft().player); }
        else if (npc.display.getVisible() == 2) { isInvisible = Minecraft.getMinecraft().player.getHeldItemMainhand().getItem() != CustomRegisters.wand; }
        int color = npc.display.getTint();
        float red = (color >> 16 & 0xFF) / 255.0f;
        float green = (color >> 8 & 0xFF) / 255.0f;
        float blue = (color & 0xFF) / 255.0f;
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        if (isInvisible) {
            GlStateManager.color(1.0f, 1.0f, 1.0f, 0.15f);
            GlStateManager.blendFunc(770, 771);
            GlStateManager.alphaFunc(516, 0.003921569f);
        }
        if (map.containsKey(EnumParts.HEAD)) { renderPart(model.bipedHead, map.get(EnumParts.HEAD), mc, red, green, blue); }
        if (map.containsKey(EnumParts.BODY)) { renderPart(model.bipedBody, map.get(EnumParts.BODY), mc, red, green, blue); }
        if (map.containsKey(EnumParts.ARM_RIGHT)) { renderPart(model.bipedRightArm, map.get(EnumParts.ARM_RIGHT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.ARM_LEFT)) { renderPart(model.bipedLeftArm, map.get(EnumParts.ARM_LEFT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.LEG_RIGHT)) { renderPart(model.bipedRightLeg, map.get(EnumParts.LEG_RIGHT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.LEG_LEFT)) { renderPart(model.bipedLeftLeg, map.get(EnumParts.LEG_LEFT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.WRIST_RIGHT)) { renderPartNext(true, EnumHandSide.RIGHT, map.get(EnumParts.WRIST_RIGHT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.WRIST_LEFT)) { renderPartNext(true, EnumHandSide.LEFT, map.get(EnumParts.WRIST_LEFT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.FOOT_RIGHT)) { renderPartNext(false, EnumHandSide.RIGHT, map.get(EnumParts.FOOT_RIGHT), mc, red, green, blue); }
        if (map.containsKey(EnumParts.FOOT_LEFT)) { renderPartNext(false, EnumHandSide.LEFT, map.get(EnumParts.FOOT_LEFT), mc, red, green, blue); }
        if (isInvisible) {
            GlStateManager.disableBlend();
            GlStateManager.alphaFunc(516, 0.1f);
            GlStateManager.depthMask(true);
        }
        GlStateManager.popMatrix();
    }

    @Override
    public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) { }

    @Override
    public void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) { }

    public void preRender(float red, float green, float blue) {
        if (npc.hurtTime > 0 || npc.deathTime > 0) { GlStateManager.color(1.0f, 0.0f, 0.0f, 0.3f); }
        if (npc.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
        if (npc.hurtTime > 0 || npc.deathTime > 0) { return; }
        GlStateManager.color(red, green, blue, 1.0f);
    }

    private void renderPart(ModelRenderer modelRenderer, List<LayerModel> layers, Minecraft mc, float red, float green, float blue) {
        GlStateManager.pushMatrix();
        modelRenderer.postRender(0.0625f);
        for (LayerModel lm : layers) {
            GlStateManager.pushMatrix();
            // rotate
            GlStateManager.translate(lm.offset[0], lm.offset[1], lm.offset[2]);
            if (lm.rotation[2] != 0.0f) { GlStateManager.rotate(lm.rotation[2], 0.0f, 0.0f, 1.0f); }
            if (lm.rotation[1] != 0.0f) { GlStateManager.rotate(lm.rotation[1], 0.0f, 1.0f, 0.0f); }
            GlStateManager.rotate(lm.rotation[0] + 180.0f, 1.0f, 0.0f, 0.0f);
            if (lm.scale[0] != 0.0f || lm.scale[1] != 0.0f || lm.scale[2] != 0.0f) {
                GlStateManager.scale(lm.scale[0], lm.scale[1], lm.scale[2]);
            }
            // render
            preRender(red, green, blue);
            GlStateManager.enableRescaleNormal();
            if (lm.getOBJ() != null) {
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                GlStateManager.callList(ModelBuffer.getDisplayList(lm.getOBJ(), null, null));
            }
            else {
                if (Block.getBlockFromItem(lm.getStack().getItem()) == Blocks.AIR) {
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                }
                mc.getRenderItem().renderItem(lm.getStack(), ItemCameraTransforms.TransformType.FIXED);
            }
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

    private void renderPartNext(boolean isArm, EnumHandSide handSide, List<LayerModel> layers, Minecraft mc, float red, float green, float blue) {
        GlStateManager.pushMatrix();
        if (isArm) {
            model.postRenderArm(0.0625f, handSide);
        }
        else {
            if (model instanceof ModelNpcAlt) { ((ModelNpcAlt) model).postRenderLeg(0.0625f, handSide); }
            else { (handSide == EnumHandSide.LEFT ? model.bipedLeftLeg : model.bipedRightLeg).postRender(0.0625f); }
        }
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate((handSide == EnumHandSide.LEFT ? -1.0F : 1.0F) / 16.0F, 0.125f, -0.625f);

        for (LayerModel lm : layers) {
            GlStateManager.pushMatrix();
            // rotate
            GlStateManager.translate(lm.offset[0], lm.offset[1], lm.offset[2]);
            if (lm.rotation[2] != 0.0f) { GlStateManager.rotate(lm.rotation[2], 0.0f, 0.0f, 1.0f); }
            if (lm.rotation[1] != 0.0f) { GlStateManager.rotate(lm.rotation[1], 0.0f, 1.0f, 0.0f); }
            GlStateManager.rotate(lm.rotation[0] + 90.0f, 1.0f, 0.0f, 0.0f);

            if (lm.scale[0] != 0.0f || lm.scale[1] != 0.0f || lm.scale[2] != 0.0f) {
                GlStateManager.scale(lm.scale[0], lm.scale[1], lm.scale[2]);
            }
            // render
            preRender(red, green, blue);
            GlStateManager.enableRescaleNormal();
            if (lm.getOBJ() != null) {
                GlStateManager.scale(0.5f, 0.5f, 0.5f);
                GlStateManager.callList(ModelBuffer.getDisplayList(lm.getOBJ(), null, null));
            }
            else {
                if (Block.getBlockFromItem(lm.getStack().getItem()) == Blocks.AIR) {
                    GlStateManager.scale(0.5f, 0.5f, 0.5f);
                }
                mc.getRenderItem().renderItem(lm.getStack(), ItemCameraTransforms.TransformType.FIXED);
            }
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
        GlStateManager.popMatrix();
    }

}
