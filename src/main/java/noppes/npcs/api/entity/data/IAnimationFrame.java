package noppes.npcs.api.entity.data;

import noppes.npcs.api.item.IItemStack;

public interface IAnimationFrame {

	int getEndDelay();

	IAnimationPart getPart(int id);

	int getSpeed();

	boolean isSmooth();

	void setEndDelay(int ticks);

	void setSmooth(boolean isSmooth);

	void setSpeed(int ticks);

	String getStartSound();

	void setStartSound(String sound);

	int getStartEmotion();

	void setStartEmotion(int id);

	boolean isNowDamage();

	int getHoldRightStackType();

	int getHoldLeftStackType();

	IItemStack getHoldRightStack();

	IItemStack getHoldLeftStack();

	void setHoldRightStackType(int type);

	void setHoldLeftStackType(int type);

	void setHoldRightStack(IItemStack stack);

	void setHoldLeftStack(IItemStack stack);

}
