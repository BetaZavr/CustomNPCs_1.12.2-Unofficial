package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.ICustomNpc;

public interface IAnimation {
	
	IAnimationFrame[] getFrames();
	
	IAnimationFrame getFrame(int frame);
	
	boolean hasFrame(int frame);

	IAnimationFrame addFrame();

	IAnimationFrame addFrame(IAnimationFrame frame);

	boolean removeFrame(int frame);
	
	boolean removeFrame(IAnimationFrame frame);

	int getType();

	String getName();

	void setName(String name);

	INbt getNbt();

	void setNbt(INbt nbt);
	
	void startToNpc(ICustomNpc<?> npc);

	boolean isDisable();

	void setDisable(boolean bo);

	int getRepeatLast();
	
	void setRepeatLast(int frames);

	
}
