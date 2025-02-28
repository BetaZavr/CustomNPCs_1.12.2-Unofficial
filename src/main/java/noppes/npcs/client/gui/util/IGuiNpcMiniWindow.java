package noppes.npcs.client.gui.util;

public interface IGuiNpcMiniWindow extends IComponentGui {

    void buttonEvent(IGuiNpcButton button);

    void resetButtons();

    void mouseEvent(int mouseX, int mouseY, int mouseButton);

}
