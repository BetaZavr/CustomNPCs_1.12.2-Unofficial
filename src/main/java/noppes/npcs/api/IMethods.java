package noppes.npcs.api;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.util.IRayTraceResults;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;

import java.io.File;
import java.util.List;

@SuppressWarnings("all")
public interface IMethods {

	String ticksToElapsedTime(@ParamName("ticks") long ticks, @ParamName("isMilliSeconds") boolean isMilliSeconds,
							  @ParamName("colored") boolean colored, @ParamName("upped") boolean upped);

	String deleteColor(@ParamName("y") String str);

	double distanceTo(@ParamName("x0") double x0, @ParamName("y0") double y0, @ParamName("z0") double z0,
					  @ParamName("x1") double x1, @ParamName("y1") double y1, @ParamName("z1") double z1);

	double distanceTo(@ParamName("y") IEntity<?> entity, @ParamName("y") IEntity<?> target);

	IRayTraceRotate getAngles3D(@ParamName("dx") double dx, @ParamName("dy") double dy, @ParamName("dz") double dz,
								@ParamName("mx") double mx, @ParamName("dy") double my, @ParamName("mz") double mz);

	IRayTraceRotate getAngles3D(@ParamName("entity") IEntity<?> entity, @ParamName("target") IEntity<?> target);

	String getJSONStringFromObject(@ParamName("obj") Object obj);

	String getDataFile(@ParamName("fileName") String fileName);

	IRayTraceVec getPosition(@ParamName("cx") double cx, @ParamName("cy") double cy, @ParamName("cz") double cz,
							 @ParamName("yaw") double yaw, @ParamName("pitch") double pitch, @ParamName("radius") double radius);

	IRayTraceVec getPosition(@ParamName("entity") IEntity<?> entity,
							 @ParamName("yaw") double yaw, @ParamName("pitch") double pitch, @ParamName("radius") double radius);

	IRayTraceVec getVector3D(@ParamName("dx") double dx, @ParamName("dy") double dy, @ParamName("dz") double dz,
							 @ParamName("mx") double mx, @ParamName("my") double my, @ParamName("mz") double mz);

	IRayTraceVec getVector3D(@ParamName("entity") IEntity<?> entity, @ParamName("target") IEntity<?> target);

	IRayTraceVec getVector3D(@ParamName("entity") IEntity<?> entity, @ParamName("pos") IPos pos);

	IRayTraceResults rayTraceBlocksAndEntitys(@ParamName("entity") IEntity<?> entity, @ParamName("yaw") double yaw, @ParamName("pitch") double pitch,
											  @ParamName("distance") double distance);

	Object readObjectFromNbt(@ParamName("tag") NBTBase tag);

	IEntity<?> transferEntity(@ParamName("entity") IEntity<?> entity, @ParamName("dimension") int dimension, @ParamName("pos") IPos pos);

	NBTBase writeObjectToNbt(@ParamName("value") Object value);

	List<File> getFiles(@ParamName("dir") File dir, @ParamName("index") String index);

    String getTextNumberToRoman(@ParamName("value") int value);

    String getTextReducedNumber(@ParamName("value") double value, @ParamName("isInteger") boolean isInteger, @ParamName("color") boolean color, @ParamName("notPfx") boolean notPfx);

    boolean removeFile(@ParamName("directory") File directory);

	String loadFile(@ParamName("file") File file);

	boolean saveFile(@ParamName("file") File file, @ParamName("text") String text);

	boolean saveFile(@ParamName("file") File file, @ParamName("compound") NBTTagCompound compound);

    String translateGoogle(@ParamName("textLanguageKey") String textLanguageKey, @ParamName("translationLanguageKey") String translationLanguageKey, @ParamName("originalText") String originalText);

}
