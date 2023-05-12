package noppes.npcs.constants;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import com.google.common.collect.Lists;

import noppes.npcs.NoppesStringUtils;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleBank;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.roles.RoleInterface;
import noppes.npcs.roles.RolePostman;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.roles.RoleTransporter;

public enum EnumNpcRole {

	DEFAULT(RoleInterface.class, "none", false),
	TRADER(RoleTrader.class, "trader", true),
	FOLLOWER(RoleFollower.class, "mercenary", true),
	BANK(RoleBank.class, "bank", true),
	TRANSPORTER(RoleTransporter.class, "transporter", true),
	POSTMAN(RolePostman.class, "mailman", false),
	COMPANION(RoleCompanion.class, "companion", true),
	DIALOG(RoleDialog.class, "dialog", true);
	
	public String name;
	public boolean hasSettings;
	Class<?> parent;
	
	EnumNpcRole(Class<?> clazz, String named, boolean hasSet) {
		parent = clazz;
		name = "role."+named;
		hasSettings = hasSet;
	}
	
	public void setToNpc(EntityNPCInterface npc) {
		try {
			npc.advanced.roleInterface = (RoleInterface) parent.getConstructor(EntityNPCInterface.class).newInstance(npc);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}

	public static String[] getNames() {
		List<String> list = Lists.newArrayList();
		for (EnumNpcRole er : EnumNpcRole.values()) {
			if (er==COMPANION) { list.add(NoppesStringUtils.translate("role."+er.name, "(WIP)")); }
			else { list.add(er.name); }
		}
		return list.toArray(new String[list.size()]);
	}
	
	public boolean isClass(RoleInterface roleInterface) { return roleInterface.getClass() == parent; }
	
}
