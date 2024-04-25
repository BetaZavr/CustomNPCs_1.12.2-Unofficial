package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IGuiEntity;
import noppes.npcs.controllers.PlayerSkinController;

public class CustomGuiEntityWrapper extends CustomGuiComponentWrapper implements IGuiEntity {

	public NBTTagCompound entityNbt = null;
	IEntity<?> entity = null;

	float scale = 1.0f;
	boolean hasBorder = false, showArmor = true;

	public CustomGuiEntityWrapper() {
		this(-1, 0, 0, null);
	}

	public CustomGuiEntityWrapper(int id, int x, int y, IEntity<?> entity) {
		setId(id);
		setPos(x, y);
		this.entity = entity;
		scale = 1.0f;
	}

	@Override
	public CustomGuiEntityWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		this.setScale(nbt.getFloat("Scale"));
		hasBorder = nbt.getBoolean("HasBorder");
		showArmor = nbt.getBoolean("ShowArmor");
		entityNbt = nbt.getCompoundTag("Entity");
		if (entityNbt.getKeySet().isEmpty()) {
			entity = null;
		}
		return this;
	}

	@Override
	public IEntity<?> getEntity() {
		return entity;
	}

	@Override
	public float getScale() {
		return scale;
	}

	@Override
	public int getType() {
		return GuiComponentType.ENTITY.get();
	}

	@Override
	public boolean hasBorder() {
		return hasBorder;
	}

	@Override
	public boolean isShowArmorAndItems() {
		return showArmor;
	}

	@Override
	public void setBorder(boolean hasBorder) {
		this.hasBorder = hasBorder;
	}

	@Override
	public void setEntity(IEntity<?> entity) {
		this.entity = entity;
	}

	@Override
	public void setScale(float scale) {
		if (scale < 0) {
			scale *= -1.0f;
		}
		this.scale = scale;
	}

	@Override
	public void setShowArmorAndItems(boolean show) {
		showArmor = show;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setFloat("Scale", scale);
		nbt.setBoolean("HasBorder", hasBorder);
		nbt.setBoolean("ShowArmor", showArmor);
		entityNbt = new NBTTagCompound();
		if (entity != null && entity.getMCEntity() != null) {
			entityNbt.setBoolean("IsPlayer", entity instanceof IPlayer);
			if (entity instanceof IPlayer) {
				entity.getMCEntity().writeToNBT(entityNbt);
				if (PlayerSkinController.getInstance().playerTextures.containsKey(entity.getMCEntity().getUniqueID())) {
					entityNbt.setTag("SkinData",
							PlayerSkinController.getInstance().getNBT(entity.getMCEntity().getUniqueID()));
				}
			} else {
				entity.getMCEntity().writeToNBTAtomically(entityNbt);
			}
		}
		nbt.setTag("Entity", entityNbt);
		return nbt;
	}

}
