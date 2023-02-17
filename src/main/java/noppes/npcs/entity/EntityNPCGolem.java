package noppes.npcs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelData;

public class EntityNPCGolem extends EntityNPCInterface {
	public EntityNPCGolem(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/golem/Iron Golem.png");
		this.width = 1.4f;
		this.height = 2.5f;
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
			data.setEntityClass((Class<? extends EntityLivingBase>) EntityNPCGolem.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}

	@Override
	public void updateHitbox() {
		this.currentAnimation = this.dataManager.get(EntityNPCGolem.Animation);
		if (this.currentAnimation == 2) {
			float n = 0.5f;
			this.height = n;
			this.width = n;
		} else if (this.currentAnimation == 1) {
			this.width = 1.4f;
			this.height = 2.0f;
		} else {
			this.width = 1.4f;
			this.height = 2.5f;
		}
	}
}
