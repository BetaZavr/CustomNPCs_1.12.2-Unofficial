package moe.plushie.armourers_workshop.api.common.skin.data;

public interface ISkinProperties {

	public Object getProperty(String key, Object defaultValue);

	public Boolean getPropertyBoolean(String key, Boolean defaultValue);

	public double getPropertyDouble(String key, double defaultValue);

	public int getPropertyInt(String key, int defaultValue);

	public String getPropertyString(String key, String defaultValue);

	public boolean haveProperty(String key);

	public void removeProperty(String key);

	public void setProperty(String key, Object value);

}
