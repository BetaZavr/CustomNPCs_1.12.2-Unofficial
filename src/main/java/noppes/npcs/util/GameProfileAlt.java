package noppes.npcs.util;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityNPCInterface;

public class GameProfileAlt extends GameProfile {
	private static UUID id = UUID.fromString("c9c843f8-4cb1-4c82-aa61-e264291b7bd6");
	public EntityNPCInterface npc;

	public GameProfileAlt() {
		super(GameProfileAlt.id, "[" + CustomNpcs.MODID + "]");
	}

	public UUID getId() {
		if (this.npc == null) {
			return GameProfileAlt.id;
		}
		return this.npc.getPersistentID();
	}

	public String getName() {
		if (this.npc == null) {
			return super.getName();
		}
		return this.npc.getName();
	}

	public boolean isComplete() {
		return false;
	}

}
