package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumPacketServer;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface implements GuiYesNoCallback, IGuiData {
	private static boolean serverSide = false;
	private static int tab = 1;
	private NBTTagCompound compound;
	private Entity toClone;

	public GuiNpcMobSpawnerAdd(NBTTagCompound compound) {
		this.toClone = EntityList.createEntityFromNBT(compound, Minecraft.getMinecraft().world);
		this.compound = compound;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 216;
	}

	@Override
	public void buttonEvent(GuiButton guibutton) {
		int id = guibutton.id;
		if (id == 0) {
			String name = this.getTextField(0).getText();
			if (name.isEmpty()) {
				return;
			}
			int tab = ((GuiNpcButton) guibutton).getValue() + 1;
			if (!GuiNpcMobSpawnerAdd.serverSide) {
				if (ClientCloneController.Instance.getCloneData(null, name, tab) != null) {
					this.displayGuiScreen((GuiScreen) new GuiYesNo((GuiYesNoCallback) this, "",
							new TextComponentTranslation("clone.overwrite").getFormattedText(), 1));
				} else {
					this.confirmClicked(true, 0);
				}
			} else {
				Client.sendData(EnumPacketServer.ClonePreSave, name, tab);
			}
		}
		if (id == 1) {
			this.close();
		}
		if (id == 2) {
			GuiNpcMobSpawnerAdd.tab = ((GuiNpcButton) guibutton).getValue() + 1;
		}
		if (id == 3) {
			GuiNpcMobSpawnerAdd.serverSide = (((GuiNpcButton) guibutton).getValue() == 1);
		}
	}

	public void confirmClicked(boolean confirm, int id) {
		if (confirm) {
			String name = this.getTextField(0).getText();
			if (!GuiNpcMobSpawnerAdd.serverSide) {
				ClientCloneController.Instance.addClone(this.compound, name, GuiNpcMobSpawnerAdd.tab);
			} else {
				Client.sendData(EnumPacketServer.CloneSave, name, GuiNpcMobSpawnerAdd.tab);
			}
			this.close();
		} else {
			this.displayGuiScreen(this);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		String name = this.toClone.getName();
		this.addLabel(new GuiNpcLabel(0, "Save as", this.guiLeft + 4, this.guiTop + 6));
		this.addTextField(
				new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 4, this.guiTop + 18, 200, 20, name));
		this.addLabel(new GuiNpcLabel(1, "Tab", this.guiLeft + 10, this.guiTop + 50));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 40, this.guiTop + 45, 20, 20,
				new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }, GuiNpcMobSpawnerAdd.tab - 1));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 4, this.guiTop + 95,
				new String[] { "clone.client", "clone.server" }, (GuiNpcMobSpawnerAdd.serverSide ? 1 : 0)));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 70, 80, 20, "gui.save"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 86, this.guiTop + 70, 80, 20, "gui.cancel"));
	}

	@Override
	public void save() {
	}

	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NameExists")) {
			if (compound.getBoolean("NameExists")) {
				this.displayGuiScreen((GuiScreen) new GuiYesNo((GuiYesNoCallback) this, "",
						new TextComponentTranslation("clone.overwrite").getFormattedText(), 1));
			} else {
				this.confirmClicked(true, 0);
			}
		}
	}

}
