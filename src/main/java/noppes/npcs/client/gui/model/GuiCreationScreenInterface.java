package noppes.npcs.client.gui.model;

import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.model.GuiCreationParts.GuiPartEyes;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public abstract class GuiCreationScreenInterface extends GuiContainerNPCInterface
		implements ISliderListener {

	protected static float rotation = 0.5f;
	protected final boolean saving = false;
	protected boolean hasSaving = true;
	protected NBTTagCompound original;
	public static String Message = "";
	public EntityLivingBase entity;
	public EntityLivingBase showEntity;
	public ModelData playerdata;
	public int active = 0;
	public int xOffset = 140;

	public GuiCreationScreenInterface(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		closeOnEsc = true;
		xSize = 400;
		ySize = 240;

		playerdata = ((EntityCustomNpc) npc).modelData;
		original = playerdata.save();
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots)); break;
			case 2: {
				if (entity == null) { openGui(new GuiCreationParts(npc, (ContainerLayer) inventorySlots)); }
				else { openGui(new GuiCreationExtra(npc, (ContainerLayer) inventorySlots)); }
				break;
			}
			case 3: openGui(new GuiCreationScale(npc, (ContainerLayer) inventorySlots)); break;
			case 4: setSubGui(new SubGuiPresetSave(this, playerdata)); break;
			case 5: openGui(new GuiCreationLoad(npc, (ContainerLayer) inventorySlots)); break;
			case 6: openGui(new GuiCreationLayers(npc, (ContainerLayer) inventorySlots)); break;
			case 66: save(); NoppesUtil.openGUI(player, new GuiNpcDisplay(npc)); break;
		}
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (keyCode == Keyboard.KEY_ESCAPE) {
			NoppesUtil.openGUI(player, new GuiNpcDisplay(npc));
			return true;
		}
		return bo;
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		entity = playerdata.getEntity(npc);
		showEntity = entity;
		if (showEntity == null && npc != null || (showEntity == npc)) {
			showEntity = Util.instance.copyToGUI(npc, mc.world, false);
			((EntityNPCInterface) showEntity).display.setSize(5);
		}
		else { EntityUtil.Copy(npc, showEntity); }
		if (subgui != null) { return; }
		if (showEntity instanceof EntityNPCInterface) {
			if (showEntity.equals(npc)) {
				NBTTagCompound npcNbt = new NBTTagCompound();
				npc.writeEntityToNBT(npcNbt);
				npc.writeToNBTOptional(npcNbt);
				Entity e = EntityList.createEntityFromNBT(npcNbt, mc.world);
				if (!(e instanceof EntityNPCInterface)) {
					e = EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), mc.world);
					if (e instanceof EntityNPCInterface) { e.readFromNBT(npcNbt); }
				}
				if (e instanceof EntityNPCInterface) {
					showEntity = (EntityNPCInterface) e;
				}
			}
			EntityNPCInterface npcIn = (EntityNPCInterface) showEntity;
			npcIn.ais.setStandingType(1);
			npcIn.ticksExisted = npc.ticksExisted;
			if (npcIn instanceof EntityCustomNpc && npc instanceof EntityCustomNpc
					&& ((EntityCustomNpc) npcIn).modelData != null
					&& ((EntityCustomNpc) npc).modelData != null) {
				((EntityCustomNpc) npcIn).modelData.entity = ((EntityCustomNpc) npc).modelData.entity;
			}
			npcIn.rotationYaw = 0;
			npcIn.prevRotationYaw = 0;
			npcIn.rotationYawHead = 0;
			npcIn.rotationPitch = 0;
			npcIn.prevRotationPitch = 0;
			npcIn.ais.orientation = 0;
			if (this instanceof GuiCreationParts && ((GuiCreationParts) this).getPart() instanceof GuiPartEyes) {
				npcIn.lookPos[0] = ValueUtil.correctInt(mouseX - guiLeft - 350, -45, 45);
				npcIn.lookPos[1] = ValueUtil.correctInt((mouseY - guiTop - 135) * -1, -45, 45);
			} else {
				npcIn.lookPos[0] = ValueUtil.correctInt(mouseX - guiLeft - 340, -45, 45);
				npcIn.lookPos[1] = ValueUtil.correctInt((mouseY - guiTop - 100) * -1, -45, 45);
			}
			npcIn.display.setShowName(1);
			MarkData.get(npcIn).marks.clear();
		}
		if (this instanceof GuiCreationParts && ((GuiCreationParts) this).getPart() instanceof GuiPartEyes) {
			showEntity.ticksExisted = player.ticksExisted;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, -300.0f);
			drawNpc(showEntity, xOffset + 210, 425, 6.0f, 0, 0, 0);
			GlStateManager.popMatrix();
		}
		else {
			drawNpc(showEntity, xOffset + 200, 200, 2.0f, (int) (GuiCreationScreenInterface.rotation * 360.0f - 180.0f), 0, 1);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		entity = playerdata.getEntity(npc);
		Keyboard.enableRepeatEvents(true);
		addButton(new GuiNpcButton(1, guiLeft + 62, guiTop, 60, 20, "gui.entity")
				.setHoverText("display.hover.part.entity"));
		addButton(new GuiNpcButton(6, guiLeft, guiTop, 60, 20, "gui.layers")
				.setHoverText("display.hover.part.layers"));
		if (entity == null) {
			addButton(new GuiNpcButton(2, guiLeft, guiTop + 23, 60, 20, "gui.parts")
					.setHoverText("display.hover.extra"));
		}
		else if (!(entity instanceof EntityNPCInterface)) {
			GuiCreationExtra gui = new GuiCreationExtra(npc, (ContainerLayer) inventorySlots);
			gui.playerdata = playerdata;
			if (!gui.getData(entity).isEmpty()) {
				addButton(new GuiNpcButton(2, guiLeft, guiTop + 23, 60, 20, "gui.extra")
						.setHoverText("display.hover.parts"));
			}
			else if (active == 2) {
				openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots));
				return;
			}
		}
		if (entity == null) {
			addButton(new GuiNpcButton(3, guiLeft + 62, guiTop + 23, 60, 20, "gui.scale")
					.setHoverText("display.hover.part.size"));
		}
		if (hasSaving) {
			addButton(new GuiNpcButton(4, guiLeft, guiTop + ySize - 24, 60, 20, "gui.save")
					.setHoverText("display.hover.part.save"));
			addButton(new GuiNpcButton(5, guiLeft + 62, guiTop + ySize - 24, 60, 20, "gui.load")
					.setHoverText("display.hover.part.load"));
		}
		if (getButton(active) == null) {
			openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots));
			return;
		}
		getButton(active).setIsEnable(false);
		addButton(new GuiNpcButton(66, guiLeft + xSize - 20, guiTop, 20, 20, "X")
				.setHoverText("hover.back"));
		addLabel(new GuiNpcLabel(0, GuiCreationScreenInterface.Message, guiLeft + 120, guiTop + ySize - 10, 16711680)
				.setCenter(xSize - 120));
		addSlider(new GuiNpcSlider(this, 500, guiLeft + xOffset + 142, guiTop + 210, 120, 20, GuiCreationScreenInterface.rotation)
				.setHoverText("display.hover.part.rotate"));
		if (showEntity instanceof EntityCustomNpc && playerdata != null){
			((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
		}
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (!saving) { return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton); }
		return false;
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (slider.getID() == 500) {
			GuiCreationScreenInterface.rotation = slider.sliderValue;
			slider.setString("" + (GuiCreationScreenInterface.rotation * 360.0f));
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) { }

	@Override
	public void mouseReleased(GuiNpcSlider slider) { }

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}

	public void openGui(GuiScreen gui) {
		ClientEvent.NextToGuiCustomNpcs event = new ClientEvent.NextToGuiCustomNpcs(npc, this, gui);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.returnGui == null || event.isCanceled()) { return; }
		mc.displayGuiScreen(event.returnGui);
		if (mc.currentScreen == null) { mc.setIngameFocus(); }
	}

	@Override
	public void save() {
		NBTTagCompound newCompound = playerdata.save();
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, npc.display.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.ModelDataSave, newCompound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		initGui();
	}

}
