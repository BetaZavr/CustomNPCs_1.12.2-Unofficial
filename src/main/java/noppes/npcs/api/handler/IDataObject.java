package noppes.npcs.api.handler;

import noppes.npcs.api.handler.data.IDataElement;

public interface IDataObject {

	String get();

	IDataElement[] getClasses();

	String getClassesInfo();

	IDataElement getClazz(String name);

	IDataElement[] getConstructors();

	String getConstructorsInfo();

	IDataElement getField(String name);

	IDataElement[] getFields();

	String getFieldsInfo();

	String getInfo();

	IDataElement getMethod(String name);

	IDataElement[] getMethods();

	String getMethodsInfo();

}
