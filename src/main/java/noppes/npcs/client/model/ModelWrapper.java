package noppes.npcs.client.model;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;

public class ModelWrapper extends ModelBase {
	
	public ModelBase mainModelOld;
	public ResourceLocation texture;
	public ModelBase wrapped;

	public void render(Entity entityIn, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (this.texture!=null && !this.texture.getResourcePath().isEmpty()) { Minecraft.getMinecraft().getRenderManager().renderEngine.bindTexture(this.texture); }
		this.wrapped.render(entityIn, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
	}
}
