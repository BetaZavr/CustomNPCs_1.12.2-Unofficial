package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
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

	private final HashMap<String, Integer> data = new HashMap<>();
	private GuiCustomScroll scroll;
	private SpawnData spawn = new SpawnData();
	private Entity displayNpc = null;

	public GuiNpcNaturalSpawns(EntityNPCInterface npc) {
		super(npc);
		Client.sendData(EnumPacketServer.NaturalSpawnGetAll);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch(button.id) {
			case 1: { // add
				this.save();
				String name = new TextComponentTranslation("gui.new").getFormattedText();
				while (this.data.containsKey(name)) { name += "_"; }
				SpawnData spawn = new SpawnData();
				spawn.name = name;
				Client.sendData(EnumPacketServer.NaturalSpawnSave, spawn.writeNBT(new NBTTagCompound()));
				break;
			}
			case 2: { // remove
				if (!this.data.containsKey(this.scroll.getSelected())) { return; }
				Client.sendData(EnumPacketServer.NaturalSpawnRemove, this.spawn.id);
				this.spawn = new SpawnData();
				this.scroll.clear();
				displayNpc = null;
				break;
			}
			case 3: { // set biome
				this.setSubGui(new SubGuiNpcBiomes(this.spawn));
				break;
			}
			case 4: { // set liquid
				spawn.liquid = button.getValue() == 0;
				button.setHoverText("spawning.hover.liquid." + button.getValue());
				break;
			}
			case 5: { // select npc
				this.setSubGui(new GuiNpcMobSpawnerSelector());
				break;
			}
			case 25: { // nbt
				this.spawn.compound1 = new NBTTagCompound();
				displayNpc = null;
				this.initGui();
				break;
			}
			case 27: { // type
				this.spawn.type = button.getValue();
				break;
			}
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
				if (compound.hasKey("SpawnCycle", 3)) { compound.setInteger("SpawnCycle", 4); }
			}
		}
		this.initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null) { return; }
		int r, p = 0, x = 387, y = 196;
		GlStateManager.pushMatrix();
		if (this.displayNpc != null) {
			this.displayNpc.ticksExisted = this.player.ticksExisted;
			if (this.displayNpc instanceof EntityLivingBase) {
				r = (int) (3 * this.player.world.getTotalWorldTime() % 360);
			} else {
				r = 0;
				y -= 34;
				if (this.displayNpc instanceof EntityItem) {
					p = 30;
					y += 10;
				}
				if (this.displayNpc instanceof EntityItemFrame) {
					x += 16;
				}
			}
			this.drawNpc(this.displayNpc, x, y, 1.0f, r, p, 0);
		}
		Gui.drawRect(this.guiLeft + x - 30, this.guiTop + y - 77, this.guiLeft + x + 31, this.guiTop + y + 9, new Color(0xFF808080).getRGB());
		Gui.drawRect(this.guiLeft + x - 29, this.guiTop + y - 76, this.guiLeft + x + 30, this.guiTop + y + 8, new Color(0xFF000000).getRGB());
		GlStateManager.popMatrix();
	}
	
	private String getTitle(NBTTagCompound compound) {
		displayNpc = EntityList.createEntityFromNBT(compound, this.mc.world);
		if (displayNpc != null) { return displayNpc.getName(); }
		return "gui.selectnpc";
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(143, 208); }
		this.scroll.guiLeft = this.guiLeft + 214;
		this.scroll.guiTop = this.guiTop + 4;
		this.addScroll(this.scroll);
		GuiNpcButton button = new GuiNpcButton(1, this.guiLeft + 358, this.guiTop + 38, 58, 20, "gui.add");
		button.setHoverText("spawning.hover.add");
		addButton(button);
		button = new GuiNpcButton(2, this.guiLeft + 358, this.guiTop + 61, 58, 20, "gui.remove");
		button.setHoverText("spawning.hover.del");
		addButton(button);
		if (this.spawn.id >= 0) { showSpawn(); }
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
	}

	@Override
	public void mouseDragged(GuiNpcSlider guiNpcSlider) {
		this.spawn.itemWeight = (int) (guiNpcSlider.sliderValue * 100.0f);
		if (this.spawn.itemWeight < 1) { this.spawn.itemWeight = 1; }
		guiNpcSlider.displayString = new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ": " + this.spawn.itemWeight + "%";
		if (this.getTextField(2) != null) { this.getTextField(2).setText("" + this.spawn.itemWeight); }
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
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			this.save();
			String selected = this.scroll.getSelected();
			this.spawn = new SpawnData();
			Client.sendData(EnumPacketServer.NaturalSpawnGet, this.data.get(selected));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		String name = this.scroll.getSelected();
		this.data.clear();
		this.data.putAll(data);
		this.scroll.setList(list);
		if (name != null) { this.scroll.setSelected(name); }
		this.initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.spawn.readNBT(compound);
		this.setSelected(this.spawn.name);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) { }

	private void showSpawn() {
		int lId = 0;
		int x = this.guiLeft + 5;
		int y = this.guiTop + 5;
		this.addLabel(new GuiNpcLabel(lId++, "gui.title", x, y + 5));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, this.fontRenderer, x + 56, y, 150, 20, this.spawn.name);
		textField.setHoverText("spawning.hover.name");
		addTextField(textField);
		
		this.addLabel(new GuiNpcLabel(lId++, "spawning.biomes", x, (y += 22) + 5));
		GuiNpcButton button = new GuiNpcButton(3, x + 156, y, 50, 20, "selectServer.edit");
		if (this.spawn.biomes.isEmpty()) { button.layerColor = new Color(0xFFF02020).getRGB(); }
		button.setHoverText("spawning.hover.biomes");
		addButton(button);

		GuiNpcSlider slider = new GuiNpcSlider(this, 4, x, y += 22, 160, 20, (float) this.spawn.itemWeight / 100.0f);
		slider.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText());
		addSlider(slider);

		textField = new GuiNpcTextField(2, this, this.fontRenderer, x + 163, y, 42, 20, "" + this.spawn.itemWeight);
		textField.setMinMaxDefault(1, 100, this.spawn.itemWeight);
		textField.setHoverText("spawning.hover.chance");
		addTextField(textField);
		
		this.addLabel(new GuiNpcLabel(lId++, "gui.type", x, (y += 22) + 5));
		button = new GuiNpcButton(27, x + 86, y, 120, 20, new String[] { "spawner.any", "spawner.dark", "spawner.light" }, this.spawn.type);
		button.setHoverText("spawning.hover.type");
		addButton(button);

		button = new GuiNpcButton(5, x, y += 22, 184, 20, this.getTitle(this.spawn.compound1));
		button.setHoverText("spawning.hover.sel.npc");
		addButton(button);
		button = new GuiNpcButton(25, x + 186, y, 20, 20, "X");
		button.setHoverText("spawning.hover.del.npc");
		addButton(button);

		button = new GuiNpcButton(4, x, y += 22, 80, 20, new String[] { "spawning.liquid.0", "spawning.liquid.1" }, this.spawn.liquid ? 0 : 1);
		button.setHoverText("spawning.hover.liquid." + button.getValue());
		addButton(button);

		this.addLabel(new GuiNpcLabel(lId++, "spawning.group", x, (y += 22) + 5));
		textField = new GuiNpcTextField(3, this, this.fontRenderer, x + 164, y, 42, 20, "" + this.spawn.group);
		textField.setMinMaxDefault(1, 8, this.spawn.group);
		textField.setHoverText("spawning.hover.group");
		addTextField(textField);

		this.addLabel(new GuiNpcLabel(lId, "spawning.range", x, (y += 22) + 5));
		textField = new GuiNpcTextField(4, this, this.fontRenderer, x + 164, y, 42, 20, "" + this.spawn.range);
		textField.setMinMaxDefault(1, 16, this.spawn.range);
		textField.setHoverText("spawning.hover.range");
		addTextField(textField);
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getId() == 1) {
			String name = textField.getText();
			if (name.isEmpty() || this.data.containsKey(name)) {
				textField.setText(this.spawn.name);
			} else {
				String old = this.spawn.name;
				this.data.remove(old);
				this.spawn.name = name;
				this.data.put(this.spawn.name, this.spawn.id);
				this.scroll.replace(old, this.spawn.name);
			}
		}
		else if (textField.getId() == 2) {
			this.spawn.itemWeight = textField.getInteger();
			if (this.getSlider(4) != null) { this.getSlider(4).displayString = new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ": " + this.spawn.itemWeight; }
		}
		else if (textField.getId() == 3) {
			this.spawn.group = textField.getInteger();
		}
		else if (textField.getId() == 4) {
			this.spawn.range = textField.getInteger();
		}
	}
}
