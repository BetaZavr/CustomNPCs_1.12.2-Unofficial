package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;

public class ModelPlaneRenderer extends ModelRenderer {
	public enum EnumPlanePosition {
		BACK, BOTTOM, FRONT, LEFT, RIGHT, TOP;
	}

	public class ModelPlane extends ModelBox {
		private TexturedQuad quad;
		private PositionTextureVertex[] vertexs;

		public ModelPlane(ModelRenderer par1ModelRenderer, int textureOffsetX, int textureOffsetY, float par4,
				float par5, float par6, int par7, int par8, int par9, float par10, EnumPlanePosition position) {
			super(par1ModelRenderer, textureOffsetX, textureOffsetY, par4, par5, par6, par7, par8, par9, par10);
			this.vertexs = new PositionTextureVertex[8];
			float var11 = par4 + par7;
			float var12 = par5 + par8;
			float var13 = par6 + par9;
			par4 -= par10;
			par5 -= par10;
			par6 -= par10;
			var11 += par10;
			var12 += par10;
			var13 += par10;
			if (par1ModelRenderer.mirror) {
				float var14 = var11;
				var11 = par4;
				par4 = var14;
			}
			PositionTextureVertex var15 = new PositionTextureVertex(par4, par5, par6, 0.0f, 0.0f);
			PositionTextureVertex var16 = new PositionTextureVertex(var11, par5, par6, 0.0f, 8.0f);
			PositionTextureVertex var17 = new PositionTextureVertex(var11, var12, par6, 8.0f, 8.0f);
			PositionTextureVertex var18 = new PositionTextureVertex(par4, var12, par6, 8.0f, 0.0f);
			PositionTextureVertex var19 = new PositionTextureVertex(par4, par5, var13, 0.0f, 0.0f);
			PositionTextureVertex var20 = new PositionTextureVertex(var11, par5, var13, 0.0f, 8.0f);
			PositionTextureVertex var21 = new PositionTextureVertex(var11, var12, var13, 8.0f, 8.0f);
			PositionTextureVertex var22 = new PositionTextureVertex(par4, var12, var13, 8.0f, 0.0f);
			this.vertexs[0] = var15;
			this.vertexs[1] = var16;
			this.vertexs[2] = var17;
			this.vertexs[3] = var18;
			this.vertexs[4] = var19;
			this.vertexs[5] = var20;
			this.vertexs[6] = var21;
			this.vertexs[7] = var22;
			if (position == EnumPlanePosition.LEFT) {
				this.quad = new TexturedQuad(new PositionTextureVertex[] { var20, var16, var17, var21 }, textureOffsetX,
						textureOffsetY, textureOffsetX + par9, textureOffsetY + par8, par1ModelRenderer.textureWidth,
						par1ModelRenderer.textureHeight);
			}
			if (position == EnumPlanePosition.TOP) {
				this.quad = new TexturedQuad(new PositionTextureVertex[] { var20, var19, var15, var16 }, textureOffsetX,
						textureOffsetY, textureOffsetX + par7, textureOffsetY + par9, par1ModelRenderer.textureWidth,
						par1ModelRenderer.textureHeight);
			}
			if (position == EnumPlanePosition.BACK) {
				this.quad = new TexturedQuad(new PositionTextureVertex[] { var16, var15, var18, var17 }, textureOffsetX,
						textureOffsetY, textureOffsetX + par7, textureOffsetY + par8, par1ModelRenderer.textureWidth,
						par1ModelRenderer.textureHeight);
			}
			if (par1ModelRenderer.mirror) {
				this.quad.flipFace();
			}
		}

		public void render(BufferBuilder par1Tessellator, float par2) {
			this.quad.draw(par1Tessellator, par2);
		}
	}

	private int textureOffsetX;

	private int textureOffsetY;

	public ModelPlaneRenderer(ModelBase modelbase, int i, int j) {
		super(modelbase, i, j);
		this.textureOffsetX = i;
		this.textureOffsetY = j;
	}

	public void addBackPlane(float f, float f1, float f2, int i, int j) {
		this.addPlane(f, f1, f2, i, j, 0, 0.0f, EnumPlanePosition.BACK);
	}

	public void addBackPlane(float f, float f1, float f2, int i, int j, float scale) {
		this.addPlane(f, f1, f2, i, j, 0, scale, EnumPlanePosition.BACK);
	}

	public void addPlane(float par1, float par2, float par3, int par4, int par5, int par6, float f3,
			EnumPlanePosition pos) {
		this.cubeList.add(new ModelPlane(this, this.textureOffsetX, this.textureOffsetY, par1, par2, par3, par4, par5,
				par6, f3, pos));
	}

	public void addSidePlane(float f, float f1, float f2, int j, int k) {
		this.addPlane(f, f1, f2, 0, j, k, 0.0f, EnumPlanePosition.LEFT);
	}

	public void addSidePlane(float f, float f1, float f2, int j, int k, float scale) {
		this.addPlane(f, f1, f2, 0, j, k, scale, EnumPlanePosition.LEFT);
	}

	public void addTopPlane(float f, float f1, float f2, int i, int k) {
		this.addPlane(f, f1, f2, i, 0, k, 0.0f, EnumPlanePosition.TOP);
	}

	public void addTopPlane(float f, float f1, float f2, int i, int k, float scale) {
		this.addPlane(f, f1, f2, i, 0, k, scale, EnumPlanePosition.TOP);
	}
}
