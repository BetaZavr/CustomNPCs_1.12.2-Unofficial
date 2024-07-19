package noppes.npcs.client;

import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;

public class TextureCache extends SimpleTexture {

	private BufferedImage bufferedImage;
	private boolean textureUploaded;

	public TextureCache(ResourceLocation location) {
		super(location);
	}

	private void checkTextureUploaded() {
		if (!this.textureUploaded && this.bufferedImage != null) {
			if (this.textureLocation != null && this.glTextureId != -1) {
				TextureUtil.deleteTexture(this.glTextureId);
				this.glTextureId = -1;
			}
			TextureUtil.uploadTextureImage(super.getGlTextureId(), this.bufferedImage);
			this.textureUploaded = true;
		}
	}

	public int getGlTextureId() {
		this.checkTextureUploaded();
		return super.getGlTextureId();
	}

	public void loadTexture(@Nonnull IResourceManager resourceManager) {
	}

	public void setImage(ResourceLocation location) {
		try {
			IResourceManager manager = Minecraft.getMinecraft().getResourceManager();
			BufferedImage bufferedimage = ImageIO.read(manager.getResource(location).getInputStream());
			int i = bufferedimage.getWidth();
			int j = bufferedimage.getHeight();
			this.bufferedImage = new BufferedImage(i * 4, j * 2, 1);
			Graphics g = this.bufferedImage.getGraphics();
			g.drawImage(bufferedimage, 0, 0, null);
			g.drawImage(bufferedimage, i, 0, null);
			g.drawImage(bufferedimage, i * 2, 0, null);
			g.drawImage(bufferedimage, i * 3, 0, null);
			g.drawImage(bufferedimage, 0, i, null);
			g.drawImage(bufferedimage, i, j, null);
			g.drawImage(bufferedimage, i * 2, j, null);
			g.drawImage(bufferedimage, i * 3, j, null);
			this.textureUploaded = false;
		} catch (Exception e) {
			LogWriter.error("Failed caching texture: " + location, e);
		}
	}
}
