package noppes.npcs.api;

public interface IContainerCustomChest extends IContainer {

	String getName();

	void setName(@ParamName("name") String name);

}
