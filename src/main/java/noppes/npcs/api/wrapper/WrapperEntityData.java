package noppes.npcs.api.wrapper;

import java.util.concurrent.Callable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityProjectile;

public class WrapperEntityData implements Callable<PlayerData>, ICapabilityProvider {
	@CapabilityInject(WrapperEntityData.class)
	public static Capability<WrapperEntityData> ENTITYDATA_CAPABILITY = null;
	private static ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "entitydata");

	public static IEntity<?> get(Entity entity) {
		if (entity == null) {
			return null;
		}
		WrapperEntityData data = entity.getCapability(WrapperEntityData.ENTITYDATA_CAPABILITY, null);
		if (data == null) {
			LogWriter.warn("Unable to get EntityData for " + entity);
			return getData(entity).base;
		}
		return data.base;
	}

	private static WrapperEntityData getData(Entity entity) {
		if (entity == null || entity.world == null || entity.world.isRemote) {
			return null;
		}
		if (entity instanceof EntityPlayerMP) {
			return new WrapperEntityData(new PlayerWrapper<EntityPlayerMP>((EntityPlayerMP) entity));
		}
		if (PixelmonHelper.isPixelmon(entity)) {
			return new WrapperEntityData(new PixelmonWrapper<EntityTameable>((EntityTameable) entity));
		}
		if (entity instanceof EntityAnimal) {
			return new WrapperEntityData(new AnimalWrapper<EntityAnimal>((EntityAnimal) entity));
		}
		if (entity instanceof EntityMob) {
			return new WrapperEntityData(new MonsterWrapper<EntityMob>((EntityMob) entity));
		}
		if (entity instanceof EntityLiving) {
			return new WrapperEntityData(new EntityLivingWrapper<EntityLiving>((EntityLiving) entity));
		}
		if (entity instanceof EntityLivingBase) {
			return new WrapperEntityData(new EntityLivingBaseWrapper<EntityLivingBase>((EntityLivingBase) entity));
		}
		if (entity instanceof EntityVillager) {
			return new WrapperEntityData(new VillagerWrapper<EntityVillager>((EntityVillager) entity));
		}
		if (entity instanceof EntityItem) {
			return new WrapperEntityData(new EntityItemWrapper<EntityItem>((EntityItem) entity));
		}
		if (entity instanceof EntityProjectile) {
			return new WrapperEntityData(new ProjectileWrapper<EntityProjectile>((EntityProjectile) entity));
		}
		if (entity instanceof EntityThrowable) {
			return new WrapperEntityData(new ThrowableWrapper<EntityThrowable>((EntityThrowable) entity));
		}
		if (entity instanceof EntityArrow) {
			return new WrapperEntityData(new ArrowWrapper<EntityArrow>((EntityArrow) entity));
		}
		return new WrapperEntityData(new EntityWrapper<Entity>(entity));
	}

	public static void register(AttachCapabilitiesEvent<Entity> event) {
		event.addCapability(WrapperEntityData.key, getData(event.getObject()));
	}

	public IEntity<?> base;

	public WrapperEntityData(IEntity<?> base) {
		this.base = base;
	}

	@Override
	public PlayerData call() throws Exception {
		return null;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == WrapperEntityData.ENTITYDATA_CAPABILITY;
	}

}
