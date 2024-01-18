package noppes.npcs.client.layer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.part.legs.ModelDigitigradeLegs;
import noppes.npcs.client.model.part.legs.ModelHorseLegs;
import noppes.npcs.client.model.part.legs.ModelMermaidLegs;
import noppes.npcs.client.model.part.legs.ModelNagaLegs;
import noppes.npcs.client.model.part.legs.ModelSpiderLegs;
import noppes.npcs.client.model.part.tails.ModelCanineTail;
import noppes.npcs.client.model.part.tails.ModelDragonTail;
import noppes.npcs.client.model.part.tails.ModelFeatherTail;
import noppes.npcs.client.model.part.tails.ModelRodentTail;
import noppes.npcs.client.model.part.tails.ModelSquirrelTail;
import noppes.npcs.client.model.part.tails.ModelTailFin;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;

public class LayerLegs<T extends EntityLivingBase> extends LayerInterface<T> implements LayerPreRender {
	private ModelDigitigradeLegs digitigrade;
	private ModelRenderer dragon;
	private ModelRenderer feathers;
	private ModelRenderer fin;
	private ModelCanineTail fox;
	private ModelRenderer horse;
	private ModelHorseLegs horseLegs;
	private ModelMermaidLegs mermaid;
	private ModelNagaLegs naga;
	private ModelRenderer rodent;
	float rotationPointY;
	float rotationPointZ;
	private ModelSpiderLegs spiderLegs;
	private ModelRenderer squirrel;
	private ModelRenderer tail;

	public LayerLegs(RenderLiving<?> render) {
		super(render);
		this.createParts();
	}

	private void createParts() {
		this.spiderLegs = new ModelSpiderLegs(this.model);
		this.horseLegs = new ModelHorseLegs(this.model);
		this.naga = new ModelNagaLegs((ModelBase) this.model);
		this.mermaid = new ModelMermaidLegs((ModelBase) this.model);
		this.digitigrade = new ModelDigitigradeLegs(this.model);
		this.fox = new ModelCanineTail(this.model);
		(this.tail = new ModelRenderer((ModelBase) this.model, 56, 21)).addBox(-1.0f, 0.0f, 0.0f, 2, 9, 2);
		this.tail.setRotationPoint(0.0f, 0.0f, 1.0f);
		this.setRotation(this.tail, 0.8714253f, 0.0f, 0.0f);
		(this.horse = new ModelRenderer((ModelBase) this.model)).setTextureSize(32, 32);
		this.horse.setRotationPoint(0.0f, -1.0f, 1.0f);
		ModelRenderer tailBase = new ModelRenderer((ModelBase) this.model, 0, 26);
		tailBase.setTextureSize(32, 32);
		tailBase.addBox(-1.0f, -1.0f, 0.0f, 2, 2, 3);
		this.setRotation(tailBase, -1.134464f, 0.0f, 0.0f);
		this.horse.addChild(tailBase);
		ModelRenderer tailMiddle = new ModelRenderer((ModelBase) this.model, 0, 13);
		tailMiddle.setTextureSize(32, 32);
		tailMiddle.addBox(-1.5f, -2.0f, 3.0f, 3, 4, 7);
		this.setRotation(tailMiddle, -1.134464f, 0.0f, 0.0f);
		this.horse.addChild(tailMiddle);
		ModelRenderer tailTip = new ModelRenderer((ModelBase) this.model, 0, 0);
		tailTip.setTextureSize(32, 32);
		tailTip.addBox(-1.5f, -4.5f, 9.0f, 3, 4, 7);
		this.setRotation(tailTip, -1.40215f, 0.0f, 0.0f);
		this.horse.addChild(tailTip);
		this.horse.rotateAngleX = 0.5f;
		this.dragon = new ModelDragonTail(this.model);
		this.squirrel = new ModelSquirrelTail(this.model);
		this.fin = new ModelTailFin(this.model);
		this.rodent = new ModelRodentTail(this.model);
		this.feathers = new ModelFeatherTail(this.model);
	}

