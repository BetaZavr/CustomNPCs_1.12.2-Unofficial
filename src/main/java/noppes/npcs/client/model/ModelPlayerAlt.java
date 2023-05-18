package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.gui.animation.GuiNpcAnimation;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

public class ModelPlayerAlt
extends ModelPlayer {
	
	private ModelRenderer body;
	private ModelRenderer head;
	private Map<EnumParts, List<ModelScaleRenderer>> map;

	public ModelPlayerAlt(float scale, boolean arms) {
		super(scale, arms);
		this.map = new HashMap<EnumParts, List<ModelScaleRenderer>>();
		(this.head = new ModelScaleRenderer((ModelBase) this, 24, 0, EnumParts.HEAD)).addBox(-3.0f, -6.0f, -1.0f, 6, 6,
				1, scale);
		(this.body = new ModelScaleRenderer((ModelBase) this, 0, 0, EnumParts.BODY)).setTextureSize(64, 32);
		this.body.addBox(-5.0f, 0.0f, -1.0f, 10, 16, 1, scale);
		ObfuscationHelper.setValue(ModelPlayer.class, this, this.head, 6);
		ObfuscationHelper.setValue(ModelPlayer.class, this, this.body, 5);
		this.bipedLeftArm = this.createScale(this.bipedLeftArm, EnumParts.ARM_LEFT);
		this.bipedLeftArmwear = this.createScale(this.bipedLeftArmwear, EnumParts.ARM_LEFT);
		this.bipedRightArm = this.createScale(this.bipedRightArm, EnumParts.ARM_RIGHT);
		this.bipedRightArmwear = this.createScale(this.bipedRightArmwear, EnumParts.ARM_RIGHT);
		
		this.bipedLeftLeg = this.createScale(this.bipedLeftLeg, EnumParts.LEG_LEFT);
		this.bipedLeftLegwear = this.createScale(this.bipedLeftLegwear, EnumParts.LEG_LEFT);
		this.bipedRightLeg = this.createScale(this.bipedRightLeg, EnumParts.LEG_RIGHT);
		this.bipedRightLegwear = this.createScale(this.bipedRightLegwear, EnumParts.LEG_RIGHT);
		
		this.bipedHead = this.createScale(this.bipedHead, EnumParts.HEAD);
		this.bipedHeadwear = this.createScale(this.bipedHeadwear, EnumParts.HEAD);
		
		this.bipedBody = this.createScale(this.bipedBody, EnumParts.BODY);
		this.bipedBodyWear = this.createScale(this.bipedBodyWear, EnumParts.BODY);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ModelScaleRenderer createScale(ModelRenderer renderer, EnumParts part) {
		int textureX = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 2);
		int textureY = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 3);
		ModelScaleRenderer model = new ModelScaleRenderer((ModelBase) this, textureX, textureY, part);
		model.textureHeight = renderer.textureHeight;
		model.textureWidth = renderer.textureWidth;
		if (renderer.childModels != null) { model.childModels = new ArrayList(renderer.childModels); }
		model.cubeList = new ArrayList(renderer.cubeList);
		copyModelAngles(renderer, (ModelRenderer) model);
		List<ModelScaleRenderer> list = this.map.get(part);
		if (list == null) { this.map.put(part, list = new ArrayList<ModelScaleRenderer>()); }
		list.add(model);
		return model;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isSwingInProgress) {
			return super.getMainHand(entityIn);
		}
		EntityLivingBase living = (EntityLivingBase) entityIn;
		if (living.swingingHand == EnumHand.MAIN_HAND) { return EnumHandSide.RIGHT; }
		return EnumHandSide.LEFT;
	}

	public ModelRenderer getRandomModelBox(Random random) {
		switch (random.nextInt(5)) {
			case 0: return this.bipedHead;
			case 1: return this.bipedBody;
			case 2: return this.bipedLeftArm;
			case 3: return this.bipedRightArm;
			case 4: return this.bipedLeftLeg;
			case 5: return this.bipedRightLeg;
			default: return this.bipedHead;
		}
	}

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		try {
			GlStateManager.pushMatrix();
			if (entityIn.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
			this.bipedHead.render(scale);
			this.bipedBody.render(scale);
			this.bipedRightArm.render(scale);
			this.bipedLeftArm.render(scale);
			this.bipedRightLeg.render(scale);
			this.bipedLeftLeg.render(scale);
			this.bipedHeadwear.render(scale);
			this.bipedLeftLegwear.render(scale);
			this.bipedRightLegwear.render(scale);
			this.bipedLeftArmwear.render(scale);
			this.bipedRightArmwear.render(scale);
			this.bipedBodyWear.render(scale);
			GlStateManager.popMatrix();
		}
		catch (Exception ex) { }
	}

	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		EntityCustomNpc npc = (EntityCustomNpc) entity;
		ModelData playerdata = npc.modelData;
		for (EnumParts part : this.map.keySet()) {
			ModelPartConfig config = playerdata.getPartConfig(part);
			for (ModelScaleRenderer model : this.map.get(part)) { model.config = config; }
		}
		if (!this.isRiding) { this.isRiding = (npc.currentAnimation == 1); }
		if (this.isSneak && (npc.currentAnimation == 7 || npc.isPlayerSleeping())) { this.isSneak = false; }
		if (npc.currentAnimation == 6) { this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW; }
		this.isSneak = npc.isSneaking();
		this.bipedBody.rotationPointZ =  0.0f;
		this.bipedBody.rotationPointY =  0.0f;
		this.bipedBody.rotationPointX =  0.0f;
		this.bipedBody.rotateAngleZ =  0.0f;
		this.bipedBody.rotateAngleY =  0.0f;
		this.bipedBody.rotateAngleX =  0.0f;
		this.bipedHead.rotateAngleX =  0.0f;
		this.bipedHeadwear.rotateAngleX =  0.0f;
		this.bipedHead.rotateAngleZ =  0.0f;
		this.bipedHeadwear.rotateAngleZ =  0.0f;
		this.bipedHead.rotationPointX =  0.0f;
		this.bipedHeadwear.rotationPointX =  0.0f;
		this.bipedHead.rotationPointY =  0.0f;
		this.bipedHeadwear.rotationPointY =  0.0f;
		this.bipedHead.rotationPointZ =  0.0f;
		this.bipedHeadwear.rotationPointZ =  0.0f;
		this.bipedLeftLeg.rotateAngleX = 0.0f;
		this.bipedLeftLeg.rotateAngleY = 0.0f;
		this.bipedLeftLeg.rotateAngleZ = 0.0f;
		this.bipedRightLeg.rotateAngleX = 0.0f;
		this.bipedRightLeg.rotateAngleY = 0.0f;
		this.bipedRightLeg.rotateAngleZ = 0.0f;
		this.bipedLeftArm.rotationPointX = 0.0f;
		this.bipedLeftArm.rotationPointY = 2.0f;
		this.bipedLeftArm.rotationPointZ = 0.0f;
		this.bipedRightArm.rotationPointX = 0.0f;
		this.bipedRightArm.rotationPointY = 2.0f;
		this.bipedRightArm.rotationPointZ = 0.0f;
		try { super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity); }
		catch (Exception ex) { }
		
		if (npc.isPlayerSleeping()) {
			if (this.bipedHead.rotateAngleX < 0.0f) {
				this.bipedHead.rotateAngleX = 0.0f;
				this.bipedHeadwear.rotateAngleX = 0.0f;
			}
		} else if (npc.currentAnimation == 9) {
			this.bipedHeadwear.rotateAngleX = 0.7f;
			this.bipedHead.rotateAngleX = 0.7f;
		} else if (npc.currentAnimation == 3) {
			AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 7) {
			AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 10) {
			AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 5) {
			AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 11) {
			AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 13) {
			AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 12) {
			AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 8) {
			AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity, (ModelBiped) this);
		} else if (this.isSneak) {
			this.bipedBody.rotateAngleX = 0.5f / playerdata.getPartConfig(EnumParts.BODY).scaleY;
		}
		
		AnimationConfig anim = npc.animation.activeAnim;
		if (!(Minecraft.getMinecraft().currentScreen instanceof GuiNpcAnimation)) {
			// Dies
			if (npc.isDead && (anim==null || anim.type!=EnumAnimationType.dies)) {
				anim = npc.animation.getActive(EnumAnimationType.dies);
			}
			// Swing
			if (this.swingProgress>0 && (anim==null || anim.type!=EnumAnimationType.attacking) &&
					(anim==null || anim.type!=EnumAnimationType.init)) {
				anim = npc.animation.getActive(EnumAnimationType.attacking);
			}
			// Jump
			if ((boolean) ObfuscationHelper.getValue(EntityLivingBase.class, npc, 49)/*npc.isJumping*/ &&
					(anim==null || anim.type!=EnumAnimationType.jump) &&
					(anim==null || anim.type!=EnumAnimationType.init)) {
				anim = npc.animation.getActive(EnumAnimationType.jump);
			}
			// Init animation starts in EntityNPCInterface.class
			// Moving/Standing
			if (anim==null || anim.type.isCyclical()) {
				boolean isNavigate = npc.motionX!=0.0d && npc.motionY!=0.0d && npc.motionZ!=0.0d;
				if (npc.isInWater() || npc.isInLava()) {
					if (isNavigate && anim==null || anim.type!=EnumAnimationType.waterwalk) {
						anim = npc.animation.getActive(EnumAnimationType.waterwalk);
					} else if (!isNavigate && anim==null || anim.type!=EnumAnimationType.waterstand) {
						anim = npc.animation.getActive(EnumAnimationType.waterstand);
					}
				}
				else {
					if (!npc.onGround && npc.ais.getNavigationType()==1) {
						if (isNavigate && anim==null || anim.type!=EnumAnimationType.flywalk) {
							anim = npc.animation.getActive(EnumAnimationType.flywalk);
						} else if (!isNavigate && anim==null || anim.type!=EnumAnimationType.flystand) {
							anim = npc.animation.getActive(EnumAnimationType.flystand);
						}
					} else {
						if (isNavigate && (anim==null || anim.type!=EnumAnimationType.walking)) {
							anim = npc.animation.getActive(EnumAnimationType.walking);
						} else if (!isNavigate && (anim==null || anim.type!=EnumAnimationType.standing)) {
							anim = npc.animation.getActive(EnumAnimationType.standing);
						}
					}
				}
			}
		}
		if (anim!=null) {
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			float[] headRot = anim.getRotation(0, 0, anim.type.isCyclical(), partialTicks, npc);
			if (headRot == null) { npc.animation.stop(); }
			if (headRot.length==3) {
				float[] headTr = anim.getRotation(0, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] headSc = anim.getRotation(0, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedHead.rotateAngleX = headRot[0];
				this.bipedHead.rotateAngleY = headRot[1];
				this.bipedHead.rotateAngleZ = headRot[2];
				this.bipedHeadwear.rotateAngleX = headRot[0];
				this.bipedHeadwear.rotateAngleY = headRot[1];
				this.bipedHeadwear.rotateAngleZ = headRot[2];
				((ModelScaleRenderer) this.bipedHead).config.transX = headTr[0];
				((ModelScaleRenderer) this.bipedHead).config.transY = headTr[1];
				((ModelScaleRenderer) this.bipedHead).config.transZ = headTr[2];
				((ModelScaleRenderer) this.bipedHeadwear).config.transX = headTr[0];
				((ModelScaleRenderer) this.bipedHeadwear).config.transY = headTr[1];
				((ModelScaleRenderer) this.bipedHeadwear).config.transZ = headTr[2];
				
				((ModelScaleRenderer) this.bipedHead).config.scaleX = headSc[0];
				((ModelScaleRenderer) this.bipedHead).config.scaleY = headSc[1];
				((ModelScaleRenderer) this.bipedHead).config.scaleZ = headSc[2];
				((ModelScaleRenderer) this.bipedHeadwear).config.scaleX = headSc[0];
				((ModelScaleRenderer) this.bipedHeadwear).config.scaleY = headSc[1];
				((ModelScaleRenderer) this.bipedHeadwear).config.scaleZ = headSc[2];
			}
			float[] leftArmsRot = anim.getRotation(1, 0, anim.type.isCyclical(), partialTicks, npc);
			if (leftArmsRot == null) { npc.animation.stop(); }
			if (leftArmsRot.length>0) {
				float[] leftArmsTr = anim.getRotation(1, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] leftArmsSc = anim.getRotation(1, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedLeftArm.rotateAngleX = leftArmsRot[0];
				this.bipedLeftArm.rotateAngleY = leftArmsRot[1];
				this.bipedLeftArm.rotateAngleZ = leftArmsRot[2];
				((ModelScaleRenderer) this.bipedLeftArm).config.transX = leftArmsTr[0];
				((ModelScaleRenderer) this.bipedLeftArm).config.transY = leftArmsTr[1];
				((ModelScaleRenderer) this.bipedLeftArm).config.transZ = leftArmsTr[2];
				((ModelScaleRenderer) this.bipedLeftArm).config.scaleX = leftArmsSc[0];
				((ModelScaleRenderer) this.bipedLeftArm).config.scaleY = leftArmsSc[1];
				((ModelScaleRenderer) this.bipedLeftArm).config.scaleZ = leftArmsSc[2];
				if (npc.display.getHasLivingAnimation() && (anim.type==EnumAnimationType.standing || anim.type==EnumAnimationType.flystand || anim.type==EnumAnimationType.waterstand)) {
					ModelRenderer bipedLeftArm = this.bipedLeftArm;
					bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
					bipedLeftArm2.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
				}
			}
			float[] rightArmsRot = anim.getRotation(2, 0, anim.type.isCyclical(), partialTicks, npc);
			if (rightArmsRot == null) { npc.animation.stop(); }
			if (rightArmsRot.length>0) {
				float[] rightArmsTr = anim.getRotation(2, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] rightArmsSc = anim.getRotation(2, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedRightArm.rotateAngleX = rightArmsRot[0];
				this.bipedRightArm.rotateAngleY = rightArmsRot[1];
				this.bipedRightArm.rotateAngleZ = rightArmsRot[2];
				((ModelScaleRenderer) this.bipedRightArm).config.transX = rightArmsTr[0];
				((ModelScaleRenderer) this.bipedRightArm).config.transY = rightArmsTr[1];
				((ModelScaleRenderer) this.bipedRightArm).config.transZ = rightArmsTr[2];
				((ModelScaleRenderer) this.bipedRightArm).config.scaleX = rightArmsSc[0];
				((ModelScaleRenderer) this.bipedRightArm).config.scaleY = rightArmsSc[1];
				((ModelScaleRenderer) this.bipedRightArm).config.scaleZ = rightArmsSc[2];
				if (npc.display.getHasLivingAnimation() && (anim.type==EnumAnimationType.standing || anim.type==EnumAnimationType.flystand || anim.type==EnumAnimationType.waterstand)) {
					ModelRenderer bipedRightArm = this.bipedRightArm;
					bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedRightArm2 = this.bipedRightArm;
					bipedRightArm2.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
				}
			}
			float[] bodyRot = anim.getRotation(3, 0, anim.type.isCyclical(), partialTicks, npc);
			if (bodyRot == null) { npc.animation.stop(); }
			if (bodyRot.length>0) {
				float[] bodyTr = anim.getRotation(2, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] bodySc = anim.getRotation(2, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedBody.rotateAngleX = bodyRot[0];
				this.bipedBody.rotateAngleY = bodyRot[1];
				this.bipedBody.rotateAngleZ = bodyRot[2];
				((ModelScaleRenderer) this.bipedBody).config.transX = bodyTr[0];
				((ModelScaleRenderer) this.bipedBody).config.transY = bodyTr[1];
				((ModelScaleRenderer) this.bipedBody).config.transZ = bodyTr[2];
				((ModelScaleRenderer) this.bipedBody).config.scaleX = bodySc[0];
				((ModelScaleRenderer) this.bipedBody).config.scaleY = bodySc[1];
				((ModelScaleRenderer) this.bipedBody).config.scaleZ = bodySc[2];
			}
			float[] leftLegRot = anim.getRotation(4, 0, anim.type.isCyclical(), partialTicks, npc);
			if (leftLegRot == null) { npc.animation.stop(); }
			if (leftLegRot.length>0) {
				float[] leftLegTr = anim.getRotation(2, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] leftLegSc = anim.getRotation(2, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedLeftLeg.rotateAngleX = leftLegRot[0];
				this.bipedLeftLeg.rotateAngleY = leftLegRot[1];
				this.bipedLeftLeg.rotateAngleZ = leftLegRot[2];
				((ModelScaleRenderer) this.bipedLeftLeg).config.transX = leftLegTr[0];
				((ModelScaleRenderer) this.bipedLeftLeg).config.transY = leftLegTr[1];
				((ModelScaleRenderer) this.bipedLeftLeg).config.transZ = leftLegTr[2];
				((ModelScaleRenderer) this.bipedLeftLeg).config.scaleX = leftLegSc[0];
				((ModelScaleRenderer) this.bipedLeftLeg).config.scaleY = leftLegSc[1];
				((ModelScaleRenderer) this.bipedLeftLeg).config.scaleZ = leftLegSc[2];
			}
			float[] rightLegRot = anim.getRotation(5, 0, anim.type.isCyclical(), partialTicks, npc);
			if (rightLegRot == null) { npc.animation.stop(); }
			if (rightLegRot.length>0) {
				float[] rightLegTr = anim.getRotation(2, 1, anim.type.isCyclical(), partialTicks, npc);
				float[] rightLegSc = anim.getRotation(2, 2, anim.type.isCyclical(), partialTicks, npc);
				this.bipedRightLeg.rotateAngleX = rightLegRot[0];
				this.bipedRightLeg.rotateAngleY = rightLegRot[1];
				this.bipedRightLeg.rotateAngleZ = rightLegRot[2];
				((ModelScaleRenderer) this.bipedRightLeg).config.transX = rightLegTr[0];
				((ModelScaleRenderer) this.bipedRightLeg).config.transY = rightLegTr[1];
				((ModelScaleRenderer) this.bipedRightLeg).config.transZ = rightLegTr[2];
				((ModelScaleRenderer) this.bipedRightLeg).config.scaleX = rightLegSc[0];
				((ModelScaleRenderer) this.bipedRightLeg).config.scaleY = rightLegSc[1];
				((ModelScaleRenderer) this.bipedRightLeg).config.scaleZ = rightLegSc[2];
			}
		}
		ModelPlayerAlt.copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
		ModelPlayerAlt.copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
		ModelPlayerAlt.copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
		ModelPlayerAlt.copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
		ModelPlayerAlt.copyModelAngles(this.bipedBody, this.bipedBodyWear);
		ModelPlayerAlt.copyModelAngles(this.bipedHead, this.bipedHeadwear);
	}
	
	public static void copyModelAngles(ModelRenderer source, ModelRenderer dest) {
		
		dest.rotateAngleX = source.rotateAngleX;
        dest.rotateAngleY = source.rotateAngleY;
        dest.rotateAngleZ = source.rotateAngleZ;
        
        dest.rotationPointX = source.rotationPointX;
        dest.rotationPointY = source.rotationPointY;
        dest.rotationPointZ = source.rotationPointZ;
        
        /*if (source instanceof ModelScaleRenderer && dest instanceof ModelScaleRenderer) {
			((ModelScaleRenderer) dest).config.transX = ((ModelScaleRenderer) source).config.transX;
			((ModelScaleRenderer) dest).config.transY = ((ModelScaleRenderer) source).config.transY;
			((ModelScaleRenderer) dest).config.transZ = ((ModelScaleRenderer) source).config.transZ;
			
			((ModelScaleRenderer) dest).config.scaleX = ((ModelScaleRenderer) source).config.scaleX;
			((ModelScaleRenderer) dest).config.scaleY = ((ModelScaleRenderer) source).config.scaleY;
			((ModelScaleRenderer) dest).config.scaleZ = ((ModelScaleRenderer) source).config.scaleZ;
        }*/
	}
}
