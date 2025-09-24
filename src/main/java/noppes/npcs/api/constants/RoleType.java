package noppes.npcs.api.constants;

import java.util.ArrayList;
import java.util.List;

import noppes.npcs.LogWriter;
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

	DEFAULT("none", 0, false),
	TRADER("trader", 1, true),
	FOLLOWER("mercenary", 2, true),
	BANK("bank", 3, true),
	TRANSPORTER("transporter", 4, true),
	POSTMAN("mailman", 5, false),
	COMPANION("companion", 6, true),
	DIALOG("dialog", 7, true);

	public static RoleType get(int id) {
		for (RoleType er : RoleType.values()) {
			if (er.type == id) {
				return er;
			}
		}
		return RoleType.DEFAULT;
	}

	public static String[] getNames() {
		List<String> list = new ArrayList<>();
		for (RoleType er : RoleType.values()) {
			if (er == COMPANION) {
				list.add(NoppesStringUtils.translate(er.name, " (WIP)"));
			} else {
				list.add(er.name);
			}
		}
		return list.toArray(new String[0]);
	}

	private final int type;
	public final String name;
	public final boolean hasSettings;

	RoleType(String named, int t, boolean hasSet) {
		type = t;
		name = "role." + named;
		hasSettings = hasSet;
	}

	public int get() {
		return this.type;
	}

	public void setToNpc(EntityNPCInterface npc) {
		switch (this) {
			case DEFAULT: npc.advanced.roleInterface = new RoleInterface(npc); break;
			case TRADER: npc.advanced.roleInterface = new RoleTrader(npc); break;
			case FOLLOWER: npc.advanced.roleInterface = new RoleFollower(npc); break;
			case BANK: npc.advanced.roleInterface = new RoleBank(npc); break;
			case TRANSPORTER: npc.advanced.roleInterface = new RoleTransporter(npc); break;
			case POSTMAN: npc.advanced.roleInterface = new RolePostman(npc); break;
			case COMPANION: npc.advanced.roleInterface = new RoleCompanion(npc); break;
			case DIALOG: npc.advanced.roleInterface = new RoleDialog(npc); break;
		}
	}

}
