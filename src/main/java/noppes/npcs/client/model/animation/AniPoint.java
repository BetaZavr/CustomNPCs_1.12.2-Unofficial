package noppes.npcs.client.model.animation;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelRendererAlt;

public class AniPoint {
	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, ModelBiped model) {
		model.bipedRightArm.rotateAngleX = -1.570796f;
		model.bipedRightArm.rotateAngleY = par4 / 57.295776f;
		model.bipedRightArm.rotateAngleZ = 0.0f;
		if (model.bipedRightArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedRightArm).setIsNormal(true); }
	}
}
