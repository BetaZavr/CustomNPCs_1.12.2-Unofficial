package noppes.npcs.api.handler.data;

import java.awt.Point;

import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;

public interface IBorder {

	boolean contains(int x, int z);

	void setHomePos(int x, int y, int z);

	IPos getHomePos();

	Point[] getPoints();

	INbt getNbt();

	double distanceTo(IEntity<?> entity);

	double distanceTo(double x, double z);

	Point[] getClosestPoints(Point point, IPos pos);

	int getClosestPoint(Point point, IPos pos);

	boolean removePoint(Point point);

	boolean removePoint(int x, int z);

	boolean contains(double x, double y, double z, double height);

	void clear();

	int size();

	IPos getCenter();

	int getMaxZ();

	int getMinZ();

	void setNbt(INbt nbt);

	int getMaxX();

	int getMinX();

	int getMaxY();

	int getMinY();
	
	void scaling(float scale, boolean type);

	void scaling(double radius, boolean type);

	void centerOffsetTo(int x, int y, int z, boolean type);

	void centerOffsetTo(Point point, boolean type);

	void centerOffsetTo(IPos pos, boolean type);

	void offset(int x, int y, int z);

	void offset(Point point);

	void offset(IPos position);

	boolean insertPoint(int x, int y, int z, IPos pos);

	boolean insertPoint(Point point, int y, IPos pos);

	boolean insertPoint(IPos pos0, IPos pos1);

	Point addPoint(int x, int y, int z);

	Point addPoint(Point point, int y);

	Point addPoint(IPos pos);

	Point setPoint(int index, int x, int y, int z);

	Point setPoint(int index, Point point);

	Point setPoint(int index, Point point, int y);

	Point setPoint(int index, IPos pos);

	int getId();

	String getName();

	void setName(String name);

	int getDimensionId();

	void setDimensionId(int dimID);

	int getColor();

	void setColor(int color);

	IAvailability getAvailability();

	String getMessage();

	void setMessage(String message);

	void update();

}
