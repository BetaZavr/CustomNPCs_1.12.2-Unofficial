package noppes.npcs.client.layer;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.EntityLivingBase;
import noppes.npcs.client.model.ModelNpcSlime;

import javax.annotation.Nonnull;

public class LayerSlimeNpc<T extends EntityLivingBase> implements LayerRenderer<T> {

	private final RenderLiving<?> renderer;
	private final ModelBase slimeModel;

	public LayerSlimeNpc(RenderLiving<?> renderer) {
		this.slimeModel = new ModelNpcSlime(0);
		this.renderer = renderer;
	}

	@Override
	public void doRenderLayer(@Nonnull T living, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
							  float netHeadYaw, float headPitch, float scale) {
		if (living.isInvisible()) {
			return;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableNormalize();
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(770, 771);
		this.slimeModel.setModelAttributes(this.renderer.getMainModel());
		this.slimeModel.render(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		GlStateManager.disableBlend();
		GlStateManager.disableNormalize();
	}

	public boolean shouldCombineTextures() {
		return true;
	}
}
