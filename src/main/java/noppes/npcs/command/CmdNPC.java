package noppes.npcs.command;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.command.PlayerNotFoundException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleFollower;

public class CmdNPC extends CommandNoppesBase {
	public EntityNPCInterface selectedNpc;

	@SubCommand(desc = "Creates an NPC", usage = "[name]")
	public void create(MinecraftServer server, ICommandSender sender, String[] args) {
		World pw = sender.getEntityWorld();
		EntityCustomNpc npc = new EntityCustomNpc(pw);
		if (args.length > 0) {
			npc.display.setName(args[0]);
		}
		BlockPos pos = sender.getPosition();
		npc.setPositionAndRotation(pos.getX(), pos.getY(), pos.getZ(), 0.0f, 0.0f);
		npc.ais.setStartPos(pos);
		pw.spawnEntity(npc);
		npc.setHealth(npc.getMaxHealth());
	}

	@SubCommand(desc = "Delete an NPC")
	public void delete(MinecraftServer server, ICommandSender sender, String[] args) {
		this.selectedNpc.delete();
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String npcname = args[0].replace("%", " ");
		String command = args[1];
		args = Arrays.copyOfRange(args, 2, args.length);
		if (command.equalsIgnoreCase("create")) {
			args = (String[]) ArrayUtils.add((Object[]) args, 0, npcname);
			this.executeSub(server, sender, command, args);
			return;
		}
		List<EntityNPCInterface> list = this.getEntities((Class<? extends EntityNPCInterface>) EntityNPCInterface.class,
				sender.getEntityWorld(), sender.getPosition(), 80);
		for (EntityNPCInterface npc : list) {
			String name = npc.display.getName().replace(" ", "_");
			if (name.equalsIgnoreCase(npcname) && (this.selectedNpc == null || this.selectedNpc
					.getDistanceSq(sender.getPosition()) > npc.getDistanceSq(sender.getPosition()))) {
				this.selectedNpc = npc;
			}
		}
		if (this.selectedNpc == null) {
			throw new CommandException("Npc '%s' was not found", new Object[] { npcname });
		}
		this.executeSub(server, sender, command, args);
		this.selectedNpc = null;
	}

	@Override
	public String getDescription() {
		return "NPC operation";
	}

	@SuppressWarnings("unchecked")
	public <T extends Entity> List<T> getEntities(Class<? extends T> cls, World world, BlockPos pos, int range) {
		return (List<T>) world.getEntitiesWithinAABB(cls,
				new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(range, range, range));
	}

	public String getName() {
		return "npc";
	}

	@Override
	public int getRequiredPermissionLevel() {
		return 4;
	}

	public List<String> getTabCompletions(MinecraftServer server, ICommandSender par1, String[] args, BlockPos pos) {
		if (args.length == 2) {
			return CommandBase.getListOfStringsMatchingLastWord(args,
					new String[] { "create", "home", "visible", "delete", "owner", "name" });
		}
		if (args.length == 3 && args[1].equalsIgnoreCase("owner")) {
			return CommandBase.getListOfStringsMatchingLastWord(args, server.getOnlinePlayerNames());
		}
		return null;
	}

	@Override
	public String getUsage() {
		return "<name> <command>";
	}

	@SubCommand(desc = "Set Home (respawn place)", usage = "[x] [y] [z]", permission = 2)
	public void home(MinecraftServer server, ICommandSender sender, String[] args) {
		BlockPos pos = sender.getPosition();
		if (args.length == 3) {
			try {
				pos = CommandBase.parseBlockPos(sender, args, 0, false);
			} catch (NumberInvalidException ex) {
			}
		}
		this.selectedNpc.ais.setStartPos(pos);
	}

	@SubCommand(desc = "Set NPC name", usage = "[name]", permission = 2)
	public void name(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length < 1) {
			return;
		}
		String name = args[0];
		for (int i = 1; i < args.length; ++i) {
			name = name + " " + args[i];
		}
		if (!this.selectedNpc.display.getName().equals(name)) {
			this.selectedNpc.display.setName(name);
			this.selectedNpc.updateClient = true;
		}
	}

	@SubCommand(desc = "Sets the owner of an follower/companion", usage = "[player]", permission = 2)
	public void owner(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length < 1) {
			EntityPlayer player = null;
			if (this.selectedNpc.roleInterface instanceof RoleFollower) {
				player = ((RoleFollower) this.selectedNpc.roleInterface).owner;
			}
			if (this.selectedNpc.roleInterface instanceof RoleCompanion) {
				player = ((RoleCompanion) this.selectedNpc.roleInterface).owner;
			}
			if (player == null) {
				this.sendMessage(sender, "No owner", new Object[0]);
			} else {
				this.sendMessage(sender, "Owner is: " + player.getName(), new Object[0]);
			}
		} else {
			EntityPlayerMP player2 = null;
			try {
				player2 = CommandBase.getPlayer(server, sender, args[0]);
			} catch (PlayerNotFoundException ex) {
			} catch (CommandException ex2) {
			}
			if (this.selectedNpc.roleInterface instanceof RoleFollower) {
				((RoleFollower) this.selectedNpc.roleInterface).setOwner((EntityPlayer) player2);
			}
			if (this.selectedNpc.roleInterface instanceof RoleCompanion) {
				((RoleCompanion) this.selectedNpc.roleInterface).setOwner((EntityPlayer) player2);
			}
		}
	}

	@SubCommand(desc = "Resets the npc", usage = "[name]", permission = 2)
	public void reset(MinecraftServer server, ICommandSender sender, String[] args) {
		this.selectedNpc.reset();
	}

	@Override
	public boolean runSubCommands() {
		return false;
	}

	@SubCommand(desc = "Set NPC visibility", usage = "[true/false/semi]", permission = 2)
	public void visible(MinecraftServer server, ICommandSender sender, String[] args) {
		if (args.length < 1) {
			return;
		}
		boolean bo = args[0].equalsIgnoreCase("true");
		boolean semi = args[0].equalsIgnoreCase("semi");
		if (semi) {
			this.selectedNpc.display.setVisible(2);
		} else if (bo) {
			this.selectedNpc.display.setVisible(0);
		} else {
			this.selectedNpc.display.setVisible(1);
		}
	}
}
