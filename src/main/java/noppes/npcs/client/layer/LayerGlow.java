package noppes.npcs.client.layer;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

@SideOnly(Side.CLIENT)
public class LayerGlow implements LayerRenderer<EntityNPCInterface> {
	private RenderCustomNpc<?> renderer;

	public LayerGlow(RenderCustomNpc<?> npcRenderer) {
		this.renderer = npcRenderer;
	}

	public void doRenderLayer(EntityNPCInterface npc, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (npc.display.getOverlayTexture().isEmpty()) {
			return;
		}
		if (npc.textureGlowLocation == null) {
			npc.textureGlowLocation = new ResourceLocation(npc.display.getOverlayTexture());
		}
		this.renderer.bindTexture(npc.textureGlowLocation);
		GlStateManager.enableBlend();
		GlStateManager.blendFunc(1, 1);
		GlStateManager.disableLighting();
		GlStateManager.depthFunc(514);
		char c0 = '\uf0f0';
		int i = c0 % 65536;
		int j = c0 / 65536;
		OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, i / 1.0f, j / 1.0f);
		GlStateManager.enableLighting();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.renderer.getMainModel().render(npc, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		this.renderer.setLightmap((EntityCustomNpc) npc);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.depthFunc(515);
	}

	public boolean shouldCombineTextures() {
		return true;
	}
}
