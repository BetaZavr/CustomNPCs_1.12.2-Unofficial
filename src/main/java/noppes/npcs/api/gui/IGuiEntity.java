package noppes.npcs.api.gui;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;

@SuppressWarnings("all")
public interface IGuiEntity extends ICustomGuiComponent {

	IEntity<?> getEntity();

	float getScale();

	boolean hasBorder();

	boolean isShowArmorAndItems();

	void setBorder(@ParamName("hasBorder") boolean hasBorder);

	void setEntity(@ParamName("entity") IEntity<?> entity);

	void setScale(@ParamName("scale") float scale);

	void setShowArmorAndItems(@ParamName("show") boolean show);

	int getRotationType();
	
	void setRotationType(@ParamName("type") int type);
	
	int getYaw();
	
	int getPitch();
	
	void setRotation(@ParamName("yaw") int yaw, @ParamName("pitch") int pitch);
	
}
