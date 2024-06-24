package noppes.npcs.constants;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public enum EnumScriptType
{
	INIT("init", new int[] { 0, 1, 2, 3, 4, 5, 6 }), 
	TICK("tick", new int[] { 0, 1, 2, 3, 6 }), 
	INTERACT("interact", new int[] { 0, 1, 2, 3 }), 
	DIALOG("dialog", new int[] { 0, 1 }), 
	DAMAGED("damaged", new int[] { 0, 1, 2, 3 }), 
	DIED("died", new int[] { 0, 1, 2, 3 }), 
	ATTACK_MELEE("meleeAttack", new int[] { 1 }), 
	TARGET("target", new int[] { 1 }), 
	COLLIDE("collide", new int[] { 1, 2 }), 
	KILL("kill", new int[] { 0, 1 }), 
	DIALOG_OPTION("dialogOption", new int[] { 0, 1 }), 
	TARGET_LOST("targetLost", new int[] { 1 }), 
	ROLE("role", new int[] { 1 }), 
	RANGED_LAUNCHED("rangedLaunched", new int[] { 0, 1 }), 
	CLICKED("clicked", new int[] { 2 }), 
	FALLEN_UPON("fallenUpon", new int[] { 2 }), 
	RAIN_FILLED("rainFilled", new int[] { 2 }), 
	BROKEN("broken", new int[] { 0, 2 }), 
	HARVESTED("harvested", new int[] { 2 }), 
	EXPLODED("exploded", new int[] { 2 }), 
	NEIGHBOR_CHANGED("neighborChanged", new int[] { 2 }), 
	REDSTONE("redstone", new int[] { 2 }), 
	DOOR_TOGGLE("doorToggle", new int[] { 2 }), 
	TIMER("timer", new int[] { 0, 1, 2 }), 
	TOSS("toss", new int[] { 0 }), 
	CONTAINER_OPEN("containerOpen", new int[] { 0 }), 
	CONTAINER_CLOSED("containerClosed", new int[] { 0 }), 
	LOGIN("login", new int[] { 0 }), 
	LOGOUT("logout", new int[] { 0 }), 
	CHAT("chat", new int[] { 0 }), 
	DAMAGED_ENTITY("damagedEntity", new int[] { 0 }), 
	DIALOG_CLOSE("dialogClose", new int[] { 0, 1 }), 
	SPAWN("spawn", new int[] { 3 }), 
	TOSSED("tossed", new int[] { 3 }), 
	PICKEDUP("pickedUp", new int[] { 3 }), 
	PICKUP("pickUp", new int[] { 0 }), 
	ATTACK("attack", new int[] { 0, 3 }), 
	PROJECTILE_TICK("projectileTick", new int[] { 6 }), 
	PROJECTILE_IMPACT("projectileImpact", new int[] { 6 }), 
	FACTION_UPDATE("factionUpdate", new int[] { 0 }), 
	LEVEL_UP("levelUp", new int[] { 0 }), 
	QUEST_START("questStart", new int[] { 0 }), 
	QUEST_COMPLETED("questCompleted", new int[] { 0 }), 
	QUEST_TURNIN("questTurnIn", new int[] { 0 }), 
	KEY_UP("keyPressed", new int[] { 0 }), 
	KEY_GUI_UP("keyGUIPressed", new int[] { 0 }), 
	CUSTOM_CHEST_CLOSED("customChestClosed", new int[] { 0 }), 
	CUSTOM_CHEST_CLICKED("customChestClicked", new int[] { 0 }),
	SCRIPT_COMMAND("scriptCommand", new int[] { 7 }), 
	CUSTOM_GUI_CLOSED("customGuiClosed", new int[] { 0 }), 
	CUSTOM_GUI_BUTTON("customGuiButton", new int[] { 0 }), 
	CUSTOM_GUI_SLOT("customGuiSlot", new int[] { 0 }), 
	CUSTOM_GUI_SCROLL("customGuiScroll", new int[] { 0 }), 
	CUSTOM_GUI_SLOT_CLICKED("customGuiSlotClicked", new int[] { 0 }),
	QUEST_CANCELED("questCanceled", new int[] { 0 }), 
	ITEM_FISHED("itemFished", new int[] { 0 }), 
	ITEM_CRAFTED("itemCrafted", new int[] { 0 }), 
	KEY_DOWN("keyDown", new int[] { 0 }), 
	MOUSE_DOWN("mouseDown", new int[] { 0 }), 
	MOUSE_UP("mousePressed", new int[] { 0 }), 
	POTION_IS_READY("isReady", new int[] { 5 }), 
	POTION_PERFORM("performEffect", new int[] { 5 }), 
	POTION_AFFECT("affectEntity", new int[] { 5 }), 
	PLASED("plased", new int[] { 0 }), 
	POTION_END("endEffect", new int[] { 5 }), 
	SCRIPT_TRIGGER("trigger", new int[] { 0, 1, 2, 3, 4, 5, 6 }), 
	SOUND_PLAY("soundPlayed", new int[] { 0 }), 
	SOUND_STOP("soundStoped", new int[] { 0 }), 	
	PACKEGE_RECEIVED("packageReceived", new int[] { 4 }), 	
	PACKEGE_FROM("packageFrom", new int[] { 0, 4 }),
	CUSTOM_TELEPORT("customTeleport", new int[] { 0, 1 }),
	KEY_ACTIVE("keyActive", new int[] { 0 }),
	STOP_ANIMATION("stopAnimation", new int[] { 1 }),
	GUI_OPEN("openGUI", new int[] { 0 }),
	SOUND_TICK_EVENT("soundTickEvent", new int[] { 4 }),
	QUEST_LOG_BUTTON("questButton", new int[] { 0 }),
	IN_GAME("inGame", new int[] { 0 });
	
	public String function;
	public List<Integer> hundelerTypes;
	
	private EnumScriptType(String function, int[] types) {
		this.function = function;
		this.hundelerTypes = Lists.<Integer>newArrayList();
		for (int type : types) { this.hundelerTypes.add(type); }
	}
	
	public boolean isFor(int type) {
		for (int t : hundelerTypes) {
			if (type == t) { return true; }
		}
		return false;
	}
	
	/** IDs:
	 * 0 - players;
	 * 1 - NPCs;
	 * 2 - blocks;
	 * 3 - items;
	 * 4 - forge / client;
	 * 5 - potions;
	 * 6 - projectiles;
	 * 7 - world / mods;
	 */
	public static List<EnumScriptType> getAllFunctions(int type) {
		Map<String, EnumScriptType> map = Maps.<String, EnumScriptType>newTreeMap();
		for (EnumScriptType est : EnumScriptType.values()) {
			if (est.hundelerTypes.contains(type)) { map.put(est.function, est); }
		}
		return Lists.<EnumScriptType>newArrayList(map.values());
	}
}
