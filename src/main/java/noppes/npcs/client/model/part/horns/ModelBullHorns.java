package noppes.npcs.client.model.part.horns;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelBullHorns extends ModelRenderer {

	public ModelBullHorns(ModelBiped base) {
		super(base);
		ModelRenderer Left1 = new ModelRenderer(base, 36, 16);
		Left1.mirror = true;
		Left1.addBox(0.0f, 0.0f, 0.0f, 2, 2, 2);
		Left1.setRotationPoint(4.0f, -8.0f, -2.0f);
		this.addChild(Left1);
		ModelRenderer Right1 = new ModelRenderer(base, 36, 16);
		Right1.addBox(-3.0f, 0.0f, 0.0f, 2, 2, 2);
		Right1.setRotationPoint(-3.0f, -8.0f, -2.0f);
		this.addChild(Right1);
		ModelRenderer Left2 = new ModelRenderer(base, 12, 16);
		Left2.mirror = true;
		Left2.addBox(0.0f, 0.0f, 0.0f, 2, 2, 2);
		Left2.setRotationPoint(5.0f, -8.0f, -2.0f);
		this.setRotation(Left2, 0.0371786f, 0.3346075f, -0.2602503f);
		this.addChild(Left2);
		ModelRenderer Right2 = new ModelRenderer(base, 12, 16);
		Right2.addBox(-2.0f, 0.0f, 0.0f, 2, 2, 2);
		Right2.setRotationPoint(-5.0f, -8.0f, -2.0f);
		this.setRotation(Right2, 0.0371786f, -0.3346075f, 0.2602503f);
		this.addChild(Right2);
		ModelRenderer Left3 = new ModelRenderer(base, 13, 17);
		Left3.mirror = true;
		Left3.addBox(-1.0f, 0.0f, 0.0f, 2, 1, 1);
		Left3.setRotationPoint(7.0f, -8.0f, -2.0f);
		this.setRotation(Left3, 0.2602503f, 0.8551081f, -0.4089647f);
		this.addChild(Left3);
		ModelRenderer Right3 = new ModelRenderer(base, 13, 17);
		Right3.addBox(-1.0f, 0.0f, 0.0f, 2, 1, 1);
		Right3.setRotationPoint(-7.0f, -8.0f, -2.0f);
		this.setRotation(Right3, -0.2602503f, -0.8551081f, 0.4089647f);
		this.addChild(Right3);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
