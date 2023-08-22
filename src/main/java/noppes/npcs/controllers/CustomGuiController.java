package noppes.npcs.controllers;

import java.io.IOException;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.WrapperNpcAPI;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.util.CustomNPCsScheduler;

public class CustomGuiController {
	static boolean checkGui(CustomGuiEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		return player.openContainer instanceof ContainerCustomGui
				&& ((ContainerCustomGui) player.openContainer).customGui.getId() == event.gui.getId();
	}

	public static CustomGuiWrapper getOpenGui(EntityPlayer player) {
		if (player.openContainer instanceof ContainerCustomGui) {
			return ((ContainerCustomGui) player.openContainer).customGui;
		}
		return null;
	}

	public static IItemStack[] getSlotContents(EntityPlayer player) {
		IItemStack[] slotContents = new IItemStack[0];
		if (player.openContainer instanceof ContainerCustomGui) {
			ContainerCustomGui container = (ContainerCustomGui) player.openContainer;
			slotContents = new IItemStack[container.guiInventory.getSizeInventory()];
			for (int i = 0; i < container.guiInventory.getSizeInventory(); ++i) {
				slotContents[i] = NpcAPI.Instance().getIItemStack(container.guiInventory.getStackInSlot(i));
			}
		}
		return slotContents;
	}

	public static void onButton(CustomGuiEvent.ButtonEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
			getOpenGui(player).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_BUTTON, event, true);
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void onClose(CustomGuiEvent.CloseEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		CustomGuiWrapper gui = getOpenGui(player);
		if (checkGui(event) && gui.getScriptHandler() != null) { // Changed
			gui.getScriptHandler().run(EnumScriptType.CUSTOM_GUI_CLOSED, event, true);
			event.player.closeGui();
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void onScrollClick(CustomGuiEvent.ScrollEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
			getOpenGui(player).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SCROLL, event, true);
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void onSlotChange(CustomGuiEvent.SlotEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
			getOpenGui(player).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SLOT, event, true);
		}
		WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static boolean onSlotClick(CustomGuiEvent.SlotClickEvent event) {
		EntityPlayer player = event.player.getMCEntity();
		if (checkGui(event) && getOpenGui(player).getScriptHandler() != null) {
			getOpenGui(player).getScriptHandler().run(EnumScriptType.CUSTOM_GUI_SLOT_CLICKED, event, true);
		}
		return WrapperNpcAPI.EVENT_BUS.post((Event) event);
	}

	public static void openGui(PlayerWrapper<?> player, CustomGuiWrapper gui) {
		EntityPlayerMP pl = (EntityPlayerMP) player.getMCEntity();
		gui.setPlayer(pl);
		pl.openGui(CustomNpcs.instance, EnumGuiType.CustomGui.ordinal(), pl.world, gui.getSlots().length, 0, 0);
		CustomNPCsScheduler.runTack(() -> {
			Server.sendDataChecked(pl, EnumPacketClient.GUI_DATA, gui.toNBT());
		});
		((ContainerCustomGui) pl.openContainer).setGui(gui, pl);
	}

	public static String[] readScrollSelection(ByteBuf buffer) {
		try {
			NBTTagList list = Server.readNBT(buffer).getTagList("selection", 8);
			String[] selection = new String[list.tagCount()];
			for (int i = 0; i < list.tagCount(); ++i) {
				selection[i] = ((NBTTagString) list.get(i)).getString();
			}
			return selection;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static boolean updateGui(PlayerWrapper<?> player, CustomGuiWrapper gui) {
		EntityPlayerMP pl = (EntityPlayerMP) player.getMCEntity();
		if (!(pl.openContainer instanceof ContainerCustomGui)) { return false; }
		CustomNPCsScheduler.runTack(() -> {
			Server.sendDataChecked(pl, EnumPacketClient.GUI_DATA, gui.toNBT());
		});
		return true;
	}
}
