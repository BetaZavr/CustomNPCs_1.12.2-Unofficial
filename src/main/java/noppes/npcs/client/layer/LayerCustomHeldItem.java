package noppes.npcs.client.layer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class LayerCustomHeldItem<T extends EntityLivingBase>
extends LayerInterface<T>
{

    public LayerCustomHeldItem(RenderLiving<?> livingEntityRendererIn) {
    	super(livingEntityRendererIn);
    }
    
    private void renderHeldItem(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transform, EnumHandSide handSide) {
    	if (stack.isEmpty()) { return; }
        GlStateManager.pushMatrix();
        boolean isLeft = handSide == EnumHandSide.LEFT;
        if (entity.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
        this.model.postRenderArm(0.0625F, handSide);
        GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.translate((float)(isLeft ? -1 : 1) / 16.0F, 0.125F, -0.625F);
		
        Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack, transform, isLeft);
        GlStateManager.popMatrix();
    }

    public boolean shouldCombineTextures() { return false; }

	@Override
	public void render(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		boolean flag = this.npc.getPrimaryHand() == EnumHandSide.RIGHT;
        ItemStack mainhand = flag ? this.npc.getHeldItemOffhand() : this.npc.getHeldItemMainhand();
        ItemStack offhand = flag ? this.npc.getHeldItemMainhand() : this.npc.getHeldItemOffhand();
        if (!mainhand.isEmpty() || !offhand.isEmpty()) {
            GlStateManager.pushMatrix();
            if (this.npc.isChild()) {
                GlStateManager.translate(0.0F, 0.75F, 0.0F);
                GlStateManager.scale(0.5F, 0.5F, 0.5F);
            }
            this.renderHeldItem(this.npc, offhand, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT);
            this.renderHeldItem(this.npc, mainhand, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT);
            GlStateManager.popMatrix();
        }
	}

	@Override
	public void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {  }
	
}