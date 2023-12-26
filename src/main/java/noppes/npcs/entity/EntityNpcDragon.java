package noppes.npcs.entity;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.part.ModelData;

public class EntityNpcDragon extends EntityNPCInterface {
	private boolean exploded;
	public double[][] field_40162_d;
	public int field_40164_e;
	public float field_40172_ax;
	public float field_40173_aw;
	public int field_40178_aA;
	public boolean isFlying;

	public EntityNpcDragon(World world) {
		super(world);
		this.isFlying = false;
		this.exploded = false;
		this.field_40162_d = new double[64][3];
		this.field_40164_e = -1;
		this.field_40173_aw = 0.0f;
		this.field_40172_ax = 0.0f;
		this.field_40178_aA = 0;
		this.scaleX = 0.4f;
		this.scaleY = 0.4f;
		this.scaleZ = 0.4f;
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/dragon/BlackDragon.png");
		this.width = 1.8f;
		this.height = 1.4f;
	}

	public double[] func_40160_a(int i, float f) {
		f = 1.0f - f;
		int j = this.field_40164_e - i * 1 & 0x3F;
		int k = this.field_40164_e - i * 1 - 1 & 0x3F;
		double[] ad = new double[3];
		double d = this.field_40162_d[j][0];
		double d2;
		for (d2 = this.field_40162_d[k][0] - d; d2 < -180.0; d2 += 360.0) {
		}
		while (d2 >= 180.0) {
			d2 -= 360.0;
		}
		ad[0] = d + d2 * f;
		d = this.field_40162_d[j][1];
		d2 = this.field_40162_d[k][1] - d;
		ad[1] = d + d2 * f;
		ad[2] = this.field_40162_d[j][2] + (this.field_40162_d[k][2] - this.field_40162_d[j][2]) * f;
		return ad;
	}

	public double getMountedYOffset() {
		return 1.1;
	}

	@Override
	public void onLivingUpdate() {
		this.field_40173_aw = this.field_40172_ax;
		if (this.world.isRemote && this.getHealth() <= 0.0f) {
			if (!this.exploded) {
				this.exploded = true;
				float f = (this.rand.nextFloat() - 0.5f) * 8.0f;
				float f2 = (this.rand.nextFloat() - 0.5f) * 4.0f;
				float f3 = (this.rand.nextFloat() - 0.5f) * 8.0f;
				this.world.spawnParticle(EnumParticleTypes.EXPLOSION_LARGE, this.posX + f, this.posY + 2.0 + f2,
						this.posZ + f3, 0.0, 0.0, 0.0, new int[0]);
			}
		} else {
			this.exploded = false;
			float f4 = 0.2f
					/ (MathHelper.sqrt(this.motionX * this.motionX + this.motionZ * this.motionZ) * 10.0f + 1.0f);
			f4 = 0.045f;
			f4 *= Math.pow(2.0, this.motionY);
			this.field_40172_ax += f4 * 0.5f;
		}
		super.onLivingUpdate();
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
			data.setEntityClass((Class<? extends EntityLivingBase>) EntityNpcDragon.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}

	@Override
	public void updateHitbox() {
		this.width = 1.8f;
		this.height = 1.4f;
	}
}
