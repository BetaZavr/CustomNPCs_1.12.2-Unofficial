package noppes.npcs.api.handler;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;

public interface ICloneHandler {

	IEntity<?> get(@ParamName("tab") int tab, @ParamName("name") String name, @ParamName("world") IWorld world);

	void remove(@ParamName("tab") int tab, @ParamName("name") String name);

	void set(@ParamName("tab") int tab, @ParamName("name") String name, @ParamName("entity") IEntity<?> entity);

	IEntity<?> spawn(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
					 @ParamName("tab") int tab, @ParamName("name") String name, @ParamName("world") IWorld world);

}
