package noppes.npcs.client.model.part.legs;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;

public class ModelMermaidLegs extends ModelRenderer {
	private ModelRenderer bottom;
	private ModelRenderer fin1;
	private ModelRenderer fin2;
	private ModelRenderer middle;
	private ModelRenderer top;

	public ModelMermaidLegs(ModelBase base) {
		super(base);
		this.textureWidth = 64.0f;
		this.textureHeight = 32.0f;
		(this.top = new ModelRenderer(base, 0, 16)).addBox(-2.0f, -2.5f, -2.0f, 8, 9, 4);
		this.top.setRotationPoint(-2.0f, 14.0f, 1.0f);
		this.setRotation(this.top, 0.26f, 0.0f, 0.0f);
		(this.middle = new ModelRenderer(base, 28, 0)).addBox(0.0f, 0.0f, 0.0f, 7, 6, 4);
		this.middle.setRotationPoint(-1.5f, 6.5f, -1.0f);
		this.setRotation(this.middle, 0.86f, 0.0f, 0.0f);
		this.top.addChild(this.middle);
		(this.bottom = new ModelRenderer(base, 24, 16)).addBox(0.0f, 0.0f, 0.0f, 6, 7, 3);
		this.bottom.setRotationPoint(0.5f, 6.0f, 0.5f);
		this.setRotation(this.bottom, 0.15f, 0.0f, 0.0f);
		this.middle.addChild(this.bottom);
		(this.fin1 = new ModelRenderer(base, 0, 0)).addBox(0.0f, 0.0f, 0.0f, 5, 9, 1);
		this.fin1.setRotationPoint(0.0f, 4.5f, 1.0f);
		this.setRotation(this.fin1, 0.05f, 0.0f, 0.5911399f);
		this.bottom.addChild(this.fin1);
		this.fin2 = new ModelRenderer(base, 0, 0);
		this.fin2.mirror = true;
		this.fin2.addBox(-5.0f, 0.0f, 0.0f, 5, 9, 1);
		this.fin2.setRotationPoint(6.0f, 4.5f, 1.0f);
		this.setRotation(this.fin2, 0.05f, 0.0f, -0.591143f);
		this.bottom.addChild(this.fin2);
	}

	public void render(float f5) {
		if (this.isHidden || !this.showModel) {
			return;
		}
		this.top.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity) {
		float ani = MathHelper.sin(par1 * 0.6662f);
		if (ani > 0.2) {
			ani /= 3.0f;
		}
		this.top.rotateAngleX = 0.26f - ani * 0.2f * par2;
		this.middle.rotateAngleX = 0.86f - ani * 0.24f * par2;
		this.bottom.rotateAngleX = 0.15f - ani * 0.28f * par2;
		ModelRenderer fin2 = this.fin2;
		ModelRenderer fin3 = this.fin1;
		float n = 0.05f - ani * 0.35f * par2;
		fin3.rotateAngleX = n;
		fin2.rotateAngleX = n;
	}
}
