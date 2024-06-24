package noppes.npcs.api.handler;

import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IEmotion;

public interface IAnimationHandler {

	IAnimation createNewAnim();

	IAnimation getAnimation(int animationId);

	IAnimation getAnimation(String animationName);

	IAnimation[] getAnimations();

	boolean removeAnimation(int animationId);

	boolean removeAnimation(String animationName);

	
	IEmotion createNewEmtn();

	IEmotion getEmotion(int emotionId);

	IEmotion getEmotion(String emotionName);
	
	IEmotion[] getEmotions();
	
	boolean removeEmotion(int emotionId);

	boolean removeEmotion(String emotionName);

}
