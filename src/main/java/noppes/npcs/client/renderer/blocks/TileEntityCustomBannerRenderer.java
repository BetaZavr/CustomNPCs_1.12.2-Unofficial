package noppes.npcs.client.renderer.blocks;

import javax.annotation.Nullable;

import net.minecraft.client.model.ModelBanner;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BannerTextures;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.tileentity.TileEntityBannerRenderer;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntityBanner;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.blocks.tiles.TileEntityCustomBanner;
import noppes.npcs.util.ObfuscationHelper;

public class TileEntityCustomBannerRenderer
extends TileEntityBannerRenderer {
	
	private final ModelBanner bannerModel = new ModelBanner();
	private final ModelRenderer customBannerSlate;
	
	public TileEntityCustomBannerRenderer() {
		rendererDispatcher = TileEntityRendererDispatcher.instance;
		customBannerSlate = new ModelRenderer(bannerModel, 0, 0);
		customBannerSlate.addBox(-10.0F, -32.0F, -2.0F, 20, 40, 1, 0.0F);
		ModelBox list = customBannerSlate.cubeList.get(0);
		PositionTextureVertex[] vp = ObfuscationHelper.getValue(ModelBox.class, list, 0);
		TexturedQuad[] quadList = new TexturedQuad[6];
		quadList[0] = new TexturedQuad(new PositionTextureVertex[] {vp[5], vp[1], vp[2], vp[6]}, 11, 1, 12, 17, 64, 32); // right
        quadList[1] = new TexturedQuad(new PositionTextureVertex[] {vp[0], vp[4], vp[7], vp[3]}, 0, 1, 1, 17, 64, 32); // left
        quadList[2] = new TexturedQuad(new PositionTextureVertex[] {vp[5], vp[4], vp[0], vp[1]}, 1, 0, 11, 0, 64, 32); // top
        quadList[3] = new TexturedQuad(new PositionTextureVertex[] {vp[2], vp[3], vp[7], vp[6]}, 11, 0, 21, 1, 64, 32); // bottom
        quadList[4] = new TexturedQuad(new PositionTextureVertex[] {vp[1], vp[0], vp[3], vp[2]}, 1, 1, 11, 17, 64, 32); // front
        quadList[5] = new TexturedQuad(new PositionTextureVertex[] {vp[4], vp[5], vp[6], vp[7]}, 12, 1, 22, 17, 64, 32); // back
        ObfuscationHelper.setValue(ModelBox.class, list, quadList, 1);
	}

	public void render(TileEntityBanner te, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
		boolean hasWorld = te.getWorld() != null;
		boolean isStanding = !hasWorld || te.getBlockType() == Blocks.STANDING_BANNER;
		int meta = hasWorld ? te.getBlockMetadata() : 0;
		float ticks = hasWorld ? te.getWorld().getTotalWorldTime() : 0.0f;
		GlStateManager.pushMatrix();

		if (isStanding) {
			GlStateManager.translate((float)x + 0.5F, (float)y + 0.5F, (float)z + 0.5F);
			float f1 = (float) (meta * 360) / 16.0F;
			GlStateManager.rotate(-f1, 0.0F, 1.0F, 0.0F);
			bannerModel.bannerStand.showModel = true;
		}
		else  {
			float rot = 0.0F;
			if (meta == 2) { rot = 180.0F; }
			if (meta == 4) { rot = 90.0F; }
			if (meta == 5) { rot = -90.0F; }
			GlStateManager.translate((float)x + 0.5F, (float)y - 0.16666667F, (float)z + 0.5F);
			GlStateManager.rotate(-rot, 0.0F, 1.0F, 0.0F);
			GlStateManager.translate(0.0F, -0.3125F, -0.4375F);
			bannerModel.bannerStand.showModel = false;
		}

		BlockPos blockpos = te.getPos();
		float angle = (float)(blockpos.getX() * 7 + blockpos.getY() * 9 + blockpos.getZ() * 13) + ticks + partialTicks;
		bannerModel.bannerSlate.rotateAngleX = (-0.0125F + 0.01F * MathHelper.cos(angle * (float)Math.PI * 0.02F)) * (float)Math.PI;
		GlStateManager.enableRescaleNormal();
		ResourceLocation resourcelocation = getBannerResourceLocation(te);
		if (resourcelocation != null) {
			
			GlStateManager.pushMatrix();
			
			ResourceLocation loc = null;
			if (te instanceof TileEntityCustomBanner) {
				TileEntityCustomBanner banner = (TileEntityCustomBanner) te;
				loc = banner.getFactionFlag();
			}
			float scale = 0.66666667F;
			GlStateManager.scale(scale, -scale, -scale);
			bindTexture(resourcelocation);
			bannerModel.bannerSlate.rotationPointY = -32.0F;
			if (loc != null) {
				bindTexture(loc);
				customBannerSlate.render(0.0625F);
				bindTexture(resourcelocation);
			}
			else { bannerModel.bannerSlate.render(0.0625F); }
			bannerModel.bannerStand.render(0.0625F);
			bannerModel.bannerTop.render(0.0625F);
			
			GlStateManager.popMatrix();
		}
		
		GlStateManager.color(1.0F, 1.0F, 1.0F, alpha);
		GlStateManager.popMatrix();
	}

	@Nullable
	private ResourceLocation getBannerResourceLocation(TileEntityBanner bannerObj) {
		return BannerTextures.BANNER_DESIGNS.getResourceLocation(bannerObj.getPatternResourceLocation(), bannerObj.getPatternList(), bannerObj.getColorList());
	}
	
}
