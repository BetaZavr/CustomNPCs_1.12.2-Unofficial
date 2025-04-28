package noppes.npcs.client.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureUtil;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SideOnly(Side.CLIENT)
public class ImageDownloadAlt extends SimpleTexture
{
    private static final Logger logger = LogManager.getLogger();
    private static final AtomicInteger threadDownloadCounter = new AtomicInteger(0);
    private final File cacheFile;
    private final String imageUrl;
    private final IImageBuffer imageBuffer;
    private BufferedImage bufferedImage;
    private Thread imageThread;
    private boolean textureUploaded;

    public ImageDownloadAlt(File file, String url, ResourceLocation resource, IImageBuffer buffer) {
        super(resource);
        cacheFile = file;
        imageUrl = url;
        imageBuffer = buffer;
    }

    private void checkTextureUploaded() {
        if (!textureUploaded && bufferedImage != null) {
            if (textureLocation != null) {
                deleteGlTexture();
            }
            TextureUtil.uploadTextureImage(super.getGlTextureId(), bufferedImage);
            textureUploaded = true;
        }
    }

    public int getGlTextureId() {
        checkTextureUploaded();
        return super.getGlTextureId();
    }

    public void setBufferedImage(BufferedImage bufferedImageIn) {
        bufferedImage = bufferedImageIn;
        if (imageBuffer != null) {
            imageBuffer.skinAvailable();
        }
    }

    public void loadTexture(@Nonnull IResourceManager resourceManager) throws IOException {
        if (bufferedImage == null && textureLocation != null) {
            super.loadTexture(resourceManager);
        }
        if (imageThread == null) {
            if (cacheFile != null && cacheFile.isFile()) {
                ImageDownloadAlt.logger.debug("Loading http texture from local cache ({})", new Object[] { cacheFile });
                try {
                    bufferedImage = ImageIO.read(cacheFile);
                    if (imageBuffer != null) {
                        setBufferedImage(imageBuffer.parseUserSkin(bufferedImage));
                    }
                }
                catch (IOException ioexception) {
                    ImageDownloadAlt.logger.error("Couldn't load skin {}", cacheFile, ioexception);
                    loadTextureFromServer();
                }
            }
            else {
                loadTextureFromServer();
            }
        }
    }

    protected void loadTextureFromServer() {
        (imageThread = new Thread("Texture Downloader #" + ImageDownloadAlt.threadDownloadCounter.incrementAndGet()) {
            @Override
            public void run() {
                HttpURLConnection connection = null;
                ImageDownloadAlt.logger.debug("Downloading http texture from {} to {}", new Object[] { imageUrl, cacheFile });
                try {
                    connection = (HttpURLConnection)new URL(imageUrl).openConnection(Minecraft.getMinecraft().getProxy());
                    connection.setConnectTimeout(20000);
                    connection.setReadTimeout(20000);
                    connection.setDoInput(true);
                    connection.setDoOutput(false);
                    connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 5.1; rv:19.0) Gecko/20100101 Firefox/19.0");
                    connection.connect();

                    String type = connection.getContentType();
                    int rCode = connection.getResponseCode();

                    BufferedImage bufferedimage;
                    if (rCode == 200 && type.equals("image/png")) {
                        FileUtils.copyInputStreamToFile(connection.getInputStream(), cacheFile);
                        bufferedimage = ImageIO.read(cacheFile);
                    }
                    else {
                        bufferedimage = TextureUtil.readBufferedImage(connection.getInputStream());
                        logger.warn("Failed to load \"{}\". ResponseCode: {}, Type \"{}\"", imageUrl, rCode, type);
                    }

                    if (imageBuffer != null) { bufferedimage = imageBuffer.parseUserSkin(bufferedimage); }
                    setBufferedImage(bufferedimage);
                }
                catch (Exception exception) { logger.error("Couldn't download http texture", exception); }
                finally {
                    if (connection != null) { connection.disconnect(); }
                }
            }
        }).setDaemon(true);
        imageThread.start();
    }

}
