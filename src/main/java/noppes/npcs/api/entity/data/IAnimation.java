package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.ICustomNpc;

public interface IAnimation {
	
	public interface IAnimationPart {

		void clear();
		
		float[] getRotation();

		float[] getOffset();

		float[] getScale();

		void setRotation(float x, float y, float z);

		void setOffset(float x, float y, float z);

		void setScale(float x, float y, float z);

		boolean isDisabled();

		void setDisabled(boolean disabled);

		boolean isSmooth();

		void setSmooth(boolean isSmooth);

		int getSpeed();

		void setSpeed(int ticks);

		void setEndDelay(int ticks);

		int getEndDelay();
		
		INbt getNbt();

		void setNbt(INbt nbt);
		
	}

	IAnimationPart[] getParts(int frame);
	
	IAnimationPart getPart(int frame, int part);

	int getType();

	int getId();

	String getName();

	void setName(String name);

	INbt getNbt();

	void setNbt(INbt nbt);
	
	void startToNpc(ICustomNpc<?> npc);

	int addFrame();

	int addFrame(IAnimationPart[] parts);

	boolean isDisable();

	void setDisable(boolean bo);

	int getRepeatLast();
	
	void setRepeatLast(int frames);
	
}
