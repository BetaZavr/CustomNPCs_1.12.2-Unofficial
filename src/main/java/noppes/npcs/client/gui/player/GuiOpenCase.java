package noppes.npcs.client.gui.player;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.renderer.ModelBuffer;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class GuiOpenCase extends GuiNPCInterface {

    protected final GuiContainer parent;
    protected final Map<ItemStack, Integer> map = new LinkedHashMap<>();
    protected final ResourceLocation objModel;
    protected final Map<String, String> materialTextures = new HashMap<>();
    protected int scrollX;
    protected int step;
    protected long startTicks = 0L;
    protected long maxTick;

    public GuiOpenCase(GuiContainer parentIn, int dealID, Map<ItemStack, Integer> mapIn) {
        super();
        drawDefaultBackground = false;
        hoverIsGame = true;
        parent = parentIn;
        map.putAll(mapIn);
        Deal deal = MarcetController.getInstance().getDeal(dealID);
        if (deal != null) {
            objModel = deal.getCaseObjModel();
            materialTextures.put("minecraft:entity/chest/christmas", deal.getCaseTexture().toString());
            if (deal.getCaseSound() != null) {
                mc.getSoundHandler().playSound(new PositionedSoundRecord(deal.getCaseSound(), SoundCategory.PLAYERS, 1.0F, 1.0F,
                        false, 0, ISound.AttenuationType.LINEAR,
                        (float) player.posX, (float) player.posY + player.eyeHeight, (float) player.posZ));
            }
        }
        else { objModel = null; }
        maxTick = 18;
        if (mc.world != null) { startTicks = mc.world.getTotalWorldTime() + maxTick; }
        step = 0;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (map.isEmpty()) {
            onGuiClosed();
            return;
        }
        //mc.options.hideGui = true;
        GlStateManager.pushMatrix();
        float rotTop = 0.0f;
        float x = width * 0.5f - 10.0f;
        float y = height * 0.5f - 19.0f;
        float caseScale = 36.0f;
        boolean isRotX = true;
        if (mc.world != null) {
            if (startTicks > 0) {
                float tick = ValueUtil.correctFloat(partialTicks + (float) (startTicks - mc.world.getTotalWorldTime()), 0.0f , Float.MAX_VALUE);
                switch (step) {
                    case 0: {
                        rotTop = 0.0f;
                        caseScale = 72.0f;
                        float sin = ValueUtil.correctFloat((float) Math.sin(90.0d * tick / (double) maxTick * Math.PI / 180.0d), 0.0f, 1.0f);
                        GlStateManager.translate(x + sin * width * 0.75f, y, 0.0f);
                        if (tick <= 0.0f) {
                            maxTick = 12;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 1;
                        }
                        break;
                    } // move to center
                    case 1: {
                        isRotX = false;
                        rotTop = 0.0f;
                        caseScale = 72.0f;
                        float cos = ValueUtil.correctFloat((float) Math.cos(90.0d * tick / (double) maxTick * Math.PI / 180.0d), 0.0f, 1.0f);
                        GlStateManager.translate(x, y, 0.0f);
                        GlStateManager.translate(8.0f, 18.0f, 36.0f);
                        GlStateManager.rotate(-30.0f, 1.0f, 0.0f, 0.0f);
                        GlStateManager.rotate(cos * -360.0f, 0.0f, 1.0f, 0.0f);
                        GlStateManager.translate(-8.0f, 0.0f, -36.0f);
                        if (tick <= 0.0f) {
                            maxTick = 10;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 2;
                        }
                        break;
                    } // rotate
                    case 2: {
                        rotTop = 0.0f;
                        caseScale = 72.0f;
                        GlStateManager.translate(x, y, 0.0f);
                        if (tick <= 0.0f) {
                            maxTick = 5;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 3;
                        }
                        break;
                    } // wait open
                    case 3: {
                        caseScale = 72.0f;
                        float a = 135.0f / (float) maxTick;
                        float b = - a * (float) maxTick;
                        rotTop = a * tick + b;
                        GlStateManager.translate(x, y, 0.0f);
                        if (tick <= 0.0f) {
                            maxTick = 6;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 4;
                        }
                        break;
                    } // open
                    case 4: {
                        rotTop = -135.0f;
                        float cos = ValueUtil.correctFloat((float) Math.cos(90.0d * tick / (double) maxTick * Math.PI / 180.0d), 0.0f, 1.0f);
                        float a = - 0.5f / (float) maxTick;
                        float b = - a * (float) maxTick;
                        drawStacks(ValueUtil.correctFloat(a * tick + b, 0.0f, 1.0f),
                                0.0f, height * -0.4f * cos, mouseX, mouseY);

                        a = 18.0f / (float) maxTick;
                        b = 72.0f - a * (float) maxTick;
                        caseScale = a * tick + b;
                        GlStateManager.translate(x, y, 0.0f);
                        if (tick <= 0.0f) {
                            maxTick = 6;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 5;
                        }
                        break;
                    } // items up
                    case 5: {
                        rotTop = -135.0f;
                        float sin = ValueUtil.correctFloat((float) Math.sin(90.0d * tick / (double) maxTick * Math.PI / 180.0d), 0.0f, 1.0f);
                        float a = -0.5f / (float) maxTick;
                        float b = 0.5f - a * (float) maxTick;
                        drawStacks(ValueUtil.correctFloat(a * tick + b, 0.0f, 1.0f),
                                0.0f, height * -0.4f * sin, mouseX, mouseY);

                        a = 18.0f / (float) maxTick;
                        b = 54.0f - a * (float) maxTick;
                        caseScale = a * tick + b;
                        GlStateManager.translate(x, y, 0.0f);
                        if (tick <= 0.0f) {
                            maxTick = 5;
                            startTicks = mc.world.getTotalWorldTime() + maxTick;
                            step = 6;
                        }
                        break;
                    } // items to center
                    case 6: {
                        rotTop = -135.0f;
                        drawStacks(1.0f, 0.0f, 0.0f, mouseX, mouseY);
                        GlStateManager.translate(x, y, 0.0f);
                        if (tick <= 0.0f) { startTicks = 0; }
                        break;
                    } // end
                    case 7: {
                        //tick = maxTick;
                        rotTop = -135.0f;
                        float a = 1.0f / (float) maxTick;
                        float b = 1.0f - a * (float) maxTick;
                        float c = - width * 0.5f / (float) maxTick;
                        float d = - c * (float) maxTick;
                        float e = - height * 0.5f / (float) maxTick;
                        float f = - e * (float) maxTick;
                        drawStacks(ValueUtil.correctFloat(a * tick + b, 0.0f, 1.0f),
                                c * tick + d, e * tick + f, mouseX, mouseY);

                        a = x / (float) maxTick;
                        b = x - a * (float) maxTick;
                        c = (y - height + 19.0f)/ (float) maxTick;
                        d = y - c * (float) maxTick;
                        GlStateManager.translate(a * tick + b, c * tick + d, 0.0f);

                        a = 24.0f / (float) maxTick;
                        b = 36.0f - a * (float) maxTick;
                        caseScale = a * tick + b;
                        if (tick <= 0.0f) { onGuiClosed(); }
                        break;
                    } // close
                }
            }
            else {
                rotTop = -135.0f;
                drawStacks(1.0f, 0.0f, 0.0f, mouseX, mouseY);
                GlStateManager.translate(x, y, 0.0f);
            }
        }
        if (isRotX) {
            GlStateManager.rotate(-30.0f, 1.0f, 0.0f, 0.0f); }
        GlStateManager.rotate(-75.0f, 0.0f, 1.0f, 0.0f);
        GlStateManager.scale(caseScale, caseScale, caseScale);
        if (rotTop != 0.0f) {
            GlStateManager.callList(ModelBuffer.getDisplayList(objModel, Collections.singletonList("body"), materialTextures));
            GlStateManager.rotate(rotTop, 0.0f, 0.0f, 1.0f);
            GlStateManager.callList(ModelBuffer.getDisplayList(objModel, Collections.singletonList("top"), materialTextures));
        }
        else { GlStateManager.callList(ModelBuffer.getDisplayList(objModel, null, materialTextures)); }
        GlStateManager.popMatrix();
        if (startTicks == 0 && step == 6) { super.drawScreen(mouseX, mouseY, partialTicks); }
    }

    @Override
    public boolean keyCnpcsPressed(char typedChar, int keyCode) {
        if (startTicks == 0 && step == 6 && keyCode == Keyboard.KEY_ESCAPE) {
            if (mc.world != null) {
                maxTick = 5;
                startTicks = mc.world.getTotalWorldTime() + maxTick;
                step = 7;
            }
        }
        return super.keyCnpcsPressed(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        if (startTicks == 0 && step == 6) { return; }
        //mc.options.hideGui = false;
        mc.displayGuiScreen(parent);
    }

    private void drawStacks(float scale, float posX, float posY, int mouseX, int mouseY) {
        if (map.isEmpty()) { return; }
        GlStateManager.pushMatrix();
        float s = 4.0f * scale;
        int h = (int) (height * 0.5f) - 32;
        int w0 = (int) (width * 0.5f) - 32;
        GlStateManager.translate(width * 0.5f - 8.0f * s + posX, height * 0.5f - 8.0f * s + posY, 150.0f);
        GlStateManager.scale(s, s, s);
        ItemStack stack;
        List<String> hovers;
        ArrayList<Map.Entry<ItemStack, Integer>> list = new ArrayList<>(map.entrySet());
        if (map.size() == 1) {
            stack = list.get(0).getKey();
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hovers
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(0).getValue()); }
                hoverText.addAll(hovers);
            }
        }
        else if (list.size() == 2) {
            stack = list.get(0).getKey();
            GlStateManager.translate(-9.0f, 0.0f, 0.0f);
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hover 0
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0 - 36, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(0).getValue()); }
                hoverText.addAll(hovers);
            }
            stack = list.get(1).getKey();
            GlStateManager.translate(18.0f, 0.0f, 0.0f);
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hover 1
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0 + 36, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(1).getValue()); }
                hoverText.addAll(hovers);
            }
        }
        else if (list.size() == 3) {
            stack = list.get(0).getKey();
            GlStateManager.translate(-18.0f, 0.0f, 0.0f);
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hover 0
            w0 -= 72;
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(0).getValue()); }
                hoverText.addAll(hovers);
            }
            for (int i = 1; i < 3; i++) {
                GlStateManager.translate(18.0f, 0.0f, 0.0f);
                stack = list.get((i + scrollX) % list.size()).getKey();
                mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
                // hover i
                if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0 + i * 72, h, 64, 64)) {
                    hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                    if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get((i + scrollX) % list.size()).getValue()); }
                    hoverText.addAll(hovers);
                }
            }
        }
        else if (list.size() == 4) {
            stack = list.get(0).getKey();
            GlStateManager.translate(-27.0f, 0.0f, 0.0f);
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hover 0
            w0 -= 108;
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(0).getValue()); }
                hoverText.addAll(hovers);
            }
            for (int i = 1; i < 4; i++) {
                GlStateManager.translate(18.0f, 0.0f, 0.0f);
                stack = list.get((i + scrollX) % list.size()).getKey();
                mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
                // hover i
                if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0 + i * 72, h, 64, 64)) {
                    hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                    if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get((i + scrollX) % list.size()).getValue()); }
                    hoverText.addAll(hovers);
                }
            }
        }
        else {
            stack = list.get(0).getKey();
            GlStateManager.translate(-36.0f, 0.0f, 0.0f);
            mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
            // hover 0
            w0 -= 144;
            if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0, h, 64, 64)) {
                hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get(0).getValue()); }
                hoverText.addAll(hovers);
            }
            for (int i = 1; i < 5; i++) {
                GlStateManager.translate(18.0f, 0.0f, 0.0f);
                stack = list.get((i + scrollX) % list.size()).getKey();
                mc.getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.NONE);
                // hover i
                if (scale == 1.0f && isMouseHover(mouseX, mouseY, w0 + i * 72, h, 64, 64)) {
                    hovers = stack.getTooltip(player, mc.gameSettings.advancedItemTooltips ? ITooltipFlag.TooltipFlags.ADVANCED : ITooltipFlag.TooltipFlags.NORMAL);
                    if (hovers.get(0) instanceof String) { hovers.set(0, hovers.get(0) + " " + TextFormatting.RESET + " x" + list.get((i + scrollX) % list.size()).getValue()); }
                    hoverText.addAll(hovers);
                }
            }
        }
        GlStateManager.popMatrix();
    }

}
