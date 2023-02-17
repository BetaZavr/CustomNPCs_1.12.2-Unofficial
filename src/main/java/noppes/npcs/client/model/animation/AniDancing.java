package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class AniDancing {
	public static void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity, ModelBiped model) {
		float dancing = entity.ticksExisted / 4.0f;
		float dancing2 = (entity.ticksExisted + 1) / 4.0f;
		dancing += (dancing2 - dancing) * Minecraft.getMinecraft().getRenderPartialTicks();
		float x = (float) Math.sin(dancing);
		float y = (float) Math.abs(Math.cos(dancing));
		ModelRenderer bipedHeadwear = model.bipedHeadwear;
		ModelRenderer bipedHead = model.bipedHead;
		float n = x * 0.75f;
		bipedHead.rotationPointX = n;
		bipedHeadwear.rotationPointX = n;
		ModelRenderer bipedHeadwear2 = model.bipedHeadwear;
		ModelRenderer bipedHead2 = model.bipedHead;
		float n2 = y * 1.25f - 0.02f;
		bipedHead2.rotationPointY = n2;
		bipedHeadwear2.rotationPointY = n2;
		ModelRenderer bipedHeadwear3 = model.bipedHeadwear;
		ModelRenderer bipedHead3 = model.bipedHead;
		float n3 = -y * 0.75f;
		bipedHead3.rotationPointZ = n3;
		bipedHeadwear3.rotationPointZ = n3;
		ModelRenderer bipedLeftArm = model.bipedLeftArm;
		bipedLeftArm.rotationPointX += x * 0.25f;
		ModelRenderer bipedLeftArm2 = model.bipedLeftArm;
		bipedLeftArm2.rotationPointY += y * 1.25f;
		ModelRenderer bipedRightArm = model.bipedRightArm;
		bipedRightArm.rotationPointX += x * 0.25f;
		ModelRenderer bipedRightArm2 = model.bipedRightArm;
		bipedRightArm2.rotationPointY += y * 1.25f;
		model.bipedBody.rotationPointX = x * 0.25f;
	}
}
