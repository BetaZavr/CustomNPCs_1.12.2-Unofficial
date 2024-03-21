package noppes.npcs.client.gui.global;

import java.util.HashMap;
import java.util.Vector;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.GuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.SubGuiNpcBiomes;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcNaturalSpawns
extends GuiNPCInterface2
implements IGuiData, IScrollData, ITextfieldListener, ICustomScrollListener, ISliderListener {
	
	private HashMap<String, Integer> data;
	private GuiCustomScroll scroll;
	private SpawnData spawn;

	public GuiNpcNaturalSpawns(EntityNPCInterface npc) {
		super(npc);
		this.data = new HashMap<String, Integer>();
		this.spawn = new SpawnData();
		Client.sendData(EnumPacketServer.NaturalSpawnGetAll, new Object[0]);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		int id = button.id;
		if (id == 1) {
			this.save();
			String name;
			for (name = new TextComponentTranslation("gui.new").getFormattedText(); this.data
					.containsKey(name); name += "_") {
			}
			SpawnData spawn = new SpawnData();
			spawn.name = name;
			Client.sendData(EnumPacketServer.NaturalSpawnSave, spawn.writeNBT(new NBTTagCompound()));
		}
		if (id == 2 && this.data.containsKey(this.scroll.getSelected())) {
			Client.sendData(EnumPacketServer.NaturalSpawnRemove, this.spawn.id);
			this.spawn = new SpawnData();
			this.scroll.clear();
		}
		if (id == 3) {
			this.setSubGui(new SubGuiNpcBiomes(this.spawn));
		}
		if (id == 5) {
			this.setSubGui(new GuiNpcMobSpawnerSelector());
		}
		if (id == 25) {
			this.spawn.compound1 = new NBTTagCompound();
			this.initGui();
		}
		if (id == 27) {
			this.spawn.type = button.getValue();
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof GuiNpcMobSpawnerSelector) {
			GuiNpcMobSpawnerSelector selector = (GuiNpcMobSpawnerSelector) gui;
			NBTTagCompound compound = selector.getCompound();
			if (compound != null) {
				this.spawn.compound1 = compound;
			}
			this.initGui();
		}
	}

	private String getTitle(NBTTagCompound compound) {
		if (compound != null && compound.hasKey("ClonedName")) {
			return compound.getString("ClonedName");
		}
		return "gui.selectnpc";
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(143, 208);
		}
		this.scroll.guiLeft = this.guiLeft + 214;
		this.scroll.guiTop = this.guiTop + 4;
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.add"));
		this.addButton(new GuiNpcButton(2, this.guiLeft + 358, this.guiTop + 61, 58, 20, "gui.remove"));
		if (this.spawn.id >= 0) {
			this.showSpawn();
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider guiNpcSlider) {
		guiNpcSlider.displayString = new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ": "
				+ (guiNpcSlider.sliderValue * 100.0f);
	}

	@Override
	public void mousePressed(GuiNpcSlider guiNpcSlider) {
	}

	@Override
	public void mouseReleased(GuiNpcSlider guiNpcSlider) {
		this.spawn.itemWeight = (int) (guiNpcSlider.sliderValue * 100.0f);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (this.spawn.id >= 0) {
			Client.sendData(EnumPacketServer.NaturalSpawnSave, this.spawn.writeNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		if (guiCustomScroll.id == 0) {
			this.save();
			String selected = this.scroll.getSelected();
			this.spawn = new SpawnData();
			Client.sendData(EnumPacketServer.NaturalSpawnGet, this.data.get(selected));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = this.scroll.getSelected();
		this.data.clear();
		this.data.putAll(data);
		this.scroll.setList(list);
		if (name != null) {
			this.scroll.setSelected(name);
		}
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.spawn.readNBT(compound);
		this.setSelected(this.spawn.name);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
	}

	private void showSpawn() {
		this.addLabel(new GuiNpcLabel(1, "gui.title", this.guiLeft + 4, this.guiTop + 8));
		this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 60, this.guiTop + 3, 140, 20,
				this.spawn.name));
		this.addLabel(new GuiNpcLabel(3, "spawning.biomes", this.guiLeft + 4, this.guiTop + 30));
		this.addButton(new GuiNpcButton(3, this.guiLeft + 120, this.guiTop + 25, 50, 20, "selectServer.edit"));
		this.addSlider(
				new GuiNpcSlider(this, 4, this.guiLeft + 4, this.guiTop + 47, 180, 20, this.spawn.itemWeight / 100.0f));
		int y = this.guiTop + 70;
		this.addButton(new GuiNpcButton(25, this.guiLeft + 14, y, 20, 20, "X"));
		this.addLabel(new GuiNpcLabel(5, "1:", this.guiLeft + 4, y + 5));
		this.addButton(new GuiNpcButton(5, this.guiLeft + 36, y, 170, 20, this.getTitle(this.spawn.compound1)));
		this.addLabel(new GuiNpcLabel(26, "gui.type", this.guiLeft + 4, this.guiTop + 100));
		this.addButton(new GuiNpcButton(27, this.guiLeft + 70, this.guiTop + 93, 120, 20,
				new String[] { "spawner.any", "spawner.dark", "spawner.light" }, this.spawn.type));
	}

	@Override
	public void unFocused(GuiNpcTextField guiNpcTextField) {
		String name = guiNpcTextField.getText();
		if (name.isEmpty() || this.data.containsKey(name)) {
			guiNpcTextField.setText(this.spawn.name);
		} else {
			String old = this.spawn.name;
			this.data.remove(old);
			this.spawn.name = name;
			this.data.put(this.spawn.name, this.spawn.id);
			this.scroll.replace(old, this.spawn.name);
		}
	}
	
	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui==null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}
}
