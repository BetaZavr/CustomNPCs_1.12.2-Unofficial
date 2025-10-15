package noppes.npcs.api.entity.data;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
public interface INPCDisplay {

	int getBossbar();

	int getBossColor();

	String getCapeTexture();

	int getHitboxState();

	boolean getHasLivingAnimation();

	String getModel();

	float[] getModelScale(@ParamName("part") int part);

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

	boolean isVisibleTo(@ParamName("player") IPlayer<?> player);

	void setBossbar(@ParamName("type") int type);

	void setBossColor(@ParamName("color") int color);

	void setCapeTexture(@ParamName("texture") String texture);

	void setHitboxState(@ParamName("state") int state);

	void setHasLivingAnimation(@ParamName("enabled") boolean enabled);

	void setModel(@ParamName("model") String model);

	void setModelScale(@ParamName("part") int part, @ParamName("x") float x, @ParamName("y") float y, @ParamName("z") float z);

	void setName(@ParamName("name") String name);

	void setOverlayTexture(@ParamName("texture") String texture);

	void setShadowType(@ParamName("type") int type);

	void setShowName(@ParamName("type") int type);

	void setSize(@ParamName("size") int size);

	void setSkinPlayer(@ParamName("name") String name);

	void setSkinTexture(@ParamName("texture") String texture);

	void setSkinUrl(@ParamName("url") String url);

	void setTint(@ParamName("color") int color);

	void setTitle(@ParamName("title") String title);

	void setVisible(@ParamName("type") int type);

	boolean isNormalModel();

	void setNormalModel(@ParamName("bo") boolean bo);

}
