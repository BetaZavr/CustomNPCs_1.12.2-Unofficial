package noppes.npcs.entity;

import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;

public class EntityNpcAlex extends EntityCustomNpc {
	public EntityNpcAlex(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/alex_skins/alex_2.png");
	}
}
