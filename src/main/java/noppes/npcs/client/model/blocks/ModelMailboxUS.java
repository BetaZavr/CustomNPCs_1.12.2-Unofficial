package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

public class ModelMailboxUS extends ModelBase {
	ModelRenderer Shape1;
	ModelRenderer Shape10;
	ModelRenderer Shape11;
	ModelRenderer Shape12;
	ModelRenderer Shape13;
	ModelRenderer Shape2;
	ModelRenderer Shape3;
	ModelRenderer Shape4;
	ModelRenderer Shape5;
	ModelRenderer Shape6;
	ModelRenderer Shape7;
	ModelRenderer Shape8;
	ModelRenderer Shape9;

	public ModelMailboxUS() {
		this.textureWidth = 64;
		this.textureHeight = 128;
		(this.Shape1 = new ModelRenderer((ModelBase) this, 0, 48)).addBox(0.0f, 0.0f, 0.0f, 16, 14, 16);
		this.Shape1.setRotationPoint(-8.0f, 8.0f, -8.0f);
		(this.Shape2 = new ModelRenderer((ModelBase) this, 0, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape2.setRotationPoint(-8.0f, 22.0f, -8.0f);
		(this.Shape3 = new ModelRenderer((ModelBase) this, 5, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape3.setRotationPoint(-8.0f, 22.0f, 7.0f);
		(this.Shape4 = new ModelRenderer((ModelBase) this, 10, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape4.setRotationPoint(7.0f, 22.0f, -8.0f);
		(this.Shape5 = new ModelRenderer((ModelBase) this, 15, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape5.setRotationPoint(7.0f, 22.0f, 7.0f);
		(this.Shape6 = new ModelRenderer((ModelBase) this, 0, 14)).addBox(0.0f, 0.0f, 0.0f, 16, 3, 7);
		this.Shape6.setRotationPoint(-8.0f, 5.0f, 0.0f);
		(this.Shape7 = new ModelRenderer((ModelBase) this, 0, 6)).addBox(0.0f, 0.0f, 0.0f, 16, 2, 6);
		this.Shape7.setRotationPoint(-8.0f, 3.0f, 0.0f);
		(this.Shape8 = new ModelRenderer((ModelBase) this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 16, 1, 5);
		this.Shape8.setRotationPoint(-8.0f, 2.0f, 0.0f);
		(this.Shape9 = new ModelRenderer((ModelBase) this, 0, 37)).addBox(0.0f, 0.0f, 0.0f, 1, 3, 7);
		this.Shape9.setRotationPoint(-8.0f, 5.0f, -7.0f);
		(this.Shape10 = new ModelRenderer((ModelBase) this, 16, 37)).addBox(0.0f, 0.0f, 0.0f, 1, 3, 7);
		this.Shape10.setRotationPoint(7.0f, 5.0f, -7.0f);
		(this.Shape11 = new ModelRenderer((ModelBase) this, 0, 29)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 6);
		this.Shape11.setRotationPoint(-8.0f, 3.0f, -6.0f);
		(this.Shape12 = new ModelRenderer((ModelBase) this, 14, 29)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 6);
		this.Shape12.setRotationPoint(7.0f, 3.0f, -6.0f);
		(this.Shape13 = new ModelRenderer((ModelBase) this, 0, 25)).addBox(0.0f, 0.0f, 0.0f, 16, 1, 3);
		this.Shape13.setRotationPoint(-8.0f, 2.0f, -3.0f);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		this.Shape1.render(f5);
		this.Shape2.render(f5);
		this.Shape3.render(f5);
		this.Shape4.render(f5);
		this.Shape5.render(f5);
		this.Shape6.render(f5);
		this.Shape7.render(f5);
		this.Shape8.render(f5);
		this.Shape9.render(f5);
		this.Shape10.render(f5);
		this.Shape11.render(f5);
		this.Shape12.render(f5);
		this.Shape13.render(f5);
	}
}
