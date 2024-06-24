package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;

public interface INPCAnimation {

	boolean hasAnimations(int animationType);
	
	boolean hasAnimation(int animationType, int animationId);
	
	IAnimation[] getAnimations(int animationType);

	boolean removeAnimation(int type, int animationId);

	void removeAnimations(int type);

	void startAnimation(int animationType);

	void startAnimation(int animationType, int variant);

	void stopAnimation();

	void addAnimation(int animationType, int animationID);

	
	IEmotion getEmotion();
	
	void startEmotion(int emotionId);
	
	void stopEmotion();


	void clear();

	INbt getNbt();
	
	void reset();

	void setNbt(INbt nbt);
	
	void update();

}
