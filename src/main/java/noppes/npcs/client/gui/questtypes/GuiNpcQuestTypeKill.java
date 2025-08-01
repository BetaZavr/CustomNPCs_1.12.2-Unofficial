package noppes.npcs.client.gui.questtypes;

import java.lang.reflect.Modifier;
import java.text.DecimalFormat;
import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.global.GuiQuestEdit;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;

public class GuiNpcQuestTypeKill extends SubGuiInterface implements ITextfieldListener, ICustomScrollListener {

	public GuiScreen parent;
	private GuiCustomScroll scroll;
	private final QuestObjective task;
	private final Map<Integer, Integer> dataDimIDs = new HashMap<>();
	private final Map<String, EntityNPCInterface> dataNPCs = new HashMap<>();
	private final DecimalFormat df = new DecimalFormat("#.#");

	public GuiNpcQuestTypeKill(EntityNPCInterface npc, QuestObjective taskObj, GuiScreen gui) {
		super(npc);
		setBackground("menubg.png");
		title = new TextComponentTranslation("quest.title.kill").getFormattedText();
		xSize = 356;
		ySize = 216;
		closeOnEsc = true;

		parent = gui;
		task = taskObj;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		if (!(guibutton instanceof GuiNpcButton)) {
			super.actionPerformed(guibutton);
			return;
		}
		if (task == null) { return; }
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 1: {
				task.setPartName(((GuiNpcCheckBox) guibutton).isSelected());
				break;
			}
			case 2: {
				task.setAndTitle(((GuiNpcCheckBox) guibutton).isSelected());
				break;
			}
			case 4: {
				if (!dataDimIDs.containsKey(button.getValue())) { return; }
				task.dimensionID = dataDimIDs.get(button.getValue());
				button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim", "" + task.dimensionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 5: {
				task.setPointOnMiniMap(((GuiNpcCheckBox) guibutton).isSelected());
				break;
			}
			case 10: {
				task.pos = new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ));
				task.dimensionID = mc.player.world.provider.getDimension();
				initGui();
				break;
			}
			case 11: {
				Client.sendData(EnumPacketServer.TeleportTo, task.dimensionID, task.pos.getX(), task.pos.getY(), task.pos.getZ());
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int lId = 0;
		int x = guiLeft + 6;
		int y = guiTop + 6;
		// is part name
		GuiNpcButton button = new GuiNpcCheckBox(1, x, y, 208, 14, "quest.kill.part.name", null, task.isPartName());
		button.setHoverText("quest.hover.part.name");
		addButton(button);
		// check title
		button = new GuiNpcCheckBox(2, x, y += 16, 208, 14, new TextComponentTranslation("quest.kill.add.title", new TextComponentTranslation("gui.title").getFormattedText()).getFormattedText(), null, task.isAndTitle());
		button.setHoverText("quest.hover.add.title", new TextComponentTranslation("gui.title").getFormattedText());
		addButton(button);
		// info
		String text = new TextComponentTranslation("quest.player.to").getFormattedText();
		while (text.contains("<br>")) { text = text.replace("<br>", "" + ((char) 10)); }
		addLabel(new GuiNpcLabel(lId++, text, x, y += 16));
		// target
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x, y += 22, 180, 14, task.getTargetName());
		textField.setHoverText("quest.hover.edit.kill.name");
		addTextField(textField);
		// max progress
		textField = new GuiNpcTextField(1, this, fontRenderer, x + 183, y, 24, 14, task.getMaxProgress() + "");
		textField.setMinMaxDefault(1, Integer.MAX_VALUE, 1);
		textField.setHoverText("quest.hover.edit.kill.value", "" + Integer.MAX_VALUE);
		addTextField(textField);
		// entities list
		ArrayList<String> list = new ArrayList<>();
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 1;
		list.add(0, ((char) 167) + "bPlayer");
		hts.put(0, Collections.singletonList(((char) 167) + "7Any player"));
		List<EntityNPCInterface> npcs = new ArrayList<>();
		try {
			npcs = player.world.getEntitiesWithinAABB(EntityNPCInterface.class, new AxisAlignedBB(player.getPosition()).grow(32));
		}
		catch (Exception ignored) { }
		TreeMap<Float, EntityNPCInterface> map = new TreeMap<>();
		for (EntityNPCInterface npc : npcs) {
			float distance = player.getDistance(npc);
			while (map.containsKey(distance)) { distance += 0.0001f; }
			map.put(distance, npc);
		}
		dataNPCs.clear();
		for (Float distance : map.keySet()) {
			String name = map.get(distance).getName();
			String key = ((char) 167) + "a" + name;
			if (list.contains(key)) { continue; }
			list.add(key);
			dataNPCs.put(key, map.get(distance));
			ArrayList<String> hoverList = new ArrayList<>();
			hoverList.add(((char) 167) + "7NPC name: \"" + ((char) 167) + "r" + name + ((char) 167) + "7\"");
			hoverList.add(((char) 167) + "7Distance Of: " + ((char) 167) + "6" + df.format(distance));
			hts.put(i++, hoverList);
		}
		// Registry entity names
		ArrayList<String> regNames = new ArrayList<>();
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			Class<? extends Entity> c = ent.getEntityClass();
			String name = ent.getName();
			try {
				if (!EntityLivingBase.class.isAssignableFrom(c) || EntityNPCInterface.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())) { continue; }
				regNames.add(name);
			} catch (Exception e) { LogWriter.error(e); }
		}
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0); }
		Collections.sort(regNames);

		list.addAll(regNames);
		for (int j = 0; j < regNames.size(); j++) {
			hts.put(i++, Collections.singletonList(((char) 167) + "7Normal entity name"));
		}

		scroll.setListNotSorted(list);
		scroll.setHoverTexts(hts);
		scroll.setSize(130, 198);
		scroll.guiLeft = guiLeft + 220;
		scroll.guiTop = guiTop + 14;
		addScroll(scroll);
		// exit
		button = new GuiNpcButton(66, x, guiTop + ySize - 21, 98, 16, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
		// range to area kill
		if (task.getEnumType() == EnumQuestTask.AREAKILL) {
			addLabel(new GuiNpcLabel(lId++, "gui.searchdistance", x, (y += 19) + 3));
			textField = new GuiNpcTextField(2, this, fontRenderer, x + 114, y, 40, 14, "" + task.getAreaRange());
			textField.setMinMaxDefault(3, 32, task.getAreaRange());
			textField.setHoverText("quest.hover.area.range");
			addTextField(textField);
			y += 2;
		}
		// X
		addLabel(new GuiNpcLabel(lId++, "quest.task.pos.set", x, y += 17));
		addLabel(new GuiNpcLabel(lId++, "X:", x, (y += 12) + 2));
		ITextComponent compass = new TextComponentTranslation("quest.hover.compass");
		textField = new GuiNpcTextField(10, this, fontRenderer, x + 8, y, 40, 14, "" + task.pos.getX());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getX());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "X").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Y
		addLabel(new GuiNpcLabel(lId++, "Y:", x + 51, y + 2));
		textField = new GuiNpcTextField(11, this, fontRenderer, x + 59, y, 40, 14, "" + task.pos.getY());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getY());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Y").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// Z
		addLabel(new GuiNpcLabel(lId++, "Z:", x + 102, y + 2));
		textField = new GuiNpcTextField(12, this, fontRenderer, x + 112, y, 40, 14, "" + task.pos.getZ());
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.pos.getZ());
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.pos", "Z").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// R
		addLabel(new GuiNpcLabel(lId++, "R:", guiLeft + 160, y + 2));
		textField = new GuiNpcTextField(14, this, fontRenderer, x + 164, y, 45, 14, "" + task.rangeCompass);
		textField.setMinMaxDefault(0, 64, task.rangeCompass);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.range").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// dim ID
		addLabel(new GuiNpcLabel(lId++, "D:", x, (y += 17) + 2));
		int p = 0;
		i = 0;
		List<Integer> ids = Arrays.asList(DimensionManager.getStaticDimensionIDs());
		Collections.sort(ids);
		String[] dimIDs = new String[ids.size()];
		for (int id : ids) {
			dimIDs[i] = id + "";
			dataDimIDs.put(i, id);
			if (id == task.dimensionID) { p = i; }
			i++;
		}
		button = new GuiNpcButton(4, x + 8, y - 1, 30, 16, dimIDs, p);
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.dim").appendSibling(compass).getFormattedText());
		addButton(button);
		// region ID
		addLabel(new GuiNpcLabel(lId, "P:", x + 40, y + 2));
		textField = new GuiNpcTextField(9, this, fontRenderer, x + 47, y, 32, 14, "" + task.regionID);
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, task.regionID);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", task.regionID).appendSibling(compass).getFormattedText());
		addTextField(textField);
		// N
		addLabel(new GuiNpcLabel(lId, "N:", x + 81, y + 2));
		textField = new GuiNpcTextField(15, this, fontRenderer, x + 89, y, 123, 14, task.entityName);
		textField.setHoverText(new TextComponentTranslation("quest.hover.compass.entity").appendSibling(compass).getFormattedText());
		addTextField(textField);
		// set player pos
		button = new GuiNpcButton(10, x + 150, y += 16, 60, 16, "gui.set");
		button.setHoverText(new TextComponentTranslation("quest.hover.compass.set").appendSibling(compass).getFormattedText());
		addButton(button);
		// tp
		button = new GuiNpcButton(11, x + 128, y, 20, 16, "TP");
		button.setHoverText("hover.teleport");
		addButton(button);
		// mini map point
		button = new GuiNpcCheckBox(5, x, y - 2, 123, 16, "quest.set.minimap.point", null, task.isSetPointOnMiniMap());
		button.setHoverText("quest.hover.set.minimap.point");
		addButton(button);
	}

	@Override
	public void save() {
		task.setTargetName(getTextField(0).getFullText());
		task.setMaxProgress(getTextField(1).getInteger());

		for (QuestObjective taskObj : NoppesUtilServer.getEditingQuest(player).questInterface.tasks) {
			if (taskObj == task || taskObj.getEnumType() != task.getEnumType()) {
				continue;
			}
			if (taskObj.getTargetName().equals(task.getTargetName())) {
				getTextField(0).setFullText("");
				task.setTargetName("");
				task.setMaxProgress(1);
				break;
			}
		}

		if (task.getTargetName().isEmpty()) {
			NoppesUtilServer.getEditingQuest(player).questInterface.removeTask(task);
		} else {
			if (((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui instanceof GuiQuestEdit) {
				GuiQuestEdit subgui = (GuiQuestEdit) ((GuiNPCManageQuest) GuiNPCManageQuest.Instance).subgui;
				subgui.setSubGui(null);
				subgui.initGui();
			}
		}
	}

	private void saveTargets() {
		task.setTargetName(getTextField(0).getFullText());
		task.setMaxProgress(getTextField(1).getInteger());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		String name = Util.instance.deleteColor(scroll.getSelected());
		getTextField(0).setFullText(name);
		if (dataNPCs.containsKey(scroll.getSelected())) {
			EntityNPCInterface npcIn = dataNPCs.get(scroll.getSelected());
			task.dimensionID = npcIn.world.provider.getDimension();
			task.pos = npcIn.getPosition();
			task.entityName = name;
			int range = 5;
			if (npcIn.ais.getMovingType() == 1) { range = npcIn.ais.getWanderingRange(); }
			else if (npcIn.ais.getMovingType() == 2) {
				int xm = Integer.MAX_VALUE, xn = Integer.MIN_VALUE;
				int ym = Integer.MAX_VALUE, yn = Integer.MIN_VALUE;
				int zm = Integer.MAX_VALUE, zn = Integer.MIN_VALUE;
				for (int[] pos : npcIn.ais.getMovingPath()) {
					if (xm > pos[0]) { xm = pos[0]; }
					if (xn < pos[0]) { xn = pos[0]; }
					if (ym > pos[1]) { ym = pos[1]; }
					if (yn < pos[1]) { yn = pos[1]; }
					if (zm > pos[2]) { zm = pos[2]; }
					if (zn < pos[2]) { zn = pos[2]; }
				}
				if (xm != Integer.MAX_VALUE) {
					if (xm == xn) { task.pos = new BlockPos(xm, ym, zm); } // One pos
					else {
						task.pos = new BlockPos(xm + (xn - xm) / 2, ym + (yn - ym) / 2, zm + (zn - zm) / 2);
						range = 5 + Math.max(xn - xm, Math.max(yn - ym, zn - zm)) / 2;
					}
				}
			}
			task.regionID = BorderController.getInstance().getRegionID(task.dimensionID, task.pos);
			task.setAreaRange(Math.max(range, 32));
		}
		else {
			task.dimensionID = player.world.provider.getDimension();
			task.pos = BlockPos.ORIGIN;
			task.entityName = "";
			task.regionID = BorderController.getInstance().getRegionID(task.dimensionID, player.getPosition());
			task.setAreaRange(5);
		}
		saveTargets();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (task == null) { return; }
		switch (textField.getID()) {
			case 0: task.setTargetName(textField.getFullText()); break;
			case 1: task.setMaxProgress(textField.getInteger()); break;
			case 2: task.setAreaRange(textField.getInteger()); break;
			case 9: {
				if (!BorderController.getInstance().regions.containsKey(textField.getInteger())) {
					textField.setFullText("" + textField.getDefault());
					return;
				}
				task.regionID = textField.getInteger();
				textField.setHoverText(new TextComponentTranslation("quest.hover.compass.reg", "" + task.regionID).appendSibling(new TextComponentTranslation("quest.hover.compass")).getFormattedText());
				break;
			}
			case 10: task.pos = new BlockPos(textField.getInteger(), task.pos.getY(), task.pos.getZ()); break;
			case 11: task.pos = new BlockPos(task.pos.getX(), textField.getInteger(), task.pos.getZ()); break;
			case 12: task.pos = new BlockPos(task.pos.getX(), task.pos.getY(), textField.getInteger()); break;
			case 14: task.rangeCompass = textField.getInteger(); break;
			case 15: task.entityName = textField.getFullText(); break;
		}
	}

}
