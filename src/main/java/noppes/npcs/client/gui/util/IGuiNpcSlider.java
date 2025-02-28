package noppes.npcs.client.gui.util;

public interface IGuiNpcSlider extends IComponentGui {

    String getDisplayString();

    void setString(String str);

    void setDisplayString(String newDisplayString);

    float getSliderValue();

    void setSliderValue(float value);

}
