package noppes.npcs.controllers.data;

import java.awt.Point;
import java.awt.Polygon;
import java.util.*;

import com.google.common.base.Predicate;

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
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.api.handler.data.IBorder;
import noppes.npcs.api.util.IRayTraceRotate;
import noppes.npcs.api.util.IRayTraceVec;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.util.Util;

public class Zone3D implements IBorder, Predicate<Entity> {

	private static class AntiLagTime {
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
			if (!this.pos.equals(pos) || this.time + 3000L >= System.currentTimeMillis()) {
				this.clear(pos);
				return false;
			}
			this.count++;
			return this.count > 50;
		}
	}
	private int id = -1;
	public String name = "Default Region";
	public TreeMap<Integer, Point> points = new TreeMap<>();
	public int[] y = new int[] { 0, 255 };
	public int dimensionID = 0;

	public int color;
	public Availability availability;
	public String message;
	private final List<Entity> entitiesWithinRegion = new ArrayList<>();
	private final Map<Entity, AntiLagTime> playerAntiLag = new HashMap<>();
	public IPos homePos;
	public boolean keepOut, showInClient;

	private boolean update;

	public Zone3D() {
		this.color = (new Random()).nextInt(0xFFFFFF);
		this.availability = new Availability();
		this.message = "availability.areaNotAvailable";
		this.keepOut = false;
		this.showInClient = false;
		this.update = true;
	}

	public Zone3D(int id, int dimensionID, int x, int y, int z) {
		this();
		this.id = id;
		this.dimensionID = dimensionID;
		this.y[0] = y - 1;
		this.y[1] = y + 4;
		if (this.y[0] < 0) {
			this.y[0] = 0;
		}
		if (this.y[1] > 255) {
			this.y[1] = 255;
		}
		points.put(0, new Point(x, z - 4));
		points.put(1, new Point(x + 4, z + 2));
		points.put(2, new Point(x - 4, z + 2));
		update = true;
	}

	/**
	 * Adds a new point to the end
	 * 
	 * @param position - block pos
	 */
	public Point addPoint(BlockPos position) {
		return this.addPoint(position.getX(), position.getY(), position.getZ());
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
		return this.addPoint(point, y);
	}

	@Override
	public Point addPoint(IPos position) {
		return this.addPoint(position.getMCBlockPos());
	}

	/**
	 * Adds a new point to the end
	 * 
	 * @param point - pos
	 * @param y - height
	 */
	@Override
	public Point addPoint(Point point, int y) {
		for (Point p : this.points.values()) {
			if (p.x == point.x && p.y == point.y) {
				return null;
			}
		}
		this.points.put(this.points.size(), point);
		if (y < 0) {
			y = 0;
		} else if (y > 255) {
			y = 255;
		}
		if (y < this.y[0]) {
			this.y[0] = y;
		} else if (y > this.y[1]) {
			this.y[1] = y;
		}
		this.update = true;
		return point;
	}

	@Override
	public boolean apply(Entity entity) {
		if (entity == null || entity.isDead || (!(entity instanceof EntityPlayerMP) && !(entity instanceof EntityEnderPearl))) {
			return false;
		}
		EntityPlayerMP player = entity instanceof EntityPlayerMP ? (EntityPlayerMP) entity : null;
		if (entity instanceof EntityEnderPearl) {
			if (((EntityEnderPearl) entity).getThrower() instanceof EntityPlayerMP) {
				player = (EntityPlayerMP) ((EntityEnderPearl) entity).getThrower();
			} else {
				return false;
			}
		}
		if (player != null) {
			if (this.availability.isAvailable(player) || player.capabilities.isCreativeMode) {
				return false;
			}
		}
		return this.contains(entity.posX, entity.posY, entity.posZ, entity.height);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param position - block pos
	 * @param type = 0 - relative to the zero coordinate; 1 - relative to the center of the described contour; 2 - relative to the center of mass
	 */
	public void centerOffsetTo(BlockPos position, boolean type) {
		this.centerOffsetTo(position.getX(), position.getY(), position.getZ(), type);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param x - x pos
	 * @param y - y pos
	 * @param z - z pos
	 * @param type - 0 - relative to the zero coordinate, 1 - relative to the center of
	 *            the described contour;
	 */
	@Override
	public void centerOffsetTo(int x, int y, int z, boolean type) {
		IPos ctr;
		int ry = (this.y[1] - this.y[0]) / 2;
		if (type) {
			ctr = this.getCenter();
			this.y[0] = y - ry;
			this.y[1] = y + ry;
		} else {
			ctr = Objects.requireNonNull(NpcAPI.Instance()).getIPos(this.getMinX(), this.y[0], this.getMinZ());
			this.y[0] = y;
			this.y[1] = y + ry * 2;
		}
		for (int key : this.points.keySet()) {
			Point p = this.points.get(key);
			this.points.get(key).move(x + p.x - (int) ctr.getX(), z + p.y - (int) ctr.getZ());
		}
		this.update = true;
	}

	@Override
	public void centerOffsetTo(IPos position, boolean type) {
		this.centerOffsetTo(position.getMCBlockPos(), type);
	}

	/**
	 * Offsets the position of the zone
	 * 
	 * @param point - pos
	 * @param type = 0 - relative to the zero coordinate; 1 - relative to the center of the described contour; 2 - relative to the center of mass
	 */
	@Override
	public void centerOffsetTo(Point point, boolean type) {
		this.centerOffsetTo(point.x, (this.y[0] + this.y[1]) / 2, point.y, type);
	}

	@Override
	public void clear() {
		this.points.clear();
		this.availability.clear();
		this.playerAntiLag.clear();
		this.entitiesWithinRegion.clear();
		this.y[0] = 255;
		this.y[1] = 0;
		this.message = "availability.areaNotAvailable";
		this.keepOut = false;
		this.showInClient = false;
		this.update = true;
	}

	@Override
	public boolean contains(IEntity<?> entity) {
		return entity.getWorld().getDimension().getId() == dimensionID && contains(entity.getX(), entity.getY(), entity.getZ(), entity.getHeight());
	}

	@Override
	public boolean contains(double x, double y, double z, double height) {
		int dx = (int) (x * 10.0d);
		int dz = (int) (z * 10.0d);
		if (y + height < this.y[0] || y - height > this.y[1]) {
			return false;
		}
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
	public boolean contains(int x, int z) {
		for (Point p : this.points.values()) {
			if (p.x == x && p.y == z) {
				return true;
			}
		}
		return false;
	}

	@Override
	public double distanceTo(double x, double z) {
		IPos pos = this.getCenter();
		return Util.instance.distanceTo(pos.getX() + 0.5d, 0.0d, pos.getZ() + 0.5d, x, 0.0d, z);
	}

	public double distanceTo(Entity entity) {
		if (entity == null) {
			return -1;
		}
		IPos c = this.getCenter();
		return Util.instance.distanceTo(entity.posX, entity.posY, entity.posZ, c.getX() + 0.5d,
				c.getY() + 0.5d, c.getZ() + 0.5d);
	}

	@Override
	public double distanceTo(IEntity<?> entity) {
		return this.distanceTo(entity.getMCEntity());
	}

	public boolean equals(Zone3D zone) {
		if (zone == null) {
			return false;
		}
		if (zone.y[0] != this.y[0] || zone.y[1] != this.y[1]) {
			return false;
		}
		if (zone.points.size() != this.points.size()) {
			return false;
		}
		for (int key : zone.points.keySet()) {
			if (!this.points.containsKey(key)) {
				return false;
			}
			Point p0 = zone.points.get(key);
			Point p1 = this.points.get(key);
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
		if (this.points == null) {
			this.points = new TreeMap<>();
		}
		TreeMap<Integer, Point> newList = new TreeMap<>();
		int i = 0;
		boolean needChange = false;
		for (int pos : this.points.keySet()) {
			newList.put(i, this.points.get(pos));
			if (i != pos) {
				needChange = true;
			}
			i++;
		}
		if (needChange) {
			this.points = newList;
		}
		this.getHomePos();
	}

	@Override
	public IAvailability getAvailability() {
		return this.availability;
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
		if (this.points.isEmpty()) {
			return -1;
		}
		if (this.points.size() == 1) {
			return 0;
		}
		int n = 0;
		Point entPoint = new Point((int) pos.getX(), (int) pos.getZ());
		double dm0 = this.points.get(0).distance(point);
		double dm1 = this.points.get(1).distance(point);
		double dm2 = this.points.get(0).distance(entPoint);
		double dm3 = this.points.get(1).distance(entPoint);
		for (int p = 0; (p + 1) < this.points.size(); p++) {
			double d0 = this.points.get(p).distance(point);
			double d1 = this.points.get(p + 1).distance(point);
			double d2 = this.points.get(p).distance(entPoint);
			double d3 = this.points.get(p + 1).distance(entPoint);
			if (dm0 + dm1 + dm2 + dm3 > d0 + d1 + d2 + d3) {
				dm0 = d0;
				dm1 = d1;
				dm2 = d2;
				dm3 = d3;
				n = p;
			}
		}
		double d0 = this.points.get(0).distance(point);
		double d1 = this.points.get(this.points.size() - 1).distance(point);
		double d2 = this.points.get(0).distance(entPoint);
		double d3 = this.points.get(this.points.size() - 1).distance(entPoint);
		if (dm0 + dm1 + dm2 + dm3 > d0 + d1 + d2 + d3) {
			n = this.points.size() - 1;
		}
		return n;
	}

	@Override
	public Point[] getClosestPoints(Point point, IPos pos) {
		Point[] ps = new Point[2];
		ps[0] = null;
		ps[1] = null;
		int n = this.getClosestPoint(point, pos);
		if (this.points.containsKey(n)) {
			ps[0] = this.points.get(n);
		}
		if (this.points.containsKey(n + 1)) {
			ps[1] = this.points.get(n + 1);
		} else if (n == this.points.size() - 1) {
			ps[1] = this.points.get(0);
		}
		return ps;
	}

	@Override
	public int getColor() {
		return this.color;
	}

	@Override
	public int getDimensionId() {
		return this.dimensionID;
	}

	public int getHeight() {
		return this.y[1] - this.y[0];
	}

	@Override
	public IPos getHomePos() {
		if (this.homePos == null || this.keepOut != this.contains(this.homePos.getX() + 0.5d,
				this.homePos.getY() + 0.5d, this.homePos.getZ() + 0.5d, 0.0d)) {
			this.homePos = this.getCenter();
			if (this.keepOut && !this.points.isEmpty()) {
				for (int i = 0; i < 4; i++) {
					int x = this.points.get(0).x, z = this.points.get(0).y;
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
					if (!this.contains(x, z)) {
						this.homePos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, this.y[0] + (double) (this.y[1] - this.y[0]) / 2, z);
					}
				}
			}
		}
		return this.homePos;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public int getIdNearestPoint(BlockPos pos) {
		if (this.points.isEmpty() || pos == null) {
			return -1;
		}
		double min = Double.MAX_VALUE;
		int id = -1;
		for (int i : this.points.keySet()) {
			double dist = Util.instance.distanceTo(this.points.get(i).x, 0, this.points.get(i).y,
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
		if (this.points.isEmpty()) {
			return 0;
		}
		int value = this.points.get(0).x;
		for (Point v : this.points.values()) {
			if (value < v.x) {
				value = v.x;
			}
		}
		return value;
	}

	@Override
	public int getMaxY() {
		return Math.max(this.y[0], this.y[1]);
	}

	@Override
	public int getMaxZ() {
		if (this.points.isEmpty()) {
			return 0;
		}
		int value = this.points.get(0).y;
		for (Point v : this.points.values()) {
			if (value < v.y) {
				value = v.y;
			}
		}
		return value;
	}

	@Override
	public String getMessage() {
		return this.message;
	}

	@Override
	public int getMinX() {
		if (this.points.isEmpty()) {
			return 0;
		}
		int value = this.points.get(0).x;
		for (Point v : this.points.values()) {
			if (value > v.x) {
				value = v.x;
			}
		}
		return value;
	}

	@Override
	public int getMinY() {
		return Math.min(this.y[0], this.y[1]);
	}

	@Override
	public int getMinZ() {
		if (this.points.isEmpty()) {
			return 0;
		}
		int value = this.points.get(0).y;
		for (Point v : this.points.values()) {
			if (value > v.y) {
				value = v.y;
			}
		}
		return value;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public INbt getNbt() {
		NBTTagCompound nbtRegion = new NBTTagCompound();
		this.readFromNBT(nbtRegion);
		return Objects.requireNonNull(NpcAPI.Instance()).getINbt(nbtRegion);
	}

	private double[] getPlayerTeleportPosition(Entity entity) {
		double x = 0.0d, z = 0.0d;
		for (Point v : this.points.values()) {
			x += v.x;
			z += v.y;
		}
		if (!this.points.isEmpty()) {
			x /= this.points.size();
			z /= this.points.size();
		}
		double y = ((double) this.y[1] - (double) this.y[0]) / 2.0d;

		IRayTraceRotate d = Util.instance.getAngles3D(entity.posX, entity.posY, entity.posZ, x, y, z);
		double[] p = new double[] { (x - entity.posX), (y - entity.posY), (z - entity.posZ) };
		for (int i = 0; i < 4; i++) {
			double radiusXZ = d.getRadiusXZ() + ((double) (i + 1) * (this.keepOut ? 0.5d : -0.5d));
			p[0] = Math.sin(d.getYaw() * Math.PI / 180.0d) * radiusXZ * -1.0d;
			p[2] = Math.cos(d.getYaw() * Math.PI / 180.0d) * radiusXZ;
			if (this.keepOut == this.contains(entity.posX, this.y[0], entity.posZ, 0.0d)) {
				break;
			}
		}
		p[0] = x - p[0];
		p[1] = entity.getPosition().getY();
		p[2] = z - p[2];

		boolean rev = false;
		while (p[1] > 0 && p[1] < 256) {
			if (!rev && p[1] > this.y[1]) {
				p[1] = entity.posY;
				rev = true;
			}
			if (this.keepOut != this.contains(p[0], p[1], p[2], entity.height)) {
				BlockPos pos = new BlockPos(p[0], p[1], p[2]);
				IBlockState state = entity.world.getBlockState(pos);
				if (state.getBlock().isAir(state, entity.world, pos.up(1))
						&& state.getBlock().isAir(state, entity.world, pos.up(2))) {
					break;
				}
			}
			p[1] += rev ? -1.0d : 1.0d;
		}
		if (p[1] <= 0 || p[1] >= 256) {
			p[1] = entity.posY;
		}
		return p;
	}

	@Override
	public Point[] getPoints() {
		return this.points.values().toArray(new Point[0]);
	}

	/**
	 * @return size X x Y x Z
	 */
	public String getSize() {
		int x = this.getMaxX() - this.getMinX();
		if (x < 0) {
			x *= -1;
		}
		int y = this.y[0] - this.y[1];
		if (y < 0) {
			y *= -1;
		}
		int z = this.getMaxZ() - this.getMinZ();
		if (z < 0) {
			z *= -1;
		}
		return x + "x" + y + "x" + z;
	}
	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * 
	 * @param x - x pos
	 * @param y - y pos
	 * @param z - z pos
	 */
	@Override
	public boolean insertPoint(int x, int y, int z, IPos pos) {
		Point p = new Point();
		p.x = x;
		p.y = z;
		return this.insertPoint(p, y, pos);
	}

	/**
	 * Adds a new point between two existing ones (through the smallest lengths)
	 * 
	 * @param pos0 - start block pos
	 * @param pos1 - end block pos
	 */
	@Override
	public boolean insertPoint(IPos pos0, IPos pos1) {
		return this.insertPoint((int) pos0.getX(), (int) pos0.getY(), (int) pos0.getZ(), pos1);
	}

	@Override
	public boolean insertPoint(Point point, int y, IPos pos) {
		if (y >= 0) {
			int min = Math.abs(this.y[0] - y);
			int max = Math.abs(this.y[1] - y);
			if (min <= max) {
				this.y[0] = y;
			} else {
				this.y[1] = y;
			}
		}
		if (this.contains(point.x, point.y)) {
			return false;
		}
		if (this.points.size() < 2) {
			return this.addPoint(point, y) != null;
		}
		int n = this.getClosestPoint(point, pos);
		TreeMap<Integer, Point> temp = new TreeMap<>();
		for (int i = 0, j = 0; i < this.points.size(); i++) {
			temp.put(i + j, this.points.get(i));
			if (i == n) {
				j = 1;
				temp.put(i + j, point);
			}
		}
		this.points = temp;
		this.update = true;
		return true;
	}

	@Override
	public boolean isShowToPlayers() {
		return this.showInClient;
	}

	private void kick(Entity entity) {
		double[] pos = this.getPlayerTeleportPosition(entity);
		EntityPlayerMP player = null;
		if (entity instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) entity;
		} else if (entity instanceof EntityEnderPearl
				&& ((EntityEnderPearl) entity).getThrower() instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) ((EntityEnderPearl) entity).getThrower();
			entity.isDead = true;
		}
		if (player == null) {
			return;
		}
		if (!this.playerAntiLag.containsKey(player)) {
			this.playerAntiLag.put(player, new AntiLagTime(player.getPosition()));
		}
		if (this.playerAntiLag.get(player).isLag(player.getPosition())) {
			pos[0] = this.homePos.getX() + 0.5d;
			pos[1] = this.homePos.getY();
			pos[2] = this.homePos.getZ() + 0.5d;
			this.playerAntiLag.get(player).clear(player.getPosition());
		}
		player.setPositionAndUpdate(pos[0], pos[1], pos[2]);
		if (this.message.isEmpty()) {
			return;
		}
		player.sendStatusMessage(new TextComponentTranslation(this.message), true);
	}

	/**
	 * Offsets the entire zone by the specified value
	 * 
	 * @param position - block pos
	 */
	public void offset(BlockPos position) {
		this.offset(position.getX(), position.getY(), position.getZ());
	}

	/**
	 * (this.y[1]+this.y[0])/2 Offsets the entire zone by the specified value
	 * 
	 * @param x - x pos
	 * @param y - y pos
	 * @param z - z pos
	 */
	@Override
	public void offset(int x, int y, int z) {
		this.y[0] += y;
		this.y[1] += y;
		for (int key : this.points.keySet()) {
			Point p = this.points.get(key);
			this.points.get(key).move(p.x + x, p.y + z);
		}
		this.update = true;
	}

	@Override
	public void offset(IPos position) {
		this.offset(position.getMCBlockPos());
	}

	/**
	 * Offsets the entire zone by the specified value
	 * 
	 * @param point - pos
	 */
	@Override
	public void offset(Point point) {
		this.offset(point.x, 0, point.y);
	}

	public void readFromNBT(NBTTagCompound nbtRegion) {
		this.id = nbtRegion.getInteger("ID");
		this.name = nbtRegion.getString("Name");
		this.dimensionID = nbtRegion.getInteger("DimensionID");
		this.color = nbtRegion.getInteger("Color");

		int[] sy = nbtRegion.getIntArray("AxisY");
		this.y[0] = sy[0];
		this.y[1] = sy[1];
		if (this.y[0] < 0) {
			this.y[0] = 0;
		}
		if (this.y[1] > 255) {
			this.y[1] = 255;
		}
		this.points = new TreeMap<>();
		for (int i = 0; i < nbtRegion.getTagList("Points", 11).tagCount(); i++) {
			int[] p = nbtRegion.getTagList("Points", 11).getIntArrayAt(i);
			this.points.put(i, new Point(p[0], p[1]));
		}
		this.availability.readFromNBT(nbtRegion.getCompoundTag("Availability"));
		this.message = nbtRegion.getString("Message");

		if (nbtRegion.hasKey("HomePos", 4)) {
			BlockPos pos = BlockPos.fromLong(nbtRegion.getLong("HomePos"));
			this.setHomePos(pos.getX(), pos.getY(), pos.getZ());
		} else if (nbtRegion.hasKey("HomePos", 11)) { // old
			int[] pos = nbtRegion.getIntArray("HomePos");
			this.setHomePos(pos[0], pos[1], pos[2]);
		}
		this.keepOut = nbtRegion.getBoolean("IsKeepOut");
		this.showInClient = nbtRegion.getBoolean("ShowInClient");
		this.fix();
		this.update = false;
	}

	/**
	 * Remove point from polygon
	 * 
	 * @param x - x pos
	 * @param z - z pos
	 */
	@Override
	public boolean removePoint(int x, int z) {
		if (this.points == null || this.points.isEmpty()) {
			return false;
		}
		for (int key : this.points.keySet()) {
			if (this.points.get(key).x == x && this.points.get(key).y == z) {
				this.points.remove(key);
				this.fix();
				this.update = true;
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
		if (point == null || this.points == null || this.points.size() <= 3) {
			return false;
		}
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
		if (this.points.size() <= 1) {
			return;
		}
		this.y[0] -= (int) radius;
		this.y[1] += (int) radius;
		IPos pos;
		if (type) {
			pos = this.getCenter();
		} else {
			pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(this.getMinX(), this.y[0], this.getMinZ());
		}
		for (int key : this.points.keySet()) {
			Point v = this.points.get(key);
			IRayTraceRotate d = Util.instance.getAngles3D(pos.getX(), 0, pos.getZ(), v.x, 0, v.y);
			IRayTraceVec p = Util.instance.getPosition(pos.getX(), 0, pos.getZ(), d.getYaw(), d.getPitch(), radius + d.getRadiusXZ());
			this.points.put(key, new Point((int) p.getX(), (int) p.getZ()));
		}
		this.update = true;
	}

	/**
	 * Scale zone outline
	 * 
	 * @param scale - percentage where 100% = 1.0f
	 * @param type - type
	 */
	@Override
	public void scaling(float scale, boolean type) {
		if (this.points.size() <= 1) {
			return;
		}
		IPos pos;
		if (type) {
			pos = this.getCenter();
		} else {
			pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(this.getMinX(), this.y[0], this.getMinZ());
		}
		for (int key : this.points.keySet()) {
			Point v = this.points.get(key);
			IRayTraceRotate d = Util.instance.getAngles3D(pos.getX(), pos.getY(), pos.getZ(), v.x, pos.getY(), v.y);
			IRayTraceVec p = Util.instance.getPosition(pos.getX(), pos.getY(), pos.getZ(), d.getYaw(), d.getPitch(), (double) scale * d.getRadiusXZ());
			this.points.put(key, new Point((int) p.getX(), (int) p.getZ()));
			if (this.y[0] > (int) p.getY()) {
				this.y[0] = (int) p.getY();
			}
			if (this.y[1] < (int) p.getY()) {
				this.y[1] = (int) p.getY();
			}
		}
		this.update = true;
	}

	@Override
	public void setColor(int color) {
		this.color = color;
		this.update = true;
	}

	@Override
	public void setDimensionId(int dimID) {
		this.dimensionID = dimID;
		this.update = true;
	}

	@Override
	public void setHomePos(int x, int y, int z) {
		if (this.homePos == null || this.keepOut != this.contains(x + 0.5d, y, z + 0.5d, 0.0d)) {
			return;
		}
		this.homePos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(x, y, z);
		this.update = true;
	}

	@Override
	public void setMessage(String message) {
		this.message = message;
		this.update = true;
	}

	@Override
	public void setName(String name) {
		if (name == null || name.isEmpty()) {
			name = "Default Region";
		}
		this.name = name;
		this.update = true;
	}

	@Override
	public void setNbt(INbt nbt) {
		this.writeToNBT(nbt.getMCNBT());
		this.update = true;
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param position - new block pos
	 */
	public Point setPoint(int index, BlockPos position) {
		return this.setPoint(index, position.getX(), position.getY(), position.getZ());
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
		this.update = true;
		return this.setPoint(index, point, y);
	}

	@Override
	public Point setPoint(int index, IPos position) {
		return this.setPoint(index, position.getMCBlockPos());
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param point - new pos
	 */
	@Override
	public Point setPoint(int index, Point point) {
		if (!this.points.containsKey(index) && index != this.points.size()) {
			return null;
		}
		this.points.put(index, point);
		this.update = true;
		return point;
	}

	/**
	 * Sets a point instead of an existing one
	 * 
	 * @param index - index
	 * @param y - new height
	 * @param point - new pos
	 */
	@Override
	public Point setPoint(int index, Point point, int y) {
		if (!this.points.containsKey(index) && index != this.points.size()) {
			return null;
		}
		this.points.put(index, point);
		if (y >= 0) {
			if (y < this.y[0]) {
				this.y[0] = y;
			}
			if (y > this.y[1]) {
				this.y[1] = y;
			}
		}
		this.update = true;
		return point;
	}

	@Override
	public void setShowToPlayers(boolean show) {
		this.showInClient = show;
		this.update = true;
	}

	/**
	 * @return number of vertices of a zone
	 */
	@Override
	public int size() {
		return this.points.size();
	}

	@Override
	public String toString() {
		return "ID:" + this.id + "; name: \"" + this.name + "\"";
	}

	@Override
	public void update() {
		this.update = true;
	}

	public void update(WorldServer world) {
		if (this.update) {
			this.entitiesWithinRegion.clear();
			this.playerAntiLag.clear();
			BorderController.getInstance().update(this.id);
			this.update = false;
			return;
		}
		if (this.points.isEmpty() || this.dimensionID != world.provider.getDimension()) {
			return;
		}
		List<Entity> listEntitiesInside = world.getEntities(Entity.class, this);
		for (Entity entity : listEntitiesInside) {
			if (this.keepOut) {
				this.kick(entity);
				continue;
			}
			if (!this.entitiesWithinRegion.contains(entity)) {
				this.entitiesWithinRegion.add(entity);
			}
		}
		List<Entity> del = new ArrayList<>();
		for (Entity entity : this.entitiesWithinRegion) {
			if (entity.isDead || (!this.keepOut && listEntitiesInside.contains(entity))) {
				if (entity.isDead) {
					this.entitiesWithinRegion.remove(entity);
				}
				continue;
			}
			if (entity instanceof EntityPlayer) {
				if (this.availability.isAvailable((EntityPlayer) entity)
						|| ((EntityPlayer) entity).capabilities.isCreativeMode) {
					del.add(entity);
					continue;
				}
			}
			boolean cont = this.contains(entity.posX, entity.posY, entity.posZ, entity.height);
			if (this.keepOut == cont) {
				this.kick(entity);
			} else {
				this.playerAntiLag.remove(entity);
			}
		}
		for (Entity entity : del) {
			this.entitiesWithinRegion.remove(entity);
			this.playerAntiLag.remove(entity);
		}
	}

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

		nbtRegion.setLong("HomePos", homePos.getMCBlockPos().toLong());
		nbtRegion.setBoolean("IsKeepOut", this.keepOut);
		nbtRegion.setBoolean("ShowInClient", this.showInClient);
	}

}
