package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;

public class AniBow {
	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity, ModelBiped model) {
		float ticks = (entity.ticksExisted - ((EntityNPCInterface) entity).animationStart) / 10.0f;
		if (ticks > 1.0f) {
			ticks = 1.0f;
		}
		float ticks2 = (entity.ticksExisted + 1 - ((EntityNPCInterface) entity).animationStart) / 10.0f;
		if (ticks2 > 1.0f) {
			ticks2 = 1.0f;
		}
		ticks += (ticks2 - ticks) * Minecraft.getMinecraft().getRenderPartialTicks();
		model.bipedBody.rotateAngleX = ticks;
		model.bipedHead.rotateAngleX = ticks;
		model.bipedLeftArm.rotateAngleX = ticks;
		model.bipedRightArm.rotateAngleX = ticks;
		model.bipedBody.rotationPointZ = -ticks * 10.0f;
		model.bipedBody.rotationPointY = ticks * 6.0f;
		model.bipedHead.rotationPointZ = -ticks * 10.0f;
		model.bipedHead.rotationPointY = ticks * 6.0f;
		model.bipedLeftArm.rotationPointZ = -ticks * 10.0f;
		ModelRenderer bipedLeftArm = model.bipedLeftArm;
		bipedLeftArm.rotationPointY += ticks * 6.0f;
		model.bipedRightArm.rotationPointZ = -ticks * 10.0f;
		ModelRenderer bipedRightArm = model.bipedRightArm;
		bipedRightArm.rotationPointY += ticks * 6.0f;
	}
}
