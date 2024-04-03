package noppes.npcs.client.util.aw;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import moe.plushie.armourers_workshop.api.ArmourersWorkshopApi;
import moe.plushie.armourers_workshop.api.common.IExtraColours;
import moe.plushie.armourers_workshop.api.common.capability.IEntitySkinCapability;
import moe.plushie.armourers_workshop.api.common.capability.IWardrobeCap;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDescriptor;
import moe.plushie.armourers_workshop.api.common.skin.data.ISkinDye;
import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.entity.EntityCustomNpc;

public class ArmourersWorkshopUtil {

	private static ArmourersWorkshopUtil INSTANCE;
	
	public String MODID = "armourers_workshop";
	public ResourceLocation slotOutfit = new ResourceLocation(MODID, "textures/items/slot/skin-outfit.png");
	public ResourceLocation slotWings = new ResourceLocation(MODID, "textures/items/slot/skin-wings.png");
	
	public Class<?> clientProxy; // ArmourersWorkshop ClientProxy
	public Method getTexturePaintType; // ClientProxy.getTexturePaintType
	public Enum<?> TEXTURE_REPLACE; // TexturePaintType.TEXTURE_REPLACE
	
	public Object skinModelRenderHelper; // SkinModelRenderHelper
	public Method renderEquipmentPart; // SkinModelRenderHelper.renderEquipmentPart
	public Method getTypeHelperForModel; // SkinModelRenderHelper.getTypeHelperForModel
	public Enum<?> MODEL_BIPED; // SkinModelRenderHelper.ModelType.MODEL_BIPED

	public Object clientSkinCache; // ClientSkinCache
	
	public IExtraColours extraColours; // ExtraColours.EMPTY_COLOUR
	public Constructor<?> skinDyeConstructor; // SkinDye
	
	public Constructor<?> skinRenderDataConstructor; // SkinRenderData
	public Method getSkinDye; // SkinRenderData.getSkinDye
	public Method getExtraColours; // SkinRenderData.getSkinDye
	public Method isShowSkinPaint; // SkinRenderData.isShowSkinPaint
	public Method isItemRender; // SkinRenderData.isItemRender
	
	public Method getSkin; // ClientSkinCache.INSTANCE.getSkin
	
	public Object clientSkinPaintCache; // ClientSkinPaintCache.INSTANCE
	public Method getTextureForSkin; // ClientSkinPaintCache.getTextureForSkin
	
	public Method bindTexture; // SkinModelTexture.bindTexture
	
	public Method hasPaintData; // Skin.hasPaintData
	public Method getProperties; // Skin.getProperties
	public Field paintTextureId; // Skin.paintTextureId
	
	public Constructor<?> skinPartRenderDataConstructor; // SkinPartRenderData
	public Method getSkinPart; // SkinPartRenderData.getSkinPart
	public Method isDoLodLoading; // SkinPartRenderData.isDoLodLoading
	public Method getScale; // SkinPartRenderData.getScale
	
	public Method getModelForDye; // ClientSkinPartData.getModelForDye
	public Field vertexLists; // ClientSkinPartData.vertexLists

	public Class<?> configHandlerClient; // ConfigHandlerClient
	public Field renderDistanceSkin; // ConfigHandlerClient.renderDistanceSkin
	
	public Method renderVertex; // ColouredFace.renderVertex
	
	public Object skinPartRenderer; // SkinPartRenderer.INSTANCE
	public Field texture; // SkinPartRenderer.texture
	public Method renderVertexList; // SkinPartRenderer.renderVertexList
	public Method renderPart; // SkinPartRenderer.renderPart
	
	public Field npcSkinData; // ModelTypeHelper.npcSkinData
	public Field npcDyeData; // ModelTypeHelper.npcSkinData

	public Class<?> skinUtils; // SkinUtils
	public Method getFlapAngleForWings; // SkinUtils.getFlapAngleForWings
	
