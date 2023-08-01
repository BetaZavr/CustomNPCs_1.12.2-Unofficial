package noppes.npcs.api;

import noppes.npcs.api.entity.IEntity;

public interface IMetods {
	
	String deleteColor(String str);

	double distanceTo(IEntity<?> entity, IEntity<?> target);
	
	double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1);

	double[] getAngles3D(IEntity<?> entity, IEntity<?> target);
	
	double[] getAngles3D(double dx, double dy, double dz, double mx, double my, double mz);

	double[] getPosition(IEntity<?> entity, double yaw, double pitch, double radius);
	
	double[] getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius);

	double[] getVector3D(IEntity<?> entity, IEntity<?> target);

	double[] getVector3D(IEntity<?> entity, IPos pos);

	double[] getVector3D(double dx, double dy, double dz, double mx, double my, double mz);

	IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos);
	
}
