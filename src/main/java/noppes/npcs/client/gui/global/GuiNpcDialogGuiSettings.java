package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.data.DialogGuiSettings;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;
import org.lwjgl.opengl.GL11;

public class GuiNpcDialogGuiSettings
        extends GuiNPCInterface2
        implements ISubGuiListener, ISliderListener, ITextfieldListener {

    private final ResourceLocation[] resources = {
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/overworld.png"),
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/netherworld.png"),
            new ResourceLocation(CustomNpcs.MODID, "textures/gui/screens/endworld.png")
    };
    private final DialogGuiSettings guiSettings;
    protected int dialogHeight;
    protected final EntityNPCInterface dialogNpc;

    ScaledResolution sw = new ScaledResolution(mc);

    private int screenID = 0;

    public GuiNpcDialogGuiSettings(EntityNPCInterface npc) {
        super(npc);
        closeOnEsc = true;

        guiSettings = DialogController.instance.getGuiSettings();

        dialogNpc = Util.instance.copyToGUI(npc != null ? npc : (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), mc.world), mc.world, false);
        dialogNpc.display.setVisible(0);
    }

    @Override
    public void buttonEvent(IGuiNpcButton button) {
        switch (button.getID()) {
            case 1: guiSettings.setType(button.getValue()); initGui(); break;
            case 2: guiSettings.npcInLeft = ((GuiNpcCheckBox) button).isSelected(); initGui(); break;
            case 3: guiSettings.showNPC = ((GuiNpcCheckBox) button).isSelected(); initGui(); break;


            case 100: screenID = button.getValue() - 1; break;
            case 66: close(); break;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
        GlStateManager.pushMatrix();
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

        // window background
        drawGradientRect(0, 0, 427, 240, 0xFFC6C6C6, 0xFFC6C6C6);
        if (screenID > -1 && screenID < 3) {
            mc.getTextureManager().bindTexture(resources[screenID]);
            GlStateManager.pushMatrix();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.scale(1.66796875f, 0.9375f, 1.0f);
            drawTexturedModalRect(0, 0, 0, 0, 256, 256);
            GlStateManager.popMatrix();
        }

        // Back
        int vType = guiSettings.getShowVerticalLines();
        int hType = guiSettings.getShowVerticalLines();
        int blurringLine = (int) (guiSettings.dialogWidth * guiSettings.getBlurringLine());
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
        // text
        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 1.0f);
        drawString(mc.fontRenderer, "Noppes: Dialog text", 1 + guiSettings.guiLeft / 2, 0, 0xE0E0E0);
        GlStateManager.popMatrix();

        GlStateManager.pushMatrix();
        GlStateManager.scale(2.0f, 2.0f, 1.0f);
        drawString(mc.fontRenderer, "  -> Player option", 1 + guiSettings.guiLeft / 2, dialogHeight / 2, 0xE0E0E0);
        GlStateManager.popMatrix();

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.popMatrix();

        // NPC
        if (guiSettings.showNPC && dialogNpc != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(199.0f, 7.0f, 1.0f);
            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor((int)d1, (int)d2, Math.max(0, (int)d3), Math.max(0, (int)d4));
            drawNpc(dialogNpc, (guiSettings.guiLeft + guiSettings.npcPosX) / 2, guiSettings.npcPosY / 2, guiSettings.getNpcScale() / 2.0f,
                    guiSettings.getType() == 2 || (guiSettings.getType() == 1 && !guiSettings.npcInLeft) ? 40 : -40,
                    15, 2);
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
            GlStateManager.popMatrix();
        }
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

        GuiNpcButton button;
        GuiNpcSlider slider;
        GuiNpcTextField textField;
        int x0 = guiLeft + 5;
        int x1 = guiLeft + 50;
        int x2 = guiLeft + 200;
        int y = guiTop + 4;
        int lId = 0;
        // type
        addLabel(new GuiNpcLabel(lId++, "gui.type", x0, y + 2));
        addButton(button = new GuiButtonBiDirectional(1, x1, y, 60, 14, new String[] { "gui.right", "gui.center", "gui.left"}, guiSettings.getType()));
        button.setHoverText("gui.settings.hover.dialog.place");
        // npc in left
        addButton(button = new GuiNpcCheckBox(2, x1 + 63, y - 1, 83, 14, "gui.left", "gui.right", guiSettings.npcInLeft));
        button.setHoverText("gui.settings.hover.dialog.in.left");
        button.setIsVisible(guiSettings.getType() == 1);
        // width
        addLabel(new GuiNpcLabel(lId++, "scale.width", x0, (y += 16) + 2));
        addSlider(slider = new GuiNpcSlider(this, 0, x1, y + 2, 108, 8, 2.0f * (float) guiSettings.getWidth() - 0.8f));
        slider.setHoverText("gui.settings.hover.dialog.width");
        addTextField(textField = new GuiNpcTextField(0, this, x1 + 110, y, 30, 13, "" + (int) (guiSettings.getWidth() * 100.0d)));
        textField.setMinMaxDefault(40, 90, (int) (guiSettings.getWidth() * 100.0d));
        textField.setHoverText("gui.settings.hover.dialog.width");
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // option height
        addLabel(new GuiNpcLabel(lId++, "schematic.height", x0, (y += 15) + 2));
        addSlider(slider = new GuiNpcSlider(this, 1, x1, y + 2, 108, 8, 1.666667f * (float) guiSettings.getOptionHeight() - 0.25f));
        slider.setHoverText("gui.settings.hover.dialog.height");
        addTextField(textField = new GuiNpcTextField(1, this, x1 + 110, y, 30, 13, "" + (int) (guiSettings.getOptionHeight() * 100.0d)));
        textField.setMinMaxDefault(15, 75, (int) (guiSettings.getOptionHeight() * 100.0d));
        textField.setHoverText("gui.settings.hover.dialog.width");
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
        // npc
            // show
        addLabel(new GuiNpcLabel(lId++, "NPC: ", x0, (y += 15) + 4));
        addButton(button = new GuiNpcCheckBox(3, x1, y, 83, 14, "availability.visible", "availability.visible", guiSettings.showNPC));
        button.setHoverText("gui.settings.hover.dialog.visible");
            // pos x
        addLabel(new GuiNpcLabel(lId++, "X: ", x1 - 12, (y += 15) + 2));
        float[] pos = guiSettings.getNpcPos();
        addSlider(slider = new GuiNpcSlider(this, 2, x1, y + 2, 108, 8, pos[0] + 0.5f));
        slider.setHoverText("gui.settings.hover.dialog.pos", "X");
        addTextField(textField = new GuiNpcTextField(2, this, x1 + 110, y, 30, 13, "" + (int) (100.0f * pos[0] + 50.0f)));
        textField.setMinMaxDefault(0, 100, (int) (100.0f * pos[0] + 50.0f));
        textField.setHoverText("gui.settings.hover.dialog.pos", "X");
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
            // pos y
        addLabel(new GuiNpcLabel(lId++, "Y: ", x1 - 12, (y += 15) + 2));
        addSlider(slider = new GuiNpcSlider(this, 3, x1, y + 2, 108, 8, 2.0f * pos[1] + 0.5f));
        slider.setHoverText("gui.settings.hover.dialog.pos", "Y");
        addTextField(textField = new GuiNpcTextField(3, this, x1 + 110, y, 30, 13, "" + Math.round(200.0f * pos[1] + 50.0f)));
        textField.setMinMaxDefault(0, 100, Math.round(200.0f * pos[1] + 50.0f));
        textField.setHoverText("gui.settings.hover.dialog.pos", "Y");
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));
            // scale
        addLabel(new GuiNpcLabel(lId++, "model.scale", x0, (y += 15) + 2));
        addSlider(slider = new GuiNpcSlider(this, 4, x1, y + 2, 108, 8, 0.2f * guiSettings.getNpcScale()));
        slider.setHoverText("gui.settings.hover.dialog.pos", "Y");
        addTextField(textField = new GuiNpcTextField(4, this, x1 + 110, y, 30, 13, "" + Math.round(20.0f * guiSettings.getNpcScale())));
        textField.setMinMaxDefault(0, 100, Math.round(20.0f * guiSettings.getNpcScale()));
        textField.setHoverText("gui.settings.hover.dialog.scale");
        addLabel(new GuiNpcLabel(lId++, "%", x1 + 142, y + 2));

        y = guiTop + 128;
        addButton(button = new GuiButtonBiDirectional(100, x2, y, 60, 14, new String[] { "type.empty", "over", "nether", "end"}, screenID + 1));
        button.setHoverText("gui.settings.hover.dialog.place");
    }

    @Override
    public void mouseDragged(IGuiNpcSlider slider) {
        switch (slider.getID()) {
            case 0: {
                guiSettings.setWidth(Math.ceil((0.5f * slider.getSliderValue() + 0.4f) * 100.0f) / 100.0f);
                if (getTextField(0) != null) { getTextField(0).setFullText("" + Math.round(guiSettings.getWidth() * 100.0d)); }
                break;
            } // width
            case 1: {
                guiSettings.setOptionHeight(Math.round((0.6f * slider.getSliderValue() + 0.15f) * 100.0d) / 100.0d);
                if (getTextField(1) != null) { getTextField(1).setFullText("" + Math.round(guiSettings.getOptionHeight() * 100.0d)); }
                break;
            } // option height
            case 2: {
                float[] pos = guiSettings.getNpcPos();
                pos[0] = slider.getSliderValue() - 0.5f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getTextField(2) != null) { getTextField(2).setFullText("" + Math.round(100.0f * pos[0] + 50.0f)); }
                break;
            } // npc pos x
            case 3: {
                float[] pos = guiSettings.getNpcPos();
                pos[1] = 0.5f * slider.getSliderValue() - 0.25f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getTextField(3) != null) { getTextField(3).setFullText("" + Math.round(200.0f * pos[1] + 50.0f)); }
                break;
            } // npc pos y
        }
        guiSettings.init(427, 240);
        dialogHeight = 240 - guiSettings.optionHeight;
    }

    @Override
    public void unFocused(IGuiNpcTextField textField) {
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
                pos[0] = 0.01f * textField.getInteger() - 0.5f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getSlider(2) != null) { getSlider(2).setSliderValue(pos[0] + 0.5f); }
                break;
            } // npc pos x
            case 3: {
                float[] pos = guiSettings.getNpcPos();
                pos[1] = 0.005f * (float) textField.getInteger() - 0.25f;
                guiSettings.setNpcPos(pos[0], pos[1]);
                if (getSlider(3) != null) { getSlider(3).setSliderValue(2.0f * pos[1] + 0.5f); }
                break;
            } // option height
        }
        guiSettings.init(427, 240);
        dialogHeight = 240 - guiSettings.optionHeight;
    }

    @Override
    public void mousePressed(IGuiNpcSlider slider) { }

    @Override
    public void mouseReleased(IGuiNpcSlider slider) { }

    @Override
    public void subGuiClosed(ISubGuiInterface subgui) {

    }

    @Override
    public void close() {
        save();
        CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal);
    }

    @Override
    public void save() {
        Client.sendData(EnumPacketServer.DialogGuiSettings, guiSettings.save());
    }

}
