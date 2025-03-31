package noppes.npcs.client.gui.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GuiNpcUtil {

    private static final Map<ResourceLocation, GuiNpcPngAnimation> itemsMap = new HashMap<>(); // Items or Blocks [texture, settings]
    private static final Map<ResourceLocation, GuiNpcPngAnimation> entitysMap = new HashMap<>(); // [texture, [frame ID, settings]]
    private static final List<ResourceLocation> notAnimated = new ArrayList<>();

    public static void drawTexturedModalRect(ResourceLocation textureLocation, int textureU, int textureV, int textureWidth, int textureHeight, float scaleSize) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(textureLocation);
        int addV = 0;
        int drawHeight = textureHeight;
        if (!notAnimated.contains(textureLocation)) {
            if (!itemsMap.containsKey(textureLocation)) {
                load(textureLocation, true);
            }
            if (itemsMap.containsKey(textureLocation)) {
                float wight = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH);
                float height = GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT);
                float frame = itemsMap.get(textureLocation).getFrameId();
                float scale = height / wight;
                drawHeight = (int) (scaleSize / scale);
                addV = (int) (frame * (float) drawHeight);
                GlStateManager.scale(1.0f, scale, 1.0f);
            }
        }
        drawTexturedModalRect(0, 0, textureU, textureV + addV, textureWidth, drawHeight);
    }

    public static void load(ResourceLocation textureLocation, boolean isItem) {
        Minecraft mc = Minecraft.getMinecraft();
        try {
            IResource res = mc.getResourceManager().getResource(new ResourceLocation(textureLocation.getResourceDomain(), textureLocation.getResourcePath() + ".mcmeta"));
            try (InputStreamReader reader = new InputStreamReader(res.getInputStream(), StandardCharsets.UTF_8)) {
                JsonParser parser = new JsonParser();
                JsonElement json = parser.parse(reader);
                if (json != null && json.getAsJsonObject().getAsJsonObject("animation") != null) {
                    JsonObject animation = json.getAsJsonObject().getAsJsonObject("animation");
                    mc.getTextureManager().bindTexture(textureLocation);
                    GuiNpcPngAnimation pngAnimation = new GuiNpcPngAnimation(GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_WIDTH),
                            GL11.glGetTexLevelParameteri(GL11.GL_TEXTURE_2D, 0, GL11.GL_TEXTURE_HEIGHT),
                            GL11.glGetInteger(GL11.GL_TEXTURE_BINDING_2D),
                            animation);
                    if (isItem) { itemsMap.put(textureLocation, pngAnimation); }
                    else {
                        pngAnimation.createEntityIDs();
                        entitysMap.put(textureLocation, pngAnimation);
                    }
                    return;
                }
            }
        } catch (Exception ignored) {}
        if (!notAnimated.contains(textureLocation)) { notAnimated.add(textureLocation); }
    }

    public static void drawTexturedModalRect(int x, int y, int textureX, int textureY, int width, int height) {
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        bufferbuilder.pos(x, y + height, 0.0d).tex((float) textureX * f, (float)(textureY + height) * f1).endVertex();
        bufferbuilder.pos(x + width, y + height, 0.0d).tex((float)(textureX + width) * f, (float)(textureY + height) * f1).endVertex();
        bufferbuilder.pos(x + width, y, 0.0d).tex((float) (textureX + width) * f, (float) textureY * f1).endVertex();
        bufferbuilder.pos(x, y, 0.0d).tex((float) textureX * f, (float) textureY * f1).endVertex();
        tessellator.draw();
    }

    public static void bindEntityTexture(TextureManager renderEngine, ResourceLocation textureLocation) {
        renderEngine.bindTexture(textureLocation);
        //notAnimated.clear();
        if (!notAnimated.contains(textureLocation)) {
            if (!entitysMap.containsKey(textureLocation)) {
                load(textureLocation, false);
            }
            if (entitysMap.containsKey(textureLocation)) {
                GlStateManager.bindTexture(entitysMap.get(textureLocation).getFrameEntityId());
            }
        }
    }

}
