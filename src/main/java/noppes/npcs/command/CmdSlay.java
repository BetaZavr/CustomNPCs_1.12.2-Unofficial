package noppes.npcs.command;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.monster.EntityGhast;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityHorse;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.api.CommandNoppesBase;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class CmdSlay extends CommandNoppesBase {

	public Map<String, Class<?>> slayMap = Maps.newHashMap();

	public CmdSlay() {
        this.slayMap.put("all", EntityLivingBase.class);
		this.slayMap.put("mobs", EntityMob.class);
		this.slayMap.put("animals", EntityAnimal.class);
		this.slayMap.put("items", EntityItem.class);
		this.slayMap.put("xporbs", EntityXPOrb.class);
		this.slayMap.put("npcs", EntityNPCInterface.class);
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			String name = ent.getName();
			Class<? extends Entity> cls = ent.getEntityClass();
			if (EntityNPCInterface.class.isAssignableFrom(cls)) {
				continue;
			}
			if (!EntityLivingBase.class.isAssignableFrom(cls)) {
				continue;
			}
			this.slayMap.put(name.toLowerCase(), cls);
		}
		this.slayMap.remove("monster");
		this.slayMap.remove("mob");
	}

	private boolean delete(Entity entity, ArrayList<Class<?>> toDelete) {
		for (Class<?> delete : toDelete) {
			if (delete == EntityAnimal.class && entity instanceof EntityHorse) {
				continue;
			}
			if (delete.isAssignableFrom(entity.getClass())) {
				return entity.isDead = true;
			}
		}
		return false;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		ArrayList<Class<?>> toDelete = new ArrayList<>();
		boolean deleteNPCs = false;
		for (String delete : args) {
			delete = delete.toLowerCase();
			Class<?> cls = this.slayMap.get(delete);
			if (cls != null) {
				toDelete.add(cls);
			}
			if (delete.equals("mobs")) {
				toDelete.add(EntityGhast.class);
				toDelete.add(EntityDragon.class);
			}
			if (delete.equals("npcs")) {
				deleteNPCs = true;
			}
		}
		int count = 0;
		int range = 120;
		try {
			range = Integer.parseInt(args[args.length - 1]);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		AxisAlignedBB box = new AxisAlignedBB(sender.getPosition(), sender.getPosition().add(1, 1, 1)).grow(range, range, range);
		List<? extends Entity> list = sender.getEntityWorld().getEntitiesWithinAABB(EntityLivingBase.class, box);
		for (Entity entity : list) {
			if (entity instanceof EntityPlayer) {
				continue;
			}
			if (entity instanceof EntityTameable && ((EntityTameable) entity).isTamed()) {
				continue;
			}
			if (entity instanceof EntityNPCInterface && !deleteNPCs) {
				continue;
			}
			if (!this.delete(entity, toDelete)) {
				continue;
			}
			++count;
		}
		if (toDelete.contains(EntityXPOrb.class)) {
			list = sender.getEntityWorld().getEntitiesWithinAABB(EntityXPOrb.class, box);
			for (Entity entity : list) {
				entity.isDead = true;
				++count;
			}
		}
		if (toDelete.contains(EntityItem.class)) {
			list = sender.getEntityWorld().getEntitiesWithinAABB(EntityItem.class, box);
			for (Entity entity : list) {
				entity.isDead = true;
				++count;
			}
		}
		sender.sendMessage(new TextComponentTranslation(count + " entities deleted"));
	}

	@Override
	public String getDescription() {
		return "Kills given entity within range. Also has all, mobs, animal options. Can have multiple types";
	}

	@Nonnull
	public String getName() {
		return "slay";
	}

	public @Nonnull List<String> getTabCompletions(@Nonnull MinecraftServer server, @Nonnull ICommandSender sender, @Nonnull String[] args, BlockPos pos) {
		return CommandBase.getListOfStringsMatchingLastWord(args, this.slayMap.keySet().toArray(new String[0]));
	}

	@Override
	public String getUsage() {
		return "<type>.. [range]";
	}
}
