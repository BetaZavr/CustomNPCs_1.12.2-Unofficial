package noppes.npcs.client.model;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.constants.EnumParts;

public class ModelScaleRenderer
extends ModelRenderer {
	
	public ModelPartConfig config;
	public int displayList;
	public boolean isCompiled;
	public EnumParts part;

	public ModelScaleRenderer(ModelBase modelBase, EnumParts part) {
		super(modelBase);
		this.part = part;
	}

	public ModelScaleRenderer(ModelBase modelBase, int x, int y, EnumParts part) {
		this(modelBase, part);
		this.setTextureOffset(x, y);
	}

	public void compile(float scale) {
		GlStateManager.glNewList(this.displayList = GLAllocation.generateDisplayLists(1), 4864);
		BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
		for (int i = 0; i < this.cubeList.size(); ++i) {
			this.cubeList.get(i).render(tessellator, scale);
		}
		GL11.glEndList();
		this.isCompiled = true;
	}

	public void parentRender(float scale) {
		super.render(scale);
	}

	public void postRender(float scale) {
		if (this.config != null) {
			GlStateManager.translate(this.config.offsetBase[0], this.config.offsetBase[1], this.config.offsetBase[2]);
			GlStateManager.translate(this.config.offsetAnimation[0], this.config.offsetAnimation[1], this.config.offsetAnimation[2]);
		}
		this.postRenderAnimRotate(scale); // translate model
		if (this.config != null) {
			GlStateManager.scale(this.config.scaleBase[0], this.config.scaleBase[1], this.config.scaleBase[2]);
			GlStateManager.scale(this.config.scaleAnimation[0], this.config.scaleAnimation[1], this.config.scaleAnimation[2]);
		}
	}

	public void postRenderAnimRotate(float scale) {
		if (this.isHidden || !this.showModel) { return; }
		if (!this.isCompiled) { this.compile(scale); }
		float x = this.rotateAngleX + (this.config != null ? this.config.rotateAnimation[0] : 0.0f);
		float y = this.rotateAngleY + (this.config != null ? this.config.rotateAnimation[1] : 0.0f);
		float z = this.rotateAngleZ + (this.config != null ? this.config.rotateAnimation[2] : 0.0f);
		if (x == 0.0F && y == 0.0F && z == 0.0F) {
			if (this.rotationPointX != 0.0F || this.rotationPointY != 0.0F || this.rotationPointZ != 0.0F) {
				GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
			}
		}
		else {
			GlStateManager.translate(this.rotationPointX * scale, this.rotationPointY * scale, this.rotationPointZ * scale);
			if (z != 0.0F) { GlStateManager.rotate(z * (180F / (float)Math.PI), 0.0F, 0.0F, 1.0F); }
			if (y != 0.0F) { GlStateManager.rotate(y * (180F / (float)Math.PI), 0.0F, 1.0F, 0.0F); }
			if (x != 0.0F) { GlStateManager.rotate(x * (180F / (float)Math.PI), 1.0F, 0.0F, 0.0F); }
		}
	}

	public void render(float scale) {
		if (!this.showModel || this.isHidden) { return; }
		if (!this.isCompiled) {
			this.compile(scale);
		}
		GlStateManager.pushMatrix();
		this.postRender(scale);
		GlStateManager.callList(this.displayList); // main
		if (this.childModels != null) {
			for (int i = 0; i < this.childModels.size(); ++i) {
				this.childModels.get(i).render(scale);
			}
		}
		GlStateManager.popMatrix();
	}

	public void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setAnim(Float[] values) {
		for (int i=0; i<3; i++) {
			this.config.rotateAnimation[i] = values==null || i>=values.length || values[i]==null ? 0.0f : values[i];
			this.config.offsetAnimation[i] = values==null || i+3>=values.length || values[i+3]==null ? 0.0f : values[i+3];
			this.config.scaleAnimation[i] = values==null || i+6>=values.length || values[i+6]==null ? 1.0f : values[i+6];
		}
	}

	public void clearRotation() {
		this.rotateAngleX = 0.0f;
		this.rotateAngleY = 0.0f;
		this.rotateAngleZ = 0.0f;
	}
	
}
