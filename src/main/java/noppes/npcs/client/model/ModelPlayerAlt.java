package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

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
	private Map<Integer, Float[]> data;

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
		
		this.data = Maps.<Integer, Float[]>newTreeMap();
		for (int i=0; i<12; i++) { this.data.put(i, new Float[6]); }
		System.out.println("Create new NPC Render");
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
		this.resetModel();
	}
	
	private void saveModel() {
		if (this.bipedLeftArm instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(0)) { this.data.put(0, new Float[6]); }
			Float[] sets = this.data.get(0);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftArm).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedLeftArmwear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(1)) { this.data.put(1, new Float[6]); }
			Float[] sets = this.data.get(1);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftArmwear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedRightArm instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(2)) { this.data.put(2, new Float[6]); }
			Float[] sets = this.data.get(2);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightArm).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedRightArmwear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(3)) { this.data.put(3, new Float[6]); }
			Float[] sets = this.data.get(3);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightArmwear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedLeftLeg instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(4)) { this.data.put(4, new Float[6]); }
			Float[] sets = this.data.get(4);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftLeg).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedLeftLegwear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(5)) { this.data.put(5, new Float[6]); }
			Float[] sets = this.data.get(5);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftLegwear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedRightLeg instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(6)) { this.data.put(6, new Float[6]); }
			Float[] sets = this.data.get(6);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightLeg).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedRightLegwear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(7)) { this.data.put(7, new Float[6]); }
			Float[] sets = this.data.get(7);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightLegwear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedHead instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(8)) { this.data.put(8, new Float[6]); }
			Float[] sets = this.data.get(8);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedHead).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedHeadwear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(9)) { this.data.put(9, new Float[6]); }
			Float[] sets = this.data.get(9);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedHeadwear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedBody instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(10)) { this.data.put(10, new Float[6]); }
			Float[] sets = this.data.get(10);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedBody).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
		if (this.bipedBodyWear instanceof ModelScaleRenderer) {
			if (!this.data.containsKey(11)) { this.data.put(11, new Float[6]); }
			Float[] sets = this.data.get(11);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedBodyWear).config;
			if (config!=null ) {
				sets[0] = config.transX;
				sets[1] = config.transY;
				sets[2] = config.transZ;
				sets[3] = config.scaleX;
				sets[4] = config.scaleY;
				sets[5] = config.scaleZ;
			}
		}
	}
	
	private void resetModel() {
		if (this.bipedLeftArm instanceof ModelScaleRenderer && this.data.containsKey(0)) {
			Float[] sets = this.data.get(0);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftArm).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedLeftArmwear instanceof ModelScaleRenderer && this.data.containsKey(1)) {
			Float[] sets = this.data.get(1);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftArmwear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedRightArm instanceof ModelScaleRenderer && this.data.containsKey(2)) {
			Float[] sets = this.data.get(2);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightArm).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedRightArmwear instanceof ModelScaleRenderer && this.data.containsKey(3)) {
			Float[] sets = this.data.get(3);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightArmwear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedLeftLeg instanceof ModelScaleRenderer && this.data.containsKey(4)) {
			Float[] sets = this.data.get(4);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftLeg).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedLeftLegwear instanceof ModelScaleRenderer && this.data.containsKey(5)) {
			Float[] sets = this.data.get(5);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedLeftLegwear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedRightLeg instanceof ModelScaleRenderer && this.data.containsKey(6)) {
			Float[] sets = this.data.get(6);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightLeg).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedRightLegwear instanceof ModelScaleRenderer && this.data.containsKey(7)) {
			Float[] sets = this.data.get(7);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedRightLegwear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedHead instanceof ModelScaleRenderer && this.data.containsKey(8)) {
			Float[] sets = this.data.get(8);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedHead).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedHeadwear instanceof ModelScaleRenderer && this.data.containsKey(9)) {
			Float[] sets = this.data.get(9);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedHeadwear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedBody instanceof ModelScaleRenderer && this.data.containsKey(10)) {
			Float[] sets = this.data.get(10);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedBody).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
		}
		if (this.bipedBodyWear instanceof ModelScaleRenderer && this.data.containsKey(11)) {
			Float[] sets = this.data.get(11);
			ModelPartConfig config = ((ModelScaleRenderer) this.bipedBodyWear).config;
			if (config!=null ) {
				config.setTranslate(sets[0], sets[1], sets[2]);
				config.scaleX = sets[3];
				config.scaleY = sets[4];
				config.scaleZ = sets[5];
			}
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
				anim = npc.animation.getActiveAnimation(EnumAnimationType.dies);
			}
			// Swing
			if (this.swingProgress>0 && (anim==null || anim.type!=EnumAnimationType.attacking) &&
					(anim==null || anim.type!=EnumAnimationType.init)) {
				anim = npc.animation.getActiveAnimation(EnumAnimationType.attacking);
			}
			// Jump
			if ((boolean) ObfuscationHelper.getValue(EntityLivingBase.class, npc, 49)/*npc.isJumping*/ &&
					(anim==null || anim.type!=EnumAnimationType.jump) &&
					(anim==null || anim.type!=EnumAnimationType.init)) {
				anim = npc.animation.getActiveAnimation(EnumAnimationType.jump);
			}
			// Init animation starts in EntityNPCInterface.class
			// Moving/Standing
			if (anim==null || anim.type.isCyclical()) {
				boolean isNavigate = npc.motionX!=0.0d && npc.motionY!=0.0d && npc.motionZ!=0.0d;
				if (npc.isInWater() || npc.isInLava()) {
					if (isNavigate && anim==null || anim.type!=EnumAnimationType.waterwalk) {
						anim = npc.animation.getActiveAnimation(EnumAnimationType.waterwalk);
					} else if (!isNavigate && anim==null || anim.type!=EnumAnimationType.waterstand) {
						anim = npc.animation.getActiveAnimation(EnumAnimationType.waterstand);
					}
				}
				else {
					if (!npc.onGround && npc.ais.getNavigationType()==1) {
						if (isNavigate && anim==null || anim.type!=EnumAnimationType.flywalk) {
							anim = npc.animation.getActiveAnimation(EnumAnimationType.flywalk);
						} else if (!isNavigate && anim==null || anim.type!=EnumAnimationType.flystand) {
							anim = npc.animation.getActiveAnimation(EnumAnimationType.flystand);
						}
					} else {
						if (isNavigate && (anim==null || anim.type!=EnumAnimationType.walking)) {
							anim = npc.animation.getActiveAnimation(EnumAnimationType.walking);
						} else if (!isNavigate && (anim==null || anim.type!=EnumAnimationType.standing)) {
							anim = npc.animation.getActiveAnimation(EnumAnimationType.standing);
						}
					}
				}
			}
		}
		if (anim!=null) {
			this.saveModel();
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			Map<Integer, Float[]> animData = anim.getValues(partialTicks, npc);
			if (animData == null) { npc.animation.stopAnimation(); }
			else {
				System.out.println("animData: "+animData);
				
				// Head
				Float[] head = animData.get(0);
				if (head!=null) {
					this.bipedHead.rotateAngleX = head[0];
					this.bipedHead.rotateAngleY = head[1];
					this.bipedHead.rotateAngleZ = head[2];
					this.bipedHeadwear.rotateAngleX = head[0];
					this.bipedHeadwear.rotateAngleY = head[1];
					this.bipedHeadwear.rotateAngleZ = head[2];
					((ModelScaleRenderer) this.bipedHead).config.transX += head[3];
					((ModelScaleRenderer) this.bipedHead).config.transY += head[4];
					((ModelScaleRenderer) this.bipedHead).config.transZ += head[5];
					((ModelScaleRenderer) this.bipedHeadwear).config.transX += head[3];
					((ModelScaleRenderer) this.bipedHeadwear).config.transY += head[4];
					((ModelScaleRenderer) this.bipedHeadwear).config.transZ += head[5];
					((ModelScaleRenderer) this.bipedHead).config.scaleX *= head[6];
					((ModelScaleRenderer) this.bipedHead).config.scaleY *= head[7];
					((ModelScaleRenderer) this.bipedHead).config.scaleZ *= head[8];
					((ModelScaleRenderer) this.bipedHeadwear).config.scaleX *= head[6];
					((ModelScaleRenderer) this.bipedHeadwear).config.scaleY *= head[7];
					((ModelScaleRenderer) this.bipedHeadwear).config.scaleZ *= head[8];
				}
				
				// Left Arm
				Float[] leftArm = animData.get(1);
				if (leftArm!=null) {
					this.bipedLeftArm.rotateAngleX = leftArm[0];
					this.bipedLeftArm.rotateAngleY = leftArm[1];
					this.bipedLeftArm.rotateAngleZ = leftArm[2];
					((ModelScaleRenderer) this.bipedLeftArm).config.transX += leftArm[3];
					((ModelScaleRenderer) this.bipedLeftArm).config.transY += leftArm[4];
					((ModelScaleRenderer) this.bipedLeftArm).config.transZ += leftArm[5];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.transX += leftArm[3];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.transY += leftArm[4];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.transZ += leftArm[5];
					((ModelScaleRenderer) this.bipedLeftArm).config.scaleX *= leftArm[6];
					((ModelScaleRenderer) this.bipedLeftArm).config.scaleY *= leftArm[7];
					((ModelScaleRenderer) this.bipedLeftArm).config.scaleZ *= leftArm[8];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.scaleX *= leftArm[6];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.scaleY *= leftArm[7];
					((ModelScaleRenderer) this.bipedLeftArmwear).config.scaleZ *= leftArm[8];
					if (npc.display.getHasLivingAnimation() && (anim.type==EnumAnimationType.standing || anim.type==EnumAnimationType.flystand || anim.type==EnumAnimationType.waterstand)) {
						ModelRenderer bipedLeftArm = this.bipedLeftArm;
						bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
						ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
						bipedLeftArm2.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
					}
				}
				
				// Right Arm
				Float[] rightArm = animData.get(2);
				if (rightArm!=null) {
					this.bipedRightArm.rotateAngleX = rightArm[0];
					this.bipedRightArm.rotateAngleY = rightArm[1];
					this.bipedRightArm.rotateAngleZ = rightArm[2];
					((ModelScaleRenderer) this.bipedRightArm).config.transX += rightArm[3];
					((ModelScaleRenderer) this.bipedRightArm).config.transY += rightArm[4];
					((ModelScaleRenderer) this.bipedRightArm).config.transZ += rightArm[5];
					((ModelScaleRenderer) this.bipedRightArmwear).config.transX += rightArm[3];
					((ModelScaleRenderer) this.bipedRightArmwear).config.transY += rightArm[4];
					((ModelScaleRenderer) this.bipedRightArmwear).config.transZ += rightArm[5];
					
					((ModelScaleRenderer) this.bipedRightArm).config.scaleX *= rightArm[6];
					((ModelScaleRenderer) this.bipedRightArm).config.scaleY *= rightArm[7];
					((ModelScaleRenderer) this.bipedRightArm).config.scaleZ *= rightArm[8];
					((ModelScaleRenderer) this.bipedRightArmwear).config.scaleX *= rightArm[6];
					((ModelScaleRenderer) this.bipedRightArmwear).config.scaleY *= rightArm[7];
					((ModelScaleRenderer) this.bipedRightArmwear).config.scaleZ *= rightArm[8];
					
					if (npc.display.getHasLivingAnimation() && (anim.type==EnumAnimationType.standing || anim.type==EnumAnimationType.flystand || anim.type==EnumAnimationType.waterstand)) {
						ModelRenderer bipedRightArm = this.bipedRightArm;
						bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
						ModelRenderer bipedRightArm2 = this.bipedRightArm;
						bipedRightArm2.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
					}
				}
				
				// Body
				Float[] body = animData.get(3);
				if (body!=null) {
					this.bipedBody.rotateAngleX = body[0];
					this.bipedBody.rotateAngleY = body[1];
					this.bipedBody.rotateAngleZ = body[2];
					((ModelScaleRenderer) this.bipedBody).config.transX += body[3];
					((ModelScaleRenderer) this.bipedBody).config.transY += body[4];
					((ModelScaleRenderer) this.bipedBody).config.transZ += body[5];
					((ModelScaleRenderer) this.bipedBodyWear).config.transX += body[3];
					((ModelScaleRenderer) this.bipedBodyWear).config.transY += body[4];
					((ModelScaleRenderer) this.bipedBodyWear).config.transZ += body[5];
					
					((ModelScaleRenderer) this.bipedBody).config.scaleX *= body[6];
					((ModelScaleRenderer) this.bipedBody).config.scaleY *= body[7];
					((ModelScaleRenderer) this.bipedBody).config.scaleZ *= body[8];
					((ModelScaleRenderer) this.bipedBodyWear).config.scaleX *= body[6];
					((ModelScaleRenderer) this.bipedBodyWear).config.scaleY *= body[7];
					((ModelScaleRenderer) this.bipedBodyWear).config.scaleZ *= body[8];
				}
				// Left Leg
				Float[] leftLeg = animData.get(4);
				if (body!=null) {
					this.bipedLeftLeg.rotateAngleX = leftLeg[0];
					this.bipedLeftLeg.rotateAngleY = leftLeg[1];
					this.bipedLeftLeg.rotateAngleZ = leftLeg[2];
					((ModelScaleRenderer) this.bipedLeftLeg).config.transX += leftLeg[3];
					((ModelScaleRenderer) this.bipedLeftLeg).config.transY += leftLeg[4];
					((ModelScaleRenderer) this.bipedLeftLeg).config.transZ += leftLeg[5];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transX += leftLeg[3];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transY += leftLeg[4];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transZ += leftLeg[5];
					
					((ModelScaleRenderer) this.bipedLeftLeg).config.scaleX *= leftLeg[6];
					((ModelScaleRenderer) this.bipedLeftLeg).config.scaleY *= leftLeg[7];
					((ModelScaleRenderer) this.bipedLeftLeg).config.scaleZ *= leftLeg[8];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleX *= leftLeg[6];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleY *= leftLeg[7];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleZ *= leftLeg[8];
				}

				// Left Leg
				Float[] rightLeg = animData.get(5);
				if (body!=null) {
					this.bipedRightLeg.rotateAngleX = rightLeg[0];
					this.bipedRightLeg.rotateAngleY = rightLeg[1];
					this.bipedRightLeg.rotateAngleZ = rightLeg[2];
					((ModelScaleRenderer) this.bipedRightLeg).config.transX += rightLeg[3];
					((ModelScaleRenderer) this.bipedRightLeg).config.transY += rightLeg[4];
					((ModelScaleRenderer) this.bipedRightLeg).config.transZ += rightLeg[5];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transX += rightLeg[3];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transY += rightLeg[4];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.transZ += rightLeg[5];
					
					((ModelScaleRenderer) this.bipedRightLeg).config.scaleX *= rightLeg[6];
					((ModelScaleRenderer) this.bipedRightLeg).config.scaleY *= rightLeg[7];
					((ModelScaleRenderer) this.bipedRightLeg).config.scaleZ *= rightLeg[8];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleX *= rightLeg[6];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleY *= rightLeg[7];
					((ModelScaleRenderer) this.bipedLeftLegwear).config.scaleZ *= rightLeg[8];
				}
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
