package noppes.npcs;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.AxisAlignedBB;
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
import noppes.npcs.util.CustomNPCsScheduler;

public class Server {

	private static List<EnumPacketClient> list;
	
	static {
		Server.list = new ArrayList<EnumPacketClient>();
		Server.list.add(EnumPacketClient.EYE_BLINK);
		Server.list.add(EnumPacketClient.UPDATE_NPC);
		Server.list.add(EnumPacketClient.SET_TILE_DATA);
		Server.list.add(EnumPacketClient.NPC_VISUAL_DATA);
		Server.list.add(EnumPacketClient.SEND_FILE_PART);
		Server.list.add(EnumPacketClient.PLAY_SOUND);
		Server.list.add(EnumPacketClient.UPDATE_NPC_ANIMATION);
		Server.list.add(EnumPacketClient.CHATBUBBLE);
		Server.list.add(EnumPacketClient.SYNC_ADD);
		Server.list.add(EnumPacketClient.BORDER_DATA);
		Server.list.add(EnumPacketClient.MARCET_DATA);
		Server.list.add(EnumPacketClient.SYNC_END);
	}
	
	public static boolean fillBuffer(ByteBuf buffer, Enum<?> enu, Object... obs) throws IOException {
		buffer.writeInt(enu.ordinal());
		for (Object ob : obs) {
			if (ob != null) {
				if (ob instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, Integer> map = (Map<String, Integer>) ob;
					buffer.writeInt(map.size());
					for (String key : map.keySet()) {
						int value = map.get(key);
						buffer.writeInt(value);
						writeString(buffer, key);
					}
				} else if (ob instanceof MerchantRecipeList) {
					((MerchantRecipeList) ob).writeToBuf(new PacketBuffer(buffer));
				} else if (ob instanceof List) {
					try {
						@SuppressWarnings("unchecked")
						List<String> list = (List<String>) ob;
						buffer.writeInt(list.size());
						for (String s : list) {
							writeString(buffer, s);
						}
					} catch (Exception e) { }
					try {
						@SuppressWarnings("unchecked")
						List<Integer> list = (List<Integer>) ob;
						int[] a = new int[list.size()];
						int j = 0;
						for (int i : list) {
							a[j] = i;
							j++;
						}
						writeIntArray(buffer, a);
					} catch (Exception e) { }
				} else if (ob instanceof UUID) {
					writeString(buffer, ob.toString());
				} else if (ob instanceof Enum) {
					buffer.writeInt(((Enum<?>) ob).ordinal());
				} else if (ob instanceof Integer) {
					buffer.writeInt((int) ob);
				} else if (ob instanceof Boolean) {
					buffer.writeBoolean((boolean) ob);
				} else if (ob instanceof String) {
					writeString(buffer, (String) ob);
				} else if (ob instanceof ResourceLocation) {
					writeString(buffer, ((ResourceLocation) ob).toString());
				} else if (ob instanceof Float) {
					buffer.writeFloat((float) ob);
				} else if (ob instanceof Long) {
					buffer.writeLong((long) ob);
				} else if (ob instanceof Double) {
					buffer.writeDouble((double) ob);
				} else if (ob instanceof NBTTagCompound) {
					writeNBT(buffer, (NBTTagCompound) ob);
				} else if (ob instanceof FMLMessage.EntitySpawnMessage) {
					EntitySpawnMessageHelper.toBytes((FMLMessage.EntitySpawnMessage) ob, buffer);
				} else if (ob instanceof Integer[] || ob instanceof int[]) {
					writeIntArray(buffer, (int[]) ob);
				} else if (ob instanceof WorldInfo) {
					writeWorldInfo(buffer, (WorldInfo) ob);
				}
			}
		}
		if (buffer.array().length >= 65534) {
			LogWriter.error("Packet " + enu + " was too big to be send");
			return false;
		}
		return true;
	}

	public static NBTTagCompound readNBT(ByteBuf buffer) throws IOException {
		byte[] bytes = new byte[buffer.readInt()];
		buffer.readBytes(bytes);
		DataInputStream datainputstream = new DataInputStream( new BufferedInputStream(new GZIPInputStream(new ByteArrayInputStream(bytes))));
		try {
			return CompressedStreamTools.read((DataInput) datainputstream, NBTSizeTracker.INFINITE);
		} finally {
			datainputstream.close();
		}
	}

	public static String readString(ByteBuf buffer) {
		try {
			byte[] bytes = new byte[buffer.readInt()];
			buffer.readBytes(bytes);
			String str = new String(bytes, Charset.forName("UTF-8"));
			return str;
		} catch (IndexOutOfBoundsException ex) {
			return null;
		}
	}

	public static UUID readUUID(ByteBuf buffer) {
		return UUID.fromString(readString(buffer));
	}

	public static int[] readIntArray(ByteBuf buffer) {
		int[] a = new int[buffer.readInt()];
		for (int i = 0; i < a.length; i++) { a[i] = buffer.readInt(); }
		return a;
	}

	public static CustomWorldInfo readWorldInfo(ByteBuf buffer) {
		return new CustomWorldInfo(ByteBufUtils.readTag(buffer));
	}

