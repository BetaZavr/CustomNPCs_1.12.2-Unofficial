package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelCarpentryBench extends ModelBase {
	ModelRenderer Backboard;
	ModelRenderer Blueprint;
	ModelRenderer Bottom_plate;
	ModelRenderer Desktop;
	ModelRenderer Leg1;
	ModelRenderer Leg2;
	ModelRenderer Leg3;
	ModelRenderer Leg4;
	ModelRenderer Vice_Base1;
	ModelRenderer Vice_Base2;
	ModelRenderer Vice_Crank;
	ModelRenderer Vice_Jaw1;
	ModelRenderer Vice_Jaw2;
	ModelRenderer Vice_Screw;

	public ModelCarpentryBench() {
		this.textureWidth = 128;
		this.textureHeight = 64;
		(this.Leg1 = new ModelRenderer((ModelBase) this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg1.setRotationPoint(6.0f, 10.0f, 5.0f);
		(this.Leg2 = new ModelRenderer((ModelBase) this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg2.setRotationPoint(6.0f, 10.0f, -5.0f);
		(this.Leg3 = new ModelRenderer((ModelBase) this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg3.setRotationPoint(-8.0f, 10.0f, 5.0f);
		(this.Leg4 = new ModelRenderer((ModelBase) this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 14, 2);
		this.Leg4.setRotationPoint(-8.0f, 10.0f, -5.0f);
		(this.Bottom_plate = new ModelRenderer((ModelBase) this, 0, 24)).addBox(0.0f, 0.0f, 0.0f, 14, 1, 10);
		this.Bottom_plate.setRotationPoint(-7.0f, 21.0f, -4.0f);
		this.Bottom_plate.setTextureSize(130, 64);
		(this.Desktop = new ModelRenderer((ModelBase) this, 0, 3)).addBox(0.0f, 0.0f, 0.0f, 18, 2, 13);
		this.Desktop.setRotationPoint(-9.0f, 9.0f, -6.0f);
		(this.Backboard = new ModelRenderer((ModelBase) this, 0, 18)).addBox(-1.0f, 0.0f, 0.0f, 18, 5, 1);
		this.Backboard.setRotationPoint(-8.0f, 7.0f, 7.0f);
		(this.Vice_Jaw1 = new ModelRenderer((ModelBase) this, 54, 18)).addBox(0.0f, 0.0f, 0.0f, 3, 2, 1);
		this.Vice_Jaw1.setRotationPoint(3.0f, 6.0f, -8.0f);
		(this.Vice_Jaw2 = new ModelRenderer((ModelBase) this, 54, 21)).addBox(0.0f, 0.0f, 0.0f, 3, 2, 1);
		this.Vice_Jaw2.setRotationPoint(3.0f, 6.0f, -6.0f);
		(this.Vice_Base1 = new ModelRenderer((ModelBase) this, 38, 30)).addBox(0.0f, 0.0f, 0.0f, 3, 1, 3);
		this.Vice_Base1.setRotationPoint(3.0f, 8.0f, -5.0f);
		(this.Vice_Base2 = new ModelRenderer((ModelBase) this, 38, 25)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 2);
		this.Vice_Base2.setRotationPoint(4.0f, 7.0f, -5.0f);
		(this.Vice_Crank = new ModelRenderer((ModelBase) this, 54, 24)).addBox(0.0f, 0.0f, 0.0f, 1, 5, 1);
		this.Vice_Crank.setRotationPoint(6.0f, 6.0f, -9.0f);
		(this.Vice_Screw = new ModelRenderer((ModelBase) this, 44, 25)).addBox(0.0f, 0.0f, 0.0f, 1, 1, 4);
		this.Vice_Screw.setRotationPoint(4.0f, 8.0f, -8.0f);
		(this.Blueprint = new ModelRenderer((ModelBase) this, 31, 18)).addBox(0.0f, 0.0f, 0.0f, 8, 0, 7);
		this.Blueprint.setRotationPoint(0.0f, 9.0f, 1.0f);
		this.setRotation(this.Blueprint, 0.3271718f, 0.1487144f, 0.0f);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
		this.Leg1.render(f5);
		this.Leg2.render(f5);
		this.Leg3.render(f5);
		this.Leg4.render(f5);
		this.Bottom_plate.render(f5);
		this.Desktop.render(f5);
		this.Backboard.render(f5);
		this.Vice_Jaw1.render(f5);
		this.Vice_Jaw2.render(f5);
		this.Vice_Base1.render(f5);
		this.Vice_Base2.render(f5);
		this.Vice_Crank.render(f5);
		this.Vice_Screw.render(f5);
		this.Blueprint.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
