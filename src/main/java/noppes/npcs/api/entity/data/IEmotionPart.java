package noppes.npcs.api.entity.data;

public interface IEmotionPart {

	boolean isDisabled();

	void setDisable(boolean bo);
	
	int getEndDelay();

	int getSpeed();

	boolean isSmooth();

	void setEndDelay(int ticks);

	void setSmooth(boolean isSmooth);

	void setSpeed(int ticks);

	boolean isBlink();

	void setBlink(boolean bo);

	boolean isEndBlink();

	void setEndBlink(boolean bo);
	
}
