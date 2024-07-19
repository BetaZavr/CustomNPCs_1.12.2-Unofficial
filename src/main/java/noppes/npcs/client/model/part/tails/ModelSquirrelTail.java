package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelSquirrelTail extends ModelRenderer {

	public final ModelBiped base;

	public ModelSquirrelTail(ModelBiped base) {
		super(base);
		this.base = base;
		ModelRenderer Shape1 = new ModelRenderer(base, 0, 0);
		Shape1.addBox(-1.0f, -1.0f, -1.0f, 2, 2, 3);
		Shape1.setRotationPoint(0.0f, -1.0f, 3.0f);
		this.setRotation(Shape1, 0.0f);
		this.addChild(Shape1);
		ModelRenderer Shape2 = new ModelRenderer(base, 0, 9);
		Shape2.addBox(-2.0f, -5.0f, -1.0f, 4, 5, 3);
		Shape2.setRotationPoint(0.0f, 0.0f, 1.0f);
		this.setRotation(Shape2, -0.37f);
		Shape1.addChild(Shape2);
		ModelRenderer Shape3 = new ModelRenderer(base, 0, 18);
		Shape3.addBox(-2.466667f, -6.0f, -1.0f, 5, 7, 3);
		Shape3.setRotationPoint(0.0f, -5.0f, 0.0f);
		this.setRotation(Shape3, 0.3f);
		Shape2.addChild(Shape3);
		ModelRenderer Shape4 = new ModelRenderer(base, 25, 0);
		Shape4.addBox(-3.0f, -0.6f, -1.0f, 6, 5, 3);
		Shape4.setRotationPoint(0.0f, -5.0f, 1.0f);
		this.setRotation(Shape4, 2.5f);
		Shape3.addChild(Shape4);
		ModelRenderer Shape5 = new ModelRenderer(base, 25, 10);
		Shape5.addBox(-3.0f, -2.0f, -1.0f, 6, 3, 5);
		Shape5.setRotationPoint(0.0f, 3.5f, 0.0f);
		this.setRotation(Shape5, -2.5f);
		Shape4.addChild(Shape5);
	}

	private void setRotation(ModelRenderer model, float x) {
		model.rotateAngleX = x;
		model.rotateAngleY = 0.0f;
		model.rotateAngleZ = 0.0f;
	}

	public void setRotationAngles(float ignoredF, float ignoredF1, float ignoredF2, float ignoredF3, float ignoredF4, float ignoredF5, Entity ignoredEntity) {

	}

}
