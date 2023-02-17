package net.minecraftforge.fml.common.network.internal;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import io.netty.buffer.ByteBuf;

public class EntitySpawnMessageHelper {
	static EntitySpawnHandler handler = new EntitySpawnHandler();
	static Method spawn;

	static {
		for (Method m : EntitySpawnMessageHelper.handler.getClass().getDeclaredMethods()) {
			if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == FMLMessage.EntitySpawnMessage.class) {
				(EntitySpawnMessageHelper.spawn = m).setAccessible(true);
				break;
			}
		}
	}

	public static void spawn(ByteBuf buffer) {
		FMLMessage.EntitySpawnMessage msg = new FMLMessage.EntitySpawnMessage();
		msg.fromBytes(buffer);
		try {
			EntitySpawnMessageHelper.spawn.invoke(EntitySpawnMessageHelper.handler, msg);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e2) {
			e2.printStackTrace();
		} catch (InvocationTargetException e3) {
			e3.printStackTrace();
		}
	}

	public static void toBytes(FMLMessage.EntitySpawnMessage m, ByteBuf buf) {
		m.toBytes(buf);
	}
}
