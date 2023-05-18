package noppes.npcs.api.handler;

import noppes.npcs.api.entity.data.IAnimation;

public interface IAnimationHandler {
	
	IAnimation[] getAnimations(int animationType);
	
	IAnimation getAnimation(int animationId);
	
	IAnimation getAnimation(String animationName);

	boolean removeAnimation(int animationId);
	
	boolean removeAnimation(String animationName);

	IAnimation createNew(int animationType);
	
}
