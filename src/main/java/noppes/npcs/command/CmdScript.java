package noppes.npcs.command;

import java.util.Collections;
import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.dimensions.DimensionHandler;

public class CmdScript
extends CommandNoppesBase {
	
	@Override
	public String getDescription() {
		return "Commands for scripts";
	}

	public String getName() {
		return "script";
	}
	
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
		if (ScriptController.Instance.loadClientScripts()) {
			sender.sendMessage(new TextComponentString("Reload client scripts succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading client scripts"));
		}
		if (ScriptController.Instance.loadConstantData()) {
			sender.sendMessage(new TextComponentString("Reload constant data succesfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading constant data"));
		}
		if (server!=null) {
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				ScriptController.Instance.sendClientTo(player);
			}
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
	
	@SubCommand(desc = "Attempts to execute on the specified object", usage = "<dimentionID> <x> <y> <z> <entity> <trigerID> [Strings]")
	public Boolean trigger(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		IWorld world = null;
		IPos pos = null;
		IEntity<?> entity = null;
		int id = 0;
		try {
			int dimID  = Integer.parseInt(args[0]);
			if (!DimensionManager.isDimensionRegistered(dimID) || DimensionHandler.getInstance().isDelete(dimID)) {
				throw new CommandException("DimensionID: "+dimID+" - not found");
			}
			world = NpcAPI.Instance().getIWorld(dimID);
		} catch (NumberFormatException ex) {
			throw new CommandException("DimensionID must be an integer");
		}
		try {
			double dx = parseCoordinate(sender.getPosition().getX(), args[1], true).getResult();
			double dy = parseCoordinate(sender.getPosition().getY(), args[2], 0, 255, false).getResult();
			double dz = parseCoordinate(sender.getPosition().getZ(), args[3], true).getResult();
			pos = NpcAPI.Instance().getIPos(dx, dy, dz);
		}
		catch (NumberFormatException ex) { }
		IEntity<?>[] entitys = world.getNearbyEntities(pos, 2, 0);
		for (IEntity<?> e : entitys) {
			if (args[4].equalsIgnoreCase("player") && e.getType()==1 || e.getName().equalsIgnoreCase(args[4])) {
				entity = e; 
				break;
			}
		}
		try { id  = Integer.parseInt(args[5]); }
		catch (NumberFormatException ex) { throw new CommandException("TrigerID must be an integer"); }
		String[] arguments = new String[args.length-6];
		for (int i=0; i<args.length-6; i++) {
			arguments[i] = args[6+i];
		}
		if (entity==null) {
			TileEntity tile = world.getMCWorld().getTileEntity(pos.getMCBlockPos());
			if (tile instanceof TileScripted) {
				EventHooks.onScriptTriggerEvent((TileScripted) tile, id, world, pos, null, arguments);
				return true;
			}
		}
		EventHooks.onScriptTriggerEvent(id, world, pos, entity, arguments);
		return true;
	}
	
}
