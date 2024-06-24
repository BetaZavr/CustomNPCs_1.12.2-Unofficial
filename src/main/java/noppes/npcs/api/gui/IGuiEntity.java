package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IEntity;

public interface IGuiEntity extends ICustomGuiComponent {

	IEntity<?> getEntity();

	float getScale();

	boolean hasBorder();

	boolean isShowArmorAndItems();

	void setBorder(boolean hasBorder);

	void setEntity(IEntity<?> entity);

	void setScale(float scale);

	void setShowArmorAndItems(boolean show);

	int getRotationType();
	
	void setRotationType(int type);
	
	int getYaw();
	
	int getPitch();
	
	void setRotation(int yaw, int pitch);
	
}
