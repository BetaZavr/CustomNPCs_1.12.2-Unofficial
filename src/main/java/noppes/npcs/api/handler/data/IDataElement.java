package noppes.npcs.api.handler.data;

public interface IDataElement {

	String getData();

	String getName();

	Object getObject();

	Class<?> getParent();

	int getType();

	Object getValue();

	Object invoke(Object[] values);

	boolean isBelong(Class<?> clazz);

	boolean setValue(Object value);

}
