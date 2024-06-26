package noppes.npcs.api.entity.data;

public interface IAnimationPart {

	void clear();

	float[] getOffset();

	float[] getRotation();

	float[] getRotationPart();

	float[] getScale();

	boolean isDisable();

	boolean isShow();

	void setDisable(boolean bo);

	void setOffset(float x, float y, float z);

	void setRotation(float x, float y, float z);

	void setRotation(float x1, float y1);

	void setScale(float x, float y, float z);

	void setShow(boolean bo);

	int getType();

}
