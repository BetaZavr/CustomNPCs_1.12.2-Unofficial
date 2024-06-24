package noppes.npcs.api.entity.data;

public interface IEmotion {

	String getName();

	int getId();

	boolean canBlink();

	void setCanBlink(boolean bo);

	IEmotionPart addFrame();

	IEmotionPart addFrame(IEmotionPart frame);

	boolean removeFrame(IEmotionPart frame);

	boolean removeFrame(int frameId);

}
