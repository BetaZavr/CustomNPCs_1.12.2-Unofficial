package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiColorSelector;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.DialogGuiSettings;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

public class GuiNpcDialogGuiSettings extends GuiNPCInterface2
        implements ISliderListener, ITextfieldListener {

    protected final ResourceLocation[] resources = {
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/overworld.png"),
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/netherworld.png"),
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/endworld.png")
    };
    protected final DialogGuiSettings guiSettings;
    protected final EntityNPCInterface dialogNpc;
    protected int dialogHeight;
    protected ScaledResolution sw = new ScaledResolution(mc);
    protected int screenID = 0;

    public GuiNpcDialogGuiSettings(EntityNPCInterface npc) {
        super(npc);
        closeOnEsc = true;
        parentGui = EnumGuiType.MainMenuGlobal;

        guiSettings = DialogController.instance.getGuiSettings();
        dialogNpc = Util.instance.copyToGUI(npc != null ? npc : (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), mc.world), mc.world, false);
        dialogNpc.display.setVisible(0);
    }

    @Override
    public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
        if (mouseButton != 0) { return; }
        switch (button.getID()) {
            case 1: guiSettings.setType(button.getValue()); initGui(); break;
            case 2: guiSettings.npcInLeft = ((GuiNpcCheckBox) button).isSelected(); initGui(); break;
            case 3: guiSettings.showNPC = ((GuiNpcCheckBox) button).isSelected(); initGui(); break;
            case 4: {
                setSubGui(new SubGuiColorSelector(guiSettings.backWindowColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.backWindowColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.backWindowColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // window color
            case 5: {
                setSubGui(new SubGuiColorSelector(guiSettings.backBorderColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.backBorderColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.backBorderColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // background color
            case 6: setSubGui(new SubGuiTextureSelection(0, npc, guiSettings.getBackgroundTexture() == null ? "" : guiSettings.getBackgroundTexture().toString(), ".png", 3)); break;
            case 7: setSubGui(new SubGuiTextureSelection(1, npc, guiSettings.getWindowTexture() == null ? "" : guiSettings.getWindowTexture().toString(), ".png", 3)); break;
            case 8: guiSettings.setShowVerticalLines(button.getValue()); break;
            case 9: {
                setSubGui(new SubGuiColorSelector(guiSettings.linesColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.linesColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.linesColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // lines color
            case 10: guiSettings.setShowHorizontalLines(button.getValue()); break;
            case 13: {
                setSubGui(new SubGuiColorSelector(guiSettings.selectOptionLeftColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.selectOptionLeftColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.selectOptionLeftColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // select back color
            case 14: {
                setSubGui(new SubGuiColorSelector(guiSettings.pointerColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.pointerColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.pointerColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // pointer color
            case 15: {
                setSubGui(new SubGuiColorSelector(guiSettings.sliderColor, new SubGuiColorSelector.ColorCallback() {
                    @Override
                    public void color(int colorIn) { guiSettings.sliderColor = colorIn; }
                    @Override
                    public void preColor(int colorIn) { guiSettings.sliderColor = colorIn; }
                }).setIsAlpha().setOffsetX(-100));
                break;
            } // slider color
            case 100: screenID = button.getValue() - 1; break;
            case 66: onClosed(); break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
        // window:
        int x = guiLeft + 199;
        int y = guiTop + 7;
        GlStateManager.translate(x, y, 1.0f);
        GlStateManager.scale(0.5f, 0.5f, 0.5f);
        drawGradientRect(-1, -1, 428, 241, 0xFF404040, 0xFF404040);

        int i = mc.displayHeight;
        double d0 = sw.getScaledWidth() < mc.displayWidth ? (int) Math.round((double) mc.displayWidth / (double) sw.getScaledWidth()) : 1;
        double d1 = (double) x * d0;
        double d2 = (double) i - (y + 120.0d) * d0;
        double d3 = 213.5d * d0;
        double d4 = 120.0d * d0;
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
        // background
        drawGradientRect(0, 0, 427, 240, 0xFFC6C6C6, 0xFFC6C6C6);
        if (screenID > -1 && screenID < 3) {
            mc.getTextureManager().bindTexture(resources[screenID]);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.scale(1.66796875f, 0.9375f, 1.0f);
            drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }
        // dialog gui:
        // background
        int vType = guiSettings.getShowVerticalLines();
        int hType = guiSettings.getShowVerticalLines();
        int blurringLine = (int) (guiSettings.dialogWidth * guiSettings.getBlurringLine());
        if (guiSettings.backTexture != null) {
            mc.getTextureManager().bindTexture(guiSettings.backTexture);
            GlStateManager.pushMatrix();
            GlStateManager.scale(1.66796875f, 0.9375f, 1.0f);
            drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }
        if (guiSettings.windowTexture != null) {
            mc.getTextureManager().bindTexture(guiSettings.windowTexture);
            GlStateManager.pushMatrix();
            GlStateManager.translate(guiSettings.guiLeft, 0.0f, 0.0f);
            GlStateManager.scale(guiSettings.dialogWidth / 427.0f * 1.66796875f, 0.9375f, 1.0f);
            drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }
        switch (guiSettings.getType()) {
            case 0: {
                // dialogs
                int left = guiSettings.guiLeft + blurringLine;
                if (blurringLine > 0) { fill(guiSettings.guiLeft, 0, left, 240, zLevel, guiSettings.backBorderColor, guiSettings.backWindowColor); }
                fill(left, 0, 427, 240, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
                // border
                fill(0, 0, guiSettings.guiLeft, 240, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
                // lines
                if (vType != 0) {
                    int lX = guiSettings.guiLeft - 1;
                    if (hType == 2) {
                        drawVerticalLine(lX, 0, dialogHeight - 2, guiSettings.linesColor);
                        drawVerticalLine(lX, dialogHeight - 2, 238, guiSettings.linesColor);
                        lX = guiSettings.guiLeft - 3;
                    }
                    else if (hType == 1) {
                        drawVerticalLine(lX, 0, 238, guiSettings.linesColor);
                        lX = guiSettings.guiLeft - 3;
                    }
                    if (vType == 2) { drawVerticalLine(lX, 1, 238, guiSettings.linesColor); }
                }
                if (hType == 2) {
                    drawHorizontalLine(guiSettings.guiLeft, 424, dialogHeight - 3, guiSettings.linesColor);
                    drawHorizontalLine(guiSettings.guiLeft, 424, dialogHeight - 1, guiSettings.linesColor);
                }
                else if (hType == 1) { drawHorizontalLine(guiSettings.guiLeft, 424, dialogHeight - 2, guiSettings.linesColor); }
                break;
            } // right
            case 1: {
                // dialogs
                int left = guiSettings.guiLeft + blurringLine;
                int right = guiSettings.guiRight - blurringLine;
                if (blurringLine > 0) { fill(guiSettings.guiLeft, 0, left, 240, zLevel, guiSettings.backBorderColor, guiSettings.backWindowColor); }
                fill(left, 0, right, 240, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
                if (blurringLine > 0) { fill(right, 0, guiSettings.guiRight, 240, zLevel, guiSettings.backWindowColor, guiSettings.backBorderColor); }
                // border
                fill(0, 0, guiSettings.guiLeft, 240, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
                fill(guiSettings.guiRight, 0, 427, 240, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
                // lines
                if (vType != 0) {
                    int lX = guiSettings.guiLeft - 1;
                    int rX = guiSettings.guiRight + 1;
                    if (hType == 2) {
                        // left
                        drawVerticalLine(lX, 0, dialogHeight - 2, guiSettings.linesColor);
                        drawVerticalLine(lX, dialogHeight - 2, 238, guiSettings.linesColor);
                        // right
                        drawVerticalLine(rX, 0, dialogHeight - 2, guiSettings.linesColor);
                        drawVerticalLine(rX, dialogHeight - 2, 238, guiSettings.linesColor);
                        lX = guiSettings.guiLeft - 3;
                        rX += 2;
                    }
                    else if (hType == 1) {
                        // left
                        drawVerticalLine(lX, 0, 238, guiSettings.linesColor);
                        // right
                        drawVerticalLine(rX, 0, 238, guiSettings.linesColor);
                        lX = guiSettings.guiLeft - 3;
                        rX += 2;
                    }
                    if (vType == 2) {
                        // left
                        drawVerticalLine(lX, 0, 238, guiSettings.linesColor);
                        // right
                        drawVerticalLine(rX, 0, 238, guiSettings.linesColor);
                    }
                }
                if (hType == 2) {
                    drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 3, guiSettings.linesColor);
                    drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 1, guiSettings.linesColor);
                }
                else if (hType == 1) { drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 2, guiSettings.linesColor); }
                break;
            } // center
            case 2: {
                // dialogs
                int right = guiSettings.guiRight - blurringLine;
                fill(0, 0, right, 240, zLevel, guiSettings.backWindowColor, guiSettings.backWindowColor);
                if (blurringLine > 0) { fill(right, 0, guiSettings.dialogWidth, 240, zLevel, guiSettings.backWindowColor, guiSettings.backBorderColor); }
                // border
                fill(guiSettings.dialogWidth, 0, 427, 240, zLevel, guiSettings.backBorderColor, guiSettings.backBorderColor);
                // lines
                if (vType != 0) {
                    int rX = guiSettings.guiRight + 1;
                    if (hType == 2) {
                        drawVerticalLine(rX, 0, dialogHeight - 2, guiSettings.linesColor);
                        drawVerticalLine(rX, dialogHeight - 2, 238, guiSettings.linesColor);
                        rX += 2;
                    }
                    else if (hType == 1) {
                        drawVerticalLine(rX, 0, 238, guiSettings.linesColor);
                        rX += 2;
                    }
                    if (vType == 2) {
                        drawVerticalLine(rX, 0, 238, guiSettings.linesColor);
                    }
                }
                if (hType == 2) {
                    drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 3, guiSettings.linesColor);
                    drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 1, guiSettings.linesColor);
                }
                else if (hType == 1) { drawHorizontalLine(guiSettings.guiLeft, guiSettings.guiRight, dialogHeight - 2, guiSettings.linesColor); }
                break;
            } // left
        }
        // Dialog text lines
        drawRect(guiSettings.guiLeft + 44, dialogHeight + 2, guiSettings.guiRight - 12, dialogHeight + 23, guiSettings.selectOptionLeftColor);
        drawHorizontalLine(guiSettings.guiLeft + 4, guiSettings.guiRight - 13, dialogHeight + 1, guiSettings.hoverLineColor);
        drawHorizontalLine(guiSettings.guiLeft + 4, guiSettings.guiRight - 13, dialogHeight + 23, guiSettings.hoverLineColor);
        drawHorizontalLine(guiSettings.guiLeft + 4, guiSettings.guiRight - 13, dialogHeight + 46, guiSettings.scrollLineColor);
        drawHorizontalLine(guiSettings.guiLeft + 4, guiSettings.guiRight - 13, dialogHeight + 70, guiSettings.scrollLineColor);
        // scroll bar
        fill(guiSettings.guiRight - 11, 1, guiSettings.guiRight - 1, 50, zLevel, guiSettings.sliderColor, guiSettings.sliderColor);
        fill(guiSettings.guiRight - 11, dialogHeight + 1, guiSettings.guiRight - 1, dialogHeight + 35, zLevel, guiSettings.sliderColor, guiSettings.sliderColor);
        // text
        x = 1 + guiSettings.guiLeft / 2;
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 1.0f);
        drawString(mc.fontRenderer, getTempLine("Noppes: Dialog text"), x, 0, 0xE0E0E0);
        GlStateManager.popMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 1.0f);
        y = 2 + dialogHeight / 2;
        drawString(mc.fontRenderer, getTempLine("  -> 1 Option"), x, y, 0xE0E0E0);
        drawString(mc.fontRenderer, getTempLine("    * 2 Option"), x, y += 12, 0xE0E0E0);
        drawString(mc.fontRenderer, getTempLine("    * 2 Option"), x, y + 12, 0xE0E0E0);
        GlStateManager.popMatrix();
        GlStateManager.popMatrix(); // <- line 120
        // NPC
        if (!hasSubGui() && guiSettings.showNPC && dialogNpc != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(0.0f, 0.0f, 200.0f);
            drawNpc(dialogNpc,
                    199 + ((guiSettings.guiLeft + guiSettings.npcPosX) / 2), 7 + ((guiSettings.npcPosY) / 2),
                    guiSettings.getNpcScale() / 2.0f, guiSettings.getType() == 2 || (guiSettings.getType() == 1 && !guiSettings.npcInLeft) ? 40 : -40,
                    -15, 2);
            GlStateManager.popMatrix();
        }
        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initGui() {
        super.initGui();
        sw = new ScaledResolution(mc);
        guiSettings.init(427.0d, 240.0d);

        width = (int) Math.ceil(sw.getScaledWidth_double());
        height = (int) Math.ceil(sw.getScaledHeight_double());
        dialogHeight = 240 - guiSettings.optionHeight;

        int x0 = guiLeft + 5;
        int x1 = guiLeft + 50;
        int x2 = guiLeft + 200;
        int y = guiTop + 4;
        int lId = 0;
        // type
        addLabel(new GuiNpcLabel(lId++, "gui.type", x0, y + 2));
        addButton(new GuiButtonBiDirectional(1, x1, y, 60, 14, new String[] { "gui.left", "gui.center", "gui.right"}, guiSettings.getType())
                .setHoverText("dialog.gui.settings.hover.place"));
        // npc in left
        addButton(new GuiNpcCheckBox(2, x1 + 63, y - 1, 83, 14, "gui.left", "gui.right", guiSettings.npcInLeft)
                .setHoverText("dialog.gui.settings.hover.in.left")
                .setIsVisible(guiSettings.getType() == 1));
        // width
        addLabel(new GuiNpcLabel(lId++, "scale.width", x0, (y += 16) + 2));
        addSlider(new GuiNpcSlider(this, 0, x1, y + 2, 108, 8, 2.0f * (float) guiSettings.getWidth() - 0.8f)
                .setHoverText("dialog.gui.settings.hover.width"));
        addTextField(new GuiNpcTextField(0, this, x1 + 110, y, 30, 13, "" + (int) (guiSettings.getWidth() * 100.0d))
                .setMinMaxDefault(40, 90, (int) (guiSettings.getWidth() * 100.0d))
                .setHoverText("dialog.gui.settings.hover.width"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // option height
        addLabel(new GuiNpcLabel(lId++, "schematic.height", x0, (y += 15) + 2));
        addSlider(new GuiNpcSlider(this, 1, x1, y + 2, 108, 8, 1.666667f * (float) guiSettings.getOptionHeight() - 0.25f)
                .setHoverText("dialog.gui.settings.hover.height"));
        addTextField(new GuiNpcTextField(1, this, x1 + 110, y, 30, 13, "" + (int) (guiSettings.getOptionHeight() * 100.0d))
                .setMinMaxDefault(15, 75, (int) (guiSettings.getOptionHeight() * 100.0d))
                .setHoverText("dialog.gui.settings.hover.height"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // npc
            // show
        addLabel(new GuiNpcLabel(lId++, "NPC: ", x0, (y += 18) + 4));
        addButton(new GuiNpcCheckBox(3, x1, y, 83, 14, "availability.visible", "availability.invisible", guiSettings.showNPC)
                .setHoverText("dialog.gui.settings.hover.visible"));
            // pos x
        addLabel(new GuiNpcLabel(lId++, "X: ", x1 - 12, (y += 15) + 2));
        float[] pos = guiSettings.getNpcPos();
        addSlider(new GuiNpcSlider(this, 2, x1, y + 2, 108, 8, pos[0] + 0.5f)
                .setHoverText("dialog.gui.settings.hover.pos", "X"));
        addTextField(new GuiNpcTextField(2, this, x1 + 110, y, 30, 13, "" + (int) (100.0f * pos[0]))
                .setMinMaxDefault(-50, 50, (int) (100.0f * pos[0]))
                .setHoverText("dialog.gui.settings.hover.pos", "X"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
            // pos y
        addLabel(new GuiNpcLabel(lId++, "Y: ", x1 - 12, (y += 15) + 2));
        addSlider(new GuiNpcSlider(this, 3, x1, y + 2, 108, 8, 2.0f * pos[1] + 0.5f)
                .setHoverText("dialog.gui.settings.hover.pos", "Y"));
        addTextField(new GuiNpcTextField(3, this, x1 + 110, y, 30, 13, "" + Math.round(100.0f * pos[1]))
                .setMinMaxDefault(-25, 25, Math.round(100.0f * pos[1]))
                .setHoverText("dialog.gui.settings.hover.pos", "Y"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
            // scale
        addLabel(new GuiNpcLabel(lId++, "model.scale", x0, (y += 15) + 2));
        addSlider(new GuiNpcSlider(this, 4, x1, y + 2, 108, 8, 0.206186f * guiSettings.getNpcScale() - 0.030928f)
                .setHoverText("dialog.gui.settings.hover.scale"));
        addTextField(new GuiNpcTextField(4, this, x1 + 110, y, 30, 13, "" + Math.round(100.0f * guiSettings.getNpcScale()))
                .setMinMaxDefault(15, 500, Math.round(100.0f * guiSettings.getNpcScale()))
                .setHoverText("dialog.gui.settings.hover.scale"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // blurring line
        addLabel(new GuiNpcLabel(lId++, "gui.conversion", x0, (y += 15) + 2));
        addSlider(new GuiNpcSlider(this, 5, x1, y + 2, 108, 8, 4.0f * guiSettings.getBlurringLine())
                .setHoverText("dialog.gui.settings.hover.blurring.line"));
        addTextField(new GuiNpcTextField(5, this, x1 + 110, y, 30, 13, "" + Math.round(100.0f * guiSettings.getBlurringLine()))
                .setMinMaxDefault(0, 25, Math.round(100.0f * guiSettings.getBlurringLine()))
                .setHoverText("dialog.gui.settings.hover.blurring.line"));
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // background color
        addLabel(new GuiNpcLabel( lId++, "gui.background", x0, (y += 16) + 1));
        addTextField(new GuiNpcTextField(6, this, x1, y, 146, 13, guiSettings.getBackgroundTexture() == null ? "" : guiSettings.getBackgroundTexture().toString())
                .setHoverText("dialog.gui.settings.hover.blurring.line"));
        addButton(new GuiNpcButton(6, x2, y - 1, 50, 15, "mco.template.button.select"));
        addButton(new GuiColorButton(5, x2 + 52, y - 1, 30, 15, guiSettings.backBorderColor)
                .setHoverText("dialog.gui.settings.hover.back.color"));
        addButton(new GuiButtonBiDirectional(100, guiLeft + xSize - 67, y, 60, 14, new String[] { "type.empty", "over", "nether", "end"}, screenID + 1)
                .setHoverText("dialog.gui.settings.hover.place"));
        // window color
        addLabel(new GuiNpcLabel(lId++, "gui.texture", x0, (y += 15) + 1));
        addTextField(new GuiNpcTextField(7, this, x1, y, 146, 13, guiSettings.getWindowTexture() == null ? "" : guiSettings.getWindowTexture().toString())
                .setHoverText("dialog.gui.settings.hover.window"));
        addButton(new GuiNpcButton(7, x2, y - 1, 50, 15, "mco.template.button.select"));
        addButton(new GuiColorButton(4, x2 + 52, y - 1, 30, 15, guiSettings.backWindowColor)
                .setHoverText("dialog.gui.settings.hover.win.color"));
        // colors
        x1 += 30;
        // lines
        addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("gui.color").getFormattedText() + ":", x0, (y += 15) + 2));
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.line.v", x0, (y += 15) + 1));
        addButton(new GuiButtonBiDirectional(8, x1, y, 35, 14, new String[] { "0", "1", "2"}, guiSettings.getShowVerticalLines())
                .setHoverText("dialog.gui.settings.hover.line.v.type"));
        addButton(new GuiColorButton(9, x1 + 37, y + 7, 30, 15, guiSettings.linesColor)
                .setHoverText("dialog.gui.settings.hover.line.color"));
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.line.h", x0, (y += 15) + 1));
        addButton(new GuiButtonBiDirectional(10, x1, y, 35, 14, new String[] { "0", "1", "2"}, guiSettings.getShowHorizontalLines())
                .setHoverText("dialog.gui.settings.hover.line.h.type"));
        // options
        x0 = x1 + 69;
        x1 = x0 + 75;
        y -= 30;
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.line.option.slot", x0, y + 1));
        addButton(new GuiColorButton(11, x1, y, 30, 15, guiSettings.scrollLineColor)
                .setHoverText("dialog.gui.settings.hover.line.option.slot"));
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.line.option.select", x0, (y += 15) + 1));
        addButton(new GuiColorButton(12, x1, y, 30, 15, guiSettings.hoverLineColor)
                .setHoverText("dialog.gui.settings.hover.line.option.select"));
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.line.option.back", x0, (y += 15) + 1));
        addButton(new GuiColorButton(13, x1, y, 30, 15, guiSettings.selectOptionLeftColor)
                .setHoverText("dialog.gui.settings.hover.line.option.back"));
        // select
        x0 = x1 + 32;
        x1 = x0 + 75;
        y -= 30;
        addLabel(new GuiNpcLabel(lId++, "dialog.gui.settings.pointer.color", x0, y + 1));
        addButton(new GuiColorButton(14, x1, y, 30, 15, guiSettings.pointerColor)
                .setHoverText("dialog.gui.settings.hover.pointer.color"));
        addLabel(new GuiNpcLabel(lId, "dialog.gui.settings.slider.color", x0, (y += 15) + 1));
        addButton(new GuiColorButton(15, x1, y, 30, 15, guiSettings.sliderColor)
                .setHoverText("dialog.gui.settings.hover.slider.color"));
    }

    @Override
    public void mouseDragged(GuiNpcSlider slider) {
        switch (slider.getID()) {
            case 0: {
                guiSettings.setWidth(Math.ceil((0.5f * slider.sliderValue + 0.4f) * 100.0f) / 100.0f);
                if (getTextField(0) != null) { getTextField(0).setText("" + Math.round(guiSettings.getWidth() * 100.0d)); }
                break;
            } // width
            case 1: {
                guiSettings.setOptionHeight(Math.round((0.6f * slider.sliderValue + 0.15f) * 100.0d) / 100.0d);
                if (getTextField(1) != null) { getTextField(1).setText("" + Math.round(guiSettings.getOptionHeight() * 100.0d)); }
                break;
            } // option height
            case 2: {
                float[] pos = guiSettings.getNpcPos();
                pos[0] = Math.round((slider.sliderValue - 0.5f) * 100.0f) / 100.0f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getTextField(2) != null) { getTextField(2).setText("" + Math.round(100.0f * pos[0])); }
                break;
            } // npc pos x
            case 3: {
                float[] pos = guiSettings.getNpcPos();
                pos[1] = Math.round((0.5f * slider.sliderValue - 0.25f) * 100.0f) / 100.0f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getTextField(3) != null) { getTextField(3).setText("" + Math.round(100.0f * pos[1])); }
                break;
            } // npc pos y
            case 4: {
                guiSettings.setNpcScale(4.85f * slider.sliderValue + 0.15f);
                if (getTextField(4) != null) { getTextField(4).setText("" + Math.round(100.0f * guiSettings.getNpcScale())); }
                break;
            } // npc scale
            case 5: {
                guiSettings.setBlurringLine(slider.sliderValue / 4.0f);
                if (getTextField(5) != null) { getTextField(5).setText("" + Math.round(100.0f * guiSettings.getBlurringLine())); }
                break;
            } // blurring line
        }
        guiSettings.init(427, 240);
        dialogHeight = 240 - guiSettings.optionHeight;
    }

    @Override
    public void unFocused(GuiNpcTextField textField) {
        switch (textField.getID()) {
            case 0: {
                guiSettings.setWidth(textField.getInteger() / 100.0d);
                if (getSlider(0) != null) { getSlider(0).setSliderValue(2.0f * (float) guiSettings.getWidth() - 0.8f); }
                break;
            } // width
            case 1: {
                float[] pos = guiSettings.getNpcPos();
                pos[1] = 0.5f * textField.getInteger() - 0.25f;
                guiSettings.setOptionHeight(textField.getInteger() / 100.0f);
                if (getSlider(1) != null) { getSlider(1).setSliderValue(1.666667f * (float) guiSettings.getOptionHeight() - 0.25f); }
                break;
            } // option height
            case 2: {
                float[] pos = guiSettings.getNpcPos();
                pos[0] = 0.01f * textField.getInteger();
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getSlider(2) != null) { getSlider(2).setSliderValue(pos[0] + 0.5f); }
                break;
            } // npc pos x
            case 3: {
                float[] pos = guiSettings.getNpcPos();
                pos[1] = 0.01f * (float) textField.getInteger();
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getSlider(3) != null) { getSlider(3).setSliderValue(2.0f * pos[1] + 0.5f); }
                break;
            } // npc pos y
            case 4: {
                guiSettings.setNpcScale((float) textField.getInteger() / 100.0f);
                if (getSlider(4) != null) { getSlider(4).setSliderValue(0.206186f * guiSettings.getNpcScale() - 0.030928f); }
                break;
            } // npc scale
            case 5: {
                guiSettings.setBlurringLine((float) textField.getInteger() / 100.0f);
                if (getSlider(5) != null) { getSlider(5).setSliderValue(4.0f * guiSettings.getBlurringLine()); }
                break;
            } // blurring line
            case 6: {
                if (textField.isEmpty()) { guiSettings.backTexture = null; }
                else { guiSettings.backTexture = new ResourceLocation(textField.getText()); }
                break;
            } // background texture
            case 7: {
                if (textField.isEmpty()) { guiSettings.windowTexture = null; }
                else { guiSettings.windowTexture = new ResourceLocation(textField.getText()); }
                break;
            } // window texture
        }
        guiSettings.init(427, 240);
        dialogHeight = 240 - guiSettings.optionHeight;
    }

    @Override
    public void mousePressed(GuiNpcSlider slider) { }

    @Override
    public void mouseReleased(GuiNpcSlider slider) { }

    @Override
    public void subGuiClosed(GuiScreen subgui) {
        if (subgui instanceof SubGuiTextureSelection) {
            SubGuiTextureSelection tGui = (SubGuiTextureSelection) subgui;
            if (tGui.id == 0) {
                guiSettings.backTexture = tGui.resource;
            } // Background
            else if (tGui.id == 1) {
                guiSettings.windowTexture = tGui.resource;
            } // window
        }
    }

    @Override
    public void save() { Client.sendData(EnumPacketServer.DialogGuiSettings, guiSettings.save()); }

    private String getTempLine(String line) {
        if (mc.fontRenderer.getStringWidth(line) > guiSettings.dialogWidth / 2 - 7) {
            while (mc.fontRenderer.getStringWidth(line + "...") > guiSettings.dialogWidth / 2 - 7) { line = line.substring(0, line.length() - 1); }
            line += "...";
        }
        return line;
    }

}
