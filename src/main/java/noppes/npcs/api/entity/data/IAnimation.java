package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.ICustomNpc;

public interface IAnimation {

	IAnimationFrame addFrame();

	IAnimationFrame addFrame(IAnimationFrame frame);

	IAnimationFrame getFrame(int frame);

	IAnimationFrame[] getFrames();

	int getId();

	String getName();

	INbt getNbt();

	int getRepeatLast();

	boolean hasFrame(int frame);

	boolean removeFrame(IAnimationFrame frame);

	boolean removeFrame(int frame);

	void setName(String name);

	void setNbt(INbt nbt);

	void setRepeatLast(int frames);

	void startToNpc(ICustomNpc<?> npc);

}
