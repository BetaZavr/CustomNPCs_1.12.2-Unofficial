package noppes.npcs.client.model.part;

import java.lang.reflect.Method;
import java.util.Objects;

import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelData extends ModelDataShared {

	public ModelData copy() {
		ModelData data = new ModelData();
		data.load(save());
		return data;
	}

	public EntityLivingBase getEntity(EntityNPCInterface npc) {
		if (this.entityClass == null) { return null; }
		if (this.entity == null) {
			try {
				this.entity = this.entityClass.getConstructor(World.class).newInstance(npc.world);
				if (PixelmonHelper.isPixelmon(this.entity) && npc.world.isRemote && !this.extra.hasKey("Name")) {
					this.extra.setString("Name", "Abra");
				}
				try {
					this.entity.readEntityFromNBT(this.extra);
				}
				catch (Exception e) { LogWriter.error(e); }
				this.entity.setEntityInvulnerable(true);
				this.entity.getEntityAttribute(SharedMonsterAttributes.MAX_HEALTH).setBaseValue(npc.getMaxHealth());
				for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
					this.entity.setItemStackToSlot(slot, npc.getItemStackFromSlot(slot));
				}
			} catch (Exception ex) { LogWriter.error(ex); }
		}
		return this.entity;
	}

	public void setExtra(EntityLivingBase entity, String key, String value) {
		key = key.toLowerCase();
		if (key.equals("breed") && Objects.equals(EntityList.getEntityString(entity), "tgvstyle.Dog")) {
			try {
				Method method = entity.getClass().getMethod("getBreedID", Class[].class);
				Enum<?> breed = (Enum<?>) method.invoke(entity, (Object) new Class<?>[0]);
				method = entity.getClass().getMethod("setBreedID", breed.getClass());
				method.invoke(entity, ((Enum<?>[]) breed.getClass().getEnumConstants())[Integer.parseInt(value)]);
				NBTTagCompound comp = new NBTTagCompound();
				entity.writeEntityToNBT(comp);
				this.extra.setString("EntityData21", comp.getString("EntityData21"));
			} catch (Exception e) { LogWriter.error(e); }
		}
		if (key.equalsIgnoreCase("name") && PixelmonHelper.isPixelmon(entity)) {
			this.extra.setString("Name", value);
		}
		this.clearEntity();
	}
}
