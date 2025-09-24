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
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMobSpawnerSelector;
import noppes.npcs.client.gui.SubGuiNpcBiomes;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

import javax.annotation.Nonnull;

public class GuiNpcNaturalSpawns extends GuiNPCInterface2
		implements IGuiData, IScrollData, ITextfieldListener, ICustomScrollListener, ISliderListener, GuiYesNoCallback {

	protected final HashMap<String, Integer> data = new HashMap<>();
	protected GuiCustomScroll scroll;
	protected SpawnData spawn = new SpawnData();
	protected Entity displayNpc = null;
	protected int maxSize;
	protected boolean accept = false;

	public GuiNpcNaturalSpawns(EntityNPCInterface npc) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuGlobal;

		Client.sendData(EnumPacketServer.NaturalSpawnGetAll);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch(button.getID()) {
			case 1: {
				if (!accept) {
					GuiYesNo guiyesno = new GuiYesNo(this,
							new TextComponentTranslation("gui.acceptMessage").getFormattedText(),
							new TextComponentTranslation("spawning.accept.message").getFormattedText(),
							0);
					displayGuiScreen(guiyesno);
					return;
				}
				save();
				String name = new TextComponentTranslation("gui.new").getFormattedText();
				while (data.containsKey(name)) { name += "_"; }
				SpawnData spawn = new SpawnData();
				spawn.name = name;
				Client.sendData(EnumPacketServer.NaturalSpawnSave, spawn.writeNBT(new NBTTagCompound()));
				break;
			} // add
			case 2: {
				if (!data.containsKey(scroll.getSelected())) { return; }
				Client.sendData(EnumPacketServer.NaturalSpawnRemove, spawn.id);
				spawn = new SpawnData();
				scroll.clear();
				displayNpc = null;
				break;
			} // remove
			case 3: setSubGui(new SubGuiNpcBiomes(spawn)); break; // set biome
			case 4: {
				spawn.liquid = button.getValue() == 0;
				button.setHoverText("spawning.hover.liquid." + button.getValue());
				break;
			} // set liquid
			case 5: setSubGui(new SubGuiNpcMobSpawnerSelector()); break; // select npc
			case 6: spawn.canSeeSummon = ((GuiNpcCheckBox) button).isSelected(); break; // select npc
			case 25: {
				spawn.compoundEntity = new NBTTagCompound();
				displayNpc = null;
				initGui();
				break;
			} // nbt
			case 27: spawn.type = button.getValue(); break; // type
		}
	}

	@Override
	public void confirmClicked(boolean result, int id) {
		displayGuiScreen(this);
		if (!result) { return; }
		accept = true;
		buttonEvent(getButton(1), 1);
	}

	@Override
	public void subGuiClosed(SubGuiInterface gui) {
		if (gui instanceof SubGuiNpcMobSpawnerSelector) {
			SubGuiNpcMobSpawnerSelector selector = (SubGuiNpcMobSpawnerSelector) gui;
			NBTTagCompound compound = selector.getCompound();
			if (compound != null) {
				spawn.compoundEntity = compound;
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
					if (displayNpc instanceof EntityItemFrame) { x += 16; }
				}
				GlStateManager.pushMatrix();
				drawNpc(displayNpc, x, y, 1.0f, r, p, 0);
				GlStateManager.popMatrix();
			}
		}
	}
	
	private String getTitle(NBTTagCompound compound) {
		displayNpc = EntityList.createEntityFromNBT(compound, mc.world);
		if (displayNpc != null) { return displayNpc.getName(); }
		return "gui.selectnpc";
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(143, 208); }
		scroll.guiLeft = guiLeft + 214;
		scroll.guiTop = guiTop + 4;
		addScroll(scroll);
		addButton(new GuiNpcButton(1, guiLeft + 358, guiTop + 38, 58, 20, "gui.add")
				.setHoverText("spawning.hover.add"));
		addButton(new GuiNpcButton(2, guiLeft + 358, guiTop + 61, 58, 20, "gui.remove")
				.setHoverText("spawning.hover.del"));
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
		addTextField(new GuiNpcTextField(1, this, x + 56, y, 150, 14, spawn.name)
				.setHoverText("spawning.hover.name"));
		// Biomes
		addLabel(new GuiNpcLabel(lId++, "spawning.biomes", x, (y += 17) + 3));
		addButton(new GuiNpcButton(3, x + 56, y, 74, 16, "selectServer.edit")
				.setLayerColor(spawn.biomes.isEmpty() ? new Color(0xFFF02020).getRGB() : 0)
				.setHoverText("spawning.hover.biomes"));
		// spawner type
		addLabel(new GuiNpcLabel(lId++, "gui.type", x, (y += 18) + 3));
		addButton(new GuiNpcButton(27, x + 56, y, 74, 16, new String[] { "spawner.any", "spawner.dark", "spawner.light" }, spawn.type)
				.setHoverText("spawning.hover.type"));
		addButton(new GuiNpcButton(4, x + 132, y, 74, 16, new String[] { "spawning.liquid.0", "spawning.liquid.1" }, spawn.liquid ? 0 : 1)
				.setHoverText("spawning.hover.liquid." + (spawn.liquid ? 0 : 1)));
		// select entity
		addButton(new GuiNpcButton(5, x, y += 18, 184, 16, getTitle(spawn.compoundEntity))
				.setHoverText("spawning.hover.sel.npc"));
		addButton(new GuiNpcButton(25, x + 186, y, 20, 16, "X")
				.setHoverText("spawning.hover.del.npc"));
		// chance
		addLabel(new GuiNpcLabel(lId++, new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ":", x, (y += 29) - 10));
		addSlider(new GuiNpcSlider(this, 2, x, y, 160, 12, (float) spawn.itemWeight / 100.0f)
				.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText()));
		addTextField(new GuiNpcTextField(2, this, x + 163, y, 43, 12, "" + spawn.itemWeight)
				.setMinMaxDefault(1, 100, spawn.itemWeight)
				.setHoverText("spawning.hover.chance"));
		// group size
		addLabel(new GuiNpcLabel(lId++, "spawning.group", x, (y += 25) - 10));
		addSlider(new GuiNpcSlider(this, 3, x, y, 160, 12, (float) spawn.group / 8.0f)
				.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText()));
		addTextField(new GuiNpcTextField(3, this, x + 163, y, 43, 12, "" + spawn.group)
				.setMinMaxDefault(1, 8, spawn.group)
				.setHoverText("spawning.hover.group"));
		// distance
		addLabel(new GuiNpcLabel(lId, "spawning.range", x, (y += 25) - 10));
		addSlider(new GuiNpcSlider(this, 4, x, y, 160, 12, (float) spawn.range / 16.0f)
				.setHoverText(new TextComponentTranslation("spawning.hover.chance").getFormattedText()));
		addTextField(new GuiNpcTextField(4, this, x + 163, y, 43, 12, "" + spawn.range)
				.setMinMaxDefault(1, 16, spawn.range)
				.setHoverText("spawning.hover.range"));
		// maximum in player
		addLabel(new GuiNpcLabel(lId, "spawning.maximum.in", x, (y += 25) - 10));
		addSlider(new GuiNpcSlider(this, 5, x, y, 160, 12, (float) spawn.maxNearPlayer / (float) maxSize)
				.setHoverText(new TextComponentTranslation("spawning.hover.maximum.in", TextFormatting.GOLD + "" + maxSize).getFormattedText()));
		addTextField(new GuiNpcTextField(5, this, x + 163, y, 43, 12, "" + spawn.maxNearPlayer)
				.setMinMaxDefault(1, maxSize, spawn.maxNearPlayer)
				.setHoverText("spawning.hover.maximum.in", TextFormatting.GOLD + "" + maxSize));
		// player can see
		addButton(new GuiNpcCheckBox(6, x, y + 14, 184, 14, "spawning.can.see", "spawning.not.see", spawn.canSeeSummon)
				.setHoverText("spawning.hover.can.see"));
	}

	@Override
	public void mouseDragged(GuiNpcSlider guiNpcSlider) {
		String value;
		switch (guiNpcSlider.getID()) {
			case 2: {
				spawn.itemWeight = ValueUtil.correctInt((int) (guiNpcSlider.sliderValue * 100.0f), 1, 100);
				value = "" + spawn.itemWeight;
				guiNpcSlider.displayString = value + "%";
				break;
			}
			case 3: {
				spawn.group = ValueUtil.correctInt((int) (guiNpcSlider.sliderValue * 8.0f), 1, 8);
				value = "" + spawn.group;
				guiNpcSlider.displayString = value;
				break;
			}
			case 4: {
				spawn.range = ValueUtil.correctInt((int) (guiNpcSlider.sliderValue * 16.0f), 1, 16);
				value = "" + spawn.range;
				guiNpcSlider.displayString = value;
				break;
			}
			case 5: {
				spawn.maxNearPlayer = ValueUtil.correctInt((int) (guiNpcSlider.sliderValue * (float) maxSize), 1, maxSize);
				value = "" + spawn.maxNearPlayer;
				guiNpcSlider.displayString = value;
				break;
			}
			default: return;
		}
		if (getTextField(guiNpcSlider.getID()) != null) { getTextField(guiNpcSlider.getID()).setText(value); }
	}

	@Override
	public void mousePressed(GuiNpcSlider guiNpcSlider) { }

	@Override
	public void mouseReleased(GuiNpcSlider guiNpcSlider) {
		if (guiNpcSlider.getID() == 2) {
			spawn.itemWeight = ValueUtil.correctInt((int) (guiNpcSlider.sliderValue * 100.0f), 1, 100);
		}
	}

	@Override
	public void save() {
		GuiNpcTextField.unfocus();
		if (spawn.id >= 0) { Client.sendData(EnumPacketServer.NaturalSpawnSave, spawn.writeNBT(new NBTTagCompound())); }
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			save();
			String selected = scroll.getSelected();
			spawn = new SpawnData();
			Client.sendData(EnumPacketServer.NaturalSpawnGet, data.get(selected));
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		String name = scroll.getSelected();
		data.clear();
		data.putAll(dataMap);
		scroll.setList(dataList);
		if (name != null) { scroll.setSelected(name); }
		initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		spawn.readNBT(compound);
		setSelected(spawn.name);
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

	@Override
	public void unFocused(GuiNpcTextField textField) {
		switch (textField.getID()) {
			case 1: {
				String name = textField.getText();
				if (name.isEmpty() || data.containsKey(name)) { textField.setText(spawn.name); }
				else {
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
				if (getSlider(4) != null) { getSlider(4).displayString = new TextComponentTranslation("spawning.weightedChance").getFormattedText() + ": " + spawn.itemWeight; }
				break;
			}
			case 3: spawn.group = textField.getInteger(); break;
			case 4: spawn.range = textField.getInteger(); break;
			case 5: spawn.maxNearPlayer = textField.getInteger(); break;
		}
	}

}
