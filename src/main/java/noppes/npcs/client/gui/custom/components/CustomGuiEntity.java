package noppes.npcs.client.gui.custom.components;

import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.mojang.authlib.minecraft.MinecraftProfileTexture.Type;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiEntityWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.controllers.PlayerSkinController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.mixin.client.network.INetHandlerPlayClientMixin;
import noppes.npcs.util.Util;

public class CustomGuiEntity
extends Gui
implements IGuiComponent {

	public static CustomGuiEntity fromComponent(CustomGuiEntityWrapper component) {
		CustomGuiEntity entt = new CustomGuiEntity(component.getId(), component.getPosX(), component.getPosY(), component.getScale(), component.hasBorder(), component.isShowArmorAndItems(), component.entityNbt, component.rotType, component.rotYaw, component.rotPitch);
		if (component.hasHoverText()) {
			entt.hoverText = component.getHoverText();
			entt.hoverStack = component.getHoverStack();
		}
		return entt;
	}

	int id, x, y, width = 53, height = 70, rotType, rotYaw, rotPitch;
	long initTime = System.currentTimeMillis();
	boolean hasBorder, showArmor;
	String[] hoverText;
	IItemStack hoverStack;
	GuiCustom parent;
	float scale;
	final int[] offsets;
	NBTTagCompound entityNbt;
	public Entity entity;

	public CustomGuiEntity(int id, int x, int y, float scale, boolean hasBorder, boolean showArmor, NBTTagCompound entityNbt, int rotType, int rotYaw, int rotPitch) {
		this.id = id;
		this.x = GuiCustom.guiLeft + x;
		this.y = GuiCustom.guiTop + y;
		this.scale = scale;
		this.hasBorder = hasBorder;
		this.showArmor = showArmor;
		offsets = new int[] { 0, 0 };
		this.entityNbt = entityNbt;
		this.rotType = rotType;
		this.rotYaw = rotYaw;
		this.rotPitch = rotPitch;
	}

	private void createEntity(Minecraft mc) {
		if (entityNbt.getBoolean("IsPlayer")) {
			EntityNPCInterface npc = (EntityNPCInterface) EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), mc.world);
			if (npc instanceof EntityCustomNpc) {
				((EntityCustomNpc) npc).modelData.eyes.type = -1;
				UUID uuid = entityNbt.getUniqueId("UUID");
				if (entityNbt.hasKey("SkinData", 10)) {
					NBTTagList list = entityNbt.getCompoundTag("SkinData").getTagList("Textures", 10);
					for (int i = 0; i < list.tagCount(); i++) {
						NBTTagCompound nbtSkin = list.getCompoundTagAt(i);
						if (nbtSkin.getString("Type").equalsIgnoreCase("SKIN")) {
							npc.display.setSkinTexture(nbtSkin.getString("Location"));
						}
						if (nbtSkin.getString("Type").equalsIgnoreCase("CAPE")) {
							npc.display.setCapeTexture(nbtSkin.getString("Location"));
						}
					}

				} else if (PlayerSkinController.getInstance().playerTextures.containsKey(uuid)) {
					Map<Type, ResourceLocation> data = PlayerSkinController.getInstance().getData(uuid);
					if (data != null) {
						if (data.containsKey(Type.SKIN)) {
							npc.display.setSkinTexture(data.get(Type.SKIN).toString());
						}
						if (data.containsKey(Type.CAPE)) {
							npc.display.setCapeTexture(data.get(Type.CAPE).toString());
						}
					}
				} else {
					NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getConnection();
					if (netHandler != null) {
						Map<UUID, NetworkPlayerInfo> playerInfoMap = ((INetHandlerPlayClientMixin) netHandler).npcs$getplayerInfoMap();
						if (playerInfoMap.containsKey(uuid)) {
							NetworkPlayerInfo npi = playerInfoMap.get(uuid);
                            npi.getLocationSkin();
                            npc.display.setSkinTexture(npi.getLocationSkin().toString());
                            if (npi.getLocationCape() != null) {
								npc.display.setCapeTexture(npi.getLocationCape().toString());
							}
						}
					}
				}
				if (showArmor) {
					NBTTagList inv = entityNbt.getTagList("Inventory", 10);
					int mainStackSlot = entityNbt.getInteger("SelectedItemSlot");
					for (int i = 0; i < inv.tagCount(); i++) {
						NBTTagCompound nbtSlotStack = inv.getCompoundTagAt(i);
						if (nbtSlotStack.getByte("Slot") == 100) {
							npc.inventory.armor.put(3, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
						if (nbtSlotStack.getByte("Slot") == 101) {
							npc.inventory.armor.put(2, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
						if (nbtSlotStack.getByte("Slot") == 102) {
							npc.inventory.armor.put(1, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
						if (nbtSlotStack.getByte("Slot") == 103) {
							npc.inventory.armor.put(0, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
						if (nbtSlotStack.getByte("Slot") == mainStackSlot) {
							npc.inventory.weapons.put(0, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
						if (nbtSlotStack.getByte("Slot") == -106 || nbtSlotStack.getByte("Slot") == 106) {
							npc.inventory.weapons.put(2, Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbtSlotStack)));
						}
					}
				}
				entity = Util.instance.copyToGUI(npc, mc.world, false);
			}
		} else {
			entity = EntityList.createEntityFromNBT(entityNbt, mc.world);
			if (entity != null) {
				entity.rotationYaw = 0;
				entity.prevRotationYaw = 0;
				entity.setRotationYawHead(0);
				entity.rotationPitch = 0;
				entity.prevRotationPitch = 0;
				if (!showArmor) {
					for (EntityEquipmentSlot ees : EntityEquipmentSlot.values()) {
						entity.setItemStackToSlot(ees, ItemStack.EMPTY);
					}
				}
			}
		}
		if (entity instanceof EntityNPCInterface) {
			entity = Util.instance.copyToGUI((EntityNPCInterface) entity, mc.world, false);
			String skin = ((EntityNPCInterface) entity).display.getSkinTexture();
			if (skin.equals("minecraft:textures/entity/alex.png") || skin.toLowerCase().contains("alex") || skin.toLowerCase().contains("/female")) {
				((EntityNPCInterface) entity).display.setModel("customnpcs:customnpcalex");
			}
		}
	}

	private void drawEntity(Minecraft mc, int mouseX, int mouseY) {
		EntityNPCInterface npc = null;
		if (entity instanceof EntityNPCInterface) {
			npc = (EntityNPCInterface) entity;
		}
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableColorMaterial();

		GlStateManager.pushMatrix();
		GlStateManager.translate(x + 26.5f * scale, y + 65.0f * scale, 350.0f);
		if (entity.height > 2.4) { scale = 2.0f / entity.height; }
		GlStateManager.scale(-30.0f * scale, 30.0f * scale, 30.0f * scale);
		GlStateManager.rotate(180.0f, 0.0f, 0.0f, 1.0f);
		RenderHelper.enableStandardItemLighting();
		float f2 = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).renderYawOffset : entity.rotationYaw;
		float f3 = entity.rotationYaw;
		float f4 = entity.rotationPitch;
		float f5 = entity instanceof EntityLivingBase ? ((EntityLivingBase) entity).rotationYawHead : entity.rotationYaw;
		float f6 = this.rotType == 0 ? 0 : this.rotType == 1 ? x + (26.5f * scale) - mouseX : this.rotYaw;
		float f7 = this.rotType == 0 ? 0 : this.rotType == 1 ? y + (15.0f + scale) - mouseY : this.rotPitch;
		
		int orientation = 0;
		if (npc != null) {
			orientation = npc.ais.orientation;
			npc.ais.orientation = 0;
		}
		if (this.rotType == 1) {
			GlStateManager.rotate((float) (-Math.atan(f6 / 400.0f) * 20.0f), 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate((float) (-Math.atan(f7 / 40.0f) * 20.0f), 1.0f, 0.0f, 0.0f);
			entity.rotationYaw = (float) (Math.atan(f6 / 80.0f) * 40.0f + 0);
			entity.rotationPitch = (float) (-Math.atan(f7 / 40.0f) * 20.0f);
		}
		else if (this.rotType == 2) {
			GlStateManager.rotate(f6, 0.0f, 1.0f, 0.0f);
			GlStateManager.rotate(f7, 1.0f, 0.0f, 0.0f);
			entity.rotationYaw = f6;
			entity.rotationPitch = f7;
		}
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).renderYawOffset = this.rotType == 2 ? f6 : 0;
			((EntityLivingBase) entity).rotationYawHead = entity.rotationYaw;
		}
		mc.getRenderManager().playerViewY = 180.0f;
		mc.getRenderManager().renderEntity(entity, 0.0, 0.0, 0.0, 0.0f, 1.0f, false);
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).renderYawOffset = f2;
			((EntityLivingBase) entity).prevRenderYawOffset = f2;
		}
		entity.rotationYaw = f3;
		entity.prevRotationYaw = f3;
		entity.rotationPitch = f4;
		entity.prevRotationPitch = f4;
		if (entity instanceof EntityLivingBase) {
			((EntityLivingBase) entity).rotationYawHead = f5;
			((EntityLivingBase) entity).prevRotationYawHead = f5;
		}
		if (npc != null) {
			npc.ais.orientation = orientation;
		}
		GlStateManager.popMatrix();

		RenderHelper.disableStandardItemLighting();
		GlStateManager.disableRescaleNormal();
		GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GlStateManager.disableTexture2D();
		GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	@Override
	public int getId() {
		return id;
	}

	@Override
	public int[] getPosXY() {
		return new int[] { this.x, this.y };
	}

	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch (offsetType) {
		case 1: { // left down
			this.offsets[0] = 0;
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		case 2: { // right up
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = 0;
			break;
		}
		case 3: { // right down
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		default: { // left up
			this.offsets[0] = 0;
			this.offsets[1] = 0;
		}
		}
	}

	@Override
	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		if (entity == null && (entityNbt == null || entityNbt.getKeySet().isEmpty())) {
			return;
		}
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hasBorder) {
			GlStateManager.pushMatrix();
			GlStateManager.translate(x, y, 0.0f);
			GlStateManager.scale(scale, scale, 0.0f);
			this.drawGradientRect(-1, -1, width + 1, height + 1, 0xFF808080, 0xFF808080);
			this.drawGradientRect(0, 0, width, height, 0xFF000000, 0xFF000000);
			GlStateManager.popMatrix();
		}
		if (entity == null) {
			createEntity(mc);
		}
		if (entity != null) {
			entity.ticksExisted = (int) ((System.currentTimeMillis() - initTime) / 50L);
			entity.onUpdate();
			GlStateManager.pushMatrix();
			drawEntity(mc, mouseX, mouseY);
			GlStateManager.popMatrix();
		}
		if (hovered) {
			if (hoverText != null && hoverText.length > 0) { parent.hoverText = hoverText; }
			if (hoverStack != null && !hoverStack.isEmpty()) { parent.hoverStack = hoverStack.getMCItemStack(); }
		}
	}

	@Override
	public void setParent(GuiCustom gui) {
		parent = gui;
	}

	@Override
	public void setPosXY(int newX, int newY) {
		this.x = newX;
		this.y = newY;
	}

	@Override
	public ICustomGuiComponent toComponent() {
		CustomGuiEntityWrapper component = new CustomGuiEntityWrapper(id, x, y, Objects.requireNonNull(NpcAPI.Instance()).getIEntity(entity));
		component.entityNbt = this.entityNbt;
		component.setHoverText(this.hoverText);
		return component;
	}

}
