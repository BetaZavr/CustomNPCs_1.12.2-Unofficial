package noppes.npcs.controllers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class PixelmonHelper {

	public static boolean Enabled = Loader.isModLoaded("pixelmon");
	public static EventBus EVENT_BUS;
	private static Method getPartyStorage;
	private static Method getPcStorage;
	private static Method getPixelmonModel = null;
	private static Method getPokemonData;
	private static Class<?> modelSetupClass;
	private static Method modelSetupMethod;
	private static Class<?> pixelmonClass;
	public static Object storageManager;

	public static Object getModel(EntityLivingBase entity) {
		try {
			return PixelmonHelper.getPixelmonModel.invoke(entity);
		} catch (Exception e) {
			LogWriter.error("getModel:", e);
			return null;
		}
	}

	public static String getName(EntityLivingBase entity) {
		if (!PixelmonHelper.Enabled || !isPixelmon(entity)) {
			return "";
		}
		try {
			Method m = entity.getClass().getMethod("getName");
			return m.invoke(entity).toString();
		} catch (Exception e) {
			LogWriter.error("getName:", e);
			return "";
		}
	}

	public static Object getParty(EntityPlayerMP player) {
		try {
			return PixelmonHelper.getPartyStorage.invoke(PixelmonHelper.storageManager, player);
		} catch (Exception e) {
			LogWriter.error(e);
			return null;
		}
	}

	public static Object getPc(EntityPlayerMP player) {
		try {
			return PixelmonHelper.getPcStorage.invoke(PixelmonHelper.storageManager, player);
		} catch (Exception e) {
			LogWriter.error("getPc:", e);
			return null;
		}
	}

	public static Class<?> getPixelmonClass() {
		return PixelmonHelper.pixelmonClass;
	}

	public static List<String> getPixelmonList() {
		List<String> list = new ArrayList<>();
		if (!PixelmonHelper.Enabled) {
			return list;
		}
		try {
			Class<?> c = Class.forName("com.pixelmonmod.pixelmon.enums.EnumPokemonModel");
			for (Object ob : c.getEnumConstants()) {
				list.add(ob.toString());
			}
		} catch (Exception e) {
			LogWriter.error("getPixelmonList:", e);
		}
		return list;
	}

	public static Object getPokemonData(Entity entity) {
		try {
			return PixelmonHelper.getPokemonData.invoke(entity);
		} catch (Exception e) {
			LogWriter.error("getPokemonData:", e);
			return null;
		}
	}

	public static boolean isPixelmon(Entity entity) {
		if (!PixelmonHelper.Enabled) {
			return false;
		}
		String s = EntityList.getEntityString(entity);
		return s != null && s.contains("Pixelmon");
	}

	public static void load() {
		if (!PixelmonHelper.Enabled) { return; }
		CustomNpcs.debugData.start(null);
		try {
			Class<?> c = Class.forName("com.pixelmonmod.pixelmon.Pixelmon");
			PixelmonHelper.storageManager = c.getDeclaredField("storageManager").get(null);
			PixelmonHelper.EVENT_BUS = (EventBus) c.getDeclaredField("EVENT_BUS").get(null);
			c = Class.forName("com.pixelmonmod.pixelmon.api.storage.IStorageManager");
			PixelmonHelper.getPartyStorage = c.getMethod("getParty", EntityPlayerMP.class);
			PixelmonHelper.getPcStorage = c.getMethod("getPCForPlayer", EntityPlayerMP.class);
			PixelmonHelper.pixelmonClass = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity1Base");
			PixelmonHelper.getPokemonData = PixelmonHelper.pixelmonClass.getMethod("getPokemonData");
		} catch (Exception e) {
			LogWriter.except(e);
			PixelmonHelper.Enabled = false;
		}
		CustomNpcs.debugData.end(null);
	}

	public static void loadClient() {
		if (!PixelmonHelper.Enabled) {
			return;
		}
		try {
			Class<?> c = Class.forName("com.pixelmonmod.pixelmon.entities.pixelmon.Entity2Client");
			PixelmonHelper.getPixelmonModel = c.getMethod("getModel");
			PixelmonHelper.modelSetupClass = Class.forName("com.pixelmonmod.pixelmon.client.models.PixelmonModelSmd");
			PixelmonHelper.modelSetupMethod = PixelmonHelper.modelSetupClass.getMethod("setupForRender", c);
		} catch (Exception e) {
			LogWriter.except(e);
			PixelmonHelper.Enabled = false;
		}
	}

	public static void setupModel(EntityLivingBase entity, Object model) {
		try {
			if (PixelmonHelper.modelSetupClass.isAssignableFrom(model.getClass())) {
				PixelmonHelper.modelSetupMethod.invoke(model, entity);
			}
		} catch (Exception e) {
			LogWriter.error("setupModel:", e);
		}
	}

}
