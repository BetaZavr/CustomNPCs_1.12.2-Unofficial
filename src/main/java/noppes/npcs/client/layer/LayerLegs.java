package noppes.npcs.client.layer;

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
		this.naga = new ModelNagaLegs(this.model);
		this.mermaid = new ModelMermaidLegs(this.model);
		this.digitigrade = new ModelDigitigradeLegs(this.model);
		this.fox = new ModelCanineTail(this.model);
		(this.tail = new ModelRenderer(this.model, 56, 21)).addBox(-1.0f, 0.0f, 0.0f, 2, 9, 2);
		this.tail.setRotationPoint(0.0f, 0.0f, 1.0f);
		this.setRotation(this.tail, 0.8714253f, 0.0f, 0.0f);
		(this.horse = new ModelRenderer(this.model)).setTextureSize(32, 32);
		this.horse.setRotationPoint(0.0f, -1.0f, 1.0f);
		ModelRenderer tailBase = new ModelRenderer(this.model, 0, 26);
		tailBase.setTextureSize(32, 32);
		tailBase.addBox(-1.0f, -1.0f, 0.0f, 2, 2, 3);
		this.setRotation(tailBase, -1.134464f, 0.0f, 0.0f);
		this.horse.addChild(tailBase);
		ModelRenderer tailMiddle = new ModelRenderer(this.model, 0, 13);
		tailMiddle.setTextureSize(32, 32);
		tailMiddle.addBox(-1.5f, -2.0f, 3.0f, 3, 4, 7);
		this.setRotation(tailMiddle, -1.134464f, 0.0f, 0.0f);
		this.horse.addChild(tailMiddle);
		ModelRenderer tailTip = new ModelRenderer(this.model, 0, 0);
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
		if (!this.npc.animation.showParts.get(EnumParts.BODY)) { return; }
		this.renderTails(par7);
	}

	private void renderLegs(float par7) {
		ModelPartData data = this.playerdata.getPartData(EnumParts.LEGS);
		if (data.type <= 0) {
			return;
		}
		GlStateManager.pushMatrix();
		ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.LEG_LEFT);
		this.preRender(data);
		if (data.type == 1) {
			GlStateManager.translate(0.0f, config.offset[1] * 2.0f, config.offset[2] * par7 + 0.04f);
			GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
			this.naga.render(par7);
		} else if (data.type == 2) {
			GlStateManager.translate(0.0, config.offset[1] * 1.76f - 0.1 * config.scale[1],
					(config.offset[2] * par7));
			GlStateManager.scale(1.06f, 1.06f, 1.06f);
			GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
			this.spiderLegs.render(par7);
		} else if (data.type == 3) {
			if (config.scale[1] >= 1.0f) {
				GlStateManager.translate(0.0f, config.offset[1] * 1.76f, config.offset[2] * par7);
			} else {
				GlStateManager.translate(0.0f, config.offset[1] * 1.86f, config.offset[2] * par7);
			}
			GlStateManager.scale(0.79f, 0.9f - config.scale[1] / 10.0f, 0.79f);
			GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
			this.horseLegs.render(par7);
		} else if (data.type == 4) {
			GlStateManager.translate(0.0f, config.offset[1] * 1.86f, config.offset[2] * par7);
			GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
			this.mermaid.render(par7);
		} else if (data.type == 5) {
			GlStateManager.translate(0.0f, config.offset[1] * 1.86f, config.offset[2] * par7);
			GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
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
		GlStateManager.translate(config.offset[0] * par7, config.offset[1] + this.rotationPointY * par7, config.offset[2] * par7 + this.rotationPointZ * par7);
		GlStateManager.translate(0.0f, 0.0f, (config.scale[2] - 1.0f) * 5.0f * par7);
		GlStateManager.scale(config.scale[0], config.scale[1], config.scale[2]);
		this.preRender(data);
		if (data.type == 0) {
			if (data.pattern == 1) {
				this.tail.rotationPointX = -0.5f;
				this.tail.rotateAngleY -= 0.2f;
				this.tail.render(par7);
				++this.tail.rotationPointX;
				this.tail.rotateAngleY += 0.4f;
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
        this.rotationPointY = 11.0f;
		if (part.type == 2) {
			this.rotationPointY = 12.0f + (config.scale[1] - 1.0f) * 3.0f;
			this.rotationPointZ = 15.0f + (config.scale[2] - 1.0f) * 10.0f;
			if (this.npc.isPlayerSleeping() || this.npc.currentAnimation == 7) {
				this.rotationPointY = 12.0f + 16.0f * config.scale[2];
				this.rotationPointZ = config.scale[1];
				rotateAngleX = -0.7853982f;
			}
		} else if (part.type == 3) {
			this.rotationPointY = 10.0f;
			this.rotationPointZ = 16.0f + (config.scale[2] - 1.0f) * 12.0f;
		} else {
			this.rotationPointZ = 1.0f - config.scale[2];
		}
		if (partTail != null) {
			if (partTail.type == 2) {
				rotateAngleX += 0.5f;
			}
			if (partTail.type == 0) {
				rotateAngleX += 0.87f;
			}
			if (partTail.type == 7) {
				this.fox.setRotationAngles(par1, par2, par3, par4, par5, par6, this.npc);
			}
		}
		this.rotationPointZ += this.model.bipedRightLeg.rotationPointZ + 0.5f;
		this.rodent.rotateAngleX = rotateAngleX;
		this.fin.rotateAngleX = rotateAngleX;
		this.horse.rotateAngleX = rotateAngleX;
		this.squirrel.rotateAngleX = rotateAngleX;
		this.dragon.rotateAngleX = rotateAngleX;
		this.feathers.rotateAngleX = rotateAngleX;
		this.tail.rotateAngleX = rotateAngleX;
		this.fox.rotateAngleX = rotateAngleX;
		this.rodent.rotateAngleY = rotateAngleY;
		this.fin.rotateAngleY = rotateAngleY;
		this.horse.rotateAngleY = rotateAngleY;
		this.squirrel.rotateAngleY = rotateAngleY;
		this.dragon.rotateAngleY = rotateAngleY;
		this.feathers.rotateAngleY = rotateAngleY;
		this.tail.rotateAngleY = rotateAngleY;
		this.fox.rotateAngleY = rotateAngleY;
	}

	@Override
	public boolean shouldCombineTextures() {
		return true;
	}

}
