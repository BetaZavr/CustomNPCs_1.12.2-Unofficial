package noppes.npcs.client.util.aw;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

import moe.plushie.armourers_workshop.api.common.skin.data.ISkin;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

@SideOnly(Side.CLIENT)
public class CustomSkinModelRenderHelper {

	private static CustomSkinModelRenderHelper INSTANCE = null;

	public static CustomSkinModelRenderHelper getInstance() {
		if (INSTANCE != null) {
			return INSTANCE;
		}
		return new CustomSkinModelRenderHelper();
	}
	private final HashMap<String, Object> helperModelsMap;

	private Method render;

	public CustomSkinModelRenderHelper() {
		INSTANCE = this;
		helperModelsMap = new HashMap<>();
		helperModelsMap.put("armourers:head", new CustomModelSkinHead());
		helperModelsMap.put("armourers:chest", new CustomModelSkinChest());
		helperModelsMap.put("armourers:legs", new CustomModelSkinLegs());
		helperModelsMap.put("armourers:feet", new CustomModelSkinFeet());
		helperModelsMap.put("armourers:outfit", new CustomModelSkinOutfit());

		try {
			Class<?> smrh = Class.forName("moe.plushie.armourers_workshop.client.render.SkinModelRenderHelper");
			Field hmm = smrh.getDeclaredField("helperModelsMap");
			hmm.setAccessible(true);

			Field modifiersField = Field.class.getDeclaredField("modifiers");
			modifiersField.setAccessible(true);
			modifiersField.setInt(hmm, hmm.getModifiers() - Modifier.FINAL - Modifier.PRIVATE + Modifier.PUBLIC);

			HashMap<?, ?> map = (HashMap<?, ?>) hmm.get(smrh.getDeclaredField("INSTANCE").get(smrh));
			for (Object key : map.keySet()) {
				if (((String) key).indexOf("armourers:head") == 0 || ((String) key).indexOf("armourers:chest") == 0
						|| ((String) key).indexOf("armourers:legs") == 0
						|| ((String) key).indexOf("armourers:feet") == 0
						|| ((String) key).indexOf("armourers:outfit") == 0) {
					continue;
				}
				helperModelsMap.put(((String) key).replace(":MODEL_BIPED", ""), map.get(key));
			}

			Class<?> iem = Class.forName("moe.plushie.armourers_workshop.client.model.skin.IEquipmentModel");
			for (Method m : iem.getDeclaredMethods()) {
				Parameter[] ps = m.getParameters();
				if (ps.length == 4 && ps[0].getType() == Entity.class && ps[2].getType() == ModelBiped.class
						&& ps[1].getType().getSimpleName().equals("Skin")
						&& ps[3].getType().getSimpleName().equals("SkinRenderData")) {
					render = m;
					break;
				}
			}
		}
		catch (Exception e) { LogWriter.error(e); }
	}

	public void renderEquipmentPart(ISkin skin, Object renderData, EntityNPCInterface npc, ModelBiped modelBiped, float scale, Map<EnumParts, Boolean> ba) {
		if (skin == null) {
			return;
		}
		try {
			String key = skin.getSkinType().getRegistryName();
			Object model = helperModelsMap.get(key);
			if (model == null) {
				return;
			}
			GlStateManager.pushMatrix();
			GlStateManager.pushAttrib();
			GlStateManager.enableCull();
			GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
			GlStateManager.enableBlend();
			GlStateManager.enableRescaleNormal();
			boolean canDraw = true;
            switch (key) {
                case "armourers:wings":
                    modelBiped.bipedBody.postRender(scale);
                    break;
                case "armourers:head":
                    ((CustomModelSkinHead) model).render(npc, skin, modelBiped, renderData, scale, ba);
                    canDraw = false;
                    break;
                case "armourers:chest":
                    ((CustomModelSkinChest) model).render(npc, skin, modelBiped, renderData, scale, ba);
                    canDraw = false;
                    break;
                case "armourers:legs":
                    ((CustomModelSkinLegs) model).render(npc, skin, modelBiped, renderData, scale, ba);
                    canDraw = false;
                    break;
                case "armourers:feet":
                    ((CustomModelSkinFeet) model).render(npc, skin, modelBiped, renderData, scale, ba);
                    canDraw = false;
                    break;
                case "armourers:outfit":
                    ((CustomModelSkinOutfit) model).render(npc, skin, modelBiped, renderData, scale, ba);
                    canDraw = false;
                    break;
            }
			if (canDraw) {
				render.invoke(model, npc, skin, modelBiped, renderData);
			}
			GlStateManager.disableRescaleNormal();
			GlStateManager.disableBlend();
			GlStateManager.disableCull();
			GlStateManager.popAttrib();
			GlStateManager.popMatrix();
		} catch (Exception e) { LogWriter.error(e); }
	}

}
