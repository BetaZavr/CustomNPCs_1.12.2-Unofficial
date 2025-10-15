package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

public interface IEmotionPart {

	boolean isDisabled();

	void setDisable(@ParamName("bo") boolean bo);
	
	int getEndDelay();

	int getSpeed();

	boolean isSmooth();

	void setEndDelay(@ParamName("ticks") int ticks);

	void setSmooth(@ParamName("isSmooth") boolean isSmooth);

	void setSpeed(@ParamName("ticks") int ticks);

	boolean isBlink();

	void setBlink(@ParamName("bo") boolean bo);

	boolean isEndBlink();

	void setEndBlink(@ParamName("bo") boolean bo);
	
}
