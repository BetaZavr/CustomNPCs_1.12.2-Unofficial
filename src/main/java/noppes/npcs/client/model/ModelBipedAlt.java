package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import noppes.npcs.ModelData;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.model.animation.AniBow;
import noppes.npcs.client.model.animation.AniCrawling;
import noppes.npcs.client.model.animation.AniDancing;
import noppes.npcs.client.model.animation.AniHug;
import noppes.npcs.client.model.animation.AniNo;
import noppes.npcs.client.model.animation.AniPoint;
import noppes.npcs.client.model.animation.AniWaving;
import noppes.npcs.client.model.animation.AniYes;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

public class ModelBipedAlt
extends ModelBiped {
	
	private Map<EnumParts, List<ModelScaleRenderer>> map;

	public ModelBipedAlt(float scale) {
		super(scale);
		this.map = new HashMap<EnumParts, List<ModelScaleRenderer>>();
		this.bipedLeftArm = this.createScale(this.bipedLeftArm, EnumParts.ARM_LEFT);
		this.bipedRightArm = this.createScale(this.bipedRightArm, EnumParts.ARM_RIGHT);
		this.bipedLeftLeg = this.createScale(this.bipedLeftLeg, EnumParts.LEG_LEFT);
		this.bipedRightLeg = this.createScale(this.bipedRightLeg, EnumParts.LEG_RIGHT);
		this.bipedHead = this.createScale(this.bipedHead, EnumParts.HEAD);
		this.bipedHeadwear = this.createScale(this.bipedHeadwear, EnumParts.HEAD);
		this.bipedBody = this.createScale(this.bipedBody, EnumParts.BODY);
	}

	private ModelScaleRenderer createScale(ModelRenderer renderer, EnumParts part) {
		int textureX = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 2);
		int textureY = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 3);
		ModelScaleRenderer model = new ModelScaleRenderer((ModelBase) this, textureX, textureY, part);
		model.textureHeight = renderer.textureHeight;
		model.textureWidth = renderer.textureWidth;
		model.childModels = renderer.childModels;
		model.cubeList = renderer.cubeList;
		copyModelAngles(renderer, (ModelRenderer) model);
		List<ModelScaleRenderer> list = this.map.get(part);
		if (list == null) {
			this.map.put(part, list = new ArrayList<ModelScaleRenderer>());
		}
		list.add(model);
		return model;
	}

	protected EnumHandSide getMainHand(Entity entityIn) {
		if (!(entityIn instanceof EntityLivingBase) || !((EntityLivingBase) entityIn).isSwingInProgress) {
			return super.getMainHand(entityIn);
		}
		EntityLivingBase living = (EntityLivingBase) entityIn;
		if (living.swingingHand == EnumHand.MAIN_HAND) {
			return EnumHandSide.RIGHT;
		}
		return EnumHandSide.LEFT;
	}

	public ModelRenderer getRandomModelBox(Random random) {
		switch (random.nextInt(5)) {
		case 0: {
			return this.bipedHead;
		}
		case 1: {
			return this.bipedBody;
		}
		case 2: {
			return this.bipedLeftArm;
		}
		case 3: {
			return this.bipedRightArm;
		}
		case 4: {
			return this.bipedLeftLeg;
		}
		case 5: {
			return this.bipedRightLeg;
		}
		default: {
			return this.bipedHead;
		}
		}
	}

	public void setRotationAngles(float par1, float par2, float par3, float par4, float par5, float par6,
			Entity entity) {
		EntityCustomNpc npc = (EntityCustomNpc) entity;
		ModelData playerdata = npc.modelData;
		for (EnumParts part : this.map.keySet()) {
			ModelPartConfig config = playerdata.getPartConfig(part);
			for (ModelScaleRenderer model : this.map.get(part)) { model.config = config; }
		}
		if (!this.isRiding) { this.isRiding = (npc.currentAnimation == 1); }
		if (this.isSneak && (npc.currentAnimation == 7 || npc.isPlayerSleeping())) { this.isSneak = false; }
		if (npc.currentAnimation == 6) { this.rightArmPose = ModelBiped.ArmPose.BOW_AND_ARROW; }
		this.isSneak = npc.isSneaking();
		ModelRenderer bipedBody = this.bipedBody;
		ModelRenderer bipedBody2 = this.bipedBody;
		ModelRenderer bipedBody3 = this.bipedBody;
		float rotationPointX = 0.0f;
		bipedBody3.rotationPointZ = rotationPointX;
		bipedBody2.rotationPointY = rotationPointX;
		bipedBody.rotationPointX = rotationPointX;
		ModelRenderer bipedBody4 = this.bipedBody;
		ModelRenderer bipedBody5 = this.bipedBody;
		ModelRenderer bipedBody6 = this.bipedBody;
		float rotateAngleX = 0.0f;
		bipedBody6.rotateAngleZ = rotateAngleX;
		bipedBody5.rotateAngleY = rotateAngleX;
		bipedBody4.rotateAngleX = rotateAngleX;
		ModelRenderer bipedHeadwear = this.bipedHeadwear;
		ModelRenderer bipedHead = this.bipedHead;
		float n = 0.0f;
		bipedHead.rotateAngleX = n;
		bipedHeadwear.rotateAngleX = n;
		ModelRenderer bipedHeadwear2 = this.bipedHeadwear;
		ModelRenderer bipedHead2 = this.bipedHead;
		float n2 = 0.0f;
		bipedHead2.rotateAngleZ = n2;
		bipedHeadwear2.rotateAngleZ = n2;
		ModelRenderer bipedHeadwear3 = this.bipedHeadwear;
		ModelRenderer bipedHead3 = this.bipedHead;
		float n3 = 0.0f;
		bipedHead3.rotationPointX = n3;
		bipedHeadwear3.rotationPointX = n3;
		ModelRenderer bipedHeadwear4 = this.bipedHeadwear;
		ModelRenderer bipedHead4 = this.bipedHead;
		float n4 = 0.0f;
		bipedHead4.rotationPointY = n4;
		bipedHeadwear4.rotationPointY = n4;
		ModelRenderer bipedHeadwear5 = this.bipedHeadwear;
		ModelRenderer bipedHead5 = this.bipedHead;
		float n5 = 0.0f;
		bipedHead5.rotationPointZ = n5;
		bipedHeadwear5.rotationPointZ = n5;
		this.bipedLeftLeg.rotateAngleX = 0.0f;
		this.bipedLeftLeg.rotateAngleY = 0.0f;
		this.bipedLeftLeg.rotateAngleZ = 0.0f;
		this.bipedRightLeg.rotateAngleX = 0.0f;
		this.bipedRightLeg.rotateAngleY = 0.0f;
		this.bipedRightLeg.rotateAngleZ = 0.0f;
		this.bipedLeftArm.rotationPointX = 0.0f;
		this.bipedLeftArm.rotationPointY = 2.0f;
		this.bipedLeftArm.rotationPointZ = 0.0f;
		this.bipedRightArm.rotationPointX = 0.0f;
		this.bipedRightArm.rotationPointY = 2.0f;
		this.bipedRightArm.rotationPointZ = 0.0f;
		super.setRotationAngles(par1, par2, par3, par4, par5, par6, entity);
		if (npc.isPlayerSleeping()) {
			if (this.bipedHead.rotateAngleX < 0.0f) {
				this.bipedHead.rotateAngleX = 0.0f;
				this.bipedHeadwear.rotateAngleX = 0.0f;
			}
		} else if (npc.currentAnimation == 9) {
			ModelRenderer bipedHeadwear6 = this.bipedHeadwear;
			ModelRenderer bipedHead6 = this.bipedHead;
			float n6 = 0.7f;
			bipedHead6.rotateAngleX = n6;
			bipedHeadwear6.rotateAngleX = n6;
		} else if (npc.currentAnimation == 3) {
			AniHug.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 7) {
			AniCrawling.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 10) {
			AniWaving.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 5) {
			AniDancing.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 11) {
			AniBow.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 13) {
			AniYes.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 12) {
			AniNo.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (npc.currentAnimation == 8) {
			AniPoint.setRotationAngles(par1, par2, par3, par4, par5, par6, entity, this);
		} else if (this.isSneak) {
			this.bipedBody.rotateAngleX = 0.5f / playerdata.getPartConfig(EnumParts.BODY).scaleY;
		}
		
		if (npc.animation.getActive()!=null) {
			AnimationConfig anim = npc.animation.getActive();
			/*float pi = (float) Math.PI;
			float partialTicks = Minecraft.getMinecraft().getRenderPartialTicks();
			if (!npc.animation.head.disabled) {
				ModelRenderer bipedHeadwear7 = this.bipedHeadwear;
				ModelRenderer bipedHead7 = this.bipedHead;
				float n7 = npc.animation.getRotation(npc.animation.head, npc.animation.head2, 0, partialTicks) * pi;
				bipedHead7.rotateAngleX = n7;
				bipedHeadwear7.rotateAngleX = n7;
				ModelRenderer bipedHeadwear8 = this.bipedHeadwear;
				ModelRenderer bipedHead8 = this.bipedHead;
				float n8 = npc.animation.getRotation(npc.animation.head, npc.animation.head2, 1, partialTicks) * pi;
				bipedHead8.rotateAngleY = n8;
				bipedHeadwear8.rotateAngleY = n8;
				ModelRenderer bipedHeadwear9 = this.bipedHeadwear;
				ModelRenderer bipedHead9 = this.bipedHead;
				float n9 = npc.animation.getRotation(npc.animation.head, npc.animation.head2, 2, partialTicks) * pi;
				bipedHead9.rotateAngleZ = n9;
				bipedHeadwear9.rotateAngleZ = n9;
			}
			if (!npc.animation.body.disabled) {
				this.bipedBody.rotateAngleX = npc.animation.getRotation(npc.animation.body, npc.animation.body2, 0, partialTicks) * pi;
				this.bipedBody.rotateAngleY = npc.animation.getRotation(npc.animation.body, npc.animation.body2, 1, partialTicks) * pi;
				this.bipedBody.rotateAngleZ = npc.animation.getRotation(npc.animation.body, npc.animation.body2, 2, partialTicks) * pi;
			}
			if (!npc.animation.larm.disabled) {
				this.bipedLeftArm.rotateAngleX = npc.animation.getRotation(npc.animation.larm, npc.animation.larm2, 0, partialTicks) * pi;
				this.bipedLeftArm.rotateAngleY = npc.animation.getRotation(npc.animation.larm, npc.animation.larm2, 1, partialTicks) * pi;
				this.bipedLeftArm.rotateAngleZ = npc.animation.getRotation(npc.animation.larm, npc.animation.larm2, 2, partialTicks) * pi;
				if (npc.display.getHasLivingAnimation()) {
					ModelRenderer bipedLeftArm = this.bipedLeftArm;
					bipedLeftArm.rotateAngleZ -= MathHelper.cos(par3 * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedLeftArm2 = this.bipedLeftArm;
					bipedLeftArm2.rotateAngleX -= MathHelper.sin(par3 * 0.067f) * 0.05f;
				}
			}
			if (!npc.animation.rarm.disabled) {
				this.bipedRightArm.rotateAngleX = npc.animation.getRotation(npc.animation.rarm, npc.animation.rarm2, 0, partialTicks) * pi;
				this.bipedRightArm.rotateAngleY = npc.animation.getRotation(npc.animation.rarm, npc.animation.rarm2, 1, partialTicks) * pi;
				this.bipedRightArm.rotateAngleZ = npc.animation.getRotation(npc.animation.rarm, npc.animation.rarm2, 2, partialTicks) * pi;
				if (npc.display.getHasLivingAnimation()) {
					ModelRenderer bipedRightArm = this.bipedRightArm;
					bipedRightArm.rotateAngleZ += MathHelper.cos(par3 * 0.09f) * 0.05f + 0.05f;
					ModelRenderer bipedRightArm2 = this.bipedRightArm;
					bipedRightArm2.rotateAngleX += MathHelper.sin(par3 * 0.067f) * 0.05f;
				}
			}
			if (!npc.animation.rleg.disabled) {
				this.bipedRightLeg.rotateAngleX = npc.animation.getRotation(npc.animation.rleg, npc.animation.rleg2, 0, partialTicks) * pi;
				this.bipedRightLeg.rotateAngleY = npc.animation.getRotation(npc.animation.rleg, npc.animation.rleg2, 1, partialTicks) * pi;
				this.bipedRightLeg.rotateAngleZ = npc.animation.getRotation(npc.animation.rleg, npc.animation.rleg2, 2, partialTicks) * pi;
			}
			if (!npc.animation.lleg.disabled) {
				this.bipedLeftLeg.rotateAngleX = npc.animation.getRotation(npc.animation.lleg, npc.animation.lleg2, 0, partialTicks) * pi;
				this.bipedLeftLeg.rotateAngleY = npc.animation.getRotation(npc.animation.lleg, npc.animation.lleg2, 1, partialTicks) * pi;
				this.bipedLeftLeg.rotateAngleZ = npc.animation.getRotation(npc.animation.lleg, npc.animation.lleg2, 2, partialTicks) * pi;
			}*/
		}
	}
	
}
