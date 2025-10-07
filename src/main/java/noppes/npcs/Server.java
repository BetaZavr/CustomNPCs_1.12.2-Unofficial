package noppes.npcs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.Map.Entry;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagFloat;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagLong;
import net.minecraft.nbt.NBTTagShort;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.network.PacketBuffer;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.village.MerchantRecipeList;
import net.minecraft.world.World;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.internal.EntitySpawnMessageHelper;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.reflection.pathfinding.PathReflection;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;

public class Server {

	private static List<EnumPacketClient> list;

	static {
		Server.list = new ArrayList<>();
		Server.list.add(EnumPacketClient.EYE_BLINK);
		Server.list.add(EnumPacketClient.UPDATE_NPC);
		Server.list.add(EnumPacketClient.SET_TILE_DATA);
		Server.list.add(EnumPacketClient.SEND_FILE_PART);
		Server.list.add(EnumPacketClient.PLAY_SOUND);
		Server.list.add(EnumPacketClient.UPDATE_NPC_ANIMATION);
		Server.list.add(EnumPacketClient.UPDATE_NPC_NAVIGATION);
		Server.list.add(EnumPacketClient.UPDATE_NPC_TARGET);
		Server.list.add(EnumPacketClient.CHAT_BUBBLE);
		Server.list.add(EnumPacketClient.SYNC_ADD);
		Server.list.add(EnumPacketClient.BORDER_DATA);
		Server.list.add(EnumPacketClient.MARCET_DATA);
		Server.list.add(EnumPacketClient.SYNC_END);
		Server.list.add(EnumPacketClient.SYNC_UPDATE);
		Server.list.add(EnumPacketClient.NPC_MOVING_PATH);
		Server.list.add(EnumPacketClient.VISIBLE_TRUE);
		Server.list.add(EnumPacketClient.VISIBLE_FALSE);
		Server.list.add(EnumPacketClient.NPC_DATA);
		Server.list.add(EnumPacketClient.NPC_VISUAL_DATA);
		Server.list.add(EnumPacketClient.FORCE_PLAY_SOUND);
		Server.list.add(EnumPacketClient.NPC_LOOK_POS);
		Server.list.add(EnumPacketClient.UPDATE_HUD);
		Server.list.add(EnumPacketClient.ANIMATION_DATA_SET);
		Server.list.add(EnumPacketClient.ANIMATION_DATA_BASE_ANIMATIONS);
		Server.list.add(EnumPacketClient.ANIMATION_DATA_RUN_ANIMATION);
		Server.list.add(EnumPacketClient.ANIMATION_DATA_STOP_ANIMATION);
		Server.list.add(EnumPacketClient.ANIMATION_DATA_STOP_EMOTION);
		Server.list.add(EnumPacketClient.SCRIPT_CONSOLE);
		Server.list.add(EnumPacketClient.SCRIPT_CODE);
		Server.list.add(EnumPacketClient.GUI);
		Server.list.add(EnumPacketClient.GUI_DATA);
		Server.list.add(EnumPacketClient.SCROLL_DATA);
		Server.list.add(EnumPacketClient.SCROLL_SELECTED);
		Server.list.add(EnumPacketClient.EDIT_NPC);
	}

