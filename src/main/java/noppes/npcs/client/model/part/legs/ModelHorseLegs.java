package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.model.part.ModelData;

public class ModelHorseLegs extends ModelRenderer {
    private final ModelRenderer backLeftLeg;
    private final ModelRenderer backRightLeg;
    public final ModelBiped base;
    private final ModelRenderer frontLeftLeg;
    private final ModelRenderer frontRightLeg;

    public ModelHorseLegs(ModelBiped model) {
		super(model);
		this.base = model;
		int zOffset = 10;
		float yOffset = 7.0f;
		ModelRenderer body = new ModelRenderer(model, 0, 34);
		body.setTextureSize(128, 128);
		body.addBox(-5.0f, -8.0f, -19.0f, 10, 10, 24);
		body.setRotationPoint(0.0f, 11.0f + yOffset, 9.0f + zOffset);
		this.addChild(body);
		(this.backLeftLeg = new ModelRenderer(model, 78, 29)).setTextureSize(128, 128);
		this.backLeftLeg.addBox(-2.0f, -2.0f, -2.5f, 4, 9, 5);
		this.backLeftLeg.setRotationPoint(4.0f, 9.0f + yOffset, 11.0f + zOffset);
		this.addChild(this.backLeftLeg);
        ModelRenderer backLeftShin;
        (backLeftShin = new ModelRenderer(model, 78, 43)).setTextureSize(128, 128);
		backLeftShin.addBox(-1.5f, 0.0f, -1.5f, 3, 5, 3);
		backLeftShin.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.backLeftLeg.addChild(backLeftShin);
        ModelRenderer backLeftHoof;
        (backLeftHoof = new ModelRenderer(model, 78, 51)).setTextureSize(128, 128);
		backLeftHoof.addBox(-2.0f, 5.0f, -2.0f, 4, 3, 4);
		backLeftHoof.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.backLeftLeg.addChild(backLeftHoof);
		(this.backRightLeg = new ModelRenderer(model, 96, 29)).setTextureSize(128, 128);
		this.backRightLeg.addBox(-2.0f, -2.0f, -2.5f, 4, 9, 5);
		this.backRightLeg.setRotationPoint(-4.0f, 9.0f + yOffset, 11.0f + zOffset);
		this.addChild(this.backRightLeg);
        ModelRenderer backRightShin;
        (backRightShin = new ModelRenderer(model, 96, 43)).setTextureSize(128, 128);
		backRightShin.addBox(-1.5f, 0.0f, -1.5f, 3, 5, 3);
		backRightShin.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.backRightLeg.addChild(backRightShin);
        ModelRenderer backRightHoof;
        (backRightHoof = new ModelRenderer(model, 96, 51)).setTextureSize(128, 128);
		backRightHoof.addBox(-2.0f, 5.0f, -2.0f, 4, 3, 4);
		backRightHoof.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.backRightLeg.addChild(backRightHoof);
		(this.frontLeftLeg = new ModelRenderer(model, 44, 29)).setTextureSize(128, 128);
		this.frontLeftLeg.addBox(-1.4f, -1.0f, -2.1f, 3, 8, 4);
		this.frontLeftLeg.setRotationPoint(4.0f, 9.0f + yOffset, -8.0f + zOffset);
		this.addChild(this.frontLeftLeg);
        ModelRenderer frontLeftShin;
        (frontLeftShin = new ModelRenderer(model, 44, 41)).setTextureSize(128, 128);
		frontLeftShin.addBox(-1.4f, 0.0f, -1.6f, 3, 5, 3);
		frontLeftShin.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.frontLeftLeg.addChild(frontLeftShin);
        ModelRenderer frontLeftHoof;
        (frontLeftHoof = new ModelRenderer(model, 44, 51)).setTextureSize(128, 128);
		frontLeftHoof.addBox(-1.9f, 5.0f, -2.1f, 4, 3, 4);
		frontLeftHoof.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.frontLeftLeg.addChild(frontLeftHoof);
		(this.frontRightLeg = new ModelRenderer(model, 60, 29)).setTextureSize(128, 128);
		this.frontRightLeg.addBox(-1.6f, -1.0f, -2.1f, 3, 8, 4);
		this.frontRightLeg.setRotationPoint(-4.0f, 9.0f + yOffset, -8.0f + zOffset);
		this.addChild(this.frontRightLeg);
        ModelRenderer frontRightShin;
        (frontRightShin = new ModelRenderer(model, 60, 41)).setTextureSize(128, 128);
		frontRightShin.addBox(-1.6f, 0.0f, -1.6f, 3, 5, 3);
		frontRightShin.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.frontRightLeg.addChild(frontRightShin);
        ModelRenderer frontRightHoof;
        (frontRightHoof = new ModelRenderer(model, 60, 51)).setTextureSize(128, 128);
		frontRightHoof.addBox(-2.1f, 5.0f, -2.1f, 4, 3, 4);
		frontRightHoof.setRotationPoint(0.0f, 7.0f, 0.0f);
		this.frontRightLeg.addChild(frontRightHoof);
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(ModelData data, float par1, float par2, float par3, float par4, float par5, float par6, Entity entity) {
		this.frontLeftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662f) * 0.4f * par2;
		this.frontRightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662f + 3.1415927f) * 0.4f * par2;
		this.backLeftLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662f + 3.1415927f) * 0.4f * par2;
		this.backRightLeg.rotateAngleX = MathHelper.cos(par1 * 0.6662f) * 0.4f * par2;
	}
}
