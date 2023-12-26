package noppes.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumSync;

public class CmdDebug extends CommandNoppesBase {

	@Override
	public String getDescription() {
		return "Debug control";
	}

	@Override
	public String getName() {
		return "debug";
	}
	
	@SubCommand(desc = "Enable or disable debugging")
	public void activate(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		CustomNpcs.VerboseDebug = !CustomNpcs.VerboseDebug;
		if (sender instanceof EntityPlayerMP) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setBoolean("debug", CustomNpcs.VerboseDebug);
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_UPDATE, EnumSync.Debug, nbt);
		}
		sender.sendMessage(new TextComponentTranslation("command.debug."+CustomNpcs.VerboseDebug));
	}

	@SubCommand(desc = "Enable or disable debugging")
	public void report(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		CustomNpcs.showDebugs();
		if (sender instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) sender, EnumPacketClient.SYNC_ADD, EnumSync.Debug, new NBTTagCompound());
		}
		sender.sendMessage(new TextComponentTranslation("command.debug.show"));
	}
	
}
