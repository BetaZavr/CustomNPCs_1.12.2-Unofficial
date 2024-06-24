package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.entity.ICustomNpc;

public interface IAnimation {

	IAnimationFrame addFrame();

	IAnimationFrame addFrame(int frameId, IAnimationFrame frame);

	IAnimationFrame getFrame(int frameId);

	IAnimationFrame[] getFrames();

	int getId();

	String getName();

	INbt getNbt();

	int getRepeatLast();

	boolean hasFrame(int frame);

	boolean removeFrame(IAnimationFrame frameId);

	boolean removeFrame(int frameId);

	void setName(String name);

	void setNbt(INbt nbt);

	void setRepeatLast(int frames);

	void startToNpc(ICustomNpc<?> npc);

}
