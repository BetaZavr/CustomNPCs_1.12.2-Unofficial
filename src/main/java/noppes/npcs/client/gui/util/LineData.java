package noppes.npcs.client.gui.util;

import java.util.List;

public class LineData {
    public String text;
    public int start;
    public int end;

    public LineData(String text, int start, int end) {
        this.text = text;
        this.start = start;
        this.end = end;
    }

    public String getFormattedString(List<MarkUp> makeup) {
        StringBuilder builder = new StringBuilder(this.text);
        int found = 0;
        for (MarkUp entry : makeup) {
            if (entry.start >= this.start && entry.start < this.end) {
                builder.insert(entry.start - this.start + found * 2,
                        '\uffff' + Character.toString(entry.c));
                ++found;
            }
            if (entry.start < this.start && entry.end > this.start) {
                builder.insert(0, '\uffff' + Character.toString(entry.c));
                ++found;
            }
            if (entry.end >= this.start && entry.end < this.end) {
                builder.insert(entry.end - this.start + found * 2,
                        '\uffff' + Character.toString('r'));
                ++found;
            }
        }
        return builder.toString();
    }
}
