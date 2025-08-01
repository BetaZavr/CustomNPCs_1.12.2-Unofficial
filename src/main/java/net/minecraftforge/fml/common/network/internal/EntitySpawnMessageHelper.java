package net.minecraftforge.fml.common.network.internal;

import java.lang.reflect.Method;

import io.netty.buffer.ByteBuf;
import noppes.npcs.LogWriter;

public class EntitySpawnMessageHelper {

	static EntitySpawnHandler handler = new EntitySpawnHandler();
	static Method spawn;

	static {
		for (Method m : EntitySpawnMessageHelper.handler.getClass().getDeclaredMethods()) {
			if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == FMLMessage.EntitySpawnMessage.class) { // spawnEntity
				(EntitySpawnMessageHelper.spawn = m).setAccessible(true);
				break;
			}
		}
	}

	public static void spawn(ByteBuf buffer) {
		FMLMessage.EntitySpawnMessage msg = new FMLMessage.EntitySpawnMessage();
		msg.fromBytes(buffer);
		try { EntitySpawnMessageHelper.spawn.invoke(EntitySpawnMessageHelper.handler, msg); }
		catch (Exception e) { LogWriter.error(e); }
	}

	public static void toBytes(FMLMessage.EntitySpawnMessage m, ByteBuf buf) {
		m.toBytes(buf);
	}

}
