package noppes.npcs.api.entity.data;

import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
public interface INPCDisplay {

	int getBossbar();

	int getBossColor();

	String getCapeTexture();

	int getHitboxState();

	boolean getHasLivingAnimation();

	String getModel();

	float[] getModelScale(int part);

	String getName();

	String getOverlayTexture();

	int getShadowType();

	int getShowName();

	int getSize();

	String getSkinPlayer();

	String getSkinTexture();

	String getSkinUrl();

	int getTint();

	String getTitle();

	int getVisible();

	boolean isVisibleTo(IPlayer<?> player);

	void setBossbar(int type);

	void setBossColor(int color);

	void setCapeTexture(String texture);

	void setHitboxState(int state);

	void setHasLivingAnimation(boolean enabled);

	void setModel(String model);

	void setModelScale(int part, float x, float y, float z);

	void setName(String name);

	void setOverlayTexture(String texture);

	void setShadowType(int type);

	void setShowName(int type);

	void setSize(int size);

	void setSkinPlayer(String name);

	void setSkinTexture(String texture);

	void setSkinUrl(String url);

	void setTint(int color);

	void setTitle(String title);

	void setVisible(int type);

	boolean isNormalModel();

	void setNormalModel(boolean bo);

	String[] getDisableLayers();

	void setDisableLayers(String[] newLayers);
}
