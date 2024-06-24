package net.minecraft.client.renderer.entity;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelWrapper;
import noppes.npcs.util.ObfuscationHelper;

public class NPCRendererHelper {

	private static ModelWrapper wrapper = new ModelWrapper();

	public static void drawLayers(EntityLivingBase entity, float p_177093_2_, float p_177093_3_, float p_177093_4_,
			float p_177093_5_, float p_177093_6_, float p_177093_7_, float p_177093_8_,
			RenderLivingBase<EntityLivingBase> renderEntity) {
		renderEntity.renderLayers(entity, p_177093_2_, p_177093_3_, p_177093_4_, p_177093_5_, p_177093_6_, p_177093_7_,
				p_177093_8_);
	}

	public static String getTexture(RenderLivingBase<EntityLivingBase> render, EntityLivingBase entity) {
		ResourceLocation location = render.getEntityTexture(entity);
		if (location != null) {
			return location.toString();
		}
		return TextureMap.LOCATION_MISSING_TEXTURE.toString();
	}

	public static float handleRotationFloat(EntityLivingBase entity, float partialTicks, RenderLivingBase<EntityLivingBase> renderEntity) {
		return renderEntity.handleRotationFloat(entity, partialTicks);
	}

	public static void preRenderCallback(EntityLivingBase entity, float f, RenderLivingBase<EntityLivingBase> render) {
		render.preRenderCallback(entity, f);
	}

	public static void renderModel(EntityLivingBase entity, EntityLivingBase parent, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, RenderLivingBase<EntityLivingBase> render, ModelBase main, ResourceLocation resource) {
		NPCRendererHelper.wrapper.mainModelOld = render.getMainModel();
		if (!(main instanceof ModelWrapper)) {
			NPCRendererHelper.wrapper.wrapped = main;
			NPCRendererHelper.wrapper.texture = resource;
			render.mainModel = NPCRendererHelper.wrapper;
		}
		try {
			if (entity instanceof EntityLiving && parent instanceof EntityLiving) {
				try {
					Path path = ((EntityLiving) parent).getNavigator().getPath();
					((EntityLiving) entity).getNavigator().setPath(path, ObfuscationHelper.getValue(PathNavigate.class, ((EntityLiving) parent).getNavigator(), double.class));
					if (path != null && (netHeadYaw < -2.0f || netHeadYaw > 2.0f)) {
						entity.turn(netHeadYaw / 3.0f, headPitch / 3.0f);
						ObfuscationHelper.setValue(EntityLivingBase.class, entity, entity.rotationYaw, 58);
						ObfuscationHelper.setValue(EntityLivingBase.class, entity, entity.rotationPitch, 59);
					}
				}
				catch (Exception e) { }
			}
			render.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor);
		} catch (Exception e) {
			LogWriter.except(e);
		}
		render.mainModel = NPCRendererHelper.wrapper.mainModelOld;
	}

}
