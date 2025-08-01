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

	DEFAULT(RoleInterface.class, "none", 0, false), TRADER(RoleTrader.class, "trader", 1, true), FOLLOWER(
			RoleFollower.class, "mercenary", 2,
			true), BANK(RoleBank.class, "bank", 3, true), TRANSPORTER(RoleTransporter.class, "transporter", 4,
					true), POSTMAN(RolePostman.class, "mailman", 5, false), COMPANION(RoleCompanion.class, "companion",
							6, true), DIALOG(RoleDialog.class, "dialog", 7, true);

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
	private final Class<?> parent;

	RoleType(Class<?> clazz, String named, int t, boolean hasSet) {
		this.type = t;
		this.parent = clazz;
		this.name = "role." + named;
		this.hasSettings = hasSet;
	}

	public int get() {
		return this.type;
	}

	public void setToNpc(EntityNPCInterface npc) {
		try {
			npc.advanced.roleInterface = (RoleInterface) parent.getConstructor(EntityNPCInterface.class).newInstance(npc);
		} catch (Exception e) {
			LogWriter.error(e);
		}
	}

}
