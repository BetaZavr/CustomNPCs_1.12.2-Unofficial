package noppes.npcs.client.util;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import net.minecraft.client.renderer.ImageBufferDownload;

public class ImageBufferDownloadAlt extends ImageBufferDownload {

    private int[] imageData;
    private int imageWidth;

    public BufferedImage parseUserSkin(BufferedImage bufferedimage) {
        imageWidth = bufferedimage.getWidth(null);
        int imageHeight = bufferedimage.getHeight(null);
        BufferedImage buffered = new BufferedImage(imageWidth, imageHeight, 2);
        Graphics graphics = buffered.getGraphics();
        graphics.drawImage(bufferedimage, 0, 0, null);
        graphics.dispose();
        imageData = ((DataBufferInt)buffered.getRaster().getDataBuffer()).getData();
        return buffered;
    }

    public void setAreaTransparent(int par1, int par2, int par3, int par4) {
        if (!hasTransparency(par1, par2, par3, par4)) {
            for (int i1 = par1; i1 < par3; ++i1) {
                for (int j1 = par2; j1 < par4; ++j1) {
                    int n = i1 + j1 * imageWidth;
                    imageData[n] &= 0xFFFFFF;
                }
            }
        }
    }

    private boolean hasTransparency(int par1, int par2, int par3, int par4) {
        for (int i1 = par1; i1 < par3; ++i1) {
            for (int j1 = par2; j1 < par4; ++j1) {
                int k1 = imageData[i1 + j1 * imageWidth];
                if ((k1 >> 24 & 0xFF) < 128) {
                    return true;
                }
            }
        }
        return false;
    }

}
