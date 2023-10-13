package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.client.model.animation.AnimationConfig;

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

	void startAnimationFromSaved(int animationId);

	void startAnimationFromSaved(String animationName);

	boolean removeAnimation(int type, String name);
	
	void removeAnimations(int type);

	AnimationConfig createAnimation(int animationType);
	
	void setBaseAnimation(IAnimation animation);
	
}
