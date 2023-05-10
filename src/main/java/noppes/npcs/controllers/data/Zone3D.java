package noppes.npcs.controllers.data;

import java.awt.Point;
import java.awt.Polygon;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import com.google.common.base.Predicate;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.util.AdditionalMethods;

public class Zone3D
implements IBorder, Predicate<Entity> {

	private int id = -1;
	public String name = "Default Region";
	public TreeMap<Integer, Point> points = Maps.<Integer, Point>newTreeMap();
	public int[] y = new int[] {0, 255};
	public int dimensionID = 0;
	public int color;
	
	public Availability availability;
	public String message;
	private List<Entity> entitiesWithinRegion;
	private Map<Entity, AntiLagTime> playerAntiLag;
	public BlockPos homePos;
	public boolean keepOut = false;
	
	public Zone3D () {
		this.color = (new Random()).nextInt(0xFFFFFF);
		this.availability = new Availability();
		this.message = "availability.areaNotAvailble";
		this.playerAntiLag = Maps.<Entity, AntiLagTime>newHashMap();
		this.entitiesWithinRegion = Lists.<Entity>newArrayList();
	}
	
	public Zone3D (int id, int dimensionID, int x, int y, int z) {
		this();
		this.id = id;
		this.dimensionID = dimensionID;
		this.y[0] = y-2;
		this.y[1] = y+2;
		if (this.y[0]<0) { this.y[0] = 0; }
		if (this.y[1]>255) { this.y[1] = 255; }
		this.addPoint(x, y, z);
	}
	
	/**
	 * Sets a point instead of an existing one
	 * @param index
	 * @param position
	 */
	public Point setPoint(int index, BlockPos position) { return this.setPoint(index, position.getX(), position.getY(), position.getZ()); }

	@Override
	public Point setPoint(int index,IPos position) { return this.setPoint(index, position.getMCBlockPos()); }
	
	/**
	 * Sets a point instead of an existing one
	 * @param index
	 * @param y
	 * @param point
	 */
	@Override
	public Point setPoint(int index, Point point, int y) {
		if (!this.points.containsKey(index) && index!=this.points.size()) { return null; }
		this.points.put(index, point);
		if (y>=0) {
			if (y<this.y[0]) { this.y[0] = y; }
			if (y>this.y[1]) { this.y[1] = y; }
		}
		return point;
	}

	/**
	 * Sets a point instead of an existing one
	 * @param index
	 * @param point
	 */
	@Override
	public Point setPoint(int index, Point point) {
		if (!this.points.containsKey(index) && index!=this.points.size()) { return null; }
		this.points.put(index, point);
		return point;
	}
	
	/**
	 * Sets a point instead of an existing one
	 * @param index
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public Point setPoint(int index, int x, int y, int z) {
		Point point = new Point();
		point.x = x;
		point.y = z;
		return this.setPoint(index, point, y);
	}

	/**
	 * Adds a new point to the end
	 * @param position
	 */
	public Point addPoint(BlockPos position) { return this.addPoint(position.getX(), position.getY(), position.getZ()); }

	@Override
	public Point addPoint(IPos position) { return this.addPoint(position.getMCBlockPos()); }

	/**
	 * Adds a new point to the end
	 * @param point
	 */
	@Override
	public Point addPoint(Point point, int y) {
		for (Point p : this.points.values()) { if (p.x==point.x && p.y==point.y) { return null; } }
		this.points.put(this.points.size(), point);
		if (y<0) { y=0; } else if (y>255) { y = 255; }
		if (y<this.y[0]) { this.y[0] = y; }
		else if (y>this.y[1]) { this.y[1] = y; }
		return point;
	}
	
	/**
	 * Adds a new point to the end
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public Point addPoint(int x, int y, int z) {
		Point point = new Point();
		point.x = x;
		point.y = z;
		return this.addPoint(point, y);
	}

	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * @param position
	 */
	public boolean insertPoint(BlockPos position, BlockPos entityPos) { return this.insertPoint(position.getX(), position.getY(), position.getZ(), entityPos); }

	@Override
	public boolean insertPoint(IPos position, IPos entityPos) { return this.insertPoint(position.getMCBlockPos(), entityPos.getMCBlockPos()); }
	
	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * @param point
	 */
	public boolean insertPoint(Point point, int y, BlockPos entityPos) {
		if (y>=0) {
			int min = Math.abs(this.y[0]-y);
			int max = Math.abs(this.y[1]-y);
			if (min<=max) { this.y[0] = y; }
			else { this.y[1] = y;}
		}
		if (this.contains(point.x, point.y)) { return false; }
		if (this.points.size()<2) { return this.addPoint(point, y)!=null; }
		int pos = this.getClosestPoint(point, entityPos);
		TreeMap<Integer, Point> temp = Maps.<Integer, Point>newTreeMap();
		for (int i=0, j=0; i<this.points.size(); i++) {
			temp.put(i+j, this.points.get(i));
			if (i==pos) { j=1; temp.put(i+j, point); }
		}
		this.points = temp;
		return true;
	}
	
	@Override
	public boolean insertPoint(Point point, int y, IPos entityPos) { return this.insertPoint(point, y, entityPos.getMCBlockPos()); }
	
	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * @param x
	 * @param y
	 * @param z
	 */
	public boolean insertPoint(int x, int y, int z, BlockPos entityPos) {
		Point p = new Point();
		p.x = x;
		p.y = z;
		return this.insertPoint(p, y, entityPos);
	}
	
	@Override
	public void insertPoint(int x, int y, int z, IPos entityPos) { this.insertPoint(x, y, z, entityPos.getMCBlockPos()); }
	
	/**
	 * Offsets the entire zone by the specified value
	 * @param position
	 */
	public void offset(BlockPos position) { this.offset(position.getX(), position.getY(), position.getZ()); }

	@Override
	public void offset(IPos position) { this.offset(position.getMCBlockPos()); }
	
	/**
	 * Offsets the entire zone by the specified value
	 * @param point
	 */
	@Override
	public void offset(Point point) { this.offset(point.x, 0, point.y); }
	
	/**(this.y[1]+this.y[0])/2
	 * Offsets the entire zone by the specified value
	 * @param x
	 * @param y
	 * @param z
	 */
	@Override
	public void offset(int x, int y, int z) {
		this.y[0] += y;
		this.y[1] += y;
		for (int key : this.points.keySet()) {
			Point p = this.points.get(key);
			this.points.get(key).move(p.x+x, p.y+z);
		}
	}

	/**
	 * Offsets the position of the zone
	 * @param position
	 * @param typePos = 0 - relative to the zero coordinate;
	 * @param typePos = 1 - relative to the center of the described contour;
	 * @param typePos = 2 - relative to the center of mass
	 */
	public void centerOffsetTo(BlockPos position, int typePos) { this.centerOffsetTo(position.getX(), position.getY(), position.getZ(), typePos); }
	
	@Override
	public void centerOffsetToIPos(IPos position, int typePos) {
		this.centerOffsetTo(position.getMCBlockPos(), typePos);
	}
	/**
	 * Offsets the position of the zone
	 * @param point
	 * @param typePos = 0 - relative to the zero coordinate;
	 * @param typePos = 1 - relative to the center of the described contour;
	 * @param typePos = 2 - relative to the center of mass
	 */
	@Override
	public void centerOffsetTo(Point point, int typePos) { this.centerOffsetTo(point.x, (int) ((this.y[0]+this.y[1])/2), point.y, typePos); }
	
	/**
	 * Offsets the position of the zone
	 * @param x
	 * @param y
	 * @param z
	 * @param typePos = 0 - relative to the zero coordinate;
	 * @param typePos = 1 - relative to the center of the described contour;
	 * @param typePos = 2 - relative to the center of mass
	 */
	@Override
	public void centerOffsetTo(int x, int y, int z, int typePos) {
		BlockPos ctr = null;
		int ry = (this.y[1]-this.y[0])/2;
		if (typePos==1) {
			ctr = this.getCenter();
			this.y[0] = y-ry;
			this.y[1] = y+ry;
		}
		else if (typePos==1) {
			ctr = this.getCenterMass();
			this.y[0] = y-ry;
			this.y[1] = y+ry;
		}
		else {
			ctr = new BlockPos(this.getMinX(), this.y[0], this.getMinZ());
			this.y[0] = y;
			this.y[1] = y+ry*2;
		}
		for (int key : this.points.keySet()) {
			Point p = this.points.get(key);
			this.points.get(key).move(x+p.x-ctr.getX(), z+p.y-ctr.getZ());
		}
	}

	/**
	 * Expand or Shrink a zone outline by a specific value
	 * @param radius - offset value of each point relative to the center
	 * @param typePos = 0 - relative to the zero coordinate;
	 * @param typePos = 1 - relative to the center of the described contour;
	 * @param typePos = 2 - relative to the center of mass
	 */
	@Override
	public void scaling(double radius, int typePos) {
		if (this.points.size()<=1) { return; }
		BlockPos ctr = null;
		if (typePos==1) { ctr = this.getCenter(); }
		else if (typePos==1) { ctr = this.getCenterMass(); }
		else { ctr = new BlockPos(this.getMinX(), this.y[0], this.getMinZ()); }
		this.y[0] -= (int) radius;
		this.y[1] += (int) radius;
		for (int key :this.points.keySet()) {
			Point v = this.points.get(key);
			double[] data = AdditionalMethods.getAngles3D(ctr.getX(), 0, ctr.getZ(), v.x, 0, v.y);
			double[] newPoint = AdditionalMethods.getPosition(ctr.getX(), 0, ctr.getZ(), data[0], data[1], radius + data[2]);
			this.points.put(key, new Point((int) newPoint[0], (int) newPoint[2]));
		}
	}
	
	/**
	 * Scale zone outline
	 * @param scale - percentage where 100% = 1.0f
	 * @param typePos = 0 - relative to the zero coordinate;
	 * @param typePos = 1 - relative to the center of the described contour;
	 * @param typePos = 2 - relative to the center of mass
	 */
	@Override
	public void scaling(float scale, int typePos) {
		if (this.points.size()<=1) { return; }
		BlockPos ctr = null;
		if (typePos==1) { ctr = this.getCenter(); }
		else if (typePos==1) { ctr = this.getCenterMass(); }
		else { ctr = new BlockPos(this.getMinX(), this.y[0], this.getMinZ()); }
		
		for (int key :this.points.keySet()) {
			Point v = this.points.get(key);
			double[] data = AdditionalMethods.getAngles3D(ctr.getX(), ctr.getY(), ctr.getZ(), v.x, ctr.getY(), v.y);
			double[] newPoint = AdditionalMethods.getPosition(ctr.getX(), ctr.getY(), ctr.getZ(), data[0], data[1], (double) scale * data[2]);
			this.points.put(key, new Point((int) newPoint[0], (int) newPoint[2]));
			if (this.y[0] > (int) newPoint[2]) { this.y[0] = (int) newPoint[1]; }
			if (this.y[1] < (int) newPoint[2]) { this.y[1] = (int) newPoint[1]; }
		}
	}

	@Override
	public int getMinX() {
		if (this.points.size()==0) { return 0; }
		int value = this.points.get(0).x;
		for (Point v : this.points.values()) { if (value > v.x) { value = v.x; } }
		return value;
	}

	@Override
	public int getMaxX() {
		if (this.points.size()==0) { return 0; }
		int value = this.points.get(0).x;
		for (Point v : this.points.values()) { if (value < v.x) { value = v.x; } }
		return value;
	}

	@Override
	public int getMinZ() {
		if (this.points.size()==0) { return 0; }
		int value = this.points.get(0).y;
		for (Point v : this.points.values()) { if (value > v.y) { value = v.y; } }
		return value;
	}

	@Override
	public int getMaxZ() {
		if (this.points.size()==0) { return 0; }
		int value = this.points.get(0).y;
		for (Point v : this.points.values()) { if (value < v.y) { value = v.y; } }
		return value;
	}
	
	/**
	 * orders positions
	 */
	public void fix() {
		if (this.points==null) { this.points = Maps.<Integer, Point>newTreeMap(); }
		TreeMap<Integer, Point> newList = Maps.<Integer, Point>newTreeMap();
		int i=0;
		boolean needChange = false;
		for (int pos : this.points.keySet()) {
			newList.put(i, this.points.get(pos));
			if (i!=pos) { needChange = true; }
			i++;
		}
		if (needChange) { this.points = newList; }
		this.getHomePos();
	}

	/**
	 * @return center of the described zone contour
	 */
	@Override
	public BlockPos getCenter() {
		return new BlockPos((this.getMinX()+this.getMaxX())/2.0d, (this.y[0]+this.y[1])/2.0d, (this.getMinZ()+this.getMaxZ())/2.0d);
	}

	@Override
	public double[] getExactCenter() {
		double x = 0.0d, z = 0.0d;
		double y = ((double) this.y[1] - (double) this.y[0]) / 2.0d;
		for (Point v : this.points.values()) {
			 x += (double) v.x;
			 z += (double) v.y;
		}
		if (this.points.size()>0) {
			x /= (double) this.points.size();
			z /= (double) this.points.size();
		}
		return new double[] { x, y, z};
	}
	
	/**
	 * @return center of mass of the zone
	 */
	public BlockPos getCenterMass() {
		double x=0.0d, y=(this.y[0]+this.y[1])/2.0d, z=0.0d;
		for (Point v :this.points.values()) { x += v.x; z += v.y; }
		return new BlockPos(x/(double)this.points.size(), y, z/(double)this.points.size());
	}
	
	@Override
	public IPos getIPosCenterMass() {
		BlockPos pos = this.getCenterMass();
		return NpcAPI.Instance().getIPos(pos.getX(), pos.getY(), pos.getZ());
	}
	
	/**
	 * @return number of vertices of a zone
	 */
	@Override
	public int size() { return this.points.size(); }

	/**
	 * @return size X x Y x Z
	 */
	public String getSize() {
		int x = this.getMaxX()-this.getMinX();
		if (x<0) { x *= -1; }
		int y = this.y[0]-this.y[1];
		if (y<0) { y *= -1; }
		int z = this.getMaxZ()-this.getMinZ();
		if (z<0) { z *= -1; }
		return x+"x"+y+"x"+z;
	}

	@Override
	public void clear() {
		this.points = Maps.<Integer, Point>newTreeMap();
		this.y[0] = 255;
		this.y[1] = 0;
	}

	@Override
	public boolean contains(double x, double y, double z, double height) {
		int dx = (int) (x*10.0d);
		int dz = (int) (z*10.0d);
		if (y+height<this.y[0] || y-height>this.y[1]) { return false; }
		Polygon poly = new Polygon();
		boolean isIn = false;
		for (Point p : this.points.values()) {
			int	px = 5+(p.x*10);
			int	py = 5+(p.y*10);
			poly.addPoint(px, py);
			isIn = (px==dx && py==dz);
			if (isIn) { break; }
		}
		if (isIn) { return true; }
		isIn = poly.contains(dx, dz);
		return isIn;
	}

	/**
	 * Remove point from polygon
	 * @param x
	 * @param z
	 */
	@Override
	public boolean removePoint(int x, int z) {
		if (this.points==null || this.points.size()==0) { return false; }
		for (int key : this.points.keySet()) {
			if (this.points.get(key).x==x && this.points.get(key).y==z) {
				this.points.remove(key);
				this.fix();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Remove point from polygon
	 * @param point
	 */
	@Override
	public boolean removePoint(Point point) {
		if (point==null || this.points==null || this.points.size()<=3) { return false; }
		return removePoint(point.x, point.y);
	}

	public boolean equals(Zone3D zone) {
		if (zone==null) { return false; }
		if (zone.y[0]!=this.y[0] || zone.y[1]!=this.y[1]) { return false; }
		if (zone.points.size() != this.points.size()) { return false; }
		for (int key : zone.points.keySet()) {
			if (!this.points.containsKey(key)) { return false; }
			Point p0 = zone.points.get(key);
			Point p1 = this.points.get(key);
			if (p0.x!=p1.x || p0.y!=p1.y) { return false; }
		}
		return true;
	}

	@Override
	public int getClosestPoint(Point point, IPos pos) {
		return this.getClosestPoint(point, pos.getMCBlockPos());
	}
	
	public int getClosestPoint(Point point, BlockPos entityPos) {
		if (this.points.size()==0) { return -1; }
		if (this.points.size()==1) { return 0; }
		int pos = 0;
		Point entPoint = new Point(entityPos.getX(), entityPos.getZ());
		double dm0 = this.points.get(0).distance(point);
		double dm1 = this.points.get(1).distance(point);
		double dm2 = this.points.get(0).distance(entPoint);
		double dm3 = this.points.get(1).distance(entPoint);
		for (int p=0; (p+1)<this.points.size(); p++) {
			double d0 = this.points.get(p).distance(point);
			double d1 = this.points.get(p+1).distance(point);
			double d2 = this.points.get(p).distance(entPoint);
			double d3 = this.points.get(p+1).distance(entPoint);
			if (dm0+dm1+dm2+dm3>d0+d1+d2+d3) {
				dm0=d0; dm1=d1;
				dm2=d2; dm3=d3;
				pos = p;
			}
		}
		double d0 = this.points.get(0).distance(point);
		double d1 = this.points.get(this.points.size()-1).distance(point);
		double d2 = this.points.get(0).distance(entPoint);
		double d3 = this.points.get(this.points.size()-1).distance(entPoint);
		if (dm0+dm1+dm2+dm3>d0+d1+d2+d3) { pos = this.points.size()-1; }
		return pos;
	}

	@Override
	public Point[] getClosestPoints(Point point, IPos pos) {
		return this.getClosestPoints(point, pos.getMCBlockPos());
	}
	
	public Point[] getClosestPoints(Point point, BlockPos entityPos) {
		Point[] ps = new Point[2];
		ps[0] = null;
		ps[1] = null;
		int pos = this.getClosestPoint(point, entityPos);
		if (this.points.containsKey(pos)) { ps[0] = this.points.get(pos); }
		if (this.points.containsKey(pos+1)) { ps[1] = this.points.get(pos+1); }
		else if (pos==this.points.size()-1) { ps[1] = this.points.get(0); }
		return ps;
	}

	@Override
	public double distanceTo(double px, double py) {
		BlockPos pos = this.getCenter();
		return AdditionalMethods.distanceTo(pos.getX()+0.5d, 0.0d, pos.getZ()+0.5d, px, 0.0d, py);
	}

	@Override
	public double distanceTo(IEntity<?> entity) {
		return this.distanceTo(entity.getMCEntity());
	}
	
	public double distanceTo(Entity entity) {
		if (entity==null) { return -1; }
		BlockPos c = this.getCenter();
		return AdditionalMethods.distanceTo(entity.posX, entity.posY, entity.posZ, c.getX()+0.5d, c.getY()+0.5d, c.getZ()+0.5d);
	}

	public int getWidthX() { return this.getMaxX() - this.getMinX(); }
	public int getWidthZ() { return this.getMaxZ() - this.getMinZ(); }
	public int getHeight() { return this.y[1] - this.y[0]; }

	@Override
	public INbt getNbt() {
		NBTTagCompound nbtRegion = new NBTTagCompound();
		this.readFromNBT(nbtRegion);
		return NpcAPI.Instance().getINbt(nbtRegion);
	}
	
	public void readFromNBT(NBTTagCompound nbtRegion) {
		this.id = nbtRegion.getInteger("ID");
		this.name = nbtRegion.getString("Name");
		this.dimensionID = nbtRegion.getInteger("DimensionID");
		this.color = nbtRegion.getInteger("Color");
		
		int [] sy = nbtRegion.getIntArray("AxisY");
		this.y[0] = sy[0];
		this.y[1] = sy[1];
		if (this.y[0]<0) { this.y[0] = 0; }
		if (this.y[1]>255) { this.y[1] = 255; }
		this.points = Maps.<Integer, Point>newTreeMap();
		for (int i=0; i<nbtRegion.getTagList("Points", 11).tagCount(); i++) {
			int[] p = nbtRegion.getTagList("Points", 11).getIntArrayAt(i);
			this.points.put(i, new Point(p[0], p[1]));
		}
		this.availability.readFromNBT(nbtRegion.getCompoundTag("Availability"));
		this.message = nbtRegion.getString("Message");
		int[] pos = nbtRegion.getIntArray("HomePos");
		this.setHomePos(pos[0], pos[1], pos[2]);
		this.keepOut = nbtRegion.getBoolean("IsKeepOut");
		this.fix();
	}
	
	@Override
	public void setNbt(INbt nbt) { this.writeToNBT(nbt.getMCNBT()); }
	
	public void writeToNBT(NBTTagCompound nbtRegion) {
		nbtRegion.setInteger("ID", this.id);
		nbtRegion.setString("Name", this.name);
		nbtRegion.setInteger("DimensionID", this.dimensionID);
		nbtRegion.setInteger("Color", this.color);
		this.fix();
		NBTTagList ps = new NBTTagList();
		for (int pos : this.points.keySet()) {
			ps.appendTag(new NBTTagIntArray(new int[] { this.points.get(pos).x, this.points.get(pos).y }));
		}
		nbtRegion.setTag("Points", ps);
		nbtRegion.setIntArray("AxisY", this.y);
		nbtRegion.setTag("Availability", this.availability.writeToNBT(new NBTTagCompound()));
		nbtRegion.setString("Message", this.message);
		
		int[] pos = new int[] { this.homePos.getX(), this.homePos.getY(), this.homePos.getZ() };
		nbtRegion.setIntArray("HomePos", pos);
		nbtRegion.setBoolean("IsKeepOut", this.keepOut);
	}

	@Override
	public Point[] getPoints() {
		return this.points.values().toArray(new Point[this.points.size()]);
	}
	
	public List<Point> getPointList() {
		List<Point> list = Lists.<Point>newArrayList();
		for (Point p : this.points.values()) { list.add(p); }
		return list;
	}

	@Override
	public String toString() { return "ID:"+this.id+"; name: \""+this.name+"\""; }

	public int getIdNearestPoint(BlockPos pos) {
		if (this.points.size()==0 || pos==null) { return -1; }
		double min = Double.MAX_VALUE;
		int id = -1;
		for (int i : this.points.keySet()) {
			double dist = AdditionalMethods.distanceTo(this.points.get(i).x, 0, this.points.get(i).y, pos.getX(), 0, pos.getZ());
			if (dist<=min) {
				min = dist;
				id = i;
			}
		}
		return id;
	}

	public void update(WorldServer worldsworld) {
		if (this.points.size()==0) { return; }
		List<Entity> listEntitiesInside = worldsworld.getEntities(Entity.class, this);
		for (Entity entity : listEntitiesInside) {
			if (this.keepOut) {
				this.kick(entity);
				continue;
			}
			if (!this.entitiesWithinRegion.contains(entity)) { this.entitiesWithinRegion.add(entity); }
		}
		List<Entity> del = Lists.<Entity>newArrayList();
		for (Entity entity : this.entitiesWithinRegion) {
			if (entity.isDead || (!this.keepOut && listEntitiesInside.contains(entity))) {
				if (entity.isDead) { this.entitiesWithinRegion.remove(entity); }
				continue;
			}
			if (entity instanceof EntityPlayer) {
				if (this.availability.isAvailable((EntityPlayer) entity) || ((EntityPlayer) entity).capabilities.isCreativeMode) {
					del.add(entity);
					continue;
				}
			}
			boolean cont = this.contains(entity.posX, entity.posY, entity.posZ, entity.height);
			if (this.keepOut==cont) { this.kick(entity); }
			else { this.playerAntiLag.remove(entity); }
		}
		for (Entity entity : del) {
			this.entitiesWithinRegion.remove(entity);
			this.playerAntiLag.remove(entity);
		}
	}

	private void kick(Entity entity) {
		double[] pos = this.getPlayerTeleportPosition(entity);
		EntityPlayerMP player = null;
		if (entity instanceof EntityPlayerMP) { player = (EntityPlayerMP) entity; }
		else if (entity instanceof EntityEnderPearl && ((EntityEnderPearl) entity).getThrower() instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) ((EntityEnderPearl) entity).getThrower();
			entity.isDead = true;
		}
		if (player==null) { return; }
		if (!this.playerAntiLag.containsKey(player)) { this.playerAntiLag.put(player, new AntiLagTime(player.getPosition())); }
		if (this.playerAntiLag.get(player).isLag(player.getPosition())) {
			pos[0] = this.homePos.getX()+0.5d;
			pos[1] = this.homePos.getY();
			pos[2] = this.homePos.getZ()+0.5d;
			this.playerAntiLag.get(player).clear(player.getPosition());
		}
		player.setPositionAndUpdate(pos[0], pos[1], pos[2]);
		if (this.message.isEmpty()) { return; }
		player.sendStatusMessage(new TextComponentTranslation(this.message), true);
	}

	private double[] getPlayerTeleportPosition(Entity entity) {
		double[] c = this.getExactCenter();
		double[] data = AdditionalMethods.getAngles3D(entity.posX, entity.posY, entity.posZ, c[0], c[1], c[2]);
		double[] p = new double[] { (c[0]-entity.posX), (c[1]-entity.posY), (c[2]-entity.posZ) };
		for (int i=0; i<4; i++) {
			double radiusXZ = data[2]+((double) (i+1) * (this.keepOut ? 0.5d : -0.5d));
			p[0] = Math.sin(data[0]*Math.PI/180.0d) * radiusXZ * -1.0d;
			p[2] = Math.cos(data[0]*Math.PI/180.0d) * radiusXZ;
			if (this.keepOut==this.contains(entity.posX, this.y[0], entity.posZ, 0.0d)) {
				break;
			}
		}
		p[0] = c[0]-p[0];
		p[1] = entity.getPosition().getY();
		p[2] = c[2]-p[2];
		
		boolean rev = false;
		while (p[1]>0 && p[1]<256) {
			if (!rev && p[1] > this.y[1]) {
				p[1] = entity.posY;
				rev = true;
			}
			if (this.keepOut!=this.contains(p[0], p[1], p[2], entity.height)) {
				BlockPos pos = new BlockPos(p[0], p[1], p[2]);
				IBlockState state = entity.world.getBlockState(pos);
				if (state.getBlock().isAir(state, entity.world, pos.up(1)) && state.getBlock().isAir(state, entity.world, pos.up(2))) {
					break;
				}
			}
			p[1] += rev ? -1.0d : 1.0d;
		}
		if (p[1]<=0 || p[1]>=256) { p[1] = entity.posY; }
		return p;
	}

	@Override
	public BlockPos getHomePos() {
		if (this.homePos==null  || this.keepOut!=this.contains(this.homePos.getX()+0.5d, this.homePos.getY()+0.5d, this.homePos.getZ()+0.5d, 0.0d)) {
			this.homePos = this.getCenter();
			if (this.keepOut && this.points.size()>0) {
				for (int i=0; i<4; i++) {
					int x = this.points.get(0).x, z = this.points.get(0).y;
					switch(i) {
						case 1: { x--; break; }
						case 2: { z++; break; }
						case 3: { z--; break; }
						default: { x++; }
					}
					if (!this.contains(x, z)) {
						this.homePos = new BlockPos(x, this.y[0]+(this.y[1]-this.y[0])/2, z);
					}
				}
			}
		}
		return this.homePos;
	}

	@Override
	public void setHomePos(int x, int y, int z) {
		if (this.homePos==null  || this.keepOut!=this.contains(x+0.5d, y, z+0.5d, 0.0d)) { return; }
		this.homePos = new BlockPos(x, y, z);
	}

	@Override
	public boolean apply(Entity entity) {
		if (entity.isDead || (!(entity instanceof EntityPlayerMP) && !(entity instanceof EntityEnderPearl))) {
			return false;
		}
		EntityPlayerMP player = null;
		if (entity instanceof EntityPlayerMP) { player = (EntityPlayerMP) entity; }
		else if (entity instanceof EntityEnderPearl) {
			if (((EntityEnderPearl) entity).getThrower() instanceof EntityPlayerMP) { player = (EntityPlayerMP) ((EntityEnderPearl) entity).getThrower(); }
			else { return false; }
		}
		if (player!=null) {
			if (this.availability.isAvailable(player) || player.capabilities.isCreativeMode) { return false; }
		}
		return this.contains(entity.posX, entity.posY, entity.posZ, entity.height);
	}
	
	@Override
	public boolean contains(int x, int z) {
		for (Point p : this.points.values()) { if (p.x==x && p.y==z) { return true; } }
		return false;
	}
	
	@Override
	public int getId() { return this.id; }

	@Override
	public String getName() { return this.name; }
	
	@Override
	public void setName(String name) {
		if (name==null || name.isEmpty()) { name = "Default Region"; }
		this.name = name;
	}
	
	@Override
	public int getDimensionId() { return this.dimensionID; }
	
	@Override
	public void setDimensionId(int dimID) {
		
		this.dimensionID = dimID;
	}
	
	@Override
	public int getColor() { return this.color; }
	
	@Override
	public void setColor(int color) { this.color = color; }
	
	private class AntiLagTime {
		
		private int count;
		private long time;
		private BlockPos pos;

		public AntiLagTime(BlockPos pos) {
			this.count = 0;
			this.time = System.currentTimeMillis();
			this.pos = pos;
		}

		public void clear(BlockPos pos) {
			this.time = System.currentTimeMillis();
			this.count = 0;
			this.pos = pos;
		}

		public boolean isLag(BlockPos pos) {
			if (!this.pos.equals(pos) || this.time+3000L >= System.currentTimeMillis()) {
				this.clear(pos);
				return false;
			}
			this.count++;
			return this.count>50;
		}
		
	}
}
