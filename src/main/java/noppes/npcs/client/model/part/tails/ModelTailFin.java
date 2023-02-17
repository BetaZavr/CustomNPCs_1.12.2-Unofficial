package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelTailFin extends ModelRenderer {
	public ModelTailFin(ModelBiped base) {
		super((ModelBase) base);
		ModelRenderer Shape1 = new ModelRenderer((ModelBase) base, 0, 0);
		Shape1.addBox(-2.0f, -2.0f, -2.0f, 3, 3, 8);
		Shape1.setRotationPoint(0.5f, 0.0f, 1.0f);
		this.setRotation(Shape1, -0.669215f, 0.0f, 0.0f);
		this.addChild(Shape1);
		ModelRenderer Shape2 = new ModelRenderer((ModelBase) base, 2, 2);
		Shape2.addBox(-1.0f, -1.0f, 1.0f, 3, 2, 6);
		Shape2.setRotationPoint(-0.5f, 3.0f, 4.5f);
		this.setRotation(Shape2, -0.2602503f, 0.0f, 0.0f);
		this.addChild(Shape2);
		ModelRenderer Shape3 = new ModelRenderer((ModelBase) base, 0, 11);
		Shape3.addBox(-1.0f, -1.0f, -1.0f, 3, 1, 6);
		Shape3.setRotationPoint(0.5f, 5.0f, 12.0f);
		this.setRotation(Shape3, 0.0f, 1.07818f, 0.0f);
		this.addChild(Shape3);
		ModelRenderer Shape4 = new ModelRenderer((ModelBase) base, 0, 11);
		Shape4.mirror = true;
		Shape4.addBox(-2.0f, 0.0f, -1.0f, 3, 1, 6);
		Shape4.setRotationPoint(-0.5f, 4.0f, 12.0f);
		this.setRotation(Shape4, 0.0f, -1.003822f, 0.0f);
		this.addChild(Shape4);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
