package noppes.npcs.api.handler.data;

import java.awt.Point;

import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.entity.IEntity;

public interface IBorder {

	Point addPoint(int x, int y, int z);

	Point addPoint(IPos pos);

	Point addPoint(Point point, int y);

	void centerOffsetTo(int x, int y, int z, boolean type);

	void centerOffsetTo(IPos pos, boolean type);

	void centerOffsetTo(Point point, boolean type);

	void clear();

	boolean contains(double x, double y, double z, double height);

	boolean contains(int x, int z);

	double distanceTo(double x, double z);

	double distanceTo(IEntity<?> entity);

	IAvailability getAvailability();

	IPos getCenter();

	int getClosestPoint(Point point, IPos pos);

	Point[] getClosestPoints(Point point, IPos pos);

	int getColor();

	int getDimensionId();

	IPos getHomePos();

	int getId();

	int getMaxX();

	int getMaxY();

	int getMaxZ();

	String getMessage();

	int getMinX();

	int getMinY();

	int getMinZ();

	String getName();

	INbt getNbt();

	Point[] getPoints();

	boolean insertPoint(int x, int y, int z, IPos pos);

	boolean insertPoint(IPos pos0, IPos pos1);

	boolean insertPoint(Point point, int y, IPos pos);

	boolean isShowToPlayers();

	void offset(int x, int y, int z);

	void offset(IPos position);

	void offset(Point point);

	boolean removePoint(int x, int z);

	boolean removePoint(Point point);

	void scaling(double radius, boolean type);

	void scaling(float scale, boolean type);

	void setColor(int color);

	void setDimensionId(int dimID);

	void setHomePos(int x, int y, int z);

	void setMessage(String message);

	void setName(String name);

	void setNbt(INbt nbt);

	Point setPoint(int index, int x, int y, int z);

	Point setPoint(int index, IPos pos);

	Point setPoint(int index, Point point);

	Point setPoint(int index, Point point, int y);

	void setShowToPlayers(boolean show);

	int size();

	void update();

}
