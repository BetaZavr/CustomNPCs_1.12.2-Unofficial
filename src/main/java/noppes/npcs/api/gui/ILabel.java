package noppes.npcs.api.gui;

public interface ILabel extends ICustomGuiComponent {

	int getColor();

	int getHeight();

	float getScale();

	String getText();

	int getWidth();

	boolean isShadow();

	ILabel setColor(int color);

	ILabel setScale(float scale);

	void setShadow(boolean showShadow);

	ILabel setSize(int width, int height);

	ILabel setText(String label);

}
