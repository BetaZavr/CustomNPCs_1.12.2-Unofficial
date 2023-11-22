package noppes.npcs.client.model.part;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import noppes.npcs.client.model.ModelOBJArmor;
import noppes.npcs.client.model.ModelScaleRenderer;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;

public class ModelOBJPatr
extends ModelRenderer {

	public float x, y, z;
	public ModelOBJArmor modelBase;
	public List<String> meshes;
	public List<String> feetMesh = Lists.<String>newArrayList();
	public ModelScaleRenderer msr;
	public EnumParts part;
    
    public ModelOBJPatr(ModelOBJArmor modelBase, EnumParts part, List<String> meshs, float x, float y, float z){
        super(modelBase);
        this.modelBase  = modelBase;
        this.meshes = meshs;
        this.x = x;
        this.y = y;
        this.z = z;
        this.part = part;
    }
    
    @Override
    public void render(float scale) {
        if (this.isHidden || !this.showModel) { return; }
		Map<String, String> rmt = null;
		/*if (this.modelBase.mainTexture != null && !this.modelBase.texture.isEmpty()) {
			rmt = Maps.<String, String>newHashMap();
			rmt.put(this.modelBase.mainTexture.toString(), this.modelBase.texture);
		}*/
		int displayList = ModelBuffer.getDisplayList(this.modelBase.objModel, this.meshes, rmt); // get the previously created render sheet
		if (displayList>=0) { // if it exists
			GlStateManager.pushMatrix();
			//System.out.println("CNPCs: "+this.msr);
			if (this.msr != null) {
				this.msr.postRender(scale);
			}
			float addX = 0.0f;
			if (this.modelBase.smallArms && (this.part==EnumParts.ARM_LEFT || this.part==EnumParts.ARM_RIGHT)) {
				GlStateManager.scale(0.75f, 1.0f, 1.0f);
				addX = this.part==EnumParts.ARM_LEFT ? -0.0175f : 0.0175f;
			}
			if ((this.x + addX) != 0.0f || this.y != 0.0f || this.z != 0.0f) { GlStateManager.translate(this.x + addX, this.y, this.z); } // offset relative to model
			GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE); // setting a regular texture
			GlStateManager.callList(displayList); // display in game
			GlStateManager.popMatrix();
			return;
		}
    }
	
}