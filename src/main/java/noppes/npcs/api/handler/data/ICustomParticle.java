package noppes.npcs.api.handler.data;

import noppes.npcs.api.IWorld;

public interface ICustomParticle {

	boolean canCollide();

	int getAge();

	int getColorMask();

	float getHeight();

	String getObj();

	double[] getPrevPoses();

	float getRotationX();

	float getRotationY();

	float getRotationZ();

	float getScale();

	String getTexture();

	int getTotalAge();

	float getWidth();

	IWorld getWorld();

	boolean isAlive();

	void move(double x, double y, double z);

	boolean onGround();

	double posX();

	double posY();

	double posZ();

	void setAge(int ticks);

	void setCanCollide(boolean collide);

	void setColorMask(int color);

	void setCustomSize(float width, float height);

	void setObj(String objPath);

	void setPos(double x, double y, double z);

	void setRotation(float angleX, float angleY, float angleZ);

	void setScale(float scale);

	void setTexture(String texture);

	void setTotalAge(int ticks);

}
