package noppes.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.Server;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;

import javax.annotation.Nonnull;

public class CmdAccepts extends CommandNoppesBase {

    @SubCommand(desc = "Open the agreement settings window")
    public void script(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
        if (!(sender instanceof EntityPlayerMP)) { return; }
        Server.sendData((EntityPlayerMP) sender, EnumPacketClient.GUI, EnumGuiType.AcceptScripts, 3, 0, 0);
    }

    @Override
    public String getDescription() {
        return "Player Agreements";
    }

    @Nonnull
    @Override
    public String getName() {
        return "accept";
    }

}
