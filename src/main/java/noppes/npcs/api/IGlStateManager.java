package noppes.npcs.api;

@SuppressWarnings("all")
public interface IGlStateManager {

	void enableBlend();
	void disableBlend();
	void enableAlpha();
	void disableAlpha();
	void pushMatrix();
	void popMatrix();

	void color(@ParamName("red") float red, @ParamName("green") float green, @ParamName("blue") float blue, @ParamName("alpha") float alpha);
	void translate(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);
	void scale(@ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);
	void rotate(@ParamName("angle") float angle, @ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);
	
	void drawString(@ParamName("text") String text,
					@ParamName("u") float x, @ParamName("v") float v,
					@ParamName("color") int color, @ParamName("dropShadow") boolean dropShadow);
	void drawTexture(@ParamName("resourceLocation") String resourceLocation, @ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
					 @ParamName("u") double u, @ParamName("v") double v, @ParamName("width") double width, @ParamName("height") double height,
					 @ParamName("revers") boolean revers);
	void draw(@ParamName("left") double left, @ParamName("top") double top, @ParamName("width") double width, @ParamName("height") double height,
			  @ParamName("color") int color, @ParamName("alpha") float alpha);
	void draw(@ParamName("left") double left, @ParamName("top") double top, @ParamName("width") double width, @ParamName("height") double height,
			  @ParamName("red") float red, @ParamName("green") float green, @ParamName("blue") float blue, @ParamName("alpha") float alpha);
	void renderEntity(@ParamName("entity") Object entity, @ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
					  @ParamName("yaw") float yaw, @ParamName("partialTicks") float partialTicks, @ParamName("disableDebugBoundingBox") boolean disableDebugBoundingBox);
	void drawOBJ(String resourceLocation);
	
}
