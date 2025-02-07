package noppes.npcs.controllers.data;

import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

import com.google.common.base.Predicate;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.util.RayTraceVec;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;

public class Zone3D implements IBorder, Predicate<Entity> {

	private static class AntiLagTime {
		private int count;
		private long time;
		private BlockPos pos;

		public AntiLagTime(BlockPos blockPos) {
			count = 0;
			time = System.currentTimeMillis();
			pos = blockPos;
		}

		public void clear(BlockPos blockPos) {
			time = System.currentTimeMillis();
			count = 0;
			pos = blockPos;
		}

		public boolean isLag(BlockPos blockPos) {
			if (!pos.equals(blockPos) || time + 3000L >= System.currentTimeMillis()) {
				clear(blockPos);
				return false;
			}
			count++;
			return count > 50;
		}
	}
	private int id = -1;
	public String name = "Default Region";
	public final TreeMap<Integer, Point> points = new TreeMap<>();
	public int[] y = new int[] { 0, 255 };
	public int dimensionID = 0;

	public int color;
	public Availability availability;
	public String message; // kick message
	private final List<Entity> entitiesWithinRegion = new ArrayList<>();
	private final Map<Entity, AntiLagTime> playerAntiLag = new HashMap<>();
	public IPos homePos;
	public boolean keepOut;
	public boolean showInClient;
	public NBTTagCompound addData = new NBTTagCompound();

	private boolean update;

	public Zone3D() {
		color = (new Random()).nextInt(0xFFFFFF);
		availability = new Availability();
		message = "availability.areaNotAvailable";
		keepOut = false;
		showInClient = false;
		update = true;
	}

	public Zone3D(int id, int dimID, int posX, int posY, int posZ) {
		this();
		this.id = id;
		dimensionID = dimID;
		y[0] = ValueUtil.correctInt(posY - 1, 0, 255);
		y[1] = ValueUtil.correctInt(posY + 4, 0, 255);
		points.put(0, new Point(posX, posZ - 4));
		points.put(1, new Point(posX + 4, posZ + 2));
		points.put(2, new Point(posX - 4, posZ + 2));
		update = true;
	}

	/**
	 * Adds a new point to the end
	 * 
	 * @param position - block pos
	 */
	public Point addPoint(BlockPos position) {
		return addPoint(position.getX(), position.getY(), position.getZ());
	}

	/**
	 * Adds a new point to the end
	 * 
	 * @param x - x pos
	 * @param y - y pos
	 * @param z - z pos
	 */
	@Override
	public Point addPoint(int x, int y, int z) {
		Point point = new Point();
		point.x = x;
		point.y = z;
		return addPoint(point, y);
	}

	@Override
	public Point addPoint(IPos position) {
		return addPoint(position.getMCBlockPos());
	}

	/**
	 * Adds a new point to the end
	 * 
	 * @param point - pos
	 * @param posY - height
	 */
	@Override
	public Point addPoint(Point point, int posY) {
		for (Point p : points.values()) {
			if (p.x == point.x && p.y == point.y) {
				return null;
			}
		}
		points.put(points.size(), point);
		posY = ValueUtil.correctInt(posY, 0, 255);
		if (posY < y[0]) { y[0] = posY; }
		else if (posY > y[1]) { y[1] = posY; }
		update = true;
		return point;
	}

