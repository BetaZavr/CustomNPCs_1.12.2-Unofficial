package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.MathHelper;

public class ModelNpcCrystal extends ModelBase {
	private ModelRenderer field_41057_g;
	private ModelRenderer field_41058_h;
	private ModelRenderer field_41059_i;
	float ticks;

	public ModelNpcCrystal(float par1) {
		this.field_41058_h = new ModelRenderer((ModelBase) this, "glass");
		this.field_41058_h.setTextureOffset(0, 0).addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
		this.field_41057_g = new ModelRenderer((ModelBase) this, "cube");
		this.field_41057_g.setTextureOffset(32, 0).addBox(-4.0f, -4.0f, -4.0f, 8, 8, 8);
		this.field_41059_i = new ModelRenderer((ModelBase) this, "base");
		this.field_41059_i.setTextureOffset(0, 16).addBox(-6.0f, 16.0f, -6.0f, 12, 4, 12);
	}

	public void render(Entity par1Entity, float par2, float par3, float par4, float par5, float par6, float par7) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(2.0f, 2.0f, 2.0f);
		GlStateManager.translate(0.0f, -0.5f, 0.0f);
		this.field_41059_i.render(par7);
		float f = par1Entity.ticksExisted + this.ticks;
		float f2 = MathHelper.sin(f * 0.2f) / 2.0f + 0.5f;
		f2 += f2 * f2;
		par3 = f * 3.0f;
		par4 = f2 * 0.2f;
		GlStateManager.rotate(par3, 0.0f, 1.0f, 0.0f);
		GlStateManager.translate(0.0f, 0.1f + par4, 0.0f);
		GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f);
		this.field_41058_h.render(par7);
		float sca = 0.875f;
		GlStateManager.scale(sca, sca, sca);
		GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f);
		GlStateManager.rotate(par3, 0.0f, 1.0f, 0.0f);
		this.field_41058_h.render(par7);
		GlStateManager.scale(sca, sca, sca);
		GlStateManager.rotate(60.0f, 0.7071f, 0.0f, 0.7071f);
		GlStateManager.rotate(par3, 0.0f, 1.0f, 0.0f);
		this.field_41057_g.render(par7);
		GlStateManager.popMatrix();
	}

	public void setLivingAnimations(EntityLivingBase par1EntityLiving, float f6, float f5, float par9) {
		this.ticks = par9;
	}
}
