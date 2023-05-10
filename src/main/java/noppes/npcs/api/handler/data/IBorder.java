package noppes.npcs.api.handler.data;

import java.awt.Point;

import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;

public interface IBorder {

	boolean contains(int x, int z);

	void setHomePos(int x, int y, int z);

	BlockPos getHomePos();

	Point[] getPoints();

	INbt getNbt();

	double distanceTo(IEntity<?> entity);

	double distanceTo(double px, double py);

	Point[] getClosestPoints(Point point, IPos pos);

	int getClosestPoint(Point point, IPos pos);

	boolean removePoint(Point point);

	boolean removePoint(int x, int z);

	boolean contains(double x, double y, double z, double height);

	void clear();

	int size();

	IPos getIPosCenterMass();

	double[] getExactCenter();

	BlockPos getCenter();

	int getMaxZ();

	int getMinZ();

	void setNbt(INbt nbt);

	int getMaxX();

	int getMinX();

	void scaling(float scale, int typePos);

	void scaling(double radius, int typePos);

	void centerOffsetTo(int x, int y, int z, int typePos);

	void centerOffsetTo(Point point, int typePos);

	void centerOffsetToIPos(IPos position, int typePos);

	void offset(int x, int y, int z);

	void offset(Point point);

	void offset(IPos position);

	void insertPoint(int x, int y, int z, IPos entityPos);

	boolean insertPoint(Point point, int y, IPos entityPos);

	boolean insertPoint(IPos position, IPos entityPos);

	Point addPoint(int x, int y, int z);

	Point addPoint(Point point, int y);

	Point addPoint(IPos position);

	Point setPoint(int index, int x, int y, int z);

	Point setPoint(int index, Point point);

	Point setPoint(int index, Point point, int y);

	Point setPoint(int index, IPos position);

	int getId();

	String getName();

	void setName(String name);

	int getDimensionId();

	void setDimensionId(int dimID);

	int getColor();

	void setColor(int color);

}
