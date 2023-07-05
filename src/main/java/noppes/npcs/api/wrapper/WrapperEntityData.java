package noppes.npcs.api.wrapper;

import java.util.List;
import java.util.concurrent.Callable;

import com.google.common.collect.Lists;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityThrowable;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityDispatcher;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.capability.INbtHandler;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.ObfuscationHelper;

public class WrapperEntityData
implements INbtHandler, ICapabilityProvider, Callable<PlayerData> {
	
	@CapabilityInject(WrapperEntityData.class)
	public static Capability<WrapperEntityData> ENTITYDATA_CAPABILITY = null;
	private static ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "entitydata");


	public WrapperEntityData() { }
	
	public static IEntity<?> get(Entity entity) {
		if (entity == null || entity.world==null) { return null; }
		WrapperEntityData data = entity.getCapability(WrapperEntityData.ENTITYDATA_CAPABILITY, null);
		if (data == null) {
			LogWriter.warn("Unable to get EntityData for " + entity);
			WrapperEntityData ret = WrapperEntityData.getData(entity);
			CapabilityDispatcher capabilities = ObfuscationHelper.getValue(Entity.class, entity, CapabilityDispatcher.class);
			ICapabilityProvider[] caps = ObfuscationHelper.getValue(CapabilityDispatcher.class, capabilities, 0);
			List<ICapabilityProvider> list = Lists.newArrayList();
			for (ICapabilityProvider cap : caps) { list.add(cap); }
			list.add(ret);
			ObfuscationHelper.setValue(CapabilityDispatcher.class, capabilities, list.toArray(new ICapabilityProvider[list.size()]), 0);
			return ret.base;
		}
		return data.base;
	}

	public static WrapperEntityData getData(Entity entity) {
		if (entity == null) {
			return null;
		}
		if (entity instanceof EntityPlayer) {
			return new WrapperEntityData(new PlayerWrapper<EntityPlayer>((EntityPlayer) entity));
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

	@Override
	public NBTTagCompound getCapabilityNBT() { return null; }

	@Override
	public void setCapabilityNBT(NBTTagCompound compound) { }

}
