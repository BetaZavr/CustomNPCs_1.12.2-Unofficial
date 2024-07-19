package noppes.npcs.client.model.animation;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.ModelRendererAlt;

public class AniHug {
	
	public static void setRotationAngles(float ignoredLimbSwing, float ignoredLimbSwingAmount, float ageInTicks, float ignoredNetHeadYaw, float ignoredHeadPitch, float ignoredScale, Entity ignoredEntity, ModelBiped model) {
		float f6 = MathHelper.sin(model.swingProgress * 3.141593f);
		float f7 = MathHelper.sin((1.0f - (1.0f - model.swingProgress) * (1.0f - model.swingProgress)) * 3.141593f);
		if (model.bipedLeftArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedLeftArm).setIsNormal(true); }
		if (model.bipedRightArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedRightArm).setIsNormal(true); }
		model.bipedRightArm.rotateAngleZ = 0.0f;
		model.bipedLeftArm.rotateAngleZ = 0.0f;
		model.bipedRightArm.rotateAngleY = -(0.1f - f6 * 0.6f);
		model.bipedLeftArm.rotateAngleY = 0.1f;
		model.bipedRightArm.rotateAngleX = -1.570796f;
		model.bipedLeftArm.rotateAngleX = -1.570796f;
		model.bipedRightArm.rotateAngleX -= f6 * 1.2f - f7 * 0.4f;
		model.bipedRightArm.rotateAngleZ += MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
		model.bipedLeftArm.rotateAngleZ -= MathHelper.cos(ageInTicks * 0.09f) * 0.05f + 0.05f;
		model.bipedRightArm.rotateAngleX += MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
		model.bipedLeftArm.rotateAngleX -= MathHelper.sin(ageInTicks * 0.067f) * 0.05f;
	}
}
