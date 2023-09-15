package noppes.npcs.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.util.CustomNPCsScheduler;

public class Client {

	// Clear in
	public static Map<Object, Long> delayPackets = new HashMap<Object, Long>();
	public static List<EnumPacketServer> notDebugShow;

	static {
		Client.notDebugShow = new ArrayList<EnumPacketServer>();
		Client.notDebugShow.add(EnumPacketServer.StopSound);
		Client.notDebugShow.add(EnumPacketServer.PlaySound);
		Client.notDebugShow.add(EnumPacketServer.RemoteReset);
	}
	
	public static void sendData(EnumPacketServer type, Object... obs) {
		CustomNPCsScheduler.runTack(() -> {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				if (!(!Server.fillBuffer(buffer, type, obs))) {
					if (!notDebugShow.contains(type)) { LogWriter.debug("Send: " + type); }
					CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
	}

	public static boolean sendDataDelayCheck(EnumPacketServer type, Object key, int delayMilliSec, Object... obs) {
		if (delayMilliSec>0 && Client.delayPackets.containsKey(key) && Client.delayPackets.get(key) + delayMilliSec > System.currentTimeMillis()) {
			return false;
		}
		if (delayMilliSec>0) { Client.delayPackets.put(key, System.currentTimeMillis()); }
		CustomNPCsScheduler.runTack(() -> {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				if (!(!Server.fillBuffer(buffer, type, obs))) {
					if (!notDebugShow.contains(type)) { LogWriter.debug("Send: " + type); }
					CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		});
		return true;
	}
	
	public static boolean sendDataDelayCheck(EnumPlayerPacket type, Object key, int delayMilliSec, Object... obs) {
		if (delayMilliSec>0 && Client.delayPackets.containsKey(key) && Client.delayPackets.get(key) + delayMilliSec > System.currentTimeMillis()) {
			return false;
		}
		if (delayMilliSec>0) { Client.delayPackets.put(key, System.currentTimeMillis()); }
		NoppesUtilPlayer.sendData(type, obs);
		return true;
	}

	public static void sendDirectData(EnumPacketServer type, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!Server.fillBuffer(buffer, type, obs)) {
				return;
			}
			LogWriter.debug("Send: " + type);
			CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
