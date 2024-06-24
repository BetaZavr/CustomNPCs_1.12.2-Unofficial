package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.ModelRendererAlt;

public class AniWaving {
	
	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6, Entity entity, ModelBiped model) {
		float f = MathHelper.sin(entity.ticksExisted * 0.27f);
		float f2 = MathHelper.sin((entity.ticksExisted + 1) * 0.27f);
		f += (f2 - f) * Minecraft.getMinecraft().getRenderPartialTicks();
		model.bipedRightArm.rotateAngleX = -0.1f;
		model.bipedRightArm.rotateAngleY = 0.0f;
		model.bipedRightArm.rotateAngleZ = 2.141592653589793f - f * 0.5f;
		if (model.bipedRightArm instanceof ModelRendererAlt) { ((ModelRendererAlt) model.bipedRightArm).setIsNormal(true); }
	}
}
