package noppes.npcs.api.gui;

import noppes.npcs.api.entity.IEntity;

public interface IGuiEntity
extends ICustomGuiComponent {

	IEntity<?> getEntity();
	
	void setEntity(IEntity<?> entity);
	
	float getScale();
	
	void setScale(float scale);
	
	boolean hasBorder();
	
	void setBorder(boolean hasBorder);
	
	boolean isShowArmorAndItems();
	
	void setShowArmorAndItems(boolean show);

}
