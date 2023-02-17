package noppes.npcs.entity;

import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;

public class EntityNPC64x32 extends EntityCustomNpc {
	public EntityNPC64x32(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/Steve64x32.png");
	}
}
