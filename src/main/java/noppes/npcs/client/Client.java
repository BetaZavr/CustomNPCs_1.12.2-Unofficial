package noppes.npcs.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.CustomNPCsScheduler;

public class Client {

	public static List<EnumPacketServer> notDebugShow;

	static {
		Client.notDebugShow = new ArrayList<>();
		Client.notDebugShow.add(EnumPacketServer.RemoteReset);
	}

	public static void sendData(EnumPacketServer type, Object... obs) {
		CustomNPCsScheduler.runTack(() -> {
			PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
			try {
				if (Server.fillBuffer(buffer, type, obs)) {
					if (!notDebugShow.contains(type)) {
						LogWriter.debug("Send: " + type);
					}
					CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
				}
			} catch (IOException e) { LogWriter.error("Error:", e); }
		});
	}

	public static void sendDirectData(EnumPacketServer type, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!Server.fillBuffer(buffer, type, obs)) {
				return;
			}
			LogWriter.debug("Send: " + type);
			CustomNpcs.Channel.sendToServer(new FMLProxyPacket(buffer, "CustomNPCs"));
		} catch (IOException e) { LogWriter.error("Error:", e); }
	}

}
