package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IRoleTransporter;
import noppes.npcs.dimensions.DimensionHandler;

public class TransportLocation
implements IRoleTransporter.ITransportLocation {
	
	public TransportCategory category;
	public int dimension;
	public int id;
	public String name;
	public BlockPos pos;
	public int type;

	public TransportLocation() {
		this.id = -1;
		this.name = "default name";
		this.type = 0;
		this.dimension = 0;
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
		return compound;
	}

	@Override
	public void setPos(int dimentionID, int x, int y, int z) {
		if (!DimensionHandler.getInstance().getMapDimensionsIDs().containsValue(dimentionID)) {
			throw new CustomNPCsException("Unknown dimention ID: " + dimentionID);
		}
		this.dimension = dimentionID;
		this.pos = new BlockPos(x, y, z);
	}
}
