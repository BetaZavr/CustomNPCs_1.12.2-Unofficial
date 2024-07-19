package noppes.npcs.client.model;

import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class ModelNPCGolem extends ModelBipedAlt {

	private ModelRenderer bipedLowerBody;

	public ModelNPCGolem(float scale) {
		super(scale, false, false, false);
	}

	@Override
	protected void init(float modelSize) {
		float f = 0.0f;
		float f2 = -7.0f;
		// Head
		this.bipedHead = new ModelRendererAlt(this, EnumParts.HEAD, 0, 0, true);
		this.bipedHead.setTextureSize(128, 128);
		this.bipedHead.setRotationPoint(0.0f, f2, -2.0f);
		((ModelRendererAlt) this.bipedHead).setBox(-4.0f, -12.0f, -5.5f, 8.0f, 4.5f, 3.5f, 2.0f, 8.0f, f);
		ModelRendererAlt nose = new ModelRendererAlt(this, EnumParts.HEAD, 24, 0, true);
		nose.setTextureSize(128, 128);
		nose.setBox(-1.0f, -5.0f, -7.5f, 2.0f, 2.0f, 1.0f, 1.0f, 2.0f, f);
		this.bipedHead.childModels = Lists.newArrayList();
		this.bipedHead.childModels.add(nose);
		this.bipedHeadwear = new ModelRendererAlt(this, EnumParts.HEAD, 0, 85, true);
		this.bipedHeadwear.setTextureSize(128, 128);
		this.bipedHeadwear.setRotationPoint(0.0f, f2, -2.0f);
		((ModelRendererAlt) this.bipedHeadwear).setBox(-4.0f, -12.0f, -5.5f, 8.0f, 4.5f, 3.5f, 2.0f, 8.0f, f + 0.5f);

		// Body
		this.bipedBody = new ModelRendererAlt(this, EnumParts.BODY, 0, 40, true); // Top
		this.bipedBody.setTextureSize(128, 128);
		this.bipedBody.setRotationPoint(0.0f, f2, 0.0f);
		((ModelRendererAlt) this.bipedBody).setBox(-9.0f, -2.0f, -6.0f, 18.0f, 5.5f, 3.5f, 3.0f, 11.0f, f + 0.2f);
		ModelRendererAlt bodyTopWear = new ModelRendererAlt(this, EnumParts.BODY, 0, 21, true);
		bodyTopWear.setTextureSize(128, 128);
		bodyTopWear.setBox(-9.0f, -2.0f, -6.0f, 18.0f, 3.0f, 3.0f, 2.0f, 11.0f, f);
		this.bipedBody.childModels = Lists.newArrayList();
		this.bipedBody.childModels.add(bodyTopWear);

		this.bipedLowerBody = new ModelRendererAlt(this, EnumParts.BODY, 0, 70, true); // Lower
		this.bipedLowerBody.setTextureSize(128, 128);
		this.bipedLowerBody.setRotationPoint(0.0f, f2, 0.0f);
		((ModelRendererAlt) this.bipedLowerBody).setBox(-4.5f, 10.0f, -3.0f, 9.0f, 2.5f, 1.5f, 1.0f, 6.0f, f + 0.5f);
		ModelRendererAlt bodyLowerWear = new ModelRendererAlt(this, EnumParts.BODY, 30, 70, true);
		bodyLowerWear.setTextureSize(128, 128);
		bodyLowerWear.setBox(-4.5f, 6.0f, -3.0f, 9.0f, 4.5f, 3.5f, 1.0f, 6.0f, f + 0.4f);
		this.bipedLowerBody.childModels = Lists.newArrayList();
		this.bipedLowerBody.childModels.add(bodyLowerWear);

		// Right Arm
		this.bipedRightArm = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 60, 21, true);
		this.bipedRightArm.setTextureSize(128, 128);
		((ModelRendererAlt) this.bipedRightArm).setBox(-13.0f, -2.5f, -3.0f, 4.0f, 14.0f, 12.0f, 4.0f, 6.0f, f + 0.2f);
		this.bipedRightArm.setRotationPoint(0.0f, f2, 0.0f);
		ModelRendererAlt rightArmTop = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 80, 21, true);
		rightArmTop.setTextureSize(128, 128);
		rightArmTop.setBox(-13.0f, -2.5f, -3.0f, 4.0f, 9.0f, 7.0f, 4.0f, 6.0f, f);
		ModelRendererAlt rightArmTopWear = new ModelRendererAlt(this, EnumParts.ARM_RIGHT, 100, 21, true);
		rightArmTopWear.setTextureSize(128, 128);
		rightArmTopWear.setBox(-13.0f, -2.5f, -3.0f, 4.0f, 9.0f, 7.0f, 4.0f, 6.0f, f + 1.0f);
		this.bipedRightArm.childModels = Lists.newArrayList();
		this.bipedRightArm.childModels.add(rightArmTop);
		this.bipedRightArm.childModels.add(rightArmTopWear);

		// Left Arm
		this.bipedLeftArm = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 60, 58, true);
		this.bipedLeftArm.setTextureSize(128, 128);
		((ModelRendererAlt) this.bipedLeftArm).setBox(9.0f, -2.5f, -3.0f, 4.0f, 14.0f, 12.0f, 4.0f, 6.0f, f + 0.2f);
		this.bipedLeftArm.setRotationPoint(0.0f, f2, 0.0f);
		ModelRendererAlt leftArmTop = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 80, 58, true);
		leftArmTop.setTextureSize(128, 128);
		leftArmTop.setBox(9.0f, -2.5f, -3.0f, 4.0f, 9.0f, 7.0f, 4.0f, 6.0f, f);
		ModelRendererAlt leftArmTopWear = new ModelRendererAlt(this, EnumParts.ARM_LEFT, 100, 58, true);
		leftArmTopWear.setTextureSize(128, 128);
		leftArmTopWear.setBox(9.0f, -2.5f, -3.0f, 4.0f, 9.0f, 7.0f, 4.0f, 6.0f, f + 1.0f);
		this.bipedLeftArm.childModels = Lists.newArrayList();
		this.bipedLeftArm.childModels.add(leftArmTop);
		this.bipedLeftArm.childModels.add(leftArmTopWear);

		// Right Leg
		this.bipedRightLeg = new ModelRendererAlt(this, EnumParts.LEG_RIGHT, 60, 0, true);
		this.bipedRightLeg.setTextureSize(128, 128);
		this.bipedRightLeg.mirror = true;
		((ModelRendererAlt) this.bipedRightLeg).setBox(-3.5f, -3.0f, -3.0f, 6.0f, 7.0f, 6.0f, 3.0f, 5.0f, f);
		this.bipedRightLeg.setRotationPoint(5.0f, 18.0f + f2, 0.0f);

		// Left Leg
		this.bipedLeftLeg = new ModelRendererAlt(this, EnumParts.LEG_LEFT, 37, 0, true);
		this.bipedLeftLeg.setTextureSize(128, 128);
		((ModelRendererAlt) this.bipedLeftLeg).setBox(-3.5f, -3.0f, -3.0f, 6.0f, 7.0f, 6.0f, 3.0f, 5.0f, f);
		this.bipedLeftLeg.setRotationPoint(-4.0f, 18.0f + f2, 0.0f);
	}

	public void render(@Nonnull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		super.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		float r = 1.0f, g = 1.0f, b = 1.0f;
		Map<EnumParts, Boolean> ba = Maps.newHashMap();
		if (entityIn instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entityIn;
			ba.putAll(npc.animation.showParts);
			if (npc.display.getTint() != 0xFFFFFF) {
				r = (float)(npc.display.getTint() >> 16 & 255) / 255.0F;
				g = (float)(npc.display.getTint() >> 8 & 255) / 255.0F;
				b = (float)(npc.display.getTint() & 255) / 255.0F;
			}
		}
		if (ba.get(EnumParts.BODY) && this.bipedLowerBody.showModel) {
			((ModelRendererAlt) this.bipedLowerBody).checkBacklightColor(r, g, b);
			this.bipedLowerBody.render(scale);
		}
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, @Nonnull Entity entity) {
		EntityNPCInterface npc = (EntityNPCInterface) entity;
		this.isRiding = npc.isRiding();
		if (this.isSneak && (npc.currentAnimation == 7 || npc.currentAnimation == 2)) {
			this.isSneak = false;
		}
		this.bipedHead.rotateAngleY = par4 / 57.295776f;
		this.bipedHead.rotateAngleX = par5 / 57.295776f;
		this.bipedHeadwear.rotateAngleY = this.bipedHead.rotateAngleY;
		this.bipedHeadwear.rotateAngleX = this.bipedHead.rotateAngleX;
		this.bipedLeftLeg.rotateAngleX = -1.5f * this.triangleWave(par1) * par2;
		this.bipedRightLeg.rotateAngleX = 1.5f * this.triangleWave(par1) * par2;
		this.bipedLeftLeg.rotateAngleY = 0.0f;
		this.bipedRightLeg.rotateAngleY = 0.0f;
		float f6 = MathHelper.sin(this.swingProgress * 3.1415927f);
		float f7 = MathHelper.sin((16.0f - (1.0f - this.swingProgress) * (1.0f - this.swingProgress)) * 3.1415927f);
		if (this.swingProgress > 0.0) {
			this.bipedRightArm.rotateAngleZ = 0.0f;
			this.bipedLeftArm.rotateAngleZ = 0.0f;
			this.bipedRightArm.rotateAngleY = -(0.1f - f6 * 0.6f);
			this.bipedLeftArm.rotateAngleY = 0.1f - f6 * 0.6f;
			this.bipedRightArm.rotateAngleX = -1.5707964f;
			this.bipedLeftArm.rotateAngleX = -1.5707964f;
			ModelRenderer bipedRightArm = this.bipedRightArm;
			bipedRightArm.rotateAngleX -= f6 * 1.2f - f7 * 0.4f;
			ModelRenderer bipedLeftArm = this.bipedLeftArm;
			bipedLeftArm.rotateAngleX -= f6 * 1.2f - f7 * 0.4f;
		} else if (this.rightArmPose == ModelBiped.ArmPose.BOW_AND_ARROW) {
			float f8 = 0.0f;
			float f9 = 0.0f;
			this.bipedRightArm.rotateAngleZ = 0.0f;
			this.bipedRightArm.rotateAngleX = -1.5707964f + this.bipedHead.rotateAngleX;
			ModelRenderer bipedRightArm2 = this.bipedRightArm;
			bipedRightArm2.rotateAngleX -= f8 * 1.2f - f9 * 0.4f;
			ModelRenderer bipedRightArm3 = this.bipedRightArm;
			bipedRightArm3.rotateAngleZ += MathHelper.cos(par3 * 0.09f) * 0.05f + 0.05f;
			ModelRenderer bipedRightArm4 = this.bipedRightArm;
			bipedRightArm4.rotateAngleX += MathHelper.sin(par3 * 0.067f) * 0.05f;
			this.bipedLeftArm.rotateAngleX = (-0.2f - 1.5f * this.triangleWave(par1)) * par2;
			this.bipedBody.rotateAngleY = -(0.1f - f8 * 0.6f) + this.bipedHead.rotateAngleY;
			this.bipedRightArm.rotateAngleY = -(0.1f - f8 * 0.6f) + this.bipedHead.rotateAngleY;
			this.bipedLeftArm.rotateAngleY = 0.1f - f8 * 0.6f + this.bipedHead.rotateAngleY;
		} else {
			this.bipedRightArm.rotateAngleX = (-0.2f + 1.5f * this.triangleWave(par1)) * par2;
			this.bipedLeftArm.rotateAngleX = (-0.2f - 1.5f * this.triangleWave(par1)) * par2;
			this.bipedBody.rotateAngleY = 0.0f;
			this.bipedRightArm.rotateAngleY = 0.0f;
			this.bipedLeftArm.rotateAngleY = 0.0f;
			this.bipedRightArm.rotateAngleZ = 0.0f;
			this.bipedLeftArm.rotateAngleZ = 0.0f;
		}
		if (this.isRiding) {
			ModelRenderer bipedRightArm5 = this.bipedRightArm;
			bipedRightArm5.rotateAngleX -= 0.62831855f;
			ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
			bipedLeftArm2.rotateAngleX -= 0.62831855f;
			this.bipedLeftLeg.rotateAngleX = -1.2566371f;
			this.bipedRightLeg.rotateAngleX = -1.2566371f;
			this.bipedLeftLeg.rotateAngleY = 0.31415927f;
			this.bipedRightLeg.rotateAngleY = -0.31415927f;
		}
	}

	private float triangleWave(float par1) {
		return (Math.abs(par1 % 13.0F - 6.5f) - 3.25f) / 3.25f;
	}
}
