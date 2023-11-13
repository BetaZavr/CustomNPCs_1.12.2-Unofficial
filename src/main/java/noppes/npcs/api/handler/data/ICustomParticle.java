package noppes.npcs.api.handler.data;

import noppes.npcs.api.IWorld;

public interface ICustomParticle {
	
	double posX();
	
	double posY();
	
	double posZ();
	
	void setPos(double x, double y, double z);
	
	double[] getPrevPoses();
	
	float getWidth();
	
	float getHeight();
	
	boolean isAlive();
	
	boolean onGround();
	
	boolean canCollide();
	
	void setCanCollide(boolean collide);
	
	void move(double x, double y, double z);
	
	void setCustomSize(float width, float height);
	
	void setTexture(String texture);

	String getTexture();

	void setObj(String objPath);

	String getObj();
	
	int getAge();
	
	void setAge(int ticks);
	
	int getTotalAge();
	
	void setTotalAge(int ticks);
	
	float getRotationX();

	float getRotationY();
	
	float getRotationZ();
	
	void setRotation(float angleX, float angleY, float angleZ);
	
	float getScale();
	
	void setScale(float scale);
	
	int getColorMask();
	
	void setColorMask(int color);
	
	IWorld getWorld();
	
}
