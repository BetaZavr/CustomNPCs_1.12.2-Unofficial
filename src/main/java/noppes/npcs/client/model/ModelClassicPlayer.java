package noppes.npcs.client.model;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelClassicPlayer extends ModelNPCAlt {

	public ModelClassicPlayer(float scale) {
		super(scale, false);
	}

	@Override
	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity) {
		super.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		float j = 2.0f;
		if (entity.isSprinting()) {
			j = 1.0f;
		}
		ModelRenderer bipedRightArm = this.bipedRightArm;
		bipedRightArm.rotateAngleX += MathHelper.cos(par1 * 0.6662f + 3.1415927f) * j * par2;
		ModelRenderer bipedLeftArm = this.bipedLeftArm;
		bipedLeftArm.rotateAngleX += MathHelper.cos(par1 * 0.6662f) * j * par2;
		ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
		bipedLeftArm2.rotateAngleZ += (MathHelper.cos(par1 * 0.2812f) - 1.0f) * par2;
		ModelRenderer bipedRightArm2 = this.bipedRightArm;
		bipedRightArm2.rotateAngleZ += (MathHelper.cos(par1 * 0.2312f) + 1.0f) * par2;
		this.bipedLeftArmwear.rotateAngleX = this.bipedLeftArm.rotateAngleX;
		this.bipedLeftArmwear.rotateAngleY = this.bipedLeftArm.rotateAngleY;
		this.bipedLeftArmwear.rotateAngleZ = this.bipedLeftArm.rotateAngleZ;
		this.bipedRightArmwear.rotateAngleX = this.bipedRightArm.rotateAngleX;
		this.bipedRightArmwear.rotateAngleY = this.bipedRightArm.rotateAngleY;
		this.bipedRightArmwear.rotateAngleZ = this.bipedRightArm.rotateAngleZ;
	}
}
