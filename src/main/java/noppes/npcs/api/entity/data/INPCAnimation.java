package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface INPCAnimation {

	boolean hasAnimations(@ParamName("animationType") int animationType);

	boolean hasAnimation(@ParamName("animationType") int animationType, @ParamName("animationId") int animationId);

	IAnimation[] getAnimations(@ParamName("animationType") int animationType);

	boolean removeAnimation(@ParamName("animationType") int animationType, @ParamName("animationId") int animationId);

	void removeAnimations(@ParamName("animationType") int animationType);

	void stopAnimation();

	void addAnimation(@ParamName("animationType") int animationType, @ParamName("animationId") int animationId);

	IEmotion getEmotion();

	void startEmotion(@ParamName("emotionId") int emotionId);

	void stopEmotion();

	void clear();

	INbt getNbt();

	void setNbt(@ParamName("nbt") INbt nbt);

	void update();

}
