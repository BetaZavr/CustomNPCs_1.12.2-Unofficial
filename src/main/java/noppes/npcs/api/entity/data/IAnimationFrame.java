package noppes.npcs.api.entity.data;

public interface IAnimationFrame {

	boolean isSmooth();

	void setSmooth(boolean isSmooth);

	int getSpeed();

	void setSpeed(int ticks);

	void setEndDelay(int ticks);

	int getEndDelay();
	
	IAnimationPart getPart(int id);
	
}
