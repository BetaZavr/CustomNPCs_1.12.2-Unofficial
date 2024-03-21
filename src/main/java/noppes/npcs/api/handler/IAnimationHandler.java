package noppes.npcs.api.handler;

import noppes.npcs.api.entity.data.IAnimation;

public interface IAnimationHandler {
	
	IAnimation[] getAnimations();
	
	IAnimation getAnimation(int animationId);
	
	IAnimation getAnimation(String animationName);

	boolean removeAnimation(int animationId);
	
	boolean removeAnimation(String animationName);

	IAnimation createNew();
	
}
