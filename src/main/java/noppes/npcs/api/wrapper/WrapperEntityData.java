package noppes.npcs.api.wrapper;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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
import noppes.npcs.mixin.entity.IEntityMixin;

import javax.annotation.Nonnull;

public class WrapperEntityData implements IWrapperEntityDataHandler, ICapabilityProvider {

	@CapabilityInject(IWrapperEntityDataHandler.class)
	public static Capability<IWrapperEntityDataHandler> WRAPPER_ENTITY_DATA_CAPABILITY = null;
	private static final ResourceLocation key = new ResourceLocation(CustomNpcs.MODID, "entitydata");

	public static IEntity<?> get(Entity entity) {
		if (entity == null || entity.world == null) {
			return null;
		}
		WrapperEntityData data = (WrapperEntityData) entity
				.getCapability(WrapperEntityData.WRAPPER_ENTITY_DATA_CAPABILITY, null);
		if (entity instanceof EntityPlayer) {
			String k = (entity.world == null || entity.world.isRemote ? "client_" : "server_") + entity.getUniqueID();
			if (data != null && !PlayerWrapper.map.containsKey(k)) {
				PlayerWrapper.map.put(k, data);
			}
			if (PlayerWrapper.map.get(k) != null && !PlayerWrapper.map.get(k).equals(data)) {
				WrapperEntityData.setTempData(PlayerWrapper.map.get(k), data);
				PlayerWrapper.map.put(k, data);
			}
		}
		if (data == null) {
			LogWriter.warn("Unable to get EntityData for " + entity);
			WrapperEntityData ret = WrapperEntityData.getData(entity);
			CapabilityDispatcher capabilities = ((IEntityMixin) entity).npcs$getCapabilities();
			if (capabilities != null) {
				// "capabilities" does not want to be converted to the created mixin interface under any circumstances
				Field fieldCaps = null;
				for (Field f : capabilities.getClass().getDeclaredFields()) {
					if (f.getName().equals("caps")) {
						fieldCaps = f;
						break;
					}
				}
				if (fieldCaps != null) {
					try {
						fieldCaps.setAccessible(true);
						ICapabilityProvider[] caps = (ICapabilityProvider[]) fieldCaps.get(capabilities);
						if (caps != null) {
							List<ICapabilityProvider> list = Lists.newArrayList();
							Collections.addAll(list, caps);
							list.add(ret);
							fieldCaps.set(capabilities, list.toArray(new ICapabilityProvider[0]));
						}
					}
					catch (Exception e) { LogWriter.error(e); }
				}
			} else {
				Map<ResourceLocation, ICapabilityProvider> m = Maps.newHashMap();
				m.put(WrapperEntityData.key, ret);
				((IEntityMixin) entity).npcs$setCapabilities(new CapabilityDispatcher(m, null));
			}
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
		if (entity instanceof EntityVillager) {
			return new WrapperEntityData(new VillagerWrapper<EntityVillager>((EntityVillager) entity));
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

	private static void setTempData(WrapperEntityData oldData, WrapperEntityData newData) {
		if (oldData == null || newData == null || oldData.base == null || newData.base == null) {
			return;
		}
        oldData.base.getTempdata().getKeys();
        for (String key : oldData.base.getTempdata().getKeys()) {
            try {
                newData.base.getTempdata().put(key, oldData.base.getTempdata().get(key));
            }
			catch (Exception e) { LogWriter.error("Error:", e); }
        }
    }

	public IEntity<?> base;

	public WrapperEntityData() {
	}

	public WrapperEntityData(IEntity<?> base) {
		this.base = base;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public NBTTagCompound getNBT() {
		return new NBTTagCompound();
	}

	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) {
		return capability == WrapperEntityData.WRAPPER_ENTITY_DATA_CAPABILITY;
	}

	@Override
	public void setNBT(NBTTagCompound compound) {
	}

}
