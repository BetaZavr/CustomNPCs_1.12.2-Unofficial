package noppes.npcs.client.model.part.head;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;

public class ModelDuckBeak extends ModelRenderer {
	ModelRenderer Bottom;
	ModelRenderer Left;
	ModelRenderer Middle;
	ModelRenderer Right;
	ModelRenderer Top;
	ModelRenderer Top2;
	ModelRenderer Top3;

	public ModelDuckBeak(ModelBiped base) {
		super((ModelBase) base);
		(this.Top3 = new ModelRenderer((ModelBase) base, 14, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 1, 3);
		this.Top3.setRotationPoint(-1.0f, -2.0f, -5.0f);
		this.setRotation(this.Top3, 0.3346075f, 0.0f, 0.0f);
		this.addChild(this.Top3);
		(this.Top2 = new ModelRenderer((ModelBase) base, 0, 0)).addBox(0.0f, 0.0f, -0.4f, 4, 1, 3);
		this.Top2.setRotationPoint(-2.0f, -3.0f, -2.0f);
		this.setRotation(this.Top2, 0.3346075f, 0.0f, 0.0f);
		this.addChild(this.Top2);
		(this.Bottom = new ModelRenderer((ModelBase) base, 24, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 1, 5);
		this.Bottom.setRotationPoint(-1.0f, -1.0f, -5.0f);
		this.addChild(this.Bottom);
		this.Left = new ModelRenderer((ModelBase) base, 0, 4);
		this.Left.mirror = true;
		this.Left.addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
		this.Left.setRotationPoint(0.98f, -3.0f, -2.0f);
		this.addChild(this.Left);
		(this.Right = new ModelRenderer((ModelBase) base, 0, 4)).addBox(0.0f, 0.0f, 0.0f, 1, 3, 2);
		this.Right.setRotationPoint(-1.98f, -3.0f, -2.0f);
		this.addChild(this.Right);
		(this.Middle = new ModelRenderer((ModelBase) base, 3, 0)).addBox(0.0f, 0.0f, 0.0f, 2, 1, 3);
		this.Middle.setRotationPoint(-1.0f, -2.0f, -5.0f);
		this.addChild(this.Middle);
		(this.Top = new ModelRenderer((ModelBase) base, 6, 4)).addBox(0.0f, 0.0f, 0.0f, 2, 2, 1);
		this.Top.setRotationPoint(-1.0f, -4.4f, -1.0f);
		this.addChild(this.Top);
	}

	public void render(float f) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, -1.0f * f);
		GlStateManager.scale(0.82f, 0.82f, 0.7f);
		super.render(f);
		GlStateManager.popMatrix();
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}
}
