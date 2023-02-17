package noppes.npcs.entity;

import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;

public class EntityNpcClassicPlayer extends EntityCustomNpc {
	public EntityNpcClassicPlayer(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png");
	}
}
