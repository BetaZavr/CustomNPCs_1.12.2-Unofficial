package noppes.npcs.client.model.blocks;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;

public class ModelMailboxUS extends ModelBase {

	final ModelRenderer Shape1;
	final ModelRenderer Shape10;
	final ModelRenderer Shape11;
	final ModelRenderer Shape12;
	final ModelRenderer Shape13;
	final ModelRenderer Shape2;
	final ModelRenderer Shape3;
	final ModelRenderer Shape4;
	final ModelRenderer Shape5;
	final ModelRenderer Shape6;
	final ModelRenderer Shape7;
	final ModelRenderer Shape8;
	final ModelRenderer Shape9;

	public ModelMailboxUS() {
		this.textureWidth = 64;
		this.textureHeight = 128;
		(this.Shape1 = new ModelRenderer(this, 0, 48)).addBox(0.0f, 0.0f, 0.0f, 16, 14, 16);
		this.Shape1.setRotationPoint(-8.0f, 8.0f, -8.0f);
		(this.Shape2 = new ModelRenderer(this, 0, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape2.setRotationPoint(-8.0f, 22.0f, -8.0f);
		(this.Shape3 = new ModelRenderer(this, 5, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape3.setRotationPoint(-8.0f, 22.0f, 7.0f);
		(this.Shape4 = new ModelRenderer(this, 10, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape4.setRotationPoint(7.0f, 22.0f, -8.0f);
		(this.Shape5 = new ModelRenderer(this, 15, 79)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 1);
		this.Shape5.setRotationPoint(7.0f, 22.0f, 7.0f);
		(this.Shape6 = new ModelRenderer(this, 0, 14)).addBox(0.0f, 0.0f, 0.0f, 16, 3, 7);
		this.Shape6.setRotationPoint(-8.0f, 5.0f, 0.0f);
		(this.Shape7 = new ModelRenderer(this, 0, 6)).addBox(0.0f, 0.0f, 0.0f, 16, 2, 6);
		this.Shape7.setRotationPoint(-8.0f, 3.0f, 0.0f);
		(this.Shape8 = new ModelRenderer(this, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 16, 1, 5);
		this.Shape8.setRotationPoint(-8.0f, 2.0f, 0.0f);
		(this.Shape9 = new ModelRenderer(this, 0, 37)).addBox(0.0f, 0.0f, 0.0f, 1, 3, 7);
		this.Shape9.setRotationPoint(-8.0f, 5.0f, -7.0f);
		(this.Shape10 = new ModelRenderer(this, 16, 37)).addBox(0.0f, 0.0f, 0.0f, 1, 3, 7);
		this.Shape10.setRotationPoint(7.0f, 5.0f, -7.0f);
		(this.Shape11 = new ModelRenderer(this, 0, 29)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 6);
		this.Shape11.setRotationPoint(-8.0f, 3.0f, -6.0f);
		(this.Shape12 = new ModelRenderer(this, 14, 29)).addBox(0.0f, 0.0f, 0.0f, 1, 2, 6);
		this.Shape12.setRotationPoint(7.0f, 3.0f, -6.0f);
		(this.Shape13 = new ModelRenderer(this, 0, 25)).addBox(0.0f, 0.0f, 0.0f, 16, 1, 3);
		this.Shape13.setRotationPoint(-8.0f, 2.0f, -3.0f);
	}

	public void render(float scale) {
		this.Shape1.render(scale);
		this.Shape2.render(scale);
		this.Shape3.render(scale);
		this.Shape4.render(scale);
		this.Shape5.render(scale);
		this.Shape6.render(scale);
		this.Shape7.render(scale);
		this.Shape8.render(scale);
		this.Shape9.render(scale);
		this.Shape10.render(scale);
		this.Shape11.render(scale);
		this.Shape12.render(scale);
		this.Shape13.render(scale);
	}
}
