package noppes.npcs.client.gui.global;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiDataSend;
import noppes.npcs.client.gui.SubGuiEditText;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
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

public class GuiNpcManagePlayerData extends GuiNPCInterface2
		implements ISubGuiListener, IScrollData, ICustomScrollListener, GuiYesNoCallback, IGuiData, ITextfieldListener {

	public HashMap<String, Integer> data = new HashMap<>();
	public HashMap<String, String> scrollData = new HashMap<>();
	private boolean isOnline;
	private GuiCustomScroll scroll;
	public String search, selected, selectedPlayer;
	public EnumPlayerData selection;
	private NBTTagCompound gameData;

	public GuiNpcManagePlayerData(EntityNPCInterface npc) {
		super(npc);
		this.isOnline = false;
		this.selectedPlayer = null;
		this.selected = null;
		this.selection = EnumPlayerData.Players;
		this.search = "";
		this.gameData = null;
		Client.sendData(EnumPacketServer.PlayerDataGet, this.selection);
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		int id = button.id;
		GuiNpcTextField.unfocus();
		String title = this.selectedPlayer;
		switch (this.selection) {
		case Quest:
			title = new TextComponentTranslation("quest.quest").getFormattedText();
			break;
		case Dialog:
			title = new TextComponentTranslation("dialog.dialog").getFormattedText();
			break;
		case Transport:
			title = new TextComponentTranslation("global.transport").getFormattedText();
			break;
		case Bank:
			title = new TextComponentTranslation("global.banks").getFormattedText();
			break;
		case Factions:
			title = new TextComponentTranslation("menu.factions").getFormattedText();
			break;
		case Game:
			title = new TextComponentTranslation("gui.game").getFormattedText();
			break;
		default: {
			break;
		}
		}
		if (id == 0) { // del
			if (this.selection == EnumPlayerData.Players || !this.scroll.hasSelected()) {
				GuiYesNo guiyesno = new GuiYesNo(this,
						new TextComponentTranslation("global.playerdata").getFormattedText() + ": " + title,
						new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
				this.displayGuiScreen(guiyesno);
			} else {
				Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 1,
						this.data.get(this.scrollData.get(this.scroll.getSelected())));
			}
		} else if (id >= 1 && id <= 6 || id == 9) {
			if (this.selectedPlayer == null && id != 1) {
				return;
			}
			if (this.selection == EnumPlayerData.Game) {
				this.save();
			}
			if (id == 9) {
				this.selection = EnumPlayerData.Game;
			} else {
				this.selection = EnumPlayerData.values()[id - 1];
			}
			this.initButtons();
			this.scroll.clear();
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataGet, this.selection, this.selectedPlayer);
			this.selected = null;
			this.getTextField(0).setText("");
		} else if (id == 7) {
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("gui.wipe").getFormattedText() + "?",
					new TextComponentTranslation("data.hover.wipe").getFormattedText().replace("<br>",
							"" + ((char) 10)),
					7);
			this.displayGuiScreen(guiyesno);
		} else if (id == 12) {
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("gui.cleaning").getFormattedText() + "?",
					new TextComponentTranslation("data.hover.cleaning").getFormattedText().replace("<br>",
							"" + ((char) 10)),
					12);
			this.displayGuiScreen(guiyesno);
		} else if (id == 8) { // Add
			SubGuiEditText subgui = new SubGuiEditText(0, "");
			subgui.lable = "gui.add";
			switch (this.selection) {
			case Quest: {
				subgui.hovers[0] = "";
				for (int i : QuestController.instance.quests.keySet()) {
					if (this.data.containsValue(i)) {
						continue;
					}
					if (!subgui.hovers[0].isEmpty()) {
						subgui.hovers[0] += ", ";
					}
					subgui.hovers[0] += i;
				}
				subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>"
						+ subgui.hovers[0];
				break;
			}
			case Dialog: {
				subgui.hovers[0] = "";
				for (int i : DialogController.instance.dialogs.keySet()) {
					if (this.data.containsValue(i)) {
						continue;
					}
					if (!subgui.hovers[0].isEmpty()) {
						subgui.hovers[0] += ", ";
					}
					subgui.hovers[0] += i;
				}
				subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>"
						+ subgui.hovers[0];
				break;
			}
			case Transport: {
				break;
			}
			case Bank: {
				subgui.hovers[0] = "";
				for (int i : BankController.getInstance().banks.keySet()) {
					if (this.data.containsValue(i)) {
						continue;
					}
					if (!subgui.hovers[0].isEmpty()) {
						subgui.hovers[0] += ", ";
					}
					subgui.hovers[0] += i;
				}
				subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>"
						+ subgui.hovers[0];
				break;
			}
			case Factions: {
				subgui.hovers[0] = "";
				for (int i : FactionController.instance.factions.keySet()) {
					if (this.data.containsValue(i)) {
						continue;
					}
					if (!subgui.hovers[0].isEmpty()) {
						subgui.hovers[0] += ", ";
					}
					subgui.hovers[0] += i;
				}
				subgui.hovers[0] = new TextComponentTranslation("gui.options").getFormattedText() + " ID:<br>"
						+ ((char) 167) + "6" + subgui.hovers[0];
				break;
			}
			default: {
				return;
			}
			}
			this.setSubGui(subgui);
		} else if (id == 10) { // del all
			GuiYesNo guiyesno = new GuiYesNo(this,
					new TextComponentTranslation("global.playerdata").getFormattedText() + ": " + title,
					new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 1);
			this.displayGuiScreen(guiyesno);
		} else if (id == 11) { // edit
			this.editData();
		}
	}

	public void confirmClicked(boolean result, int id) {
		String sel = this.selected;
		String player = this.selectedPlayer;
		EnumPlayerData epd = this.selection;
		NoppesUtil.openGUI(this.player, this);
		this.selected = sel;
		this.selectedPlayer = player;
		this.selection = epd;
		if (!result) {
			return;
		}
		if (id == 0) {
			if (this.selection != EnumPlayerData.Players) {
				return;
			}
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, this.selection, this.selectedPlayer, this.selected);
			this.selected = null;
			this.selectedPlayer = null;
			this.scroll.selected = -1;
			this.initButtons();
		} else if (id == 1) {
			Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 3, -1);
		} else if (id == 7) {
			this.selection = EnumPlayerData.Wipe;
			this.initButtons();
			this.scroll.clear();
			this.data.clear();
			Client.sendData(EnumPacketServer.PlayerDataRemove, this.selection, this.selectedPlayer, this.selected);
			this.selected = null;
			this.selectedPlayer = null;
			this.scroll.selected = -1;
		} else if (id == 12) {
			SubGuiDataSend subgui = new SubGuiDataSend(0);
			this.setSubGui(subgui);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.list").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.quests").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.dialogs").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.transports").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.banks").getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.factions").getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.wipe").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.add").getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.game").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete.all").getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.edit").getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.cleaning").getFormattedText());
		} else if (this.getTextField(0) != null && this.getTextField(0).getVisible()
				&& this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.found").getFormattedText());
		} else if (this.getTextField(1) != null && this.getTextField(1).getVisible()
				&& this.getTextField(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("data.hover.money", "" + Long.MAX_VALUE).getFormattedText());
		} else if (this.getLabel(3) != null && this.getLabel(3).enabled && this.getLabel(3).hovered) {
			this.setHoverText(new TextComponentTranslation("data.hover.markets").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	private void editData() {
		if (!this.scroll.hasSelected()) {
			return;
		}
		switch (this.selection) {
			case Bank: {
				Client.sendData(EnumPacketServer.BankShow, this.selection, this.selectedPlayer,
						this.data.get(this.scrollData.get(this.scroll.getSelected())));
				break;
			}
			case Factions: {
				int factionId = this.data.get(this.scrollData.get(this.scroll.getSelected()));
				SubGuiEditText subgui = new SubGuiEditText(1, "");
				Faction f = FactionController.instance.factions.get(factionId);
				String v = this.scroll.hoversTexts[this.scroll.selected][0];
				int value = -1;
				try {
					value = Integer.parseInt(v.substring(v.indexOf(((char) 167) + "3") + 2));
				} catch (Exception e) { LogWriter.error("Error:", e); }
				if (f != null) {
					subgui.numbersOnly = new int[] { 0, f.friendlyPoints * 2, value };
				} else {
					subgui.numbersOnly = new int[] { 0, Integer.MAX_VALUE, value };
				}
				subgui.text[0] = "" + value;
				subgui.lable = "gui.set.new.value";
				this.setSubGui(subgui);
				break;
			}
			case Game: {
				if (this.gameData == null || !this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				SubGuiEditText subgui = new SubGuiEditText(2, "");
				subgui.initGui();
				subgui.getTextField(0).setNumbersOnly();
				int m = 3, s = 0, id = this.data.get(this.scroll.getSelected());
				MarcetController mData = MarcetController.getInstance();
				for (int i = 0; i < this.gameData.getCompoundTag("GameData").getTagList("MarketData", 10).tagCount(); i++) {
					NBTTagCompound nbt = this.gameData.getCompoundTag("GameData").getTagList("MarketData", 10)
							.getCompoundTagAt(i);
					if (id != nbt.getInteger("MarketID")) {
						continue;
					}
					s = nbt.getInteger("Slot");
					Marcet marcet = (Marcet) mData.getMarcet(id);
					if (marcet == null) {
						break;
					}
					m = marcet.markup.size() - 1;
					break;
				}
				subgui.text[0] = "" + s;
				subgui.getTextField(0).setMinMaxDefault(0, m, s);
				subgui.lable = "gui.set.new.value";
				this.setSubGui(subgui);
			}
			default: {
			}
		}
	}

	public void initButtons() {
		boolean hasPlayer = this.selectedPlayer != null && !this.selectedPlayer.isEmpty();
		this.getButton(0).setVisible(true);
		this.getButton(0).setEnabled(hasPlayer);
		this.getButton(1).setEnabled(this.selection != EnumPlayerData.Players && hasPlayer);
		this.getButton(2).setEnabled(this.selection != EnumPlayerData.Quest && hasPlayer);
		this.getButton(3).setEnabled(this.selection != EnumPlayerData.Dialog && hasPlayer);
		this.getButton(4).setEnabled(this.selection != EnumPlayerData.Transport && hasPlayer);
		this.getButton(5).setEnabled(this.selection != EnumPlayerData.Bank && hasPlayer);
		this.getButton(6).setEnabled(this.selection != EnumPlayerData.Factions && hasPlayer);
		this.getButton(9).setEnabled(this.selection != EnumPlayerData.Game && hasPlayer);
		boolean canEdit = this.selection != EnumPlayerData.Players && this.selection != EnumPlayerData.Wipe;
		this.getButton(8).setVisible(true);
		this.getButton(10).setVisible(true);
		this.getButton(11).setVisible(true);
		this.getButton(12).setEnabled(this.selection == EnumPlayerData.Players);

		if (this.scroll != null) {
			if (this.selection != EnumPlayerData.Game) {
				this.scroll.guiLeft = this.guiLeft + 7;
				this.scroll.guiTop = this.guiTop + 16;
				this.scroll.setSize(300, 152);
			} else {
				this.scroll.guiLeft = this.guiLeft + 7;
				this.scroll.guiTop = this.guiTop + 52;
				this.scroll.setSize(120, 138);
			}
		}
		this.getLabel(1).enabled = this.selection != EnumPlayerData.Game;
		this.getTextField(0).setVisible(this.selection != EnumPlayerData.Game);
		if (this.getLabel(2) != null) {
			this.getLabel(2).enabled = this.selection == EnumPlayerData.Game;
		}
		if (this.getLabel(3) != null) {
			this.getLabel(3).enabled = this.selection == EnumPlayerData.Game;
		}
		if (this.getTextField(1) != null) {
			this.getTextField(1).setVisible(this.selection == EnumPlayerData.Game);
		}

		switch (this.selection) {
			case Quest:
				case Dialog:
				case Transport: {
				this.getButton(8).setEnabled(canEdit && hasPlayer);
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(false);
				break;
			}
				case Bank: {
				this.getButton(8)
						.setEnabled(canEdit && hasPlayer && this.data.size() < BankController.getInstance().banks.size());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(canEdit && hasPlayer && this.scroll != null && this.scroll.hasSelected());
				break;
			}
			case Factions: {
				this.getButton(8)
						.setEnabled(canEdit && hasPlayer && this.data.size() < FactionController.instance.factions.size());
				this.getButton(10).setEnabled(canEdit && hasPlayer && !this.scroll.getList().isEmpty());
				this.getButton(11).setEnabled(canEdit && hasPlayer && this.scroll != null && this.scroll.hasSelected());
				break;
			}
			case Game: {
				this.getButton(0).setEnabled(canEdit && hasPlayer && this.scroll.hasSelected());
				this.getButton(8).setVisible(false);
				this.getButton(10).setEnabled(canEdit && hasPlayer && this.gameData != null);
				this.getButton(11).setEnabled(canEdit && hasPlayer && this.scroll.hasSelected());
				break;
			}
			default: {
				this.getButton(8).setVisible(false);
				this.getButton(10).setVisible(false);
				this.getButton(11).setVisible(false);
			}
		}
		if (!hasPlayer) {
			this.getLabel(0).setLabel(new TextComponentTranslation("data.all.players").getFormattedText() + " ("
					+ (this.scroll.getList() == null ? "1" : this.scroll.getList().size()) + ")");
		} else {
			this.getLabel(0)
					.setLabel(new TextComponentTranslation("data.sel.player",
							((char) 167) + (this.isOnline ? "2" : "4") + ((char) 167) + "l" + this.selectedPlayer)
									.getFormattedText()
							+ ((char) 167) + "r ("
							+ (this.scroll.getList() == null ? "1" : this.scroll.getList().size()) + ")");
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setSize(300, 152);
		}
		this.scroll.guiLeft = this.guiLeft + 7;
		this.scroll.guiTop = this.guiTop + 16;
		this.addScroll(this.scroll);
		this.selected = null;
		this.addLabel(new GuiNpcLabel(0, "data.all.players", this.guiLeft + 10, this.guiTop + 6));
		int x = this.guiLeft + 313, y = this.guiTop + 16, w = 99;
		this.addButton(new GuiNpcButton(1, x, y, w, 20, "playerdata.players"));
		this.addButton(new GuiNpcButton(2, x, (y += 22), w, 20, "quest.quest"));
		this.addButton(new GuiNpcButton(3, x, (y += 22), w, 20, "dialog.dialog"));
		this.addButton(new GuiNpcButton(4, x, (y += 22), w, 20, "global.transport"));
		this.addButton(new GuiNpcButton(5, x, (y += 22), w, 20, "global.banks"));
		this.addButton(new GuiNpcButton(6, x, (y += 22), w, 20, "menu.factions"));
		this.addButton(new GuiNpcButton(9, x, (y += 22), w, 20, "gui.game"));
		this.addButton(new GuiNpcButton(7, x, (y += 22), w, 20, "gui.wipe"));
		this.addButton(new GuiNpcButton(12, x, y + 22, w, 20, "gui.cleaning"));
		y = this.guiTop + 170;
		this.addLabel(new GuiNpcLabel(1, "gui.found", this.guiLeft + 10, y + 5));
		this.addTextField(new GuiNpcTextField(0, this, this.fontRenderer, this.guiLeft + 66, y, 240, 20, this.search));
		x = this.guiLeft + 7;
		w = 73;
		this.addButton(new GuiNpcButton(8, x, (y += 22), w, 20, "gui.add"));
		this.addButton(new GuiNpcButton(0, (x += w + 3), y, w, 20, "gui.remove"));
		this.addButton(new GuiNpcButton(10, (x += w + 3), y, w, 20, "gui.remove.all"));
		this.addButton(new GuiNpcButton(11, x + w + 3, y, w, 20, "selectServer.edit"));
		this.initButtons();
		if (this.selection == EnumPlayerData.Game) {
			y = this.guiTop + 18;
			this.addLabel(new GuiNpcLabel(2, "gui.money", this.guiLeft + 10, y + 5));
			this.addTextField(new GuiNpcTextField(1, this, this.fontRenderer, this.guiLeft + 66, y, 120, 20,
					"" + this.gameData.getLong("Money")));
			this.getTextField(1).setNumbersOnly();
			this.getTextField(1).setMinMaxDefault(0, Long.MAX_VALUE, this.gameData.getLong("Money"));
			this.addLabel(new GuiNpcLabel(3, new TextComponentTranslation("global.market").getFormattedText() + ":",
					this.guiLeft + 10, y + 25));
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && this.subgui == null) {
			this.save();
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.MainMenuGlobal);
			return;
		}
		super.keyTyped(c, i);
		if (this.selection == EnumPlayerData.Wipe) {
			return;
		}
		if (this.search.equals(this.getTextField(0).getText())) {
			return;
		}
		this.search = this.getTextField(0).getText().toLowerCase();
		this.setCurrentList();
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (k == 0 && this.scroll != null) {
			this.scroll.mouseClicked(i, j, k);
		}
	}

	@Override
	public void save() {
		ContainerNPCBank.editPlayerBankData = null;
		if (this.selection == EnumPlayerData.Game) {
			boolean hasPlayer = this.selectedPlayer != null && !this.selectedPlayer.isEmpty();
			if (hasPlayer && this.gameData != null) {
				Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 0,
						this.gameData);
			}
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll guiCustomScroll) {
		this.selected = guiCustomScroll.getSelected();
		if (this.selection == EnumPlayerData.Players) {
			this.selectedPlayer = this.selected;
			this.isOnline = this.data.get(this.selected) == 1;
		}
		this.initButtons();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		this.editData();
	}

	private void setCurrentList() {
		if (this.scroll == null) {
			return;
		}
		List<String> list = Lists.newArrayList();
		List<String> hovers = Lists.newArrayList();
		List<String> suffixs = Lists.newArrayList();
		List<Integer> colors = Lists.newArrayList();
		if (this.selection == EnumPlayerData.Wipe) {
			this.selection = EnumPlayerData.Players;
		}
		switch (this.selection) {
		case Players: {
			List<String> listOn = Lists.newArrayList(), listOff = Lists.newArrayList();
			for (String name : this.data.keySet()) {
				if (this.search.isEmpty() || name.toLowerCase().contains(this.search)) {
					if (this.data.get(name) == 1) {
						listOn.add(name);
					} else {
						listOff.add(name);
					}
				}
			}
			Collections.sort(listOn);
			Collections.sort(listOff);
			list = listOn;
            list.addAll(listOff);
			for (String name : list) {
				suffixs.add(new TextComponentTranslation(this.data.get(name) == 0 ? "gui.offline" : "gui.online")
						.getFormattedText());
			}
			break;
		}
		case Quest: {
			Map<String, Map<Integer, String>> mapA = Maps.newTreeMap();
			Map<String, Map<Integer, String>> mapF = Maps.newTreeMap();
			for (String str : this.data.keySet()) {
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
				if (!map.containsKey(cat)) {
					map.put(cat, Maps.newTreeMap());
				}
				map.get(cat).put(this.data.get(str), name);
			}
			for (String cat : mapA.keySet()) {
				ITextComponent sfx = new TextComponentTranslation("availability.active");
				sfx.getStyle().setColor(TextFormatting.GREEN);
				for (int id : mapA.get(cat).keySet()) {
					suffixs.add(sfx.getFormattedText());
					String key = ((char) 167) + "aID:" + id + ((char) 167) + "7 " + cat + ": \"" + ((char) 167) + "r"
							+ mapA.get(cat).get(id) + ((char) 167) + "7\"";
					list.add(key);
					for (String str : this.data.keySet()) {
						if (this.data.get(str) == id) {
							this.scrollData.put(key, str);
							break;
						}
					}
				}
			}
			for (String cat : mapF.keySet()) {
				ITextComponent sfx = new TextComponentTranslation("quest.complete");
				sfx.getStyle().setColor(TextFormatting.LIGHT_PURPLE);
				for (int id : mapF.get(cat).keySet()) {
					suffixs.add(sfx.getFormattedText());
					String key = ((char) 167) + "dID:" + id + ((char) 167) + "7 " + cat + ": \"" + ((char) 167) + "r"
							+ mapF.get(cat).get(id) + ((char) 167) + "7\"";
					list.add(key);
					for (String str : this.data.keySet()) {
						if (this.data.get(str) == id) {
							this.scrollData.put(key, str);
							break;
						}
					}
				}
			}
			break;
		}
		case Dialog: {
			Map<Integer, String> map = Maps.newTreeMap();
			for (String str : this.data.keySet()) {
				map.put(this.data.get(str), ((char) 167) + "7ID:" + this.data.get(str) + " " + str.replace(": ", ": " + ((char) 167) + "r"));
			}
			for (int id : map.keySet()) {
				list.add(map.get(id));
				for (String str : this.data.keySet()) {
					if (this.data.get(str) == id) {
						this.scrollData.put(map.get(id), str);
						break;
					}
				}
			}
			break;
		}
		case Transport: {
			Map<Integer, String> map = Maps.newTreeMap();
			for (String str : this.data.keySet()) {
				map.put(this.data.get(str), ((char) 167) + "7" + str.replace(": ", ": " + ((char) 167) + "r"));
			}
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
				for (String str : this.data.keySet()) {
					if (this.data.get(str) == id) {
						this.scrollData.put(map.get(id), str);
						break;
					}
				}
			}
			break;
		}
		case Bank: {
			for (String str : this.data.keySet()) {
				list.add(str);
				hovers.add("ID: " + this.data.get(str));
				this.scrollData.put(str, str);
			}
			Collections.sort(list);
			break;
		}
		case Factions: {
			Map<String, String> mapH = Maps.newHashMap();
			Map<String, Integer> mapC = Maps.newHashMap();
			this.scrollData.clear();
			for (String str : this.data.keySet()) {
				if (this.search.isEmpty() || str.toLowerCase().contains(this.search)) {
					String[] l = str.split(";");
					String key = new TextComponentTranslation(l[0]).getFormattedText() + ((char) 167) + "7 (ID:"
							+ this.data.get(str) + ")";
					list.add(key);
					int value = -1;
					try {
						value = Integer.parseInt(l[1]);
					} catch (Exception e) { LogWriter.error("Error:", e); }
					this.scrollData.put(key, str);

					int color = 0xFFFFFF;
					String hover = new TextComponentTranslation("type.value").getFormattedText() + ": " + ((char) 167)
							+ "3" + value;
					Faction f = FactionController.instance.factions.get(this.data.get(str));
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
		default: {
			return;
		}
		}
		this.scroll.setListNotSorted(list);
		this.scroll.hoversTexts = null;
		this.scroll.setSuffixes(null);
		if (!hovers.isEmpty()) {
			this.scroll.hoversTexts = new String[hovers.size()][];
			int i = 0;
			for (String str : hovers) {
				this.scroll.hoversTexts[i] = str.split("<br>");
				i++;
			}
		}
		if (!suffixs.isEmpty()) {
			this.scroll.setSuffixes(suffixs);
		}
		this.scroll.setColors(null);
		if (!colors.isEmpty()) {
			this.scroll.setColors(colors);
		}
	}

	@Override
	public void setData(Vector<String> list, HashMap<String, Integer> data) {
		if (this.selection == EnumPlayerData.Game) {
			return;
		}
		this.data.clear();
		this.data.putAll(data);
		this.setCurrentList();
		if (this.selection == EnumPlayerData.Players && this.selectedPlayer != null) {
			this.scroll.setSelected(this.selectedPlayer);
			this.selected = this.selectedPlayer;
		}
		if (this.selection == EnumPlayerData.Wipe) {
			this.selection = EnumPlayerData.Players;
		}
		this.initButtons();
		if (ContainerNPCBank.editPlayerBankData != null) {
			this.buttonEvent(new GuiNpcButton(5, 0, 0, ""));
			ContainerNPCBank.editPlayerBankData = null;
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (this.selection != EnumPlayerData.Game || !compound.hasKey("GameData", 10)) {
			return;
		}
		this.gameData = compound;
		Map<Integer, Integer> map = Maps.newTreeMap();
		for (int i = 0; i < compound.getCompoundTag("GameData").getTagList("MarketData", 10).tagCount(); i++) {
			NBTTagCompound nbt = compound.getCompoundTag("GameData").getTagList("MarketData", 10).getCompoundTagAt(i);
			map.put(nbt.getInteger("MarketID"), nbt.getInteger("Slot"));
		}
		List<String> list = Lists.newArrayList();
		MarcetController mData = MarcetController.getInstance();
		this.scroll.hoversTexts = new String[map.size()][];
		this.data.clear();
		int i = 0;
		for (int id : map.keySet()) {
			String key = ((char) 167) + "7ID:" + id + " "
					+ (new TextComponentTranslation("bank.slot").getFormattedText()) + ((char) 167) + "r: "
					+ map.get(id);
			list.add(key);
			this.data.put(key, id);
			Marcet m = (Marcet) mData.getMarcet(id);
			if (m != null) {
				this.scroll.hoversTexts[i] = new String[] { "ID:" + id + " \"" + m.getName() + "\"",
						(new TextComponentTranslation("gui.max").getFormattedText()) + ": " + (m.markup.size() - 1) };
			} else {
				this.scroll.hoversTexts[i] = new String[] {
						(new TextComponentTranslation("global.market").getFormattedText()) + " - "
								+ (new TextComponentTranslation("quest.notfound").getFormattedText()) };
			}
			i++;
		}
		this.scroll.setListNotSorted(list);
		this.initGui();
	}

	@Override
	public void setSelected(String selected) {
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiEditText) {
			if (subgui.id == 0) { // add
				try {
					Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 0, Integer.parseInt(((SubGuiEditText) subgui).text[0]));
				} catch (Exception e) { LogWriter.error("Error:", e); }
			} else if (subgui.id == 1) { // set
				try {
					Client.sendData(EnumPacketServer.PlayerDataSet, this.selection.ordinal(), this.selectedPlayer, 2, this.data.get(this.scrollData.get(this.scroll.getSelected())), Integer.parseInt(((SubGuiEditText) subgui).text[0]));
				} catch (Exception e) { LogWriter.error("Error:", e); }
			} else if (subgui.id == 2) { // change market slot
				if (this.gameData == null || !this.data.containsKey(this.scroll.getSelected())) {
					return;
				}
				int id = this.data.get(this.scroll.getSelected());
				for (int i = 0; i < this.gameData.getCompoundTag("GameData").getTagList("MarketData", 10)
						.tagCount(); i++) {
					NBTTagCompound nbt = this.gameData.getCompoundTag("GameData").getTagList("MarketData", 10)
							.getCompoundTagAt(i);
					if (id != nbt.getInteger("MarketID")) {
						continue;
					}
					nbt.setInteger("Slot", subgui.getTextField(0).getInteger());
					break;
				}
				this.setGuiData(this.gameData);
			}
			return;
		}
		if (subgui instanceof SubGuiDataSend) {
			Client.sendData(EnumPacketServer.PlayerDataCleaning, ((SubGuiDataSend) subgui).time);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.subgui != null && this.subgui instanceof SubGuiDataSend) {
			this.subgui.unFocused(textField);
			return;
		}
		if (textField.getId() != 1 || this.gameData == null || !textField.isLong()) {
			return;
		}
		this.gameData.getCompoundTag("GameData").setLong("Money", textField.getLong());
	}

}
