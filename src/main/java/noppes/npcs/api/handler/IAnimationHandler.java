package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.data.IAnimation;
import noppes.npcs.api.entity.data.IEmotion;

@SuppressWarnings("all")
public interface IAnimationHandler {

	IAnimation createNewAnim();

	IAnimation getAnimation(@ParamName("animationId") int animationId);

	IAnimation getAnimation(@ParamName("animationName") String animationName);

	IAnimation[] getAnimations();

	boolean removeAnimation(@ParamName("animationId") int animationId);

	boolean removeAnimation(@ParamName("animationName") String animationName);

	
	IEmotion createNewEmtn();

	IEmotion getEmotion(@ParamName("emotionId") int emotionId);

	IEmotion getEmotion(@ParamName("emotionName") String emotionName);
	
	IEmotion[] getEmotions();
	
	boolean removeEmotion(@ParamName("emotionId") int emotionId);

	boolean removeEmotion(@ParamName("emotionName") String emotionName);

}
