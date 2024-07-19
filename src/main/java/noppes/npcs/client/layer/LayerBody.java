package noppes.npcs.client.layer;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.Model2DRenderer;
import noppes.npcs.client.model.ModelPlaneRenderer;
import noppes.npcs.constants.EnumParts;

public class LayerBody<T extends EntityLivingBase> extends LayerInterface<T> {

	private Model2DRenderer breasts;
	private ModelRenderer breasts2;
	private ModelRenderer breasts3;
	private Model2DRenderer fin;
	private Model2DRenderer lWing;
	private Model2DRenderer rWing;
	private ModelPlaneRenderer skirt;

	public LayerBody(RenderLiving<?> render) {
		super(render);
		this.createParts();
	}

	private void createParts() {
		this.lWing = new Model2DRenderer(this.model, 56.0f, 16.0f, 8, 16);
		this.lWing.mirror = true;
		this.lWing.setRotationPoint(2.0f, 2.5f, 1.0f);
		this.lWing.setRotationOffset(8.0f, 14.0f, 0.0f);
		this.setRotation(this.lWing, 0.7141593f, -0.5235988f, -0.5090659f);
		(this.rWing = new Model2DRenderer(this.model, 56.0f, 16.0f, 8, 16)).setRotationPoint(-2.0f, 2.5f, 1.0f);
		this.rWing.setRotationOffset(-8.0f, 14.0f, 0.0f);
		this.setRotation(this.rWing, 0.7141593f, 0.5235988f, 0.5090659f);
		(this.breasts = new Model2DRenderer(this.model, 20.0f, 22.0f, 8, 3)).setRotationPoint(-3.6f, 5.2f, -3.0f);
		this.breasts.setScale(0.17f, 0.19f);
		this.breasts.setThickness(1.0f);
		this.breasts2 = new ModelRenderer(this.model);
		Model2DRenderer bottom = new Model2DRenderer(this.model, 20.0f, 22.0f, 8, 4);
		bottom.setRotationPoint(-3.6f, 5.0f, -3.1f);
		bottom.setScale(0.225f, 0.2f);
		bottom.setThickness(2.0f);
		bottom.rotateAngleX = -0.31415927f;
		this.breasts2.addChild(bottom);
		this.breasts3 = new ModelRenderer(this.model);
		Model2DRenderer right = new Model2DRenderer(this.model, 20.0f, 23.0f, 3, 2);
		right.setRotationPoint(-3.8f, 5.3f, -3.6f);
		right.setScale(0.12f, 0.14f);
		right.setThickness(1.75f);
		this.breasts3.addChild(right);
		Model2DRenderer right2 = new Model2DRenderer(this.model, 20.0f, 22.0f, 3, 1);
		right2.setRotationPoint(-3.79f, 4.1f, -3.14f);
		right2.setScale(0.06f, 0.07f);
		right2.setThickness(1.75f);
		right2.rotateAngleX = 0.34906584f;
		this.breasts3.addChild(right2);
		Model2DRenderer right3 = new Model2DRenderer(this.model, 20.0f, 24.0f, 3, 1);
		right3.setRotationPoint(-3.79f, 5.3f, -3.6f);
		right3.setScale(0.06f, 0.07f);
		right3.setThickness(1.75f);
		right3.rotateAngleX = -0.34906584f;
		this.breasts3.addChild(right3);
		Model2DRenderer right4 = new Model2DRenderer(this.model, 21.0f, 23.0f, 1, 2);
		right4.setRotationPoint(-1.8f, 5.3f, -3.14f);
		right4.setScale(0.12f, 0.14f);
		right4.setThickness(1.75f);
		right4.rotateAngleY = 0.34906584f;
		this.breasts3.addChild(right4);
		Model2DRenderer left = new Model2DRenderer(this.model, 25.0f, 23.0f, 3, 2);
		left.setRotationPoint(0.8f, 5.3f, -3.6f);
		left.setScale(0.12f, 0.14f);
		left.setThickness(1.75f);
		this.breasts3.addChild(left);
		Model2DRenderer left2 = new Model2DRenderer(this.model, 25.0f, 22.0f, 3, 1);
		left2.setRotationPoint(0.81f, 4.1f, -3.18f);
		left2.setScale(0.06f, 0.07f);
		left2.setThickness(1.75f);
		left2.rotateAngleX = 0.34906584f;
		this.breasts3.addChild(left2);
		Model2DRenderer left3 = new Model2DRenderer(this.model, 25.0f, 24.0f, 3, 1);
		left3.setRotationPoint(0.81f, 5.3f, -3.6f);
		left3.setScale(0.06f, 0.07f);
		left3.setThickness(1.75f);
		left3.rotateAngleX = -0.34906584f;
		this.breasts3.addChild(left3);
		Model2DRenderer left4 = new Model2DRenderer(this.model, 24.0f, 23.0f, 1, 2);
		left4.setRotationPoint(0.8f, 5.3f, -3.6f);
		left4.setScale(0.12f, 0.14f);
		left4.setThickness(1.75f);
		left4.rotateAngleY = -0.34906584f;
		this.breasts3.addChild(left4);
		(this.skirt = new ModelPlaneRenderer(this.model, 58, 18)).addSidePlane(0.0f, 0.0f, 0.0f, 9, 2);
		ModelPlaneRenderer part1 = new ModelPlaneRenderer(this.model, 58, 18);
		part1.addSidePlane(2.0f, 0.0f, 0.0f, 9, 2);
		part1.rotateAngleY = -1.5707964f;
		this.skirt.addChild(part1);
		this.skirt.setRotationPoint(2.4f, 8.8f, 0.0f);
		this.setRotation(this.skirt, 0.3f, -0.2f, -0.2f);
		(this.fin = new Model2DRenderer(this.model, 56.0f, 20.0f, 8, 12)).setRotationPoint(-0.5f, 12.0f, 10.0f);
		this.fin.setScale(0.74f);
		this.fin.rotateAngleY = 1.5707964f;
	}

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		this.model.bipedBody.postRender(0.0625f);
		this.renderSkirt(par7);
		this.renderWings(par7);
		this.renderFin(par7);
		this.renderBreasts(par7);
	}

	private void renderBreasts(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.BREASTS);
		if (data == null) {
			return;
		}
		data.playerTexture = true;
		this.preRender(data);
		if (data.type == 0) {
			this.breasts.render(par7);
		}
		if (data.type == 1) {
			this.breasts2.render(par7);
		}
		if (data.type == 2) {
			this.breasts3.render(par7);
		}
	}

	private void renderFin(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.FIN);
		if (data == null) {
			return;
		}
		this.preRender(data);
		this.fin.render(par7);
	}

	private void renderSkirt(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.SKIRT);
		if (data == null) {
			return;
		}
		this.preRender(data);
		GlStateManager.pushMatrix();
		GlStateManager.scale(1.7f, 1.04f, 1.6f);
		for (int i = 0; i < 10; ++i) {
			GlStateManager.rotate(36.0f, 0.0f, 1.0f, 0.0f);
			this.skirt.render(par7);
		}
		GlStateManager.popMatrix();
	}

	private void renderWings(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.WINGS);
		if (data == null) {
			return;
		}
		this.preRender(data);
		this.rWing.render(par7);
		this.lWing.render(par7);
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) {
		this.rWing.rotateAngleX = 0.7141593f;
		this.rWing.rotateAngleZ = 0.5090659f;
		this.lWing.rotateAngleX = 0.7141593f;
		this.lWing.rotateAngleZ = -0.5090659f;
		float motion = Math.abs(MathHelper.sin(par1 * 0.033f + 3.1415927f) * 0.4f) * par2;
		if (!this.npc.onGround || motion > 0.01) {
			float speed = 0.55f + 0.5f * motion;
			float y = MathHelper.sin(par3 * 0.55f);
			Model2DRenderer rWing = this.rWing;
			rWing.rotateAngleZ += y * 0.5f * speed;
			Model2DRenderer rWing2 = this.rWing;
			rWing2.rotateAngleX += y * 0.5f * speed;
			Model2DRenderer lWing = this.lWing;
			lWing.rotateAngleZ -= y * 0.5f * speed;
			Model2DRenderer lWing2 = this.lWing;
			lWing2.rotateAngleX += y * 0.5f * speed;
		} else {
			Model2DRenderer lWing3 = this.lWing;
			lWing3.rotateAngleZ += MathHelper.cos(par3 * 0.09f) * 0.05f + 0.05f;
			Model2DRenderer rWing3 = this.rWing;
			rWing3.rotateAngleZ -= MathHelper.cos(par3 * 0.09f) * 0.05f + 0.05f;
			Model2DRenderer lWing4 = this.lWing;
			lWing4.rotateAngleX += MathHelper.sin(par3 * 0.067f) * 0.05f;
			Model2DRenderer rWing4 = this.rWing;
			rWing4.rotateAngleX += MathHelper.sin(par3 * 0.067f) * 0.05f;
		}
		this.setRotation(this.skirt, 0.3f, -0.2f, -0.2f);
		ModelPlaneRenderer skirt = this.skirt;
		skirt.rotateAngleX += this.model.bipedLeftArm.rotateAngleX * 0.04f;
		ModelPlaneRenderer skirt2 = this.skirt;
		skirt2.rotateAngleZ += this.model.bipedLeftArm.rotateAngleX * 0.06f;
		ModelPlaneRenderer skirt3 = this.skirt;
		skirt3.rotateAngleZ -= MathHelper.cos(par3 * 0.09f) * 0.04f - 0.05f;
	}
}
