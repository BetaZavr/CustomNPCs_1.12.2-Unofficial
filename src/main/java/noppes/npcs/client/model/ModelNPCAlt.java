package noppes.npcs.client.model;

import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Maps;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationStack;
import noppes.npcs.client.model.part.AnimData;
import noppes.npcs.client.model.part.head.ModelHeadwear;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

/* Используется в качестве модели NPC */
public class ModelNpcAlt
extends ModelPlayer {

	public static final AnimData editAnimDataSelect = new AnimData();
	
	public static void copyModelAngles(ModelRendererAlt source, ModelRendererAlt dest) {
		dest.copyModelAngles(source);
	}
	
	private ModelHeadwear bipedHeadwear_64, bipedHeadwear_128, bipedHeadwear_256, bipedHeadwear_512, bipedHeadwear_1024, bipedHeadwear_2048, bipedHeadwear_4096;
	private ModelRendererAlt bipedCape;
	public AnimationStack rightStackData = new AnimationStack();
	public AnimationStack leftStackData = new AnimationStack();

 	public ModelNpcAlt(float modelSize, boolean smallArmsIn) {
		super(modelSize, smallArmsIn);
		
		float wear = 0.25f;
		float handWidth = smallArmsIn ? 3.0f : 4.0f;
		this.bipedHead = new ModelRendererAlt(this, EnumParts.HEAD, 0, 0, true);
		((ModelRendererAlt) this.bipedHead).setBox(-4.0F, -8.0F, -4.0F, 8, 3 , 3, 2, 8, modelSize);
		this.bipedHead.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedHeadwear_64 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 64);
		this.bipedHeadwear_128 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 128);
		this.bipedHeadwear_256 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 256);
		this.bipedHeadwear_512 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 512);
		this.bipedHeadwear_1024 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 1024);
		this.bipedHeadwear_2048 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 2048);
		this.bipedHeadwear_4096 = new ModelHeadwear(this, EnumParts.HEAD, 32, 0, 4096);
		this.bipedBody = new ModelRendererAlt(this, EnumParts.BODY, 16, 16, true);
		((ModelRendererAlt) this.bipedBody).setBox(-4.0F, 0.0F, -2.0F, 8, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedBody.setRotationPoint(0.0F, 0.0F, 0.0F);
		this.bipedBodyWear = new ModelRendererAlt(this, EnumParts.BODY, 16, 32, true);
		((ModelRendererAlt) this.bipedBodyWear).setBox(-4.0F, 0.0F, -2.0F, 8, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
		this.bipedBodyWear.setRotationPoint(0.0F, 0.0F, 0.0F);
		
		this.bipedCape = new ModelRendererAlt(this, EnumParts.BODY, 0, 0, true);
		((ModelRendererAlt) this.bipedCape).setBox(-4.0F, 0.0F, -3.0F, 10, 10, 4, 1, 1, modelSize);
		this.bipedCape.setRotationPoint(0.0F, 0.0F, 0.0F);
		ObfuscationHelper.setValue(ModelPlayer.class, this, this.bipedCape, 5);
		
		this.bipedRightArm = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 16, false);
		((ModelRendererAlt) this.bipedRightArm).setBox(-3.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightArm.setRotationPoint(-5.0F, 2.0F, 0.0F);
		this.bipedRightArmwear = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 40, 32, false);
		((ModelRendererAlt) this.bipedRightArmwear).setBox(-3.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
		this.bipedRightArmwear.setRotationPoint(-5.0F, 2.0F, 10.0F);
		this.bipedRightLeg = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 16, false);
		((ModelRendererAlt) this.bipedRightLeg).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedRightLeg.setRotationPoint(-2.0F, 12.0F, 0.0F);
		this.bipedRightLegwear = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 32, false);
		((ModelRendererAlt) this.bipedRightLegwear).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
		this.bipedRightLegwear.setRotationPoint(-2.0F, 12.0F, 0.0F);
		this.bipedLeftArm = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 32, 48, false);
		((ModelRendererAlt) this.bipedLeftArm).setBox(-1.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftArm.setRotationPoint(5.0F, 2.0F, 0.0F);
		this.bipedLeftArmwear = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 48, 48, false);
		((ModelRendererAlt) this.bipedLeftArmwear).setBox(-1.0F, -2.0F, -2.0F, handWidth, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
		this.bipedLeftArmwear.setRotationPoint(5.0F, 2.0F, 0.0F);
		
		this.bipedLeftLeg = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 16, 48, false);
		((ModelRendererAlt) this.bipedLeftLeg).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize);
		this.bipedLeftLeg.setRotationPoint(2.0F, 12.0F, 0.0F);
		this.bipedLeftLegwear = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 0, 48, false);
		((ModelRendererAlt) this.bipedLeftLegwear).setBox(-2.0F, 0.0F, -2.0F, 4, 5.5f, 3.5f, 3.0f, 4, modelSize + wear);
		this.bipedLeftLegwear.setRotationPoint(2.0F, 12.0F, 0.0F);
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
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		EntityCustomNpc npc = (EntityCustomNpc) entityIn;
		if (npc.isSneaking()) { GlStateManager.translate(0.0f, 0.2f, 0.0f); }
		if (ModelNpcAlt.editAnimDataSelect.part != null && Minecraft.getMinecraft().currentScreen == null) { ModelNpcAlt.editAnimDataSelect.part = null; }
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		Map<EnumParts, Boolean> ba = Maps.<EnumParts, Boolean>newHashMap();
		ba.putAll(npc.animation.showParts);
		float r = 1.0f, g = 1.0f, b = 1.0f;
		if (npc.display.getTint() != 0xFFFFFF) {
			r = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
			g = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
			b = (float)(npc.display.getTint() & 255) / 255.0F;
		}
		int entitySkinTextureID = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
		GlStateManager.pushMatrix();
		if (ba.get(EnumParts.HEAD) && this.bipedHead.showModel) {
			((ModelRendererAlt) this.bipedHead).chechBacklightColor(r, g, b);
			if (this.isChild) {
				GlStateManager.scale(0.75F, 0.75F, 0.75F);
				GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
				this.bipedHead.render(scale);
				if (!((ModelRendererAlt) this.bipedHead).isOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
					if (w >= 4096) { this.bipedHeadwear_4096.render(scale); }
					else if (w >= 2048) { this.bipedHeadwear_2048.render(scale); }
					else if (w >= 1024) { this.bipedHeadwear_1024.render(scale); }
					else if (w >= 512) { this.bipedHeadwear_512.render(scale); }
					else if (w >= 256) { this.bipedHeadwear_256.render(scale); }
					else if (w >= 128) { this.bipedHeadwear_128.render(scale); }
					else { this.bipedHeadwear_64.render(scale); }
				}
				GlStateManager.popMatrix();
				GlStateManager.pushMatrix();
				GlStateManager.scale(0.5F, 0.5F, 0.5F);
				GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
			} else {
				if (entityIn.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
				this.bipedHead.render(scale);
				if (!((ModelRendererAlt) this.bipedHead).isOBJModel()) {
					GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
					int w = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
					if (w >= 4096) { this.bipedHeadwear_4096.render(scale); }
					else if (w >= 2048) { this.bipedHeadwear_2048.render(scale); }
					else if (w >= 1024) { this.bipedHeadwear_1024.render(scale); }
					else if (w >= 512) { this.bipedHeadwear_512.render(scale); }
					else if (w >= 256) { this.bipedHeadwear_256.render(scale); }
					else if (w >= 128) { this.bipedHeadwear_128.render(scale); }
					else { this.bipedHeadwear_64.render(scale); }
				}
			}
		}
		if (ba.get(EnumParts.BODY) && this.bipedBody.showModel) {
			((ModelRendererAlt) this.bipedBody).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedBody).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedBody.render(scale);
			if (this.bipedBodyWear != null && !((ModelRendererAlt) this.bipedBody).isOBJModel()) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
				this.bipedBodyWear.render(scale);
			}
		}
		if (ba.get(EnumParts.ARM_RIGHT) && this.bipedRightArm.showModel) {
			((ModelRendererAlt) this.bipedRightArm).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedRightArm).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedRightArm.render(scale);
			if (this.bipedRightArmwear != null && !((ModelRendererAlt) this.bipedLeftArm).isOBJModel()) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
				this.bipedRightArmwear.render(scale);
			}
		}
		if (ba.get(EnumParts.ARM_LEFT) && this.bipedLeftArm.showModel) {
			((ModelRendererAlt) this.bipedLeftArm).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedLeftArm).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedLeftArm.render(scale);
			if (this.bipedLeftArmwear != null && !((ModelRendererAlt) this.bipedLeftArm).isOBJModel()) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
				this.bipedLeftArmwear.render(scale);
			}
		}
		if (ba.get(EnumParts.LEG_RIGHT) && this.bipedRightLeg.showModel) {
			((ModelRendererAlt) this.bipedRightLeg).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedRightLeg).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedRightLeg.render(scale);
			if (this.bipedRightLegwear != null) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
				this.bipedRightLegwear.render(scale);
			}
		}
		if (ba.get(EnumParts.LEG_LEFT) && this.bipedLeftLeg.showModel) {
			((ModelRendererAlt) this.bipedLeftLeg).chechBacklightColor(r, g, b);
			if (!((ModelRendererAlt) this.bipedLeftLeg).isOBJModel()) { GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID); }
			this.bipedLeftLeg.render(scale);
			if (this.bipedLeftLegwear != null) {
				GL11.glBindTexture(GL11.GL_TEXTURE_2D, entitySkinTextureID);
				this.bipedLeftLegwear.render(scale);
			}
		}
		GlStateManager.popMatrix();
	}
	
	@Override
	public void renderCape(float scale) { this.bipedCape.render(scale); }
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		EntityNPCInterface npc = (EntityNPCInterface) entityIn;
		if (npc.navigating != null && (netHeadYaw < -2.0f || netHeadYaw > 2.0f)) {
			npc.turn(netHeadYaw / 3.0f, headPitch / 3.0f);
			ObfuscationHelper.setValue(EntityLivingBase.class, npc, npc.rotationYaw, 58);
			ObfuscationHelper.setValue(EntityLivingBase.class, npc, npc.rotationPitch, 59);
		}
		if (!this.isRiding) { this.isRiding = npc.currentAnimation == 1; }
		if (this.isSneak && npc.isPlayerSleeping()) { this.isSneak = false; }
		if (npc.currentAnimation == 6 || (npc.inventory.getProjectile() != null && npc.isAttacking() && npc.stats.ranged.getHasAimAnimation())) {
			this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW;
		}
		this.isSneak = npc.isSneaking();
		
		// Standart Rotation
		this.clearAllRotations();
		if (npc instanceof EntityCustomNpc) {
			this.bipedHead.showModel = true;
			this.bipedBody.showModel = true;
			this.bipedLeftArm.showModel = true;
			this.bipedRightArm.showModel = true;
			this.bipedLeftLeg.showModel = true;
			this.bipedRightLeg.showModel = true;
			((ModelRendererAlt) this.bipedHead).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.HEAD));
			((ModelRendererAlt) this.bipedBody).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.BODY));
			((ModelRendererAlt) this.bipedLeftArm).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.ARM_LEFT));
			((ModelRendererAlt) this.bipedRightArm).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.ARM_RIGHT));
			((ModelRendererAlt) this.bipedLeftLeg).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.LEG_LEFT));
			((ModelRendererAlt) this.bipedRightLeg).setBaseData(((EntityCustomNpc) npc).modelData.getPartConfig(EnumParts.LEG_RIGHT));
			this.rightStackData.clear();
			this.leftStackData.clear();
		}
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		
		if (!npc.animation.hasAnim()) {
			if (npc.getAttackTarget() == null) {
				if (npc.isPlayerSleeping()) {
					if (this.bipedHead.rotateAngleX < 0.0f) {
						this.bipedHead.rotateAngleX = 0.0f;
						this.bipedHeadwear.rotateAngleX = 0.0f;
					}
				} else if (npc.currentAnimation != 0) {
					if (npc.currentAnimation == 3) {
						AniHug.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 5) {
						AniDancing.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 7) {
						AniCrawling.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 8) {
						AniPoint.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 9) {
						float n6 = 0.7f;
						this.bipedHead.rotateAngleX = n6;
						this.bipedHeadwear.rotateAngleX = n6;
					} else if (npc.currentAnimation == 10) {
						AniWaving.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 11) {
						AniBow.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 12) {
						AniNo.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					} else if (npc.currentAnimation == 13) {
						AniYes.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn, (ModelBiped) this);
					}
				}
			}
			else if (npc.ais.getStandingType() == 4 && npc.lookat != null) {
				double d0 = npc.posX - npc.lookat.posX;
				double d1 = (npc.posY + (double) npc.getEyeHeight()) - (npc.lookat.posY + (double) npc.lookat.getEyeHeight());
				double d2 = npc.posZ - npc.lookat.posZ;
				double d3 = (double)MathHelper.sqrt(d0 * d0 + d2 * d2);
				float yaw = MathHelper.wrapDegrees(npc.rotationYawHead - (float)(MathHelper.atan2(d2, d0) * (180D / Math.PI)) - 90.0F);;
				float pitch = MathHelper.wrapDegrees(npc.rotationPitch + (float)(-(MathHelper.atan2(d1, d3) * (180D / Math.PI))));
				if (yaw < -45.0f) { yaw = -45.0f; }
				else if (yaw > 45.0f) { yaw = 45.0f; }
				this.bipedHead.rotateAngleY = (float) ((-yaw * Math.PI) / 180D);
				if (pitch < -45.0f) { pitch = -45.0f; }
				else if (pitch > 45.0f) { pitch = 45.0f; }
				this.bipedHead.rotateAngleX = (float) ((-pitch * Math.PI) / 180D);
			}
		} else {
			float partialTicks = 0.0f;
			Minecraft mc = Minecraft.getMinecraft();
			if (mc.currentScreen == null || mc.currentScreen.isFocused()) { partialTicks = mc.getRenderPartialTicks(); }
			npc.animation.setRotationAngles(this.swingProgress, partialTicks);
			if (npc.animation.showParts.get(EnumParts.HEAD)) { ((ModelRendererAlt) this.bipedHead).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.BODY)) { ((ModelRendererAlt) this.bipedBody).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.ARM_RIGHT)) {
				((ModelRendererAlt) this.bipedRightArm).setAnimation(npc.animation);
				this.rightStackData.setAnimation(npc.animation, EnumParts.RIGHT_STACK.patterns);
			}
			if (npc.animation.showParts.get(EnumParts.ARM_LEFT)) {
				((ModelRendererAlt) this.bipedLeftArm).setAnimation(npc.animation);
				this.leftStackData.setAnimation(npc.animation, EnumParts.LEFT_STACK.patterns);
			}
			if (npc.animation.showParts.get(EnumParts.LEG_RIGHT)) { ((ModelRendererAlt) this.bipedRightLeg).setAnimation(npc.animation); }
			if (npc.animation.showParts.get(EnumParts.LEG_LEFT)) { ((ModelRendererAlt) this.bipedLeftLeg).setAnimation(npc.animation); }
		}
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_64);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_128);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_256);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_512);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_1024);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_2048);
		copyModelAngles((ModelRendererAlt) this.bipedHead, (ModelRendererAlt) this.bipedHeadwear_4096);
		copyModelAngles((ModelRendererAlt) this.bipedBody, (ModelRendererAlt) this.bipedBodyWear);
		copyModelAngles((ModelRendererAlt) this.bipedRightArm, (ModelRendererAlt) this.bipedRightArmwear);
		copyModelAngles((ModelRendererAlt) this.bipedLeftArm, (ModelRendererAlt) this.bipedLeftArmwear);
		copyModelAngles((ModelRendererAlt) this.bipedRightLeg, (ModelRendererAlt) this.bipedRightLegwear);
		copyModelAngles((ModelRendererAlt) this.bipedLeftLeg, (ModelRendererAlt) this.bipedLeftLegwear);
	}

	@Override
	public void postRenderArm(float scale, EnumHandSide side) { // for ItemStacks
		super.postRenderArm(scale, side);
		ModelRendererAlt modelRenderer = (ModelRendererAlt) this.bipedRightArm;
		AnimationStack hundData = this.rightStackData;
		if (side == EnumHandSide.LEFT) {
			modelRenderer = (ModelRendererAlt) this.bipedLeftArm;
			hundData = this.leftStackData;
		}
		if (!hundData.showModel) { return; }
		if (!modelRenderer.isNormal) {
			if (modelRenderer.rotateAngleX1 != 0.0f) {
				float ofsY = modelRenderer.dy2 - modelRenderer.dy0;
				GlStateManager.translate(0.0f, 0.625f, 0.0f);
				float ofsZ = modelRenderer.rotateAngleX1 * (modelRenderer.dz / -2.0f) / (float) -Math.PI;
				GlStateManager.translate(0.0f, ofsY * -0.0625f, ofsZ * 0.0625f);
				GlStateManager.rotate(modelRenderer.rotateAngleX1 * 180.0f / (float) Math.PI, 1.0f, 0.0f, 0.0f);
				GlStateManager.translate(0.0f, ofsY * 0.0625f, ofsZ * -0.0625f);
				GlStateManager.translate(0.0f, -0.625f, 0.0f);
			}
			if (modelRenderer.rotateAngleY1 != 0.0f) {
				float ofs = (side == EnumHandSide.RIGHT ? -1.0f : 1.0f) * 0.0625f;
				GlStateManager.translate(ofs, 0.0f, 0.0f);
				GlStateManager.rotate(modelRenderer.rotateAngleY1 * -180.0f / (float) Math.PI, 0.0f, 1.0f, 0.0f);
				GlStateManager.translate(-ofs, 0.0f, 0.0f);
			}
		}
		if (hundData.partSets == null) { return; }
		if (hundData.partSets[3] != 0.0f || hundData.partSets[4] != 0.0f || hundData.partSets[5] != 0.0f) {
			GlStateManager.translate(hundData.partSets[3], hundData.partSets[4], hundData.partSets[5]);
		}
		if (hundData.partSets[2] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[2] * (180F / (float) Math.PI), 0.0F, 0.0F, 1.0F);
		}
		if (hundData.partSets[1] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[1] * (180F / (float) Math.PI), 0.0F, 1.0F, 0.0F);
		}
		if (hundData.partSets[0] != 0.0F) {
			GlStateManager.rotate(hundData.partSets[0] * (180F / (float) Math.PI), 1.0F, 0.0F, 0.0F);
		}
		if (hundData.partSets[6] != 1.0f || hundData.partSets[7] != 1.0f || hundData.partSets[8] != 1.0f) {
			GlStateManager.scale(hundData.partSets[6], hundData.partSets[7], hundData.partSets[7]);
		}
	}
	
	private void clearAllRotations() {
		// Head
		if (this.bipedHead.showModel) { 
			((ModelRendererAlt) this.bipedHead).clearRotations();
			this.bipedHeadwear.rotateAngleX = 0.0f;
			this.bipedHeadwear.rotateAngleZ = 0.0f;
		}
		// Body
		if (this.bipedBody.showModel) { ((ModelRendererAlt) this.bipedBody).clearRotations(); }
		// Arm left
		if (this.bipedLeftArm.showModel) { ((ModelRendererAlt) this.bipedLeftArm).clearRotations(); }
		// Arm right
		if (this.bipedRightArm.showModel) { ((ModelRendererAlt) this.bipedRightArm).clearRotations(); }
		// Leg left
		if (this.bipedLeftLeg.showModel) {
			((ModelRendererAlt) this.bipedLeftLeg).clearRotations();
			this.bipedLeftLeg.rotationPointX = 2.0f;
			this.bipedLeftLeg.rotationPointZ = 0.0f;
		}
		// Leg right
		if (this.bipedRightLeg.showModel) {
			((ModelRendererAlt) this.bipedRightLeg).clearRotations();
			this.bipedRightLeg.rotationPointX = -2.0f;
			this.bipedRightLeg.rotationPointZ = 0.0f;
		}
	}
	
}
