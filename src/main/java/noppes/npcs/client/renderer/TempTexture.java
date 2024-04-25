package noppes.npcs.client.renderer;

import java.awt.image.BufferedImage;
import java.io.IOException;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class TempTexture extends AbstractTexture {

	protected final ResourceLocation textureLocation;
	private final BufferedImage bufferedImage;

	public TempTexture(ResourceLocation textureResourceLocation, BufferedImage bufferedImage) {
		this.textureLocation = textureResourceLocation;
		this.bufferedImage = bufferedImage;
	}

	public void loadTexture(IResourceManager resourceManager) throws IOException {
		this.deleteGlTexture();
		try {
			TextureUtil.uploadTextureImage(this.getGlTextureId(), this.bufferedImage);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
