package noppes.npcs.client.renderer;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import noppes.npcs.client.model.ModelOBJArmor;
import noppes.npcs.client.renderer.data.ParameterizedModel;

public class ModelBuffer {

	public static Map<ResourceLocation, ModelOBJArmor> ARMORS = Maps.<ResourceLocation, ModelOBJArmor>newHashMap();
	private static List<ParameterizedModel> MODELS = Lists.<ParameterizedModel>newArrayList(); // list of parameterized rendered models
	public static List<ResourceLocation> NOT_FOUND = Lists.<ResourceLocation>newArrayList(); // list of missing models so as not to freeze the client
	
	/** Actually trying to get the sheet ID:
      * @param objModel - resource for the location of the OBJ model
      * @param visibleMeshes - list of names of meshes/grids that need to be displayed from the model
      * @param replacesMaterialTextures - texture replacement map. Key is a resource for a texture from a material, Value is a new resource texture
      * @return ID of the drawing sheet
     */
	public static int getDisplayList(ResourceLocation objModel, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		if (ModelBuffer.NOT_FOUND.contains(objModel)) { return -1; }
		ParameterizedModel model = new ParameterizedModel(-1, objModel, visibleMeshes, replacesMaterialTextures);
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
				//model.iModel.process(ImmutableMap.of("flip-v", "true"));
				model.listId = GLAllocation.generateDisplayLists(1);
				GlStateManager.glNewList(model.listId, GL11.GL_COMPILE);
				Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> {
					if (location.toString().equals("minecraft:missingno") || location.toString().equals("minecraft:builtin/white")) {
						return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
					}
					ResourceLocation loc = location;
					if (replacesMaterialTextures!=null && replacesMaterialTextures.containsKey(location.toString())) {
						loc = new ResourceLocation(replacesMaterialTextures.get(location.toString()));
						LogWriter.debug("Replase texture: "+location+" -> "+loc);
					}
					TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
					if (sprite==Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
						LogWriter.debug("Not load or found texture sprite: "+loc+" to "+objModel);
					}
			        return sprite;
				};
				if (model.visibleMeshes==null || model.visibleMeshes.size()==0) { model.visibleMeshes = Lists.<String>newArrayList(model.iModel.getMatLib().getGroups().keySet()); }
				@SuppressWarnings("deprecation")
				OBJBakedModel bakedmodel = (OBJBakedModel) model.iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(model.visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
				Tessellator tessellator = Tessellator.getInstance();
				BufferBuilder worldrenderer = tessellator.getBuffer();
				worldrenderer.begin(GL11.GL_QUADS, DefaultVertexFormats.ITEM);
				for (BakedQuad bakedquad : bakedmodel.getQuads(null, null, 0)) { worldrenderer.addVertexData(bakedquad.getVertexData()); }
				tessellator.draw();
				GlStateManager.glEndList();
				ModelBuffer.MODELS.add(model);
			}
			catch (Exception e) {
				ModelBuffer.NOT_FOUND.add(objModel);
				LogWriter.error("Error create OBJ \""+objModel+"\" render list");
				e.printStackTrace();
			}
		}
		return model.listId;
	}

}
