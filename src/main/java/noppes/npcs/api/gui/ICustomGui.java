package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface ICustomGui {

	IButton addButton(@ParamName("id") int id, @ParamName("id") String label,
					  @ParamName("x") int x, @ParamName("y") int y);

	IButton addButton(@ParamName("id") int id, @ParamName("id") String label,
					  @ParamName("x") int x, @ParamName("y") int y,
					  @ParamName("width") int width, @ParamName("height") int height);

	IGuiEntity addEntity(@ParamName("id") int id,
						 @ParamName("x") int x, @ParamName("y") int y,
						 @ParamName("id") IEntity<?> entity);

	IItemSlot addItemSlot(@ParamName("x") int x, @ParamName("y") int y);

	IItemSlot addItemSlot(@ParamName("x") int x, @ParamName("y") int y,
						  @ParamName("id") IItemStack stack);

	ILabel addLabel(@ParamName("id") int id, @ParamName("id") String label,
					@ParamName("x") int x, @ParamName("y") int y,
					@ParamName("width") int width, @ParamName("height") int height);

	ILabel addLabel(@ParamName("id") int id, @ParamName("id") String label,
					@ParamName("x") int x, @ParamName("y") int y,
					@ParamName("width") int width, @ParamName("height") int height,
					@ParamName("color") int color);

	IScroll addScroll(@ParamName("id") int id,
					  @ParamName("x") int x, @ParamName("y") int y,
					  @ParamName("width") int width, @ParamName("height") int height,
					  @ParamName("id") String[] list);

	ITextField addTextField(@ParamName("id") int id,
							@ParamName("x") int x, @ParamName("y") int y,
							@ParamName("width") int width, @ParamName("height") int height);

	IButton addTexturedButton(@ParamName("id") int id, @ParamName("label") String label,
							  @ParamName("x") int x, @ParamName("y") int y,
							  @ParamName("width") int width, @ParamName("height") int height,
							  @ParamName("texture") String texture);

	IButton addTexturedButton(@ParamName("id") int id, @ParamName("label") String label,
							  @ParamName("x") int x, @ParamName("y") int y,
							  @ParamName("width") int width, @ParamName("height") int height,
							  @ParamName("texture") String texture, @ParamName("textureX") int textureX, @ParamName("textureY") int textureY);

	ITexturedRect addTexturedRect(@ParamName("id") int id, @ParamName("texture") String texture,
								  @ParamName("x") int x, @ParamName("y") int y,
								  @ParamName("width") int width, @ParamName("height") int height);

	ITexturedRect addTexturedRect(@ParamName("id") int id, @ParamName("id") String texture,
								  @ParamName("x") int x, @ParamName("y") int y,
								  @ParamName("width") int width, @ParamName("height") int height,
								  @ParamName("textureZ") int textureX, @ParamName("textureY") int textureY);

	ICustomGuiComponent getComponent(@ParamName("id") int id);

	ICustomGuiComponent[] getComponents();

	int getHeight();

	int getId();

	IItemSlot[] getSlots();

	int getWidth();

	void removeComponent(@ParamName("id") int id);

	void setBackgroundTexture(@ParamName("width") int width, @ParamName("height") int height,
							  @ParamName("textureX") int textureX, @ParamName("textureY") int textureY,
							  @ParamName("stretched") int stretched, @ParamName("resourceLocation") String resourceLocation);

	void setBackgroundTexture(@ParamName("id") String resourceLocation);

	void setDoesPauseGame(@ParamName("pauseGame") boolean pauseGame);

	void setSize(@ParamName("width") int width, @ParamName("height") int height);

	void showPlayerInventory(@ParamName("x") int x, @ParamName("Y") int y);

	void showPlayerInventory(@ParamName("x") int x, @ParamName("Y") int y, @ParamName("showSlots") boolean showSlots);

	void update(@ParamName("player") IPlayer<?> player);

	void updateComponent(@ParamName("component") ICustomGuiComponent component);

}