	@Override
	public void preRender(EntityCustomNpc player) {
		this.npc = player;
		this.playerdata = player.modelData;
		ModelPartData data = this.playerdata.getPartData(EnumParts.LEGS);
		ModelRenderer bipedLeftLeg = this.model.bipedLeftLeg;
		ModelRenderer bipedRightLeg = this.model.bipedRightLeg;
		boolean b = data == null || data.type != 0;
		bipedRightLeg.isHidden = b;
		bipedLeftLeg.isHidden = b;
	}

	@Override
	public void render(float par2, float par3, float par4, float par5, float par6, float par7) {
		this.renderLegs(par7);
		this.renderTails(par7);
	}

	private void renderLegs(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.LEGS);
		if (data.type <= 0) { return; }
		GlStateManager.pushMatrix();
		ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.LEG_LEFT);
		this.preRender(data);
		if (data.type == 1) {
			GlStateManager.translate(0.0f, config.offsetBase[1] * 2.0f, config.offsetBase[2] * par7 + 0.04f);
			GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
			this.naga.render(par7);
		} else if (data.type == 2) {
			GlStateManager.translate(0.0, config.offsetBase[1] * 1.76f - 0.1 * config.scaleBase[1], (config.offsetBase[2] * par7));
			GlStateManager.scale(1.06f, 1.06f, 1.06f);
			GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
			this.spiderLegs.render(par7);
		} else if (data.type == 3) {
			if (config.scaleBase[1] >= 1.0f) {
				GlStateManager.translate(0.0f, config.offsetBase[1] * 1.76f, config.offsetBase[2] * par7);
			} else {
				GlStateManager.translate(0.0f, config.offsetBase[1] * 1.86f, config.offsetBase[2] * par7);
			}
			GlStateManager.scale(0.79f, 0.9f - config.scaleBase[1] / 10.0f, 0.79f);
			GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
			this.horseLegs.render(par7);
		} else if (data.type == 4) {
			GlStateManager.translate(0.0f, config.offsetBase[1] * 1.86f, config.offsetBase[2] * par7);
			GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
			this.mermaid.render(par7);
		} else if (data.type == 5) {
			GlStateManager.translate(0.0f, config.offsetBase[1] * 1.86f, config.offsetBase[2] * par7);
			GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
			this.digitigrade.render(par7);
		}
		GlStateManager.popMatrix();
	}

	private void renderTails(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.TAIL);
		if (data == null) {
			return;
		}
		GlStateManager.pushMatrix();
		ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.LEG_LEFT);
		GlStateManager.translate(config.offsetBase[0] * par7, config.offsetBase[1] + this.rotationPointY * par7, config.offsetBase[2] * par7 + this.rotationPointZ * par7);
		GlStateManager.translate(0.0f, 0.0f, (config.scaleBase[2] - 1.0f) * 5.0f * par7);
		GlStateManager.scale(config.scaleBase[0], config.scaleBase[1], config.scaleBase[2]);
		this.preRender(data);
		if (data.type == 0) {
			if (data.pattern == 1) {
				this.tail.rotationPointX = -0.5f;
				ModelRenderer tail = this.tail;
				tail.rotateAngleY -= 0.2;
				this.tail.render(par7);
				ModelRenderer tail2 = this.tail;
				++tail2.rotationPointX;
				ModelRenderer tail3 = this.tail;
				tail3.rotateAngleY += 0.4;
				this.tail.render(par7);
				this.tail.rotationPointX = 0.0f;
			} else {
				this.tail.render(par7);
			}
		} else if (data.type == 1) {
			this.dragon.render(par7);
		} else if (data.type == 2) {
			this.horse.render(par7);
		} else if (data.type == 3) {
			this.squirrel.render(par7);
		} else if (data.type == 4) {
			this.fin.render(par7);
		} else if (data.type == 5) {
			this.rodent.render(par7);
		} else if (data.type == 6) {
			this.feathers.render(par7);
		} else if (data.type == 7) {
			this.fox.render(par7);
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void rotate(float par1, float par2, float par3, float par4, float par5, float par6) {
		this.rotateLegs(par1, par2, par3, par4, par5, par6);
		this.rotateTail(par1, par2, par3, par4, par5, par6);
	}

	public void rotateLegs(float par1, float par2, float par3, float par4, float par5, float par6) {
		ModelPartData part = this.playerdata.getPartData(EnumParts.LEGS);
		if (part.type == 2) {
			this.spiderLegs.setRotationAngles(this.playerdata, par1, par2, par3, par4, par5, par6, this.npc);
		} else if (part.type == 3) {
			this.horseLegs.setRotationAngles(this.playerdata, par1, par2, par3, par4, par5, par6, this.npc);
		} else if (part.type == 1) {
			this.naga.isRiding = this.model.isRiding;
			this.naga.isSleeping = this.npc.isPlayerSleeping();
			this.naga.isCrawling = (this.npc.currentAnimation == 7);
			this.naga.isSneaking = this.model.isSneak;
			this.naga.setRotationAngles(par1, par2, par3, par4, par5, par6, this.npc);
		} else if (part.type == 4) {
			this.mermaid.setRotationAngles(par1, par2, par3, par4, par5, par6, this.npc);
		} else if (part.type == 5) {
			this.digitigrade.setRotationAngles(par1, par2, par3, par4, par5, par6, this.npc);
		}
	}

	public void rotateTail(float par1, float par2, float par3, float par4, float par5, float par6) {
		ModelPartData part = this.playerdata.getPartData(EnumParts.LEGS);
		ModelPartData partTail = this.playerdata.getPartData(EnumParts.TAIL);
		ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.LEG_LEFT);
		float rotateAngleY = MathHelper.cos(par1 * 0.6662f) * 0.2f * par2;
		float rotateAngleX = MathHelper.sin(par3 * 0.067f) * 0.05f;
		this.rotationPointZ = 0.0f;
		this.rotationPointY = 11.0f;
		if (part.type == 2) {
			this.rotationPointY = 12.0f + (config.scaleBase[1] - 1.0f) * 3.0f;
			this.rotationPointZ = 15.0f + (config.scaleBase[2] - 1.0f) * 10.0f;
			if (this.npc.isPlayerSleeping() || this.npc.currentAnimation == 7) {
				this.rotationPointY = 12.0f + 16.0f * config.scaleBase[2];
				this.rotationPointZ = 1.0f * config.scaleBase[1];
				rotateAngleX = -0.7853982f;
			}
		} else if (part.type == 3) {
			this.rotationPointY = 10.0f;
			this.rotationPointZ = 16.0f + (config.scaleBase[2] - 1.0f) * 12.0f;
		} else {
			this.rotationPointZ = (1.0f - config.scaleBase[2]) * 1.0f;
		}
		if (partTail != null) {
			if (partTail.type == 2) {
				rotateAngleX += 0.5;
			}
			if (partTail.type == 0) {
				rotateAngleX += 0.87f;
			}
			if (partTail.type == 7) {
				this.fox.setRotationAngles(par1, par2, par3, par4, par5, par6, this.npc);
			}
		}
		this.rotationPointZ += this.model.bipedRightLeg.rotationPointZ + 0.5f;
		ModelCanineTail fox = this.fox;
		ModelRenderer tail = this.tail;
		ModelRenderer feathers = this.feathers;
		ModelRenderer dragon = this.dragon;
		ModelRenderer squirrel = this.squirrel;
		ModelRenderer horse = this.horse;
		ModelRenderer fin = this.fin;
		ModelRenderer rodent = this.rodent;
		float n = rotateAngleX;
		rodent.rotateAngleX = n;
		fin.rotateAngleX = n;
		horse.rotateAngleX = n;
		squirrel.rotateAngleX = n;
		dragon.rotateAngleX = n;
		feathers.rotateAngleX = n;
		tail.rotateAngleX = n;
		fox.rotateAngleX = n;
		ModelCanineTail fox2 = this.fox;
		ModelRenderer tail2 = this.tail;
		ModelRenderer feathers2 = this.feathers;
		ModelRenderer dragon2 = this.dragon;
		ModelRenderer squirrel2 = this.squirrel;
		ModelRenderer horse2 = this.horse;
		ModelRenderer fin2 = this.fin;
		ModelRenderer rodent2 = this.rodent;
		float n2 = rotateAngleY;
		rodent2.rotateAngleY = n2;
		fin2.rotateAngleY = n2;
		horse2.rotateAngleY = n2;
		squirrel2.rotateAngleY = n2;
		dragon2.rotateAngleY = n2;
		feathers2.rotateAngleY = n2;
		tail2.rotateAngleY = n2;
		fox2.rotateAngleY = n2;
	}
}
