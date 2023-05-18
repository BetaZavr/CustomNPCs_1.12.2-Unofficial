package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;

public interface INPCAnimation {

	void reset();

	void stop();

	void clear();

	void update();

	IAnimation[] getAnimations(int animationType);

	IAnimation getAnimation(int animationType, int variant);

	void start(int animationType);

	void start(int animationType, int variant);
	
	INbt getNbt();
	
	void setNbt(INbt nbt);

	void startFromSaved(int animationId);

	void startFromSaved(String animationName);

}
