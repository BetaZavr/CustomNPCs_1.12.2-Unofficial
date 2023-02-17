package noppes.npcs.client.model.part.tails;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;

public class ModelFeatherTail extends ModelRenderer {
	ModelRenderer feather1;
	ModelRenderer feather2;
	ModelRenderer feather3;
	ModelRenderer feather4;
	ModelRenderer feather5;

	public ModelFeatherTail(ModelBiped base) {
		super((ModelBase) base);
		int x = 56;
		int y = 16;
		(this.feather1 = new ModelRenderer((ModelBase) base, x, y)).addBox(-1.5f, 0.0f, 0.0f, 3, 8, 0);
		this.feather1.setRotationPoint(1.0f, -0.5f, 2.0f);
		this.setRotation(this.feather1, 1.482807f, 0.2602503f, 0.1487144f);
		this.addChild(this.feather1);
		(this.feather2 = new ModelRenderer((ModelBase) base, x, y)).addBox(-1.5f, 0.0f, 0.0f, 3, 8, 0);
		this.feather2.setRotationPoint(0.0f, -0.5f, 1.0f);
		this.setRotation(this.feather2, 1.200559f, 0.3717861f, 0.1858931f);
		this.addChild(this.feather2);
		this.feather3 = new ModelRenderer((ModelBase) base, x, y);
		this.feather3.mirror = true;
		this.feather3.addBox(-1.5f, -0.5f, 0.0f, 3, 8, 0);
		this.feather3.setRotationPoint(-1.0f, 0.0f, 2.0f);
		this.setRotation(this.feather3, 1.256389f, -0.4089647f, -0.4833219f);
		this.addChild(this.feather3);
		(this.feather4 = new ModelRenderer((ModelBase) base, x, y)).addBox(-1.5f, 0.0f, 0.0f, 3, 8, 0);
		this.feather4.setRotationPoint(0.0f, -0.5f, 2.0f);
		this.setRotation(this.feather4, 1.786329f, 0.0f, 0.0f);
		this.addChild(this.feather4);
		this.feather5 = new ModelRenderer((ModelBase) base, x, y);
		this.feather5.mirror = true;
		this.feather5.addBox(-1.5f, 0.0f, 0.0f, 3, 8, 0);
		this.feather5.setRotationPoint(-1.0f, -0.5f, 2.0f);
		this.setRotation(this.feather5, 1.570073f, -0.2602503f, -0.2230717f);
		this.addChild(this.feather5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
