package noppes.npcs.api.handler;

import noppes.npcs.api.entity.data.IAnimation;

public interface IAnimationHandler {

	IAnimation createNew();

	IAnimation getAnimation(int animationId);

	IAnimation getAnimation(String animationName);

	IAnimation[] getAnimations();

	boolean removeAnimation(int animationId);

	boolean removeAnimation(String animationName);

}
