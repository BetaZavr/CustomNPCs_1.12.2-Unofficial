package noppes.npcs.client.model;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.part.ModelOBJPart;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.mixin.api.client.renderer.entity.RenderPlayerAPIMixin;

import javax.annotation.Nonnull;

public class ModelOBJPlayerArmor extends ModelBiped {
	public ResourceLocation objModel, mainTexture;
	public ModelRenderer bipedBelt, bipedRightFeet, bipedLeftFeet;
	private ModelOBJPart childRightArm, childLeftArm;

	public ModelOBJPlayerArmor(CustomArmor armor) {
		super(0, 0, 128, 128);
		// Clear Basic Armor Pieces
		this.bipedHeadwear.cubeList.clear();
		this.bipedHeadwear.showModel = false;
		this.bipedHeadwear.isHidden = true;
		this.bipedHead.cubeList.clear();
		this.bipedBody.cubeList.clear();
		this.bipedLeftArm.cubeList.clear();
		this.bipedRightArm.cubeList.clear();
		this.bipedLeftLeg.cubeList.clear();
		this.bipedRightLeg.cubeList.clear();

		this.bipedBelt = new ModelRenderer(this);
		this.bipedRightFeet = new ModelRenderer(this);
		this.bipedLeftFeet = new ModelRenderer(this);
		this.objModel = armor.objModel;
		this.mainTexture = ModelBuffer.getMainOBJTexture(armor.objModel);

		addLayer(armor);
		setVisible(true);
	}

	public void addLayer(CustomArmor armor) {
		this.bipedHead.addChild(new ModelOBJPart(this, EnumParts.FEET_LEFT, armor.getMeshNames(EnumParts.HEAD), 0.0f, 1.5f, 0.0f));

		this.bipedBody.addChild(new ModelOBJPart(this, EnumParts.BODY, armor.getMeshNames(EnumParts.BODY), 0.0f, 1.5f, 0.0f));
		this.bipedBelt.addChild(new ModelOBJPart(this, EnumParts.BELT, armor.getMeshNames(EnumParts.BELT), 0.0f, 1.5f, 0.0f));

		List<String> listAR = armor.getMeshNames(EnumParts.ARM_RIGHT);
		listAR.addAll(armor.getMeshNames(EnumParts.WRIST_RIGHT));
		this.childRightArm = new ModelOBJPart(this, EnumParts.ARM_RIGHT, listAR, 0.3175f, 1.375f, 0.0f);
		this.bipedRightArm.addChild(this.childRightArm);
		
		List<String> listAL = armor.getMeshNames(EnumParts.ARM_LEFT);
		listAL.addAll(armor.getMeshNames(EnumParts.WRIST_LEFT));
		this.childLeftArm = new ModelOBJPart(this, EnumParts.ARM_LEFT, listAL, -0.3175f, 1.375f, 0.0f);
		this.bipedLeftArm.addChild(this.childLeftArm);

		List<String> listLR = armor.getMeshNames(EnumParts.LEG_RIGHT);
		listLR.addAll(armor.getMeshNames(EnumParts.FOOT_RIGHT));
		this.bipedRightLeg.addChild(new ModelOBJPart(this, EnumParts.LEG_RIGHT, listLR, 0.125f, 0.75f, 0.0f));

		List<String> listLL = armor.getMeshNames(EnumParts.LEG_LEFT);
		listLL.addAll(armor.getMeshNames(EnumParts.FOOT_LEFT));
		this.bipedLeftLeg.addChild(new ModelOBJPart(this, EnumParts.LEG_LEFT, listLL, -0.115f, 0.75f, 0.0f));

		this.bipedRightFeet.addChild(new ModelOBJPart(this, EnumParts.FEET_RIGHT, armor.getMeshNames(EnumParts.FEET_RIGHT), 0.125f, 0.75f, 0.0f));
		this.bipedLeftFeet.addChild(new ModelOBJPart(this, EnumParts.FEET_LEFT, armor.getMeshNames(EnumParts.FEET_LEFT), -0.115f, 0.75f, 0.0f));
	}

