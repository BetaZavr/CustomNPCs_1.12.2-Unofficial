package noppes.npcs.api.handler;

import noppes.npcs.api.ParamName;
import noppes.npcs.api.handler.data.IDataElement;

@SuppressWarnings("all")
public interface IDataObject {

	String get();

	IDataElement[] getClasses();

	String getClassesInfo();

	IDataElement getClazz(@ParamName("name") String name);

	IDataElement[] getConstructors();

	String getConstructorsInfo();

	IDataElement getField(@ParamName("name") String name);

	IDataElement[] getFields();

	String getFieldsInfo();

	String getInfo();

	IDataElement getMethod(@ParamName("name") String name);

	IDataElement[] getMethods();

	String getMethodsInfo();

}
