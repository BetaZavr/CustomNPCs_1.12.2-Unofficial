package noppes.npcs.client.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.google.common.collect.Maps;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

/** Render armor, items, layers */
public class ModelBipedAlt
extends ModelBiped {
	
	private Map<EnumParts, List<ModelScaleRenderer>> map;
	private Map<Integer, Float[]> data;

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
		
		this.data = Maps.<Integer, Float[]>newTreeMap();
		for (int i=0; i<7; i++) { this.data.put(i, new Float[6]); }
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private ModelScaleRenderer createScale(ModelRenderer renderer, EnumParts part) {
		int textureX = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 2);
		int textureY = ObfuscationHelper.getValue(ModelRenderer.class, renderer, 3);
		ModelScaleRenderer model = new ModelScaleRenderer((ModelBase) this, textureX, textureY, part);
		model.textureHeight = renderer.textureHeight;
		model.textureWidth = renderer.textureWidth;
		if (renderer.childModels != null) { model.childModels = new ArrayList(renderer.childModels); }
		model.cubeList = new ArrayList(renderer.cubeList);
		copyModelAngles(renderer, (ModelRenderer) model);
		List<ModelScaleRenderer> list = this.map.get(part);
		if (list == null) { this.map.put(part, list = new ArrayList<ModelScaleRenderer>()); }
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
			case 0: return this.bipedHead;
			case 1: return this.bipedBody;
			case 2: return this.bipedLeftArm;
			case 3: return this.bipedRightArm;
			case 4: return this.bipedLeftLeg;
			case 5: return this.bipedRightLeg;
			default: return this.bipedHead;
		}
	}
	
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entity) {
		EntityCustomNpc npc = (EntityCustomNpc) entity;
		if (npc.display.renderModel instanceof ModelPlayerAlt) {
			ModelPlayerAlt mpa = (ModelPlayerAlt) npc.display.renderModel;
			if (npc.animation.isAnimated && npc.animation.playableAnimation!=null) {
				((ModelScaleRenderer) this.bipedHead).setAnim((ModelScaleRenderer) mpa.bipedHead);
				((ModelScaleRenderer) this.bipedHeadwear).setAnim((ModelScaleRenderer) mpa.bipedHeadwear);
				((ModelScaleRenderer) this.bipedBody).setAnim((ModelScaleRenderer) mpa.bipedBody);
				((ModelScaleRenderer) this.bipedRightArm).setAnim((ModelScaleRenderer) mpa.bipedRightArm);
				((ModelScaleRenderer) this.bipedLeftArm).setAnim((ModelScaleRenderer) mpa.bipedLeftArm);
				((ModelScaleRenderer) this.bipedRightLeg).setAnim((ModelScaleRenderer) mpa.bipedRightLeg);
				((ModelScaleRenderer) this.bipedLeftLeg).setAnim((ModelScaleRenderer) mpa.bipedLeftLeg);
			}
			copyModelAngles(mpa.bipedHead, this.bipedHead);
			copyModelAngles(mpa.bipedHeadwear, this.bipedHeadwear);
			copyModelAngles(mpa.bipedBody, this.bipedBody);
			copyModelAngles(mpa.bipedRightArm, this.bipedRightArm);
			copyModelAngles(mpa.bipedLeftArm, this.bipedLeftArm);
			copyModelAngles(mpa.bipedRightLeg, this.bipedRightLeg);
			copyModelAngles(mpa.bipedLeftLeg, this.bipedLeftLeg);
			this.isSneak = mpa.isSneak;
			this.isChild = mpa.isChild;
			this.isRiding = mpa.isRiding;
		}
	}
	
}