	@Override
	public boolean apply(Entity entity) {
		if (entity == null || entity.isDead) { return false; }
		return contains(entity.posX, entity.posY, entity.posZ, entity.height);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param position - block pos
	 * @param type = 0 - relative to the zero coordinate; 1 - relative to the center of the described contour; 2 - relative to the center of mass
	 */
	public void centerOffsetTo(BlockPos position, boolean type) {
		centerOffsetTo(position.getX(), position.getY(), position.getZ(), type);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param posX - x pos
	 * @param posY - y pos
	 * @param posZ - z pos
	 * @param type - 0 - relative to the zero coordinate, 1 - relative to the center of
	 *            the described contour;
	 */
	@Override
	public void centerOffsetTo(int posX, int posY, int posZ, boolean type) {
		IPos ctr;
		int ry = (y[1] - y[0]) / 2;
		if (type) {
			ctr = getCenter();
			y[0] = posY - ry;
			y[1] = posY + ry;
		} else {
			ctr = Objects.requireNonNull(NpcAPI.Instance()).getIPos(getMinX(), y[0], getMinZ());
			y[0] = posY;
			y[1] = posY + ry * 2;
		}
		for (int key : points.keySet()) {
			Point p = points.get(key);
			points.get(key).move(posX + p.x - (int) ctr.getX(), posZ + p.y - (int) ctr.getZ());
		}
		update = true;
	}

	@Override
	public void centerOffsetTo(IPos position, boolean type) {
		centerOffsetTo(position.getMCBlockPos(), type);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param point - pos
	 * @param type = 0 - relative to the zero coordinate; 1 - relative to the center of the described contour; 2 - relative to the center of mass
	 */
	@Override
	public void centerOffsetTo(Point point, boolean type) {
		centerOffsetTo(point.x, (y[0] + y[1]) / 2, point.y, type);
	}

	@Override
	public void clear() {
		points.clear();
		availability.clear();
		playerAntiLag.clear();
		entitiesWithinRegion.clear();
		y[0] = 255;
		y[1] = 0;
		message = "availability.areaNotAvailable";
		keepOut = false;
		showInClient = false;
		update = true;
	}

	public boolean contains(Entity entity) {
		return entity.world.provider.getDimension() == dimensionID && contains(entity.posX, entity.posY, entity.posZ, entity.height);
	}

	@Override
	public boolean contains(IEntity<?> entity) {
		return entity.getWorld().getDimension().getId() == dimensionID && contains(entity.getX(), entity.getY(), entity.getZ(), entity.getHeight());
	}

	@Override
	public boolean contains(double posX, double posY, double posZ, double height) {
		if (posY + height < y[0] || posY - height > y[1]) { return false; }
		int dx = (int) (posX * 10.0d);
		int dz = (int) (posZ * 10.0d);
		Polygon poly = new Polygon();
		boolean isIn = false;
		for (Point p : points.values()) {
			int px = 5 + (p.x * 10);
			int py = 5 + (p.y * 10);
			poly.addPoint(px, py);
			isIn = (px == dx && py == dz);
			if (isIn) { break; }
		}
		if (isIn) { return true; }
		isIn = poly.contains(dx, dz);
		return isIn;
	}

	@Override
	public boolean contains(int posX, int posZ) {
		for (Point p : points.values()) {
			if (p.x == posX && p.y == posZ) { return true; }
		}
		return false;
	}

	@Override
	public double distanceTo(double posX, double posZ) {
		IPos pos = getCenter();
		return Util.instance.distanceTo(pos.getX() + 0.5d, 0.0d, pos.getZ() + 0.5d, posX, 0.0d, posZ);
	}

	public double distanceTo(Entity entity) {
		if (entity == null) {
			return -1;
		}
		IPos c = getCenter();
		return Util.instance.distanceTo(entity.posX, entity.posY, entity.posZ, c.getX() + 0.5d,
				c.getY() + 0.5d, c.getZ() + 0.5d);
	}

	@Override
	public double distanceTo(IEntity<?> entity) {
		return distanceTo(entity.getMCEntity());
	}

	public boolean equals(Zone3D zone) {
		if (zone == null) {
			return false;
		}
		if (zone.y[0] != y[0] || zone.y[1] != y[1]) {
			return false;
		}
		if (zone.points.size() != points.size()) {
			return false;
		}
		for (int key : zone.points.keySet()) {
			if (!points.containsKey(key)) {
				return false;
			}
			Point p0 = zone.points.get(key);
			Point p1 = points.get(key);
			if (p0.x != p1.x || p0.y != p1.y) {
				return false;
			}
		}
		return true;
	}

	/**
	 * orders positions
	 */
	public void fix() {
		TreeMap<Integer, Point> newPoints = new TreeMap<>();
		int i = 0;
		boolean needChange = false;
		for (int pos : points.keySet()) {
			newPoints.put(i, points.get(pos));
			if (i != pos) {
				needChange = true;
			}
			i++;
		}
		if (needChange) {
			points.clear();
			points.putAll(newPoints);
		}
		getHomePos();
	}

	@Override
	public IAvailability getAvailability() {
		return availability;
	}

	/**
	 * @return center of mass of the zone
	 */
	@Override
	public IPos getCenter() {
		double x = 0.0d, z = 0.0d;
		for (Point v : points.values()) {
			x += v.x;
			z += v.y;
		}
		if (!points.isEmpty()) {
			x /= points.size();
			z /= points.size();
		}
		return new BlockPosWrapper(x, (double) y[0] + ((double) y[1] - (double) y[0]) / 2.0d, z);
	}

	@Override
	public int getClosestPoint(Point point, IPos pos) {
		if (points.isEmpty()) {
			return -1;
		}
		if (points.size() == 1) {
			return 0;
		}
		int n = 0;
		Point entPoint = new Point((int) pos.getX(), (int) pos.getZ());
		double dm0 = points.get(0).distance(point);
		double dm1 = points.get(1).distance(point);
		double dm2 = points.get(0).distance(entPoint);
		double dm3 = points.get(1).distance(entPoint);
		for (int p = 0; (p + 1) < points.size(); p++) {
			double d0 = points.get(p).distance(point);
			double d1 = points.get(p + 1).distance(point);
			double d2 = points.get(p).distance(entPoint);
			double d3 = points.get(p + 1).distance(entPoint);
			if (dm0 + dm1 + dm2 + dm3 > d0 + d1 + d2 + d3) {
				dm0 = d0;
				dm1 = d1;
				dm2 = d2;
				dm3 = d3;
				n = p;
			}
		}
		double d0 = points.get(0).distance(point);
		double d1 = points.get(points.size() - 1).distance(point);
		double d2 = points.get(0).distance(entPoint);
		double d3 = points.get(points.size() - 1).distance(entPoint);
		if (dm0 + dm1 + dm2 + dm3 > d0 + d1 + d2 + d3) {
			n = points.size() - 1;
		}
		return n;
	}

	@Override
	public Point[] getClosestPoints(Point point, IPos pos) {
		Point[] ps = new Point[2];
		ps[0] = null;
		ps[1] = null;
		int n = getClosestPoint(point, pos);
		if (points.containsKey(n)) {
			ps[0] = points.get(n);
		}
		if (points.containsKey(n + 1)) {
			ps[1] = points.get(n + 1);
		} else if (n == points.size() - 1) {
			ps[1] = points.get(0);
		}
		return ps;
	}

	@Override
	public int getColor() {
		return color;
	}

	@Override
	public int getDimensionId() {
		return dimensionID;
	}

	public int getHeight() {
		return y[1] - y[0];
	}

	@Override
	public IPos getHomePos() {
		if (homePos == null || keepOut != contains(homePos.getX() + 0.5d,
				homePos.getY() + 0.5d, homePos.getZ() + 0.5d, 0.0d)) {
			homePos = getCenter();
			if (keepOut && !points.isEmpty()) {
				for (int i = 0; i < 4; i++) {
					int x = points.get(0).x, z = points.get(0).y;
					switch (i) {
					case 1: {
						x--;
						break;
					}
					case 2: {
						z++;
						break;
					}
					case 3: {
						z--;
						break;
					}
					default: {
						x++;
					}
					}
					if (!contains(x, z)) {
						homePos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, y[0] + (double) (y[1] - y[0]) / 2, z);
					}
				}
			}
		}
		return homePos;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getIdNearestPoint(BlockPos pos) {
		if (points.isEmpty() || pos == null) {
			return -1;
		}
		double min = Double.MAX_VALUE;
		int id = -1;
		for (int i : points.keySet()) {
			double dist = Util.instance.distanceTo(points.get(i).x, 0, points.get(i).y,
					pos.getX(), 0, pos.getZ());
			if (dist <= min) {
				min = dist;
				id = i;
			}
		}
		return id;
	}

	@Override
	public int getMaxX() {
		if (points.isEmpty()) {
			return 0;
		}
		int value = points.get(0).x;
		for (Point v : points.values()) {
			if (value < v.x) {
				value = v.x;
			}
		}
		return value;
	}

	@Override
	public int getMaxY() {
		return Math.max(y[0], y[1]);
	}

	@Override
	public int getMaxZ() {
		if (points.isEmpty()) {
			return 0;
		}
		int value = points.get(0).y;
		for (Point v : points.values()) {
			if (value < v.y) {
				value = v.y;
			}
		}
		return value;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public int getMinX() {
		if (points.isEmpty()) {
			return 0;
		}
		int value = points.get(0).x;
		for (Point v : points.values()) {
			if (value > v.x) {
				value = v.x;
			}
		}
		return value;
	}

	@Override
	public int getMinY() {
		return Math.min(y[0], y[1]);
	}

	@Override
	public int getMinZ() {
		if (points.isEmpty()) {
			return 0;
		}
		int value = points.get(0).y;
		for (Point v : points.values()) {
			if (value > v.y) {
				value = v.y;
			}
		}
		return value;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound nbtRegion = new NBTTagCompound();
		load(nbtRegion);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(nbtRegion);
	}

	@Override
	public Point[] getPoints() {
		return points.values().toArray(new Point[0]);
	}

	/**
	 * @return size X x Y x Z
	 */
	public String getSize() {
		return (getMaxX() - getMinX()) + "x" + getHeight() + "x" + (getMaxZ() - getMinZ());
	}
	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * 
	 * @param posX - x pos
	 * @param posY - y pos
	 * @param posZ - z pos
	 */
	@Override
	public boolean insertPoint(int posX, int posY, int posZ, IPos pos) {
		Point p = new Point();
		p.x = posX;
		p.y = posZ;
		return insertPoint(p, posY, pos);
	}

	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * 
	 * @param pos0 - start block pos
	 * @param pos1 - end block pos
	 */
	@Override
	public boolean insertPoint(IPos pos0, IPos pos1) {
		return insertPoint((int) pos0.getX(), (int) pos0.getY(), (int) pos0.getZ(), pos1);
	}

	@Override
	public boolean insertPoint(Point point, int posY, IPos pos) {
		posY = ValueUtil.correctInt(posY, 0, 255);
		if (posY < y[0]) { y[0] = posY; }
		if (posY > y[1]) { y[1] = posY; }
		if (contains(point.x, point.y)) { return false; }
		if (points.size() < 2) { return addPoint(point, posY) != null; }
		int n = getClosestPoint(point, pos);
		TreeMap<Integer, Point> newPoints = new TreeMap<>();
		for (int i = 0, j = 0; i < points.size(); i++) {
			newPoints.put(i + j, points.get(i));
			if (i == n) {
				j = 1;
				newPoints.put(i + j, point);
			}
		}
		if (newPoints.size() != points.size()) {
			points.clear();
			points.putAll(newPoints);
			update = true;
		}
		return update;
	}

	@Override
	public boolean isShowToPlayers() {
		return showInClient;
	}

	private EntityPlayerMP convertToPlayer(Entity entity) {
		if (entity instanceof EntityPlayerMP) { return (EntityPlayerMP) entity; }
		else if (entity instanceof EntityEnderPearl && ((EntityEnderPearl) entity).getThrower() instanceof EntityPlayerMP) {
			return (EntityPlayerMP) ((EntityEnderPearl) entity).getThrower();
		}
		return null;
	}

	/**
	 * Offsets the entire zone by the specified value
	 * 
	 * @param position - block pos
	 */
	public void offset(BlockPos position) {
		offset(position.getX(), position.getY(), position.getZ());
	}

	/**
	 * (y[1]+y[0])/2 Offsets the entire zone by the specified value
	 * 
	 * @param posX - x pos
	 * @param posY - y pos
	 * @param posZ - z pos
	 */
	@Override
	public void offset(int posX, int posY, int posZ) {
		y[0] = ValueUtil.correctInt(y[0] + posY, 0, 255);
		y[1] = ValueUtil.correctInt(y[1] + posY, 0, 255);
		for (int key : points.keySet()) {
			Point p = points.get(key);
			points.get(key).move(p.x + posX, p.y + posZ);
		}
		update = true;
	}

	@Override
	public void offset(IPos position) {
		offset(position.getMCBlockPos());
	}

	/**
	 * Offsets the entire zone by the specified value
	 * 
	 * @param point - pos
	 */
	@Override
	public void offset(Point point) {
		offset(point.x, 0, point.y);
	}

	public void load(NBTTagCompound nbtRegion) {
		id = nbtRegion.getInteger("ID");
		name = nbtRegion.getString("Name");
		dimensionID = nbtRegion.getInteger("DimensionID");
		color = nbtRegion.getInteger("Color");

		int[] sy = nbtRegion.getIntArray("AxisY");
		if (sy.length > 0) { y[0] = ValueUtil.correctInt(sy[0], 0, 255); }
		if (sy.length > 1) { y[1] = ValueUtil.correctInt(sy[1], 0, 255); }

		points.clear();
		for (int i = 0; i < nbtRegion.getTagList("Points", 11).tagCount(); i++) {
			int[] p = nbtRegion.getTagList("Points", 11).getIntArrayAt(i);
			points.put(i, new Point(p[0], p[1]));
		}
		availability.readFromNBT(nbtRegion.getCompoundTag("Availability"));
		message = nbtRegion.getString("Message");

		if (nbtRegion.hasKey("HomePos", 4)) {
			BlockPos pos = BlockPos.fromLong(nbtRegion.getLong("HomePos"));
			setHomePos(pos.getX(), pos.getY(), pos.getZ());
		} else if (nbtRegion.hasKey("HomePos", 11)) { // old
			int[] pos = nbtRegion.getIntArray("HomePos");
			setHomePos(pos[0], pos[1], pos[2]);
		}
		keepOut = nbtRegion.getBoolean("IsKeepOut");
		showInClient = nbtRegion.getBoolean("ShowInClient");

		addData = nbtRegion.getCompoundTag("AddData");
		fix();
		update = false;
		entitiesWithinRegion.clear();
	}

	/**
	 * Remove point from polygon
	 * 
	 * @param x - x pos
	 * @param z - z pos
	 */
	@Override
	public boolean removePoint(int x, int z) {
		if (points.size() <= 1) { return false; }
		for (int key : points.keySet()) {
			if (points.get(key).x == x && points.get(key).y == z) {
				points.remove(key);
				fix();
				update = true;
				return true;
			}
		}
		return false;
	}

	/**
	 * Remove point from polygon
	 * 
	 * @param point - pos
	 */
	@Override
	public boolean removePoint(Point point) {
		if (point == null || points.size() <= 1) { return false; }
		return removePoint(point.x, point.y);
	}

	/**
	 * Expand or Shrink a zone outline by a specific value
	 * 
	 * @param radius - distance
	 * @param type - type
	 */
	@Override
	public void scaling(double radius, boolean type) {
		if (points.isEmpty()) { return; }
		y[0] = ValueUtil.correctInt(y[0] - (int) radius, 0, 255);
		y[1] = ValueUtil.correctInt(y[0] + (int) radius, 0, 255);
		IPos pos;
		if (type) { pos = getCenter(); }
		else { pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(getMinX(), y[0], getMinZ()); }
		for (int id : points.keySet()) {
			Point v = points.get(id);
			IRayTraceRotate d = Util.instance.getAngles3D(pos.getX(), 0, pos.getZ(), v.x, 0, v.y);
			IRayTraceVec p = Util.instance.getPosition(pos.getX(), 0, pos.getZ(), d.getYaw(), d.getPitch(), radius + d.getRadiusXZ());
			points.put(id, new Point((int) p.getX(), (int) p.getZ()));
		}
		update = true;
	}

	/**
	 * Scale zone outline
	 * 
	 * @param scale - percentage where 100% = 1.0f
	 * @param type - type
	 */
	@Override
	public void scaling(float scale, boolean type) {
		if (points.isEmpty()) { return; }
		IPos pos;
		if (type) { pos = getCenter(); }
		else { pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(getMinX(), y[0], getMinZ()); }
		for (int key : points.keySet()) {
			Point v = points.get(key);
			IRayTraceRotate d = Util.instance.getAngles3D(pos.getX(), pos.getY(), pos.getZ(), v.x, pos.getY(), v.y);
			IRayTraceVec p = Util.instance.getPosition(pos.getX(), pos.getY(), pos.getZ(), d.getYaw(), d.getPitch(), (double) scale * d.getRadiusXZ());
			points.put(key, new Point((int) p.getX(), (int) p.getZ()));
			if (y[0] > (int) p.getY()) { y[0] = ValueUtil.correctInt((int) p.getY(), 0, 255); }
			if (y[1] < (int) p.getY()) { y[1] = ValueUtil.correctInt((int) p.getY(), 0, 255); }
		}
		update = true;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
		update = true;
	}

	@Override
	public void setDimensionId(int dimID) {
		dimensionID = dimID;
		update = true;
	}

	@Override
	public void setHomePos(int x, int y, int z) {
		if (homePos == null || keepOut != contains(x + 0.5d, y, z + 0.5d, 0.0d)) {
			return;
		}
		homePos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, y, z);
		update = true;
	}

	@Override
	public void setMessage(String message) {
		this.message = message == null ? "" : message;
		update = true;
	}

	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = "Default Region";
		}
		this.name = name;
		update = true;
	}

	@Override
	public void setNbt(INbt nbt) {
		save(nbt.getMCNBT());
		update = true;
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param position - new block pos
	 */
	public Point setPoint(int index, BlockPos position) {
		return setPoint(index, position.getX(), position.getY(), position.getZ());
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param x - new x pos
	 * @param y - new y pos
	 * @param z - new z pos
	 */
	@Override
	public Point setPoint(int index, int x, int y, int z) {
		Point point = new Point();
		point.x = x;
		point.y = z;
		update = true;
		return setPoint(index, point, y);
	}

	@Override
	public Point setPoint(int index, IPos position) {
		return setPoint(index, position.getMCBlockPos());
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param point - new pos
	 */
	@Override
	public Point setPoint(int index, Point point) {
		if (!points.containsKey(index) || index > points.size()) { return null; }
		points.put(index, point);
		update = true;
		return point;
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param posY - new height
	 * @param point - new pos
	 */
	@Override
	public Point setPoint(int index, Point point, int posY) {
		if (!points.containsKey(index) || index > points.size()) { return null; }
		points.put(index, point);
		posY = ValueUtil.correctInt(posY, 0, 255);
		if (posY < y[0]) { y[0] = posY; }
		if (posY > y[1]) { y[1] = posY; }
		update = true;
		return point;
	}

	@Override
	public void setShowToPlayers(boolean show) {
		showInClient = show;
		update = true;
	}

	/**
	 * @return number of vertices of a zone
	 */
	@Override
	public int size() {
		return points.size();
	}

	@Override
	public String toString() {
		return "ID:" + id + "; name: \"" + name + "\"";
	}

	@Override
	public void update() {
		update = true;
	}

	public void update(WorldServer world) {
		if (update) {
			entitiesWithinRegion.clear();
			playerAntiLag.clear();
			BorderController.getInstance().update(id);
			update = false;
			return;
		}
		if (points.isEmpty() || dimensionID != world.provider.getDimension()) {
			return;
		}
		List<Entity> entities = world.getEntities(Entity.class, this);
		for (Entity entity : entities) {
			if (!entitiesWithinRegion.contains(entity)) {
				if (tryEntityEnter(entity)) { continue; }
				entitiesWithinRegion.add(entity);
				MinecraftForge.EVENT_BUS.post(new ForgeEvent.EnterToRegion(entity, this));
			}
		}
		List<Entity> del = new ArrayList<>();
		for (Entity entity : entitiesWithinRegion) {
			if (tryEntityLeave(entity, contains(entity))) { del.add(entity); }
		}
		for (Entity entity : del) {
			entitiesWithinRegion.remove(entity);
			playerAntiLag.remove(entity);
			MinecraftForge.EVENT_BUS.post(new ForgeEvent.LeaveRegion(entity, this));
		}
	}

	private boolean tryEntityEnter(Entity entity) {
		EntityPlayerMP player = convertToPlayer(entity);
		if (player == null || availability.isAvailable(player) || player.capabilities.isCreativeMode) { return false; }
		if (!keepOut) { return false; }
		IPos center = getCenter();
		motionPlayer(player, new Vec3d(player.posX, player.posY, player.posZ).subtract(new Vec3d(center.getX(), center.getY(), center.getZ())).normalize());
		if (entity instanceof EntityEnderPearl) { entity.isDead = true; }
		return true;
	}

	private boolean tryEntityLeave(Entity entity, boolean isContains) {
		EntityPlayerMP player = convertToPlayer(entity);
		if (player == null || availability.isAvailable(player) || player.capabilities.isCreativeMode) { return !isContains; }
		if (!keepOut && isContains) { return false; }
		IPos center = getCenter();
		motionPlayer(player, new Vec3d(center.getX(), center.getY(), center.getZ()).subtract(new Vec3d(player.posX, player.posY, player.posZ)).normalize());
		if (entity instanceof EntityEnderPearl) { entity.isDead = true; }
		return false;
	}

	private void motionPlayer(EntityPlayerMP player, Vec3d vec) {
		double corrector = 0.25d;
		player.motionX = vec.x * corrector;
		player.motionY = vec.y * corrector;
		player.motionZ = vec.z * corrector;
		player.velocityChanged = true;
		if (!message.isEmpty()) { player.sendStatusMessage(new TextComponentTranslation(message), true); }
		// if the player is stuck
		if (!playerAntiLag.containsKey(player)) { playerAntiLag.put(player, new AntiLagTime(keepOut ? homePos.getMCBlockPos() : player.getPosition())); }
		if (playerAntiLag.get(player).isLag(player.getPosition())) {
			playerAntiLag.get(player).clear(player.getPosition());
			player.setPositionAndUpdate(homePos.getX() + 0.5d, homePos.getY(), homePos.getZ() + 0.5d);
		}
	}

	public void save(NBTTagCompound nbtRegion) {
		nbtRegion.setInteger("ID", id);
		nbtRegion.setString("Name", name);
		nbtRegion.setInteger("DimensionID", dimensionID);
		nbtRegion.setInteger("Color", color);
		NBTTagList ps = new NBTTagList();
		for (int pos : points.keySet()) {
			ps.appendTag(new NBTTagIntArray(new int[] { points.get(pos).x, points.get(pos).y }));
		}
		nbtRegion.setTag("Points", ps);
		nbtRegion.setIntArray("AxisY", y);
		nbtRegion.setTag("Availability", availability.writeToNBT(new NBTTagCompound()));
		nbtRegion.setString("Message", message);

		nbtRegion.setLong("HomePos", homePos.getMCBlockPos().toLong());
		nbtRegion.setBoolean("IsKeepOut", keepOut);
		nbtRegion.setBoolean("ShowInClient", showInClient);
		nbtRegion.setTag("AddData", addData);

		fix();
	}

	public AxisAlignedBB getAxisAlignedBB(boolean isFlat) {
		return new AxisAlignedBB(
				(5.0d + getMinX() * 10.0d) / 10.0d,
				isFlat ? 0.0d : (5.0d + getMinY() * 10.0d) / 10.0d,
				(5.0d + getMinZ() * 10.0d) / 10.0d,
				(5.0d + getMaxX() * 10.0d) / 10.0d,
				isFlat ? 1.0d : (5.0d + getMaxY() * 10.0d) / 10.0d,
				(5.0d + getMaxZ() * 10.0d) / 10.0d);
	}

	@Override
	public Vec3d intersectsWithLine(Vec3d startPos, Vec3d endPos) {
		// create vertices
		double baseY = getMinY();
		double height = getMaxY() - baseY;
		List<Vec3d> vertices = new ArrayList<>();
		for (Point point : points.values()) { vertices.add(new Vec3d(point.x, baseY, point.y)); }

		// Create the top and bottom base of the prism
		List<Vec3d> topVertices = createOffsetVertices(vertices, height);

		// We check the intersection of the ray with the upper and lower faces
		Vec3d topResult = checkIntersection(startPos, endPos, topVertices, baseY);
		Vec3d bottomResult = checkIntersection(startPos, endPos, vertices, baseY);

		// Check intersection with vertical sides
		Vec3d wallResult = null;
		for (int i = 0; i < vertices.size(); i++) {
			Vec3d v1 = vertices.get(i);
			Vec3d v2 = vertices.get((i + 1) % vertices.size());
			Vec3d topV1 = new Vec3d(v1.x, v1.y + height, v1.z);
			Vec3d topV2 = new Vec3d(v2.x, v2.y + height, v2.z);
			wallResult = checkSegmentIntersection(startPos, endPos, v1, v2);
			if (wallResult != null) { break; }
			wallResult = checkSegmentIntersection(startPos, endPos, topV1, topV2);
			if (wallResult != null) { break; }
		}
		if (wallResult != null) {
			if (topResult == null && bottomResult == null) { return wallResult; }
			double topDist = Double.MAX_VALUE;
			double bottomDist = Double.MAX_VALUE;
			double wallDist = startPos.distanceTo(wallResult);
			if (topResult != null) { topDist = startPos.distanceTo(topResult); }
			if (bottomResult != null) { bottomDist = startPos.distanceTo(bottomResult); }
			if (topDist < wallDist) {
				return topDist < bottomDist ? topResult : bottomResult;
			}
			return bottomDist < wallDist ? bottomResult : wallResult;
		}
		if (topResult == null) { return bottomResult; }
		return topResult;
    }

	private List<Vec3d> createOffsetVertices(List<Vec3d> originalVertices, double offset) {
		List<Vec3d> offsetVertices = new ArrayList<>();
		for (Vec3d vertex : originalVertices) { offsetVertices.add(new Vec3d(vertex.x, vertex.y + offset, vertex.z)); }
		return offsetVertices;
	}

	private static Vec3d checkIntersection(Vec3d start, Vec3d end, List<Vec3d> polygon, double baseY) {
		// Ray-Polygon Intersection Check Algorithm
		// Use Mo's algorithm to determine ray-polygon intersection
		int crossings = 0;
		for (int i = 0, j = polygon.size() - 1; i < polygon.size(); j = i++) {
			Vec3d edgeStart = polygon.get(i);
			Vec3d edgeEnd = polygon.get(j);
			if ((edgeStart.y > start.y) != (edgeEnd.y > start.y) && start.x < (edgeEnd.x - edgeStart.x) * (start.y - edgeStart.y) / (edgeEnd.y - edgeStart.y) + edgeStart.x) {
				crossings++;
			}
		}
		return (crossings % 2 == 1) ? new Vec3d((end.x - start.x) / (end.y - start.y) * (baseY - start.y) + start.x, baseY, (end.z - start.z) / (end.y - start.y) * (baseY - start.y) + start.z) : null;
	}

	private Vec3d checkSegmentIntersection(Vec3d startPos, Vec3d endPos, Vec3d segmentStart, Vec3d segmentEnd) {
		//Algorithm for checking the intersection of two segments
		double denominator = (segmentEnd.z - segmentStart.z) * (endPos.x - startPos.x) - (segmentEnd.x - segmentStart.x) * (endPos.z - startPos.z);
		if (denominator == 0) { return null; } // Parallel lines
		double ua = ((segmentEnd.x - segmentStart.x) * (startPos.z - segmentStart.z) - (segmentEnd.z - segmentStart.z) * (startPos.x - segmentStart.x)) / denominator;
		double ub = ((endPos.x - startPos.x) * (startPos.z - segmentStart.z) - (endPos.z - startPos.z) * (startPos.x - segmentStart.x)) / denominator;
		if (ua >= 0 && ua <= 1 && ub >= 0 && ub <= 1) { // Calculate the intersection point
			double x = startPos.x + ua * (endPos.x - startPos.x);
			double y = startPos.y + ua * (endPos.y - startPos.y);
			double z = startPos.z + ua * (endPos.z - startPos.z);
			return new Vec3d(x, y, z);
		}
		return null;
    }

}
