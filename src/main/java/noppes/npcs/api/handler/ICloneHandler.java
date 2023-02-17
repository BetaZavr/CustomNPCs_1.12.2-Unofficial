package noppes.npcs.api.handler;

import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;

public interface ICloneHandler {
	IEntity<?> get(int p0, String p1, IWorld p2);

	void remove(int p0, String p1);

	void set(int p0, String p1, IEntity<?> p2);

	IEntity<?> spawn(double p0, double p1, double p2, int p3, String p4, IWorld p5);
}
