package noppes.npcs.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.util.IRayTraceResults;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;

import java.io.File;
import java.util.List;

public interface IMethods {

	String ticksToElapsedTime(long ticks, boolean isMilliSeconds, boolean colored, boolean upped);

	String deleteColor(String str);

	double distanceTo(double x0, double y0, double z0, double x1, double y1, double z1);

	double distanceTo(IEntity<?> entity, IEntity<?> target);

	IRayTraceRotate getAngles3D(double dx, double dy, double dz, double mx, double my, double mz);

	IRayTraceRotate getAngles3D(IEntity<?> entity, IEntity<?> target);

	String getJSONStringFromObject(Object obj);

	String getDataFile(String fileName);

	IRayTraceVec getPosition(double cx, double cy, double cz, double yaw, double pitch, double radius);

	IRayTraceVec getPosition(IEntity<?> entity, double yaw, double pitch, double radius);

	IRayTraceVec getVector3D(double dx, double dy, double dz, double mx, double my, double mz);

	IRayTraceVec getVector3D(IEntity<?> entity, IEntity<?> target);

	IRayTraceVec getVector3D(IEntity<?> entity, IPos pos);

	IRayTraceResults rayTraceBlocksAndEntitys(IEntity<?> entity, double yaw, double pitch, double distance);

	Object readObjectFromNbt(NBTBase tag);

	IEntity<?> transferEntity(IEntity<?> entity, int dimension, IPos pos);

	NBTBase writeObjectToNbt(Object value);

	List<File> getFiles(File dir, String index);

    String getTextNumberToRoman(int value);

    String getTextReducedNumber(double value, boolean isInteger, boolean color, boolean notPfx);

    boolean removeFile(File directory);

	String loadFile(File file);

	boolean saveFile(File file, String text);

	boolean saveFile(File file, NBTTagCompound compound);

    String translateGoogle(String textLanguageKey, String translationLanguageKey, String originalText);

}
