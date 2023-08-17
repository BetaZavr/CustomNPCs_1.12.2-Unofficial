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
import noppes.npcs.api.entity.data.IMark;
import noppes.npcs.api.handler.capability.INbtHandler;
import noppes.npcs.api.handler.data.IAvailability;
import noppes.npcs.constants.EnumPacketClient;

public class MarkData
implements INbtHandler, ICapabilityProvider {
	
	public class Mark
	implements IMark {
		
		public Availability availability;
		public int color;
		public boolean rotate, is3d;
		public int type;

		public Mark() {
			this.type = 0;
			this.availability = new Availability();
			this.color = 16772433;
			this.rotate = false;
			this.is3d = false;
		}

		@Override
		public IAvailability getAvailability() {
			return this.availability;
		}

		@Override
		public int getColor() {
			return this.color;
		}

		@Override
		public int getType() {
			return this.type;
		}

		// New
		@Override
		public boolean isRotate() {
			return this.rotate;
		}

		@Override
		public void setColor(int color) {
			this.color = color;
		}

		@Override
		public void setRotate(boolean rotate) {
			this.rotate = rotate;
		}

		@Override
		public void setType(int type) {
			this.type = type;
		}

		@Override
		public void update() {
			MarkData.this.syncClients();
		}

		public boolean is3D() { return this.is3d; }

		public void set3D(boolean is3d) { this.is3d = is3d; }

	}

	private static ResourceLocation CAPKEY = new ResourceLocation(CustomNpcs.MODID, "markdata");
	
	@CapabilityInject(MarkData.class)
	public static Capability<MarkData> MARKDATA_CAPABILITY = null;
	
	public static MarkData get(EntityLivingBase entity) {
		MarkData data = (MarkData) entity.getCapability(MarkData.MARKDATA_CAPABILITY, null);
		if (data.entity == null) {
			data.entity = entity;
			data.setNBT(entity.getEntityData().getCompoundTag("cnpcmarkdata"));
		}
		return data;
	}

	public static void register(AttachCapabilitiesEvent<Entity> event) {
		event.addCapability(MarkData.CAPKEY, (ICapabilityProvider) new MarkData());
	}

	private EntityLivingBase entity;

	public List<Mark> marks;

	public MarkData() { this.marks = new ArrayList<Mark>(); }

	public IMark addMark(int type) {
		Mark m = new Mark();
		m.type = type;
		this.marks.add(m);
		this.syncClients();
		return m;
	}

	public IMark addMark(int type, int color) {
		Mark m = new Mark();
		m.type = type;
		m.color = color;
		this.marks.add(m);
		this.syncClients();
		return m;
	}

	@SuppressWarnings("unchecked")
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		if (this.hasCapability(capability, facing)) {
			return (T) this;
		}
		return null;
	}

	@Override
	public NBTTagCompound getCapabilityNBT() { return this.getNBT(); }

	public NBTTagCompound getNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for (Mark m : this.marks) {
			NBTTagCompound c = new NBTTagCompound();
			c.setInteger("type", m.type);
			c.setInteger("color", m.color);
			c.setTag("availability", m.availability.writeToNBT(new NBTTagCompound()));
			c.setBoolean("rotate", m.rotate);
			c.setBoolean("is3d", m.is3d);
			list.appendTag(c);
		}
		compound.setTag("marks", list);
		return compound;
	}

	// New
	public IMark getNewMark() {
		return new Mark();
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == MarkData.MARKDATA_CAPABILITY;
	}

	public void save() {
		this.entity.getEntityData().setTag("cnpcmarkdata", this.getNBT());
	}

	@Override
	public void setCapabilityNBT(NBTTagCompound compound) { this.setNBT(compound); }
	
	public void setNBT(NBTTagCompound compound) {
		List<Mark> marks = new ArrayList<Mark>();
		NBTTagList list = compound.getTagList("marks", 10);
		for (int i = 0; i < list.tagCount(); ++i) {
			NBTTagCompound c = list.getCompoundTagAt(i);
			Mark m = new Mark();
			m.type = c.getInteger("type");
			m.color = c.getInteger("color");
			m.availability.readFromNBT(c.getCompoundTag("availability"));
			m.rotate = c.getBoolean("rotate");
			m.is3d = c.getBoolean("is3d");
			marks.add(m);
		}
		this.marks = marks;
	}

	public void syncClients() {
		if (this.entity==null || this.entity.world==null || this.entity.world.isRemote) { return; }
		Server.sendToAll(this.entity.getServer(), EnumPacketClient.MARK_DATA, this.entity.getEntityId(), this.getNBT());
	}
}
