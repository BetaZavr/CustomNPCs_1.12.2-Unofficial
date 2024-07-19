package noppes.npcs.client.renderer.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.obj.OBJModel;

public class ParameterizedModel {

	public int listId;
	public ResourceLocation file;
	public List<String> visibleMeshes;
	public Map<String, String> materialTextures;
	public OBJModel iModel;

	public ParameterizedModel(int list, ResourceLocation file, List<String> visibleMeshes, Map<String, String> replacesMaterialTextures) {
		this.listId = list;
		this.file = file;
		this.iModel = null;
		this.visibleMeshes = Lists.newArrayList();
		this.materialTextures = Maps.newHashMap();
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

}
