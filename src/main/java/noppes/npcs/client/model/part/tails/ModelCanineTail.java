package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCanineTail extends ModelRenderer {

	final ModelRenderer Base_1;
	final ModelRenderer BaseB_1;
	final ModelRenderer End_1;
	final ModelRenderer Mid_1;
	final ModelRenderer Mid_2;
	final ModelRenderer MidB_1;

	public ModelCanineTail(ModelBiped base) {
		super(base);
		(this.Base_1 = new ModelRenderer(base, 56, 16)).addBox(-1.0f, 0.0f, -3.0f, 2, 3, 2);
		this.Base_1.setRotationPoint(0.0f, 1.0f, -1.2f);
		this.setRotation(this.Base_1, -0.4490659f, 3.141593f);
		this.addChild(this.Base_1);
		(this.BaseB_1 = new ModelRenderer(base, 56, 16)).addBox(-0.5f, 0.0f, -1.5f, 1, 3, 1);
		this.Base_1.addChild(this.BaseB_1);
		(this.Mid_1 = new ModelRenderer(base, 56, 20)).addBox(-1.0f, 3.0f, -2.8f, 2, 2, 2);
		this.setRotation(this.Mid_1, -0.16f, 0.0f);
		this.Base_1.addChild(this.Mid_1);
		(this.Mid_2 = new ModelRenderer(base, 56, 22)).addBox(-1.5f, 5.0f, -1.5f, 3, 6, 2);
		this.Mid_2.setRotationPoint(0.0f, 0.0f, -1.5f);
		this.setRotation(this.Mid_2, -0.0f, 0.0f);
		this.Mid_1.addChild(this.Mid_2);
		ModelRenderer Mid_2b = new ModelRenderer(base, 56, 23);
		Mid_2b.addBox(-1.5f, 5.0f, -1.5f, 3, 6, 1);
		this.setRotation(Mid_2b, -0.0f, 3.1415927f);
		this.Mid_2.addChild(Mid_2b);
		(this.MidB_1 = new ModelRenderer(base, 56, 20)).addBox(-0.5f, 3.0f, -1.0f, 1, 2, 1);
		this.Mid_1.addChild(this.MidB_1);
		(this.End_1 = new ModelRenderer(base, 56, 29)).addBox(-1.0f, 10.7f, -1.0f, 2, 1, 2);
		this.Mid_2.addChild(this.End_1);
	}

	private void setRotation(ModelRenderer model, float x, float y) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = 0.0F;
	}

	public void setRotationAngles(float ignoredF, float f1, float ignoredF2, float ignoredF3, float ignoredF4, float ignoredF5, Entity ignoredEntity) {
		this.Base_1.rotateAngleX = -0.5490659f - f1 * 0.7f;
		this.Base_1.rotateAngleY = 3.141593f + this.rotateAngleY * 0.1f;
		this.Mid_1.rotateAngleY = this.rotateAngleY * 0.2f;
		this.Mid_2.rotateAngleY = this.rotateAngleY * 0.2f;
	}
}
