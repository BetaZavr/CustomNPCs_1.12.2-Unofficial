package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;

public interface INPCAnimation {

	void clear();

	IAnimation getAnimation(int animationType, int variant);

	IAnimation[] getAnimations(int animationType);

	INbt getNbt();

	boolean removeAnimation(int type, int animationId);

	void removeAnimations(int type);

	void reset();

	void setNbt(INbt nbt);

	void startAnimation(int animationType);

	void startAnimation(int animationType, int variant);

	void stopAnimation();

	void stopEmotion();

	void update();

}
