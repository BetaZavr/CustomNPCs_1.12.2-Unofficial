package noppes.npcs.api.block;

public interface ITextPlane {

	float getOffsetX();

	float getOffsetY();

	float getOffsetZ();

	int getRotationX();

	int getRotationY();

	int getRotationZ();

	float getScale();

	String getText();

	void setOffsetX(float x);

	void setOffsetY(float y);

	void setOffsetZ(float z);

	void setRotationX(int x);

	void setRotationY(int y);

	void setRotationZ(int z);

	void setScale(float scale);

	void setText(String text);

}
