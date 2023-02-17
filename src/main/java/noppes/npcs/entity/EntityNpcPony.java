package noppes.npcs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelData;

public class EntityNpcPony extends EntityNPCInterface {
	public ResourceLocation checked;
	public boolean isFlying;
	public boolean isPegasus;
	public boolean isUnicorn;

	public EntityNpcPony(World world) {
		super(world);
		this.isPegasus = false;
		this.isUnicorn = false;
		this.isFlying = false;
		this.checked = null;
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/ponies/MineLP Derpy Hooves.png");
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
			data.setEntityClass((Class<? extends EntityLivingBase>) EntityNpcPony.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
