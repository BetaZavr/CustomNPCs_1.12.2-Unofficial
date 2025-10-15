package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IEmotion {

	String getName();

	int getId();

	boolean canBlink();

	void setCanBlink(@ParamName("bo") boolean bo);

	IEmotionPart addFrame();

	IEmotionPart addFrame(@ParamName("frame") IEmotionPart frame);

	boolean removeFrame(@ParamName("frame") IEmotionPart frame);

	boolean removeFrame(@ParamName("frameId") int frameId);

}
