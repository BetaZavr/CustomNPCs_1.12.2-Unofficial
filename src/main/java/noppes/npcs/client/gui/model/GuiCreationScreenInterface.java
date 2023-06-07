package noppes.npcs.client.gui.model;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.ModelData;
import noppes.npcs.client.Client;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.mainmenu.GuiNpcDisplay;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public abstract class GuiCreationScreenInterface
extends GuiNPCInterface
implements ISubGuiListener, ISliderListener {
	
	public static String Message = "";
	private static float rotation = 0.5f;
	public int active;
	public EntityLivingBase entity;
	protected boolean hasSaving;
	protected NBTTagCompound original;
	private EntityPlayer player;
	public ModelData playerdata;
	private boolean saving;
	public int xOffset;

	public GuiCreationScreenInterface(EntityNPCInterface npc) {
		super(npc);
		this.saving = false;
		this.hasSaving = true;
		this.active = 0;
		this.xOffset = 0;
		this.original = new NBTTagCompound();
		this.playerdata = ((EntityCustomNpc) npc).modelData;
		this.original = this.playerdata.writeToNBT();
		this.xSize = 400;
		this.ySize = 240;
		this.xOffset = 140;
		this.player = (EntityPlayer) Minecraft.getMinecraft().player;
		this.closeOnEsc = true;
	}

	@Override
	protected void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if (btn.id == 1) {
			this.openGui(new GuiCreationEntities(this.npc));
		}
		if (btn.id == 2) {
			if (this.entity == null) {
				this.openGui(new GuiCreationParts(this.npc));
			} else {
				this.openGui(new GuiCreationExtra(this.npc));
			}
		}
		if (btn.id == 3) {
			this.openGui(new GuiCreationScale(this.npc));
		}
		if (btn.id == 4) {
			this.setSubGui(new GuiPresetSave(this, this.playerdata));
		}
		if (btn.id == 5) {
			this.openGui(new GuiCreationLoad(this.npc));
		}
		if (btn.id == 66) {
			this.save();
			NoppesUtil.openGUI(this.player, new GuiNpcDisplay(this.npc));
		}
	}

	@Override
	public void drawScreen(int x, int y, float f) {
		super.drawScreen(x, y, f);
		this.entity = this.playerdata.getEntity(this.npc);
		EntityLivingBase entity = this.entity;
		if (entity == null) {
			entity = this.npc;
		} else {
			EntityUtil.Copy(this.npc, entity);
		}
		this.drawNpc(entity, this.xOffset + 200, 200, 2.0f, (int) (GuiCreationScreenInterface.rotation * 360.0f - 180.0f), 0, true);
	}

	@Override
	public void initGui() {
		super.initGui();
		this.entity = this.playerdata.getEntity(this.npc);
		Keyboard.enableRepeatEvents(true);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 62, this.guiTop, 60, 20, "gui.entity"));
		if (this.entity == null) {
			this.addButton(new GuiNpcButton(2, this.guiLeft, this.guiTop + 23, 60, 20, "gui.parts"));
		} else if (!(this.entity instanceof EntityNPCInterface)) {
			GuiCreationExtra gui = new GuiCreationExtra(this.npc);
			gui.playerdata = this.playerdata;
			if (!gui.getData(this.entity).isEmpty()) {
				this.addButton(new GuiNpcButton(2, this.guiLeft, this.guiTop + 23, 60, 20, "gui.extra"));
			} else if (this.active == 2) {
				this.mc.displayGuiScreen((GuiScreen) new GuiCreationEntities(this.npc));
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
			this.openGui(new GuiCreationEntities(this.npc));
			return;
		}
		this.getButton(this.active).enabled = false;
		this.addButton(new GuiNpcButton(66, this.guiLeft + this.xSize - 20, this.guiTop, 20, 20, "X"));
		this.addLabel(new GuiNpcLabel(0, GuiCreationScreenInterface.Message, this.guiLeft + 120,
				this.guiTop + this.ySize - 10, 16711680));
		this.getLabel(0).center(this.xSize - 120);
		this.addSlider(new GuiNpcSlider(this, 500, this.guiLeft + this.xOffset + 142, this.guiTop + 210, 120, 20,
				GuiCreationScreenInterface.rotation));
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		if (!this.saving) {
			super.mouseClicked(i, j, k);
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (slider.id == 500) {
			GuiCreationScreenInterface.rotation = slider.sliderValue;
			slider.setString("" + (GuiCreationScreenInterface.rotation * 360.0f));
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
	}

	@Override
	public void onGuiClosed() {
		Keyboard.enableRepeatEvents(false);
	}

	public void openGui(GuiScreen gui) {
		this.mc.displayGuiScreen(gui);
	}

	@Override
	public void save() {
		NBTTagCompound newCompound = this.playerdata.writeToNBT();
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, this.npc.display.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.ModelDataSave, newCompound);
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		this.initGui();
	}

}