	public Constructor<?> advancedData; // AdvancedData
	public Constructor<?> advancedPart; // AdvancedPart
	public Method getChildren; // AdvancedPart.getChildren -> ArrayList<AdvancedPart>
	public Field posAP; // AdvancedPart.pos -> Vec3d
	public Field rotationAngle; // AdvancedPart.pos -> Vec3d
	
	public Class<?> advancedPartRenderer; // AdvancedPartRenderer.INSTANCE
	public Method renderAdvancedSkin; // AdvancedPartRenderer.renderAdvancedSkin
	
	public ArmourersWorkshopUtil() {
		ArmourersWorkshopUtil.INSTANCE = this;
		try {
			Class<?> lmi = Class.forName("moe.plushie.armourers_workshop.common.lib.LibModInfo");
			MODID = (String) lmi.getField("ID").get(lmi);
			slotOutfit = new ResourceLocation(MODID, "textures/items/slot/skin-outfit.png");
			slotWings = new ResourceLocation(MODID, "textures/items/slot/skin-wings.png");
			
			Class<?> spr = Class.forName("moe.plushie.armourers_workshop.client.render.SkinPartRenderer");
			skinPartRenderer = spr.getField("INSTANCE").get(spr);
			texture = spr.getDeclaredField("texture");
			for (Method m : spr.getDeclaredMethods()) {
				if (m.getName().equals("renderPart") &&
						m.getParameterCount() == 1 &&
						m.getParameters()[0].getType().getSimpleName().equals("SkinPartRenderData")) { renderPart = m; }
				if (m.getName().equals("renderVertexList") &&
						m.getParameterCount() == 3 &&
						m.getParameters()[1].getType().getSimpleName().equals("SkinPartRenderData") &&
						m.getParameters()[2].getType().getSimpleName().equals("ClientSkinPartData")) { renderVertexList = m; }
			}
			
			advancedPartRenderer = Class.forName("moe.plushie.armourers_workshop.client.render.AdvancedPartRenderer");
			for (Method m : spr.getDeclaredMethods()) {
				if (m.getName().equals("renderAdvancedSkin") &&
						m.getParameterCount() == 5 &&
						m.getParameters()[0].getType().getSimpleName().equals("Skin") &&
						m.getParameters()[1].getType().getSimpleName().equals("SkinRenderData") &&
						m.getParameters()[2].getType() == Entity.class &&
						m.getParameters()[3].getType().getSimpleName().equals("AdvancedData") &&
						m.getParameters()[4].getType().getSimpleName().equals("AdvancedPart")) { renderAdvancedSkin = m; break; }
			}
			
			clientProxy = Class.forName("moe.plushie.armourers_workshop.proxies.ClientProxy");
			getTexturePaintType = clientProxy.getDeclaredMethod("getTexturePaintType");
			
			for (Class<?> c : clientProxy.getClasses()) {
				if (c.isEnum()) {
					for (Object e : c.getEnumConstants()) { TEXTURE_REPLACE = (Enum<?>) e; }
					if (TEXTURE_REPLACE != null) { break; }
				}
			}

			Class<?> cspc = Class.forName("moe.plushie.armourers_workshop.client.skin.cache.ClientSkinPaintCache");
			for (Method m : cspc.getDeclaredMethods()) {
				if (m.getName().equals("getTextureForSkin") &&
						m.getParameterCount() == 3 &&
						m.getParameters()[0].getType().getSimpleName().equals("Skin") &&
						m.getParameters()[1].getType() == ISkinDye.class &&
						m.getParameters()[1].getType() == IExtraColours.class) { getTextureForSkin = m; break; }
			}
			
			configHandlerClient = Class.forName("moe.plushie.armourers_workshop.client.config.ConfigHandlerClient");
			renderDistanceSkin = configHandlerClient.getDeclaredField("renderDistanceSkin");
			
			Class<?> smrh = Class.forName("moe.plushie.armourers_workshop.client.render.SkinModelRenderHelper");
			skinModelRenderHelper = smrh.getDeclaredField("INSTANCE").get(smrh);
			for (Method m : smrh.getDeclaredMethods()) {
				if (m.getName().equals("renderEquipmentPart") &&
						m.getParameterCount() == 4 &&
						m.getParameters()[0].getType().getSimpleName().equals("Skin") &&
						m.getParameters()[1].getType().getSimpleName().equals("SkinRenderData") &&
						m.getParameters()[2].getType() == Entity.class &&
						m.getParameters()[3].getType() == ModelBiped.class) { renderEquipmentPart = m; }
				if (m.getName().equals("getTypeHelperForModel") &&
						m.getParameterCount() == 2 &&
						m.getParameters()[0].getType().getSimpleName().equals("ModelType") &&
						m.getParameters()[1].getType() == ISkinType.class) { getTypeHelperForModel = m; }
			}
			for (Class<?> c : smrh.getClasses()) {
				if (c.isEnum()) {
					MODEL_BIPED = (Enum<?>) c.getEnumConstants()[0];
				}
			}
			
			Class<?> ec = Class.forName("moe.plushie.armourers_workshop.common.capability.wardrobe.ExtraColours");
			extraColours = (IExtraColours) ec.getDeclaredField("EMPTY_COLOUR").get(ec);
			
			Class<?> sd = Class.forName("moe.plushie.armourers_workshop.common.skin.data.SkinDye");
			skinDyeConstructor = sd.getConstructor(ISkinDye.class);
			
			Class<?> srd = Class.forName("moe.plushie.armourers_workshop.client.render.SkinRenderData");
			skinRenderDataConstructor = srd.getConstructor(float.class, ISkinDye.class, IExtraColours.class, double.class, boolean.class, boolean.class, boolean.class, ResourceLocation.class);
			getSkinDye = srd.getDeclaredMethod("getSkinDye");
			getExtraColours = srd.getDeclaredMethod("getExtraColours");
			isShowSkinPaint = srd.getDeclaredMethod("isShowSkinPaint");
			isItemRender = srd.getDeclaredMethod("isItemRender");
			
			Class<?> sprd = Class.forName("moe.plushie.armourers_workshop.client.render.SkinPartRenderData");
			for (Constructor<?> c : sprd.getConstructors()) {
				if (c.getParameterCount() == 2 &&
						c.getParameters()[0].getType().getSimpleName().equals("SkinPart") &&
						c.getParameters()[1].getType().getSimpleName().equals("SkinRenderData")) { skinPartRenderDataConstructor = c; }
			}
			getSkinPart = sprd.getDeclaredMethod("getSkinPart");
			isDoLodLoading = sprd.getMethod("isDoLodLoading");
			getScale = sprd.getMethod("getScale");

			Class<?> cspd = Class.forName("moe.plushie.armourers_workshop.client.skin.ClientSkinPartData");
			vertexLists = cspd.getDeclaredFields()[1];
			for (Method m : cspd.getDeclaredMethods()) {
				if (m.getName().equals("getModelForDye") &&
						m.getParameterCount() == 1 &&
						m.getParameters()[0].getType().getSimpleName().equals("SkinPartRenderData")) { getModelForDye = m; break; }
			}
			
			Class<?> csc = Class.forName("moe.plushie.armourers_workshop.client.skin.cache.ClientSkinCache");
			clientSkinCache = csc.getDeclaredFields()[0].get(ec);
			getSkin = csc.getDeclaredMethod("getSkin", ISkinDescriptor.class);

			Class<?> smt = Class.forName("moe.plushie.armourers_workshop.client.skin.SkinModelTexture");
			bindTexture = smt.getDeclaredMethod("bindTexture");
			
			Class<?> s = Class.forName("moe.plushie.armourers_workshop.common.skin.data.Skin");
			hasPaintData = s.getDeclaredMethod("hasPaintData");
			getProperties = s.getDeclaredMethod("getProperties");
			paintTextureId = s.getDeclaredField("paintTextureId");
			
			Class<?> cf = Class.forName("moe.plushie.armourers_workshop.client.model.bake.ColouredFace");
			for (Method m : cf.getDeclaredMethods()) {
				if (m.getName().equals("renderVertex") &&
						m.getParameterCount() == 3 &&
						m.getParameters()[0].getType().getSimpleName().equals("IRenderBuffer") &&
						m.getParameters()[1].getType().getSimpleName().equals("SkinPartRenderData") &&
						m.getParameters()[2].getType().getSimpleName().equals("ClientSkinPartData")) { renderVertex = m; break; }
			}
			
			Class<?> mth = Class.forName("moe.plushie.armourers_workshop.client.model.skin.ModelTypeHelper");
			npcSkinData = mth.getDeclaredField("npcSkinData");
			npcDyeData = mth.getDeclaredField("npcDyeData");
			
			skinUtils = Class.forName("moe.plushie.armourers_workshop.utils.SkinUtils");
			for (Method m : skinUtils.getDeclaredMethods()) {
				if (m.getName().equals("getFlapAngleForWings") &&
						m.getParameterCount() == 3 &&
						m.getParameters()[0].getType() == Entity.class &&
						m.getParameters()[1].getType().getSimpleName().equals("Skin") &&
						m.getParameters()[2].getType() == int.class) { getFlapAngleForWings = m; break; }
			}

			Class<?> ad = Class.forName("moe.plushie.armourers_workshop.common.skin.advanced.AdvancedData");
			advancedData = ad.getConstructors()[0];
			
			Class<?> ap = Class.forName("moe.plushie.armourers_workshop.common.skin.advanced.AdvancedPart");
			advancedPart = ap.getConstructors()[0];
			getChildren = ap.getDeclaredMethod("getChildren");
			posAP = ap.getDeclaredField("pos");
			rotationAngle = ap.getDeclaredField("rotationAngle");
			
		}
		catch (Exception e) { }
	}
	
