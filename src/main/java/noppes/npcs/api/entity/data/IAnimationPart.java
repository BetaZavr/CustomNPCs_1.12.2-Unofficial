package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IAnimationPart {

	void clear();

	float[] getOffset();

	float[] getRotation();

	float[] getRotationPart();

	float[] getScale();

	boolean isDisable();

	boolean isShow();

	void setDisable(@ParamName("bo") boolean bo);

	void setOffset(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setRotation(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setRotation(@ParamName("x1") float x1, @ParamName("y1") float y1);

	void setScale(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setShow(@ParamName("bo") boolean bo);

	int getType();

}