	public void render(@Nonnull Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!(entityIn instanceof EntityPlayerSP)) {
			return;
		}
		this.setRotationAngles(limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale, entityIn);
		GlStateManager.pushMatrix();
		if (this.isChild) {
			GlStateManager.scale(0.75F, 0.75F, 0.75F);
			GlStateManager.translate(0.0F, 16.0F * scale, 0.0F);
			this.bipedHead.render(scale);
			GlStateManager.popMatrix();
			
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5F, 0.5F, 0.5F);
			GlStateManager.translate(0.0F, 24.0F * scale, 0.0F);
		} else {
			if (entityIn.isSneaking()) {
				GlStateManager.translate(0.0F, 0.2F, 0.0F);
			}
			this.bipedHead.render(scale);
		}
		this.bipedBody.render(scale);
		this.bipedRightArm.render(scale);
		this.bipedLeftArm.render(scale);
		
		this.bipedRightLeg.render(scale);
		this.bipedLeftLeg.render(scale);

		this.bipedBelt.showModel = this.bipedRightLeg.showModel;
		this.bipedBelt.isHidden = this.bipedRightLeg.isHidden;
		this.bipedBelt.render(scale);
		this.bipedRightFeet.render(scale);
		this.bipedLeftFeet.render(scale);
		GlStateManager.popMatrix();
	}

	public void reset(EntityLivingBase entity) {
		boolean smallArms = false;
		if (entity instanceof EntityPlayerSP) {
			Minecraft mc = Minecraft.getMinecraft();
			Render<?> rp = mc.getRenderManager().getEntityRenderObject(entity);
			if (rp instanceof RenderPlayer) {
				smallArms = ((RenderPlayerAPIMixin) rp).npcs$getSmallArms();
			}
		}

		// Initially nothing is visible
		this.setHidden(true);
		this.setVisible(false);

		ItemStack headItem = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (headItem.getItem() instanceof CustomArmor && ((CustomArmor) headItem.getItem()).objModel != null &&
				((CustomArmor) headItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedHead.isHidden = false;
			this.bipedHead.showModel = true;
		}
		ItemStack chestItem = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (chestItem.getItem() instanceof CustomArmor
				&& ((CustomArmor) chestItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedBody.isHidden = false;
			this.bipedLeftArm.isHidden = false;
			this.bipedRightArm.isHidden = false;
			this.bipedBody.showModel = true;
			this.bipedLeftArm.showModel = true;
			this.bipedRightArm.showModel = true;

			this.childLeftArm.smallArms = smallArms;
			this.childRightArm.smallArms = smallArms;
		}

		ItemStack legsItem = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		if (legsItem.getItem() instanceof CustomArmor
				&& ((CustomArmor) legsItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedBelt.isHidden = false;
			this.bipedLeftLeg.isHidden = false;
			this.bipedRightLeg.isHidden = false;
			this.bipedBelt.showModel = true;
			this.bipedLeftLeg.showModel = true;
			this.bipedRightLeg.showModel = true;
		}

		ItemStack feetItem = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		if (feetItem.getItem() instanceof CustomArmor
				&& ((CustomArmor) feetItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedLeftFeet.isHidden = false;
			this.bipedRightFeet.isHidden = false;
			this.bipedLeftFeet.showModel = true;
			this.bipedRightFeet.showModel = true;
		}

		this.isSneak = entity.isSneaking();
		this.isRiding = entity.isRiding();
		this.isChild = entity.isChild();

	}

	private void resetPos(ModelRenderer part, ModelRenderer source) {
		part.offsetX = source.offsetX;
		part.offsetY = source.offsetY;
		part.offsetZ = source.offsetZ;
		copyModelAngles(source, part);
	}

	public void setHidden(boolean hidden) {
		this.bipedHead.isHidden = hidden;
		this.bipedLeftArm.isHidden = hidden;
		this.bipedRightArm.isHidden = hidden;
		this.bipedBody.isHidden = hidden;
		this.bipedLeftLeg.isHidden = hidden;
		this.bipedRightLeg.isHidden = hidden;

		this.bipedBelt.isHidden = hidden;
		this.bipedLeftFeet.isHidden = hidden;
		this.bipedRightFeet.isHidden = hidden;
	}

	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, @Nonnull Entity entityIn) {
		Render<?> re = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityIn);
		ModelBiped source = null;
		if (re instanceof RenderPlayer) {
			source = ((RenderPlayer) re).getMainModel();
		}
		if (source == null) {
			return;
		}
		this.reset((EntityLivingBase) entityIn);
		resetPos(this.bipedHead, source.bipedHead);
		resetPos(this.bipedBody, source.bipedBody);
		resetPos(this.bipedBelt, source.bipedBody);
		resetPos(this.bipedLeftArm, source.bipedLeftArm);
		resetPos(this.bipedRightArm, source.bipedRightArm);
		resetPos(this.bipedLeftLeg, source.bipedLeftLeg);
		resetPos(this.bipedRightLeg, source.bipedRightLeg);

		resetPos(this.bipedBelt, source.bipedBody);
		resetPos(this.bipedLeftFeet, source.bipedLeftLeg);
		resetPos(this.bipedRightFeet, source.bipedRightLeg);
	}

	public void setVisible(boolean visible) {
		this.bipedHead.showModel = visible;
		this.bipedBody.showModel = visible;
		this.bipedRightArm.showModel = visible;
		this.bipedLeftArm.showModel = visible;
		this.bipedRightLeg.showModel = visible;
		this.bipedLeftLeg.showModel = visible;

		this.bipedBelt.showModel = visible;
		this.bipedLeftFeet.showModel = visible;
		this.bipedRightFeet.showModel = visible;
	}

}