	public static ArmourersWorkshopUtil getInstance() {
		if (INSTANCE != null) { return INSTANCE; }
		return new ArmourersWorkshopUtil();
	}
	
	/** standard aw_mor part render */
	public void renderPart(int slotId, ItemStack stack, EntityCustomNpc entity, ModelBiped modelBiped, float scale) {
		IEntitySkinCapability skinCapability = ArmourersWorkshopApi.getEntitySkinCapability(entity);
		if (skinCapability == null) { return; }
		double distance = Minecraft.getMinecraft().player.getDistance(entity.posX, entity.posY, entity.posZ);
		int d;
		try { d = (int) renderDistanceSkin.get(configHandlerClient); } catch (Exception e1) { return; }
		if (distance > d) { return; }
		IWardrobeCap wardrobe = ArmourersWorkshopApi.getEntityWardrobeCapability(entity);
		if (wardrobe != null) { extraColours = wardrobe.getExtraColours(); }
		GlStateManager.enableRescaleNormal();
		ISkinDescriptor skinDescriptor = ArmourersWorkshopApi.getSkinNBTUtils().getSkinDescriptor(stack);
		if (skinDescriptor != null) {
			try {
				ISkin skin = (ISkin) getSkin.invoke(clientSkinCache, skinDescriptor); // Skin
				if (skin != null) {
					ISkinDye dye = (ISkinDye) skinDyeConstructor.newInstance(wardrobe.getDye());
					for (int dyeIndex = 0; dyeIndex < 8; dyeIndex++) {
						if (skinDescriptor.getSkinDye().haveDyeInSlot(dyeIndex)) { dye.addDye(dyeIndex, skinDescriptor.getSkinDye().getDyeColour(dyeIndex)); }
					}
					ResourceLocation texture = DefaultPlayerSkin.getDefaultSkinLegacy();
					Object skinRender = skinRenderDataConstructor.newInstance(scale, dye, extraColours, distance, true, true, false, texture);
					renderEquipmentPart.invoke(skinModelRenderHelper, skin, skinRender, entity, modelBiped);
				}
			}
			catch (Exception e) { e.printStackTrace(); }
		}
	}
	
}
