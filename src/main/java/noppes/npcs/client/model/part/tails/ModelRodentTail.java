package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelRodentTail extends ModelRenderer {

	final ModelRenderer Shape1;
	final ModelRenderer Shape2;

	public ModelRodentTail(ModelBiped base) {
		super(base);
		(this.Shape1 = new ModelRenderer(base, 0, 0)).addBox(-0.5333334f, -0.4666667f, -1.0f, 1, 1, 6);
		this.Shape1.setRotationPoint(0.0f, 0.0f, 2.0f);
		this.setRotation(this.Shape1, -0.9294653f);
		this.addChild(this.Shape1);
		(this.Shape2 = new ModelRenderer(base, 1, 1)).addBox(-0.5f, -0.1666667f, 1.0f, 1, 1, 5);
		this.Shape2.setRotationPoint(0.0f, 3.0f, 4.0f);
		this.setRotation(this.Shape2, -0.4833219f);
		this.addChild(this.Shape2);
	}

	private void setRotation(ModelRenderer model, float x) {
		model.rotateAngleX = x;
		model.rotateAngleY = 0.0f;
		model.rotateAngleZ = 0.0f;
	}

}
