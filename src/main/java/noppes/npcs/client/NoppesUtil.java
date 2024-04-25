package noppes.npcs.client;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Vector;

import org.lwjgl.Sys;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Util;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.client.gui.player.GuiDialogInteract;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.entity.EntityNPCInterface;

public class NoppesUtil {
	private static HashMap<String, Integer> data = new HashMap<String, Integer>();
	private static EntityNPCInterface lastNpc;

	public static void addScrollData(ByteBuf buffer) {
		Map<Object, Object> map = Server.readMap(buffer);
		for (Object name : map.keySet()) {
			Object id = map.get(name);
			if (!(id instanceof Integer)) {
				continue;
			}
			NoppesUtil.data.put(name.toString(), (Integer) id);
		}
	}

	public static void clickSound() {
		Minecraft.getMinecraft().getSoundHandler()
				.playSound((ISound) PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0f));
	}

	public static EntityNPCInterface getLastNpc() {
		return NoppesUtil.lastNpc;
	}

	public static void openDialog(Dialog dialog, EntityNPCInterface npc, EntityPlayer player) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null || !(gui instanceof GuiDialogInteract)) {
			CustomNpcs.proxy.openGui(player, new GuiDialogInteract(npc, dialog));
		} else {
			((GuiDialogInteract) gui).initGui();
			((GuiDialogInteract) gui).appenedDialog(dialog);
		}
	}

	public static void openFolder(File dir) {
		String s = dir.getAbsolutePath();
		Label_0072: {
			if (Util.getOSType() == Util.EnumOS.OSX) {
				try {
					Runtime.getRuntime().exec(new String[] { "/usr/bin/open", s });
					return;
				} catch (IOException ex) {
					break Label_0072;
				}
			}
			if (Util.getOSType() == Util.EnumOS.WINDOWS) {
				String s2 = String.format("cmd.exe /C start \"Open file\" \"%s\"", s);
				try {
					Runtime.getRuntime().exec(s2);
					return;
				} catch (IOException ex2) {
				}
			}
		}
		boolean flag = false;
		try {
			Class<?> oclass = Class.forName("java.awt.Desktop");
			Object object = oclass.getMethod("getDesktop", (Class[]) new Class[0]).invoke(null, new Object[0]);
			oclass.getMethod("browse", URI.class).invoke(object, dir.toURI());
		} catch (Throwable throwable) {
			flag = true;
		}
		if (flag) {
			Sys.openURL("file://" + s);
		}
	}

	public static void openGUI(EntityPlayer player, Object guiscreen) {
		CustomNpcs.proxy.openGui(player, guiscreen);
	}

	public static void requestOpenGUI(EnumGuiType gui) {
		requestOpenGUI(gui, 0, 0, 0);
	}

	public static void requestOpenGUI(EnumGuiType gui, int x, int y, int z) {
		Client.sendData(EnumPacketServer.Gui, gui.ordinal(), x, y, z);
	}

	public static void setLastNpc(EntityNPCInterface npc) {
		NoppesUtil.lastNpc = npc;
	}

	public static void setScrollData(ByteBuf buffer) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui == null) {
			return;
		}
		if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
			gui = ((GuiNPCInterface) gui).getSubGui();
		}
		if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
			gui = ((GuiContainerNPCInterface) gui).getSubGui();
		}
		if (!(gui instanceof IScrollData)) {
			return;
		}
		Map<Object, Object> map = Server.readMap(buffer);
		for (Entry<Object, Object> entry : map.entrySet()) {
			try {
				NoppesUtil.data.put((String) entry.getKey(), (int) entry.getValue());
			} catch (Exception ex) {
			}
		}
		((IScrollData) gui).setData(new Vector<String>(NoppesUtil.data.keySet()), NoppesUtil.data);
		NoppesUtil.data.clear();
	}

	public static void setScrollList(ByteBuf buffer) {
		GuiScreen gui = Minecraft.getMinecraft().currentScreen;
		if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
			gui = ((GuiNPCInterface) gui).getSubGui();
		}
		if (gui == null || !(gui instanceof IScrollData)) {
			return;
		}
		Vector<String> data = new Vector<String>();
		try {
			for (int size = buffer.readInt(), i = 0; i < size; ++i) {
				data.add(Server.readString(buffer));
			}
		} catch (Exception ex) {
		}
		((IScrollData) gui).setData(data, null);
	}

	public static void spawnParticle(ByteBuf buffer) throws IOException {
		double posX = buffer.readDouble();
		double posY = buffer.readDouble();
		double posZ = buffer.readDouble();
		float height = buffer.readFloat();
		float width = buffer.readFloat();
		String particle = Server.readString(buffer);
		World world = Minecraft.getMinecraft().world;
		Random rand = world.rand;
		if (particle.equals("heal")) {
			for (int k = 0; k < 6; ++k) {
				world.spawnParticle(EnumParticleTypes.SPELL_INSTANT, posX + (rand.nextDouble() - 0.5) * width,
						posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5) * width, 0.0, 0.0, 0.0,
						new int[0]);
				world.spawnParticle(EnumParticleTypes.SPELL, posX + (rand.nextDouble() - 0.5) * width,
						posY + rand.nextDouble() * height, posZ + (rand.nextDouble() - 0.5) * width, 0.0, 0.0, 0.0,
						new int[0]);
			}
		}
	}

}
