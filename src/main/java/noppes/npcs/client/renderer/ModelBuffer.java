package noppes.npcs.client.renderer;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.IModel;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import net.minecraftforge.client.model.obj.OBJModel.OBJState;
import noppes.npcs.util.ObfuscationHelper;

@SuppressWarnings("deprecation")
public class ModelBuffer {

	private static Map<String, Integer> MODELS = new HashMap<String, Integer>();
	
	public static boolean hasDisplayList(String objModel) {
		if (objModel==null || objModel.isEmpty()) { return false; }
		if (ModelBuffer.MODELS.containsKey(objModel)) { return true; }
		Map<ResourceLocation, OBJModel> cache = ObfuscationHelper.getValue(OBJLoader.class, OBJLoader.INSTANCE, Map.class);
		ResourceLocation res = new ResourceLocation(objModel);
		if (cache.containsKey(res)) { return true; }
		try {
			IModel iModel = OBJLoader.INSTANCE.loadModel(res);
			return iModel!=null;
		}
		catch (Exception e) { }
		return false;
	}
	
	public static int getDisplayList(String objModel) {
		if (!ModelBuffer.MODELS.containsKey(objModel)) {
			ResourceLocation res = new ResourceLocation(objModel);
			try {
				Map<ResourceLocation, OBJModel> cache = ObfuscationHelper.getValue(OBJLoader.class, OBJLoader.INSTANCE, Map.class);
				IModel iModel = cache.get(res);
				if (iModel==null) { return -1; }
				int list = GLAllocation.generateDisplayLists(1);
				GlStateManager.glNewList(list, 4864);

				Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
				IBakedModel bakedmodel = iModel.bake(new OBJState(ImmutableList.of(""), false), DefaultVertexFormats.ITEM, spriteFunction);
				
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder worldrenderer = tessellator.getBuffer();
				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				for (BakedQuad bakedquad : bakedmodel.getQuads(null, null, 0)) {
					worldrenderer.addVertexData(bakedquad.getVertexData());
				}
				tessellator.draw();
				GlStateManager.glEndList();
				ModelBuffer.MODELS.put(objModel, list);
			}
			catch (Exception e) { }
		}
		if (!ModelBuffer.MODELS.containsKey(objModel)) { return -1; }
		return ModelBuffer.MODELS.get(objModel);
	}

}
