package noppes.npcs.client.gui.util;

public interface IGuiNpcTextField extends IComponentGui {

    void setFocus(boolean bo);

    String getFullText();

    void setFullText(String text);

    void unFocus();

    int getInteger();

    long getLong();

    double getDouble();

    boolean isDouble();

    boolean isEmpty();

    long getDefault();

    double getDoubleDefault();

    boolean isInteger();

    boolean isLong();

    void setMinMaxDefault(long minValue, long maxValue, long defaultValue);

    boolean isLatinAlphabetOnly();

    void setLatinAlphabetOnly(boolean latinAlphabetOnly);

    void setMinMaxDoubleDefault(double minValue, double maxValue, double defaultValue);

    boolean isAllowUppercase();

    void setAllowUppercase(boolean allowUppercase);

    long getMax();

    long getMin();

    double getDoubleMax();

    double getDoubleMin();

}
