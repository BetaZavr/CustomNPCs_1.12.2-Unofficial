package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IOverlayHUD {

	IItemSlot addItemSlot(@ParamName("orientationType") int orientationType,
						  @ParamName("x") int x, @ParamName("y") int y);

	IItemSlot addItemSlot(@ParamName("orientationType") int orientationType,
						  @ParamName("x") int x, @ParamName("y") int y,
						  @ParamName("stack") IItemStack stack);

	ILabel addLabel(@ParamName("id") int id, @ParamName("orientationType") int orientationType, @ParamName("label") String label,
					@ParamName("x") int x, @ParamName("y") int y,
					@ParamName("width") int width, @ParamName("height") int height);

	ILabel addLabel(@ParamName("id") int id, @ParamName("orientationType") int orientationType, @ParamName("label") String label,
					@ParamName("x") int x, @ParamName("y") int y,
					@ParamName("width") int width, @ParamName("height") int height, @ParamName("color") int color);

	ITexturedRect addTexturedRect(@ParamName("id") int id, @ParamName("orientationType") int orientationType, @ParamName("texture") String texture,
								  @ParamName("x") int x, @ParamName("y") int y,
								  @ParamName("width") int width, @ParamName("height") int height);

	ITexturedRect addTexturedRect(@ParamName("id") int id, @ParamName("orientationType") int orientationType, @ParamName("texture") String texture,
								  @ParamName("x") int x, @ParamName("y") int y,
								  @ParamName("width") int width, @ParamName("height") int height,
			int textureX, int textureY);

	IGuiTimer addTimer(@ParamName("id") int id, @ParamName("orientationType") int orientationType,
					   @ParamName("start") long start, @ParamName("end") long end,
					   @ParamName("x") int x, @ParamName("y") int y,
					   @ParamName("width") int width, @ParamName("height") int height);

	IGuiTimer addTimer(@ParamName("id") int id, @ParamName("orientationType") int orientationType,
					   @ParamName("start") long start, @ParamName("end") long end,
					   @ParamName("x") int x, @ParamName("y") int y,
					   @ParamName("width") int width, @ParamName("height") int height, @ParamName("color") int color);

	void clear();

	ICompassData getCompassData();

	ICustomGuiComponent getComponent(@ParamName("orientationType") int orientationType, @ParamName("componentId") int componentId);

	ICustomGuiComponent[] getComponents();

	ICustomGuiComponent[] getComponents(@ParamName("orientationType") int orientationType);

	IItemSlot[] getSlots();

	IItemSlot[] getSlots(@ParamName("orientationType") int orientationType);

	boolean isShowElementType(@ParamName("type") int type);

	boolean removeComponent(@ParamName("orientationType") int orientationType, @ParamName("componentId") int componentId);

	boolean removeSlot(@ParamName("orientationType") int orientationType, @ParamName("slotId") int slotId);

	void setShowElementType(@ParamName("type") int type, @ParamName("bo") boolean bo);

	void setShowElementType(@ParamName("name") String name, @ParamName("bo") boolean bo);

	void update();

}
