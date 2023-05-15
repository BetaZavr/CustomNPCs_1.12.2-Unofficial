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
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.animation.PartConfig;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

public class ModelPlayerAlt
extends ModelPlayer {
	
	private ModelRenderer body;
	private ModelRenderer head;
	private Map<EnumParts, List<ModelScaleRenderer>> map;
	private boolean isSwing = false, isDead = false;

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
		this.bipedRightArm = this.createScale(this.bipedRightArm, EnumParts.ARM_RIGHT);
		this.bipedLeftArmwear = this.createScale(this.bipedLeftArmwear, EnumParts.ARM_LEFT);
		this.bipedRightArmwear = this.createScale(this.bipedRightArmwear, EnumParts.ARM_RIGHT);
		this.bipedLeftLeg = this.createScale(this.bipedLeftLeg, EnumParts.LEG_LEFT);
		this.bipedRightLeg = this.createScale(this.bipedRightLeg, EnumParts.LEG_RIGHT);
		this.bipedLeftLegwear = this.createScale(this.bipedLeftLegwear, EnumParts.LEG_LEFT);
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
		if (renderer.childModels != null) {
			model.childModels = new ArrayList(renderer.childModels);
		}
		model.cubeList = new ArrayList(renderer.cubeList);
		copyModelAngles(renderer, (ModelRenderer) model);
		List<ModelScaleRenderer> list = this.map.get(part);
		if (list == null) {
			this.map.put(part, list = new ArrayList<ModelScaleRenderer>());
		}
		list.add(model);
		return model;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isSwingInProgress) {
			return super.getMainHand(entityIn);
		}
		EntityLivingBase living = (EntityLivingBase) entityIn;
		if (living.swingingHand == EnumHand.MAIN_HAND) {
			return EnumHandSide.RIGHT;
		}
		return EnumHandSide.LEFT;
	}

	public ModelRenderer getRandomModelBox(Random random) {
		switch (random.nextInt(5)) {
		case 0: {
			return this.bipedHead;
		}
		case 1: {
			return this.bipedBody;
		}
		case 2: {
			return this.bipedLeftArm;
		}
		case 3: {
			return this.bipedRightArm;
		}
		case 4: {
			return this.bipedLeftLeg;
		}
		case 5: {
			return this.bipedRightLeg;
		}
		default: {
			return this.bipedHead;
		}
		}
	}

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		try {
			GlStateManager.pushMatrix();
			if (entityIn.isSneaking()) {
				GlStateManager.translate(0.0f, 0.2f, 0.0f);
			}
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
		} catch (Exception ex) {
		}
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
		ModelRenderer bipedBody = this.bipedBody;
		ModelRenderer bipedBody2 = this.bipedBody;
		ModelRenderer bipedBody3 = this.bipedBody;
		float rotationPointX = 0.0f;
		bipedBody3.rotationPointZ = rotationPointX;
		bipedBody2.rotationPointY = rotationPointX;
		bipedBody.rotationPointX = rotationPointX;
		ModelRenderer bipedBody4 = this.bipedBody;
		ModelRenderer bipedBody5 = this.bipedBody;
		ModelRenderer bipedBody6 = this.bipedBody;
		float rotateAngleX = 0.0f;
		bipedBody6.rotateAngleZ = rotateAngleX;
		bipedBody5.rotateAngleY = rotateAngleX;
		bipedBody4.rotateAngleX = rotateAngleX;
		ModelRenderer bipedHeadwear = this.bipedHeadwear;
		ModelRenderer bipedHead = this.bipedHead;
		float n = 0.0f;
		bipedHead.rotateAngleX = n;
		bipedHeadwear.rotateAngleX = n;
		ModelRenderer bipedHeadwear2 = this.bipedHeadwear;
		ModelRenderer bipedHead2 = this.bipedHead;
		float n2 = 0.0f;
		bipedHead2.rotateAngleZ = n2;
		bipedHeadwear2.rotateAngleZ = n2;
		ModelRenderer bipedHeadwear3 = this.bipedHeadwear;
		ModelRenderer bipedHead3 = this.bipedHead;
		float n3 = 0.0f;
		bipedHead3.rotationPointX = n3;
		bipedHeadwear3.rotationPointX = n3;
		ModelRenderer bipedHeadwear4 = this.bipedHeadwear;
		ModelRenderer bipedHead4 = this.bipedHead;
		float n4 = 0.0f;
		bipedHead4.rotationPointY = n4;
		bipedHeadwear4.rotationPointY = n4;
		ModelRenderer bipedHeadwear5 = this.bipedHeadwear;
		ModelRenderer bipedHead5 = this.bipedHead;
		float n5 = 0.0f;
		bipedHead5.rotationPointZ = n5;
		bipedHeadwear5.rotationPointZ = n5;
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
		try {
			super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity);
		} catch (Exception ex) {
		}
		if (npc.isPlayerSleeping()) {
			if (this.bipedHead.rotateAngleX < 0.0f) {
				this.bipedHead.rotateAngleX = 0.0f;
				this.bipedHeadwear.rotateAngleX = 0.0f;
			}
		} else if (npc.currentAnimation == 9) {
			ModelRenderer bipedHeadwear6 = this.bipedHeadwear;
			ModelRenderer bipedHead6 = this.bipedHead;
			float n6 = 0.7f;
			bipedHead6.rotateAngleX = n6;
			bipedHeadwear6.rotateAngleX = n6;
		} else if (npc.currentAnimation == 3) {
			AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity,
					(ModelBiped) this);
		} else if (npc.currentAnimation == 7) {
			AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 10) {
			AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 5) {
			AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entity, (ModelBiped) this);
		} else if (npc.currentAnimation == 11) {
			AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity,
					(ModelBiped) this);
		} else if (npc.currentAnimation == 13) {
			AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity,
					(ModelBiped) this);
		} else if (npc.currentAnimation == 12) {
			AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entity,
					(ModelBiped) this);
		} else if (npc.currentAnimation == 8) {
			AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entity, (ModelBiped) this);
		} else if (this.isSneak) {
			this.bipedBody.rotateAngleX = 0.5f / playerdata.getPartConfig(EnumParts.BODY).scaleY;
		}
		
		AnimationConfig anim = npc.animation.activeAnim;
		// Dies
		if (npc.isDead) {
			if (!this.isDead) {
				System.out.println("Start Dies");
				this.isDead = true;
				anim = npc.animation.getActive(EnumAnimationType.dies);
			}
		} else if (this.isDead) {
			System.out.println("Stop Dies");
			this.isDead = false;
			if (anim!=null && anim.type==EnumAnimationType.dies) { anim = null; }
		}
		// Swing
		if (this.swingProgress>0) {
			if (!this.isSwing) {
				System.out.println("Start Swing");
				this.isSwing = true;
				anim = npc.animation.getActive(EnumAnimationType.attacking);
			}
		} else if (this.isSwing) {
			System.out.println("Stop Swing");
			this.isSwing = false;
			anim = null;
		}
		// Moving
		if (anim==null) {
			if (npc.getNavigator().noPath()) {
				anim = npc.animation.getActive(EnumAnimationType.walking);
			} else {
				anim = npc.animation.getActive(EnumAnimationType.standing);
			}
		} else if (anim.type!=EnumAnimationType.attacking && anim.type!=EnumAnimationType.dies) {
			if (npc.getNavigator().noPath() && anim.type!=EnumAnimationType.walking) {
				anim = npc.animation.getActive(EnumAnimationType.walking);
			} else if (!npc.getNavigator().noPath() && anim.type!=EnumAnimationType.standing) {
				anim = npc.animation.getActive(EnumAnimationType.standing);
			}
		}
		
		if (anim!=null) {
			float pi = (float) Math.PI;
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			PartConfig[] heads = anim.getParts(0);
			if (heads!=null && heads.length>0) {
				ModelRenderer bipedHeadwear7 = this.bipedHeadwear;
				ModelRenderer bipedHead7 = this.bipedHead;
				float rotX = anim.getRotation(heads, 0, partialTicks) * pi;
				float rotY = anim.getRotation(heads, 1, partialTicks) * pi;
				float rotZ = anim.getRotation(heads, 2, partialTicks) * pi;
				bipedHead7.rotateAngleX = rotX;
				bipedHeadwear7.rotateAngleX = rotX;
				ModelRenderer bipedHeadwear8 = this.bipedHeadwear;
				ModelRenderer bipedHead8 = this.bipedHead;
				bipedHead8.rotateAngleY = rotY;
				bipedHeadwear8.rotateAngleY = rotY;
				ModelRenderer bipedHeadwear9 = this.bipedHeadwear;
				ModelRenderer bipedHead9 = this.bipedHead;
				bipedHead9.rotateAngleZ = rotZ;
				bipedHeadwear9.rotateAngleZ = rotZ;
			}
			PartConfig[] leftArms = anim.getParts(1);
			if (leftArms!=null && leftArms.length>0) {
				this.bipedLeftArm.rotateAngleX = anim.getRotation(leftArms, 0, partialTicks) * pi;
				this.bipedLeftArm.rotateAngleY = anim.getRotation(leftArms, 1, partialTicks) * pi;
				this.bipedLeftArm.rotateAngleZ = anim.getRotation(leftArms, 2, partialTicks) * pi;
				if (npc.display.getHasLivingAnimation() && anim.type==EnumAnimationType.standing) {
					ModelRenderer bipedLeftArm = this.bipedLeftArm;
					bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
					bipedLeftArm2.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
				}
			}
			PartConfig[] rightArms = anim.getParts(2);
			if (rightArms!=null && rightArms.length>0) {
				this.bipedRightArm.rotateAngleX = anim.getRotation(rightArms, 0, partialTicks) * pi;
				this.bipedRightArm.rotateAngleY = anim.getRotation(rightArms, 0, partialTicks) * pi;
				this.bipedRightArm.rotateAngleZ = anim.getRotation(rightArms, 0, partialTicks) * pi;
				if (npc.display.getHasLivingAnimation() && anim.type==EnumAnimationType.standing) {
					ModelRenderer bipedRightArm = this.bipedRightArm;
					bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedRightArm2 = this.bipedRightArm;
					bipedRightArm2.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
				}
			}
			PartConfig[] bodys = anim.getParts(3);
			if (bodys!=null && bodys.length>0) {
				this.bipedBody.rotateAngleX = anim.getRotation(bodys, 0, partialTicks) * pi;
				this.bipedBody.rotateAngleY = anim.getRotation(bodys, 1, partialTicks) * pi;
				this.bipedBody.rotateAngleZ = anim.getRotation(bodys, 2, partialTicks) * pi;
			}
			PartConfig[] leftLeg = anim.getParts(4);
			if (leftLeg!=null && leftLeg.length>0) {
				this.bipedLeftLeg.rotateAngleX = anim.getRotation(leftLeg, 0, partialTicks) * pi;
				this.bipedLeftLeg.rotateAngleY = anim.getRotation(leftLeg, 1, partialTicks) * pi;
				this.bipedLeftLeg.rotateAngleZ = anim.getRotation(leftLeg, 2, partialTicks) * pi;
			}
			PartConfig[] rightLeg = anim.getParts(4);
			if (rightLeg!=null && rightLeg.length>0) {
				this.bipedRightLeg.rotateAngleX = anim.getRotation(rightLeg, 0, partialTicks) * pi;
				this.bipedRightLeg.rotateAngleY = anim.getRotation(rightLeg, 1, partialTicks) * pi;
				this.bipedRightLeg.rotateAngleZ = anim.getRotation(rightLeg, 2, partialTicks) * pi;
			}
		}
		copyModelAngles(this.bipedLeftLeg, this.bipedLeftLegwear);
		copyModelAngles(this.bipedRightLeg, this.bipedRightLegwear);
		copyModelAngles(this.bipedLeftArm, this.bipedLeftArmwear);
		copyModelAngles(this.bipedRightArm, this.bipedRightArmwear);
		copyModelAngles(this.bipedBody, this.bipedBodyWear);
		copyModelAngles(this.bipedHead, this.bipedHeadwear);
	}
}
