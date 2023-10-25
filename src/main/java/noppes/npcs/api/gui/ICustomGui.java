package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public interface ICustomGui {
	
	IButton addButton(int id, String label, int x, int y);

	IButton addButton(int id, String label, int x, int y, int width, int height);

	IItemSlot addItemSlot(int x, int y);

	IItemSlot addItemSlot(int x, int y, IItemStack stack);

	ILabel addLabel(int id, String label, int x, int y, int width, int height);

	ILabel addLabel(int id, String label, int x, int y, int width, int height, int color);

	IScroll addScroll(int id, int x, int y, int width, int height, String[] list);

	ITextField addTextField(int id, int x, int y, int width, int height);

	IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture);

	IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture, int textureX, int textureY);

	ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height);

	ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY);

	ICustomGuiComponent getComponent(int id);

	ICustomGuiComponent[] getComponents();

	int getHeight();

	int getId();

	IItemSlot[] getSlots();

	int getWidth();

	void removeComponent(int id);

	void setBackgroundTexture(String resourceLocation);
	
	void setBackgroundTexture(int width, int height, int textureX, int textureY, int stretched, String resourceLocation);

	void setDoesPauseGame(boolean pauseGame);

	void setSize(int width, int height);

	void showPlayerInventory(int x, int y);

	void showPlayerInventory(int x, int y, boolean showSlots);
	
	void update(IPlayer<?> player);

	void updateComponent(ICustomGuiComponent component);

	
}
