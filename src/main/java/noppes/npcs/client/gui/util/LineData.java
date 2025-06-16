package noppes.npcs.client.gui.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class LineData {

    public final String text;
    public int start;
    public int end;
    private final Map<List<MarkUp>, String> data = new HashMap<>();

    public LineData(String textIn, int startIn, int endIn) {
        text = textIn;
        start = startIn;
        end = endIn;
    }

    public String getFormattedString(List<MarkUp> makeup) {
        if (data.containsKey(makeup)) { return data.get(makeup); }
        StringBuilder builder = new StringBuilder(text);
        int found = 0;
        for (MarkUp entry : makeup) {
            if (entry.start >= start && entry.start < end) {
                builder.insert(entry.start - start + found * 2, '\uffff' + Character.toString(entry.c));
                ++found;
            }
            if (entry.start < start && entry.end > start) {
                builder.insert(0, '\uffff' + Character.toString(entry.c));
                ++found;
            }
            if (entry.end >= start && entry.end < end) {
                builder.insert(entry.end - start + found * 2, '\uffff' + Character.toString('r'));
                ++found;
            }
        }
        data.put(makeup, builder.toString());
        return data.get(makeup);
    }

}
