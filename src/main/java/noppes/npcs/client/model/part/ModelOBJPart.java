package noppes.npcs.client.model.part;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.texture.TextureMap;
import noppes.npcs.client.model.ModelOBJPlayerArmor;
import noppes.npcs.client.model.ModelRendererAlt;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.constants.EnumParts;

public class ModelOBJPart
		extends ModelRenderer {

	public float x, y, z;
	public ModelOBJPlayerArmor modelBase;
	public List<String> meshes;
	public ModelRendererAlt msr;
	public EnumParts part;
	public boolean smallArms;

	public ModelOBJPart(ModelOBJPlayerArmor modelBaseIn, EnumParts partIn, List<String> meshesIn, float xIn, float yIn, float zIn) {
		super(modelBaseIn);
		modelBase = modelBaseIn;
		meshes = meshesIn;
		x = xIn;
		y = yIn;
		z = zIn;
		part = partIn;
	}

	@Override
	public void render(float scale) {
		if (isHidden || !showModel) { return; }
		int displayList = ModelBuffer.getDisplayList(modelBase.objModel, meshes, null); // get the previously
		// created render
		// sheet
		if (displayList >= 0) { // if it exists
			GlStateManager.pushMatrix();
			if (msr != null) { msr.postRender(scale); }
			float addX = 0.0f;
			if (smallArms && (part == EnumParts.ARM_LEFT || part == EnumParts.ARM_RIGHT)) {
				GlStateManager.scale(0.75f, 1.0f, 1.0f);
				addX = part == EnumParts.ARM_LEFT ? -0.0175f : 0.0175f;
			}
			if ((x + addX) != 0.0f || y != 0.0f || z != 0.0f) { GlStateManager.translate(x + addX, y, z); } // offset relative to model
			GlStateManager.rotate(180.0f, 1.0f, 0.0f, 0.0f);
			Minecraft.getMinecraft().getTextureManager().bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE); // setting a regular
			// texture
			GlStateManager.callList(displayList); // display in game
			GlStateManager.popMatrix();
		}
	}

}