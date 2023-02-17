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

	void setOffsetX(float p0);

	void setOffsetY(float p0);

	void setOffsetZ(float p0);

	void setRotationX(int p0);

	void setRotationY(int p0);

	void setRotationZ(int p0);

	void setScale(float p0);

	void setText(String p0);
}
