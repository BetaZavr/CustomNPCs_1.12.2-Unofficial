package noppes.npcs.client.gui.util;

import java.util.List;

public interface IGuiNpcLabel extends IComponentGui {

    List<String> getLabels();

    void setLabel(Object label);

    int getColor();

    void setColor(int color);

    int getBackColor();

    void setBackColor(int color);

    int getBorderColor();

    void setBorderColor(int color);

    void setCenter(int width);

}
