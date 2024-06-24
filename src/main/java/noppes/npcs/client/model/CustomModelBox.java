package noppes.npcs.client.model;

import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.client.model.PositionTextureVertex;
import net.minecraft.client.model.TexturedQuad;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.constants.EnumParts;

public class CustomModelBox
extends ModelBox {
	
	public final PositionTextureVertex[] vertexPositions = new PositionTextureVertex[8];
	public final TexturedQuad[] quadList = new TexturedQuad[6];
    public final float posX1;
    public final float posY1;
    public final float posZ1;
    public final float posX2;
    public final float posY2;
    public final float posZ2;
    public String boxName;
	
	public CustomModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float wear, float delta, boolean isUp, EnumParts type) {
		this(renderer, texU, texV, x, y, z, dx, dy, dz, wear, delta, renderer.mirror, isUp);
		this.boxName = (type == null ? "" : type.name) + (wear != 0.0f ? "_wear" : "");
	}

	public CustomModelBox(ModelRenderer renderer, int texU, int texV, float x, float y, float z, int dx, int dy, int dz, float wear, float delta, boolean mirror, boolean isUp) {
		super(renderer, texU, texV, x, y, z, dx, dy, dz, delta, mirror);
		// start pos
		this.posX1 = x;
		this.posY1 = y;
		this.posZ1 = z;
		// end pos
		this.posX2 = x + (float) dx;
		this.posY2 = y + (float) dy;
		this.posZ2 = z + (float) dz;
		
		float width = x + (float) dx;
		float height = y + (float) dy;
		float depth = z + (float) dz;
		x = x - delta;
		y = y - delta;
		z = z - delta;
		width = width + delta;
		height = height + delta;
		depth = depth + delta;

		if (mirror) {
			float tempWidth = width;
			width = x;
			x = tempWidth;
		}
		// Front
		this.vertexPositions[0] = new PositionTextureVertex(x, y + (wear != 0.0f && !isUp ? wear : 0.0f), z, 0.0F, 0.0F); // up [xMin, yMin, zMin]
		this.vertexPositions[1] = new PositionTextureVertex(width, y + (wear != 0.0f && !isUp ? wear : 0.0f), z, 0.0F, 8.0F); // up [xMax, yMin, zMin]
		this.vertexPositions[2] = new PositionTextureVertex(width, height - (wear != 0.0f && isUp ? wear : 0.0f), z, 8.0F, 8.0F); // down [xMax, yMax, zMin]
		this.vertexPositions[3] = new PositionTextureVertex(x, height - (wear != 0.0f && isUp ? wear : 0.0f), z, 8.0F, 0.0F); // down [xMin, yMax, zMin]
		// Back
		this.vertexPositions[4] = new PositionTextureVertex(x, y + (wear != 0.0f && !isUp ? wear : 0.0f), depth, 0.0F, 0.0F); // up [xMin, yMin, zMax]
		this.vertexPositions[5] = new PositionTextureVertex(width, y + (wear != 0.0f && !isUp ? wear : 0.0f), depth, 0.0F, 8.0F); // up [xMax, yMin, zMax]
		this.vertexPositions[6] = new PositionTextureVertex(width, height - (wear != 0.0f && isUp ? wear : 0.0f), depth, 8.0F, 8.0F); // down [xMax, yMax, zMax]
		this.vertexPositions[7] = new PositionTextureVertex(x, height - (wear != 0.0f && isUp ? wear : 0.0f), depth, 8.0F, 0.0F); // down [xMin, yMax, zMax]
		
		// right 5, 1, 2, 6
		this.quadList[0] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[5], this.vertexPositions[1], this.vertexPositions[2], this.vertexPositions[6]}, texU + dz + dx, texV + dz + (isUp ? 0 : 6), texU + dz + dx + dz, texV + dz + dy + (isUp ? 0 : 6), renderer.textureWidth, renderer.textureHeight);
		// left 0, 4, 7, 3
		this.quadList[1] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[0], this.vertexPositions[4], this.vertexPositions[7], this.vertexPositions[3]}, texU, texV + dz + (isUp ? 0 : 6), texU + dz, texV + dz + dy + (isUp ? 0 : 6), renderer.textureWidth, renderer.textureHeight);
		// up 5, 4, 0, 1
		this.quadList[2] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[5], this.vertexPositions[4], this.vertexPositions[0], this.vertexPositions[1]}, texU + (isUp ? dz : dz + dx + dx), texV, texU + (isUp ? dz + dx : 2 * (dz + dx)), texV + dz, renderer.textureWidth, renderer.textureHeight);
		// down 2, 3, 7, 6
		this.quadList[3] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[2], this.vertexPositions[3], this.vertexPositions[7], this.vertexPositions[6]}, texU + (isUp ? 0 : dz + dx), texV + dz, texU + (isUp ? dz : 2 * dz + dx), texV, renderer.textureWidth, renderer.textureHeight);
		// back 1, 0, 3, 2
		this.quadList[4] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[1], this.vertexPositions[0], this.vertexPositions[3], this.vertexPositions[2]}, texU + dz, texV + dz + (isUp ? 0 : 6), texU + dz + dx, texV + dz + dy + (isUp ? 0 : 6), renderer.textureWidth, renderer.textureHeight);
		// front 4, 5, 6, 7
		this.quadList[5] = new TexturedQuad(new PositionTextureVertex[] {this.vertexPositions[4], this.vertexPositions[5], this.vertexPositions[6], this.vertexPositions[7]}, texU + dz + dx + dz, texV + dz + (isUp ? 0 : 6), texU + dz + dx + dz + dx, texV + dz + dy + (isUp ? 0 : 6), renderer.textureWidth, renderer.textureHeight);

		if (mirror) { for (TexturedQuad texturedquad : this.quadList) { texturedquad.flipFace(); } }
	}

	@SideOnly(Side.CLIENT)
	public void render(BufferBuilder renderer, float scale) {
		for (TexturedQuad texturedquad : this.quadList) { texturedquad.draw(renderer, scale); }
	}

	public ModelBox setBoxName(String name) {
		this.boxName = name;
		return this;
	}
	
}
