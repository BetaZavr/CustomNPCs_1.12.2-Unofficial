package noppes.npcs.api.handler.data;

import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IDataElement {

	String getData();

	String getName();

	Object getObject();

	Class<?> getParent();

	int getType();

	Object getValue();

	Object invoke(@ParamName("values") Object[] values);

	boolean isBelong(@ParamName("clazz") Class<?> clazz);

	boolean setValue(@ParamName("value") Object value);

}
