package noppes.npcs.client.layer;

import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.Model2DRenderer;
import noppes.npcs.client.model.part.head.ModelDuckBeak;
import noppes.npcs.client.model.part.horns.ModelAntennasBack;
import noppes.npcs.client.model.part.horns.ModelAntennasFront;
import noppes.npcs.client.model.part.horns.ModelAntlerHorns;
import noppes.npcs.client.model.part.horns.ModelBullHorns;
import noppes.npcs.constants.EnumParts;

public class LayerHead<T extends EntityLivingBase> extends LayerInterface<T> {

	private ModelRenderer antennasBack;
	private ModelRenderer antennasFront;
	private ModelRenderer antlers;
	private ModelRenderer beak;
	private Model2DRenderer beard;
	private ModelRenderer bull;
	private ModelRenderer bunnyEars;
	private ModelRenderer bunnySnout;
	private ModelRenderer ears;
	private Model2DRenderer hair;
	private ModelRenderer large;
	private ModelRenderer medium;
	private Model2DRenderer mohawk;
	private ModelRenderer small;

	public LayerHead(RenderLiving<?> render) {
		super(render);
		this.createParts();
	}

	private void createParts() {
		(this.small = new ModelRenderer(this.model, 24, 0)).addBox(0.0f, 0.0f, 0.0f, 4, 3, 1);
		this.small.setRotationPoint(-2.0f, -3.0f, -5.0f);
		(this.medium = new ModelRenderer(this.model, 24, 0)).addBox(0.0f, 0.0f, 0.0f, 4, 3, 2);
		this.medium.setRotationPoint(-2.0f, -3.0f, -6.0f);
		(this.large = new ModelRenderer(this.model, 24, 0)).addBox(0.0f, 0.0f, 0.0f, 4, 3, 3);
		this.large.setRotationPoint(-2.0f, -3.0f, -7.0f);
		(this.bunnySnout = new ModelRenderer(this.model, 24, 0)).addBox(1.0f, 1.0f, 0.0f, 4, 2, 1);
		this.bunnySnout.setRotationPoint(-3.0f, -4.0f, -5.0f);
		ModelRenderer tooth = new ModelRenderer(this.model, 24, 3);
		tooth.addBox(2.0f, 3.0f, 0.0f, 2, 1, 1);
		tooth.setRotationPoint(0.0f, 0.0f, 0.0f);
		this.bunnySnout.addChild(tooth);
		(this.beak = new ModelDuckBeak(this.model)).setRotationPoint(0.0f, 0.0f, -4.0f);
		(this.beard = new Model2DRenderer(this.model, 56.0f, 20.0f, 8, 12)).setRotationOffset(-3.99f, 11.8f, -4.0f);
		this.beard.setScale(0.74f);
		(this.hair = new Model2DRenderer(this.model, 56.0f, 20.0f, 8, 12)).setRotationOffset(-3.99f, 11.8f, 3.0f);
		this.hair.setScale(0.75f);
		(this.mohawk = new Model2DRenderer(this.model, 0.0f, 0.0f, 64, 64)).setRotationOffset(-9.0f, 0.1f, -0.5f);
		this.setRotation(this.mohawk, 0.0f, 1.5707964f, 0.0f);
		this.mohawk.setScale(0.825f);
		this.bull = new ModelBullHorns(this.model);
		this.antlers = new ModelAntlerHorns(this.model);
		this.antennasBack = new ModelAntennasBack(this.model);
		this.antennasFront = new ModelAntennasFront(this.model);
		this.ears = new ModelRenderer(this.model);
		Model2DRenderer right = new Model2DRenderer(this.model, 56.0f, 0.0f, 8, 4);
		right.setRotationPoint(-7.44f, -7.3f, -0.0f);
		right.setScale(0.234f, 0.234f);
		right.setThickness(1.16f);
		this.ears.addChild((ModelRenderer) right);
		Model2DRenderer left = new Model2DRenderer(this.model, 56.0f, 0.0f, 8, 4);
		left.setRotationPoint(7.44f, -7.3f, 1.15f);
		left.setScale(0.234f, 0.234f);
		this.setRotation(left, 0.0f, 3.1415927f, 0.0f);
		left.setThickness(1.16f);
		this.ears.addChild((ModelRenderer) left);
		Model2DRenderer right2 = new Model2DRenderer(this.model, 56.0f, 4.0f, 8, 4);
		right2.setRotationPoint(-7.44f, -7.3f, 1.14f);
		right2.setScale(0.234f, 0.234f);
		right2.setThickness(1.16f);
		this.ears.addChild((ModelRenderer) right2);
		Model2DRenderer left2 = new Model2DRenderer(this.model, 56.0f, 4.0f, 8, 4);
		left2.setRotationPoint(7.44f, -7.3f, 2.31f);
		left2.setScale(0.234f, 0.234f);
		this.setRotation(left2, 0.0f, 3.1415927f, 0.0f);
		left2.setThickness(1.16f);
		this.ears.addChild((ModelRenderer) left2);
		this.bunnyEars = new ModelRenderer(this.model);
		ModelRenderer earleft = new ModelRenderer(this.model, 56, 0);
		earleft.mirror = true;
		earleft.addBox(-1.466667f, -4.0f, 0.0f, 3, 7, 1);
		earleft.setRotationPoint(2.533333f, -11.0f, 0.0f);
		this.bunnyEars.addChild(earleft);
		ModelRenderer earright = new ModelRenderer(this.model, 56, 0);
		earright.addBox(-1.5f, -4.0f, 0.0f, 3, 7, 1);
		earright.setRotationPoint(-2.466667f, -11.0f, 0.0f);
		this.bunnyEars.addChild(earright);
	}

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		this.model.bipedHead.postRender(0.0625f);
		this.renderSnout(par7);
		this.renderBeard(par7);
		this.renderHair(par7);
		this.renderMohawk(par7);
		this.renderHorns(par7);
		this.renderEars(par7);
	}

	private void renderBeard(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.BEARD);
		if (data == null) {
			return;
		}
		this.preRender(data);
		this.beard.render(par7);
	}

	private void renderEars(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.EARS);
		if (data == null) {
			return;
		}
		this.preRender(data);
		if (data.type == 0) {
			this.ears.render(par7);
		} else if (data.type == 1) {
			this.bunnyEars.render(par7);
		}
	}

	private void renderHair(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.HAIR);
		if (data == null) {
			return;
		}
		this.preRender(data);
		this.hair.render(par7);
	}

	private void renderHorns(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.HORNS);
		if (data == null) {
			return;
		}
		this.preRender(data);
		if (data.type == 0) {
			this.bull.render(par7);
		} else if (data.type == 1) {
			this.antlers.render(par7);
		} else if (data.type == 2 && data.pattern == 0) {
			this.antennasBack.render(par7);
		} else if (data.type == 2 && data.pattern == 1) {
			this.antennasFront.render(par7);
		}
	}

	private void renderMohawk(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.MOHAWK);
		if (data == null) {
			return;
		}
		this.preRender(data);
		this.mohawk.render(par7);
	}

	private void renderSnout(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.SNOUT);
		if (data == null) {
			return;
		}
		this.preRender(data);
		if (data.type == 0) {
			this.small.render(par7);
		} else if (data.type == 1) {
			this.medium.render(par7);
		} else if (data.type == 2) {
			this.large.render(par7);
		} else if (data.type == 3) {
			this.bunnySnout.render(par7);
		} else if (data.type == 4) {
			this.beak.render(par7);
		}
	}

	@Override
	public void rotate(float par2, float par3, float par4, float par5, float par6, float par7) {
		ModelRenderer head = this.model.bipedHead;
		if (head.rotateAngleX < 0.0f) {
			this.beard.rotateAngleX = 0.0f;
			this.hair.rotateAngleX = -head.rotateAngleX * 1.2f;
			if (head.rotateAngleX > -1.0f) {
				this.hair.rotationPointY = -head.rotateAngleX * 1.5f;
				this.hair.rotationPointZ = -head.rotateAngleX * 1.5f;
			}
		} else {
			this.hair.rotateAngleX = 0.0f;
			this.hair.rotationPointY = 0.0f;
			this.hair.rotationPointZ = 0.0f;
			this.beard.rotateAngleX = -head.rotateAngleX;
		}
	}
}
