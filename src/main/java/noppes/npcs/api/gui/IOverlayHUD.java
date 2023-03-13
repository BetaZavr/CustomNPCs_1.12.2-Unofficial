package noppes.npcs.api.gui;

import java.util.List;

import noppes.npcs.api.item.IItemStack;

public interface IOverlayHUD {

	boolean isShowElementType(int type);

	void setShowElementType(int type, boolean bo);

	void setShowElementType(String name, boolean bo);

	IItemSlot addItemSlot(int x, int y);

	IItemSlot addItemSlot(int x, int y, IItemStack stack);

	ILabel addLabel(int id, String label, int x, int y, int width, int height);

	ILabel addLabel(int id, String label, int x, int y, int width, int height, int color);

	ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height);

	ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY);

	IGuiTimer addTimer(int id, long start, long end, int x, int y, int width, int height);

	IGuiTimer addTimer(int id, long start, long end, int x, int y, int width, int height, int color);

	int[] getKeyPressed();

	int[] getMousePressed();

	boolean isMoved();

	boolean hasMousePress(int key);

	boolean hasOrKeysPressed(int ... keys);

	double[] getWindowSize();

	String getCurrentLanguage();

	ICustomGuiComponent getComponent(int componentID);

	List<ICustomGuiComponent> getComponents();

	List<IItemSlot> getSlots();

	void removeComponent(int componentID);
	
	int getOffsetType();
	
	void setOffsetType(int type);
	
	void update();
	
}
