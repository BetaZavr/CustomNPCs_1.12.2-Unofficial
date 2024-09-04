package noppes.npcs.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.util.RayTraceResults;
import noppes.npcs.util.RayTraceRotate;
import noppes.npcs.util.RayTraceVec;

import java.io.File;
import java.util.List;

public interface IMethods {

	String deleteColor(String str);

	double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1);

	double distanceTo(IEntity<?> entity, IEntity<?> target);

	RayTraceRotate getAngles3D(double dx, double dy, double dz, double mx, double my, double mz);

	RayTraceRotate getAngles3D(IEntity<?> entity, IEntity<?> target);

	String getJSONStringFromObject(Object obj);

	String getDataFile(String fileName);

	RayTraceVec getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius);

	RayTraceVec getPosition(IEntity<?> entity, double yaw, double pitch, double radius);

	RayTraceVec getVector3D(double dx, double dy, double dz, double mx, double my, double mz);

	RayTraceVec getVector3D(IEntity<?> entity, IEntity<?> target);

	RayTraceVec getVector3D(IEntity<?> entity, IPos pos);

	RayTraceResults rayTraceBlocksAndEntitys(IEntity<?> entity, double yaw, double pitch, double distance);

	Object readObjectFromNbt(NBTBase tag);

	IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos);

	NBTBase writeObjectToNbt(Object value);

	List<File> getFiles(File dir, String index);

	boolean removeFile(File directory);

	String loadFile(File file);

	boolean saveFile(File file, String text);

	boolean saveFile(File file, NBTTagCompound compound);

}
