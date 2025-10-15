package noppes.npcs.api.handler.data;

import java.awt.Point;

import net.minecraft.util.math.Vec3d;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.IEntity;

@SuppressWarnings("all")
public interface IBorder {

	Point addPoint(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	Point addPoint(@ParamName("pos") IPos pos);

	Point addPoint(@ParamName("point") Point point, @ParamName("y") int y);

	void centerOffsetTo(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z, @ParamName("type") boolean type);

	void centerOffsetTo(@ParamName("pos") IPos pos, @ParamName("type") boolean type);

	void centerOffsetTo(@ParamName("point") Point point, @ParamName("type") boolean type);

	void clear();

	boolean contains(@ParamName("entity") IEntity<?> entity);

	boolean contains(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z, @ParamName("height") double height);

	boolean contains(@ParamName("x") int x, @ParamName("z") int z);

	double distanceTo(@ParamName("x") double x, @ParamName("z") double z);

	double distanceTo(@ParamName("entity") IEntity<?> entity);

	IAvailability getAvailability();

	IPos getCenter();

	int getClosestPoint(@ParamName("point") Point point, @ParamName("pos") IPos pos);

	Point[] getClosestPoints(@ParamName("point") Point point, @ParamName("pos") IPos pos);

	int getColor();

	int getDimensionId();

	IPos getHomePos();

	int getId();

	int getMaxX();

	int getMaxY();

	int getMaxZ();

	String getMessage();

    int getQuestID();

	void setQuestID(@ParamName("id") int id);

	boolean isQuestWhenEnter();

	void setIsQuestWhenEnter(@ParamName("bo") boolean bo);

	int getMinX();

	int getMinY();

	int getMinZ();

	String getName();

	INbt getNbt();

	Point[] getPoints();

	boolean insertPoint(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z, @ParamName("pos") IPos pos);

	boolean insertPoint(@ParamName("pos0") IPos pos0, @ParamName("pos1") IPos pos1);

	boolean insertPoint(@ParamName("point") Point point, @ParamName("y") int y, @ParamName("pos") IPos pos);

	boolean isShowToPlayers();

	void offset(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void offset(@ParamName("pos") IPos pos);

	void offset(@ParamName("point") Point point);

	boolean removePoint(@ParamName("x") int x, @ParamName("z") int z);

	boolean removePoint(@ParamName("point") Point point);

	void scaling(@ParamName("radius") double radius, @ParamName("type") boolean type);

	void scaling(@ParamName("scale") float scale, @ParamName("type") boolean type);

	void setColor(@ParamName("color") int color);

	void setDimensionId(@ParamName("dimensionId") int dimensionId);

	void setHomePos(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setMessage(@ParamName("message") String message);

	void setName(@ParamName("name") String name);

	void setNbt(@ParamName("nbt") INbt nbt);

	Point setPoint(@ParamName("index") int index, @ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	Point setPoint(@ParamName("index") int index, @ParamName("pos") IPos pos);

	Point setPoint(@ParamName("index") int index, @ParamName("point") Point point);

	Point setPoint(@ParamName("index") int index, @ParamName("point") Point point, @ParamName("y") int y);

	void setShowToPlayers(@ParamName("show") boolean show);

	int size();

	void update();

	Vec3d intersectsWithLine(@ParamName("startPos") Vec3d startPos, @ParamName("endPos") Vec3d endPos);

}
