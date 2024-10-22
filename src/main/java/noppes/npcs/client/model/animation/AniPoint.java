package noppes.npcs.client.model.animation;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelRendererAlt;

public class AniPoint {

	public static void setRotationAngles(float ignoredLimbSwing, float ignoredLimbSwingAmount, float ignoredAgeInTicks, float netHeadYaw, float ignoredHeadPitch, float ignoredScale, Entity ignoredEntity, ModelBiped model) {
		model.bipedRightArm.rotateAngleX = -1.570796f;
		model.bipedRightArm.rotateAngleY = netHeadYaw / 57.295776f;
		model.bipedRightArm.rotateAngleZ = 0.0f;
		if (model.bipedRightArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedRightArm).setIsNormal(true); }
	}

}
