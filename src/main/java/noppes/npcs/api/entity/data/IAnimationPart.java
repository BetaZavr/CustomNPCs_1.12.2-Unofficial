package noppes.npcs.api.entity.data;

public interface IAnimationPart {

	void clear();
	
	float[] getRotation();

	float[] getOffset();

	float[] getScale();

	void setRotation(float x, float y, float z);

	void setOffset(float x, float y, float z);

	void setScale(float x, float y, float z);

	boolean isDisable();

	void setDisable(boolean bo);
	
}