	public static boolean fillBuffer(ByteBuf buffer, Enum<?> type, Object... obs) throws Exception {
		buffer.writeInt(type.ordinal());
		for (Object ob : obs) {
			if (ob != null) {
				if (ob instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<Object, Object> map = (Map<Object, Object>) ob;
					buffer.writeInt(map.size());
					int i = 0;
					for (Entry<Object, Object> entry : map.entrySet()) {
						NBTBase key = Util.instance.writeObjectToNbt(entry.getKey());
						NBTBase value = Util.instance.writeObjectToNbt(entry.getValue());
						if (key != null && key.getId() < (byte) 9 && key.getId() != (byte) 7) {
							buffer.writeByte(key.getId());
							switch (key.getId()) {
							case 0:
								buffer.writeByte((byte) 0);
								break;
							case 1:
								buffer.writeByte(((NBTTagByte) key).getByte());
								break;
							case 2:
								buffer.writeShort(((NBTTagShort) key).getShort());
								break;
							case 3:
								buffer.writeInt(((NBTTagInt) key).getInt());
								break;
							case 4:
								buffer.writeLong(((NBTTagLong) key).getLong());
								break;
							case 5:
								buffer.writeFloat(((NBTTagFloat) key).getFloat());
								break;
							case 6:
								buffer.writeDouble(((NBTTagDouble) key).getDouble());
								break;
							case 8:
								writeString(buffer, ((NBTTagString) key).getString());
								break;
							default:
								writeString(buffer, "unknown_key_" + i);
							}
						} else {
							buffer.writeByte((byte) 16);
							writeString(buffer, "unknown_key_" + i);
						}
						if (value != null && value.getId() < (byte) 9 && value.getId() != (byte) 7) {
							buffer.writeByte(value.getId());
							switch (value.getId()) {
							case 0:
								buffer.writeByte((byte) 0);
								break;
							case 1:
								buffer.writeByte(((NBTTagByte) value).getByte());
								break;
							case 2:
								buffer.writeShort(((NBTTagShort) value).getShort());
								break;
							case 3:
								buffer.writeInt(((NBTTagInt) value).getInt());
								break;
							case 4:
								buffer.writeLong(((NBTTagLong) value).getLong());
								break;
							case 5:
								buffer.writeFloat(((NBTTagFloat) value).getFloat());
								break;
							case 6:
								buffer.writeDouble(((NBTTagDouble) value).getDouble());
								break;
							case 8:
								writeString(buffer, ((NBTTagString) value).getString());
								break;
							default:
								writeString(buffer, "unknown_value_" + i);
							}
						} else {
							buffer.writeByte((byte) 16);
							writeString(buffer, "unknown_value_" + i);
						}
						i++;
					}
				}
				else if (ob instanceof MerchantRecipeList) {
					((MerchantRecipeList) ob).writeToBuf(new PacketBuffer(buffer));
				}
				else if (ob instanceof List) {
					boolean bo = false;
					try {
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) ob;
						boolean start = true;
						for (String s : list) {
							if (start) {
								start = false;
								buffer.writeInt(list.size());
							}
							writeString(buffer, s);
						}
						bo = true;
					} catch (Exception ignore) { }
					if (!bo) {
						try {
							@SuppressWarnings("unchecked")
							List<Integer> list = (List<Integer>) ob;
							int[] a = new int[list.size()];
							int j = 0;
							for (int i : list) { a[j] = i; j++; }
							bo = true;
							writeIntArray(buffer, a);
						} catch (Exception ignored) { }
					}
					if (!bo) {
						try {
							@SuppressWarnings("unchecked")
							List<ItemStack> list = (List<ItemStack>) ob;
							buffer.writeInt(list.size());
							for (ItemStack itemStack : list) {
								writeNBT(buffer, itemStack.writeToNBT(new NBTTagCompound()));
							}
						} catch (Exception ignored) { }
					}
				}
				else if (ob instanceof UUID) {
					writeString(buffer, ob.toString());
				}
				else if (ob instanceof Enum) {
					buffer.writeInt(((Enum<?>) ob).ordinal());
				}
				else if (ob instanceof Integer) {
					buffer.writeInt((int) ob);
				}
				else if (ob instanceof Boolean) {
					buffer.writeBoolean((boolean) ob);
				}
				else if (ob instanceof String) {
					writeString(buffer, (String) ob);
				}
				else if (ob instanceof ResourceLocation) {
					writeString(buffer, ((ResourceLocation) ob).toString());
				}
				else if (ob instanceof Float) {
					buffer.writeFloat((float) ob);
				}
				else if (ob instanceof Long) {
					buffer.writeLong((long) ob);
				}
				else if (ob instanceof Double) {
					buffer.writeDouble((double) ob);
				}
				else if (ob instanceof NBTTagCompound) {
					writeNBT(buffer, (NBTTagCompound) ob);
				}
				else if (ob instanceof FMLMessage.EntitySpawnMessage) {
					EntitySpawnMessageHelper.toBytes((FMLMessage.EntitySpawnMessage) ob, buffer);
				}
				else if (ob instanceof Integer[] || ob instanceof int[]) {
                    assert ob instanceof int[];
                    writeIntArray(buffer, (int[]) ob);
				}
				else if (ob instanceof WorldInfo) {
					writeWorldInfo(buffer, (WorldInfo) ob);
				}
			}
		}
		if (buffer.array().length > 32768) {
			throw new RuntimeException("Packet " + type + " was too big to be send ["+buffer.array().length+"/32768]");
		}
		return true;
	}

	public static int[] readIntArray(ByteBuf buffer) {
		int[] a = new int[buffer.readInt()];
		for (int i = 0; i < a.length; i++) {
			a[i] = buffer.readInt();
		}
		return a;
	}

	public static Map<Object, Object> readMap(ByteBuf buffer) {
		Map<Object, Object> map = new LinkedHashMap<>();
		int size = buffer.readInt();
		for (int i = 0; i < size; i++) {
			Object key;
			switch (buffer.readByte()) {
				case 0:
                case 1:
						key = buffer.readByte();
					break;
					case 2:
					key = buffer.readShort();
					break;
				case 3:
					key = buffer.readInt();
					break;
				case 4:
					key = buffer.readLong();
					break;
				case 5:
					key = buffer.readFloat();
					break;
				case 6:
					key = buffer.readDouble();
					break;
                default:
					key = readString(buffer);
			}
			Object value;
			switch (buffer.readByte()) {
				case 0:
                case 1:
                    value = buffer.readByte();
					break;
                case 2:
					value = buffer.readShort();
					break;
				case 3:
					value = buffer.readInt();
					break;
				case 4:
					value = buffer.readLong();
					break;
				case 5:
					value = buffer.readFloat();
					break;
				case 6:
					value = buffer.readDouble();
					break;
                default:
					value = readString(buffer);
			}
			map.put(key, value);
		}
		return map;
	}

	public static NBTTagCompound readNBT(ByteBuf buffer) throws IOException {
		byte[] bytes = new byte[buffer.readInt()];
		buffer.readBytes(bytes);
        try (DataInputStream datainputstream = new DataInputStream(
                new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))))) {
            return CompressedStreamTools.read(datainputstream, NBTSizeTracker.INFINITE);
        }
	}

	private static PathPoint readPathPoint(NBTTagCompound nbt) {
		PathPoint point = new PathPoint(nbt.getInteger("x"), nbt.getInteger("y"), nbt.getInteger("z"));
		point.distanceFromOrigin = nbt.getFloat("dfo");
		point.cost = nbt.getFloat("c");
		point.costMalus = nbt.getFloat("cm");
		point.visited = nbt.getBoolean("dfo");
		point.nodeType = PathNodeType.values()[nbt.getInteger("nt")];
		point.distanceToTarget = nbt.getFloat("d");
		return point;
	}

	public static Path readPathToNBT(NBTTagCompound nbt) {
		PathPoint[] points = new PathPoint[nbt.getTagList("ps", 10).tagCount()];
		PathPoint[] openSet = new PathPoint[nbt.getTagList("op", 10).tagCount()];
		PathPoint[] closedSet = new PathPoint[nbt.getTagList("cp", 10).tagCount()];
		for (int i = 0; i < nbt.getTagList("ps", 10).tagCount(); i++) {
			points[i] = Server.readPathPoint(nbt.getTagList("ps", 10).getCompoundTagAt(i));
		}
		for (int i = 0; i < nbt.getTagList("op", 10).tagCount(); i++) {
			openSet[i] = Server.readPathPoint(nbt.getTagList("op", 10).getCompoundTagAt(i));
		}
		for (int i = 0; i < nbt.getTagList("cp", 10).tagCount(); i++) {
			closedSet[i] = Server.readPathPoint(nbt.getTagList("cp", 10).getCompoundTagAt(i));
		}
		Path navigating = new Path(points);
		PathReflection.setOpenSet(navigating, openSet);
		PathReflection.setClosedSet(navigating, closedSet);
		PathReflection.setCurrentPathIndex(navigating, nbt.getInteger("ci"));
		return navigating;
	}

	public static String readString(ByteBuf buffer) {
		try {
			byte[] bytes = new byte[buffer.readInt()];
			buffer.readBytes(bytes);
			return new String(bytes, StandardCharsets.UTF_8);
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	public static UUID readUUID(ByteBuf buffer) {
		return UUID.fromString(Objects.requireNonNull(readString(buffer)));
	}

	public static CustomWorldInfo readWorldInfo(ByteBuf buffer) {
		return new CustomWorldInfo(ByteBufUtils.readTag(buffer));
	}

	public static void sendAssociatedData(Entity entity, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = Util.instance.getEntitiesWithinDist(EntityPlayerMP.class, entity.world, entity, 160.0d);
		if (list.isEmpty()) { return; }
		ByteBuf buffer = Unpooled.buffer();
		try {
			if (fillBuffer(buffer, type, obs)) {
				if (!Server.list.contains(type)) {
					LogWriter.debug("SendAssociatedData: " + type);
				}
				for (EntityPlayerMP entityPlayerMP : list) {
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), CustomNpcs.MODNAME), entityPlayerMP);
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
	}

	public static void sendData(EntityPlayerMP player, EnumPacketClient type, Object... obs) {
		ByteBuf buffer = Unpooled.buffer();
		try {
			if (fillBuffer(buffer, type, obs)) {
				if (!Server.list.contains(type)) {
					LogWriter.debug("SendAssociatedData: " + type);
				}
				CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), CustomNpcs.MODNAME), player);
			}
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
	}

	public static boolean sendDataChecked(EntityPlayerMP player, EnumPacketClient type, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!fillBuffer(buffer, type, obs)) {
				return false;
			}
			if (!Server.list.contains(type)) {
				LogWriter.debug("SendDataChecked: " + type);
			}
			CustomNpcs.Channel.sendTo(new FMLProxyPacket(buffer, CustomNpcs.MODNAME), player);
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
		return true;
	}

	public static void sendDataDelayed(EntityPlayerMP player, EnumPacketClient type, int delay, Object... obs) {
		CustomNPCsScheduler.runTack(() -> {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				if (fillBuffer(buffer, type, obs)) {
					if (!Server.list.contains(type)) { LogWriter.debug("SendData: " + type); }
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(buffer, CustomNpcs.MODNAME), player);
				} else {
					LogWriter.error("Not Send: " + type);
				}
			}
			catch (Exception e) { LogWriter.error("Error send data:", e); }
		}, delay);
	}

	public static void sendRangedData(Entity entity, int range, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = Util.instance.getEntitiesWithinDist(EntityPlayerMP.class, entity.world, entity, range);
		if (list.isEmpty()) { return; }
		ByteBuf buffer = Unpooled.buffer();
		try {
			if (fillBuffer(buffer, type, obs)) {
				if (!Server.list.contains(type)) {
					LogWriter.debug("SendRangedData: " + type);
				}
				for (EntityPlayerMP entityPlayerMP : list) {
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), CustomNpcs.MODNAME), entityPlayerMP);
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
	}

	public static void sendRangedData(World world, BlockPos pos, int range, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = Util.instance.getEntitiesWithinDist(EntityPlayerMP.class, world, pos, range);
		if (list.isEmpty()) { return; }
		ByteBuf buffer = Unpooled.buffer();
		try {
			if (fillBuffer(buffer, type, obs)) {
				if (!Server.list.contains(type)) {
					LogWriter.debug("SendRangedData: " + type);
				}
				for (EntityPlayerMP entityPlayerMP : list) {
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), CustomNpcs.MODNAME), entityPlayerMP);
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
	}

	public static void sendToAll(MinecraftServer server, EnumPacketClient type, Object ... obs) {
		if (server == null) { return; }
		List<EntityPlayerMP> list = new ArrayList<>(server.getPlayerList().getPlayers());
		ByteBuf buffer = Unpooled.buffer();
		try {
			if (fillBuffer(buffer, type, obs)) {
				if (!Server.list.contains(type)) {
					LogWriter.debug("SendToAll: " + type);
				}
				for (EntityPlayerMP entityPlayerMP : list) {
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), CustomNpcs.MODNAME), entityPlayerMP);
				}
			}
		}
		catch (Exception e) { LogWriter.error("Error send data:", e); }
	}

	public static void writeIntArray(ByteBuf buffer, int[] a) {
		buffer.writeInt(a.length);
		for (int i : a) {
			buffer.writeInt(i);
		}
	}

	public static void writeNBT(ByteBuf buffer, NBTTagCompound compound) throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
        try (DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream))) {
            CompressedStreamTools.write(compound, dataoutputstream);
        }
		byte[] bytes = bytearrayoutputstream.toByteArray();
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}

	private static NBTTagCompound writePathPoint(PathPoint point) {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("x", point.x);
		nbt.setInteger("y", point.y);
		nbt.setInteger("z", point.z);
		nbt.setFloat("dfo", point.distanceFromOrigin);
		nbt.setFloat("c", point.cost);
		nbt.setFloat("cm", point.costMalus);
		nbt.setFloat("d", point.distanceToTarget);
		nbt.setBoolean("dfo", point.visited);
		nbt.setInteger("nt", point.nodeType.ordinal());
		return nbt;
	}

	public static NBTTagCompound writePathToNBT(Path path) {
		NBTTagCompound nbt = new NBTTagCompound();
		PathPoint[] points = PathReflection.getPoints(path);
		PathPoint[] openSet = PathReflection.getOpenSet(path);
		PathPoint[] closedSet = PathReflection.getClosedSet(path);

		NBTTagList ps = new NBTTagList();
        assert points != null;
        for (PathPoint p : points) {
			ps.appendTag(Server.writePathPoint(p));
		}
		nbt.setTag("ps", ps);

		NBTTagList op = new NBTTagList();
        assert openSet != null;
        for (PathPoint p : openSet) {
			op.appendTag(Server.writePathPoint(p));
		}
		nbt.setTag("op", op);

		NBTTagList cp = new NBTTagList();
        assert closedSet != null;
        for (PathPoint p : closedSet) {
			cp.appendTag(Server.writePathPoint(p));
		}
		nbt.setTag("cp", cp);

		nbt.setInteger("ci", PathReflection.getCurrentPathIndex(path));
		return nbt;
	}

	public static void writeString(ByteBuf buffer, String s) {
		byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}

	public static void writeWorldInfo(ByteBuf buffer, WorldInfo wi) {
		ByteBufUtils.writeTag(buffer, wi.cloneNBTCompound(null));
	}

}
