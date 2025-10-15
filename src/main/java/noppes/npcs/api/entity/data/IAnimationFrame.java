package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IAnimationFrame {

	int getEndDelay();

	IAnimationPart getPart(@ParamName("id") int id);

	int getSpeed();

	boolean isSmooth();

	void setEndDelay(@ParamName("ticks") int ticks);

	void setSmooth(@ParamName("isSmooth") boolean isSmooth);

	void setSpeed(@ParamName("ticks") int ticks);

	String getStartSound();

	void setStartSound(@ParamName("sound") String sound);

	int getStartEmotion();

	void setStartEmotion(@ParamName("id") int id);

	boolean isNowDamage();

	int getHoldRightStackType();

	int getHoldLeftStackType();

	IItemStack getHoldRightStack();

	IItemStack getHoldLeftStack();

	void setHoldRightStackType(@ParamName("type") int type);

	void setHoldLeftStackType(@ParamName("type") int type);

	void setHoldRightStack(@ParamName("stack") IItemStack stack);

	void setHoldLeftStack(@ParamName("stack") IItemStack stack);

}
