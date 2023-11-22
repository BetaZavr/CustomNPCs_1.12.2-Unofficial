package noppes.npcs.command;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;

public class CmdPlayers extends CommandNoppesBase {

	@Override
	public String getDescription() {
		return "Player mod data";
	}

	@Override
	public String getName() {
		return "player";
	}
	
	@SubCommand(desc = "Shows the player's virtual currency balance", usage = "<playername>")
	public void getmoney(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Object[] objs = this.getPlayerData(server, args[0]);
		PlayerData playerdata = (PlayerData) objs[0];
		boolean isOnline = objs[1] != null;
		if (playerdata==null) {
			throw new PlayerNotFoundException("commands.generic.player.notFound", new Object[] {args[0]});
		}
		sender.sendMessage(new TextComponentTranslation("command.player.getmoney", playerdata.playername, ""+playerdata.game.getMoney(), ""+CustomNpcs.charCurrencies.charAt(0)).appendSibling(new TextComponentTranslation(isOnline ? "gui.online" : "gui.offline")));
	}

	@SubCommand(desc = "Change the player's virtual currency balance", usage = "<playername> <value>")
	public void addmoney(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		Object[] objs = this.getPlayerData(server, args[0]);
		PlayerData playerdata = (PlayerData) objs[0];
		boolean isOnline = objs[1] != null;
		if (playerdata==null) {
			throw new PlayerNotFoundException("commands.generic.player.notFound", new Object[] {args[0]});
		}
		try {
			long money = Long.parseLong(args[1]);
			playerdata.game.addMoney(money);
			sender.sendMessage(new TextComponentTranslation("command.player."+(money>=0 ? "add" : "del")+"money", playerdata.playername, ""+money, ""+playerdata.game.getMoney(), ""+CustomNpcs.charCurrencies.charAt(0)).appendSibling(new TextComponentTranslation(isOnline ? "gui.online" : "gui.offline")));
		}
		catch (Exception e) {
			
		}
	}
	
	private Object[] getPlayerData(MinecraftServer server, String playername) {
		EntityPlayer pl = null;
		for (EntityPlayerMP entityplayermp : server.getPlayerList().getPlayers()) {
            if (entityplayermp.getName().equalsIgnoreCase(playername)) {
            	pl = entityplayermp;
            	break;
            }
        }
		PlayerData playerdata = null;
		if (pl != null) { playerdata = PlayerData.get(pl); }
		else { playerdata = PlayerDataController.instance.getDataFromUsername(server, playername); }
		return new Object[] { playerdata, pl};
	}
	
	public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, BlockPos pos) {
		List<String> list = Lists.<String>newArrayList();
		if (args.length==2) {
			list = Lists.newArrayList(PlayerDataController.instance.nameUUIDs.keySet());
		}
		return list;
	}
	
}
