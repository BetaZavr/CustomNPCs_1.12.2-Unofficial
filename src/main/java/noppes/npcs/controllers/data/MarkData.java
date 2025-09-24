package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.api.constants.MarkType;
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.handler.capability.IMarkDataHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.constants.EnumPacketClient;

import javax.annotation.Nonnull;

public class MarkData implements IMarkDataHandler, ICapabilityProvider {

	public class Mark implements IMark {

		public Availability availability = new Availability();
		public MarkType type = MarkType.NONE;
		public boolean rotate = false;
		public boolean  is3d = false;
		public int color = 0xFFED51;

		@Override
		public IAvailability getAvailability() {
			return availability;
		}

		@Override
		public int getColor() { return color; }

		@Override
		public int getType() { return type.get(); }

		public MarkType getEnumType() { return type; }

		@Override
		public boolean is3D() { return is3d; }

		@Override
		public boolean isRotate() { return rotate; }

		@Override
		public void set3D(boolean bo) { is3d = bo; }

		@Override
		public void setColor(int colorIn) { color = colorIn; }

		@Override
		public void setRotate(boolean rotateIn) { rotate = rotateIn; }

		@Override
		public void setType(int typeIn) {
			if (typeIn < 1) { typeIn *= -1; }
			typeIn %= MarkType.values().length;
			for (MarkType mt : MarkType.values()) {
				if (mt.get() == typeIn) { type = mt; }
			}
		}

		@Override
		public void update() { syncClients(); }

	}

	@CapabilityInject(IMarkDataHandler.class)
	public static Capability<IMarkDataHandler> CNPCS_MARKDATA_CAPABILITY = null;
	private static final ResourceLocation CNPCS_CAPKEY = new ResourceLocation(CustomNpcs.MODID, "markdata");

	public static MarkData get(EntityLivingBase entity) {
		if (!(entity.getCapability(MarkData.CNPCS_MARKDATA_CAPABILITY, null) instanceof MarkData)) { return new MarkData(); }
		MarkData data = (MarkData) entity.getCapability(MarkData.CNPCS_MARKDATA_CAPABILITY, null);
		if (data != null && data.entity == null) {
			data.entity = entity;
			data.setNBT(entity.getEntityData().getCompoundTag("cnpcmarkdata"));
		}
		return data;
	}

	public static void register(AttachCapabilitiesEvent<Entity> event) { event.addCapability(MarkData.CNPCS_CAPKEY, new MarkData()); }

	public EntityLivingBase entity;

	public List<Mark> marks;

	public MarkData() {
		marks = new ArrayList<>();
	}

	public MarkData.Mark addMark(int type) {
		Mark m = new Mark();
		m.setType(type);
		marks.add(m);
		syncClients();
		return m;
	}

	public void addMark(int type, int color) {
		Mark m = new Mark();
		m.setType(type);
		m.color = color;
		marks.add(m);
		syncClients();
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(@Nonnull Capability<T> capability, EnumFacing facing) {
		if (hasCapability(capability, facing)) { return (T) this; }
		return null;
	}

	@Override
	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Mark m : marks) {
			NBTTagCompound c = new NBTTagCompound();
			c.setTag("availability", m.availability.save(new NBTTagCompound()));
			c.setInteger("type", m.type.get());
			c.setInteger("color", m.color);
			c.setBoolean("rotate", m.rotate);
			c.setBoolean("is3d", m.is3d);
			list.appendTag(c);
		}
		compound.setTag("marks", list);
		return compound;
	}

	public MarkData.Mark getNewMark() { return new Mark(); }

	public boolean hasCapability(@Nonnull Capability<?> capability, EnumFacing facing) { return capability == MarkData.CNPCS_MARKDATA_CAPABILITY; }

	public void save() { entity.getEntityData().setTag("cnpcmarkdata", getNBT()); }

	@Override
	public void setNBT(NBTTagCompound compound) {
		List<Mark> marksIn = new ArrayList<>();
		NBTTagList list = compound.getTagList("marks", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound c = list.getCompoundTagAt(i);
			Mark m = new Mark();
			m.setType(c.getInteger("type"));
			m.color = c.getInteger("color");
			m.availability.load(c.getCompoundTag("availability"));
			m.rotate = c.getBoolean("rotate");
			m.is3d = c.getBoolean("is3d");
			marksIn.add(m);
		}
		marks = marksIn;
	}

	public void syncClients() {
		if (entity == null || entity.world == null || entity.world.isRemote) { return; }
		Server.sendToAll(entity.getServer(), EnumPacketClient.MARK_DATA, entity.getEntityId(), getNBT());
	}

}
