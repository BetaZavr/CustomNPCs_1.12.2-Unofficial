package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.api.constants.AnimationKind;
import noppes.npcs.client.gui.animation.GuiNpcAnimation;
import noppes.npcs.client.gui.animation.SubGuiEditAnimation;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.util.ObfuscationHelper;

public class ModelBipedAlt extends ModelBiped {
	private Map<EnumParts, List<ModelScaleRenderer>> map;
	private Map<EntityCustomNpc, Boolean> isAttaking;
	private Map<EntityCustomNpc, Boolean> isJump;
	private EntityEquipmentSlot slot;

	public ModelBipedAlt(float scale) {
		super(scale);
		this.map = Maps.<EnumParts, List<ModelScaleRenderer>>newHashMap();
		this.isAttaking = Maps.<EntityCustomNpc, Boolean>newHashMap();
		this.isJump = Maps.<EntityCustomNpc, Boolean>newHashMap();
		this.slot = null;
		this.bipedHead = this.createScale(this.bipedHead, EnumParts.HEAD);
		this.bipedHeadwear = this.createScale(this.bipedHead, EnumParts.MOHAWK);
		this.bipedBody = this.createScale(this.bipedBody, EnumParts.BODY);
		this.bipedLeftArm = this.createScale(this.bipedLeftArm, EnumParts.ARM_LEFT);
		this.bipedRightArm = this.createScale(this.bipedRightArm, EnumParts.ARM_RIGHT);
		this.bipedLeftLeg = this.createScale(this.bipedLeftLeg, EnumParts.LEG_LEFT);
		this.bipedRightLeg = this.createScale(this.bipedRightLeg, EnumParts.LEG_RIGHT);
	}

