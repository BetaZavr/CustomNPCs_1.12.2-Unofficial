package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelSpiderLegs extends ModelRenderer {

	private final ModelBiped base;
	private final ModelRenderer spiderBody;
	private final ModelRenderer spiderLeg1;
	private final ModelRenderer spiderLeg2;
	private final ModelRenderer spiderLeg3;
	private final ModelRenderer spiderLeg4;
	private final ModelRenderer spiderLeg5;
	private final ModelRenderer spiderLeg6;
	private final ModelRenderer spiderLeg7;
	private final ModelRenderer spiderLeg8;
	private final ModelRenderer spiderNeck;

	public ModelSpiderLegs(ModelBiped base) {
		super(base);
		this.base = base;
		float var1 = 0.0f;
		byte var2 = 15;
		(this.spiderNeck = new ModelRenderer(base, 0, 0)).addBox(-3.0f, -3.0f, -3.0f, 6, 6, 6, var1);
		this.spiderNeck.setRotationPoint(0.0f, var2, 2.0f);
		this.addChild(this.spiderNeck);
		(this.spiderBody = new ModelRenderer(base, 0, 12)).addBox(-5.0f, -4.0f, -6.0f, 10, 8, 12, var1);
		this.spiderBody.setRotationPoint(0.0f, var2, 11.0f);
		this.addChild(this.spiderBody);
		(this.spiderLeg1 = new ModelRenderer(base, 18, 0)).addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg1.setRotationPoint(-4.0f, var2, 4.0f);
		this.addChild(this.spiderLeg1);
		(this.spiderLeg2 = new ModelRenderer(base, 18, 0)).addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg2.setRotationPoint(4.0f, var2, 4.0f);
		this.addChild(this.spiderLeg2);
		(this.spiderLeg3 = new ModelRenderer(base, 18, 0)).addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg3.setRotationPoint(-4.0f, var2, 3.0f);
		this.addChild(this.spiderLeg3);
		(this.spiderLeg4 = new ModelRenderer(base, 18, 0)).addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg4.setRotationPoint(4.0f, var2, 3.0f);
		this.addChild(this.spiderLeg4);
		(this.spiderLeg5 = new ModelRenderer(base, 18, 0)).addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg5.setRotationPoint(-4.0f, var2, 2.0f);
		this.addChild(this.spiderLeg5);
		(this.spiderLeg6 = new ModelRenderer(base, 18, 0)).addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg6.setRotationPoint(4.0f, var2, 2.0f);
		this.addChild(this.spiderLeg6);
		(this.spiderLeg7 = new ModelRenderer(base, 18, 0)).addBox(-15.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg7.setRotationPoint(-4.0f, var2, 1.0f);
		this.addChild(this.spiderLeg7);
		(this.spiderLeg8 = new ModelRenderer(base, 18, 0)).addBox(-1.0f, -1.0f, -1.0f, 16, 2, 2, var1);
		this.spiderLeg8.setRotationPoint(4.0f, var2, 1.0f);
		this.addChild(this.spiderLeg8);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(ModelData data, float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
		this.rotateAngleX = 0.0f;
		this.rotationPointY = 0.0f;
		this.rotationPointZ = 0.0f;
		this.spiderBody.rotationPointY = 15.0f;
		this.spiderBody.rotationPointZ = 11.0f;
		this.spiderNeck.rotateAngleX = 0.0f;
		float var8 = 0.7853982f;
		this.spiderLeg1.rotateAngleZ = -var8;
		this.spiderLeg2.rotateAngleZ = var8;
		this.spiderLeg3.rotateAngleZ = -var8 * 0.74f;
		this.spiderLeg4.rotateAngleZ = var8 * 0.74f;
		this.spiderLeg5.rotateAngleZ = -var8 * 0.74f;
		this.spiderLeg6.rotateAngleZ = var8 * 0.74f;
		this.spiderLeg7.rotateAngleZ = -var8;
		this.spiderLeg8.rotateAngleZ = var8;
		float var9 = -0.0f;
		float var10 = 0.3926991f;
		this.spiderLeg1.rotateAngleY = var10 * 2.0f + var9;
		this.spiderLeg2.rotateAngleY = -var10 * 2.0f - var9;
		this.spiderLeg3.rotateAngleY = var10 + var9;
		this.spiderLeg4.rotateAngleY = -var10 - var9;
		this.spiderLeg5.rotateAngleY = -var10 + var9;
		this.spiderLeg6.rotateAngleY = var10 - var9;
		this.spiderLeg7.rotateAngleY = -var10 * 2.0f + var9;
		this.spiderLeg8.rotateAngleY = var10 * 2.0f - var9;
		float var11 = -(MathHelper.cos(par1 * 0.6662f * 2.0f + 0.0f) * 0.4f) * par2;
		float var12 = -(MathHelper.cos(par1 * 0.6662f * 2.0f + 3.1415927f) * 0.4f) * par2;
		float var13 = -(MathHelper.cos(par1 * 0.6662f * 2.0f + 1.5707964f) * 0.4f) * par2;
		float var14 = -(MathHelper.cos(par1 * 0.6662f * 2.0f + 4.712389f) * 0.4f) * par2;
		float var15 = Math.abs(MathHelper.sin(par1 * 0.6662f + 0.0f) * 0.4f) * par2;
		float var16 = Math.abs(MathHelper.sin(par1 * 0.6662f + 3.1415927f) * 0.4f) * par2;
		float var17 = Math.abs(MathHelper.sin(par1 * 0.6662f + 1.5707964f) * 0.4f) * par2;
		float var18 = Math.abs(MathHelper.sin(par1 * 0.6662f + 4.712389f) * 0.4f) * par2;
		this.spiderLeg1.rotateAngleY += var11;
		this.spiderLeg2.rotateAngleY -= var11;
		this.spiderLeg3.rotateAngleY += var12;
		this.spiderLeg4.rotateAngleY -= var12;
		this.spiderLeg5.rotateAngleY += var13;
		this.spiderLeg6.rotateAngleY -= var13;
		this.spiderLeg7.rotateAngleY += var14;
		this.spiderLeg8.rotateAngleY -= var14;
		this.spiderLeg1.rotateAngleZ += var15;
		this.spiderLeg2.rotateAngleZ -= var15;
		this.spiderLeg3.rotateAngleZ += var16;
		this.spiderLeg4.rotateAngleZ -= var16;
		this.spiderLeg5.rotateAngleZ += var17;
		this.spiderLeg6.rotateAngleZ -= var17;
		this.spiderLeg7.rotateAngleZ += var18;
		this.spiderLeg8.rotateAngleZ -= var18;
		if (this.base.isSneak) {
			this.rotationPointZ = 5.0f;
			this.rotationPointY = -1.0f;
			this.spiderBody.rotationPointY = 16.0f;
			this.spiderBody.rotationPointZ = 10.0f;
			this.spiderNeck.rotateAngleX = -0.3926991f;
		}
		if (((EntityNPCInterface) entity).isPlayerSleeping() || ((EntityNPCInterface) entity).currentAnimation == 7) {
			this.rotationPointY = 12.0f * data.getPartConfig(EnumParts.LEG_LEFT).scale[1];
			this.rotationPointZ = 15.0f * data.getPartConfig(EnumParts.LEG_LEFT).scale[1];
			this.rotateAngleX = -1.5707964f;
		}
	}
}
