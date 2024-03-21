package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;

public interface INPCAnimation {

	void reset();

	void stopAnimation();
	
	void stopEmotion();

	void clear();

	void update();

	IAnimation[] getAnimations(int animationType);

	IAnimation getAnimation(int animationType, int variant);

	void startAnimation(int animationType);

	void startAnimation(int animationType, int variant);
	
	INbt getNbt();
	
	void setNbt(INbt nbt);

	boolean removeAnimation(int type, int animationId);
	
	void removeAnimations(int type);
	
}
