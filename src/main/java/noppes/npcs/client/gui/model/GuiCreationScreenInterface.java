package noppes.npcs.client.gui.model;

import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.api.event.ClientEvent;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.util.Util;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
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

public abstract class GuiCreationScreenInterface
extends GuiContainerNPCInterface
implements ISubGuiListener, ISliderListener {

	public static String Message = "";
	private static float rotation = 0.5f;
	public int active;
	public EntityLivingBase entity;
	protected boolean hasSaving;
	protected NBTTagCompound original;
	private final EntityPlayer player;
	public ModelData playerdata;
	private final boolean saving;
	public int xOffset;
	public EntityLivingBase showEntity;

	public GuiCreationScreenInterface(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		this.saving = false;
		this.hasSaving = true;
		this.active = 0;
		this.xOffset = 0;
		this.original = new NBTTagCompound();
		this.playerdata = ((EntityCustomNpc) npc).modelData;
		this.original = this.playerdata.save();
		this.xSize = 400;
		this.ySize = 240;
		this.xOffset = 140;
		this.player = Minecraft.getMinecraft().player;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (btn.id == 1) {
			this.openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots));
		}
		if (btn.id == 2) {
			if (this.entity == null) {
				this.openGui(new GuiCreationParts(npc, (ContainerLayer) inventorySlots));
			} else {
				this.openGui(new GuiCreationExtra(npc, (ContainerLayer) inventorySlots));
			}
		}
		if (btn.id == 3) {
			this.openGui(new GuiCreationScale(this.npc, (ContainerLayer) inventorySlots));
		}
		if (btn.id == 4) {
			this.setSubGui(new GuiPresetSave(this, this.playerdata));
		}
		if (btn.id == 5) {
			this.openGui(new GuiCreationLoad(this.npc, (ContainerLayer) inventorySlots));
		}
		if (btn.id == 66) {
			this.save();
			NoppesUtil.openGUI(this.player, new GuiNpcDisplay(this.npc));
		}
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
		if (this.subgui != null) { return; }
		if (this.showEntity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) showEntity;
			if (npc.equals(this.npc)) {
				NBTTagCompound npcNbt = new NBTTagCompound();
				this.npc.writeEntityToNBT(npcNbt);
				this.npc.writeToNBTOptional(npcNbt);
				Entity e = EntityList.createEntityFromNBT(npcNbt, this.mc.world);
				if (!(e instanceof EntityNPCInterface)) {
					e = EntityList.createEntityByIDFromName(new ResourceLocation(CustomNpcs.MODID, "customnpc"), this.mc.world);
					if (e instanceof EntityNPCInterface) { e.readFromNBT(npcNbt); }
				}
				if (e instanceof EntityNPCInterface) {
					showEntity = (EntityNPCInterface) e;
					npc = (EntityNPCInterface) e;
				}
			}
			npc.ais.setStandingType(1);
			npc.ticksExisted = 100;
			if (npc instanceof EntityCustomNpc && this.npc instanceof EntityCustomNpc
					&& ((EntityCustomNpc) npc).modelData != null
					&& ((EntityCustomNpc) this.npc).modelData != null) {
				((EntityCustomNpc) npc).modelData.entity = ((EntityCustomNpc) this.npc).modelData.entity;
			}
			npc.rotationYaw = 0;
			npc.prevRotationYaw = 0;
			npc.rotationYawHead = 0;
			npc.rotationPitch = 0;
			npc.prevRotationPitch = 0;
			npc.ais.orientation = 0;
			if (this instanceof GuiCreationParts && ((GuiCreationParts) this).getPart() instanceof GuiPartEyes) {
				npc.lookPos[0] = ValueUtil.correctInt(mouseX - guiLeft - 350, -45, 45);
				npc.lookPos[1] = ValueUtil.correctInt((mouseY - guiTop - 135) * -1, -45, 45);
			} else {
				npc.lookPos[0] = ValueUtil.correctInt(mouseX - guiLeft - 340, -45, 45);
				npc.lookPos[1] = ValueUtil.correctInt((mouseY - guiTop - 100) * -1, -45, 45);
			}
			npc.display.setShowName(1);
			MarkData.get(npc).marks.clear();
		}
		if (this instanceof GuiCreationParts && ((GuiCreationParts) this).getPart() instanceof GuiPartEyes) {
			showEntity.ticksExisted = this.player.ticksExisted;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, -300.0f);
			drawNpc(showEntity, xOffset + 210, 425, 6.0f, 0, 0, 0);
			GlStateManager.popMatrix();
		}
		else {
			this.drawNpc(this.showEntity, this.xOffset + 200, 200, 2.0f, (int) (GuiCreationScreenInterface.rotation * 360.0f - 180.0f), 0, 1);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.entity = this.playerdata.getEntity(this.npc);
		Keyboard.enableRepeatEvents(true);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 62, this.guiTop, 60, 20, "gui.entity"));

		if (this.entity == null) {
			this.addButton(new GuiNpcButton(2, this.guiLeft, this.guiTop + 23, 60, 20, "gui.parts"));
		}
		else if (!(this.entity instanceof EntityNPCInterface)) {
			GuiCreationExtra gui = new GuiCreationExtra(this.npc, (ContainerLayer) inventorySlots);
			gui.playerdata = this.playerdata;
			if (!gui.getData(this.entity).isEmpty()) {
				this.addButton(new GuiNpcButton(2, this.guiLeft, this.guiTop + 23, 60, 20, "gui.extra"));
			} else if (this.active == 2) {
				openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots));
				return;
			}
		}
		if (this.entity == null) {
			this.addButton(new GuiNpcButton(3, this.guiLeft + 62, this.guiTop + 23, 60, 20, "gui.scale"));
		}
		if (this.hasSaving) {
			this.addButton(new GuiNpcButton(4, this.guiLeft, this.guiTop + this.ySize - 24, 60, 20, "gui.save"));
			this.addButton(new GuiNpcButton(5, this.guiLeft + 62, this.guiTop + this.ySize - 24, 60, 20, "gui.load"));
		}
		if (this.getButton(this.active) == null) {
			this.openGui(new GuiCreationEntities(npc, (ContainerLayer) inventorySlots));
			return;
		}
		this.getButton(this.active).setEnabled(false);
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 20, this.guiTop, 20, 20, "X"));
		this.addLabel(new GuiNpcLabel(0, GuiCreationScreenInterface.Message, this.guiLeft + 120, this.guiTop + this.ySize - 10, 16711680));
		this.getLabel(0).setCenter(this.xSize - 120);
		this.addSlider(new GuiNpcSlider(this, 500, this.guiLeft + this.xOffset + 142, this.guiTop + 210, 120, 20, GuiCreationScreenInterface.rotation));
		if (showEntity instanceof EntityCustomNpc && playerdata != null){
			((EntityCustomNpc) showEntity).modelData.load(playerdata.save());
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		if (!this.saving) {
			super.mouseClicked(i, j, k);
		}
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		if (slider.getID() == 500) {
			GuiCreationScreenInterface.rotation = slider.getSliderValue();
			slider.setString("" + (GuiCreationScreenInterface.rotation * 360.0f));
		}
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(IGuiNpcSlider slider) {
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
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
		NBTTagCompound newCompound = this.playerdata.save();
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, this.npc.display.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.ModelDataSave, newCompound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		initGui();
	}

}
