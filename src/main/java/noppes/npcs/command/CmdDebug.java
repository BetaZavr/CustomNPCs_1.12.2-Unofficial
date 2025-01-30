package noppes.npcs.command;

import java.util.List;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;
import noppes.npcs.util.CustomNPCsScheduler;

import javax.annotation.Nonnull;

public class CmdDebug extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 4;
	}

	@SubCommand(desc = "Enable or disable debugging")
	public void activate(MinecraftServer server, ICommandSender sender, String[] args) {
		CustomNpcs.VerboseDebug = !CustomNpcs.VerboseDebug;
		if (sender instanceof EntityPlayerMP) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("debug", CustomNpcs.VerboseDebug);
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_UPDATE, EnumSync.Debug, nbt);
		}
		sender.sendMessage(new TextComponentTranslation("command.debug." + CustomNpcs.VerboseDebug));
	}

	@SubCommand(desc = "Delete monitoring data")
	public void clear(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		CustomNpcs.debugData.clear();
		if (sender instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_REMOVE, EnumSync.Debug,
					new NBTTagCompound());
		}
		sender.sendMessage(new TextComponentTranslation("command.debug.clear"));
	}

	@Override
	public String getDescription() {
		return "Debug control";
	}

	@Nonnull
	@Override
	public String getName() {
		return "debug";
	}

	@SubCommand(desc = "Will display the current mod debug report")
	public void report(MinecraftServer server, ICommandSender sender, String[] args) {
		List<String> list = CustomNpcs.showDebugs();
		if (sender instanceof EntityPlayerMP) {
			CustomNPCsScheduler.runTack(() -> Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_ADD, EnumSync.Debug, new NBTTagCompound()), 500);
		}
		for (String str : list) {
			sender.sendMessage(new TextComponentString(str));
		}
		try {
			Class<?> nirn = Class.forName("nirn.betazavr.Nirn");
			Object nirnMod = nirn.getField("instance").get(null);
			List<String> nirnList = (List<String>) nirn.getMethod("showDebugs").invoke(nirnMod);
			for (String str : nirnList) {
				sender.sendMessage(new TextComponentString(str));
			}
		}
		catch (Exception ignored) { }
        sender.sendMessage(new TextComponentTranslation("command.debug.show"));
	}

}
