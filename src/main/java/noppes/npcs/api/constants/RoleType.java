package noppes.npcs.api.constants;

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

public enum RoleType {
	
	DEFAULT(RoleInterface.class, "none", 0, false),
	TRADER(RoleTrader.class, "trader", 1, true),
	FOLLOWER(RoleFollower.class, "mercenary", 2, true),
	BANK(RoleBank.class, "bank", 3, true),
	TRANSPORTER(RoleTransporter.class, "transporter", 4, true),
	POSTMAN(RolePostman.class, "mailman", 5, false),
	COMPANION(RoleCompanion.class, "companion", 6, true),
	DIALOG(RoleDialog.class, "dialog", 7, true);
	
	private int type;
	public String name;
	public boolean hasSettings;
	private Class<?> parent;
	
	RoleType(Class<?> clazz, String named, int t, boolean hasSet) {
		this.type = t;
		this.parent = clazz;
		this.name = "role."+named;
		this.hasSettings = hasSet;
	}
	
	public void setToNpc(EntityNPCInterface npc) {
		try {
			npc.advanced.roleInterface = (RoleInterface) parent.getConstructor(EntityNPCInterface.class).newInstance(npc);
		}
		catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) { e.printStackTrace(); }
	}

	public static String[] getNames() {
		List<String> list = Lists.newArrayList();
		for (RoleType er : RoleType.values()) {
			if (er==COMPANION) { list.add(NoppesStringUtils.translate(er.name, " (WIP)")); }
			else { list.add(er.name); }
		}
		return list.toArray(new String[list.size()]);
	}
	
	public boolean isClass(RoleInterface roleInterface) { return roleInterface.getClass() == parent; }
	
	public int get() { return this.type; }

	public static RoleType get(int id) {
		for (RoleType er : RoleType.values()) { if (er.type==id) { return er; } }
		return RoleType.DEFAULT;
	}
	
}
