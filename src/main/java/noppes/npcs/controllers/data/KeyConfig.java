package noppes.npcs.controllers.data;

import java.util.List;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IKeySetting;
import noppes.npcs.controllers.KeyController;

public class KeyConfig
implements IKeySetting {
	
	public String name, category;
	private int id;
	public int keyId, modifer; // 0-none, 1-Shift, 2-Ctrl, 3-Alt
	
	public KeyConfig(int id) {
		this.name = "key.custom.name";
		this.category = "key.custom.category";
		this.keyId = 44;
		this.modifer = 2;
		if (id<0) { id *= -1; }
		this.id = id;
	}
	
	@Override
	public String getName() { return this.name; }

	@Override
	public void setName(String name) {
		if (name==null || name.isEmpty()) { name = "key.custom.name"; }
		this.name = name;
		KeyController.getInstance().update(this.id);
	}

	@Override
	public String getCategory() { return this.category; }

	@Override
	public void setCategory(String name) {
		if (name==null || name.isEmpty()) { name = "key.custom.category"; }
		this.category = name;
		KeyController.getInstance().update(this.id);
	}

	@Override
	public int getId() { return this.id; }

	@Override
	public int getKeyId() { return this.keyId; }
	
	@Override
	public int getModiferType() { return this.modifer; }
	
	@Override
	public void setModiferType(int type) {
		if (type<0 || type>3) {
			throw new CustomNPCsException("Modifer Type must be between 0 and 3");
		}
		this.modifer = type;
	}

	@Override
	public void setKeyId(int keyId) {
		if (keyId<2) {
			throw new CustomNPCsException("Key ID:"+keyId+" must be greater than 2");
		}
		if (keyId==157 || keyId==29 || keyId==54 || keyId==42 || keyId==184 || keyId==56) {
			throw new CustomNPCsException("Key ID:"+keyId+" cannot be of type Ctrl, Alt or Shift");
		}
		this.keyId = keyId;
		KeyController.getInstance().update(this.id);
	}
	
	@Override
	public INbt getNbt() { return NpcAPI.Instance().getINbt(this.write()); }
	
	@Override
	public void setNbt(INbt nbt) { this.read(nbt.getMCNBT()); }
	
	public void read(NBTTagCompound nbtKey) {
		this.name = nbtKey.getString("Name");
		this.category = nbtKey.getString("Category");
		this.id = nbtKey.getInteger("ID");
		this.keyId = nbtKey.getInteger("KeyID");
		if (this.keyId<0) { this.keyId *=-1; }
		if (this.keyId<2 || this.keyId==157 || this.keyId==29 || this.keyId==54 || this.keyId==42 || this.keyId==184 || this.keyId==56) {
			this.keyId = 44;
		}
		this.modifer = nbtKey.getInteger("ModiferType") % 4;
		if (this.modifer<0) { this.modifer *=-1; }
	}

	public NBTTagCompound write() {
		NBTTagCompound nbtKey = new NBTTagCompound();
		nbtKey.setString("Name", this.name);
		nbtKey.setString("Category", this.category);
		nbtKey.setInteger("KeyID", this.keyId);
		nbtKey.setInteger("ModiferType", this.modifer);
		nbtKey.setInteger("ID", this.id);
		return nbtKey;
	}

	public boolean isActive(int key, List<Integer> keyPress) {
		if (this.keyId!=key) { return false; }
		switch(this.modifer) {
			case 1: return keyPress.contains(54) || keyPress.contains(42);
			case 2: return keyPress.contains(157) || keyPress.contains(29);
			case 3: return keyPress.contains(184) || keyPress.contains(56);
			default: return true;
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof KeyConfig)) { return false; }
		if (obj == this) { return true; }
		KeyConfig key = (KeyConfig) obj;
		if (this.id!=key.id || this.keyId!=key.keyId || this.modifer!=key.modifer) { return false; }
		if (!this.name.equals(key.name) || !this.category.equals(key.category)) { return false; }
		return true;
	}

	@Override
	public String toString() {
		return "KeyConfig { ID: "+this.id+"; keyID: "+this.keyId+"; modiferType: "+this.modifer+", name: \""+this.name+"\"; category: \""+this.category+"\"}";
	}
	
}
