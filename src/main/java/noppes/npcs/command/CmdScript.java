package noppes.npcs.command;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.controllers.ScriptController;

public class CmdScript extends CommandNoppesBase {
	
	@SubCommand(desc = "List of available Forge event names")
	public Boolean forgelist(MinecraftServer server, ICommandSender sender, String[] args) {
		String list = "";
		List<String> g = Lists.newArrayList(CustomNpcs.forgeEventNames.values());
		Collections.sort(g);
		for (String name : g) {
			if (!list.isEmpty()) {
				list += ", ";
			} else {
				list += new String(Character.toChars(0x00A7)) + "6Forge event names:\n"
						+ new String(Character.toChars(0x00A7)) + "r";
			}
			list += name;
		}
		list += ";\n" + new String(Character.toChars(0x00A7)) + "6Total Size: " + new String(Character.toChars(0x00A7))
				+ "e" + CustomNpcs.forgeEventNames.size();
		sender.sendMessage(new TextComponentString(list));
		return true;
	}

	@Override
	public String getDescription() {
		return "Commands for scripts";
	}

	public String getName() {
		return "script";
	}

	@SubCommand(desc = "Reload scripts and saved data from disks script folder.")
	public Boolean reload(MinecraftServer server, ICommandSender sender, String[] args) {
		ScriptController.Instance.loadCategories();
		if (ScriptController.Instance.loadPlayerScripts()) {
			sender.sendMessage(new TextComponentString("Reload player scripts succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading player scripts"));
		}
		if (ScriptController.Instance.loadForgeScripts()) {
			sender.sendMessage(new TextComponentString("Reload forge scripts succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading forge scripts"));
		}
		if (ScriptController.Instance.loadStoredData()) {
			sender.sendMessage(new TextComponentString("Reload stored data succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading stored data"));
		}
		if (ScriptController.Instance.loadPotionScripts()) {
			sender.sendMessage(new TextComponentString("Reload potion scripts succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading potion scripts"));
		}
		if (ScriptController.Instance.loadConstantData()) {
			sender.sendMessage(new TextComponentString("Reload constant data succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading constant data"));
		}
		return true;
	}

	@SubCommand(desc = "Runs scriptCommand in the players scripts", usage = "[args]")
	public Boolean run(MinecraftServer server, ICommandSender sender, String[] args) {
		IWorld world = NpcAPI.Instance().getIWorld((WorldServer) sender.getEntityWorld());
		BlockPos bpos = sender.getPosition();
		IPos pos = NpcAPI.Instance().getIPos(bpos.getX(), bpos.getY(), bpos.getZ());
		WorldEvent.ScriptCommandEvent event = new WorldEvent.ScriptCommandEvent(world, pos, args);
		EventHooks.onWorldScriptEvent(event);
		return true;
	}
}
