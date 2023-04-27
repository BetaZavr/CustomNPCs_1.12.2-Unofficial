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
		this.visibleMeshes = Lists.<String>newArrayList();
		this.materialTextures = Maps.<String, String>newHashMap();
		if (visibleMeshes!=null && visibleMeshes.size()>0) { this.visibleMeshes = visibleMeshes; }
		if (replacesMaterialTextures!=null && replacesMaterialTextures.size()>0) { this.materialTextures = replacesMaterialTextures; }
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof ParameterizedModel)) { return false; }
		ParameterizedModel objPM = (ParameterizedModel) obj;
		if (this == objPM) { return true; }
		if (!this.file.equals(objPM.file)) { return false; }
		if (this.visibleMeshes.size()==0 && this.materialTextures.size()==0 && objPM.visibleMeshes.size()==0 && objPM.materialTextures.size()==0) { return true; }
		if (this.visibleMeshes.size()!=objPM.visibleMeshes.size()) {
			if (objPM.visibleMeshes.size()>0) { return false; }
		}
		if (this.visibleMeshes.size()>0 && objPM.visibleMeshes.size()>0) {
			for (String name : this.visibleMeshes) {
				if (!objPM.visibleMeshes.contains(name)) { return false; }
			}
		}
		if (this.materialTextures.size()!=objPM.materialTextures.size()) {
			if (objPM.materialTextures.size()>0) { return false; }
		}
		if (this.materialTextures.size()>0 && objPM.materialTextures.size()>0) {
			for (String name : this.materialTextures.keySet()) {
				if (!objPM.materialTextures.containsKey(name) || !objPM.materialTextures.get(name).equals(this.materialTextures.get(name))) { return false; }
			}
		}	
		return true;
	}
	
}
