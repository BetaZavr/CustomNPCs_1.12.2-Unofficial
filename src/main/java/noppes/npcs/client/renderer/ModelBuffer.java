package noppes.npcs.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJBakedModel;
import noppes.npcs.LogWriter;
import noppes.npcs.client.renderer.data.ParameterizedModel;

public class ModelBuffer {

	private static List<ParameterizedModel> MODELS = Lists.<ParameterizedModel>newArrayList(); // список параметризированных отрисованных моделей
	private static List<String> NOT_FOUND = Lists.<String>newArrayList(); // список отсутствующих моделей, чтобы не фризить клиент
	
	/** Собственно попытка получить ID листа:
     * @param objModel - ресурс на расположение OBJ модели
     * @param visibleMeshes - список имён мешей/сеток, которые нужно отобразить из модели
     * @param replacesMaterialTextures - карта замены текстур. Ключ-ресурс на текстуру из материала, Значение-новый ресурс текстура
     * @return ID листа для рисовки
     */
	public static int getDisplayList(String objModel, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		if (ModelBuffer.NOT_FOUND.contains(objModel)) { return -1; }
		ParameterizedModel model = new ParameterizedModel(-1, new ResourceLocation(objModel), visibleMeshes, replacesMaterialTextures);
		for (ParameterizedModel pm : ModelBuffer.MODELS) {
			if (pm.equals(model)) {
				model = pm;
				break;
			}
		}
		if (model.listId<0) {
			try {
				model.iModel = (OBJModel) OBJLoader.INSTANCE.loadModel(model.file);
				if (model.iModel==null) {
					LogWriter.error("Error: OBJ model\""+objModel+"\" file not found");
					ModelBuffer.NOT_FOUND.add(objModel);
					return -1;
				}
				model.iModel.process(ImmutableMap.of("flip-v", "true"));
				model.listId = GLAllocation.generateDisplayLists(1);
				GlStateManager.glNewList(model.listId, GL11.GL_COMPILE);
				Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> {
					ResourceLocation loc;
					if (replacesMaterialTextures!=null && replacesMaterialTextures.containsKey(location.toString())) { loc = new ResourceLocation(replacesMaterialTextures.get(location.toString())); }
					else { loc = location; }
					TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
			        return sprite;
				};
				if (model.visibleMeshes==null || model.visibleMeshes.size()==0) { model.visibleMeshes = Lists.<String>newArrayList(model.iModel.getMatLib().getGroups().keySet()); }
				@SuppressWarnings("deprecation")
				OBJBakedModel bakedmodel = (OBJBakedModel) model.iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(model.visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder worldrenderer = tessellator.getBuffer();
				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				for (BakedQuad bakedquad : bakedmodel.getQuads(null, null, 0)) {
					worldrenderer.addVertexData(bakedquad.getVertexData());
				}
				tessellator.draw();
				GlStateManager.glEndList();
				ModelBuffer.MODELS.add(model);
			}
			catch (Exception e) {
				ModelBuffer.NOT_FOUND.add(objModel);
				LogWriter.error("Error create OBJ render list: "+e);
			}
		}
		return model.listId;
	}

}
