package noppes.npcs.client.renderer.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJLoader;
import net.minecraftforge.client.model.obj.OBJModel;
import noppes.npcs.LogWriter;

public class ParameterizedModel {

	public int listId;
	public ResourceLocation file;
	public List<String> visibleMeshes = new ArrayList<>();
	public Map<String, String> materialTextures = new HashMap<>();
	public OBJModel iModel = null;
	public IBakedModel bakedModel;
	public ResourceLocation atlas = TextureMap.LOCATION_BLOCKS_TEXTURE;

	public ParameterizedModel(int list, ResourceLocation file, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		this.listId = list;
		this.file = file;

		if (visibleMeshes != null && !visibleMeshes.isEmpty()) {
			this.visibleMeshes = visibleMeshes;
		}
		if (replacesMaterialTextures != null && !replacesMaterialTextures.isEmpty()) {
			this.materialTextures = replacesMaterialTextures;
		}
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterizedModel)) {
			return false;
		}
		ParameterizedModel objPM = (ParameterizedModel) obj;
		if (this == objPM) {
			return true;
		}
		if (!this.file.equals(objPM.file)) {
			return false;
		}
		if (this.visibleMeshes.isEmpty() && this.materialTextures.isEmpty() && objPM.visibleMeshes.isEmpty() && objPM.materialTextures.isEmpty()) {
			return true;
		}
		if (this.visibleMeshes.size() != objPM.visibleMeshes.size()) {
			if (!objPM.visibleMeshes.isEmpty()) {
				return false;
			}
		}
		if (!this.visibleMeshes.isEmpty() && !objPM.visibleMeshes.isEmpty()) {
			for (String name : this.visibleMeshes) {
				if (!objPM.visibleMeshes.contains(name)) {
					return false;
				}
			}
		}
		if (this.materialTextures.size() != objPM.materialTextures.size()) {
			if (!objPM.materialTextures.isEmpty()) {
				return false;
			}
		}
		if (!this.materialTextures.isEmpty() && !objPM.materialTextures.isEmpty()) {
			for (String name : this.materialTextures.keySet()) {
				if (!objPM.materialTextures.containsKey(name) || !objPM.materialTextures.get(name).equals(this.materialTextures.get(name))) {
					return false;
				}
			}
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public void load() throws Exception {
		iModel = (OBJModel) OBJLoader.INSTANCE.loadModel(file);
		Function<ResourceLocation, TextureAtlasSprite> spriteFunction = location -> {
			if (location.toString().equals("minecraft:missingno") || location.toString().equals("minecraft:builtin/white")) {
				return Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(location.toString());
			}
			ResourceLocation loc = location;
			if (materialTextures != null && materialTextures.containsKey(location.toString())) {
				loc = new ResourceLocation(materialTextures.get(location.toString()));
				LogWriter.debug("Replace texture: " + location + " -> " + loc);
			}
			TextureAtlasSprite sprite = Minecraft.getMinecraft().getTextureMapBlocks().getAtlasSprite(loc.toString());
			if (sprite == Minecraft.getMinecraft().getTextureMapBlocks().getMissingSprite()) {
				LogWriter.debug("Not load or found texture sprite: " + loc + " to " + file);
			}
			return sprite;
		};
		if (visibleMeshes == null || visibleMeshes.isEmpty()) { visibleMeshes = new ArrayList<>(iModel.getMatLib().getGroups().keySet()); }
		bakedModel = iModel.bake(new OBJModel.OBJState(ImmutableList.copyOf(visibleMeshes), true), DefaultVertexFormats.ITEM, spriteFunction);
	}

}
