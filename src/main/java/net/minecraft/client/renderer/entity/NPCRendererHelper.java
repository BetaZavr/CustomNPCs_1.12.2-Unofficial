package net.minecraft.client.renderer.entity;

import com.google.common.collect.Maps;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.model.ModelWrapper;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.util.ObfuscationHelper;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.Map;

public class NPCRendererHelper {

	private static final ModelWrapper wrapper = new ModelWrapper();
	private static final Map<Class<?>, Method> mapApplyRotations = Maps.newHashMap();

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

	public static void renderModel(EntityLivingBase entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scaleFactor, RenderLivingBase<EntityLivingBase> render, ModelBase main, ResourceLocation resource) {
		NPCRendererHelper.wrapper.mainModelOld = render.getMainModel();
		if (!(main instanceof ModelWrapper)) {
			NPCRendererHelper.wrapper.wrapped = main;
			NPCRendererHelper.wrapper.texture = resource;
			render.mainModel = NPCRendererHelper.wrapper;
		}
		try { render.renderModel(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scaleFactor); }
		catch (Exception e) { LogWriter.except(e); }
		render.mainModel = NPCRendererHelper.wrapper.mainModelOld;
	}

	public static <T extends EntityCustomNpc> void applyRotations(RenderLivingBase<EntityLivingBase> renderEntity, T entity, float handleRotation, float rotationYaw, float partialTicks) {
		Method renderApplyRotations = getApplyRotations(renderEntity);
		if (renderApplyRotations != null) {
			try { renderApplyRotations.invoke(renderEntity, entity, handleRotation, rotationYaw, partialTicks); }
			catch (Exception e) { LogWriter.error("Error render applyRotations :", e); }
		}
	}

	private static @Nullable Method getApplyRotations(@Nullable RenderLivingBase<EntityLivingBase> renderEntity) {
		Method renderApplyRotations = null;
		if (renderEntity != null) {
			if (!mapApplyRotations.containsKey(renderEntity.getClass())) {
				renderApplyRotations = ObfuscationHelper.getMethod(renderEntity.getClass(), "applyRotations", Object.class, float.class, float.class, float.class);
				if (renderApplyRotations == null) { renderApplyRotations = ObfuscationHelper.getMethod(renderEntity.getClass(), "func_77043_a", Object.class, float.class, float.class, float.class); }
				mapApplyRotations.put(renderEntity.getClass(), renderApplyRotations);
			}
			renderApplyRotations = mapApplyRotations.get(renderEntity.getClass());
		}
		if (renderApplyRotations == null) { // base class
			if (!mapApplyRotations.containsKey(RenderLivingBase.class)) {
				renderApplyRotations = ObfuscationHelper.getMethod(RenderLivingBase.class, "applyRotations", Object.class, float.class, float.class, float.class);
				if (renderApplyRotations == null) { renderApplyRotations = ObfuscationHelper.getMethod(RenderLivingBase.class, "func_77043_a", Object.class, float.class, float.class, float.class); }
				mapApplyRotations.put(RenderLivingBase.class, renderApplyRotations);
			}
			renderApplyRotations = mapApplyRotations.get(RenderLivingBase.class);
		}
		return renderApplyRotations;
	}

}
