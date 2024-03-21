package noppes.npcs.api.wrapper;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.command.CommandException;
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
import noppes.npcs.api.handler.capability.IWrapperEntityDataHandler;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityProjectile;
import noppes.npcs.util.ObfuscationHelper;

public class WrapperEntityData
implements IWrapperEntityDataHandler, ICapabilityProvider {
	
	@CapabilityInject(IWrapperEntityDataHandler.class)
	public static Capability<IWrapperEntityDataHandler> WRAPPER_ENTITY_DATA_CAPABILITY = null;
	private static ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "entitydata");

	public WrapperEntityData() { }
	
	public static IEntity<?> get(Entity entity) {
		if (entity == null || entity.world==null) { return null; }
		WrapperEntityData data = (WrapperEntityData) entity.getCapability(WrapperEntityData.WRAPPER_ENTITY_DATA_CAPABILITY, null);
		if (entity instanceof EntityPlayer) {
			String k = (entity.world==null || entity.world.isRemote ? "client_" : "server_") + entity.getUniqueID().toString();
			if (data != null && !PlayerWrapper.map.containsKey(k)) { PlayerWrapper.map.put(k, data); }
			if (PlayerWrapper.map.get(k) != null && !PlayerWrapper.map.get(k).equals(data)) {
				WrapperEntityData.setTempData(PlayerWrapper.map.get(k), data);
				PlayerWrapper.map.put(k, data);
			}
		}
		if (data == null) {
			LogWriter.warn("Unable to get EntityData for " + entity);
			WrapperEntityData ret = WrapperEntityData.getData(entity);
			CapabilityDispatcher capabilities = ObfuscationHelper.getValue(Entity.class, entity, CapabilityDispatcher.class);
			if (capabilities!=null) {
				ICapabilityProvider[] caps = ObfuscationHelper.getValue(CapabilityDispatcher.class, capabilities, 0);
				List<ICapabilityProvider> list = Lists.newArrayList();
				for (ICapabilityProvider cap : caps) { list.add(cap); }
				list.add(ret);
				ObfuscationHelper.setValue(CapabilityDispatcher.class, capabilities, list.toArray(new ICapabilityProvider[list.size()]), 0);
			} else {
				Map<ResourceLocation, ICapabilityProvider> m = Maps.<ResourceLocation, ICapabilityProvider>newHashMap();
				m.put(WrapperEntityData.key, ret);
				ObfuscationHelper.setValue(Entity.class, entity, new CapabilityDispatcher(m, null), CapabilityDispatcher.class);
			}
			return ret.base;
		}
		return data.base;
	}

	private static void setTempData(WrapperEntityData oldData, WrapperEntityData newData) {
		if (oldData == null || newData == null || oldData.base == null || newData.base == null) { return; }
		if (oldData.base.getTempdata().getKeys().length>0) {
			for (String key : oldData.base.getTempdata().getKeys()) {
				try { newData.base.getTempdata().put(key, oldData.base.getTempdata().get(key)); }
				catch (CommandException e) { e.printStackTrace(); }
			}
		}
	}

	public static WrapperEntityData getData(Entity entity) {
		if (entity == null) { return null; }
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

	public WrapperEntityData(IEntity<?> base) { this.base = base; }

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == WrapperEntityData.WRAPPER_ENTITY_DATA_CAPABILITY;
	}

	@Override
	public NBTTagCompound getNBT() { return null; }

	@Override
	public void setNBT(NBTTagCompound compound) { }

}
