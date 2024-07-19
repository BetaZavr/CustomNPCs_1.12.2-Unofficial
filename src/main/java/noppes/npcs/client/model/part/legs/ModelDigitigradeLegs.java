package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelDigitigradeLegs extends ModelRenderer {
	private final ModelBiped base;
    private final ModelRenderer leftleg;
    private final ModelRenderer rightleg;

    public ModelDigitigradeLegs(ModelBiped base) {
		super(base);
		this.base = base;
		(this.rightleg = new ModelRenderer(base, 0, 16)).addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4);
		this.rightleg.setRotationPoint(-2.1f, 11.0f, 0.0f);
		this.setRotation(this.rightleg, -0.3f);
		this.addChild(this.rightleg);
        ModelRenderer rightleg2;
        (rightleg2 = new ModelRenderer(base, 0, 20)).addBox(-1.5f, -1.0f, -2.0f, 3, 7, 3);
		rightleg2.setRotationPoint(0.0f, 4.1f, 0.0f);
		this.setRotation(rightleg2, 1.1f);
		this.rightleg.addChild(rightleg2);
        ModelRenderer rightleglow;
        (rightleglow = new ModelRenderer(base, 0, 24)).addBox(-1.5f, 0.0f, -1.0f, 3, 5, 2);
		rightleglow.setRotationPoint(0.0f, 5.0f, 0.0f);
		this.setRotation(rightleglow, -1.35f);
		rightleg2.addChild(rightleglow);
        ModelRenderer rightfoot;
        (rightfoot = new ModelRenderer(base, 1, 26)).addBox(-1.5f, 0.0f, -5.0f, 3, 2, 4);
		rightfoot.setRotationPoint(0.0f, 3.7f, 1.2f);
		this.setRotation(rightfoot, 0.55f);
		rightleglow.addChild(rightfoot);
		this.leftleg = new ModelRenderer(base, 0, 16);
		this.leftleg.mirror = true;
		this.leftleg.addBox(-2.0f, 0.0f, -2.0f, 4, 6, 4);
		this.leftleg.setRotationPoint(2.1f, 11.0f, 0.0f);
		this.setRotation(this.leftleg, -0.3f);
		this.addChild(this.leftleg);
        ModelRenderer leftleg2 = new ModelRenderer(base, 0, 20);
		leftleg2.mirror = true;
		leftleg2.addBox(-1.5f, -1.0f, -2.0f, 3, 7, 3);
		leftleg2.setRotationPoint(0.0f, 4.1f, 0.0f);
		this.setRotation(leftleg2, 1.1f);
		this.leftleg.addChild(leftleg2);
        ModelRenderer leftleglow = new ModelRenderer(base, 0, 24);
		leftleglow.mirror = true;
		leftleglow.addBox(-1.5f, 0.0f, -1.0f, 3, 5, 2);
		leftleglow.setRotationPoint(0.0f, 5.0f, 0.0f);
		this.setRotation(leftleglow, -1.35f);
		leftleg2.addChild(leftleglow);
        ModelRenderer leftfoot = new ModelRenderer(base, 1, 26);
		leftfoot.mirror = true;
		leftfoot.addBox(-1.5f, 0.0f, -5.0f, 3, 2, 4);
		leftfoot.setRotationPoint(0.0f, 3.7f, 1.2f);
		this.setRotation(leftfoot, 0.55f);
		leftleglow.addChild(leftfoot);
	}

	private void setRotation(ModelRenderer model, float x) {
		model.rotateAngleX = x;
		model.rotateAngleY = 0.0f;
		model.rotateAngleZ = 0.0f;
	}

	public void setRotationAngles(float ignoredPar1, float ignoredPar2, float ignoredPar3, float ignoredPar4, float ignoredPar5, float ignoredPar6, Entity ignoredEntity) {
		this.rightleg.rotateAngleX = this.base.bipedRightLeg.rotateAngleX - 0.3f;
		this.leftleg.rotateAngleX = this.base.bipedLeftLeg.rotateAngleX - 0.3f;
		this.rightleg.rotationPointY = this.base.bipedRightLeg.rotationPointY;
		this.leftleg.rotationPointY = this.base.bipedLeftLeg.rotationPointY;
		this.rightleg.rotationPointZ = this.base.bipedRightLeg.rotationPointZ;
		this.leftleg.rotationPointZ = this.base.bipedLeftLeg.rotationPointZ;
		if (!this.base.isSneak) {
			--this.leftleg.rotationPointY;
			--this.rightleg.rotationPointY;
		}
	}
}
