package noppes.npcs;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.server.permission.DefaultPermissionLevel;
import net.minecraftforge.server.permission.PermissionAPI;

public class CustomNpcsPermissions {

	public static class Permission {

		private final static List<Permission> permissions = new ArrayList<>();

		public boolean defaultValue;

		public String name;

		public Permission(String name) {
			this.defaultValue = true;
			this.name = name;
			Permission.permissions.add(this);
		}

		public Permission(String name, boolean defaultValue) {
			this.defaultValue = true;
			this.name = name;
			Permission.permissions.add(this);
			this.defaultValue = defaultValue;
		}

	}

	public static Permission EDIT_BLOCKS = new Permission(CustomNpcs.MODID + ".edit.blocks");
	public static Permission EDIT_VILLAGER = new Permission(CustomNpcs.MODID + ".edit.villager");
	public static Permission GLOBAL_BANK = new Permission(CustomNpcs.MODID + ".global.bank");
	public static Permission GLOBAL_DIALOG = new Permission(CustomNpcs.MODID + ".global.dialog");
	public static Permission GLOBAL_FACTION = new Permission(CustomNpcs.MODID + ".global.faction");
	public static Permission GLOBAL_LINKED = new Permission(CustomNpcs.MODID + ".global.linked");
	public static Permission GLOBAL_MARKET = new Permission(CustomNpcs.MODID + ".global.marcet");
	public static Permission GLOBAL_NATURAL_SPAWN = new Permission(CustomNpcs.MODID + ".global.naturalspawn");
	public static Permission GLOBAL_PLAYERDATA = new Permission(CustomNpcs.MODID + ".global.playerdata");
	public static Permission GLOBAL_QUEST = new Permission(CustomNpcs.MODID + ".global.quest");
	public static Permission GLOBAL_RECIPE = new Permission(CustomNpcs.MODID + ".global.recipe");
	public static Permission GLOBAL_TRANSPORT = new Permission(CustomNpcs.MODID + ".global.transport");
	public static CustomNpcsPermissions Instance;
	public static Permission NPC_ADVANCED = new Permission(CustomNpcs.MODID + ".npc.advanced");
	public static Permission NPC_CLONE = new Permission(CustomNpcs.MODID + ".npc.clone");
	public static Permission NPC_CREATE = new Permission(CustomNpcs.MODID + ".npc.create");
	public static Permission NPC_DELETE = new Permission(CustomNpcs.MODID + ".npc.delete");
	public static Permission NPC_DISPLAY = new Permission(CustomNpcs.MODID + ".npc.display");
	public static Permission NPC_FREEZE = new Permission(CustomNpcs.MODID + ".npc.freeze");
	public static Permission NPC_GUI = new Permission(CustomNpcs.MODID + ".npc.gui");
	public static Permission NPC_INVENTORY = new Permission(CustomNpcs.MODID + ".npc.inventory");
	public static Permission NPC_RESET = new Permission(CustomNpcs.MODID + ".npc.reset");
	public static Permission NPC_STATS = new Permission(CustomNpcs.MODID + ".npc.stats");
	public static Permission SCENES = new Permission(CustomNpcs.MODID + ".scenes");
	public static Permission SOULSTONE_ALL = new Permission(CustomNpcs.MODID + ".soulstone.all", false);
	public static Permission SPAWNER_CREATE = new Permission(CustomNpcs.MODID + ".spawner.create");
	public static Permission SPAWNER_MOB = new Permission(CustomNpcs.MODID + ".spawner.mob");
	public static Permission TOOL_MOUNTER = new Permission(CustomNpcs.MODID + ".tool.mounter");
	public static Permission TOOL_NBT_BOOK = new Permission(CustomNpcs.MODID + ".tool.nbtbook");
	public static Permission TOOL_PATHER = new Permission(CustomNpcs.MODID + ".tool.pather");
	public static Permission TOOL_SCRIPTER = new Permission(CustomNpcs.MODID + ".tool.scripter");
	public static Permission TOOL_TELEPORTER = new Permission(CustomNpcs.MODID + ".tool.teleporter");

	public static boolean hasPermission(EntityPlayer player, Permission permission) {
		if (CustomNpcs.DisablePermissions) {
			return permission.defaultValue;
		}
		return hasPermissionString(player, permission.name);
	}

	public static boolean hasPermissionString(EntityPlayer player, String permission) {
		return CustomNpcs.DisablePermissions || PermissionAPI.hasPermission(player, permission);
	}

	public CustomNpcsPermissions() {
		CustomNpcsPermissions.Instance = this;
		if (!CustomNpcs.DisablePermissions) {
			LogManager.getLogger(CustomNpcsPermissions.class).info("CustomNPC Permissions available:");
			Permission.permissions.sort((o1, o2) -> o1.name.compareToIgnoreCase(o2.name));
			for (Permission p : Permission.permissions) {
				PermissionAPI.registerNode(p.name, p.defaultValue ? DefaultPermissionLevel.ALL : DefaultPermissionLevel.OP, p.name);
				LogManager.getLogger(CustomNpcsPermissions.class).info(p.name);
			}
		}
	}
}
