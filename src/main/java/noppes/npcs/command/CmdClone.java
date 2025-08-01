package noppes.npcs.command;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.state.IBlockState;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.command.NumberInvalidException;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.LogWriter;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class CmdClone extends CommandNoppesBase {

	public int getRequiredPermissionLevel() {
		return 2;
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Add NPC(s) to clone storage", usage = "<npc> <tab> [clonedname]", permission = 2)
	public void add(MinecraftServer server, ICommandSender sender, String[] args) {
		int tab = 0;
		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) { LogWriter.error(e); }
		List<EntityNPCInterface> list = this.getEntities(EntityNPCInterface.class,
				sender.getEntityWorld(), sender.getPosition(), 80);
		for (EntityNPCInterface npc : list) {
			if (npc.display.getName().equalsIgnoreCase(args[0])) {
				String name = npc.display.getName();
				if (args.length > 2) {
					name = args[2];
				}
				NBTTagCompound compound = new NBTTagCompound();
				if (!npc.writeToNBTAtomically(compound)) {
					return;
				}
				ServerCloneController.Instance.addClone(compound, name, tab);
			}
		}
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Remove NPC from clone storage", usage = "<name> <tab>", permission = 2)
	public void del(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String nameModel = args[0];
		int tab = 0;
		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) { LogWriter.error(e); }
		for (String name : ServerCloneController.Instance.getClones(tab)) {
			if (nameModel.equalsIgnoreCase(name)) {
				ServerCloneController.Instance.removeClone(name, tab);
				break;
			}
		}
		if (!ServerCloneController.Instance.removeClone(nameModel, tab)) {
			throw new CommandException("Npc '%s' wasn't found", nameModel);
		}
	}

	@Override
	public String getDescription() {
		return "Clone operation (server side)";
	}

	public <T extends Entity> List<T> getEntities(Class<? extends T> cls, World world, BlockPos pos, int range) {
		List<T> list = new ArrayList<>();
		try {
			list = world.getEntitiesWithinAABB(cls, new AxisAlignedBB(pos, pos.add(1, 1, 1)).grow(range, range, range));
		}
		catch (Exception ignored) { }
		return list;
	}

	@Nonnull
	public String getName() {
		return "clone";
	}

	public World getWorld(MinecraftServer server, String t) {
		WorldServer[] worlds;
		worlds = server.worlds;
		for (WorldServer w : worlds) {
			if (w != null && (w.provider.getDimension() + "").equalsIgnoreCase(t)) {
				return w;
			}
		}
		return null;
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Spawn multiple cloned NPC in a grid", usage = "<name> <tab> <length> <width> [[world:]x,y,z]] [newname]", permission = 2)
	public boolean grid(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String name = args[0].replaceAll("%", " ");
		int tab = 0;
		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) { LogWriter.error(e); }
		int width;
		int height;
		try {
			width = Integer.parseInt(args[2]);
			height = Integer.parseInt(args[3]);
		} catch (NumberFormatException ex) {
			throw new CommandException("Length or width want a number");
		}
		String newname = null;
		NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
		if (compound == null) {
			throw new CommandException("Unknown npc");
		}
		World world = sender.getEntityWorld();
		BlockPos curpos = sender.getPosition();
		if (args.length > 4) {
			String location = args[4];
			if (location.contains(":")) {
				String[] par = location.split(":");
				location = par[1];
				world = this.getWorld(server, par[0]);
				if (world == null) {
					throw new CommandException("'%s' is an unknown world", par[0]);
				}
			}
			if (location.contains(",")) {
				String[] par = location.split(",");
				if (par.length != 3) {
					throw new CommandException("Location need be x,y,z");
				}
				try {
					curpos = CommandBase.parseBlockPos(sender, par, 0, false);
				} catch (NumberInvalidException e) {
					throw new CommandException("Location should be in numbers");
				}
				if (args.length > 5) {
					newname = args[5];
				}
			} else {
				newname = location;
			}
		}
		if (curpos.getX() == 0 && curpos.getY() == 0 && curpos.getZ() == 0) {
			throw new CommandException("Location needed");
		}
		for (int x = 0; x < width; ++x) {
			for (int z = 0; z < height; ++z) {
				BlockPos npcpos = curpos.add(x, -2, z);
				for (int y = 0; y < 10; ++y) {
					BlockPos pos = npcpos.up(y);
					BlockPos pos2 = pos.up();
					IBlockState b = world.getBlockState(pos);
					IBlockState b2 = world.getBlockState(pos2);
					if (b.causesSuffocation() && !b2.causesSuffocation()) {
						npcpos = pos;
						break;
					}
				}
				Entity entity = EntityList.createEntityFromNBT(compound, world);
				if (entity != null) {
					entity.setPosition(npcpos.getX() + 0.5, (npcpos.getY() + 1), npcpos.getZ() + 0.5);
					if (entity instanceof EntityNPCInterface) {
						EntityNPCInterface npc = (EntityNPCInterface) entity;
						npc.ais.setStartPos(npcpos);
						if (newname != null && !newname.isEmpty()) {
							npc.display.setName(newname.replaceAll("%", " "));
						}
					}
					world.spawnEntity(entity);
				}
			}
		}
		return true;
	}

	@SubCommand(desc = "List NPC from clone storage", usage = "<tab>", permission = 2)
	public void list(MinecraftServer server, ICommandSender sender, String[] args) {
		this.sendMessage(sender, "--- Stored NPCs --- (server side)");
		int tab = 0;
		try {
			tab = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) { LogWriter.error(e); }
		for (String name : ServerCloneController.Instance.getClones(tab)) {
			this.sendMessage(sender, name);
		}
		this.sendMessage(sender, "------------------------------------");
	}

	@SuppressWarnings("all")
	@SubCommand(desc = "Spawn cloned NPC", usage = "<name> <tab> [[world:]x,y,z]] [newname]", permission = 2)
	public void spawn(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		String name = args[0].replaceAll("%", " ");
		int tab = 0;
		try {
			tab = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) { LogWriter.error(e); }
		String newname = null;
		NBTTagCompound compound = ServerCloneController.Instance.getCloneData(sender, name, tab);
		if (compound == null) {
			throw new CommandException("Unknown npc");
		}
		World world = sender.getEntityWorld();
		BlockPos pos = sender.getPosition();
		if (args.length > 2) {
			String location = args[2];
			if (location.contains(":")) {
				String[] par = location.split(":");
				location = par[1];
				world = this.getWorld(server, par[0]);
				if (world == null) {
					throw new CommandException("'%s' is an unknown world", par[0]);
				}
			}
			if (location.contains(",")) {
				String[] par = location.split(",");
				if (par.length != 3) {
					throw new CommandException("Location need be x,y,z");
				}
				try {
					pos = CommandBase.parseBlockPos(sender, par, 0, false);
				} catch (NumberInvalidException e) {
					throw new CommandException("Location should be in numbers");
				}
				if (args.length > 3) {
					newname = args[3];
				}
			} else {
				newname = location;
			}
		}
		if (pos.getX() == 0 && pos.getY() == 0 && pos.getZ() == 0) {
			throw new CommandException("Location needed");
		}
		Entity entity = EntityList.createEntityFromNBT(compound, world);
		if (entity != null) {
			entity.setPosition(pos.getX() + 0.5, (pos.getY() + 1), pos.getZ() + 0.5);
			if (entity instanceof EntityNPCInterface) {
				EntityNPCInterface npc = (EntityNPCInterface) entity;
				npc.ais.setStartPos(pos);
				if (newname != null && !newname.isEmpty()) {
					npc.display.setName(newname.replaceAll("%", " "));
				}
			}
			world.spawnEntity(entity);
		} else {
			throw new CommandException("Unknown entity: '%s'", compound.getString("id"));
		}
	}
}
