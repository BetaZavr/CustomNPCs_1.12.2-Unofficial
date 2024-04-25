package noppes.npcs.api.handler;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public interface ICloneHandler {

	IEntity<?> get(int tab, String name, IWorld world);

	void remove(int tab, String name);

	void set(int tab, String name, IEntity<?> entity);

	IEntity<?> spawn(double x, double y, double z, int tab, String name, IWorld world);

}
