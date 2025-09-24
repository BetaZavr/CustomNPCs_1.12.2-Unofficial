package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IGuiEntity;
import noppes.npcs.controllers.PlayerSkinController;
import noppes.npcs.util.ValueUtil;

public class CustomGuiEntityWrapper extends CustomGuiComponentWrapper implements IGuiEntity {

	protected IEntity<?> entity;
	protected float scale = 1.0f;
	protected boolean hasBorder = false;
	protected boolean showArmor = true;
	public NBTTagCompound entityNbt = null;
	/**
	 * 0 - nope
	 * 1 - mouse
	 * 2 - set rot
	 */
	public int rotType = 0;
	public int rotYaw = 0;
	public int rotPitch = 0;

	public CustomGuiEntityWrapper() {
		this(-1, 0, 0, null);
	}

	public CustomGuiEntityWrapper(int id, int x, int y, IEntity<?> entityIn) {
		setId(id);
		setPos(x, y);
		entity = entityIn;
	}

	@Override
	public CustomGuiEntityWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setScale(nbt.getFloat("Scale"));
		hasBorder = nbt.getBoolean("HasBorder");
		showArmor = nbt.getBoolean("ShowArmor");
		entityNbt = nbt.getCompoundTag("Entity");
		if (entityNbt.getKeySet().isEmpty()) { entity = null; }
		rotType = nbt.getInteger("RotationType");
		rotYaw = nbt.getInteger("RotationYaw");
		rotPitch = nbt.getInteger("RotationPitch");
		return this;
	}

	@Override
	public IEntity<?> getEntity() { return entity; }

	@Override
	public float getScale() { return scale; }

	@Override
	public int getType() { return GuiComponentType.ENTITY.get(); }

	@Override
	public boolean hasBorder() { return hasBorder; }

	@Override
	public boolean isShowArmorAndItems() { return showArmor; }

	@Override
	public void setBorder(boolean hasBorderIn) { hasBorder = hasBorderIn; }

	@Override
	public void setEntity(IEntity<?> entityIn) { entity = entityIn; }

	@Override
	public void setScale(float scaleIn) { scale = ValueUtil.correctFloat(scaleIn, 0.0f, 10.0f); }

	@Override
	public void setShowArmorAndItems(boolean show) { showArmor = show; }

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
					entityNbt.setTag("SkinData", PlayerSkinController.getInstance().getNBT(entity.getMCEntity().getUniqueID()));
				}
			}
			else { entity.getMCEntity().writeToNBTAtomically(entityNbt); }
		}
		nbt.setTag("Entity", entityNbt);
		nbt.setInteger("RotationType", rotType);
		nbt.setInteger("RotationYaw", rotYaw);
		nbt.setInteger("RotationPitch", rotPitch);
		return nbt;
	}

	@Override
	public int getRotationType() { return rotType; }

	@Override
	public void setRotationType(int type) {
		if (type < 0) { type *= -1; }
		rotType = type % 3;
	}

	@Override
	public int getYaw() { return rotYaw; }

	@Override
	public int getPitch() { return rotPitch; }

	@Override
	public void setRotation(int yaw, int pitch) { // int horizontal, int vertical
		while (yaw < 0) { yaw += 360; }
		rotYaw = yaw % 360;
		rotPitch = pitch % 91;
	}

}
