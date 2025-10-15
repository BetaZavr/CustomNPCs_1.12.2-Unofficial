package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

public interface ILine {

	boolean getShowText();

	String getSound();

	String getText();

	void setShowText(@ParamName("show") boolean show);

	void setSound(@ParamName("sound") String sound);

	void setText(@ParamName("text") String text);

}
