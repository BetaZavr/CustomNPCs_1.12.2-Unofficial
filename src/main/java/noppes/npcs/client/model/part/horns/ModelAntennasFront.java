package noppes.npcs.client.model.part.horns;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelAntennasFront extends ModelRenderer {

	public ModelAntennasFront(ModelBiped base) {
		super(base);
		ModelRenderer rightantenna1 = new ModelRenderer(base, 60, 27);
		rightantenna1.addBox(0.0f, 0.0f, 0.0f, 1, 4, 1);
		rightantenna1.setRotationPoint(2.0f, -9.4f, -2.0f);
		this.setRotation(rightantenna1, 0.4014257f, 0.0698132f, 0.0698132f);
		this.addChild(rightantenna1);
		ModelRenderer leftantenna1 = new ModelRenderer(base, 56, 27);
		leftantenna1.mirror = true;
		leftantenna1.addBox(-1.0f, 0.0f, 0.0f, 1, 4, 1);
		leftantenna1.setRotationPoint(-2.0f, -9.4f, -2.0f);
		this.setRotation(leftantenna1, 0.4014257f, -0.0698132f, -0.0698132f);
		this.addChild(leftantenna1);
		ModelRenderer rightantenna2 = new ModelRenderer(base, 60, 27);
		rightantenna2.addBox(0.0f, 0.0f, 0.0f, 1, 4, 1);
		rightantenna2.setRotationPoint(3.0f, -10.2f, -5.3f);
		this.setRotation(rightantenna2, 1.22173f, -0.2094395f, 0.0698132f);
		this.addChild(rightantenna2);
		ModelRenderer leftantenna2 = new ModelRenderer(base, 56, 27);
		leftantenna2.mirror = true;
		leftantenna2.addBox(-1.0f, 0.0f, 0.0f, 1, 4, 1);
		leftantenna2.setRotationPoint(-3.0f, -10.2f, -5.3f);
		this.setRotation(leftantenna2, 1.22173f, 0.2094395f, -0.0698132f);
		this.addChild(leftantenna2);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
