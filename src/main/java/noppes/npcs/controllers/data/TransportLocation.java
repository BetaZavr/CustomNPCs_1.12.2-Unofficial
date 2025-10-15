package noppes.npcs.controllers.data;

import java.util.Collections;
import java.util.UUID;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.ITransportLocation;

public class TransportLocation implements ITransportLocation {

	public TransportCategory category;
	public int dimension, id, type;
	public long money;
	public String name;
	public BlockPos pos;
	public UUID npc;
	public final NpcMiscInventory inventory;
	public float yaw, pitch;

	public TransportLocation() {
		this.id = -1;
		this.name = "aitactics.default";
		this.type = 0;
		this.dimension = 0;
		this.npc = null;
		this.money = 0;
		this.inventory = new NpcMiscInventory(9);
		this.yaw = 0.0f;
		this.pitch = 0.0f;
	}

	public TransportLocation copy() {
		TransportLocation tl = new TransportLocation();
		tl.id = this.id;
		tl.name = this.name;
		tl.type = this.type;
		tl.dimension = this.dimension;
		tl.npc = this.npc;
		tl.money = this.money;
		tl.pos = this.pos;
        for (int i = 0; i < this.inventory.items.size(); i++) {
			tl.inventory.items.set(i, this.inventory.items.get(i).copy());
		}
		tl.category = this.category;
		tl.yaw = this.yaw;
		tl.pitch = this.yaw;
		return tl;
	}

	@Override
	public int getDimension() {
		return this.dimension;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public int getType() {
		return this.type;
	}

	@Override
	public int getX() {
		return this.pos.getX();
	}

	@Override
	public int getY() {
		return this.pos.getY();
	}

	@Override
	public int getZ() {
		return this.pos.getZ();
	}

	public boolean isDefault() {
		return this.type == 1;
	}

	public void readNBT(NBTTagCompound compound) {
		if (compound == null) {
			return;
		}
		this.id = compound.getInteger("Id");
		this.pos = new BlockPos(compound.getDouble("PosX"), compound.getDouble("PosY"), compound.getDouble("PosZ"));
		this.type = compound.getInteger("Type");
		this.dimension = compound.getInteger("Dimension");
		this.name = compound.getString("Name");
		this.npc = null;
		this.yaw = compound.getFloat("PlayerYaw");
		this.pitch = compound.getFloat("PlayerPitch");
		if (compound.hasKey("NpcUUIDMost", 4) && compound.hasKey("NpcUUIDLeast", 4)) {
			this.npc = compound.getUniqueId("NpcUUID");
		}
		this.money = compound.getLong("Cost");
		if (compound.hasKey("CostInv", 10)) {
			this.inventory.load(compound.getCompoundTag("CostInv"));
			while (this.inventory.items.size() < 9) {
				this.inventory.items.add(ItemStack.EMPTY);
			}
			while (this.inventory.items.size() > 9) {
				this.inventory.items.remove(this.inventory.items.size() - 1);
			}
		} else {
            Collections.fill(this.inventory.items, ItemStack.EMPTY);
		}
	}

	@Override
	public void setPos(int dimensionId, int x, int y, int z) {
		this.dimension = dimensionId;
		this.pos = new BlockPos(x, y, z);
	}

	@Override
	public void setType(int type) {
		if (type < 0 || type > 2) {
			throw new CustomNPCsException("Unknown location type: " + type);
		}
		this.type = type;
	}

	public NBTTagCompound writeNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setInteger("Id", this.id);
		compound.setDouble("PosX", this.pos.getX());
		compound.setDouble("PosY", this.pos.getY());
		compound.setDouble("PosZ", this.pos.getZ());
		compound.setInteger("Type", this.type);
		compound.setInteger("Dimension", this.dimension);
		compound.setString("Name", this.name);
		if (this.npc != null) {
			compound.setUniqueId("NpcUUID", this.npc);
		}
		compound.setLong("Cost", this.money);
		compound.setTag("CostInv", this.inventory.save());
		compound.setFloat("PlayerYaw", this.yaw);
		compound.setFloat("PlayerPitch", this.pitch);
		return compound;
	}

}
