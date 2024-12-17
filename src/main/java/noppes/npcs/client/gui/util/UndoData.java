package noppes.npcs.client.gui.util;

public class UndoData {

    public String text;
    public int cursorPosition;

    public UndoData(String nowText, int nowCursorPosition) {
        text = nowText;
        cursorPosition = nowCursorPosition;
    }

}