	public static void sendAssociatedData(Entity entity, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = (List<EntityPlayerMP>) entity.world.getEntitiesWithinAABB(EntityPlayerMP.class, entity.getEntityBoundingBox().grow(160.0, 160.0, 160.0));
		if (list.isEmpty()) { return; }
		CustomNPCsScheduler.runTack(() -> {
			ByteBuf buffer = Unpooled.buffer();
			try {
				if (!(!fillBuffer(buffer, type, obs))) {
					if (!Server.list.contains(type)) {
						LogWriter.debug("SendAssociatedData: " + type);
					}
					Iterator<EntityPlayerMP> iterator = list.iterator();
					;
					while (iterator.hasNext()) {
						CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), "CustomNPCs"),
								iterator.next());
					}
				}
			} catch (IOException e) {
				LogWriter.error(type + " Errored", e);
			} finally {
				buffer.release();
			}
		});
	}

	public static void sendData(EntityPlayerMP player, EnumPacketClient enu, Object... obs) {
		sendDataDelayed(player, enu, 0, obs);
	}

	public static boolean sendDataChecked(EntityPlayerMP player, EnumPacketClient type, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!fillBuffer((ByteBuf) buffer, type, obs)) {
				return false;
			}
			if (!Server.list.contains(type)) {
				LogWriter.debug("SendDataChecked: " + type);
			}
			CustomNpcs.Channel.sendTo(new FMLProxyPacket(buffer, "CustomNPCs"), player);
		} catch (IOException e) {
			LogWriter.error(type + " Errored", e);
		}
		return true;
	}

	public static void sendDataDelayed(EntityPlayerMP player, EnumPacketClient type, int delay, Object... obs) {
		CustomNPCsScheduler.runTack(() -> {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				if (fillBuffer((ByteBuf) buffer, type, obs)) {
					if (!Server.list.contains(type)) {
						LogWriter.debug("SendData: " + type);
					}
					CustomNpcs.Channel.sendTo(new FMLProxyPacket(buffer, "CustomNPCs"), player);
				} else {
					LogWriter.error("Not Send: " + type);
				}
			} catch (IOException e) {
				LogWriter.error(type + " Errored", e);
			}
		}, delay);
	}

	public static void sendRangedData(Entity entity, int range, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = (List<EntityPlayerMP>) entity.world.getEntitiesWithinAABB(EntityPlayerMP.class,
				entity.getEntityBoundingBox().grow(range, range, range));
		if (list.isEmpty()) {
			return;
		}
		CustomNPCsScheduler.runTack(() -> {
			ByteBuf buffer = Unpooled.buffer();
			try {
				if (!(!fillBuffer(buffer, type, obs))) {
					if (!Server.list.contains(type)) {
						LogWriter.debug("SendRangedData: " + type);
					}
					Iterator<EntityPlayerMP> iterator = list.iterator();
					while (iterator.hasNext()) {
						CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), "CustomNPCs"),
								iterator.next());
					}
				}
			} catch (IOException e) {
				LogWriter.error(type + " Errored", e);
			} finally {
				buffer.release();
			}
		});
	}

	public static void sendRangedData(World world, BlockPos pos, int range, EnumPacketClient type, Object... obs) {
		List<EntityPlayerMP> list = (List<EntityPlayerMP>) world.getEntitiesWithinAABB(EntityPlayerMP.class, new AxisAlignedBB(pos).grow(range, range, range));
		if (list.isEmpty()) { return; }
		CustomNPCsScheduler.runTack(() -> {
			ByteBuf buffer = Unpooled.buffer();
			try {
				if (!(!fillBuffer(buffer, type, obs))) {
					if (!Server.list.contains(type)) {
						LogWriter.debug("SendRangedData: " + type);
					}
					Iterator<EntityPlayerMP> iterator = list.iterator();
					while (iterator.hasNext()) {
						CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), "CustomNPCs"), iterator.next());
					}
				}
			} catch (IOException e) {
				LogWriter.error(type + " Errored", e);
			} finally {
				buffer.release();
			}
		});
	}

	public static void sendToAll(MinecraftServer server, EnumPacketClient type, Object... obs) {
		if (server==null) { return; }
		List<EntityPlayerMP> list = new ArrayList<EntityPlayerMP>(server.getPlayerList().getPlayers());
		CustomNPCsScheduler.runTack(() -> {
			ByteBuf buffer = Unpooled.buffer();
			try {
				if (!(!fillBuffer(buffer, type, obs))) {
					if (!Server.list.contains(type)) {
						LogWriter.debug("SendToAll: " + type);
					}
					Iterator<EntityPlayerMP> iterator = list.iterator();
					while (iterator.hasNext()) {
						CustomNpcs.Channel.sendTo(new FMLProxyPacket(new PacketBuffer(buffer.copy()), "CustomNPCs"),
								iterator.next());
					}
				}
			} catch (IOException e) {
				LogWriter.error(type + " Errored", e);
			} finally {
				buffer.release();
			}
		});
	}

	public static void writeNBT(ByteBuf buffer, NBTTagCompound compound) throws IOException {
		ByteArrayOutputStream bytearrayoutputstream = new ByteArrayOutputStream();
		DataOutputStream dataoutputstream = new DataOutputStream(new GZIPOutputStream(bytearrayoutputstream));
		try {
			CompressedStreamTools.write(compound, (DataOutput) dataoutputstream);
		} finally {
			dataoutputstream.close();
		}
		byte[] bytes = bytearrayoutputstream.toByteArray();
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}

	public static void writeString(ByteBuf buffer, String s) {
		byte[] bytes = s.getBytes(Charset.forName("UTF-8"));
		buffer.writeInt(bytes.length);
		buffer.writeBytes(bytes);
	}

	public static void writeIntArray(ByteBuf buffer, int[] a) {
		buffer.writeInt(a.length);
		for (int i : a) {
			buffer.writeInt(i);
		}
	}

	public static void writeWorldInfo(ByteBuf buffer, WorldInfo wi) {
		ByteBufUtils.writeTag(buffer, wi.cloneNBTCompound(null));
	}
	
}
