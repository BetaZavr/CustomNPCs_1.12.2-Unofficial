package noppes.npcs.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

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
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import noppes.npcs.client.renderer.data.ParameterizedModel;

public class ModelBuffer {

	private static List<ParameterizedModel> MODELS = Lists.<ParameterizedModel>newArrayList();
	private static List<String> NOT_FOUND = Lists.<String>newArrayList();
		
	public static int getDisplayList(String objModel, float[] baseAxisOffsets, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		ParameterizedModel model = new ParameterizedModel(-1, new ResourceLocation(objModel), baseAxisOffsets, visibleMeshes, replacesMaterialTextures);
		for (ParameterizedModel pm : ModelBuffer.MODELS) {
			if (pm.equals(model)) {
				model = pm;
				break;
			}
		}
		if (model.listId==0) { model.listId = -1; }
		if (model.listId<0 && !ModelBuffer.NOT_FOUND.contains(objModel)) {
			try {
				model.iModel = (OBJModel) OBJLoader.INSTANCE.loadModel(model.file);
				if (model.iModel==null) {
					ModelBuffer.NOT_FOUND.add(objModel);
					return -1;
				}
				model.listId = GLAllocation.generateDisplayLists(1);
				GlStateManager.enableDepth();
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
				IBakedModel bakedmodel = model.iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(model.visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
				if (model.baseOffset[0]!=0.0f || model.baseOffset[1]!=0.0f || model.baseOffset[2]!=0.0f) {
					GlStateManager.translate(model.baseOffset[0], model.baseOffset[1], model.baseOffset[2]);
				}
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
			catch (Exception e) { }
		}
		return model.listId;
	}

}
