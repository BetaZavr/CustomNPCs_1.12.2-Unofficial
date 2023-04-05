package noppes.npcs.api.gui;

import noppes.npcs.api.item.IItemStack;

public interface IOverlayHUD {

	boolean isShowElementType(int type);

	void setShowElementType(int type, boolean bo);

	void setShowElementType(String name, boolean bo);

	IItemSlot addItemSlot(int orientationType, int x, int y);

	IItemSlot addItemSlot(int orientationType, int x, int y, IItemStack stack);

	ILabel addLabel(int id, int orientationType, String label, int x, int y, int width, int height);

	ILabel addLabel(int id, int orientationType, String label, int x, int y, int width, int height, int color);

	ITexturedRect addTexturedRect(int id, int orientationType, String texture, int x, int y, int width, int height);

	ITexturedRect addTexturedRect(int id, int orientationType, String texture, int x, int y, int width, int height, int textureX, int textureY);

	IGuiTimer addTimer(int id, int orientationType, long start, long end, int x, int y, int width, int height);

	IGuiTimer addTimer(int id, int orientationType, long start, long end, int x, int y, int width, int height, int color);

	int[] getKeyPressed();

	int[] getMousePressed();

	boolean isMoved();

	boolean hasMousePress(int key);

	boolean hasOrKeysPressed(int ... keys);

	double[] getWindowSize();

	String getCurrentLanguage();

	ICustomGuiComponent getComponent(int orientationType, int componentID);

	ICustomGuiComponent[] getComponents(int orientationType);
	
	ICustomGuiComponent[] getComponents();

	IItemSlot[] getSlots(int orientationType);
	
	IItemSlot[] getSlots();

	boolean removeComponent(int orientationType, int componentID);
	
	boolean removeSlot(int orientationType, int slotID);
	
	void clear();
	
	void update();
	
}
