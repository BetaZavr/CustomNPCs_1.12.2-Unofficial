package noppes.npcs.api;

public interface IGlStateManager {

	void enableBlend();
	void disableBlend();
	void enableAlpha();
	void disableAlpha();
	void pushMatrix();
	void popMatrix();

	void color(float red, float green, float blue, float alpha);
	void translate(float x, float y, float z);
	void scale(float x, float y, float z);
	void rotate(float angle, float axisX, float axisY, float axisZ);
	
	void drawString(String text, float x, float y, int color, boolean dropShadow);
	void drawTexture(String resourceLocation, double x, double y, double z, double u, double v, double width, double height, boolean revers);
	void draw(double left, double top, double width, double height, int color, float alpha);
	void draw(double left, double top, double width, double height, float red, float green, float blue, float alpha);
	void renderEntity(Object entity, double x, double y, double z, float yaw, float partialTicks, boolean disableDebugBoundingBox);
	void drawOBJ(String resourceLocation);
	
}
