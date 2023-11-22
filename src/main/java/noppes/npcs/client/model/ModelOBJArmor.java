package noppes.npcs.client.model;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.resources.IResource;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.client.model.part.ModelOBJPatr;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.CustomArmor;
import noppes.npcs.util.ObfuscationHelper;

public class ModelOBJArmor
extends ModelBiped
{
	public ResourceLocation objModel, mainTexture;
	public ModelRenderer bipedBelt, bipedRightFeet, bipedLeftFeet;
	public boolean smallArms;

	public ModelOBJArmor(CustomArmor armor) {
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
		
		try {
			ResourceLocation location = new ResourceLocation(armor.objModel.getResourceDomain(), armor.objModel.getResourcePath().replace(".obj", ".mtl"));
			IResource res = Minecraft.getMinecraft().getResourceManager().getResource(location);
			if (res!=null) {
				String mat_lib = IOUtils.toString(res.getInputStream(), Charset.forName("UTF-8"));
				if (mat_lib.indexOf("map_Kd")!=-1) {
					int endIndex = mat_lib.indexOf(""+((char) 10), mat_lib.indexOf("map_Kd"));
					if (endIndex == -1) { endIndex = mat_lib.length(); }
					String txtr = mat_lib.substring(mat_lib.indexOf(" ", mat_lib.indexOf("map_Kd"))+1, endIndex);
					String domain = "", path = "";
					if (txtr.indexOf(":")==-1) { path = txtr; }
					else {
						domain = txtr.substring(0, txtr.indexOf(":"));
						path = txtr.substring(txtr.indexOf(":")+1);
					}
					this.mainTexture = new ResourceLocation(domain, path);
				}
			}
		}
		catch (IOException e) {}
		
		addLayer(armor);
		setVisible(true);
	}
	
	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!(entityIn instanceof EntityPlayerSP) && !(entityIn instanceof EntityNPCInterface)) { return; }
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
			this.bipedBody.render(scale);
			this.bipedRightArm.render(scale);
			this.bipedLeftArm.render(scale);
			this.bipedRightLeg.render(scale);
			this.bipedLeftLeg.render(scale);
			
			this.bipedBelt.render(scale);
			this.bipedRightFeet.render(scale);
			this.bipedLeftFeet.render(scale);
		} else {
			if (entityIn.isSneaking()) { GlStateManager.translate(0.0F, 0.2F, 0.0F); }
			this.bipedHead.render(scale);
			this.bipedBody.render(scale);
			this.bipedRightArm.render(scale);
			this.bipedLeftArm.render(scale);
			this.bipedRightLeg.render(scale);
			this.bipedLeftLeg.render(scale);
			
			this.bipedBelt.render(scale);
			this.bipedRightFeet.render(scale);
			this.bipedLeftFeet.render(scale);
		}
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setRotationAngles(float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, Entity entityIn) {
		Render<?> re = Minecraft.getMinecraft().getRenderManager().getEntityRenderObject(entityIn);
		ModelBiped source = null;
		boolean isAnimated = false;
		if (re instanceof RenderCustomNpc && ((RenderCustomNpc<?>) re).npcmodel instanceof ModelPlayerAlt) {
			source = ((RenderCustomNpc<?>) re).npcmodel;
			if (entityIn instanceof EntityNPCInterface) {
				isAnimated = ((EntityNPCInterface) entityIn).animation.isAnimated;
			}
		}
		if (re instanceof RenderPlayer) { source = ((RenderPlayer) re).getMainModel(); }
		if (source == null) { return; }
		this.reset((EntityLivingBase) entityIn);
		//System.out.println("isAnimated: "+isAnimated+" / "+source);
		resetPos(this.bipedHead, source.bipedHead, isAnimated);
		resetPos(this.bipedBody, source.bipedBody, isAnimated);
		resetPos(this.bipedBelt, source.bipedBody, isAnimated);
		resetPos(this.bipedLeftArm, source.bipedLeftArm, isAnimated);
		resetPos(this.bipedRightArm, source.bipedRightArm, isAnimated);
		resetPos(this.bipedLeftLeg, source.bipedLeftLeg, isAnimated);
		resetPos(this.bipedRightLeg, source.bipedRightLeg, isAnimated);

		resetPos(this.bipedBelt, source.bipedBody, isAnimated);
		resetPos(this.bipedLeftFeet, source.bipedLeftLeg, isAnimated);
		resetPos(this.bipedRightFeet, source.bipedRightLeg, isAnimated);
	}
	
	public void addLayer(CustomArmor armor) {
		this.bipedHead.addChild(new ModelOBJPatr(this, EnumParts.FEET_LEFT, armor.getMeshNames(EnumParts.HEAD), 0.0f, 1.5f, 0.0f));

		this.bipedBody.addChild(new ModelOBJPatr(this, EnumParts.BODY, armor.getMeshNames(EnumParts.BODY), 0.0f, 1.5f, 0.0f));
		this.bipedRightArm.addChild(new ModelOBJPatr(this, EnumParts.ARM_RIGHT, armor.getMeshNames(EnumParts.ARM_RIGHT), 0.3175f, 1.375f, 0.0f));
		this.bipedLeftArm.addChild(new ModelOBJPatr(this, EnumParts.ARM_LEFT, armor.getMeshNames(EnumParts.ARM_LEFT), -0.3175f, 1.375f, 0.0f));

		this.bipedBelt.addChild(new ModelOBJPatr(this, EnumParts.BELT, armor.getMeshNames(EnumParts.BELT), 0.0f, 1.5f, 0.0f));
		this.bipedRightLeg.addChild(new ModelOBJPatr(this, EnumParts.LEG_RIGHT, armor.getMeshNames(EnumParts.LEG_RIGHT), 0.125f, 0.75f, 0.0f));
		this.bipedLeftLeg.addChild(new ModelOBJPatr(this, EnumParts.LEG_LEFT, armor.getMeshNames(EnumParts.LEG_LEFT), -0.115f, 0.75f, 0.0f));

		this.bipedRightFeet.addChild(new ModelOBJPatr(this, EnumParts.FEET_RIGHT, armor.getMeshNames(EnumParts.FEET_RIGHT), 0.125f, 0.75f, 0.0f));
		this.bipedLeftFeet.addChild(new ModelOBJPatr(this, EnumParts.FEET_LEFT, armor.getMeshNames(EnumParts.FEET_LEFT), -0.115f, 0.75f, 0.0f));
	}
	
	public void reset(EntityLivingBase entity) {
		this.smallArms = false;
		if (entity instanceof EntityNPCInterface) {
			String m = ((EntityNPCInterface) entity).display.getModel();
			this.smallArms = m != null && m.equals("minecraft:customnpcs.customnpcalex");
		} else if (entity instanceof EntityPlayerSP) {
			Minecraft mc = Minecraft.getMinecraft();
			Render<?> rp = mc.getRenderManager().getEntityRenderObject(entity);
			if (rp instanceof RenderPlayer) {
				this.smallArms = ObfuscationHelper.getValue(RenderPlayer.class, (RenderPlayer) rp, boolean.class);
			}
		}
		
		// Initially nothing is visible
		this.setHidden(true);
		this.setVisible(false);
		
		ItemStack headItem = entity.getItemStackFromSlot(EntityEquipmentSlot.HEAD);
		if (headItem.getItem() instanceof CustomArmor && ((CustomArmor) headItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedHead.isHidden = false;
			this.bipedHead.showModel = true;
		}
		
		ItemStack cheastItem = entity.getItemStackFromSlot(EntityEquipmentSlot.CHEST);
		if (cheastItem.getItem() instanceof CustomArmor && ((CustomArmor) cheastItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedBody.isHidden = false;
			this.bipedLeftArm.isHidden = false;
			this.bipedRightArm.isHidden = false;
			this.bipedBody.showModel = true;
			this.bipedLeftArm.showModel = true;
			this.bipedRightArm.showModel = true;
		}

		ItemStack legsItem = entity.getItemStackFromSlot(EntityEquipmentSlot.LEGS);
		if (legsItem.getItem() instanceof CustomArmor && ((CustomArmor) legsItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedBelt.isHidden = false;
			this.bipedLeftLeg.isHidden = false;
			this.bipedRightLeg.isHidden = false;
			this.bipedBelt.showModel = true;
			this.bipedLeftLeg.showModel = true;
			this.bipedRightLeg.showModel = true;
		}

		ItemStack feetItem = entity.getItemStackFromSlot(EntityEquipmentSlot.FEET);
		if (feetItem.getItem() instanceof CustomArmor && ((CustomArmor) feetItem.getItem()).objModel.equals(this.objModel)) {
			this.bipedLeftFeet.isHidden = false;
			this.bipedRightFeet.isHidden = false;
			this.bipedLeftFeet.showModel = true;
			this.bipedRightFeet.showModel = true;
		}
		
		this.isSneak = entity.isSneaking();
		this.isRiding = entity.isRiding();
		this.isChild = entity.isChild();
		
	}
	
	private void resetPos(ModelRenderer part, ModelRenderer source, boolean isAnimated) {
		part.offsetX = source.offsetX;
		part.offsetY = source.offsetY;
		part.offsetZ = source.offsetZ;
		copyModelAngles(source, part);
		
		ModelOBJPatr mr = null;
		if (part.childModels!=null && part.childModels.get(0) instanceof ModelOBJPatr) {
			mr = ((ModelOBJPatr) part.childModels.get(0));
			if (!isAnimated && mr.msr != null) { mr.msr = null; }
		}
		if (isAnimated && mr != null && source instanceof ModelScaleRenderer) {
			if (mr.msr==null) { mr.msr = new ModelScaleRenderer(this, mr.part); }
			mr.msr.setAnim((ModelScaleRenderer) source);
		}
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
		
}
