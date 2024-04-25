package noppes.npcs.api.entity.data;

public interface IAnimationFrame {

	int getEndDelay();

	IAnimationPart getPart(int id);

	int getSpeed();

	boolean isSmooth();

	void setEndDelay(int ticks);

	void setSmooth(boolean isSmooth);

	void setSpeed(int ticks);

}
