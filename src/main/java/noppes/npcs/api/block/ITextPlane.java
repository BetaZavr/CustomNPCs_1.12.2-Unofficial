package noppes.npcs.api.block;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface ITextPlane {

	float getOffsetX();

	float getOffsetY();

	float getOffsetZ();

	int getRotationX();

	int getRotationY();

	int getRotationZ();

	float getScale();

	String getText();

	void setOffsetX(@ParamName("x") float x);

	void setOffsetY(@ParamName("y") float y);

	void setOffsetZ(@ParamName("z") float z);

	void setRotationX(@ParamName("x") int x);

	void setRotationY(@ParamName("y") int y);

	void setRotationZ(@ParamName("z") int z);

	void setScale(@ParamName("scale") float scale);

	void setText(@ParamName("text") String text);

}
