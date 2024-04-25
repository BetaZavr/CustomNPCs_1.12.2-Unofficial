package noppes.npcs.api.entity.data;

public interface ILine {

	boolean getShowText();

	String getSound();

	String getText();

	void setShowText(boolean show);

	void setSound(String sound);

	void setText(String text);

}
