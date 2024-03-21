package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.entity.EntityNpcDragon;

public class ModelNpcDragon extends ModelBase {
	private ModelRenderer body;
	private float field_40317_s;
	private ModelRenderer frontFoot;
	private ModelRenderer frontLeg;
	private ModelRenderer frontLegTip;
	private ModelRenderer head;
	private ModelRenderer jaw;
	private ModelRenderer neck;
	private ModelRenderer rearFoot;
	private ModelRenderer rearLeg;
	private ModelRenderer rearLegTip;
	private ModelRenderer wing;
	private ModelRenderer wingTip;

	public ModelNpcDragon(float scale) {
		this.textureWidth = 256;
		this.textureHeight = 256;
		this.setTextureOffset("body.body", 0, 0);
		this.setTextureOffset("wing.skin", -56, 88);
		this.setTextureOffset("wingtip.skin", -56, 144);
		this.setTextureOffset("rearleg.main", 0, 0);
		this.setTextureOffset("rearfoot.main", 112, 0);
		this.setTextureOffset("rearlegtip.main", 196, 0);
		this.setTextureOffset("head.upperhead", 112, 30);
		this.setTextureOffset("wing.bone", 112, 88);
		this.setTextureOffset("head.upperlip", 176, 44);
		this.setTextureOffset("jaw.jaw", 176, 65);
		this.setTextureOffset("frontleg.main", 112, 104);
		this.setTextureOffset("wingtip.bone", 112, 136);
		this.setTextureOffset("frontfoot.main", 144, 104);
		this.setTextureOffset("neck.box", 192, 104);
		this.setTextureOffset("frontlegtip.main", 226, 138);
		this.setTextureOffset("body.scale", 220, 53);
		this.setTextureOffset("head.scale", 0, 0);
		this.setTextureOffset("neck.scale", 48, 0);
		this.setTextureOffset("head.nostril", 112, 0);
		(this.head = new ModelRenderer((ModelBase) this, "head")).addBox("upperlip", -6.0f, -1.0f, -24.0f, 12, 5, 16);
		this.head.addBox("upperhead", -8.0f, -8.0f, -10.0f, 16, 16, 16);
		this.head.mirror = true;
		this.head.addBox("scale", -5.0f, -12.0f, -4.0f, 2, 4, 6);
		this.head.addBox("nostril", -5.0f, -3.0f, -22.0f, 2, 2, 4);
		this.head.mirror = false;
		this.head.addBox("scale", 3.0f, -12.0f, -4.0f, 2, 4, 6);
		this.head.addBox("nostril", 3.0f, -3.0f, -22.0f, 2, 2, 4);
		(this.jaw = new ModelRenderer((ModelBase) this, "jaw")).setRotationPoint(0.0f, 4.0f, -8.0f);
		this.jaw.addBox("jaw", -6.0f, 0.0f, -16.0f, 12, 4, 16);
		this.head.addChild(this.jaw);
		(this.neck = new ModelRenderer((ModelBase) this, "neck")).addBox("box", -5.0f, -5.0f, -5.0f, 10, 10, 10);
		this.neck.addBox("scale", -1.0f, -9.0f, -3.0f, 2, 4, 6);
		(this.body = new ModelRenderer((ModelBase) this, "body")).setRotationPoint(0.0f, 4.0f, 8.0f);
		this.body.addBox("body", -12.0f, 0.0f, -16.0f, 24, 24, 64);
		this.body.addBox("scale", -1.0f, -6.0f, -10.0f, 2, 6, 12);
		this.body.addBox("scale", -1.0f, -6.0f, 10.0f, 2, 6, 12);
		this.body.addBox("scale", -1.0f, -6.0f, 30.0f, 2, 6, 12);
		(this.wing = new ModelRenderer((ModelBase) this, "wing")).setRotationPoint(-12.0f, 5.0f, 2.0f);
		this.wing.addBox("bone", -56.0f, -4.0f, -4.0f, 56, 8, 8);
		this.wing.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56);
		(this.wingTip = new ModelRenderer((ModelBase) this, "wingtip")).setRotationPoint(-56.0f, 0.0f, 0.0f);
		this.wingTip.addBox("bone", -56.0f, -2.0f, -2.0f, 56, 4, 4);
		this.wingTip.addBox("skin", -56.0f, 0.0f, 2.0f, 56, 0, 56);
		this.wing.addChild(this.wingTip);
		(this.frontLeg = new ModelRenderer((ModelBase) this, "frontleg")).setRotationPoint(-12.0f, 20.0f, 2.0f);
		this.frontLeg.addBox("main", -4.0f, -4.0f, -4.0f, 8, 24, 8);
		(this.frontLegTip = new ModelRenderer((ModelBase) this, "frontlegtip")).setRotationPoint(0.0f, 20.0f, -1.0f);
		this.frontLegTip.addBox("main", -3.0f, -1.0f, -3.0f, 6, 24, 6);
		this.frontLeg.addChild(this.frontLegTip);
		(this.frontFoot = new ModelRenderer((ModelBase) this, "frontfoot")).setRotationPoint(0.0f, 23.0f, 0.0f);
		this.frontFoot.addBox("main", -4.0f, 0.0f, -12.0f, 8, 4, 16);
		this.frontLegTip.addChild(this.frontFoot);
		(this.rearLeg = new ModelRenderer((ModelBase) this, "rearleg")).setRotationPoint(-16.0f, 16.0f, 42.0f);
		this.rearLeg.addBox("main", -8.0f, -4.0f, -8.0f, 16, 32, 16);
		(this.rearLegTip = new ModelRenderer((ModelBase) this, "rearlegtip")).setRotationPoint(0.0f, 32.0f, -4.0f);
		this.rearLegTip.addBox("main", -6.0f, -2.0f, 0.0f, 12, 32, 12);
		this.rearLeg.addChild(this.rearLegTip);
		(this.rearFoot = new ModelRenderer((ModelBase) this, "rearfoot")).setRotationPoint(0.0f, 31.0f, 4.0f);
		this.rearFoot.addBox("main", -9.0f, 0.0f, -20.0f, 18, 6, 24);
		this.rearLegTip.addChild(this.rearFoot);
	}

	private float func_40307_a(double d) {
		while (d >= 180.0d) {
			d -= 360.0d;
		}
		while (d < -180.0d) {
			d += 360.0d;
		}
		return (float) d;
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		EntityNpcDragon entitydragon = (EntityNpcDragon) entity;
		GlStateManager.pushMatrix();
		float f6 = entitydragon.field_40173_aw + (entitydragon.field_40172_ax - entitydragon.field_40173_aw) * this.field_40317_s;
		this.jaw.rotateAngleX = (float) ((Math.sin(f6 * 3.1415927f * 2.0f) + 1.0) * 0.2f);
		float f7 = (float) (Math.sin(f6 * 3.1415927f * 2.0f - 1.0f) + 1.0);
		f7 = (f7 * f7 * 1.0f + f7 * 2.0f) * 0.05f;
		GlStateManager.translate(0.0f, f7 - 2.0f, -3.0f);
		GlStateManager.rotate(f7 * 2.0f, 1.0f, 0.0f, 0.0f);
		float f8 = -30.0f;
		float f9 = 22.0f;
		float f10 = 0.0f;
		float f11 = 1.5f;
		double[] ad = entitydragon.func_40160_a(6, this.field_40317_s);
		float f12 = this.func_40307_a(entitydragon.func_40160_a(5, this.field_40317_s)[0] - entitydragon.func_40160_a(10, this.field_40317_s)[0]);
		float f13 = this.func_40307_a(entitydragon.func_40160_a(5, this.field_40317_s)[0] + f12 / 2.0f);
		f8 += 2.0f;
		float f14 = 0.0f;
		float f15 = f6 * 3.141593f * 2.0f;
		f8 = 20.0f;
		f9 = -12.0f;
		for (int i = 0; i < 5; ++i) {
			double[] ad2 = entitydragon.func_40160_a(5 - i, this.field_40317_s);
			f14 = (float) (Math.cos(i * 0.45f + f15) * 0.15f);
			this.neck.rotateAngleY = this.func_40307_a(ad2[0] - ad[0]) * 3.1415927f / 180.0f * f11;
			this.neck.rotateAngleX = (float) (f14 + (ad2[1] - ad[1]) * 3.1415927f / 180.0f * f11 * 5.0f);
			this.neck.rotateAngleZ = -this.func_40307_a(ad2[0] - f13) * 3.1415927f / 180.0f * f11;
			this.neck.rotationPointY = f8;
			this.neck.rotationPointZ = f9;
			this.neck.rotationPointX = f10;
			f8 += (Math.sin(this.neck.rotateAngleX) * 10.0);
			f9 -= (Math.cos(this.neck.rotateAngleY) * Math.cos(this.neck.rotateAngleX) * 10.0);
			f10 -= (Math.sin(this.neck.rotateAngleY) * Math.cos(this.neck.rotateAngleX) * 10.0);
			this.neck.render(f5);
		}
		this.head.rotationPointY = f8;
		this.head.rotationPointZ = f9;
		this.head.rotationPointX = f10;
		double[] ad3 = entitydragon.func_40160_a(0, this.field_40317_s);
		this.head.rotateAngleY = this.func_40307_a(ad3[0] - ad[0]) * 3.1415927f / 180.0f * 1.0f;
		this.head.rotateAngleZ = -this.func_40307_a(ad3[0] - f13) * 3.1415927f / 180.0f * 1.0f;
		this.head.render(f5);
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 1.0f, 0.0f);
		if (entitydragon.onGround) {
			GlStateManager.rotate(-f12 * f11 * 0.3f, 0.0f, 0.0f, 1.0f);
		} else {
			GlStateManager.rotate(-f12 * f11 * 1.0f, 0.0f, 0.0f, 1.0f);
		}
		GlStateManager.translate(0.0f, -1.18f, 0.0f);
		this.body.rotateAngleZ = 0.0f;
		this.body.render(f5);
		if (entitydragon.onGround) {
			for (int j = 0; j < 2; ++j) {
				GlStateManager.enableCull();
				this.wing.rotateAngleX = 0.25f;
				this.wing.rotateAngleY = 0.95f;
				this.wing.rotateAngleZ = -0.5f;
				this.wingTip.rotateAngleZ = -0.4f;
				this.frontLeg.rotateAngleX = MathHelper .cos((float) (f * 0.6662f + ((j == 0) ? 0.0 : 3.141592653589793))) * 0.6f * f1 + 0.45f + f7 * 0.5f;
				this.frontLegTip.rotateAngleX = -1.3f - f7 * 1.2f;
				this.frontFoot.rotateAngleX = 0.85f + f7 * 0.5f;
				this.frontLeg.render(f5);
				this.rearLeg.rotateAngleX = MathHelper.cos((float) (f * 0.6662f + ((j == 0) ? 3.141592653589793 : 0.0))) * 0.6f * f1 + 0.75f + f7 * 0.5f;
				this.rearLegTip.rotateAngleX = -1.6f - f7 * 0.8f;
				this.rearLegTip.rotationPointY = 20.0f;
				this.rearLegTip.rotationPointZ = 2.0f;
				this.rearFoot.rotateAngleX = 0.85f + f7 * 0.2f;
				this.rearLeg.render(f5);
				this.wing.render(f5);
				GlStateManager.scale(-1.0f, 1.0f, 1.0f);
				if (j == 0) { GlStateManager.cullFace(GlStateManager.CullFace.FRONT); }
			}
		} else {
			for (int j = 0; j < 2; ++j) {
				GlStateManager.enableCull();
				float f16 = f6 * 3.1415927f * 2.0f;
				this.wing.rotateAngleX = (float) (0.125f - Math.cos(f16) * 0.2f);
				this.wing.rotateAngleY = 0.25f;
				this.wing.rotateAngleZ = (float) ((Math.sin(f16) + 0.125) * 0.8f);
				this.wingTip.rotateAngleZ = (float) (-(Math.sin(f16 + 2.0f) + 0.5) * 0.75f);
				this.rearLegTip.rotationPointY = 32.0f;
				this.rearLegTip.rotationPointZ = -2.0f;
				this.rearLeg.rotateAngleX = 1.0f + f7 * 0.1f;
				this.rearLegTip.rotateAngleX = 0.5f + f7 * 0.1f;
				this.rearFoot.rotateAngleX = 0.75f + f7 * 0.1f;
				this.frontLeg.rotateAngleX = 1.3f + f7 * 0.1f;
				this.frontLegTip.rotateAngleX = -0.5f - f7 * 0.1f;
				this.frontFoot.rotateAngleX = 0.75f + f7 * 0.1f;
				this.wing.render(f5);
				this.frontLeg.render(f5);
				this.rearLeg.render(f5);
				GlStateManager.scale(-1.0f, 1.0f, 1.0f);
				if (j == 0) {
					GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
				}
			}
		}
		GlStateManager.popMatrix();
		GlStateManager.cullFace(GlStateManager.CullFace.BACK);
		GlStateManager.disableCull();
		f14 = (float) (-Math.sin(f6 * 3.141593f * 2.0f) * 0.0f);
		f15 = f6 * 3.1415927f * 2.0f;
		f8 = 10.0f;
		f9 = 60.0f;
		f10 = 0.0f;
		ad = entitydragon.func_40160_a(11, this.field_40317_s);
		for (int k = 0; k < 12; ++k) {
			double[] ad4 = entitydragon.func_40160_a(12 + k, this.field_40317_s);
			f14 += (Math.sin(k * 0.45f + f15) * 0.05000000074505806);
			this.neck.rotateAngleY = (this.func_40307_a(ad4[0] - ad[0]) * f11 + 180.0f) * 3.1415927f / 180.0f;
			this.neck.rotateAngleX = (float) (f14 + (ad4[1] - ad[1]) * 3.1415927f / 180.0f * f11 * 5.0f);
			this.neck.rotateAngleZ = this.func_40307_a(ad4[0] - f13) * 3.1415927f / 180.0f * f11;
			this.neck.rotationPointY = f8;
			this.neck.rotationPointZ = f9;
			this.neck.rotationPointX = f10;
			f8 += (Math.sin(this.neck.rotateAngleX) * 10.0);
			f9 -= (Math.cos(this.neck.rotateAngleY) * Math.cos(this.neck.rotateAngleX) * 10.0);
			f10 -= (Math.sin(this.neck.rotateAngleY) * Math.cos(this.neck.rotateAngleX) * 10.0);
			this.neck.render(f5);
		}
		GlStateManager.popMatrix();
	}

	public void setLivingAnimations(EntityLivingBase entityliving, float f, float f1, float f2) {
		this.field_40317_s = f2;
	}
}