	private ModelScaleRenderer createScale(ModelRenderer renderer, EnumParts part) {
		int textureX = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 2);
		int textureY = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 3);
		ModelScaleRenderer model = new ModelScaleRenderer((ModelBase) this, textureX, textureY, part);
		model.textureHeight = renderer.textureHeight;
		model.textureWidth = renderer.textureWidth;
		model.childModels = renderer.childModels;
		model.cubeList = renderer.cubeList;
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

	@Override
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scale) {
		if (!(entityIn instanceof EntityCustomNpc)) {
			super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
			return;
		}
		if (Minecraft.getMinecraft().currentScreen instanceof SubGuiEditAnimation) {
			SubGuiEditAnimation gui = (SubGuiEditAnimation) Minecraft.getMinecraft().currentScreen;
			if (gui.getDisplayNpc().equals(entityIn) && !((SubGuiEditAnimation) gui).showArmor) {
				return;
			}
		} else if (Minecraft.getMinecraft().currentScreen instanceof GuiNpcAnimation) {
			GuiNpcAnimation gui = (GuiNpcAnimation) Minecraft.getMinecraft().currentScreen;
			if (gui.hasSubGui() && gui.subgui instanceof SubGuiEditAnimation
					&& ((SubGuiEditAnimation) gui.subgui).getDisplayNpc().equals(entityIn)
					&& !((SubGuiEditAnimation) gui.subgui).showArmor) {
				return;
			}
		}

		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		EntityCustomNpc npc = (EntityCustomNpc) entityIn;
		if (this.slot == null || !(npc.getItemStackFromSlot(this.slot).getItem() instanceof CustomArmor)
				|| ((CustomArmor) npc.getItemStackFromSlot(this.slot).getItem()).objModel == null) {
			if (this.slot != null) {
				this.slot = null;
			}
			if (((ModelScaleRenderer) this.bipedHead).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedHead).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedHeadwear).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedHeadwear).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedBody).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedBody).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedLeftArm).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedLeftArm).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedRightArm).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedRightArm).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedLeftLeg).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedLeftLeg).displayOBJList = -1;
			}
			if (((ModelScaleRenderer) this.bipedRightLeg).displayOBJList > 0) {
				((ModelScaleRenderer) this.bipedRightLeg).displayOBJList = -1;
			}
		} else {
			ItemStack stack = npc.getItemStackFromSlot(this.slot);
			if (this.slot == EntityEquipmentSlot.HEAD) {
				((ModelScaleRenderer) this.bipedHead).setOBJModel(stack, null);
				((ModelScaleRenderer) this.bipedHeadwear).setOBJModel(stack, null);
			} else if (this.slot == EntityEquipmentSlot.CHEST) {
				((ModelScaleRenderer) this.bipedBody).setOBJModel(stack, null);
				((ModelScaleRenderer) this.bipedLeftArm).setOBJModel(stack, null);
				((ModelScaleRenderer) this.bipedRightArm).setOBJModel(stack, null);
				String m = npc.display.getModel();
				boolean smallArms = m != null && m.indexOf("customnpcalex") != -1;
				((ModelScaleRenderer) this.bipedLeftArm).smallArms = smallArms;
				((ModelScaleRenderer) this.bipedRightArm).smallArms = smallArms;
			} else if (this.slot == EntityEquipmentSlot.LEGS || this.slot == EntityEquipmentSlot.FEET) {
				if (this.slot == EntityEquipmentSlot.LEGS) {
					((ModelScaleRenderer) this.bipedBody).setOBJModel(stack, EnumParts.BELT);
				}
				EnumParts part = this.slot == EntityEquipmentSlot.FEET ? EnumParts.FEET_LEFT : null;
				((ModelScaleRenderer) this.bipedLeftLeg).setOBJModel(stack, part);
				part = this.slot == EntityEquipmentSlot.FEET ? EnumParts.FEET_RIGHT : null;
				((ModelScaleRenderer) this.bipedRightLeg).setOBJModel(stack, part);
			}
			Minecraft.getMinecraft().renderEngine.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
		}
		List<Boolean> ba = ((EntityNPCInterface) entityIn).animation.showParts;
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
			if (ba.get(0)) {
				bipedHead.render(scale);
				bipedHeadwear.render(scale);
			}
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
		} else {
			if (entityIn.isSneaking()) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
			if (ba.get(0)) {
				bipedHead.render(scale);
				bipedHeadwear.render(scale);
			}
		}
		if (ba.get(3)) {
			bipedBody.render(scale);
		}
		if (ba.get(2)) {
			bipedRightArm.render(scale);
		}
		if (ba.get(1)) {
			bipedLeftArm.render(scale);
		}
		if (ba.get(5)) {
			bipedRightLeg.render(scale);
		}
		if (ba.get(4)) {
			bipedLeftLeg.render(scale);
		}
		GlStateManager.popMatrix();
	}

	private boolean setAnimationRotationAngles(EntityCustomNpc npc, AnimationConfig anim, float ageInTicks) {
		if (npc == null || anim == null) {
			((ModelScaleRenderer) this.bipedHead).clearAnim();
			((ModelScaleRenderer) this.bipedHeadwear).clearAnim();

			((ModelScaleRenderer) this.bipedBody).clearAnim();
			((ModelScaleRenderer) this.bipedLeftArm).clearAnim();
			((ModelScaleRenderer) this.bipedRightArm).clearAnim();

			((ModelScaleRenderer) this.bipedLeftLeg).clearAnim();
			((ModelScaleRenderer) this.bipedRightLeg).clearAnim();
			return false;
		}
		float pt = 0.0f;
		Minecraft mc = Minecraft.getMinecraft();
		if (!(mc.currentScreen instanceof GuiIngameMenu)) {
			pt = mc.getRenderPartialTicks();
		}
		Map<Integer, Float[]> animData = npc.animation.getValues(npc, anim, pt);
		if (animData == null) {
			return false;
		}
		// Head
		Float[] head = animData.get(0);
		if (head != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedHead).clearRotation();
				((ModelScaleRenderer) this.bipedHeadwear).clearRotation();
			}
			((ModelScaleRenderer) this.bipedHead).setAnim(head);
			((ModelScaleRenderer) this.bipedHeadwear).setAnim(head);
		}

		// Left Arm
		Float[] leftArm = animData.get(1);
		if (leftArm != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedLeftArm).clearRotation();
			}
			((ModelScaleRenderer) this.bipedLeftArm).setAnim(leftArm);
			if (npc.display.getHasLivingAnimation() && (anim.type == AnimationKind.STANDING
					|| anim.type == AnimationKind.FLY_STAND || anim.type == AnimationKind.WATER_STAND)) {
				this.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
				this.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
			}
		}
		// Right Arm
		Float[] rightArm = animData.get(2);
		if (rightArm != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedRightArm).clearRotation();
			}
			((ModelScaleRenderer) this.bipedRightArm).setAnim(rightArm);
			if (npc.display.getHasLivingAnimation()) {
				this.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
				this.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
			}
		}
		// Body
		Float[] body = animData.get(3);
		if (body != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedBody).clearRotation();
			}
			((ModelScaleRenderer) this.bipedBody).setAnim(body);
		}
		// Left Leg
		Float[] leftLeg = animData.get(4);
		if (leftLeg != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedLeftLeg).clearRotation();
			}
			((ModelScaleRenderer) this.bipedLeftLeg).setAnim(leftLeg);
		}

		// Right Leg
		Float[] rightLeg = animData.get(5);
		if (rightLeg != null) {
			if (anim.type.isMoving()) {
				((ModelScaleRenderer) this.bipedRightLeg).clearRotation();
			}
			((ModelScaleRenderer) this.bipedRightLeg).setAnim(rightLeg);
		}
		return true;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw,
			float headPitch, float scaleFactor, Entity entityIn) {
		if (!(entityIn instanceof EntityCustomNpc)) {
			super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn);
			return;
		}
		EntityCustomNpc npc = (EntityCustomNpc) entityIn;
		ModelData playerdata = npc.modelData;
		for (EnumParts part : this.map.keySet()) {
			ModelPartConfig config = playerdata.getPartConfig(part);
			for (ModelScaleRenderer model : this.map.get(part)) {
				model.config = config;
			}
		}
		if (!this.isRiding) {
			this.isRiding = (npc.currentAnimation == 1);
		}
		if (this.isSneak && npc.isPlayerSleeping()) {
			this.isSneak = false;
		}
		if (npc.currentAnimation == 6) {
			this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
		}
		this.isSneak = npc.isSneaking();
		this.bipedBody.rotationPointZ = 0.0f;
		this.bipedBody.rotationPointY = 0.0f;
		this.bipedBody.rotationPointX = 0.0f;
		this.bipedBody.rotateAngleZ = 0.0f;
		this.bipedBody.rotateAngleY = 0.0f;
		this.bipedBody.rotateAngleX = 0.0f;
		this.bipedHead.rotateAngleX = 0.0f;
		this.bipedHead.rotateAngleZ = 0.0f;
		this.bipedHead.rotationPointX = 0.0f;
		this.bipedHead.rotationPointY = 0.0f;
		this.bipedHead.rotationPointZ = 0.0f;
		this.bipedHeadwear.rotationPointY = 0.0f;
		this.bipedHeadwear.rotationPointX = 0.0f;
		this.bipedHeadwear.rotateAngleZ = 0.0f;
		this.bipedHeadwear.rotateAngleX = 0.0f;
		this.bipedHeadwear.rotationPointZ = 0.0f;
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
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
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
			AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 7) {
			AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 10) {
			AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 5) {
			AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 11) {
			AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 13) {
			AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 12) {
			AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (npc.currentAnimation == 8) {
			AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor,
					entityIn, (ModelBiped) this);
		} else if (this.isSneak) {
			this.bipedBody.rotateAngleX = 0.5f / playerdata.getPartConfig(EnumParts.BODY).scaleY;
		}
		AnimationConfig anim = npc.animation.activeAnim != null
				? npc.animation.getActiveAnimation(npc.animation.activeAnim.type)
				: null;
		// Dies
		if (npc.isKilled() && !npc.stats.hideKilledBody) {
			if (anim == null || anim.type != AnimationKind.DIES) {
				anim = npc.animation.getActiveAnimation(AnimationKind.DIES);
			}
		} else {
			if (anim != null && anim.type == AnimationKind.DIES && npc.animation.isComplete && !npc.isKilled()
					|| npc.stats.hideKilledBody) {
				anim = null;
				npc.animation.isComplete = false;
			}
			// Hit
			if (npc.hurtTime > 0 && npc.hurtTime == npc.maxHurtTime && npc.getHealth() != 0) {
				anim = npc.animation.getActiveAnimation(AnimationKind.HIT);
			}
			if (anim == null || anim.type != AnimationKind.INIT) {
				this.isAttaking.put(npc, false);
				if (!this.isJump.containsKey(npc)) {
					this.isJump.put(npc, false);
				}

				if (this.isAttaking.get(npc) && this.swingProgress > 0) {
					npc.swingProgress = 0.0f;
					npc.swingProgressInt = 5;
				}
				// Swing
				if (this.swingProgress > 0) {
					anim = npc.animation.getActiveAnimation(AnimationKind.ATTACKING);
					if (anim != null) {
						npc.swingProgress = 0.0f;
						npc.swingProgressInt = 5;
						this.isAttaking.put(npc, true);
					}
				}
				// Jump
				if (!this.isJump.get(npc) && !(npc.isInWater() || npc.isInLava())) {
					if (!npc.onGround && npc.motionY > 0.0d) {
						anim = npc.animation.getActiveAnimation(AnimationKind.JUMP);
						if (anim != null) {
							this.isJump.put(npc, true);
						}
					}
				} else if (npc.onGround) {
					this.isJump.put(npc, false);
				}
			}
			// INIT started in EntityNPCInterface.reset()
			if (anim == null || !anim.isEdit) {
				// Moving or Standing
				if (anim == null) {
					boolean isNavigate = npc.navigating != null || npc.motionX != 0.0d || npc.motionZ != 0.0d;
					// Revenge Target
					if (npc.isAttacking()) {
						if (isNavigate && (anim == null || anim.type != AnimationKind.REVENGE_WALK)) {
							anim = npc.animation.getActiveAnimation(AnimationKind.REVENGE_WALK);
						} else if (!isNavigate && (anim == null || anim.type != AnimationKind.REVENGE_STAND)) {
							anim = npc.animation.getActiveAnimation(AnimationKind.REVENGE_STAND);
						}
					} else {
						if (npc.isInWater() || npc.isInLava()) {
							if (isNavigate && (anim == null || anim.type != AnimationKind.WATER_WALK)) {
								anim = npc.animation.getActiveAnimation(AnimationKind.WATER_WALK);
							} else if (!isNavigate && (anim == null || anim.type != AnimationKind.WATER_STAND)) {
								anim = npc.animation.getActiveAnimation(AnimationKind.WATER_STAND);
							}
						} else {
							if (!npc.onGround && npc.ais.getNavigationType() == 1) {
								if (isNavigate && (anim == null || anim.type != AnimationKind.FLY_WALK)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.FLY_WALK);
								} else if (!isNavigate && (anim == null || anim.type != AnimationKind.FLY_STAND)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.FLY_STAND);
								}
							} else {
								if (isNavigate && (anim == null || anim.type != AnimationKind.WALKING)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.WALKING);
								} else if (!isNavigate && (anim == null || anim.type != AnimationKind.STANDING)) {
									anim = npc.animation.getActiveAnimation(AnimationKind.STANDING);
								}
							}
						}
					}
				}
			}
		}
		npc.animation.activeAnim = anim;
		npc.animation.isAnimated = this.setAnimationRotationAngles(npc, anim, ageInTicks);
	}

	public ModelBiped setShowSlot(EntityEquipmentSlot slot) {
		this.slot = slot;
		return this;
	}

}
