package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelCarpentryBench extends ModelBase {

	final ModelRenderer Backboard;
	final ModelRenderer Blueprint;
	final ModelRenderer Bottom_plate;
	final ModelRenderer Desktop;
	final ModelRenderer Leg1;
	final ModelRenderer Leg2;
	final ModelRenderer Leg3;
	final ModelRenderer Leg4;
	final ModelRenderer Vice_Base1;
	final ModelRenderer Vice_Base2;
	final ModelRenderer Vice_Crank;
	final ModelRenderer Vice_Jaw1;
	final ModelRenderer Vice_Jaw2;
	final ModelRenderer Vice_Screw;

	public ModelCarpentryBench() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		(this.Leg1 = new ModelRenderer(this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg1.setRotationPoint(6.0f, 10.0f, 5.0f);
		(this.Leg2 = new ModelRenderer(this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg2.setRotationPoint(6.0f, 10.0f, -5.0f);
		(this.Leg3 = new ModelRenderer(this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg3.setRotationPoint(-8.0f, 10.0f, 5.0f);
		(this.Leg4 = new ModelRenderer(this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg4.setRotationPoint(-8.0f, 10.0f, -5.0f);
		(this.Bottom_plate = new ModelRenderer(this, 0, 24)).addBox(0.0f, 0.0f, 0.0f, 14, 1, 10);
		this.Bottom_plate.setRotationPoint(-7.0f, 21.0f, -4.0f);
		this.Bottom_plate.setTextureSize(130, 64);
		(this.Desktop = new ModelRenderer(this, 0, 3)).addBox(0.0f, 0.0f, 0.0f, 18, 2, 13);
		this.Desktop.setRotationPoint(-9.0f, 9.0f, -6.0f);
		(this.Backboard = new ModelRenderer(this, 0, 18)).addBox(-1.0f, 0.0f, 0.0f, 18, 5, 1);
		this.Backboard.setRotationPoint(-8.0f, 7.0f, 7.0f);
		(this.Vice_Jaw1 = new ModelRenderer(this, 54, 18)).addBox(0.0f, 0.0f, 0.0f, 3, 2, 1);
		this.Vice_Jaw1.setRotationPoint(3.0f, 6.0f, -8.0f);
		(this.Vice_Jaw2 = new ModelRenderer(this, 54, 21)).addBox(0.0f, 0.0f, 0.0f, 3, 2, 1);
		this.Vice_Jaw2.setRotationPoint(3.0f, 6.0f, -6.0f);
		(this.Vice_Base1 = new ModelRenderer(this, 38, 30)).addBox(0.0f, 0.0f, 0.0f, 3, 1, 3);
		this.Vice_Base1.setRotationPoint(3.0f, 8.0f, -5.0f);
		(this.Vice_Base2 = new ModelRenderer(this, 38, 25)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 2);
		this.Vice_Base2.setRotationPoint(4.0f, 7.0f, -5.0f);
		(this.Vice_Crank = new ModelRenderer(this, 54, 24)).addBox(0.0f, 0.0f, 0.0f, 1, 5, 1);
		this.Vice_Crank.setRotationPoint(6.0f, 6.0f, -9.0f);
		(this.Vice_Screw = new ModelRenderer(this, 44, 25)).addBox(0.0f, 0.0f, 0.0f, 1, 1, 4);
		this.Vice_Screw.setRotationPoint(4.0f, 8.0f, -8.0f);
		(this.Blueprint = new ModelRenderer(this, 31, 18)).addBox(0.0f, 0.0f, 0.0f, 8, 0, 7);
		this.Blueprint.setRotationPoint(0.0f, 9.0f, 1.0f);
		this.setRotation(this.Blueprint);
	}

	public void render(float scale) {
		this.Leg1.render(scale);
		this.Leg2.render(scale);
		this.Leg3.render(scale);
		this.Leg4.render(scale);
		this.Bottom_plate.render(scale);
		this.Desktop.render(scale);
		this.Backboard.render(scale);
		this.Vice_Jaw1.render(scale);
		this.Vice_Jaw2.render(scale);
		this.Vice_Base1.render(scale);
		this.Vice_Base2.render(scale);
		this.Vice_Crank.render(scale);
		this.Vice_Screw.render(scale);
		this.Blueprint.render(scale);
	}

	private void setRotation(ModelRenderer model) {
		model.rotateAngleX = 0.3271718f;
		model.rotateAngleY = 0.1487144f;
		model.rotateAngleZ = 0.0f;
	}
}
