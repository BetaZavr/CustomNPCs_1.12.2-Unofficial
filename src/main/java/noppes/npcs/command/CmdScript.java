package noppes.npcs.command;

import java.util.*;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.blocks.tiles.TileScripted;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class CmdScript extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 4;
	}

	@SubCommand(desc = "List of available event names from all APIs in mod")
	public Boolean apilist(MinecraftServer server, ICommandSender sender, String[] args) {
		StringBuilder list = new StringBuilder();
		List<String> g = new ArrayList<>();
		for (EnumScriptType est : EnumScriptType.values()) { g.add(est.function); }
		Collections.sort(g);
		for (String name : g) {
			if (list.length() > 0) {
				list.append(", ");
			} else {
				list.append(((char) 167) + "6Mod APIs event names:\n" + ((char) 167) + "r");
			}
			list.append(name);
		}
		list.append(";\n" + ((char) 167) + "6Total Size: " + ((char) 167) + "e").append(g.size());
		sender.sendMessage(new TextComponentString(list.toString()));
		if (sender instanceof EntityPlayerMP) { Server.sendData((EntityPlayerMP) sender, EnumPacketClient.EVENT_NAMES, list.toString()); }
		return true;
	}

	@SubCommand(desc = "List of available Forge event names")
	public Boolean clientlist(MinecraftServer server, ICommandSender sender, String[] args) {
		StringBuilder list = new StringBuilder();
		List<String> g = new ArrayList<>(CustomNpcs.forgeClientEventNames.values());
		Collections.sort(g);
		for (String name : g) {
			if (list.length() > 0) {
				list.append(", ");
			} else {
				list.append(((char) 167) + "6Client forge event names:\n" + ((char) 167) + "r");
			}
			list.append(name);
		}
		list.append(";\n" + ((char) 167) + "6Total Size: " + ((char) 167) + "e").append(g.size());
		sender.sendMessage(new TextComponentString(list.toString()));
		if (sender instanceof EntityPlayerMP) { Server.sendData((EntityPlayerMP) sender, EnumPacketClient.EVENT_NAMES, list.toString()); }
		return true;
	}

	@SubCommand(desc = "List of available Forge event names")
	public Boolean forgelist(MinecraftServer server, ICommandSender sender, String[] args) {
		StringBuilder list = new StringBuilder();
		List<String> g = new ArrayList<>(CustomNpcs.forgeEventNames.values());
		Collections.sort(g);
		for (String name : g) {
			if (list.length() > 0) {
				list.append(", ");
			} else {
				list.append(((char) 167) + "6Server Forge event names:\n" + ((char) 167) + "r");
			}
			list.append(name);
		}
		list.append(";\n" + ((char) 167) + "6Total Size: " + ((char) 167) + "e").append(g.size());
		sender.sendMessage(new TextComponentString(list.toString()));
		if (sender instanceof EntityPlayerMP) { Server.sendData((EntityPlayerMP) sender, EnumPacketClient.EVENT_NAMES, list.toString()); }
		return true;
	}

	@SubCommand(desc = "Displays all script owners that have logs.")
	public Boolean logs(MinecraftServer server, ICommandSender sender, String[] args) {
		Map<String, ITextComponent> map = new LinkedHashMap<>();
	 	for (ScriptContainer container : ScriptController.Instance.getErrored()) {
			ITextComponent message = container.noticeString();
			map.put(Util.instance.deleteColor(message.getFormattedText()), message);
		}
		if (map.isEmpty()) {
			sender.sendMessage(new TextComponentTranslation("command.script.logs.empty"));
		} else {
			sender.sendMessage(new TextComponentTranslation("command.script.logs.info"));
			for (ITextComponent message : map.values()) {
				sender.sendMessage(message);
			}
		}
		sender.sendMessage(new TextComponentTranslation("command.script.logs.end"));
		return true;
	}

	@Override
	public String getDescription() {
		return "Commands for scripts";
	}

	@Nonnull
	public String getName() {
		return "script";
	}

	@SubCommand(desc = "Reload scripts and saved data from disks script folder.")
	public Boolean reload(MinecraftServer server, ICommandSender sender, String[] args) {
		ScriptController.Instance.loadCategories();
		if (ScriptController.Instance.loadPlayerScripts()) {
			sender.sendMessage(new TextComponentString("Reload player scripts successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading player scripts"));
		}
		if (ScriptController.Instance.loadNPCsScripts()) {
			sender.sendMessage(new TextComponentString("Reload NPCs scripts successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading NPCs scripts"));
		}
		if (ScriptController.Instance.loadForgeScripts()) {
			sender.sendMessage(new TextComponentString("Reload forge scripts successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading forge scripts"));
		}
		if (ScriptController.Instance.loadClientScripts()) {
			sender.sendMessage(new TextComponentString("Reload client scripts successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading client scripts"));
		}
		if (ScriptController.Instance.loadPotionScripts()) {
			sender.sendMessage(new TextComponentString("Reload potion scripts successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading potion scripts"));
		}
		if (ScriptController.Instance.loadConstantData()) {
			sender.sendMessage(new TextComponentString("Reload constant data successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading constant data"));
		}
		if (ScriptController.Instance.loadStoredData()) {
			sender.sendMessage(new TextComponentString("Reload stored data successfully"));
		} else {
			sender.sendMessage(new TextComponentString("Failed reloading stored data"));
		}
		if (server != null) {
			for (EntityPlayerMP player : server.getPlayerList().getPlayers()) {
				ScriptController.Instance.sendClientTo(player);
			}
		}
		return true;
	}

	@SubCommand(desc = "Runs scriptCommand in the players scripts", usage = "[args]")
	public Boolean run(MinecraftServer server, ICommandSender sender, String[] args) {
		IWorld world = Objects.requireNonNull(NpcAPI.Instance()).getIWorld(sender.getEntityWorld());
		BlockPos bpos = sender.getPosition();
		IPos pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(bpos.getX(), bpos.getY(), bpos.getZ());
		WorldEvent.ScriptCommandEvent event = new WorldEvent.ScriptCommandEvent(world, pos, args);
		EventHooks.onWorldScriptEvent(event);
		return true;
	}

	@SubCommand(desc = "Attempts to execute on the specified object", usage = "<dimensionID> <x> <y> <z> <entity> <triggerID> [Strings]")
	public Boolean trigger(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		IWorld world;
		IPos pos = null;
		IEntity<?> entity = null;
		int id;
		try {
			int dimID = Integer.parseInt(args[0]);
			if (!DimensionManager.isDimensionRegistered(dimID) || DimensionHandler.getInstance().isDelete(dimID)) {
				throw new CommandException("DimensionID: " + dimID + " - not found");
			}
			world = Objects.requireNonNull(NpcAPI.Instance()).getIWorld(dimID);
		} catch (NumberFormatException ex) {
			throw new CommandException("DimensionID \"" + args[0] + "\" - must be an integer ");
		}
		try {
			double dx = parseCoordinate(sender.getPosition().getX(), args[1], true).getResult();
			double dy = parseCoordinate(sender.getPosition().getY(), args[2], 0, 255, false).getResult();
			double dz = parseCoordinate(sender.getPosition().getZ(), args[3], true).getResult();
			pos = Objects.requireNonNull(NpcAPI.Instance()).getIPos(dx, dy, dz);
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		IEntity<?>[] entitys = world.getNearbyEntities(pos, 2, 0);
		for (IEntity<?> e : entitys) {
			if (args[4].equalsIgnoreCase("player") && e.getType() == 1 || e.getName().equalsIgnoreCase(args[4])) {
				entity = e;
				break;
			}
		}
		try {
			id = Integer.parseInt(args[5]);
		} catch (NumberFormatException ex) {
			throw new CommandException("TriggerID \"" + args[0] + "\" must be an integer");
		}
		Object[] arguments = new String[args.length - 6];
        System.arraycopy(args, 6, arguments, 0, args.length - 6);
		if (entity == null) {
            assert pos != null;
            TileEntity tile = world.getMCWorld().getTileEntity(pos.getMCBlockPos());
			if (tile instanceof TileScripted) {
				EventHooks.onScriptTriggerEvent((TileScripted) tile, id, world, pos, null, arguments);
				return true;
			}
		}
		EventHooks.onScriptTriggerEvent(id, world, pos, entity, arguments);
		return true;
	}

	@Override
	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, BlockPos pos) {
		List<String> list = new ArrayList<>();
		if (args.length == 2) {
            switch (args[0]) {
                case "clientlist":
                    return new ArrayList<>(CustomNpcs.forgeClientEventNames.values());
                case "forgelist":
                    return new ArrayList<>(CustomNpcs.forgeEventNames.values());
                case "apilist":
                    for (EnumScriptType est : EnumScriptType.values()) {
                        list.add(est.function);
                    }
                    return list;
            }
		}
		return list;
	}

}
