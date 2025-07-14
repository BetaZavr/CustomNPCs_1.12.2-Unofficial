package noppes.npcs.mixin.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumRarity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.util.Util;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GuiContainer.class, priority = 499)
public class GuiContainerMixin {

    @Unique
    private static final ResourceLocation NPCS$LOCATION = new ResourceLocation(CustomNpcs.MODID, "textures/gui/slot.png");

    /**
     * @author BetaZavr
     * @reason Shows the rarity of the item in the inventory slot
     */
    @Inject(method = "drawSlot", at = @At("HEAD"))
    public void npcs$drawSlot(Slot slotIn, CallbackInfo ci) {
        if (!CustomNpcs.ShowRarityItem || slotIn.getStack().isEmpty()) { return; }
        float[] color = null; // [ red, green, blue, alpha ]
        float y = 0;
        if (slotIn.getStack().getTagCompound() != null && slotIn.getStack().getTagCompound().hasKey("RarityShow", 10)) {
            NBTTagCompound nbt = slotIn.getStack().getTagCompound().getCompoundTag("RarityShow");
            if (nbt.hasKey("color", 3) || nbt.hasKey("color", 4) || nbt.hasKey("color", 8)) {
                int c;
                if (nbt.hasKey("color", 4)) {
                    c = (int) nbt.getLong("color");
                    color = new float[] {
                            (float)(c >> 16 & 255) / 255.0F,
                            (float)(c >> 8 & 255) / 255.0F,
                            (float)(c & 255) / 255.0F,
                            (float)(c >> 24 & 255) / 255.0F
                    };
                }
                else {
                    c = -1;
                    if (nbt.hasKey("color", 3)) { c = nbt.getInteger("color"); }
                    else if (nbt.hasKey("color", 4)) { c = (int) nbt.getLong("color"); }
                    else {
                        try { c = Integer.getInteger(nbt.getString("color")); } catch (Exception ignored) { }
                    }
                    color = new float[] {
                            (float)(c >> 16 & 255) / 255.0F,
                            (float)(c >> 8 & 255) / 255.0F,
                            (float)(c & 255) / 255.0F,
                            1.0f
                    };
                }
            }
            else if (nbt.hasKey("rarity", 8)) {
                String rarity = nbt.getString("rarity");
                EnumRarity found = null;
                for (EnumRarity er : EnumRarity.values()) {
                    if (er.getName().equalsIgnoreCase(rarity)) {
                        found = er;
                        break;
                    }
                }
                if (found == null) {
                    for (EnumRarity er : EnumRarity.values()) {
                        if (er.name().equalsIgnoreCase(rarity)) {
                            found = er;
                            break;
                        }
                    }
                }
                if (found != null) {
                    color = Util.instance.getColorF(found.getColor().getColorIndex());
                }
            }
            if (nbt.hasKey("type", 3)) {
                y = nbt.getInteger("type") % 4;
            }
        }
        if (color == null) {
            int index = slotIn.getStack().getItem().getForgeRarity(slotIn.getStack()).getColor().getColorIndex();
            if (index > 14) { return; }
            color = Util.instance.getColorF(index);
        }
        if (color == null) { return; }
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.translate(slotIn.xPos, slotIn.yPos, 1.0F);
        GlStateManager.scale(0.5f, 0.5f, 1.0f);
        GlStateManager.color(color[0], color[1], color[2], color[3]);
        Minecraft.getMinecraft().getTextureManager().bindTexture(NPCS$LOCATION);
        npcs$drawTexturedModalRect(y);
        GlStateManager.disableAlpha();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Unique
    public void npcs$drawTexturedModalRect(float y) {
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferbuilder = tessellator.getBuffer();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_TEX);
        float f = 0.00390625f;
        bufferbuilder.pos(0.0d, 32.0d, 0.0d).tex(0.734375f, (1.0d + y) * 32.0d * f).endVertex(); // [188, 32]
        bufferbuilder.pos(32.0d, 32.0d, 0.0d).tex(0.859375f, (1.0d + y) * 32.0d * f).endVertex(); // [188 + 32, 32]
        bufferbuilder.pos(32.0d, 0.0d, 0.0d).tex(0.859375f, y * 32.0d * f).endVertex(); // [188 + 32, 0]
        bufferbuilder.pos(0.0d, 0.0d, 0.0d).tex(0.734375f, y * 32.0d * f).endVertex(); // [188, 0]
        tessellator.draw();
    }

}
