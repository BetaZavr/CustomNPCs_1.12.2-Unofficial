package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class ModelNpcSlime extends ModelBase {
	ModelRenderer innerBody;
	ModelRenderer outerBody;
	ModelRenderer slimeLeftEye;
	ModelRenderer slimeMouth;
	ModelRenderer slimeRightEye;

	public ModelNpcSlime(int par1) {
		this.textureHeight = 64;
		this.textureWidth = 64;
		this.outerBody = new ModelRenderer(this, 0, 0);
		(this.outerBody = new ModelRenderer(this, 0, 0)).addBox(-8.0f, 32.0f, -8.0f, 16, 16, 16);
		if (par1 > 0) {
			(this.innerBody = new ModelRenderer(this, 0, 32)).addBox(-3.0f, 17.0f, -3.0f, 6, 6, 6);
			(this.slimeRightEye = new ModelRenderer(this, 0, 0)).addBox(-3.25f, 18.0f, -3.5f, 2, 2, 2);
			(this.slimeLeftEye = new ModelRenderer(this, 0, 4)).addBox(1.25f, 18.0f, -3.5f, 2, 2, 2);
			(this.slimeMouth = new ModelRenderer(this, 0, 8)).addBox(0.0f, 21.0f, -3.5f, 1, 1, 1);
		}
	}

	public void render(@Nonnull Entity entity, float par2, float par3, float par4, float par5, float par6, float par7) {
		this.setRotationAngles(par2, par3, par4, par5, par6, par7, entity);
		if (this.innerBody != null) {
			this.innerBody.render(par7);
		} else {
			GlStateManager.pushMatrix();
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			this.outerBody.render(par7);
			GlStateManager.popMatrix();
		}
		if (this.slimeRightEye != null) {
			this.slimeRightEye.render(par7);
			this.slimeLeftEye.render(par7);
			this.slimeMouth.render(par7);
		}
	}
}
