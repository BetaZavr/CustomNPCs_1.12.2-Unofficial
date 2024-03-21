package noppes.npcs.api;

import net.minecraft.nbt.NBTBase;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.util.RayTraceResults;
import noppes.npcs.util.RayTraceRotate;
import noppes.npcs.util.RayTraceVec;

public interface IMetods {
	
	String deleteColor(String str);

	double distanceTo(IEntity<?> entity, IEntity<?> target);
	
	double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1);

	RayTraceRotate getAngles3D(IEntity<?> entity, IEntity<?> target);
	
	RayTraceRotate getAngles3D(double dx, double dy, double dz, double mx, double my, double mz);

	RayTraceVec getPosition(IEntity<?> entity, double yaw, double pitch, double radius);
	
	RayTraceVec getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius);

	RayTraceVec getVector3D(IEntity<?> entity, IEntity<?> target);

	RayTraceVec getVector3D(IEntity<?> entity, IPos pos);

	RayTraceVec getVector3D(double dx, double dy, double dz, double mx, double my, double mz);

	IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos);

	NBTBase writeObjectToNbt(Object value);

	Object readObjectFromNbt(NBTBase tag);

	String getJSONStringFromObject(Object obj);

	RayTraceResults rayTraceBlocksAndEntitys(IEntity<?> entity, double yaw, double pitch, double distance);
	
}
