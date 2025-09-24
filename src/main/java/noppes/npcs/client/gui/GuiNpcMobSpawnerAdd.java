package noppes.npcs.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.controllers.ClientCloneController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;

import javax.annotation.Nonnull;

public class GuiNpcMobSpawnerAdd extends GuiNPCInterface
		implements GuiYesNoCallback, IGuiData, ITextfieldListener {

	protected static final String[] arrSymbols = new String[] { "\\", "/", ":", "*", "?", "\"", "<", ">", "|" };
	protected static boolean serverSide = false;
	protected static int tab = 1;
	protected final NBTTagCompound compound;
	protected final Entity toClone;

	public GuiNpcMobSpawnerAdd(NBTTagCompound nbt) {
		super();
		setBackground("menubg.png");
		xSize = 256;
		ySize = 216;

		toClone = EntityList.createEntityFromNBT(nbt, Minecraft.getMinecraft().world);
		compound = nbt;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0 : {
				String name = getTextField(0).getText();
				if (name.isEmpty()) { return; }
				int tab = button.getValue() + 1;
				if (!GuiNpcMobSpawnerAdd.serverSide) {
					if (ClientCloneController.Instance.getCloneData(null, name, tab) != null) {
						displayGuiScreen(new GuiYesNo(this, "", new TextComponentTranslation("clone.overwrite").getFormattedText(), 1));
					}
					else { confirmClicked(true, 0); }
				}
				else { Client.sendData(EnumPacketServer.ClonePreSave, name, tab); }
				break;
			}
			case 2: GuiNpcMobSpawnerAdd.tab = button.getValue() + 1; break;
			case 3: GuiNpcMobSpawnerAdd.serverSide = button.getValue() == 1; break;
			case 66: onClosed(); break;
		}
	}

	public void confirmClicked(boolean confirm, int id) {
		if (confirm) {
			String name = getTextField(0).getText();
			if (!GuiNpcMobSpawnerAdd.serverSide) { ClientCloneController.Instance.addClone(compound, name, GuiNpcMobSpawnerAdd.tab); }
			else { Client.sendData(EnumPacketServer.CloneSave, name, GuiNpcMobSpawnerAdd.tab); }
			onClosed();
		}
		else { displayGuiScreen(this); }
	}

	@Override
	public void initGui() {
		super.initGui();
		String name = toClone.getName();
		for (String c : arrSymbols) {
			while (name.contains(c)) { name = name.replace(c, "_"); }
		}
		addLabel(new GuiNpcLabel(0, "Save as", guiLeft + 4, guiTop + 6));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 4, guiTop + 18, 200, 20, name));
		addLabel(new GuiNpcLabel(1, "Tab", guiLeft + 10, guiTop + 50));
		addButton(new GuiNpcButton(2, guiLeft + 40, guiTop + 45, 20, 20, new String[] { "1", "2", "3", "4", "5", "6", "7", "8", "9" }, GuiNpcMobSpawnerAdd.tab - 1));
		addButton(new GuiNpcButton(3, guiLeft + 4, guiTop + 95, new String[] { "clone.client", "clone.server" }, (GuiNpcMobSpawnerAdd.serverSide ? 1 : 0)));
		addButton(new GuiNpcButton(0, guiLeft + 4, guiTop + 70, 80, 20, "gui.save"));
		addButton(new GuiNpcButton(66, guiLeft + 86, guiTop + 70, 80, 20, "gui.cancel").setHoverText("hover.back"));
	}

    public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("NameExists")) {
			if (compound.getBoolean("NameExists")) {
				displayGuiScreen(new GuiYesNo(this, "", new TextComponentTranslation("clone.overwrite").getFormattedText(), 1));
			}
			else { confirmClicked(true, 0); }
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		String name = textField.getText();
		for (String c : arrSymbols) {
			while (name.contains(c)) { name = name.replace(c, "_"); }
		}
		if (!textField.getText().equals(name)) { textField.setText(name); }
	}

}
