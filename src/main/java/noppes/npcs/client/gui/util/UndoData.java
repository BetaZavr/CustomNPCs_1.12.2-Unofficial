package noppes.npcs.client.gui.util;

public class UndoData {
    public String text;
    public int cursorPosition;

    public UndoData(String text, int cursorPosition) {
        this.text = text;
        this.cursorPosition = cursorPosition;
    }
}
