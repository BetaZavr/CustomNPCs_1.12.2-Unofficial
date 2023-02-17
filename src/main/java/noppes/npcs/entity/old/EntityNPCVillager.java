package noppes.npcs.entity.old;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.ModelData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNPCVillager extends EntityNPCInterface {
	public EntityNPCVillager(World world) {
		super(world);
		this.display.setSkinTexture("textures/entity/villager/villager.png");
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
			data.setEntityClass(EntityVillager.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
