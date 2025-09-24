package noppes.npcs.client.gui.global;

import java.util.*;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiDataSend;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerData;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.controllers.BankController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.TransportController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.TransportLocation;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNpcManagePlayerData extends GuiNPCInterface2
		implements  IScrollData, ICustomScrollListener, GuiYesNoCallback, IGuiData, ITextfieldListener {

	protected HashMap<String, Integer> data = new HashMap<>();
	protected HashMap<String, String> scrollData = new HashMap<>();
	protected EnumPlayerData selection = EnumPlayerData.Players;
	protected NBTTagCompound gameData = null;
	protected GuiCustomScroll scroll;
	protected boolean isOnline = false;
	protected String search = "";
	protected String selected = null;
	protected String selectedPlayer = null;

	public GuiNpcManagePlayerData(EntityNPCInterface npc) {
		super(npc);
		parentGui = EnumGuiType.MainMenuGlobal;

		Client.sendData(EnumPacketServer.PlayerDataGet, selection);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		GuiNpcTextField.unfocus();
		String title = selectedPlayer;
		switch (selection) {
			case Quest: title = new TextComponentTranslation("quest.quest").getFormattedText(); break;
			case Dialog: title = new TextComponentTranslation("dialog.dialog").getFormattedText(); break;
			case Transport: title = new TextComponentTranslation("global.transport").getFormattedText(); break;
			case Bank: title = new TextComponentTranslation("global.banks").getFormattedText(); break;
			case Factions: title = new TextComponentTranslation("menu.factions").getFormattedText(); break;
			case Game: title = new TextComponentTranslation("gui.game").getFormattedText(); break;
		}
		int id = button.getID();
		if (id == 0) {
			if (selection == EnumPlayerData.Players || !scroll.hasSelected()) {
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("global.playerdata").getFormattedText() + ": " + title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				displayGuiScreen(guiyesno);
			} else {
				Client.sendData(EnumPacketServer.PlayerDataSet, selection.ordinal(), selectedPlayer, 1, data.get(scrollData.get(scroll.getSelected())));
			}
		} // del
		else if (id >= 1 && id <= 6 || id == 9) {
			if (selectedPlayer == null && id != 1) { return; }
			if (selection == EnumPlayerData.Game) { save(); }
			if (id == 9) { selection = EnumPlayerData.Game; }
			else { selection = EnumPlayerData.values()[id - 1]; }
			initButtons();
			scroll.clear();
			data.clear();
			Client.sendData(EnumPacketServer.PlayerDataGet, selection, selectedPlayer);
			selected = null;
			getTextField(0).setText("");
		}
		else if (id == 7) {
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("gui.wipe").getFormattedText() + "?",
					new TextComponentTranslation("data.hover.wipe").getFormattedText().replace("<br>",
							"" + ((char) 10)),
					7);
			displayGuiScreen(guiyesno);
		}
		else if (id == 12) {
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("gui.cleaning").getFormattedText() + "?",
					new TextComponentTranslation("data.hover.cleaning").getFormattedText().replace("<br>",
							"" + ((char) 10)),
					12);
			displayGuiScreen(guiyesno);
		}
		else if (id == 8) { // Add
			SubGuiEditText subgui = new SubGuiEditText(0, "");
			subgui.label = "gui.add";
			switch (selection) {
				case Quest: {
					subgui.hovers[0] = "";
					for (int i : QuestController.instance.quests.keySet()) {
						if (data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>" + subgui.hovers[0];
					break;
				}
				case Dialog: {
					subgui.hovers[0] = "";
					for (int i : DialogController.instance.dialogs.keySet()) {
						if (data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>" + subgui.hovers[0];
					break;
				}
				case Transport: {
					break;
				}
				case Bank: {
					subgui.hovers[0] = "";
					for (int i : BankController.getInstance().banks.keySet()) {
						if (data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>" + subgui.hovers[0];
					break;
				}
				case Factions: {
					subgui.hovers[0] = "";
					for (int i : FactionController.instance.factions.keySet()) {
						if (data.containsValue(i)) { continue; }
						if (!subgui.hovers[0].isEmpty()) { subgui.hovers[0] += ", "; }
						subgui.hovers[0] += i;
					}
					subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>" + ((char) 167) + "6" + subgui.hovers[0];
					break;
				}
			}
			setSubGui(subgui);
		}
		else if (id == 10) {
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("global.playerdata").getFormattedText() + ": " + title,
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
			displayGuiScreen(guiyesno);
		} // del all
		else if (id == 11) { editData(); } // edit
	}

	public void confirmClicked(boolean result, int id) {
		String sel = selected;
		String playerName = selectedPlayer;
		EnumPlayerData epd = selection;
		NoppesUtil.openGUI(player, this);
		selected = sel;
		selectedPlayer = playerName;
		selection = epd;
		if (!result) { return; }
		if (id == 0) {
			if (selection != EnumPlayerData.Players) { return; }
			data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, selection, selectedPlayer, selected);
			selected = null;
			selectedPlayer = null;
			scroll.setSelect(-1);
			initButtons();
		}
		else if (id == 1) { Client.sendData(EnumPacketServer.PlayerDataSet, selection.ordinal(), selectedPlayer, 3, -1); }
		else if (id == 7) {
			selection = EnumPlayerData.Wipe;
			initButtons();
			scroll.clear();
			data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, selection, selectedPlayer, selected);
			selected = null;
			selectedPlayer = null;
			scroll.setSelect(-1);
		}
		else if (id == 12) {
			SubGuiDataSend subgui = new SubGuiDataSend(0);
			setSubGui(subgui);
		}
	}

	private void editData() {
		if (!scroll.hasSelected()) { return; }
		switch (selection) {
			case Bank: Client.sendData(EnumPacketServer.BankShow, selection, selectedPlayer, data.get(scrollData.get(scroll.getSelected()))); break;
			case Factions: {
				int factionId = data.get(scrollData.get(scroll.getSelected()));
				SubGuiEditText subgui = new SubGuiEditText(1, "");
				Faction f = FactionController.instance.factions.get(factionId);
				String v = scroll.getHoversTexts().get(scroll.getSelect()).get(0);
				int value = -1;
				try { value = Integer.parseInt(v.substring(v.indexOf(((char) 167) + "3") + 2)); } catch (Exception e) { LogWriter.error(e); }
				if (f != null) { subgui.numbersOnly = new int[] { 0, f.friendlyPoints * 2, value }; }
				else { subgui.numbersOnly = new int[] { 0, Integer.MAX_VALUE, value }; }
				subgui.text[0] = "" + value;
				subgui.label = "gui.set.new.value";
				setSubGui(subgui);
				break;
			}
			case Game: {
				if (gameData == null || !data.containsKey(scroll.getSelected())) { return; }
				SubGuiEditText subgui = new SubGuiEditText(2, "");
				subgui.initGui();
				subgui.getTextField(0).setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, 0);
				int m = 3, s = 0, id = data.get(scroll.getSelected());
				MarcetController mData = MarcetController.getInstance();
				for (int i = 0; i < gameData.getCompoundTag("GameData").getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = gameData.getCompoundTag("GameData").getTagList("MarketData", 10).getCompoundTagAt(i);
					if (id != nbt.getInteger("MarketID")) { continue; }
					s = nbt.getInteger("Slot");
					Marcet marcet = mData.getMarcet(id);
					if (marcet == null) { break; }
					m = marcet.markup.size() - 1;
					break;
				}
				subgui.text[0] = "" + s;
				subgui.getTextField(0).setMinMaxDefault(0, m, s);
				subgui.label = "gui.set.new.value";
				setSubGui(subgui);
			}
		}
	}

	public void initButtons() {
		boolean hasPlayer = selectedPlayer != null && !selectedPlayer.isEmpty();
		getButton(0).setIsVisible(true);
		getButton(0).setIsEnable(hasPlayer);
		getButton(1).setIsEnable(selection != EnumPlayerData.Players && hasPlayer);
		getButton(2).setIsEnable(selection != EnumPlayerData.Quest && hasPlayer);
		getButton(3).setIsEnable(selection != EnumPlayerData.Dialog && hasPlayer);
		getButton(4).setIsEnable(selection != EnumPlayerData.Transport && hasPlayer);
		getButton(5).setIsEnable(selection != EnumPlayerData.Bank && hasPlayer);
		getButton(6).setIsEnable(selection != EnumPlayerData.Factions && hasPlayer);
		getButton(9).setIsEnable(selection != EnumPlayerData.Game && hasPlayer);
		boolean canEdit = selection != EnumPlayerData.Players && selection != EnumPlayerData.Wipe;
		getButton(8).setIsVisible(true);
		getButton(10).setIsVisible(true);
		getButton(11).setIsVisible(true);
		getButton(12).setIsEnable(selection == EnumPlayerData.Players);

		if (scroll != null) {
			if (selection != EnumPlayerData.Game) {
				scroll.guiLeft = guiLeft + 7;
				scroll.guiTop = guiTop + 16;
				scroll.setSize(300, 152);
			} else {
				scroll.guiLeft = guiLeft + 7;
				scroll.guiTop = guiTop + 52;
				scroll.setSize(120, 138);
			}
		}
		getLabel(1).setIsEnable(selection != EnumPlayerData.Game);
		getTextField(0).setIsVisible(selection != EnumPlayerData.Game);
		if (getLabel(2) != null) { getLabel(2).setIsEnable(selection == EnumPlayerData.Game); }
		if (getLabel(3) != null) { getLabel(3).setIsEnable(selection == EnumPlayerData.Game); }
		if (getTextField(1) != null) { getTextField(1).setIsVisible(selection == EnumPlayerData.Game); }
		switch (selection) {
			case Quest:
				case Dialog:
				case Transport: {
				getButton(8).setIsEnable(canEdit && hasPlayer);
				getButton(10).setIsEnable(canEdit && hasPlayer && !scroll.getList().isEmpty());
				getButton(11).setIsEnable(false);
				break;
			}
			case Bank: {
				getButton(8).setIsEnable(canEdit && hasPlayer && data.size() < BankController.getInstance().banks.size());
				getButton(10).setIsEnable(canEdit && hasPlayer && !scroll.getList().isEmpty());
				getButton(11).setIsEnable(canEdit && hasPlayer && scroll != null && scroll.hasSelected());
				break;
			}
			case Factions: {
				getButton(8).setIsEnable(canEdit && hasPlayer && data.size() < FactionController.instance.factions.size());
				getButton(10).setIsEnable(canEdit && hasPlayer && !scroll.getList().isEmpty());
				getButton(11).setIsEnable(canEdit && hasPlayer && scroll != null && scroll.hasSelected());
				break;
			}
			case Game: {
				getButton(0).setIsEnable(canEdit && hasPlayer && scroll.hasSelected());
				getButton(8).setIsVisible(false);
				getButton(10).setIsEnable(canEdit && hasPlayer && gameData != null);
				getButton(11).setIsEnable(canEdit && hasPlayer && scroll.hasSelected());
				break;
			}
			default: {
				getButton(8).setIsVisible(false);
				getButton(10).setIsVisible(false);
				getButton(11).setIsVisible(false);
			}
		}
		if (!hasPlayer) { getLabel(0).setLabel(new TextComponentTranslation("data.all.players").getFormattedText() + " (" + (scroll.getList() == null ? "1" : scroll.getList().size()) + ")"); }
		else { getLabel(0).setLabel(new TextComponentTranslation("data.sel.player", ((char) 167) + (isOnline ? "2" : "4") + ((char) 167) + "l" + selectedPlayer).getFormattedText() + ((char) 167) + "r (" + (scroll.getList() == null ? "1" : scroll.getList().size()) + ")"); }
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(300, 152); }
		scroll.guiLeft = guiLeft + 7;
		scroll.guiTop = guiTop + 16;
		addScroll(scroll);
		selected = null;
		addLabel(new GuiNpcLabel(0, "data.all.players", guiLeft + 10, guiTop + 6));
		int x = guiLeft + 313, y = guiTop + 16, w = 99;
		addButton(new GuiNpcButton(1, x, y, w, 20, "playerdata.players")
				.setHoverText("data.hover.list"));
		addButton(new GuiNpcButton(2, x, (y += 22), w, 20, "quest.quest")
				.setHoverText("data.hover.quests"));
		addButton(new GuiNpcButton(3, x, (y += 22), w, 20, "dialog.dialog")
				.setHoverText("data.hover.dialogs"));
		addButton(new GuiNpcButton(4, x, (y += 22), w, 20, "global.transport")
				.setHoverText("data.hover.transports"));
		addButton(new GuiNpcButton(5, x, (y += 22), w, 20, "global.banks")
				.setHoverText("data.hover.banks"));
		addButton(new GuiNpcButton(6, x, (y += 22), w, 20, "menu.factions")
				.setHoverText("data.hover.factions"));
		addButton(new GuiNpcButton(9, x, (y += 22), w, 20, "gui.game")
				.setHoverText("data.hover.game"));
		addButton(new GuiNpcButton(7, x, (y += 22), w, 20, "gui.wipe")
				.setHoverText("data.hover.wipe"));
		addButton(new GuiNpcButton(12, x, y + 22, w, 20, "gui.cleaning")
				.setHoverText("data.hover.cleaning"));
		y = guiTop + 170;
		addLabel(new GuiNpcLabel(1, "gui.found", guiLeft + 10, y + 5));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 66, y, 240, 20, search)
				.setHoverText("data.hover.found"));
		x = guiLeft + 7;
		w = 73;
		addButton(new GuiNpcButton(8, x, (y += 22), w, 20, "gui.add")
				.setHoverText("hover.add"));
		addButton(new GuiNpcButton(0, (x += w + 3), y, w, 20, "gui.remove")
				.setHoverText("hover.delete"));
		addButton(new GuiNpcButton(10, (x += w + 3), y, w, 20, "gui.remove.all")
				.setHoverText("hover.delete.all"));
		addButton(new GuiNpcButton(11, x + w + 3, y, w, 20, "selectServer.edit")
				.setHoverText("hover.edit"));
		initButtons();
		if (selection == EnumPlayerData.Game) {
			y = guiTop + 18;
			addLabel(new GuiNpcLabel(2, "gui.money", guiLeft + 10, y + 5));
			addTextField(new GuiNpcTextField(1, this, guiLeft + 66, y, 120, 20, "" + gameData.getLong("Money"))
					.setMinMaxDefault(0, Long.MAX_VALUE, gameData.getLong("Money"))
					.setHoverText("data.hover.money", "" + Long.MAX_VALUE));
			addLabel(new GuiNpcLabel(3, new TextComponentTranslation("global.market").getFormattedText() + ":", guiLeft + 10, y + 25)
					.setHoverText("data.hover.markets"));
		}
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (!bo) {
			if (selection == EnumPlayerData.Wipe || search.equals(getTextField(0).getText())) { return false; }
			search = getTextField(0).getText().toLowerCase();
			setCurrentList();
		}
		return bo;
	}

	@Override
	public void save() {
		ContainerNPCBank.editPlayerBankData = null;
		if (selection == EnumPlayerData.Game) {
			boolean hasPlayer = selectedPlayer != null && !selectedPlayer.isEmpty();
			if (hasPlayer && gameData != null) { Client.sendData(EnumPacketServer.PlayerDataSet, selection.ordinal(), selectedPlayer, 0, gameData); }
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		selected = scroll.getSelected();
		if (selection == EnumPlayerData.Players) {
			selectedPlayer = selected;
			isOnline = data.get(selected) == 1;
		}
		initButtons();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { editData(); }

	private void setCurrentList() {
		if (scroll == null) { return; }
		List<String> list = new ArrayList<>();
		List<String> hovers = new ArrayList<>();
		List<String> suffixes = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		if (selection == EnumPlayerData.Wipe) { selection = EnumPlayerData.Players; }
		switch (selection) {
			case Players: {
				List<String> listOn = new ArrayList<>();
				List<String> listOff = new ArrayList<>();
				for (String name : data.keySet()) {
					if (search.isEmpty() || name.toLowerCase().contains(search)) {
						if (data.get(name) == 1) { listOn.add(name); }
						else { listOff.add(name); }
					}
				}
				Collections.sort(listOn);
				Collections.sort(listOff);
				list = listOn;
				list.addAll(listOff);
				for (String name : list) {
					suffixes.add(new TextComponentTranslation(data.get(name) == 0 ? "gui.offline" : "gui.online").getFormattedText());
				}
				break;
			}
			case Quest: {
				Map<String, Map<Integer, String>> mapA = new TreeMap<>();
				Map<String, Map<Integer, String>> mapF = new TreeMap<>();
				for (String str : data.keySet()) {
					String cat = str.substring(0, str.indexOf(": "));
					String name = str.substring(str.indexOf(": ") + 2);
					Map<String, Map<Integer, String>> map;
					if (name.endsWith("(Active quest)")) {
						name = name.substring(0, name.lastIndexOf("(Active quest)"));
						map = mapA;
					} else {
						name = name.substring(0, name.lastIndexOf("(Finished quest)"));
						map = mapF;
					}
					if (!map.containsKey(cat)) { map.put(cat, new TreeMap<>()); }
					map.get(cat).put(data.get(str), name);
				}
				for (String cat : mapA.keySet()) {
					ITextComponent sfx = new TextComponentTranslation("availability.active");
					sfx.getStyle().setColor(TextFormatting.GREEN);
					for (int id : mapA.get(cat).keySet()) {
						suffixes.add(sfx.getFormattedText());
						String key = ((char) 167) + "aID:" + id + ((char) 167) + "7 " + cat + ": \"" + ((char) 167) + "r" + mapA.get(cat).get(id) + ((char) 167) + "7\"";
						list.add(key);
						for (String str : data.keySet()) {
							if (data.get(str) == id) {
								scrollData.put(key, str);
								break;
							}
						}
					}
				}
				for (String cat : mapF.keySet()) {
					ITextComponent sfx = new TextComponentTranslation("quest.complete");
					sfx.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
					for (int id : mapF.get(cat).keySet()) {
						suffixes.add(sfx.getFormattedText());
						String key = ((char) 167) + "dID:" + id + ((char) 167) + "7 " + cat + ": \"" + ((char) 167) + "r" + mapF.get(cat).get(id) + ((char) 167) + "7\"";
						list.add(key);
						for (String str : data.keySet()) {
							if (data.get(str) == id) {
								scrollData.put(key, str);
								break;
							}
						}
					}
				}
				break;
			}
			case Dialog: {
				Map<Integer, String> map = new TreeMap<>();
				for (String str : data.keySet()) { map.put(data.get(str), ((char) 167) + "7ID:" + data.get(str) + " " + str.replace(": ", ": " + ((char) 167) + "r")); }
				for (int id : map.keySet()) {
					list.add(map.get(id));
					for (String str : data.keySet()) {
						if (data.get(str) == id) {
							scrollData.put(map.get(id), str);
							break;
						}
					}
				}
				break;
			}
			case Transport: {
				Map<Integer, String> map = new TreeMap<>();
				for (String str : data.keySet()) { map.put(data.get(str), ((char) 167) + "7" + str.replace(": ", ": " + ((char) 167) + "r")); }
				for (int id : map.keySet()) {
					list.add(map.get(id));
					String catData = "cat null", locData = "loc null", pos = "pos null";
					TransportLocation loc = TransportController.getInstance().getTransport(id);
					if (loc != null) {
						catData = ((char) 167) + "7" + new TextComponentTranslation("drop.category").getFormattedText()
								+ ((char) 167) + "7: \"" + ((char) 167) + "r"
								+ new TextComponentTranslation(loc.category.title).getFormattedText() + ((char) 167)
								+ "7\" ID: " + ((char) 167) + "6" + loc.category.id;
						locData = ((char) 167) + "7" + new TextComponentTranslation("gui.location").getFormattedText()
								+ ((char) 167) + "7: \"" + ((char) 167) + "r"
								+ new TextComponentTranslation(loc.name).getFormattedText() + ((char) 167) + "7\" ID: "
								+ ((char) 167) + "6" + id;
						pos = ((char) 167) + "7" + new TextComponentTranslation("parameter.world").getFormattedText()
								+ ((char) 167) + "7 ID: " + ((char) 167) + "a" + loc.dimension + ((char) 167) + "7; "
								+ new TextComponentTranslation("parameter.position").getFormattedText() + ((char) 167)
								+ "7 X:" + ((char) 167) + "b" + loc.getX() + ((char) 167) + "7 Y:" + ((char) 167) + "b"
								+ loc.getY() + ((char) 167) + "7 Z:" + ((char) 167) + "b" + loc.getZ();
					}
					hovers.add(catData + "<br>" + locData + "<br>" + pos);
					for (String str : data.keySet()) {
						if (data.get(str) == id) {
							scrollData.put(map.get(id), str);
							break;
						}
					}
				}
				break;
			}
			case Bank: {
				for (String str : data.keySet()) {
					list.add(str);
					hovers.add("ID: " + data.get(str));
					scrollData.put(str, str);
				}
				Collections.sort(list);
				break;
			}
			case Factions: {
				Map<String, String> mapH = new HashMap<>();
				Map<String, Integer> mapC = new HashMap<>();
				scrollData.clear();
				for (String str : data.keySet()) {
					if (search.isEmpty() || str.toLowerCase().contains(search)) {
						String[] l = str.split(";");
						String key = new TextComponentTranslation(l[0]).getFormattedText() + ((char) 167) + "7 (ID:" + data.get(str) + ")";
						list.add(key);
						int value = -1;
						try { value = Integer.parseInt(l[1]); } catch (Exception e) { LogWriter.error(e); }
						scrollData.put(key, str);
						int color = 0xFFFFFF;
						String hover = new TextComponentTranslation("type.value").getFormattedText() + ": " + ((char) 167) + "3" + value;
						Faction f = FactionController.instance.factions.get(data.get(str));
						if (f != null) {
							hover += "<br>" + new TextComponentTranslation("gui.attitude").getFormattedText() + ": ";
							ITextComponent add;
							if (value < f.neutralPoints) {
								add = new TextComponentTranslation("faction.unfriendly");
								add.getStyle().setColor(TextFormatting.DARK_RED);
							} else if (value < f.friendlyPoints) {
								add = new TextComponentTranslation("faction.neutral");
								add.getStyle().setColor(TextFormatting.GOLD);
							} else {
								add = new TextComponentTranslation("faction.friendly");
								add.getStyle().setColor(TextFormatting.DARK_GREEN);
							}
							hover += add.getFormattedText();
							color = f.color;
						}
						mapH.put(key, hover);
						mapC.put(key, color);
					}
				}
				Collections.sort(list);
				for (String key : list) {
					hovers.add(mapH.get(key));
					colors.add(mapC.get(key));
				}
				break;
			}
		}
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		if (!hovers.isEmpty()) {
			int i = 0;
			for (String str : hovers) {
				hts.put(i, Arrays.asList(str.split("<br>")));
				i++;
			}
		}
		scroll.setUnsortedList(list)
				.setSuffixes(!suffixes.isEmpty() ? suffixes : null)
				.setColors(!colors.isEmpty() ? colors : null)
				.setHoverTexts(hts);
	}

	@Override
	public void setData(Vector<String> dataList, HashMap<String, Integer> dataMap) {
		if (selection == EnumPlayerData.Game) { return; }
		data.clear();
		data.putAll(dataMap);
		setCurrentList();
		if (selection == EnumPlayerData.Players && selectedPlayer != null) {
			scroll.setSelected(selectedPlayer);
			selected = selectedPlayer;
		}
		if (selection == EnumPlayerData.Wipe) {
			selection = EnumPlayerData.Players;
		}
		initButtons();
		if (ContainerNPCBank.editPlayerBankData != null) {
			buttonEvent(new GuiNpcButton(5, 0, 0, ""), 1);
			ContainerNPCBank.editPlayerBankData = null;
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (selection != EnumPlayerData.Game || !compound.hasKey("GameData", 10)) { return; }
		gameData = compound;
		Map<Integer, Integer> map = new TreeMap<>();
		for (int i = 0; i < compound.getCompoundTag("GameData").getTagList("MarketData", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getCompoundTag("GameData").getTagList("MarketData", 10).getCompoundTagAt(i);
			map.put(nbt.getInteger("MarketID"), nbt.getInteger("Slot"));
		}
		List<String> list = new ArrayList<>();
		MarcetController mData = MarcetController.getInstance();
		data.clear();
		int i = 0;
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		for (int id : map.keySet()) {
			String key = ((char) 167) + "7ID:" + id + " "
					+ (new TextComponentTranslation("bank.slot").getFormattedText()) + ((char) 167) + "r: "
					+ map.get(id);
			list.add(key);
			data.put(key, id);
			Marcet m = mData.getMarcet(id);
			List<String> hList = new ArrayList<>();
			if (m != null) {
				hList.add("ID:" + id + " \"" + m.getName() + "\"");
				hList.add(new TextComponentTranslation("gui.max").getFormattedText() + ": " + (m.markup.size() - 1));
			} else {
				hList.add(new TextComponentTranslation("global.market").getFormattedText() + " - " + new TextComponentTranslation("quest.notfound").getFormattedText());
			}
			hts.put(i, hList);
			i++;
		}
		scroll.setUnsortedList(list).setHoverTexts(hts);
		initGui();
	}

	@Override
	public void setSelected(String selected) { }

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			if (subgui.getId() == 0) {
				try { Client.sendData(EnumPacketServer.PlayerDataSet, selection.ordinal(), selectedPlayer, 0, Integer.parseInt(((SubGuiEditText) subgui).text[0])); } catch (Exception e) { LogWriter.error(e); }
			} // add
			else if (subgui.getId() == 1) {
				try {
					Client.sendData(EnumPacketServer.PlayerDataSet, selection.ordinal(), selectedPlayer, 2, data.get(scrollData.get(scroll.getSelected())), Integer.parseInt(((SubGuiEditText) subgui).text[0]));
				} catch (Exception e) { LogWriter.error(e); }
			} // set
			else if (subgui.getId() == 2) {
				if (gameData == null || !data.containsKey(scroll.getSelected())) {
					return;
				}
				int id = data.get(scroll.getSelected());
				for (int i = 0; i < gameData.getCompoundTag("GameData").getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = gameData.getCompoundTag("GameData").getTagList("MarketData", 10).getCompoundTagAt(i);
					if (id != nbt.getInteger("MarketID")) {
						continue;
					}
					nbt.setInteger("Slot", subgui.getTextField(0).getInteger());
					break;
				}
				setGuiData(gameData);
			} // change market slot
			return;
		}
		if (subgui instanceof SubGuiDataSend) { Client.sendData(EnumPacketServer.PlayerDataCleaning, ((SubGuiDataSend) subgui).time); }
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (subgui != null && subgui instanceof SubGuiDataSend) {
			((SubGuiDataSend) subgui).unFocused(textField);
			return;
		}
		if (textField.getID() != 1 || gameData == null || !textField.isLong()) {
			return;
		}
		gameData.getCompoundTag("GameData").setLong("Money", textField.getLong());
	}

}
