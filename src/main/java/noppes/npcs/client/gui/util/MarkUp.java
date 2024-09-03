package noppes.npcs.client.gui.util;

public class MarkUp {
    public int start;
    public int end;
    public int level;
    public char c;

    public MarkUp(int start, int end, char c, int level) {
        this.start = start;
        this.end = end;
        this.c = c;
        this.level = level;
    }
}
