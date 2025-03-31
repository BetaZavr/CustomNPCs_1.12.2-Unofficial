package noppes.npcs.client.gui.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.nio.ByteBuffer;

public class GuiNpcPngAnimation {

    private final int mcmetaFrameTime;
    private final int mcmetaFrameSize;
    private long mcmetaCurrentTime = 0;
    private final long mcmetaTotalTime;
    private final int[] mcmetaFrames;
    private final int[] mcmetaTexturesFrames;

    public GuiNpcPngAnimation(int width, int height, int baseTextureId, JsonObject animation) {
        mcmetaFrameTime = animation.get("frametime") != null ? animation.get("frametime").getAsInt() : 0;
        if (animation.get("framesize") != null) { mcmetaFrameSize = animation.get("framesize").getAsInt(); }
        else { mcmetaFrameSize = height / width; }
        if (animation.getAsJsonArray("frames") != null) {
            JsonArray frames = animation.getAsJsonArray("frames");
            mcmetaFrames = new int[frames.size()];
            mcmetaTexturesFrames = new int[frames.size()];
            for (int i = 0; i < frames.size(); i++) {
                mcmetaFrames[i] = frames.get(i).getAsInt();
                mcmetaTexturesFrames[i] = baseTextureId;
            }
        }
        else {
            mcmetaFrames = new int[mcmetaFrameSize];
            mcmetaTexturesFrames = new int[mcmetaFrameSize];
            for (int i = 0; i < mcmetaFrames.length; i++) {
                mcmetaFrames[i] = i;
                mcmetaTexturesFrames[i] = baseTextureId;
            }
        }
        mcmetaTotalTime = (long) mcmetaFrames.length * mcmetaFrameTime;
    }

    public void createEntityIDs() {
        int textureId = GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D);
        int width = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
        int height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
        int heightSize = height / mcmetaFrameSize;
        // write current texture to baseBuffer
        ByteBuffer baseBuffer = BufferUtils.createByteBuffer(width * height * 4);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureId);
        GL11.glGetTexImage(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, baseBuffer);
        // create new textures
        for (int i = 0; i < mcmetaFrameSize; i++) {
            ByteBuffer buffer = BufferUtils.createByteBuffer(width * heightSize * 4);
            int h = i * (width * heightSize);
            for (int j = h, k = 0; j < h + width * heightSize; ++j, ++k) {
                buffer.put(k * 4, baseBuffer.get(j * 4));
                buffer.put(k * 4 + 1, baseBuffer.get(j * 4 + 1));
                buffer.put(k * 4 + 2, baseBuffer.get(j * 4 + 2));
                buffer.put(k * 4 + 3, baseBuffer.get(j * 4 + 3));
            }
            // write buffer to new texture
            mcmetaTexturesFrames[i] = GL11.glGenTextures();
            GL11.glBindTexture(GL11.GL_TEXTURE_2D, mcmetaTexturesFrames[i]);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
            GL11.glTexParameterf(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
            GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, heightSize, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);
        }
    }

    private int getFrame() {
        if (mcmetaFrameTime != 0 && mcmetaTotalTime != 0) {
            double time = (double) mcmetaCurrentTime;
            Minecraft mc = Minecraft.getMinecraft();
            if (mc.world != null) {
                time = (double) mc.world.getTotalWorldTime() % mcmetaTotalTime;
            }
            else {
                mcmetaCurrentTime++;
                if (mcmetaCurrentTime >= mcmetaTotalTime) {
                    mcmetaCurrentTime = 0L;
                }
            }
            return (int) Math.floor(time / (double) mcmetaFrameTime);
        }
        return 0;
    }

    public int getFrameId() {
        if (mcmetaFrames.length != 0) {
            return mcmetaFrames[getFrame()];
        }
        return 0;
    }

    public int getFrameEntityId() {
        if (mcmetaTexturesFrames.length != 0) {
            return mcmetaTexturesFrames[getFrame()];
        }
        return 0;
    }

}
