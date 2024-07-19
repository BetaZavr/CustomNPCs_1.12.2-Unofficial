package noppes.npcs.client.model.animation;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.entity.Entity;
import noppes.npcs.entity.EntityNPCInterface;

public class AniYes {

	public static void setRotationAngles(float ignoredLimbSwing, float ignoredLimbSwingAmount, float ignoredAgeInTicks, float ignoredNetHeadYaw, float ignoredHeadPitch, float ignoredScale, Entity entity, ModelBiped model) {
		float ticks = (entity.ticksExisted - ((EntityNPCInterface) entity).animationStart) / 8.0f;
		float ticks2 = (entity.ticksExisted + 1 - ((EntityNPCInterface) entity).animationStart) / 8.0f;
		ticks += (ticks2 - ticks) * Minecraft.getMinecraft().getRenderPartialTicks();
		ticks %= 2.0f;
		float ani = ticks - 0.5f;
		if (ticks > 1.0f) {
			ani = 1.5f - ticks;
		}
		model.bipedHead.rotateAngleX = ani;
	}
}
