package noppes.npcs.api.entity.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.ICustomNpc;

@SuppressWarnings("all")
public interface IAnimation {

	IAnimationFrame addFrame();

	IAnimationFrame addFrame(@ParamName("frameId") int frameId, @ParamName("frame") IAnimationFrame frame);

	IAnimationFrame getFrame(@ParamName("frameId") int frameId);

	IAnimationFrame[] getFrames();

	int getId();

	String getName();

	INbt getNbt();

	int getRepeatLast();

	boolean hasFrame(@ParamName("frameId") int frameId);

	void removeFrame(@ParamName("frame") IAnimationFrame frame);

	void removeFrame(@ParamName("frameId") int frameId);

	void setName(@ParamName("name") String name);

	void setNbt(@ParamName("nbt") INbt nbt);

	void setRepeatLast(@ParamName("frames") int frames);

	void startToNpc(@ParamName("npc") ICustomNpc<?> npc);

	float getChance();

	void setChance(@ParamName("chance") float chance);

}
