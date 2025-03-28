package noppes.npcs.client.gui.global;

import java.awt.*;
import java.util.HashMap;
import java.util.Vector;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.GuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.SubGuiNpcBiomes;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class GuiNpcNaturalSpawns
extends GuiNPCInterface2
implements IGuiData, IScrollData, ITextfieldListener, ICustomScrollListener, ISliderListener, ISubGuiListener, GuiYesNoCallback {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected SpawnData spawn = new SpawnData();
	protected Entity displayNpc = null;
	protected int maxSize;
	protected boolean accept = false;

	public GuiNpcNaturalSpawns(EntityNPCInterface npc) {
		super(npc);
		Client.sendData(EnumPacketServer.NaturalSpawnGetAll);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch(button.getID()) {
			case 1: { // add
				if (!accept) {
					GuiYesNo guiyesno = new GuiYesNo(this,
							new TextComponentTranslation("gui.acceptMessage").getFormattedText(),
							new TextComponentTranslation("spawning.accept.message").getFormattedText(),
							0);
					displayGuiScreen(guiyesno);
					return;
				}
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
			case 6: { // select npc
				spawn.canSeeSummon = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 25: { // nbt
				this.spawn.compoundEntity = new NBTTagCompound();
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
	public void confirmClicked(boolean result, int id) {
		displayGuiScreen(this);
		if (!result) { return; }
		accept = true;
		buttonEvent(getButton(1));
	}

	@Override
	public void subGuiClosed(ISubGuiInterface gui) {
		if (gui instanceof GuiNpcMobSpawnerSelector) {
			GuiNpcMobSpawnerSelector selector = (GuiNpcMobSpawnerSelector) gui;
			NBTTagCompound compound = selector.getCompound();
			if (compound != null) {
				this.spawn.compoundEntity = compound;
				if (compound.hasKey("SpawnCycle", 3)) { compound.setInteger("SpawnCycle", 4); }
			}
		}
		initGui();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (subgui == null) {
			int x = 387, y = 196;
			GlStateManager.pushMatrix();
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			Gui.drawRect(guiLeft + x - 30, guiTop + y - 77, guiLeft + x + 31, guiTop + y + 9, 0xFF808080);
			Gui.drawRect(guiLeft + x - 29, guiTop + y - 76, guiLeft + x + 30, guiTop + y + 8, 0xFF000000);
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui == null) {
			int r, p = 0, x = 387, y = 196;
			if (displayNpc != null) {
				displayNpc.ticksExisted = player.ticksExisted;
				if (displayNpc instanceof EntityLivingBase) { r = (int) (3 * player.world.getTotalWorldTime() % 360); }
				else {
					r = 0;
					y -= 34;
					if (displayNpc instanceof EntityItem) {
						p = 30;
						y += 10;
					}
					if (displayNpc instanceof EntityItemFrame) {
						x += 16;
					}
				}
				GlStateManager.pushMatrix();
				drawNpc(displayNpc, x, y, 1.0f, r, p, 0);
				GlStateManager.popMatrix();
			}
		}
	}
	
	private String getTitle(NBTTagCompound compound) {
		displayNpc = EntityList.createEntityFromNBT(compound, this.mc.world);
		if (displayNpc != null) { return displayNpc.getName(); }
		return "gui.selectnpc";
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(143, 208); }
		scroll.guiLeft = guiLeft + 214;
		scroll.guiTop = guiTop + 4;
		addScroll(scroll);
		GuiNpcButton button = new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.add");
		button.setHoverText("spawning.hover.add");
		addButton(button);
		button = new GuiNpcButton(2, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove");
		button.setHoverText("spawning.hover.del");
		addButton(button);
		if (spawn.id < 0) { return; }
		// entity max size
		Entity entity = null;
		try { entity = EntityList.createEntityFromNBT(spawn.compoundEntity, mc.world); } catch (Exception ignored) { }
		maxSize = 50;
		if (entity instanceof EntityNPCInterface) { maxSize = 70; }
		else if (entity instanceof EntityAnimal) { maxSize = 10; }
		else if (entity instanceof EntityMob) { maxSize = 70; }
		// Spawner name
		int lId = 0;
		int x = guiLeft + 5;
		int y = guiTop + 5;
		addLabel(new GuiNpcLabel(lId++, "gui.title", x, y + 3));
		GuiNpcTextField textField = new GuiNpcTextField(1, this, fontRenderer, x + 56, y, 150, 14, spawn.name);
		textField.setHoverText("spawning.hover.name");
		addTextField(textField);
		// Biomes
		addLabel(new GuiNpcLabel(lId++, "spawning.biomes", x, (y += 17) + 3));
		button = new GuiNpcButton(3, x + 56, y, 74, 16, "selectServer.edit");
		if (spawn.biomes.isEmpty()) { button.layerColor = new Color(0xFFF02020).getRGB(); }
		button.setHoverText("spawning.hover.biomes");
		addButton(button);
		// spawner type
		addLabel(new GuiNpcLabel(lId++, "gui.type", x, (y += 18) + 3));
		button = new GuiNpcButton(27, x + 56, y, 74, 16, new String[] { "spawner.any", "spawner.dark", "spawner.light" }, spawn.type);
		button.setHoverText("spawning.hover.type");
		addButton(button);
		button = new GuiNpcButton(4, x + 132, y, 74, 16, new String[] { "spawning.liquid.0", "spawning.liquid.1" }, spawn.liquid ? 0 : 1);
		button.setHoverText("spawning.hover.liquid." + button.getValue());
		addButton(button);
		// select entity
		button = new GuiNpcButton(5, x, y += 18, 184, 16, getTitle(spawn.compoundEntity));
		button.setHoverText("spawning.hover.sel.npc");
		addButton(button);
		button = new GuiNpcButton(25, x + 186, y, 20, 16, "X");
		button.setHoverText("spawning.hover.del.npc");
		addButton(button);
		// chance
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ":", x, (y += 29) - 10));
		GuiNpcSlider slider = new GuiNpcSlider(this, 2, x, y, 160, 12, (float) spawn.itemWeight / 100.0f);
		slider.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText());
		addSlider(slider);
		textField = new GuiNpcTextField(2, this, fontRenderer, x + 163, y, 43, 12, "" + spawn.itemWeight);
		textField.setMinMaxDefault(1, 100, spawn.itemWeight);
		textField.setHoverText("spawning.hover.chance");
		addTextField(textField);
		// group size
		addLabel(new GuiNpcLabel(lId++, "spawning.group", x, (y += 25) - 10));
		slider = new GuiNpcSlider(this, 3, x, y, 160, 12, (float) spawn.group / 8.0f);
		slider.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText());
		addSlider(slider);
		textField = new GuiNpcTextField(3, this, fontRenderer, x + 163, y, 43, 12, "" + spawn.group);
		textField.setMinMaxDefault(1, 8, spawn.group);
		textField.setHoverText("spawning.hover.group");
		addTextField(textField);
		// distance
		addLabel(new GuiNpcLabel(lId, "spawning.range", x, (y += 25) - 10));
		slider = new GuiNpcSlider(this, 4, x, y, 160, 12, (float) spawn.range / 16.0f);
		slider.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText());
		addSlider(slider);
		textField = new GuiNpcTextField(4, this, fontRenderer, x + 163, y, 43, 12, "" + spawn.range);
		textField.setMinMaxDefault(1, 16, spawn.range);
		textField.setHoverText("spawning.hover.range");
		addTextField(textField);
		// maximum in player
		addLabel(new GuiNpcLabel(lId, "spawning.maximum.in", x, (y += 25) - 10));
		slider = new GuiNpcSlider(this, 5, x, y, 160, 12, (float) spawn.maxNearPlayer / (float) maxSize);
		slider.setHoverText(new TextComponentTranslation("spawning.hover.maximum.in", TextFormatting.GOLD + "" + maxSize).getFormattedText());
		addSlider(slider);
		textField = new GuiNpcTextField(5, this, fontRenderer, x + 163, y, 43, 12, "" + spawn.maxNearPlayer);
		textField.setMinMaxDefault(1, maxSize, spawn.maxNearPlayer);
		textField.setHoverText("spawning.hover.maximum.in", TextFormatting.GOLD + "" + maxSize);
		addTextField(textField);
		// player can see
		button = new GuiNpcCheckBox(6, x, y + 14, 184, 14, "spawning.can.see", "spawning.not.see", spawn.canSeeSummon);
		button.setHoverText("spawning.hover.can.see");
		addButton(button);
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
	public void mouseDragged(IGuiNpcSlider guiNpcSlider) {
		int id = guiNpcSlider.getID();
		String value;
		switch (id) {
			case 2: {
				spawn.itemWeight = ValueUtil.correctInt((int) (guiNpcSlider.getSliderValue() * 100.0f), 1, 100);
				value = "" + spawn.itemWeight;
				guiNpcSlider.setDisplayString(value + "%");
				break;
			}
			case 3: {
				spawn.group = ValueUtil.correctInt((int) (guiNpcSlider.getSliderValue() * 8.0f), 1, 8);
				value = "" + spawn.group;
				guiNpcSlider.setDisplayString(value);
				break;
			}
			case 4: {
				spawn.range = ValueUtil.correctInt((int) (guiNpcSlider.getSliderValue() * 16.0f), 1, 16);
				value = "" + spawn.range;
				guiNpcSlider.setDisplayString(value);
				break;
			}
			case 5: {
				spawn.maxNearPlayer = ValueUtil.correctInt((int) (guiNpcSlider.getSliderValue() * (float) maxSize), 1, maxSize);
				value = "" + spawn.maxNearPlayer;
				guiNpcSlider.setDisplayString(value);
				break;
			}
			default: return;
		}
		if (getTextField(id) != null) { getTextField(id).setFullText(value); }
	}

	@Override
	public void mousePressed(IGuiNpcSlider guiNpcSlider) {
	}

	@Override
	public void mouseReleased(IGuiNpcSlider guiNpcSlider) {
		this.spawn.itemWeight = (int) (guiNpcSlider.getSliderValue() * 100.0f);
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (this.spawn.id >= 0) {
			Client.sendData(EnumPacketServer.NaturalSpawnSave, this.spawn.writeNBT(new NBTTagCompound()));
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			this.save();
			String selected = this.scroll.getSelected();
			this.spawn = new SpawnData();
			Client.sendData(EnumPacketServer.NaturalSpawnGet, this.data.get(selected));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }

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

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: {
				String name = textField.getFullText();
				if (name.isEmpty() || data.containsKey(name)) {
					textField.setFullText(spawn.name);
				} else {
					String old = spawn.name;
					data.remove(old);
					spawn.name = name;
					data.put(spawn.name, spawn.id);
					scroll.replace(old, spawn.name);
				}
				break;
			}
			case 2: {
				spawn.itemWeight = textField.getInteger();
				if (getSlider(4) != null) { getSlider(4).setDisplayString(new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ": " + spawn.itemWeight); }
				break;
			}
			case 3: spawn.group = textField.getInteger(); break;
			case 4: spawn.range = textField.getInteger(); break;
			case 5: spawn.maxNearPlayer = textField.getInteger(); break;
		}
	}

}
