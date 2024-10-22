package noppes.npcs.client.model.animation;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.ModelNpcAlt;

public class AniClassicPlayer {

    public static void setRotationAngles(float limbSwing, float limbSwingAmount, float ignoredAgeInTicks, float ignoredNetHeadYaw, float ignoredHeadPitch, float ignoredScale, Entity entityIn, ModelNpcAlt model) {
        float j = 2.0f;
        if (entityIn.isSprinting()) { j = 1.0f; }
        ModelRenderer bipedRightArm = model.bipedRightArm;
        bipedRightArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662f + 3.1415927f) * j * limbSwingAmount;
        ModelRenderer bipedLeftArm = model.bipedLeftArm;
        bipedLeftArm.rotateAngleX += MathHelper.cos(limbSwing * 0.6662f) * j * limbSwingAmount;
        ModelRenderer bipedLeftArm2 = model.bipedLeftArm;
        bipedLeftArm2.rotateAngleZ += (MathHelper.cos(limbSwing * 0.2812f) - 1.0f) * limbSwingAmount;
        ModelRenderer bipedRightArm2 = model.bipedRightArm;
        bipedRightArm2.rotateAngleZ += (MathHelper.cos(limbSwing * 0.2312f) + 1.0f) * limbSwingAmount;
        model.bipedLeftArmwear.rotateAngleX = model.bipedLeftArm.rotateAngleX;
        model.bipedLeftArmwear.rotateAngleY = model.bipedLeftArm.rotateAngleY;
        model.bipedLeftArmwear.rotateAngleZ = model.bipedLeftArm.rotateAngleZ;
        model.bipedRightArmwear.rotateAngleX = model.bipedRightArm.rotateAngleX;
        model.bipedRightArmwear.rotateAngleY = model.bipedRightArm.rotateAngleY;
        model.bipedRightArmwear.rotateAngleZ = model.bipedRightArm.rotateAngleZ;
    }

}
