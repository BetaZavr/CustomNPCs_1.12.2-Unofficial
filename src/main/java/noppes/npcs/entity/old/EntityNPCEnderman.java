package noppes.npcs.entity.old;

import net.minecraft.entity.monster.EntityEnderman;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelData;
import noppes.npcs.entity.EntityCustomNpc;

public class EntityNPCEnderman extends EntityNpcEnderchibi {
	public EntityNPCEnderman(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/enderman/enderman.png");
		this.display.setOverlayTexture(CustomNpcs.MODID + ":textures/overlays/ender_eyes.png");
		this.width = 0.6f;
		this.height = 2.9f;
	}

	@Override
	public void onUpdate() {
		this.setNoAI(this.isDead = true);
		if (!this.world.isRemote) {
			NBTTagCompound compound = new NBTTagCompound();
			this.writeToNBT(compound);
			EntityCustomNpc npc = new EntityCustomNpc(this.world);
			npc.readFromNBT(compound);
			ModelData data = npc.modelData;
			data.setEntityClass(EntityEnderman.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}

	@Override
	public void updateHitbox() {
		if (this.currentAnimation == 2) {
			float n = 0.2f;
			this.height = n;
			this.width = n;
		} else if (this.currentAnimation == 1) {
			this.width = 0.6f;
			this.height = 2.3f;
		} else {
			this.width = 0.6f;
			this.height = 2.9f;
		}
		this.width = this.width / 5.0f * this.display.getSize();
		this.height = this.height / 5.0f * this.display.getSize();
	}
}
