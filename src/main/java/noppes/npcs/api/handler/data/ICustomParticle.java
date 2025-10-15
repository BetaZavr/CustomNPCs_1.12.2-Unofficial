package noppes.npcs.api.handler.data;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
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

	void move(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	boolean onGround();

	double posX();

	double posY();

	double posZ();

	void setAge(@ParamName("ticks") int ticks);

	void setCanCollide(@ParamName("collide") boolean collide);

	void setColorMask(@ParamName("color") int color);

	void setCustomSize(@ParamName("width") float width, @ParamName("height") float height);

	void setObj(@ParamName("objPath") String objPath);

	void setPos(@ParamName("y") double x, @ParamName("y") double y, @ParamName("y") double z);

	void setRotation(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setScale(@ParamName("scale") float scale);

	void setTexture(@ParamName("texture") String texture);

	void setTotalAge(@ParamName("ticks") int ticks);

}
