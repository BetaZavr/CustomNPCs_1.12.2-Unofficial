package noppes.npcs.client.gui.util;

public class UndoData {

    public final String text;
    public final int cursorPosition;
    public final int startSelection;
    public final int endSelection;
    public final int scrolledLine;

    public UndoData(String textIn, int cursorPositionIn, int startSelectionIn, int endSelectionIn, int scrolledLineIn) {
        text = textIn;
        cursorPosition = cursorPositionIn;
        startSelection = startSelectionIn;
        endSelection = endSelectionIn;
        scrolledLine = scrolledLineIn;
    }

}
