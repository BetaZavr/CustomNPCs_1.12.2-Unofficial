package noppes.npcs.client.layer;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.ArmourersWorkshopClientApi;
import moe.plushie.armourers_workshop.api.common.capability.IEntitySkinCapability;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.client.util.aw.ArmourersWorkshopUtil;
import noppes.npcs.client.util.aw.CustomSkinModelRenderHelper;

@SideOnly(Side.CLIENT)
public class LayerCustomHeldItem<T extends EntityLivingBase>
extends LayerInterface<T> {
	
	public LayerCustomHeldItem(RenderLiving<?> livingEntityRendererIn) {
		super(livingEntityRendererIn);
	}
	
	private void renderHeldItem(EntityLivingBase entity, ItemStack stack, ItemCameraTransforms.TransformType transform, EnumHandSide handSide, float scale) {
		if (stack.isEmpty()) { return; }
		GlStateManager.pushMatrix();
		boolean isLeft = handSide == EnumHandSide.LEFT;
		if (entity.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
		this.model.postRenderArm(scale, handSide);
		boolean drawStack = true;
		if (ArmourersWorkshopClientApi.getSkinRenderHandler() != null) {
			ArmourersWorkshopUtil awu = ArmourersWorkshopUtil.getInstance();
			IEntitySkinCapability skinCapability = ArmourersWorkshopApi.getEntitySkinCapability(this.npc);
            if (skinCapability != null) {
            	ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
        		if (skinDescriptor != null) {
        			double distance = Minecraft.getMinecraft().player.getDistance(this.npc.posX, this.npc.posY, this.npc.posZ);
                    int d = 0;
        			try { d = (int) awu.renderDistanceSkin.get(awu.configHandlerClient); } catch (Exception e) {
        				e.printStackTrace();
        				GlStateManager.popMatrix();
        				return;
        			}
        			if (distance <= d) {
        				CustomSkinModelRenderHelper modelRenderer = CustomSkinModelRenderHelper.getInstance();
        	            IWardrobeCap wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(this.npc);
                        if (skinDescriptor != null) {
                        	try { 
	                			ISkin skin = (ISkin) awu.getSkin.invoke(awu.clientSkinCache, skinDescriptor);
	                            if (skin != null) {
		                			ISkinDye dye = (ISkinDye) awu.skinDyeConstructor.newInstance(wardrobe.getDye());
		                            for (int dyeIndex = 0; dyeIndex < 8; dyeIndex++) {
		                                if (skinDescriptor.getSkinDye().haveDyeInSlot(dyeIndex)) {
		                                    dye.addDye(dyeIndex, skinDescriptor.getSkinDye().getDyeColour(dyeIndex));
		                                }
		                            }
		                            ResourceLocation texture = DefaultPlayerSkin.getDefaultSkinLegacy();
		                			Object renderData = awu.skinRenderDataConstructor.newInstance(scale, dye, awu.extraColours, distance, true, true, false, texture);
		                			GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
		                			GlStateManager.translate((float) (isLeft ? 1.0F : -1.0F) / 16.0F, 0.0F, -0.5F);
		                			if (isLeft) {
		                				String type = skinDescriptor.getIdentifier().getSkinType().getName();
		                				if (type.equalsIgnoreCase("shield")) {
		                					GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
		                					GlStateManager.rotate(180.0F, 1.0F, 0.0F, 0.0F);
		                					GlStateManager.translate(0.0f, 0.0f, -0.125f);
		                				}
		                				else if (type.equalsIgnoreCase("bow")) {
		                					GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
		                					GlStateManager.rotate(220.0F, 1.0F, 0.0F, 0.0F);
		                					GlStateManager.translate(0.0f, 0.1875f, -0.0625f);
		                				}
		                			}
		                			modelRenderer.renderEquipmentPart(skin, renderData, this.npc, (ModelBiped) this.render.getMainModel(), scale, null);
	                            }
                        	} catch (Exception e) {
                        		e.printStackTrace();
                				GlStateManager.popMatrix();
                				return;
                			}
                        }
        	            GlStateManager.disableRescaleNormal();
        				drawStack = false;
        			}
        		}
            }
		}
		if (drawStack) {
			GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
			GlStateManager.rotate(180.0F, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate((float)(isLeft ? -1.0F : 1.0F) / 16.0F, 0.125f, -0.625f);
			Minecraft.getMinecraft().getItemRenderer().renderItemSide(entity, stack, transform, isLeft);
		}
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
			if (this.npc.animation.showParts.get(1)) {
				this.renderHeldItem(this.npc, mainhand, ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND, EnumHandSide.LEFT, scale);
			}
			if (this.npc.animation.showParts.get(2)) {
				this.renderHeldItem(this.npc, offhand, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, EnumHandSide.RIGHT, scale);
			}
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void rotate(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {  }
	
}