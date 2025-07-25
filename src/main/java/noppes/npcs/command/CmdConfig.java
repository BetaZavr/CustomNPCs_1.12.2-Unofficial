package noppes.npcs.command;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import net.minecraft.block.Block;
import net.minecraft.block.BlockIce;
import net.minecraft.block.BlockLeaves;
import net.minecraft.block.BlockVine;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class CmdConfig extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@Override
	public String getDescription() {
		return "Some config things you can set";
	}

	@Nonnull
	public String getName() {
		return "config";
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Set how many active chunkloaders you can have", usage = "<number>", permission = 4)
	public void chunkloaders(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length == 0) {
			this.sendMessage(sender, "ChunkLoaders: " + ChunkController.instance.size() + "/" + CustomNpcs.ChuckLoaders);
		} else {
			try {
				CustomNpcs.ChuckLoaders = Integer.parseInt(args[0]);
			} catch (NumberFormatException ex) {
				throw new CommandException("Didn't get a number");
			}
			CustomNpcs.Config.resetConfig();
			int size = ChunkController.instance.size();
			if (size > CustomNpcs.ChuckLoaders) {
				ChunkController.instance.unload(size - CustomNpcs.ChuckLoaders);
				this.sendMessage(sender, size - CustomNpcs.ChuckLoaders + " chunksloaders unloaded");
			}
			this.sendMessage(sender, "ChunkLoaders: " + ChunkController.instance.size() + "/" + CustomNpcs.ChuckLoaders);
		}
	}

	@SubCommand(desc = "Add debug info to log", usage = "<true/false>", permission = 4)
	public void debug(MinecraftServer server, ICommandSender sender, String[] args) {
		CustomNpcs.VerboseDebug = Boolean.parseBoolean(args[0]);
		sender.sendMessage(new TextComponentTranslation("command.debug." + CustomNpcs.VerboseDebug));
	}

	@SubCommand(desc = "Delete monitoring data", permission = 4)
	public void clear(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		CustomNpcs.debugData.clear();
		if (sender instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_REMOVE, EnumSync.Debug,
					new NBTTagCompound());
		}
		sender.sendMessage(new TextComponentTranslation("command.debug.clear"));
	}

	@SubCommand(desc = "Will display the current mod debug report", permission = 2)
	public void report(MinecraftServer server, ICommandSender sender, String[] args) {
		List<String> list = CustomNpcs.debugData.logging();
		if (!list.isEmpty()) {
			sender.sendMessage(new TextComponentTranslation("Server info:"));
			for (String str : list) { sender.sendMessage(new TextComponentString(str)); }
		}
		if (sender instanceof EntityPlayerMP && (CustomNpcs.Server == null || !CustomNpcs.Server.isSinglePlayer())) {
			sender.sendMessage(new TextComponentString("Client info:"));
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_ADD, EnumSync.Debug, new NBTTagCompound());
		}
		CustomNPCsScheduler.runTack(() -> sender.sendMessage(new TextComponentTranslation("command.debug.show")), 1000);
	}

	@SubCommand(desc = "Get/Set font", usage = "[type] [size]", permission = 2)
	public void font(MinecraftServer server, ICommandSender sender, String[] args) {
		if (!(sender instanceof EntityPlayerMP)) {
			return;
		}
		int size = 18;
		if (args.length > 1) {
			try {
				size = Integer.parseInt(args[args.length - 1]);
				args = Arrays.copyOfRange(args, 0, args.length - 1);
			} catch (Exception e) { LogWriter.error("Error:", e); }
		}
		StringBuilder font = new StringBuilder();
        for (String arg : args) {
            font.append(" ").append(arg);
        }
		Server.sendData((EntityPlayerMP) sender, EnumPacketClient.CONFIG_FONT, 0, font.toString().trim(), size);
	}

	@SubCommand(desc = "Freezes/Unfreezes npcs", usage = "[true/false]", permission = 4)
	public void freezenpcs(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "Frozen NPCs: " + CustomNpcs.FreezeNPCs);
		} else {
			CustomNpcs.FreezeNPCs = Boolean.parseBoolean(args[0]);
			this.sendMessage(sender, "FrozenNPCs is now " + CustomNpcs.FreezeNPCs);
		}
	}

	@SubCommand(desc = "Disable/Enable the ice melting", usage = "[true/false]", permission = 4)
	public void icemelts(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "IceMelts: " + CustomNpcs.IceMeltsEnabled);
		} else {
			CustomNpcs.IceMeltsEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.resetConfig();
			Set<ResourceLocation> names = Block.REGISTRY.getKeys();
			for (ResourceLocation name : names) {
				Block block = Block.REGISTRY.getObject(name);
				if (block instanceof BlockIce) {
					block.setTickRandomly(CustomNpcs.IceMeltsEnabled);
				}
			}
			this.sendMessage(sender, "IceMelts is now " + CustomNpcs.IceMeltsEnabled);
		}
	}

	@SubCommand(desc = "Disable/Enable the natural leaves decay", usage = "[true/false]", permission = 4)
	public void leavesdecay(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "LeavesDecay: " + CustomNpcs.LeavesDecayEnabled);
		} else {
			CustomNpcs.LeavesDecayEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.resetConfig();
			Set<ResourceLocation> names = Block.REGISTRY.getKeys();
			for (ResourceLocation name : names) {
				Block block = Block.REGISTRY.getObject(name);
				if (block instanceof BlockLeaves) {
					block.setTickRandomly(CustomNpcs.LeavesDecayEnabled);
				}
			}
			this.sendMessage(sender, "LeavesDecay is now " + CustomNpcs.LeavesDecayEnabled);
		}
	}

	@SubCommand(desc = "Enables/Disables scripting", usage = "[true/false]", permission = 4)
	public void scripting(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "Scripting: " + CustomNpcs.EnableScripting);
		} else {
			CustomNpcs.EnableScripting = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.resetConfig();
			this.sendMessage(sender, "Scripting is now " + CustomNpcs.EnableScripting);
		}
	}

	@SubCommand(desc = "Disable/Enable the vines growing", usage = "[true/false]", permission = 4)
	public void vinegrowth(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length == 0) {
			this.sendMessage(sender, "VineGrowth: " + CustomNpcs.VineGrowthEnabled);
		} else {
			CustomNpcs.VineGrowthEnabled = Boolean.parseBoolean(args[0]);
			CustomNpcs.Config.resetConfig();
			Set<ResourceLocation> names = Block.REGISTRY.getKeys();
			for (ResourceLocation name : names) {
				Block block = Block.REGISTRY.getObject(name);
				if (block instanceof BlockVine) {
					block.setTickRandomly(CustomNpcs.VineGrowthEnabled);
				}
			}
			this.sendMessage(sender, "VineGrowth is now " + CustomNpcs.VineGrowthEnabled);
		}
	}

}
