package noppes.npcs.reflection.client.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.LogWriter;

import java.lang.reflect.Method;

public class GuiScreenReflection {

    private static Method mouseClicked;
    private static Method keyTyped;

    public static void mouseClicked(GuiScreen gui, int mouseX, int mouseY, int mouseButton) {
        if (gui == null) { return; }
        if (mouseClicked == null) {
            Exception error = null;
            try { mouseClicked = GuiScreen.class.getDeclaredMethod("func_73864_a", int.class, int.class, int.class); } catch (Exception e) { error = e; }
            if (mouseClicked == null) {
                try {
                    mouseClicked = GuiScreen.class.getDeclaredMethod("mouseClicked", int.class, int.class, int.class);
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found method \"mouseClicked\"", error);
                return;
            }
        }
        try {
            mouseClicked.setAccessible(true);
            mouseClicked.invoke(gui, mouseX, mouseY, mouseButton);
        } catch (Exception e) {
            LogWriter.error("Error invoke \"mouseClicked\" in " + gui, e);
        }
    }

    public static void keyTyped(GuiScreen gui, char c, int i) {
        if (gui == null) { return; }
        if (keyTyped == null) {
            Exception error = null;
            try { keyTyped = GuiScreen.class.getDeclaredMethod("func_73869_a", char.class, int.class); } catch (Exception e) { error = e; }
            if (keyTyped == null) {
                try {
                    keyTyped = GuiScreen.class.getDeclaredMethod("keyTyped", char.class, int.class);
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found method \"keyTyped\"", error);
                return;
            }
        }
        try {
            keyTyped.setAccessible(true);
            keyTyped.invoke(gui, c, i);
        } catch (Exception e) {
            LogWriter.error("Error invoke \"keyTyped\" in " + gui, e);
        }
    }

}
