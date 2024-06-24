package noppes.npcs.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelClassicPlayer extends ModelPlayer {

	public ModelClassicPlayer(float scale) {
		super(scale, false);
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		super.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor, entityIn);
		float j = 2.0f;
		if (entityIn.isSprinting()) { j = 1.0f; }
		ModelRenderer bipedRightArm = this.bipedRightArm;
		bipedRightArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662f + 3.1415927f) * j * limbSwingAmount;
		ModelRenderer bipedLeftArm = this.bipedLeftArm;
		bipedLeftArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662f) * j * limbSwingAmount;
		ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
		bipedLeftArm2.rotateAngleZ += (MathHelper.cos(limbSwing * 0.2812f) - 1.0f) * limbSwingAmount;
		ModelRenderer bipedRightArm2 = this.bipedRightArm;
		bipedRightArm2.rotateAngleZ += (MathHelper.cos(limbSwing * 0.2312f) + 1.0f) * limbSwingAmount;
		this.bipedLeftArmwear.rotateAngleX = this.bipedLeftArm.rotateAngleX;
		this.bipedLeftArmwear.rotateAngleY = this.bipedLeftArm.rotateAngleY;
		this.bipedLeftArmwear.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
		this.bipedRightArmwear.rotateAngleX = this.bipedRightArm.rotateAngleX;
		this.bipedRightArmwear.rotateAngleY = this.bipedRightArm.rotateAngleY;
		this.bipedRightArmwear.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
	}
}
