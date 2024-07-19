package noppes.npcs.client.renderer;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.entity.EntityProjectile;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
public class RenderProjectile<T extends Entity> extends Render<T> {

	private static final ResourceLocation ARROW_TEXTURES;

	static {
		ARROW_TEXTURES = new ResourceLocation("textures/entity/arrow.png");
		new ResourceLocation("textures/misc/enchanted_item_glint.png");
	}
	private boolean crash;
	private boolean crash2;

	public boolean renderWithColor;

	public RenderProjectile() {
		super(Minecraft.getMinecraft().getRenderManager());
		this.renderWithColor = true;
		this.crash = false;
		this.crash2 = false;
	}

	public void doRender(@Nonnull Entity entity, double par2, double par4, double par6, float par8, float par9) {
		this.doRenderProjectile((EntityProjectile) entity, par2, par4, par6, par9);
	}

	@SuppressWarnings("unchecked")
	public void doRenderProjectile(EntityProjectile projectile, double x, double y, double z, float partialTicks) {
		Minecraft mc = Minecraft.getMinecraft();
		GlStateManager.pushMatrix();
		GlStateManager.translate(x, y, z);
		GlStateManager.enableRescaleNormal();
		float scale = projectile.getSize() / 10.0f;
		ItemStack item = projectile.getItemDisplay();
		GlStateManager.scale(scale, scale, scale);
		if (projectile.isArrow()) {
			this.bindEntityTexture((T) projectile);
			GlStateManager.rotate(projectile.prevRotationYaw + (projectile.rotationYaw - projectile.prevRotationYaw) * partialTicks - 90.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(
					projectile.prevRotationPitch + (projectile.rotationPitch - projectile.prevRotationPitch) * partialTicks, 0.0f, 0.0f, 1.0f);
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder BufferBuilder = tessellator.getBuffer();
			float f = 0.0f;
			float f2 = 0.5f;
			float f3 = 0.0f;
			float f4 = 5.0f / 32.0f;
			float f5 = 0.0f;
			float f6 = 0.15625f;
			float f7 = 5.0f / 32.0f;
			float f8 = 10.0f / 32.0f;
			float f9 = 0.05625f;
			GlStateManager.enableRescaleNormal();
			float f10 = projectile.arrowShake - partialTicks;
			if (f10 > 0.0f) {
				float f11 = -MathHelper.sin(f10 * 3.0f) * f10;
				GlStateManager.rotate(f11, 0.0f, 0.0f, 1.0f);
			}
			GlStateManager.rotate(45.0f, 1.0f, 0.0f, 0.0f);
			GlStateManager.scale(f9, f9, f9);
			GlStateManager.translate(-4.0f, 0.0f, 0.0f);
			if (this.renderOutlines) {
				GlStateManager.enableColorMaterial();
				GlStateManager.enableOutlineMode(this.getTeamColor((T) projectile));
			}
			GlStateManager.glNormal3f(f9, 0.0f, 0.0f);
			BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			BufferBuilder.pos(-7.0, -2.0, -2.0).tex(f5, f7).endVertex();
			BufferBuilder.pos(-7.0, -2.0, 2.0).tex(f6, f7).endVertex();
			BufferBuilder.pos(-7.0, 2.0, 2.0).tex(f6, f8).endVertex();
			BufferBuilder.pos(-7.0, 2.0, -2.0).tex(f5, f8).endVertex();
			tessellator.draw();
			GlStateManager.glNormal3f(-f9, 0.0f, 0.0f);
			BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
			BufferBuilder.pos(-7.0, 2.0, -2.0).tex(f5, f7).endVertex();
			BufferBuilder.pos(-7.0, 2.0, 2.0).tex(f6, f7).endVertex();
			BufferBuilder.pos(-7.0, -2.0, 2.0).tex(f6, f8).endVertex();
			BufferBuilder.pos(-7.0, -2.0, -2.0).tex(f5, f8).endVertex();
			tessellator.draw();
			for (int j = 0; j < 4; ++j) {
				GlStateManager.rotate(90.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.glNormal3f(0.0f, 0.0f, f9);
				BufferBuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
				BufferBuilder.pos(-8.0, -2.0, 0.0).tex(f, f3).endVertex();
				BufferBuilder.pos(8.0, -2.0, 0.0).tex(f2, f3).endVertex();
				BufferBuilder.pos(8.0, 2.0, 0.0).tex(f2, f4).endVertex();
				BufferBuilder.pos(-8.0, 2.0, 0.0).tex(f, f4).endVertex();
				tessellator.draw();
			}
			if (this.renderOutlines) {
				GlStateManager.disableOutlineMode();
				GlStateManager.disableColorMaterial();
			}
		} else if (projectile.is3D()) {
			GlStateManager.rotate(projectile.prevRotationYaw
					+ (projectile.rotationYaw - projectile.prevRotationYaw) * partialTicks - 180.0f, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(
					projectile.prevRotationPitch
							+ (projectile.rotationPitch - projectile.prevRotationPitch) * partialTicks,
					1.0f, 0.0f, 0.0f);
			GlStateManager.translate(0.0, -0.125, 0.25);
			if (item.getItem() instanceof ItemBlock && Block.getBlockFromItem(item.getItem()).getDefaultState()
					.getRenderType() == EnumBlockRenderType.ENTITYBLOCK_ANIMATED) {
				GlStateManager.translate(0.0f, 0.1875f, -0.3125f);
				GlStateManager.rotate(20.0f, 1.0f, 0.0f, 0.0f);
				GlStateManager.rotate(45.0f, 0.0f, 1.0f, 0.0f);
				float f12 = 0.375f;
				GlStateManager.scale(-f12, -f12, f12);
			}
			if (!this.crash) {
				try {
					mc.getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND);
				} catch (Throwable e) {
					this.crash = true;
				}
			} else if (!this.crash2) {
				try {
					mc.getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.NONE);
				} catch (Throwable ee) {
					this.crash2 = true;
				}
			} else {
				mc.getRenderItem().renderItem(new ItemStack(Blocks.DIRT), ItemCameraTransforms.TransformType.GROUND);
			}
		} else {
			GlStateManager.enableRescaleNormal();
			GlStateManager.scale(0.5f, 0.5f, 0.5f);
			GlStateManager.rotate(-this.renderManager.playerViewY, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(this.renderManager.playerViewX, 1.0f, 0.0f, 0.0f);
			this.bindTexture(TextureMap.LOCATION_BLOCKS_TEXTURE);
			try {
				mc.getRenderItem().renderItem(item, ItemCameraTransforms.TransformType.NONE);
			} catch (Exception e2) {
				mc.getRenderItem().renderItem(new ItemStack(Blocks.DIRT), ItemCameraTransforms.TransformType.NONE);
			}
			GlStateManager.disableRescaleNormal();
		}
		if (projectile.is3D() && projectile.glows()) {
			GlStateManager.disableLighting();
		}
		GlStateManager.disableRescaleNormal();
		GlStateManager.popMatrix();
		GlStateManager.enableLighting();
	}

	protected ResourceLocation func_110779_a(EntityProjectile projectile) {
		return projectile.isArrow() ? RenderProjectile.ARROW_TEXTURES : TextureMap.LOCATION_BLOCKS_TEXTURE;
	}

	protected ResourceLocation getEntityTexture(@Nonnull Entity entity) {
		return this.func_110779_a((EntityProjectile) entity);
	}
}
