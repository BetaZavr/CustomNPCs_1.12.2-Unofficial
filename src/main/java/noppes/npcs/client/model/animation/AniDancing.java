package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import noppes.npcs.client.model.ModelRendererAlt;

public class AniDancing {
	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity, ModelBiped model) {
		float dancing = entity.ticksExisted / 4.0f;
		float dancing2 = (entity.ticksExisted + 1) / 4.0f;
		dancing += (dancing2 - dancing) * Minecraft.getMinecraft().getRenderPartialTicks();
		float x = (float) Math.sin(dancing);
		float y = (float) Math.abs(Math.cos(dancing));
		float n = x * 0.75f;
		if (model.bipedLeftArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedLeftArm).setIsNormal(true); }
		if (model.bipedRightArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedRightArm).setIsNormal(true); }
		model.bipedHead.rotationPointX = n;
		model.bipedHeadwear.rotationPointX = n;
		float n2 = y * 1.25f - 0.02f;
		model.bipedHead.rotationPointY = n2;
		model.bipedHeadwear.rotationPointY = n2;
		float n3 = -y * 0.75f;
		model.bipedHead.rotationPointZ = n3;
		model.bipedHeadwear.rotationPointZ = n3;
		model.bipedLeftArm.rotationPointX += x * 0.25f;
		model.bipedLeftArm.rotationPointY += y * 1.25f;
		model.bipedRightArm.rotationPointX += x * 0.25f;
		model.bipedRightArm.rotationPointY += y * 1.25f;
		model.bipedBody.rotationPointX = x * 0.25f;
	}
}
